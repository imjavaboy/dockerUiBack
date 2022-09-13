package com.gbq.docker.uiproject.service.impl;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.gbq.docker.uiproject.commons.activemq.MQProducer;
import com.gbq.docker.uiproject.commons.activemq.Task;
import com.gbq.docker.uiproject.commons.util.*;
import com.gbq.docker.uiproject.commons.util.jedis.JedisClient;
import com.gbq.docker.uiproject.domain.dto.SysImageDTO;
import com.gbq.docker.uiproject.domain.entity.SysImage;
import com.gbq.docker.uiproject.domain.entity.SysLogin;
import com.gbq.docker.uiproject.domain.enums.*;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.exception.CustomException;
import com.gbq.docker.uiproject.exception.JsonException;
import com.gbq.docker.uiproject.mapper.SysImageMapper;
import com.gbq.docker.uiproject.service.NoticeService;
import com.gbq.docker.uiproject.service.SysImageService;
import com.gbq.docker.uiproject.service.SysLogService;
import com.gbq.docker.uiproject.service.SysLoginService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerRequestException;
import com.spotify.docker.client.exceptions.DockerTimeoutException;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.ImageHistory;
import com.spotify.docker.client.messages.ImageInfo;
import com.spotify.docker.client.messages.ImageSearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/9 11:43
 * @Copyright 总有一天，会见到成功
 */
@Service
@Slf4j
public class SysImageServiceImpl extends ServiceImpl<SysImageMapper, SysImage> implements SysImageService {
    @Resource
    private SysImageMapper imageMapper;
    @Resource
    private SysLoginService loginService;
    @Resource
    private DockerClient dockerClient;
    @Resource
    private JedisClient jedisClient;
    @Resource
    private SysLogService sysLogService;
    @Resource
    private MQProducer mqProducer;
    @Resource
    private NoticeService noticeService;

    @Value("${docker.server.url}")
    private String serverUrl;


    @Value("${redis.local-image.key}")
    private String key;
    private final String FULL_NAME_PREFIX = "FULL_NAME:";
    private final String ID_PREFIX = "ID:";
    @Override
    public Page<SysImageDTO> listLocalPublicImage(String name, Page<SysImageDTO> page) {
       List<SysImageDTO> list =  imageMapper.listLocalPublicImage(page,name);


        return page.setRecords(list);
    }

    @Override
    public Page<SysImageDTO> listLocalUserImage(String name, boolean filterOpen, String uid, Page<SysImageDTO> page) {
        List<SysImageDTO> images;
        if(filterOpen) {
            List<SysImage> imageList = imageMapper.selectList(new EntityWrapper<SysImage>()
                    .eq("type", 2)
                    .and().eq("user_id",uid).or().eq("has_open", true));
            images = sysImage2DTO(imageList);

        } else {
            images = imageMapper.listLocalUserImage(page, name);
        }

        return page.setRecords(images);
    }
    private List<SysImageDTO> sysImage2DTO(List<SysImage> list) {
        return list.stream().map(this::sysImage2DTO).collect(Collectors.toList());
    }

    private SysImageDTO sysImage2DTO(SysImage sysImage){
        SysImageDTO dto = new SysImageDTO();
        BeanUtils.copyProperties(sysImage, dto);

        SysLogin login = loginService.getById(sysImage.getId());
        if(login != null) {
            dto.setUsername(login.getUsername());
        }
        return dto;
    }

    @Override
    public ResultVO listHubImage(String name, int limit) {
        if (StringUtils.isBlank(name)) {
            return ResultVOUtils.error(ResultEnum.PARAM_ERROR);
        }

        try {
            List<ImageSearchResult> results = dockerClient.searchImages(name);
            return ResultVOUtils.success(results);
        } catch (DockerRequestException requestException){
            return ResultVOUtils.error(
                    ResultEnum.SERVICE_CREATE_ERROR.getCode(),
                    HttpClientUtils.getErrorMessage(requestException.getMessage()));
        } catch (Exception e) {
            log.error("Docker搜索异常，错误位置：SysImageServiceImpl.listHubImage,出错信息：" + HttpClientUtils.getStackTraceAsString(e));
            return ResultVOUtils.error(ResultEnum.DOCKER_EXCEPTION);
        }
    }

    @Override
    public Page<SysImage> selfImage(String uid, Page<SysImage> page) {
        List<SysImage> records = imageMapper.listSelfImage(uid, page);

        return page.setRecords(records);
    }


    @Override
    public ResultVO pullImageCheck(String imageName, String uid) {
        if(StringUtils.isBlank(imageName)) {
            return ResultVOUtils.error(ResultEnum.PARAM_ERROR);
        }
        //若用户未输入版本号 则默认pull最新的版本
        if (!imageName.contains(":")) {
            imageName = imageName + ":latest";
        }

        AtomicBoolean flag = new AtomicBoolean(false);
        try {
            String finalImageName = imageName;
            dockerClient.listImages().stream().forEach(image -> {
                if(((Image)image).repoTags().contains(finalImageName)){
                    flag.set(true);
                }
            });
            if (flag.get()){
                return ResultVOUtils.error(ResultEnum.PULL_ERROR_BY_EXIST);
            }
            if(getByFullName(imageName) != null) {
                sync();
            }
        } catch (DockerException | InterruptedException e) {
            log.error("查询本地镜像失败，错误位置：{}，镜像名：{}，错误栈：{}",
                    "SysImageServiceImpl.pullImageFromHub()", imageName, HttpClientUtils.getStackTraceAsString(e));
            return ResultVOUtils.error(ResultEnum.DOCKER_EXCEPTION);
        }
        return ResultVOUtils.success();
    }

    /**
     *  同步镜像（数据库和本地）
     * @param
     * @since 2022/9/11
     * @return
     */
    @Transactional(rollbackFor = CustomException.class)
    @Override
    public ResultVO sync() {
        try {
            // 获取数据库中所有镜像
            List<SysImage> dbImages = imageMapper.selectList(new EntityWrapper<>());
            // 获取本地所有镜像
            List<Image> tmps = dockerClient.listImages(DockerClient.ListImagesParam.digests());

            int deleteCount = 0,addCount = 0,errorCount=0;
            boolean[] dbFlag = new boolean[dbImages.size()];
            Arrays.fill(dbFlag,false);

            // 遍历本地镜像
            for(int i=0; i<tmps.size(); i++) {
                Image image = tmps.get(i);
                // 读取所有Tag
                ImmutableList<String> list = image.repoTags();
                if(list != null) {
                    for(String tag : list) {
                        // 判断tag是否存在
                        boolean flag = false;
                        for(int j=0; j<dbImages.size(); j++) {
                            // 跳过比较过的
                            if(dbFlag[j]) {
                                continue;
                            }
                            // 比较相等
                            if(tag.equals(dbImages.get(j).getFullName())) {
                                flag = true;
                                dbFlag[j] = true;
                                break;
                            }
                        }

                        // 如果本地不存在，添加到本地
                        if(!flag) {
                            SysImage sysImage = imageToSysImage(image, tag);
                            if(sysImage == null) {
                                errorCount++;
                            } else {
                                addCount++;
                                imageMapper.insert(sysImage);
                            }
                        }
                    }
                }
            }

            // 删除失效的数据
            for(int i=0; i<dbFlag.length;i++) {
                if(!dbFlag[i]) {
                    deleteCount++;
                    SysImage sysImage = dbImages.get(i);
                    imageMapper.deleteById(sysImage);
                    // 更新缓存
                    cleanCache(sysImage.getId(), sysImage.getFullName());
                }
            }

            // 准备结果
            Map<String, Integer> map = new HashMap<>(16);
            map.put("delete", deleteCount);
            map.put("add", addCount);
            map.put("error", errorCount);

            return ResultVOUtils.success(map);
        } catch (DockerTimeoutException te) {
            log.error("同步镜像超时，错误位置：{}","SysImageServiceImpl.sync");
            return ResultVOUtils.error(ResultEnum.DOCKER_TIMEOUT);
        }  catch (Exception e) {
            log.error("Docker同步镜像异常，错误位置：{},错误栈：{}",
                    "SysImageServiceImpl.sync", HttpClientUtils.getStackTraceAsString(e));

            return ResultVOUtils.error(ResultEnum.DOCKER_EXCEPTION);
        }
    }

    private SysImage imageToSysImage(Image image, String repoTag) {
            SysImage sysImage = new SysImage();
            // 设置ImageId
            sysImage.setImageId(splitImageId(image.id()));

            // 获取repoTag
            Map<String, Object> map = splitRepoTag(repoTag);

            // 判断状态
            if(!(Boolean)map.get("status")) {
                log.error("解析repoTag出现异常，错误目标为：{}", map.get("fullName"));
                return null;
            }

            // 设置完整名
            sysImage.setFullName((String)map.get("fullName"));
            // 设置Tag
            sysImage.setTag((String)map.get("tag"));
            // 设置Repo
            sysImage.setRepo((String)map.get("repo"));
            // 设置name
            sysImage.setName((String)map.get("name"));
            // 设置type
            Integer type = (Integer)map.get("type");
            sysImage.setType(type);
            // 如果type为LOCAL_USER_IMAGE时
            if (ImageTypeEnum.LOCAL_USER_IMAGE.getCode() == type) {
                // 设置userId
                sysImage.setUserId((String)map.get("userId"));
                // 用户镜像默认不分享
                sysImage.setHasOpen(false);
            }

            // 设置CMD
            try {
                ImageInfo info = dockerClient.inspectImage(repoTag);
                sysImage.setCmd(JsonUtils.objectToJson(info.containerConfig().cmd()));
            } catch (Exception e) {
                log.error("获取镜像信息错误，错误位置：{}，错误栈：{}",
                        "SysImageServiceImpl.imageToSysImage()", HttpClientUtils.getStackTraceAsString(e));
            }

            // 设置大小
            sysImage.setSize(image.size());
            // 设置虚拟大小
            sysImage.setVirtualSize(image.virtualSize());
            // 设置Label
            sysImage.setLabels(JsonUtils.mapToJson(image.labels()));
            // 设置父节点
            sysImage.setParentId(image.parentId());
            sysImage.setCreateDate(new Date());

            return sysImage;

    }
    private Map<String, Object> splitRepoTag(String repoTag) {
        Map<String, Object> map = new HashMap<>(16);
        boolean flag = true;
        //设置tag
        int tagIndex = repoTag.lastIndexOf(":");
        String tag = repoTag.substring(tagIndex+1);

        map.put("fullName", repoTag);
        map.put("tag", tag);

        String tagHead = repoTag.substring(0, tagIndex);
        String[] names = tagHead.split("/");

        if(names.length == 1) {
            // 如果包含1个部分，代表来自官方的Image，例如nginx
            map.put("repo", "library");
            map.put("name", names[0]);
            map.put("type", ImageTypeEnum.LOCAL_PUBLIC_IMAGE.getCode());
        } else if(names.length == 2) {
            // 如果包含2个部分，代表来自指定的Image，例如：portainer/portainer
            map.put("repo", names[0]);
            map.put("name", names[1]);
            map.put("type", ImageTypeEnum.LOCAL_PUBLIC_IMAGE.getCode());
        } else if(names.length == 3) {
            // 如果包含3个部分，代表来自用户上传的Image，例如：local/jitwxs/hello-world
            map.put("repo", names[0]);
            map.put("type", ImageTypeEnum.LOCAL_USER_IMAGE.getCode());
            map.put("userId", names[1]);
            map.put("name", names[2]);
        } else {
            // 其他情况异常，形如：local/jitwxs/portainer/portainer:latest
            flag = false;
        }

        // 状态
        map.put("status", flag);

        return map;
    }

    private String splitImageId(String imageId) {
        String[] splits = imageId.split(":");
        if(splits.length == 1) {
            return imageId;
        }

        return splits[1];
    }
    @Override
    public SysImage getByFullName(String fullName) {
        String field = FULL_NAME_PREFIX + fullName;

        try {
            String json = jedisClient.hget(key, field);
            if(StringUtils.isNotBlank(json)) {
                return JsonUtils.jsonToObject(json, SysImage.class);
            }
        } catch (Exception e) {
            log.error("缓存读取异常，异常位置：{}", "SysImageServiceImpl.getByFullName()");
        }

        List<SysImage> images = imageMapper.selectList(new EntityWrapper<SysImage>().eq("full_name", fullName));
        SysImage image = CollectionUtils.getListFirst(images);
        if(image == null) {
            return null;
        }

        try {
            String json = JsonUtils.objectToJson(image);
            jedisClient.hset(key, field, json);
        } catch (Exception e) {
            log.error("缓存存储异常，异常位置：{}", "SysImageServiceImpl.getByFullName()");
        }

        return image;
    }

    public void cleanCache(String id, String fullName) {
        try {
            if (StringUtils.isNotBlank(id)) {
                jedisClient.hdel(key, ID_PREFIX + id);
            }
            if (StringUtils.isNotBlank(fullName)) {
                jedisClient.hdel(key, FULL_NAME_PREFIX + fullName);
            }
        } catch (Exception e) {
            log.error("清理本地镜像缓存失败，错误位置：{}，错误栈：{}",
                    "SysImageServiceImpl.cleanCache()", HttpClientUtils.getStackTraceAsString(e));
        }
    }

    @Override
    public SysImage getById(String id) {
       String field = ID_PREFIX + id;

        try {
            String json = jedisClient.hget(key, field);
            if (StringUtils.isNotBlank(json)) {
                return JsonUtils.jsonToObject(json,SysImage.class);
            }
        } catch (JsonException e) {
            log.error("缓存读取异常，异常位置：{}", "SysImageServiceImpl.getById()");
        }
        SysImage image = imageMapper.selectById(id);
        if(image == null) {
            return null;
        }

        try {
            String json = JsonUtils.objectToJson(image);
            jedisClient.hset(key, field, json);
        } catch (Exception e) {
            log.error("缓存存储异常，异常位置：{}", "SysImageServiceImpl.getById()");
        }

        return image;
    }

    @Async("taskExecutor")
    @Transactional(rollbackFor = CustomException.class)
    @Override
    public void pullImageTask(String name, String uid, HttpServletRequest request) {
        try {
            if(!name.contains(":")) {
                name = name + ":latest";
            }
            dockerClient.pull(name);
            // 写入日志
            sysLogService.saveLog(request, SysLogTypeEnum.PULL_IMAGE_FROM_DOCKER_HUB,null);
        } catch (DockerTimeoutException timoutException){
            sendMQ(uid, null, ResultVOUtils.error(ResultEnum.DOCKER_TIMEOUT));
            // 发送通知
            List<String> receiverList = new ArrayList<>();
            receiverList.add(uid);
            noticeService.sendUserTask("拉取Docker Hub镜像",
                    "拉取镜像【"+name+"】失败，连接超时", 4, false, receiverList, uid);
            return;
        }catch (Exception e) {
            log.error("Pull Docker Hub镜像失败，错误位置：{}，镜像名：{}，错误栈：{}"
                    , "SysImageServiceImpl.pullImageTask()", name, HttpClientUtils.getStackTraceAsString(e));
            // 写入日志
            sysLogService.saveLog(request, SysLogTypeEnum.PULL_IMAGE_FROM_DOCKER_HUB, e);
            // 发送通知
            List<String> receiverList = new ArrayList<>();
            receiverList.add(uid);
            noticeService.sendUserTask("拉取Docker Hub镜像","拉取镜像【"+name+"】失败，Docker拉取异常",
                    4, false, receiverList, uid);

            sendMQ(uid, null, ResultVOUtils.error(ResultEnum.PULL_ERROR));
            return;
        }

        // 保存信息
        try {
            List<Image> images = dockerClient.listImages(DockerClient.ListImagesParam.byName(name));
            if(images.size() <= 0) {
                sendMQ(uid, null, ResultVOUtils.error(ResultEnum.INSPECT_ERROR));
                // 发送通知
                List<String> receiverList = new ArrayList<>();
                receiverList.add(uid);
                noticeService.sendUserTask("拉取Docker Hub镜像", "拉取镜像【" + name + "】失败，查看拉取后镜像信息异常",
                        4, false, receiverList, uid);
                return;
            }

            Image image = images.get(0);

            SysImage sysImage = imageToSysImage(image, image.repoTags().get(0));
            imageMapper.insert(sysImage);
            // 发送通知
            List<String> receiverList = new ArrayList<>();
            receiverList.add(uid);
            noticeService.sendUserTask("拉取Docker Hub镜像","拉取镜像【"+name+"】成功", 4,
                    false, receiverList, uid);

            sendMQ(uid, sysImage.getId(), ResultVOUtils.successWithMsg("镜像拉取成功"));
        } catch (Exception e) {
            log.error("获取镜像详情失败，错误位置：{}，镜像名：{}，错误栈：{}",
                    "SysImageServiceImpl.pullImageFromHub()", name, HttpClientUtils.getStackTraceAsString(e));
            // 发送通知
            List<String> receiverList = new ArrayList<>();
            receiverList.add(uid);
            noticeService.sendUserTask("拉取Docker Hub镜像", "拉取镜像【" + name + "】失败，Docker异常", 4,
                    false, receiverList, uid);
            sendMQ(uid, null, ResultVOUtils.error(ResultEnum.DOCKER_EXCEPTION));
        }
    }

    /**
     *  发送镜像消息
     * @param
     * @since 2022/9/11
     * @return
     */
    private void sendMQ(String uid, String imageId, ResultVO resultVO) {
        Destination destination = new ActiveMQQueue("MQ_QUEUE_SYS_IMAGE");
        Task task = new Task();
        Map<String,Object> map = new HashMap<>();
        map.put("type", WebSocketTypeEnum.SYS_IMAGE.getCode());
        map.put("imageId", imageId);

        ResultVO resultVO1 = listExportPort(imageId, uid);
        if (resultVO1.getCode() == ResultEnum.OK.getCode()) {
            map.put("exportPort",resultVO1.getData());
        }
        resultVO.setData(map);


        Map<String,String> dataMap = new HashMap<>(16);
        dataMap.put("uid",uid);
        dataMap.put("data", JsonUtils.objectToJson(resultVO));
        task.setData(dataMap);

        mqProducer.send(destination, JsonUtils.objectToJson(task));
    }

    @Override
    public Boolean hasAuthImage(String userId, SysImage image) {

        if (ImageTypeEnum.LOCAL_PUBLIC_IMAGE.getCode() == image.getType()) {
            return true;
        }
        if (ImageTypeEnum.LOCAL_USER_IMAGE.getCode() == image.getType()) {
            if (image.getHasOpen()){
                return true;
            }
            String roleName = loginService.getRoleName(userId);
            if (RoleEnum.ROLE_USER.getMessage().equals(roleName) && !userId.equals(image.getUserId())) {
                return false;
            }else{
                return true;
            }
        }
        return false;
    }

    @Override
    public ResultVO listExportPort(String imageId, String uid) {
        SysImage sysImage = getById(imageId);
        if (sysImage == null){
            return ResultVOUtils.error(ResultEnum.IMAGE_NOT_EXIST);
        }

        if (!hasAuthImage(uid,sysImage)) {
            return ResultVOUtils.error(ResultEnum.PERMISSION_ERROR);
        }

        try {
            ImageInfo imageInfo = dockerClient.inspectImage(sysImage.getFullName());

            ImmutableSet<String> exposedPorts = imageInfo.containerConfig().exposedPorts();
            Set<String> set = new HashSet<>();

            if (exposedPorts != null && exposedPorts.size() > 0){
                exposedPorts.forEach(sss->{
                    set.add(sss.split("/")[0]);
                });
            }
            return ResultVOUtils.success(new ArrayList<>(set));
        } catch (DockerRequestException e) {
            return ResultVOUtils.error(ResultEnum.DOCKER_EXCEPTION.getCode(), HttpClientUtils.getErrorMessage(e.getMessage()));
        } catch (Exception e) {
            log.error("获取镜像暴露端口错误，出错位置：{}，出错镜像ID：{}，错误栈：{}",
                    "SysImageServiceImpl.listExportPorts()", imageId, HttpClientUtils.getStackTraceAsString(e));
            return null;
        }
    }

    @Override
    public ResultVO inspectImage(String id, String uid) {
        // 校验参数
        if(StringUtils.isBlank(id)) {
            return ResultVOUtils.error(ResultEnum.PARAM_ERROR);
        }

        // 查询数据库
        SysImage image = getById(id);
        if(image == null) {
            return ResultVOUtils.error(ResultEnum.IMAGE_EXCEPTION);
        }
        // 判断是否有权限访问
        if(!hasAuthImage(uid, image)) {
            return ResultVOUtils.error(ResultEnum.PERMISSION_ERROR);
        }

        // 查询信息
        try {
            String fullName = image.getFullName();

            return ResultVOUtils.success(dockerClient.inspectImage(fullName));
        } catch (DockerRequestException requestException){
            return ResultVOUtils.error(ResultEnum.DOCKER_EXCEPTION.getCode(),
                    HttpClientUtils.getErrorMessage(requestException.getMessage()));
        }catch (Exception e) {
            log.error("Docker查询详情异常，错误位置：{}，错误栈：{}",
                    "SysImageServiceImpl.inspectImage", HttpClientUtils.getStackTraceAsString(e));
            return ResultVOUtils.error(ResultEnum.INSPECT_ERROR);
        }
    }

    @Override
    public ResultVO getHistory(String id, String uid) {
        SysImage image = getById(id);
        if(image == null) {
            return ResultVOUtils.error(ResultEnum.IMAGE_EXCEPTION);
        }
        // 1、鉴权
        if(!hasAuthImage(uid, image)) {
            return ResultVOUtils.error(ResultEnum.PERMISSION_ERROR);
        }

        try {
            List<ImageHistory> history = dockerClient.history(image.getFullName());
            return ResultVOUtils.success(history);
        } catch (Exception e) {
            log.error("查看镜像源码文件异常，错误位置：{}，错误栈：{}",
                    "SysImageServiceImpl.imageFile", HttpClientUtils.getStackTraceAsString(e));
            return ResultVOUtils.error(ResultEnum.DOCKER_EXCEPTION);
        }
    }

    @Override
    public ResultVO exportImage(String id, String uid) {
        SysImage image = getById(id);
        if(image == null ) {
            return ResultVOUtils.error(ResultEnum.IMAGE_EXCEPTION);
        }
        if(!hasAuthImage(uid, image)) {
            return ResultVOUtils.error(ResultEnum.PERMISSION_ERROR);
        }

        String url = serverUrl + "/images/" + image.getFullName() + "/get";
        return ResultVOUtils.success(url);
    }
}
