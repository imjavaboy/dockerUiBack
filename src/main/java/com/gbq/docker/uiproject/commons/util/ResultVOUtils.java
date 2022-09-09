package com.gbq.docker.uiproject.commons.util;


import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.vo.ResultVO;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/7 16:06
 * @Copyright 总有一天，会见到成功
 */

public class ResultVOUtils {
    public static ResultVO success(Object data) {
        return new ResultVO(ResultEnum.OK.getCode(),ResultEnum.OK.getMessage(), data);
    }

    public static ResultVO successWithMsg(String message) {
        return success(message, null);
    }

    public static ResultVO success(String message, Object data) {
        return new ResultVO(ResultEnum.OK.getCode(), message, data);
    }

    public static ResultVO success() {
        return success(null);
    }

    public static ResultVO error(Integer code, String message) {
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(code);
        resultVO.setMessage(message);
        return resultVO;
    }

    public static ResultVO error(ResultEnum resultEnum) {
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(resultEnum.getCode());
        resultVO.setMessage(resultEnum.getMessage());
        return resultVO;
    }
}
