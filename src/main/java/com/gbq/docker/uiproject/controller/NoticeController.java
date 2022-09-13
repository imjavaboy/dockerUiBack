package com.gbq.docker.uiproject.controller;


import com.baomidou.mybatisplus.plugins.Page;
import com.gbq.docker.uiproject.commons.util.CollectionUtils;
import com.gbq.docker.uiproject.commons.util.ResultVOUtils;
import com.gbq.docker.uiproject.domain.dto.NoticeDTO;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/11 21:26
 * @Copyright 总有一天，会见到成功
 */
@Api(tags = "通知模块")
@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    @ApiOperation("获取个人所有通知")
    @GetMapping("/list")
    public ResultVO listSelfNotice(Integer type,
                                   @RequestParam(defaultValue = "1") Integer current,
                                   @RequestParam(defaultValue = "10") Integer size,
                                   @RequestAttribute String uid) {
        Page<NoticeDTO> selectPage = noticeService.listSelfNotice(uid, type, new Page<>(current, size, "create_date", false));

        return ResultVOUtils.success(selectPage);
    }

    @ApiOperation("标记为已读")
    @PostMapping("/read")
    public ResultVO readSelect(@RequestAttribute String uid, String[] ids) {
        return noticeService.readNotice(ids, uid);
    }

    @ApiOperation("已读所有")
    @PostMapping("/readAll")
    public ResultVO readAll(@RequestAttribute String uid, Integer type) {
        return noticeService.readAllNotice(uid, type);
    }
    @ApiOperation("批量删除通知")
    @PostMapping("/delete")
    public ResultVO deleteNotices(String ids, @RequestAttribute String uid) {
        String[] idArray = ids.split(",");
        if(CollectionUtils.isArrayEmpty(idArray)) {
            return ResultVOUtils.error(ResultEnum.PARAM_ERROR);
        }
        return noticeService.deleteNotice(idArray, uid);
    }
    @ApiOperation("统计当前未读消息数量")
    @GetMapping("/countUnRead")
    public ResultVO countUnread(@RequestAttribute String uid) {
        return ResultVOUtils.success(noticeService.countUnread(uid));
    }

}
