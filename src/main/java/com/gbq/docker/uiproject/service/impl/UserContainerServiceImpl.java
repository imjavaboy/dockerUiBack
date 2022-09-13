package com.gbq.docker.uiproject.service.impl;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.gbq.docker.uiproject.commons.activemq.MQProducer;
import com.gbq.docker.uiproject.commons.activemq.Task;
import com.gbq.docker.uiproject.commons.component.UserContainerDTOConvert;
import com.gbq.docker.uiproject.commons.util.*;
import com.gbq.docker.uiproject.commons.util.jedis.JedisClient;
import com.gbq.docker.uiproject.domain.dto.UserContainerDTO;
import com.gbq.docker.uiproject.domain.entity.SysImage;
import com.gbq.docker.uiproject.domain.entity.SysVolume;
import com.gbq.docker.uiproject.domain.entity.UserContainer;
import com.gbq.docker.uiproject.domain.entity.UserProject;
import com.gbq.docker.uiproject.domain.enums.*;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.exception.CustomException;
import com.gbq.docker.uiproject.mapper.SysVolumesMapper;
import com.gbq.docker.uiproject.mapper.UserContainerMapper;
import com.gbq.docker.uiproject.mapper.UserProjectMapper;
import com.gbq.docker.uiproject.service.*;
import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.DockerRequestException;
import com.spotify.docker.client.exceptions.DockerTimeoutException;
import com.spotify.docker.client.messages.*;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/9 21:54
 * @Copyright 总有一天，会见到成功
 */
@Service
@Slf4j
public class UserContainerServiceImpl extends ServiceImpl<UserContainerMapper, UserContainer> implements UserContainerService {
    @Resource
    private UserContainerDTOConvert dtoConvert;
    @Resource
    private UserContainerMapper userContainerMapper;
    @Resource
    private UserProjectMapper projectMapper;
    @Resource
    private SysImageService imageService;
    @Resource
    private PortService portService;
    @Resource
    private DockerClient dockerClient;
    @Resource
    private SysVolumesMapper sysVolumesMapper;
    @Resource
    private SysNetworkService networkService;
    @Resource
    private SysLogService sysLogService;
    @Resource
    private ProjectLogService projectLogService;
    @Resource
    private NoticeService noticeService;
    @Resource
    private MQProducer mqProducer;
    @Resource
   private SysLoginService loginService;


    @Resource
    private JedisClient jedisClient;
    @Value("${redis.container-name.key}")
    private String key;


    @Override
    public UserContainerDTO getById(String objId) {
        return dtoConvert.convert(userContainerMapper.selectById(objId));
    }

    @Override
    public Page<UserContainerDTO> listContainerByUserId(String uid, String name, Integer status, Page<UserContainer> page) {


        List<UserContainer> containers = userContainerMapper.listContainerByUserIdAndNameAndStatus(page, uid, name, status);
        Page<UserContainerDTO> page1 = new Page<>();
        BeanUtils.copyProperties(page, page1);
        return page1.setRecords(dtoConvert.convert(containers));
    }


    @Override
    public ResultVO createContainerCheck(String uid, String imageId, Map<String, String> portMap, String projectId) {
        //是否属于本人
        boolean isHas = projectMapper.hasBelong(projectId, uid);
        if (!isHas) {
            return ResultVOUtils.error(ResultEnum.PERMISSION_ERROR);
        }


        SysImage image = imageService.getById(imageId);
        if (image == null) {
            return ResultVOUtils.error(ResultEnum.IMAGE_EXCEPTION);
        }
        //是否有权限查看镜像
        if (!imageService.hasAuthImage(uid, image)) {
            return ResultVOUtils.error(ResultEnum.PERMISSION_ERROR);
        }
        //端口
        ResultVO resultVO = imageService.listExportPort(imageId, uid);
        if (ResultEnum.OK.getCode() != resultVO.getCode()) {
            return resultVO;
        }
        List<String> exportPorts = (List<String>) resultVO.getData();
        if (!checkPorts(exportPorts, portMap)) {
            return ResultVOUtils.error(ResultEnum.INPUT_PORT_ERROR);
        }
        return ResultVOUtils.success();
    }

    //检查端口号
    private boolean checkPorts(List<String> exportPorts, Map<String, String> portMap) {
        // 校验NULL
        if (CollectionUtils.isListEmpty(exportPorts) && portMap == null) {
            return true;
        }
        if (CollectionUtils.isListNotEmpty(exportPorts) && portMap == null) {
            return false;
        }
        if (CollectionUtils.isListNotEmpty(exportPorts)) {
            for (String port : exportPorts) {
                if (portMap.get(port) == null) {
                    return false;
                }
            }
        }
        return hasPortIllegal(portMap);

    }

    /**
     * 端口范围
     *
     * @param
     * @return
     * @since 2022/9/12
     */
    private boolean hasPortIllegal(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            Integer value = Integer.parseInt(entry.getValue());

            // 判断数字
            try {
                Integer.parseInt(entry.getKey());
            } catch (Exception e) {
                return false;
            }

            // value允许端口范围：[10000 ~ 65535)
            if (value < 10000 || value > 65535) {
                return false;
            }
        }
        return true;
    }


    @Async("taskExecutor")
    @Transactional(rollbackFor = CustomException.class)
    @Override
    public void createContainerTask(String userId, String imageId, String[] cmd, Map<String, String> portMap,
                                    String containerName, String projectId, String[] env, String[] destination,
                                    HttpServletRequest request) {
        SysImage image = imageService.getById(imageId);
        UserContainer uc = new UserContainer();
        HostConfig hostConfig;
        ContainerConfig.Builder builder = ContainerConfig.builder();

        // 1、设置暴露端口
        if(portMap != null) {
            // 宿主机端口与暴露端口绑定
            Set<String> realExportPorts = new HashSet<>();
            Map<String, List<PortBinding>> portBindings = new HashMap<>(16);

            for(Map.Entry<String, String> entry : portMap.entrySet()) {
                String k = entry.getKey();
                int v = Integer.parseInt(entry.getValue());
                realExportPorts.add(k);
                // 捆绑端口
                List<PortBinding> hostPorts = new ArrayList<>();
                // 分配主机端口，如果用户输入端口被占用，随机分配
                Integer hostPort = portService.hasUse(v) ? portService.randomPort() : v;
                hostPorts.add(PortBinding.of("0.0.0.0", hostPort));
                portBindings.put(k, hostPorts);
            }

            uc.setPort(JsonUtils.objectToJson(portBindings));

            builder.exposedPorts(realExportPorts);

            hostConfig = HostConfig.builder()
                    .portBindings(portBindings)
                    .build();
        } else {
            hostConfig = HostConfig.builder().build();
        }

        // 2、构建ContainerConfig
        builder.hostConfig(hostConfig);
        builder.image(image.getFullName());
        builder.tty(true);
        if(CollectionUtils.isNotArrayEmpty(cmd)) {
            builder.cmd(cmd);
            uc.setCommand(Arrays.toString(cmd));
        }
        if(CollectionUtils.isNotArrayEmpty(destination)) {
            builder.volumes(destination);
        }
        if(CollectionUtils.isNotArrayEmpty(env)) {
            builder.env(env);
            uc.setEnv(Arrays.toString(env));
        }
        ContainerConfig containerConfig = builder.build();

        try {
            ContainerCreation creation = dockerClient.createContainer(containerConfig);

            uc.setId(creation.id());
            // 仅存在于数据库，不代表实际容器名
            uc.setName(containerName);
            uc.setProjectId(projectId);
            uc.setUserId(userId);
            uc.setImage(image.getFullName());

            if(CollectionUtils.isNotArrayEmpty(destination)) {
                // 为数据库中的sysvolumes插入
                ImmutableList<ContainerMount> info = dockerClient.inspectContainer(creation.id()).mounts();
                if(CollectionUtils.isListNotEmpty(info)) {
                    for(ContainerMount mount : info) {
                        SysVolume sysVolume = new SysVolume();
                        sysVolume.setObjId(creation.id());
                        sysVolume.setDestination(mount.destination());
                        sysVolume.setName(mount.name());
                        sysVolume.setSource(mount.source());
                        sysVolume.setType(VolumeTypeEnum.CONTAINER.getCode());
                        sysVolumesMapper.insert(sysVolume);
                    }
                }
            }

            // 3、设置状态
            ContainerStatusEnum status = getStatus(creation.id());
            if(status == null) {
                throw new CustomException(ResultEnum.DOCKER_EXCEPTION.getCode(), "读取容器状态异常");
            }
            uc.setStatus(status.getCode());
            uc.setCreateDate(new Date());

            userContainerMapper.insert(uc);

            // 4、保存网络信息
            networkService.syncByContainerId(uc.getId());

            // 5、写入日志
            sysLogService.saveLog(request, SysLogTypeEnum.CREATE_CONTAINER,null);
            projectLogService.saveSuccessLog(projectId,uc.getId(),ProjectLogTypeEnum.CREATE_CONTAINER);

            // 发送通知
            List<String> receiverList = new ArrayList<>();
            receiverList.add(userId);
            noticeService.sendUserTask("创建容器", "创建容器【" + containerName + "】成功", 2, false, receiverList, null);

            sendMQ(userId, null, ResultVOUtils.successWithMsg("容器【"+containerName+"】创建成功"));
        } catch (DockerRequestException requestException){
            log.error("创建容器出现异常，错误位置：{}，错误原因：{}",
                    "UserContainerServiceImpl.createContainerTask()", requestException.getMessage());
            sendMQ(userId, null, ResultVOUtils.error(
                    ResultEnum.CREATE_CONTAINER_ERROR.getCode(),HttpClientUtils.getErrorMessage(requestException.getMessage())));
        } catch (Exception e) {
            log.error("创建容器出现异常，错误位置：{}，错误栈：{}",
                    "UserContainerServiceImpl.createContainerTask()", HttpClientUtils.getStackTraceAsString(e));

            // 写入日志
            sysLogService.saveLog(request, SysLogTypeEnum.CREATE_CONTAINER, e);
            projectLogService.saveErrorLog(projectId,uc.getId(),ProjectLogTypeEnum.CREATE_CONTAINER_ERROR,ResultEnum.DOCKER_EXCEPTION);

            // 发送通知
            List<String> receiverList = new ArrayList<>();
            receiverList.add(userId);
            noticeService.sendUserTask("创建容器","创建容器【"+containerName+"】失败,Docker异常", 2, false, receiverList, null);

            sendMQ(userId, null, ResultVOUtils.error(ResultEnum.DOCKER_EXCEPTION));
        }
    }
    /**
     * 发送容器的消息
     *
     * @param
     * @return
     * @since 2022/9/12
     */
    private void sendMQ(String userId, String containerId, ResultVO resultVO) {
        Destination destination = new ActiveMQQueue("MQ_QUEUE_CONTAINER");
        Task task = new Task();

        Map<String, Object> data = new HashMap<>(16);
        data.put("type", WebSocketTypeEnum.CONTAINER.getCode());
        data.put("containerId", containerId);
        resultVO.setData(data);

        Map<String, String> map = new HashMap<>(16);
        map.put("uid", userId);
        map.put("data", JsonUtils.objectToJson(resultVO));
        task.setData(map);

        mqProducer.send(destination, JsonUtils.objectToJson(task));
    }

    @Override
    public ContainerStatusEnum getStatus(String containerId) {


        try {
            ContainerInfo info = dockerClient.inspectContainer(containerId);
            ContainerState state = info.state();

            ContainerStatusEnum statusEnum;
            if (state.running()) {
                if (state.paused()) {
                    statusEnum = ContainerStatusEnum.PAUSE;
                } else {
                    statusEnum = ContainerStatusEnum.RUNNING;
                }
            } else {
                statusEnum = ContainerStatusEnum.STOP;
            }
            //保证数据库和docker容器状态一致性
            UserContainerDTO containerDTO = getById(containerId);

            if (containerDTO != null) {
                if (statusEnum.getCode() != containerDTO.getStatus()) {
                    containerDTO.setStatus(statusEnum.getCode());
                    userContainerMapper.updateById(containerDTO);
                }
            }
            return statusEnum;
        } catch (ContainerNotFoundException ee) {
            return ContainerStatusEnum.REMOVE;
        } catch (DockerTimeoutException e) {
            log.error("获取容器状态超时，异常位置：{}，容器ID：{}",
                    "UserContainerServiceImpl.getStatus()", containerId);
        } catch (Exception e) {
            log.error("获取容器状态出现异常，异常位置：{}，错误栈：{}",
                    "UserContainerServiceImpl.getStatus()", HttpClientUtils.getStackTraceAsString(e));
        }
        return null;
    }

    @Override
    public Map<String, Integer> syncStatus(String userId) {
        List<UserContainer> containers;

        //为空时同步所有
        if(StringUtils.isBlank(userId)) {
            containers = userContainerMapper.selectList(new EntityWrapper<>());
        } else{
            containers = userContainerMapper.selectList(new EntityWrapper<UserContainer>().eq("user_id",userId));
        }

        int successCount = 0, errorCount = 0;
        for(UserContainer container : containers) {
            ResultVO resultVO = changeStatus(container.getId());

            if(ResultEnum.OK.getCode() == resultVO.getCode()) {
                successCount++;
            } else {
                errorCount++;
            }
        }
        Map<String, Integer> map =new HashMap<>(16);
        map.put("success", successCount);
        map.put("error", errorCount);
        return map;
    }

    @Override
    public ResultVO changeStatus(String containerId) {
        ContainerStatusEnum statusEnum = getStatus(containerId);
        if(statusEnum == null) {
            return ResultVOUtils.error(ResultEnum.DOCKER_EXCEPTION);
        }
        if(statusEnum == ContainerStatusEnum.REMOVE) {
            userContainerMapper.deleteById(containerId);
            return ResultVOUtils.success();
        }

        UserContainerDTO containerDTO = getById(containerId);
        if(containerDTO == null) {
            return ResultVOUtils.error(ResultEnum.PARAM_ERROR);
        }

        if(containerDTO.getStatus() != statusEnum.getCode()) {
            containerDTO.setStatus(statusEnum.getCode());
            userContainerMapper.updateById(containerDTO);
        }

        return ResultVOUtils.success();
    }


    /**
     * 启动状态允许的操作
     */
    private List<ContainerOpEnum> allowOpByRunning = Arrays.asList(ContainerOpEnum.PAUSE, ContainerOpEnum.STOP,
            ContainerOpEnum.KILL, ContainerOpEnum.RESTART);
    /**
     * 暂停状态允许的操作
     */
    private List<ContainerOpEnum> allowOpByPause = Arrays.asList(ContainerOpEnum.CONTINUE, ContainerOpEnum.STOP,
            ContainerOpEnum.KILL, ContainerOpEnum.RESTART);
    /**
     * 停止状态允许的操作
     */
    private List<ContainerOpEnum> allowOpByStop = Arrays.asList(ContainerOpEnum.START, ContainerOpEnum.RESTART,
            ContainerOpEnum.DELETE);

    @Override
    public ResultVO hasAllowOp(String uid, String containerId, ContainerOpEnum containerOpEnum) {
        // 鉴权
        ResultVO resultVO = checkPermission(uid, containerId);
        if (ResultEnum.OK.getCode() != resultVO.getCode()) {
            return ResultVOUtils.error(ResultEnum.AUTHORITY_ERROR);
        }

        // 判断状态
        ContainerStatusEnum statusEnum = getStatus(containerId);
        if (statusEnum == ContainerStatusEnum.RUNNING) {
            // 运行：暂停、停止、强制停止、重启
            return allowOpByRunning.contains(containerOpEnum) ? ResultVOUtils.success() :
                    ResultVOUtils.error(ResultEnum.CONTAINER_ALREADY_START);
        } else if (statusEnum == ContainerStatusEnum.PAUSE) {
            // 暂停：恢复、停止、强制停止、重启
            return allowOpByPause.contains(containerOpEnum) ? ResultVOUtils.success() :
                    ResultVOUtils.error(ResultEnum.CONTAINER_ALREADY_PAUSE);
        } else if (statusEnum == ContainerStatusEnum.STOP) {
            // 停止：启动、重启、删除
            return allowOpByStop.contains(containerOpEnum) ? ResultVOUtils.success() :
                    ResultVOUtils.error(ResultEnum.CONTAINER_ALREADY_STOP);
        } else {
            return ResultVOUtils.error(ResultEnum.CONTAINER_STATUS_ERROR);
        }
    }

    @Override
    public ResultVO checkPermission(String uid, String containerId) {
        String roleName = loginService.getRoleName(uid);
        if(StringUtils.isBlank(roleName)) {
            return ResultVOUtils.error(ResultEnum.AUTHORITY_ERROR);
        }
        if(RoleEnum.ROLE_USER.getMessage().equals(roleName)) {
            UserContainerDTO containerDTO = getById(containerId);
            if(containerDTO == null) {
                return ResultVOUtils.error(ResultEnum.CONTAINER_NOT_FOUND);
            }

            if(!containerDTO.getUserId().equals(uid)) {
                return ResultVOUtils.error(ResultEnum.PERMISSION_ERROR);
            }
        }

        return ResultVOUtils.success();
    }

    /**
     *  开启容器
     * @param
     * @since 2022/9/12
     * @return
     */
    @Async("taskExecutor")
    @Transactional(rollbackFor = CustomException.class)
    @Override
    public void startContainerTask(String uid, String containerId) {
        try {
            dockerClient.startContainer(containerId);
            changeStatus(containerId);

            // 写入日志
            projectLogService.saveSuccessLog(getProjectId(containerId),containerId,ProjectLogTypeEnum.START_CONTAINER);

            // 发送成功消息
            sendMQ(uid, containerId, ResultVOUtils.successWithMsg("容器"+getName(containerId)+"启动成功"));
        } catch (Exception e) {
            log.error("开启容器出现异常，异常位置：{}，错误栈：{}",
                    "UserContainerServiceImpl.startContainerTask()",HttpClientUtils.getStackTraceAsString(e));
            // 写入日志
            projectLogService.saveErrorLog(getProjectId(containerId),containerId,ProjectLogTypeEnum.START_CONTAINER_ERROR,ResultEnum.DOCKER_EXCEPTION);

            // 发送异常消息
            sendMQ(uid, containerId, ResultVOUtils.error(ResultEnum.CONTAINER_START_ERROR));
        }
    }

    /**
     *  根据容器uid获取名称
     * @param
     * @since 2022/9/12
     * @return
     */
    private String getName(String id) {
        try {
            String name = jedisClient.hget(key, id);
            if(StringUtils.isNotBlank(name)) {
                return name;
            }
        } catch (Exception e) {
            log.error("缓存读取异常，异常位置：{}", "UserContainerServiceImpl.getName()");
        }

        String name = getById(id).getName();
        if(StringUtils.isBlank(name)) {
            return name;
        }

        try {
            jedisClient.hset(key,id,name);
        } catch (Exception e) {
            log.error("缓存存储异常，异常位置：{}", "UserContainerServiceImpl.getName()");
        }

        return name;
    }

    /**
     *  根据容器id或企业容器名
     * @param
     * @since 2022/9/12
     * @return
     */
    private String getProjectId(String containerId) {
        UserContainer container = getById(containerId);

        return  container == null ? null : container.getProjectId();
    }

    @Override
    public Map<String, Integer> sync() {

        //查询数据库所有的容器
        List<String> dbIdList = userContainerMapper.listAllContainerIds();
        //获取本地所有的容器
        List<Container> containersList = null;
        try {
            containersList = dockerClient.listContainers(DockerClient.ListContainersParam.allContainers());
        }  catch (DockerTimeoutException e) {
            log.error("获取容器状态超时，异常位置：{}", "UserContainerServiceImpl.sync()");
        } catch (Exception e) {
            log.error("获取容器状态出现异常，异常位置：{}，错误栈：{}",
                    "UserContainerServiceImpl.getStatus()", HttpClientUtils.getStackTraceAsString(e));
        }

        int deleteCount = 0,addCount = 0,errorCount=0;
//        boolean[] dbFlag = new boolean[dbContainerList.size()];
//        Arrays.fill(dbFlag,false);

        List<String> list = new ArrayList<>();
        // 遍历本地镜像
        for(int i=0; i< containersList.size(); i++) {
            Container container = containersList.get(i);
            // 容器id
            String containerId = container.id();
            list.add(containerId);
            System.out.println("consssssssss:"+containerId);
            if (!dbIdList.contains(containerId)) {
                //保存数据库
                UserContainer userContainer = new UserContainer();
                userContainer.setCreateDate(new Date(container.created()));

                if (container.state().equals("exited")) {
                    userContainer.setStatus(ContainerStatusEnum.STOP.getCode());
                }else if (container.state().equals("running")){
                    userContainer.setStatus(ContainerStatusEnum.RUNNING.getCode());
                }else if (container.state().equals("paused")) {
                    userContainer.setStatus(ContainerStatusEnum.PAUSE.getCode());
                }else if (container.state().equals("created")){
                    userContainer.setStatus(ContainerStatusEnum.PAUSE.getCode());
                }
                //默认是管理员创建的
                userContainer.setId(containerId);
                userContainer.setUserId("agfag13131");
                userContainer.setProjectId("f45a65eae10842b68cebeb86b10940cb");
                userContainer.setName(container.names().get(0).split("/")[1]);
                userContainer.setCommand(container.command());

               // {"3306":[{"HostIp":"0.0.0.0","HostPort":"34567"}],"33060":[{"HostIp":"0.0.0.0","HostPort":"34567"}]}
                Map<String, List<PortBinding>> portBindings = new HashMap<>(16);
                ImmutableList<Container.PortMapping> ports = container.ports();
                AtomicInteger x = new AtomicInteger();

                for (int i1 = 0; i1 < ports.size(); i1++) {
                    if (i1 >= 2 ){
                        break;
                    }
                    Container.PortMapping portMapping = ports.get(i1);
                    Integer k = portMapping.privatePort();
                    List<PortBinding> hostPorts = new ArrayList<>();
                    hostPorts.add(PortBinding.of("0.0.0.0", portMapping.publicPort()));
                    portBindings.put(String.valueOf(k), hostPorts);

                }
               userContainer.setPort(JsonUtils.mapToJson(portBindings));
               userContainer.setImage(container.image());
               userContainer.setCreateDate(new Date(container.created()));
              userContainerMapper.insert(userContainer);
              addCount++;
            }



        }

        //遍历数据库镜像
        for (int i = 0; i < dbIdList.size(); i++) {
            String id = dbIdList.get(i);
            if (!list.contains(id)){
                userContainerMapper.deleteById(id);
                deleteCount++;
            }
        }

        HashMap<String, Integer> map = new HashMap<>();
        map.put("同步增加",addCount);
        map.put("同步减少",deleteCount);
        return map;

    }
    @Async("taskExecutor")
    @Transactional(rollbackFor = CustomException.class)
    @Override
    public void pauseContainerTask(String uid, String containerId) {
        try {
            dockerClient.pauseContainer(containerId);
            changeStatus(containerId);
            // 写入日志
            projectLogService.saveSuccessLog(getProjectId(containerId), containerId, ProjectLogTypeEnum.PAUSE_CONTAINER);

            // 发送成功消息
            sendMQ(uid, containerId, ResultVOUtils.successWithMsg("容器" + getName(containerId) + "暂停成功"));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("暂停容器出现异常，异常位置：{}，错误栈：{}","UserContainerServiceImpl.pauseContainerTask()",HttpClientUtils.getStackTraceAsString(e));
            // 写入日志
            projectLogService.saveErrorLog(getProjectId(containerId), containerId, ProjectLogTypeEnum.PAUSE_CONTAINER_ERROR,ResultEnum.DOCKER_EXCEPTION);
            // 发送异常消息
            sendMQ(uid, containerId, ResultVOUtils.error(ResultEnum.DOCKER_EXCEPTION));
        }
    }

    @Async("taskExecutor")
    @Transactional(rollbackFor = CustomException.class)
    @Override
    public void restartContainerTask(String uid, String containerId) {
        try {
            dockerClient.restartContainer(containerId);
            changeStatus(containerId);
            // 写入日志
            projectLogService.saveSuccessLog(getProjectId(containerId), containerId, ProjectLogTypeEnum.RESTART_CONTAINER);

            // 发送成功消息
            sendMQ(uid, containerId, ResultVOUtils.successWithMsg("容器" + getName(containerId) + "重启成功"));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("重启容器出现异常，异常位置：{}，错误栈：{}","UserContainerServiceImpl.restartContainerTask()",HttpClientUtils.getStackTraceAsString(e));
            // 写入日志
            projectLogService.saveErrorLog(getProjectId(containerId), containerId, ProjectLogTypeEnum.RESTART_CONTAINER_ERROR,ResultEnum.DOCKER_EXCEPTION);

            // 发送异常消息
            sendMQ(uid, containerId, ResultVOUtils.error(ResultEnum.DOCKER_EXCEPTION));
        }
    }

    @Override
    public void stopContainerTask(String uid, String containerId) {
        try {
            dockerClient.stopContainer(containerId, 5);
            changeStatus(containerId);
            // 写入日志
            projectLogService.saveSuccessLog(getProjectId(containerId), containerId, ProjectLogTypeEnum.STOP_CONTAINER);

            // 发送成功消息
            sendMQ(uid, containerId, ResultVOUtils.successWithMsg("容器" + getName(containerId) + "关闭成功"));
        } catch (Exception e) {
            log.error("停止容器出现异常，异常位置：{}，错误栈：{}","UserContainerServiceImpl.stopContainerTask()",HttpClientUtils.getStackTraceAsString(e));
            // 写入日志
            projectLogService.saveErrorLog(getProjectId(containerId), containerId, ProjectLogTypeEnum.STOP_CONTAINER_ERROR,ResultEnum.DOCKER_EXCEPTION);
            // 发送异常消息
            sendMQ(uid, containerId, ResultVOUtils.error(ResultEnum.DOCKER_EXCEPTION));
        }
    }
    @Async("taskExecutor")
    @Transactional(rollbackFor = CustomException.class)
    @Override
    public void killContainerTask(String userId, String containerId) {
        try {
            dockerClient.killContainer(containerId);
            changeStatus(containerId);
            // 写入日志
            projectLogService.saveSuccessLog(getProjectId(containerId), containerId, ProjectLogTypeEnum.KILL_CONTAINER);
            // 发送成功消息
            sendMQ(userId, containerId, ResultVOUtils.successWithMsg("容器" + getName(containerId) + "强制关闭成功"));
        } catch (Exception e) {
            log.error("强制停止容器出现异常，异常位置：{}，错误栈：{}", "UserContainerServiceImpl.killContainerTask()", HttpClientUtils.getStackTraceAsString(e));
            // 写入日志
            projectLogService.saveErrorLog(getProjectId(containerId), containerId, ProjectLogTypeEnum.KILL_CONTAINER_ERROR, ResultEnum.DOCKER_EXCEPTION);
            // 发送异常消息
            sendMQ(userId, containerId, ResultVOUtils.error(ResultEnum.DOCKER_EXCEPTION));
        }
    }

    @Async("taskExecutor")
    @Transactional(rollbackFor = CustomException.class)
    @Override
    public void continueContainerTask(String uid, String containerId) {
        try {
            dockerClient.unpauseContainer(containerId);
            changeStatus(containerId);
            // 写入日志
            projectLogService.saveSuccessLog(getProjectId(containerId), containerId, ProjectLogTypeEnum.CONTINUE_CONTAINER);
            // 发送成功消息
            sendMQ(uid, containerId, ResultVOUtils.successWithMsg("容器" + getName(containerId) + "恢复成功"));
        } catch (Exception e) {
            log.error("继续容器出现异常，异常位置：{}，错误栈：{}","UserContainerServiceImpl.continueContainerTask()",HttpClientUtils.getStackTraceAsString(e));
            // 写入日志
            projectLogService.saveErrorLog(getProjectId(containerId), containerId, ProjectLogTypeEnum.CONTINUE_CONTAINER,ResultEnum.DOCKER_EXCEPTION);

            // 发送异常消息
            sendMQ(uid, containerId, ResultVOUtils.error(ResultEnum.DOCKER_EXCEPTION));
        }
    }

    @Async("taskExecutor")
    @Transactional(rollbackFor = CustomException.class)
    @Override
    public void removeContainerTask(String uid, String containerId, HttpServletRequest request) {
        try {
            dockerClient.removeContainer(containerId);
            // 删除数据
            String name = getName(containerId);
            userContainerMapper.deleteById(containerId);
            // 删除数据卷
            sysVolumesMapper.deleteByObjId(containerId);
            // 清理缓存
            cleanCache(containerId);
            // 写入日志
            sysLogService.saveLog(request, SysLogTypeEnum.DELETE_CONTAINER,null);
            projectLogService.saveSuccessLog(getProjectId(containerId), containerId, ProjectLogTypeEnum.DELETE_CONTAINER);
            // 发送成功消息
            sendMQ(uid, containerId, ResultVOUtils.successWithMsg("容器" + name + "移除成功"));
        } catch (Exception e) {
            log.error("删除容器出现异常，异常位置：{}，错误栈：{}",
                    "UserContainerServiceImpl.removeContainerTask()", HttpClientUtils.getStackTraceAsString(e));
            // 写入日志
            sysLogService.saveLog(request, SysLogTypeEnum.DELETE_CONTAINER, e);
            projectLogService.saveErrorLog(getProjectId(containerId), containerId, ProjectLogTypeEnum.DELETE_CONTAINER_ERROR,ResultEnum.DOCKER_EXCEPTION);
            // 发送异常消息
            sendMQ(uid, containerId, ResultVOUtils.error(ResultEnum.DOCKER_EXCEPTION));
        }
    }
    private void cleanCache(String id) {
        try {
            jedisClient.hdel(key, id);
        } catch (Exception e) {
            log.error("删除缓存出现异常，异常位置：{}", "UserContainerServiceImpl.cleanCache");
        }
    }

    @Override
    public ResultVO topContainer(String uid, String containerId) {
        // 鉴权
        ResultVO resultVO = checkPermission(uid, containerId);
        if(ResultEnum.OK.getCode() != resultVO.getCode()) {
            return resultVO;
        }

        // 只有启动状态才能查看进程
        ContainerStatusEnum status = getStatus(containerId);
        if(status != ContainerStatusEnum.RUNNING) {
            return ResultVOUtils.error(ResultEnum.CONTAINER_NOT_RUNNING);
        }

        try {
            TopResults results = dockerClient.topContainer(containerId);
            return ResultVOUtils.success(results);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("查看容器进程出现异常，异常位置：{}，错误栈：{}","UserContainerServiceImpl.continueContainer()",HttpClientUtils.getStackTraceAsString(e));
            return ResultVOUtils.error(ResultEnum.DOCKER_EXCEPTION);
        }
    }

    @Override
    public ResultVO changeBelongProject(String containerId, String projectId, String uid) {
        // 1、鉴权容器
        ResultVO resultVO = checkPermission(uid, containerId);
        if(ResultEnum.OK.getCode() != resultVO.getCode()) {
            return resultVO;
        }

        // 2、鉴权项目
        UserProject project = projectMapper.selectById(projectId);
        if(project == null) {
            return ResultVOUtils.error(ResultEnum.PROJECT_NOT_EXIST);
        }
        if(!project.getUserId().equals(uid)) {
            return ResultVOUtils.error(ResultEnum.PROJECT_ACCESS_ERROR);
        }

        // 3、修改所属项目
        UserContainerDTO containerDTO = getById(containerId);
        containerDTO.setProjectId(projectId);
        userContainerMapper.updateById(containerDTO);
        // 4、清除缓存
        cleanCache(containerId);

        return ResultVOUtils.success();
    }
}
