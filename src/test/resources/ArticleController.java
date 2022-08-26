package cn.jiguang.community.iportal.controller;

import cn.jiguang.community.base.data.model.ArticleEntity;
import cn.jiguang.community.base.data.model.ParamVo;
import cn.jiguang.community.base.data.model.ViolateEntity;
import cn.jiguang.community.common.enums.TargetTypeEnum;
import cn.jiguang.community.common.model.Page;
import cn.jiguang.community.iportal.bean.ArticleModel;
import cn.jiguang.community.iportal.bean.ViolateParam;
import cn.jiguang.community.iportal.common.PermConst;
import cn.jiguang.community.iportal.common.RespObject;
import cn.jiguang.community.iportal.common.HttpContextHelper;
import cn.jiguang.community.iportal.common.RespCode;
import cn.jiguang.community.iportal.service.ArticleService;
import cn.jiguang.perm.annotation.RequireButton;
import cn.jiguang.perm.annotation.RequireTab;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/article")
@Slf4j
@Api(description = "文章管理")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @RequireTab(PermConst.TAB_文章管理)
    @GetMapping("/getArticleList")
    @ApiOperation("根据条件搜索文章信息列表")
    public RespObject<Page<ArticleModel>> getArticleList(@ApiParam("状态") Integer status, @ApiParam("文章id") Long id,
                                                                 @RequestParam("currentPage") Integer currentPage,
                                                                 @RequestParam("pageSize") Integer pageSize) {

        ParamVo param = new ParamVo();
        param.setId(id);
        param.setCurrentPage(currentPage);
        param.setStatus(status);
        param.setPageSize(pageSize);
        log.info("getArticleList，param={}", param);
        Page<ArticleModel> answerList =  articleService.getArticleList(param);
        return HttpContextHelper.buildGenericResponse(RespCode.OK,answerList);
    }

    @RequireTab(PermConst.TAB_文章管理)
    @GetMapping("/audit")
    @ApiOperation("获取审核内容")
    public RespObject<ViolateEntity> getArticleAudit(@ApiParam("文章id")@RequestParam("targetId") Long targetId) {
        ParamVo param = new ParamVo();
        param.setTargetId(targetId);
        param.setTargetType(TargetTypeEnum.文章.getValue());
        log.info("getAnswerAudit，targetId={}, targetType={}", param);
        ViolateEntity entity =  articleService.getArticleAudit(param);
        return HttpContextHelper.buildGenericResponse(RespCode.OK,entity);
    }


    @RequireButton(PermConst.按钮_文章管理修改)
    @PostMapping("/audit")
    @ApiOperation("修改或者增加审核内容")
    public RespObject<ViolateEntity> updateArticleAudit(@RequestBody ViolateParam param) {
        int status = param.getStatus();
        ViolateEntity entity = new ViolateEntity();
        entity.setViolateNote(param.getViolateNote());
        entity.setAuditStatus(status);
        entity.setTargetId(param.getTargetId());
        entity.setId(param.getId());
        entity.setTargetType(TargetTypeEnum.文章.getValue());
        log.info("updateQuestionAudit:{}", entity);
        articleService.updateArticleAudit(entity);
        return HttpContextHelper.buildGenericResponse(RespCode.OK);
    }

    @RequireTab(PermConst.TAB_文章管理)
    @GetMapping("/getArticle")
    @ApiOperation("获取审核的文章内容")
    public RespObject<ArticleEntity> getArticleById(@ApiParam("文章id") @RequestParam("id") Long id){
        log.info("getAnswerById:{}", id);
        ArticleEntity entity = articleService.getArticleById(id);
        return HttpContextHelper.buildGenericResponse(RespCode.OK,entity);
    }

}
