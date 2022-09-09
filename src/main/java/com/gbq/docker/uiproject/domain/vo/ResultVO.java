package com.gbq.docker.uiproject.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 返回给前台的数据
 * @author gbq
 * @since 2022/9/10 0:03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultVO {
    private Integer code;

    private String message;

    private Object data;
}
