package com.gbq.docker.uiproject.exception;


import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import lombok.Data;

/**
 * @author 郭本琪
 * @description 异常处理
 * @date 2022/9/8 9:01
 * @Copyright 总有一天，会见到成功
 */
@Data
public class CustomException extends Exception{
    private Integer code;

    public CustomException(ResultEnum resultEnum) {
        super(resultEnum.getMessage());
        this.code = resultEnum.getCode();
    }
    public CustomException(Integer code , String info) {
        super(info);
        this.code = code;
    }
}
