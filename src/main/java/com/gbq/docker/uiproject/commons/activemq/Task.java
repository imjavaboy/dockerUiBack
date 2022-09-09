package com.gbq.docker.uiproject.commons.activemq;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * @author 郭本琪
 * @description 任务类
 * @date 2022/9/8 18:01
 * @Copyright 总有一天，会见到成功
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 任务描述
     */
    private String info;

    /**
     * 任务实体
     */
    private Map<String,String> data;
}
