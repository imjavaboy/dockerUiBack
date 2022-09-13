package com.gbq.docker.uiproject.exception;


import com.gbq.docker.uiproject.commons.util.ResultVOUtils;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.spotify.docker.client.exceptions.DockerTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author 郭本琪
 * @description 全局异常处理
 * @date 2022/9/8 9:01
 * @Copyright 总有一天，会见到成功
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
//    @ExceptionHandler(value = CustomException.class)
//    public ResultVO customException(CustomException e) {
////        log.error("其他错误，错误栈：{}", HttpClientUtils.getStackTraceAsString(e));
//        return ResultVOUtils.error(e.getCode(), e.getMessage());
//    }
//
//    @ExceptionHandler(value = DockerTimeoutException.class)
//    public ResultVO customException(DockerTimeoutException e) {
//        return ResultVOUtils.error(ResultEnum.DOCKER_TIMEOUT);
//    }
//
//    @ExceptionHandler(value = Exception.class)
//    public ResultVO exception(Exception e) {
////        log.error("其他错误，错误栈：{}", HttpClientUtils.getStackTraceAsString(e));
//        return ResultVOUtils.error(ResultEnum.OTHER_ERROR.getCode(), e.getMessage());
//    }
//    @ExceptionHandler(value = JsonException.class)
//    public ResultVO jsonexception(Exception e) {
////        log.error("其他错误，错误栈：{}", HttpClientUtils.getStackTraceAsString(e));
//        return ResultVOUtils.error(ResultEnum.OTHER_ERROR.getCode(), e.getMessage());
//    }



}
