package com.gbq.docker.uiproject.commons.component;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.gbq.docker.uiproject.domain.entity.SysLogin;
import com.gbq.docker.uiproject.domain.select.UserSelect;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author 郭本琪
 * @description mybatisplus的筛选条件
 * @date 2022/9/9 12:24
 * @Copyright 总有一天，会见到成功
 */
@Component
public class WrapperComponent {


    public EntityWrapper<SysLogin> genUserWrapper(UserSelect select) {
        EntityWrapper<SysLogin> wrapper = new EntityWrapper<>();

        if (StringUtils.isNotBlank(select.getUsername())) {
            wrapper.like("username", select.getUsername());
        }

        if (StringUtils.isNotBlank(select.getId())) {
            wrapper.eq("id", select.getId());
        }

        if (StringUtils.isNotBlank(select.getEmail())) {
            wrapper.like("email", select.getEmail());
        }

        if(select.getHasFreeze() != null) {
            wrapper.eq("has_freeze", select.getHasFreeze());
        }

        if (select.getStartDate() != null && select.getEndDate() != null) {
            wrapper.between("create_date", select.getStartDate(), select.getEndDate());
        }
        return wrapper;
    }
}
