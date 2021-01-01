package com.wangym.community.core.controller;

import com.wangym.community.base.data.model.AnswerResult;
import com.wangym.community.common.enums.TargetTypeEnum;
import com.wangym.community.common.model.Page;
import com.wangym.community.common.model.RespObject;
import com.wangym.community.core.common.*;
import com.wangym.community.core.entity.question.AnswerForm;
import com.wangym.community.core.entity.question.AnswerReplyForm;
import com.wangym.community.core.entity.question.AnswerReplyModel;
import com.wangym.community.core.service.AnswerService;
import com.wangym.community.core.service.BlackWordHitService;
import com.wangym.community.core.service.UserService;
import com.wangym.community.user.interceptor.Anonymous;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/question/answer")
@Slf4j
@Api(description = "答案及其回复操作")
public class AnswerController {

    @Resource
    private AnswerService answerService;
    @Autowired
    private UserService userService;

    @Autowired
    private BlackWordHitService blackWordService;

    @Anonymous
    @GetMapping("/answerId")
    @ApiOperation("获取指定回答")
    public RespObject<AnswerResult> getByAnswerId(@RequestAttribute(value = "userId", required = false) Long userId,
                                                  @RequestParam("answerId") Long answerId) {
        log.info("获取指定回答，answerId={}", answerId);
        AnswerResult result = answerService.getByAnswerId(answerId, userId);
        log.debug("指定回答: result");
        return HttpContextHelper.buildGenericResponse(RespCode.OK, result);
    }

    @GetMapping("/mine")
    @ApiOperation("获取自己的回答")
    public RespObject<AnswerResult> getSelfAnswer(@RequestAttribute(value = "userId") Long userId,
                                                  @RequestParam(value = "questionId") Long questionId) {
        log.info("获取自己的回答，userId={}, questionId={}", userId, questionId);
        AnswerResult result = answerService.getSelfAnswer(userId, questionId);
        return HttpContextHelper.buildGenericResponse(RespCode.OK, result);
    }

    @GetMapping("/reply/like")
    @ApiOperation("点赞回复")
    public RespObject likeReply(@RequestAttribute("userId") Long userId, @RequestParam("id") Long id) {
        log.info("点赞问题，userId={}, id={}", userId, id);
        answerService.likeReply(id, userId);
        return HttpContextHelper.buildGenericResponse(RespCode.OK);
    }

    /**
     * 操作答案
     *
     * @param id
     * @param operation 收藏-favorite、点赞-like
     * @param userId
     * @return
     */
    @GetMapping(value = "/{operation}/{id}")
    @ApiOperation("操作答案(收藏-favorite、点赞-like)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "问题ID", paramType = "path"),
            @ApiImplicitParam(name = "operation", paramType = "path", value = "操作方式(收藏-favorite、点赞-like)")
    })
    public RespObject userPrefer(@PathVariable("id") Long id,
                                 @PathVariable("operation") String operation,
                                 @RequestAttribute("userId") Long userId) {
        log.info("操作答案(收藏-favorite、关注-focus、点赞-like), questionId={}, operation={}, userId={}",
                id, operation, userId);
        answerService.operateAnswer(operation, id, userId);
        return HttpContextHelper.buildGenericResponse(RespCode.OK);
    }

    /**
     * 拉取问题回答信息
     *
     * @param userId
     * @param questionId
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @Anonymous
    @GetMapping(value = "/{questionId}")
    @ApiOperation("拉取问题的答案")
    public RespObject<Page<AnswerResult>> findQuestionAnswer(@RequestAttribute(value = "userId", required = false) Long userId,
                                                             @PathVariable("questionId") Long questionId,
                                                             @RequestParam(value = "sort", defaultValue = "1") Integer sort,
                                                             @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                                             @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        log.info("获取问题={}的回答", questionId);
        AnswerSortEnum answerSortEnum = AnswerSortEnum.valueOf(sort);
        Page<AnswerResult> page = answerService.findAnswer(questionId, userId, answerSortEnum, pageIndex, pageSize);
        return HttpContextHelper.buildGenericResponse(RespCode.OK, page);
    }

    /**
     * 拉取回复信息
     *
     * @param userId
     * @param answerId
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @Anonymous
    @GetMapping(value = "/reply/{answerId}")
    @ApiOperation("拉取问题答案的回复内容")
    public RespObject<Page<AnswerReplyModel>> findAnswerReply(@RequestAttribute(value = "userId", required = false) Long userId,
                                                              @PathVariable("answerId") Long answerId,
                                                              @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                                              @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        log.info("获取回答={}的回复", answerId);
        Page<AnswerReplyModel> page = answerService.findAnswerReply(answerId, userId, pageIndex, pageSize);
        return HttpContextHelper.buildGenericResponse(RespCode.OK, page);
    }


    @PostMapping(value = "")
    @ApiOperation("回答问题")
    @ApiLimit(name = ApiLimitConstants.ANSWER_OR_COMMENT_OR_REPLY, limit = 5)
    public RespObject answerQuestion(@RequestAttribute("userId") Long userId, @Valid @RequestBody AnswerForm answerForm) {


        blackWordService.parseBlackWord(answerForm.getAnswerContent(), userId, TargetTypeEnum.答案);
        userService.checkMute(userId);
        answerForm.setUserId(userId);
        log.info("回答问题: {}", answerForm);
        Long answerId = answerService.answerQuestion(answerForm);
        Map<String, Object> result = new HashMap<>(1);
        result.put("answerId", answerId);
        return HttpContextHelper.buildGenericResponse(RespCode.OK, result);
    }

    @PostMapping(value = "/modify")
    @ApiOperation("修改回答")
    public RespObject modifyAnswer(@RequestAttribute("userId") Long userId,
                                   @RequestBody AnswerForm answerForm) {
        blackWordService.parseBlackWord(answerForm.getAnswerContent(), userId, TargetTypeEnum.答案);
        log.info("修改回答内容: {}, userId: {}", answerForm.getAnswerContent(), userId);
        answerService.modifyAnswer(userId, answerForm.getAnswerId(), answerForm.getAnswerContent());
        return HttpContextHelper.buildGenericResponse(RespCode.OK);
    }

    @PutMapping(value = "/accept")
    @ApiOperation("采纳答案")
    public RespObject acceptAnswer(@RequestAttribute("userId") Long userId,
                                   @RequestParam("questionId") Long questionId,
                                   @RequestParam("answerId") Long answerId) {
        log.info("采纳答案, 问题id={}, 答案ID={}, userId={}", questionId, answerId, userId);
        answerService.selfCheck(userId, questionId, answerId);
        answerService.acceptAnswer(userId, questionId, answerId);
        return HttpContextHelper.buildGenericResponse(RespCode.OK);
    }

    @DeleteMapping(value = "/accept")
    @ApiOperation("取消采纳答案")
    public RespObject cancelAcceptAnswer(@RequestAttribute("userId") Long userId,
                                         @RequestParam("questionId") Long questionId,
                                         @RequestParam("answerId") Long answerId) {
        log.info("取消采纳答案, 问题id={}, 答案ID={}, userId={}");
        answerService.selfCheck(userId, questionId, answerId);
        answerService.cancelAcceptAnswer(userId, answerId);
        return HttpContextHelper.buildGenericResponse(RespCode.OK);
    }

    @GetMapping(value = "/remove")
    @ApiOperation("删除回答")
    public RespObject deleteAnswer(@RequestAttribute("userId") Long userId,
                                   @RequestParam("answerId") Long answerId) {
        log.info("修改回答内容，userId={}, answerId={}", userId, answerId);
        answerService.removeAnswer(userId, answerId);
        return HttpContextHelper.buildGenericResponse(RespCode.OK);
    }

    @PostMapping(value = "/reply")
    @ApiOperation("回复回答")
    @ApiLimit(name = ApiLimitConstants.ANSWER_OR_COMMENT_OR_REPLY, limit = 5)
    public RespObject replyAnswer(@RequestAttribute("userId") Long userId, @Valid @RequestBody AnswerReplyForm form) {
        form.setUserId(userId);
        form.setReplyContent(form.getReplyContent());
        log.info("回复回答: {}", form);
        blackWordService.parseBlackWord(form.getReplyContent(), userId, TargetTypeEnum.答案的回复);
        Long replyId = answerService.replyAnswer(form);
        Map<String, Object> result = new HashMap<>(1);
        result.put("replyId", replyId);
        return HttpContextHelper.buildGenericResponse(RespCode.OK, result);
    }

    @PostMapping(value = "/reply/modify")
    @ApiOperation("修改回复")
    public RespObject modifyAnswerReply(@RequestAttribute("userId") Long userId,
                                        @RequestParam("replyId") Long replyId,
                                        @RequestParam("answerContent") String content) {
        blackWordService.parseBlackWord(content, userId, TargetTypeEnum.答案);
        content = HtmlUtils.htmlEscapeHex(content);
        log.info("修改回复，userId={}, answerId={}, content={}", userId, replyId, content);
        answerService.modifyAnswerReply(userId, replyId, content);
        return HttpContextHelper.buildGenericResponse(RespCode.OK);
    }

    @GetMapping(value = "/reply/remove")
    @ApiOperation("删除回复")
    public RespObject deleteAnswerReply(@RequestAttribute("userId") Long userId,
                                        @RequestParam("replyId") Long replyId) {
        log.info("删除回复，userId={}, replyId={}", userId, replyId);
        answerService.removeAnswerReply(userId, replyId);
        return HttpContextHelper.buildGenericResponse(RespCode.OK);
    }

}
