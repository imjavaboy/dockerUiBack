package com.gbq.docker.uiproject.service.impl;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.gbq.docker.uiproject.commons.activemq.MQProducer;
import com.gbq.docker.uiproject.commons.activemq.Task;
import com.gbq.docker.uiproject.commons.util.*;
import com.gbq.docker.uiproject.commons.util.jedis.JedisClient;
import com.gbq.docker.uiproject.domain.entity.RepositoryImage;
import com.gbq.docker.uiproject.domain.entity.SysImage;
import com.gbq.docker.uiproject.domain.enums.ImageTypeEnum;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.enums.SysLogTypeEnum;
import com.gbq.docker.uiproject.domain.enums.WebSocketTypeEnum;
import com.gbq.docker.uiproject.domain.vo.HubImageVO;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.exception.CustomException;
import com.gbq.docker.uiproject.mapper.RepositoryImageMapper;
import com.gbq.docker.uiproject.service.NoticeService;
import com.gbq.docker.uiproject.service.RepositoryImageService;
import com.gbq.docker.uiproject.service.SysImageService;
import com.gbq.docker.uiproject.service.SysLogService;
import com.spotify.docker.client.DockerClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/11 12:11
 * @Copyright 总有一天，会见到成功
 */
@Service
@Slf4j
public class RepositoryImageServiceImpl extends ServiceImpl<RepositoryImageMapper, RepositoryImage> implements RepositoryImageService {

    @Resource
    private RepositoryImageMapper repositoryImageMapper;
    @Resource
    private SysImageService sysImageService;
    @Resource
    private DockerClient dockerClient;
    @Resource
    private SysLogService sysLogService;
    @Resource
    private NoticeService noticeService;
    @Resource
    private JedisClient jedisClient;

    @Resource
    private MQProducer mqProducer;

    @Value("${docker.registry.url}")
    private String registryUrl;

    @Value("${redis.repository.image.key}")
    private String key;
    private String ID_PREFIX = "ID:";
    private String NAME_PREFIX = "NAME:";

    @Override
    public List<HubImageVO> listHubImageVO() {
        return repositoryImageMapper.listHubImageVO();
    }

    @Override
    public ResultVO sync() {
        List<RepositoryImage> dbImage = repositoryImageMapper.selectList(new EntityWrapper<>());
        boolean[] dbFlag = new boolean[dbImage.size()];
        Arrays.fill(dbFlag, false);
        int addCount = 0, deleteCount = 0, errorCount = 0;

        try {
            // 2、遍历Hub
            List<String> names = listRepositoryFromHub();
            for(String name : names) {
                List<String> tags = listTagsFromHub(name);
                if(tags != null) {
                    for(String tag : tags) {
                        // 拼接FullName
                        String fullName = registryUrl + "/" + name + ":" + tag;
                        // 判断本地是否存在
                        boolean flag = false;
                        for(int i=0; i < dbFlag.length; i++) {
                            // 跳过验证过的
                            if(dbFlag[i]) {
                                continue;
                            }
                            if(fullName.equals(dbImage.get(i).getFullName())) {
                                dbFlag[i] = true;
                                flag = true;
                                break;
                            }
                        }

                        // 如果不存在，表示数据库中没有该记录，新增记录
                        if(!flag) {
                            RepositoryImage image = imageName2RepositoryImage(fullName);
                            if(image != null) {
                                repositoryImageMapper.insert(image);
                                addCount++;
                            } else {
                                errorCount++;
                            }
                        }
                    }
                }
            }

            // 3、清理失效的记录
            for(int i=0; i<dbFlag.length ;i++) {
                if(!dbFlag[i]) {
                    deleteCount++;
                    repositoryImageMapper.deleteById(dbImage.get(i));
                }
            }

            // 4、准备返回值
            Map<String, Integer> map = new HashMap<>(16);
            map.put("delete", deleteCount);
            map.put("add", addCount);
            map.put("error", errorCount);

            return ResultVOUtils.success(map);
        } catch (Exception e) {
            log.error("读取Hub数据失败，错误位置：{}，错误栈：{}",
                    "RepositoryImageServiceImpl.sync()", HttpClientUtils.getStackTraceAsString(e));

            return ResultVOUtils.error(ResultEnum.NETWORK_ERROR);
        }
    }

    /**
     *  镜像名 --> RepositoryImage
     * @param
     * @since 2022/9/12
     * @return
     */
    private RepositoryImage imageName2RepositoryImage(String name) {
        // 形如： 192.168.100.183:5000/hello-world-1313asfa:latest
        RepositoryImage image = new RepositoryImage();

        // 设置完整名
        image.setFullName(name);

        // 1、获取仓储地址
        int i = name.indexOf("/");
        String url = name.substring(0, i);
        image.setRepo(url);

        // 2、获取tag
        // 1313asfa/hello-world:latest
        String body = name.substring(i+1);
        String[] split = body.split(":");
        // 长度为1或2正常
        if(split.length > 2) {
            return null;
        }
        String tag = split.length == 2 ? split[1] : "latest";
        image.setTag(tag);

        // 3、获取名称和用户ID
        // 1313asfa/hello-world
        body = split[0];
        i = body.indexOf("/");
        image.setName(body);
        image.setUserId(body.substring(0,i));

        // 4、设置Digest
        try {
            String digest = getDigest(body, image.getTag());
            image.setDigest(digest);
        } catch (Exception e) {
            image.setDigest(null);
        }

        return image;
    }


    @Override
    public String getDigest(String name, String tag) throws Exception {
        return DockerRegistryApiUtils.getDigest(registryUrl, name, tag);
    }

    @Override
    public List<String> listTagsFromHub(String fullName) throws Exception {
        return DockerRegistryApiUtils.listTags(registryUrl, fullName);
    }

    @Override
    public List<String> listRepositoryFromHub() throws Exception {
        return DockerRegistryApiUtils.listRepositories(registryUrl);
    }

    @Override
    public ResultVO pushCheck(String sysImageId, String userId) {
        if(StringUtils.isBlank(sysImageId)) {
            return ResultVOUtils.error(ResultEnum.PARAM_ERROR);
        }

        SysImage sysImage = sysImageService.getById(sysImageId);
        if(sysImage == null) {
            return ResultVOUtils.error(ResultEnum.IMAGE_NOT_EXIST);
        }

        // 判断镜像类型
        if(ImageTypeEnum.LOCAL_USER_IMAGE.getCode() != sysImage.getType()) {
            return ResultVOUtils.error(ResultEnum.PUBLIC_IMAGE_UPLOAD_ERROR);
        }
        // 判断镜像所属
        if(!userId.equals(sysImage.getUserId())) {
            return ResultVOUtils.error(ResultEnum.PERMISSION_ERROR);
        }

        // 判断是否存在
        // 命名规则：registryUrl/userId/name:tag
        String newName = registryUrl + "/" + userId + "/" + sysImage.getName() + ":" + sysImage.getTag();
        if(hasExist(newName)) {
            return ResultVOUtils.error(ResultEnum.IMAGE_UPLOAD_ERROR_BY_EXIST);
        }

        return ResultVOUtils.success(sysImage);
    }
    @Override
    public Boolean hasExist(String fullName) {
        List<RepositoryImage> list = repositoryImageMapper.selectList(
                new EntityWrapper<RepositoryImage>().eq("full_name", fullName));

        RepositoryImage repositoryImage = CollectionUtils.getListFirst(list);

        return repositoryImage != null;
    }

    @Async("taskExecutor")
    @Transactional(rollbackFor = CustomException.class)
    @Override
    public void pushTask(SysImage sysImage, String userId, HttpServletRequest request) {
        try {
            // 1、创建镜像tag
            String newName = registryUrl + "/" + userId + "/" + sysImage.getName() + ":" + sysImage.getTag();
            dockerClient.tag(sysImage.getFullName(),newName);
            // 2、上传镜像
            dockerClient.push(newName);
            // 3、删除镜像tag
//            dockerClient.removeImage(newName);
            // 4、保存数据库
            RepositoryImage image = imageName2RepositoryImage(newName);
            repositoryImageMapper.insert(image);
            // 5、清理缓存
            cleanCache(null, image.getName());
            // 写入日志
            sysLogService.saveLog(request, SysLogTypeEnum.PUSH_IMAGE_TO_HUB,null);

            // 发送通知
            List<String> receiverList = new ArrayList<>();
            receiverList.add(userId);
            noticeService.sendUserTask("推送Hub镜像","推送镜像【"+sysImage.getName()+"】成功", 4, false, receiverList, userId);

            sendMQ(userId, image.getId(), ResultVOUtils.successWithMsg("镜像上传成功"));
        } catch (Exception e) {
            log.error("上传镜像失败，错误位置：{}，错误栈：{}",
                    "RepositoryImageServiceImpl.pushTask()", HttpClientUtils.getStackTraceAsString(e));
            // 写入日志
            sysLogService.saveLog(request, SysLogTypeEnum.PUSH_IMAGE_TO_HUB,e);

            // 发送通知
            List<String> receiverList = new ArrayList<>();
            receiverList.add(userId);
            noticeService.sendUserTask("推送Hub镜像","推送镜像【"+sysImage.getName()+"】失败,Docker推送异常", 4, false, receiverList, userId);

            sendMQ(userId, null, ResultVOUtils.error(ResultEnum.PUSH_ERROR));
        }
    }

    public void cleanCache(String id, String name) {
        try {
            if(StringUtils.isNotBlank(id)) {
                jedisClient.hdel(key, ID_PREFIX + id);
            }
            if(StringUtils.isNotBlank(name)) {
                jedisClient.hdel(key, NAME_PREFIX + name);
            }
        } catch (Exception e) {
            log.error("清理缓存失败，错误位置：{}，目标id：{}，目标name：{}",
                    "RepositoryImageServiceImpl.cleanCache()", id, name);
        }
    }
   /**
    *  发送Hub镜像消息
    * @param
    * @since 2022/9/13
    * @return
    */
    private void sendMQ(String userId, String imageId, ResultVO resultVO) {
        Destination destination = new ActiveMQQueue("MQ_QUEUE_HUB_IMAGE");
        Task task = new Task();

        Map<String, Object> data = new HashMap<>(16);
        data.put("type", WebSocketTypeEnum.HUB_IMAGE.getCode());
        data.put("imageId", imageId);
        // 获取暴露端口
        ResultVO resultVO1 = sysImageService.listExportPort(imageId, userId);
        if(ResultEnum.OK.getCode() == resultVO1.getCode()) {
            data.put("exportPort", resultVO1.getData());
        }

        resultVO.setData(data);

        Map<String,String> map = new HashMap<>(16);
        map.put("uid",userId);
        map.put("data", JsonUtils.objectToJson(resultVO));
        task.setData(map);

        mqProducer.send(destination, JsonUtils.objectToJson(task));
    }

    @Override
    public List<RepositoryImage> listByName(String name) {
        String field = NAME_PREFIX + name;
        try {
            String json = jedisClient.hget(key, field);
            if(StringUtils.isNotBlank(json)) {
                return JsonUtils.jsonToList(json, RepositoryImage.class);
            }
        } catch (Exception e) {
            log.error("缓存读取异常，错误位置：RepositoryImageServiceImpl.listByName()");
        }

        List<RepositoryImage> list = repositoryImageMapper.selectList(
                new EntityWrapper<RepositoryImage>().eq("name", name));
        if(list == null) {
            return null;
        }

        try {
            jedisClient.hset(key, field, JsonUtils.objectToJson(list));
        } catch (Exception e) {
            log.error("缓存存储异常，错误位置：RepositoryImageServiceImpl.listByName()");
        }

        return list;
    }
}
