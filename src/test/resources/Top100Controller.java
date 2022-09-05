package cn.jiguang.community.iportal.controller.stat;

import cn.jiguang.community.base.data.model.ParamVo;
import cn.jiguang.community.base.data.model.top100.*;
import cn.jiguang.community.common.model.Page;
import cn.jiguang.community.iportal.bean.DownLoadUrl;
import cn.jiguang.community.iportal.common.HttpContextHelper;
import cn.jiguang.community.iportal.common.PermConst;
import cn.jiguang.community.iportal.common.RespCode;
import cn.jiguang.community.iportal.common.RespObject;
import cn.jiguang.community.iportal.service.Top100Service;
import cn.jiguang.perm.annotation.RequireTab;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/top")
@Slf4j
@Api(description = "TOP100")
public class Top100Controller {

    @Autowired
    private Top100Service top100Service;


    @RequireTab(PermConst.TAB_积分榜)
    @GetMapping("/getIntegralList")
    @ApiOperation("积分榜")
    public RespObject<Page<IntegralRank>> getIntegralList(@RequestParam("currentPage") Integer currentPage,
                                                          @RequestParam("pageSize") Integer pageSize) {
        log.info("getIntegralList param：{}, {}", currentPage, pageSize);
        Page<IntegralRank> result = top100Service.getIntegralRankList(currentPage, pageSize);
        return HttpContextHelper.buildGenericResponse(RespCode.OK, result);
    }

    @RequireTab(PermConst.TAB_积分榜)
    @GetMapping("/getIntegralList/downloadUrl")
    @ApiOperation("积分榜-导出")
    public RespObject<DownLoadUrl> getIntegralListDownloadUrl() {
        log.info("getIntegralListDownloadUrl");

        String url = top100Service.getIntegralListDownloadUrl();

        return HttpContextHelper.buildGenericResponse(RespCode.OK, new DownLoadUrl(url));
    }


    @RequireTab(PermConst.TAB_粉丝榜)
    @GetMapping("/getFansList")
    @ApiOperation("粉丝榜")
    public RespObject<Page<FansRank>> getFansList(@RequestParam("currentPage") Integer currentPage,
                                                  @RequestParam("pageSize") Integer pageSize) {
        log.info("getFansRankList param：{}, {}", currentPage, pageSize);
        Page<FansRank> result = top100Service.getFansRankList(currentPage, pageSize);

        return HttpContextHelper.buildGenericResponse(RespCode.OK, result);
    }

    @RequireTab(PermConst.TAB_粉丝榜)
    @GetMapping("/getFansList/downloadUrl")
    @ApiOperation("粉丝榜-导出")
    public RespObject<DownLoadUrl> getFansListDownloadUrl() {
        log.info("getFansListDownloadUrl");
        String url = top100Service.getFansListDownloadUrl();

        return HttpContextHelper.buildGenericResponse(RespCode.OK, new DownLoadUrl(url));
    }

    @RequireTab(PermConst.TAB_提问榜)
    @GetMapping("/getQuestionList")
    @ApiOperation("提问榜")
    public RespObject<Page<QuestionRank>> getQuestionList(@RequestParam("currentPage") Integer currentPage,
                                                          @RequestParam("pageSize") Integer pageSize) {
        log.info("getQuestionRankList param：{}, {}", currentPage, pageSize);
        Page<QuestionRank> result = top100Service.getQuestionRankList(currentPage, pageSize);

        return HttpContextHelper.buildGenericResponse(RespCode.OK, result);
    }

    @RequireTab(PermConst.TAB_提问榜)
    @GetMapping("/getQuestionList/downloadUrl")
    @ApiOperation("提问榜-导出")
    public RespObject<DownLoadUrl> getQuestionListDownloadUrl() {
        log.info("getQuestionListDownloadUrl");
        String url = top100Service.getQuestionListDownloadUrl();

        return HttpContextHelper.buildGenericResponse(RespCode.OK, new DownLoadUrl(url));
    }

    @RequireTab(PermConst.TAB_回答榜)
    @GetMapping("/getAnswerList")
    @ApiOperation("回答榜")
    public RespObject<Page<AnswerRank>> getAnswerList(@RequestParam("currentPage") Integer currentPage,
                                                      @RequestParam("pageSize") Integer pageSize) {
        log.info("getAnswerRankList param：{}, {}", currentPage, pageSize);
        Page<AnswerRank> result = top100Service.getAnswerRankList(currentPage, pageSize);

        return HttpContextHelper.buildGenericResponse(RespCode.OK, result);
    }

    @RequireTab(PermConst.TAB_回答榜)
    @GetMapping("/getAnswerList/downloadUrl")
    @ApiOperation("回答榜-导出")
    public RespObject<DownLoadUrl> getAnswerListDownloadUrl() {
        log.info("getAnswerListDownloadUrl");
        String url = top100Service.getAnswerListDownloadUrl();

        return HttpContextHelper.buildGenericResponse(RespCode.OK, new DownLoadUrl(url));
    }

    @RequireTab(PermConst.TAB_作者榜)
    @GetMapping("/getAuthorList")
    @ApiOperation("作者榜")
    public RespObject<Page<AuthorRank>> getAuthorList(@RequestParam("currentPage") Integer currentPage,
                                                      @RequestParam("pageSize") Integer pageSize) {
        log.info("getAuthorList param：{}, {}", currentPage, pageSize);

        Page<AuthorRank> result = top100Service.getAuthorRankList(currentPage, pageSize);

        return HttpContextHelper.buildGenericResponse(RespCode.OK, result);
    }

    @RequireTab(PermConst.TAB_作者榜)
    @GetMapping("/getAuthorList/downloadUrl")
    @ApiOperation("作者榜-导出")
    public RespObject<DownLoadUrl> getAuthorListDownloadUrl() {
        log.info("getAuthorListDownloadUrl");
        String url = top100Service.getAuthorListDownloadUrl();

        return HttpContextHelper.buildGenericResponse(RespCode.OK, new DownLoadUrl(url));
    }


}
