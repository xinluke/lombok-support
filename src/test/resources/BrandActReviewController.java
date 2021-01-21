package com.wangym.controller.brand;

import com.wangym.client.brand.BrandActReviewFeignClient;
import com.wangym.common.BrandIportalInitBinder;
import com.wangym.model.DivideQueryReqModel;
import com.wangym.model.DivideQueryResModel;
import com.wangym.model.ResCodeModel;
import com.wangym.model.brand.BrandReqFilterCondModel;
import com.wangym.model.brand.iportal.request.BrandReqActReviewModel;
import com.wangym.model.brand.iportal.request.BrandReqStatusChangeModel;
import com.wangym.model.brand.iportal.response.BrandResActReviewEditModel;
import com.wangym.model.brand.iportal.response.BrandResBusRecordModel;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

@RestController
@Slf4j
public class BrandActReviewController extends BrandIportalInitBinder {

    @Autowired
    private BrandActReviewFeignClient actReviewFeignClient;

    @ApiOperation(value = "获取活动回顾编辑页")
    @GetMapping("/brand/actreview/edit/{uid}")
    public BrandResActReviewEditModel getActReviewEdit(@PathVariable("uid") Long uid) {
        log.info("getActReviewEdit uid: {}", uid);
        return actReviewFeignClient.getActReviewEdit(uid);
    }

    /**
     * 保存活动回顾信息
     * @param reqModel
     * @return
     */

    @ApiOperation("保存活动回顾")
    @PostMapping("/brand/actreview/save")
    public ResponseEntity<ResCodeModel> saveActReview(@Valid @RequestBody BrandReqActReviewModel reqModel,
        @RequestAttribute("userName") String operator) {
        reqModel.setCreator(operator);
        log.info("saveActReview reqModel: {}", JSON.toJSONString(reqModel));
        ResponseEntity<ResCodeModel> result = actReviewFeignClient.saveActReview(reqModel);
        return new ResponseEntity(result.getBody(), result.getStatusCode());
    }

    /**
     * 更改活动回顾状态
     * @param reqModel
     * @return
     */

    @PostMapping("/brand/actreview/change")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "uid", dataType = "Long", paramType = "query"),
        @ApiImplicitParam(name = "pstatus", dataType = "Integer", paramType = "query", value = "0: 未发布，1： 已发布"),
        @ApiImplicitParam(name = "astatus", dataType = "Integer", paramType = "query", value = "0: 未推荐, 1: 推荐"),
        @ApiImplicitParam(name = "tstatus", dataType = "Integer", paramType = "query", value = "0: 未置顶, 1: 置顶"),
        @ApiImplicitParam(required = true, name = "delete", dataType = "Integer", paramType = "query", value = "0: 不删除, 1: 删除")})
    public ResponseEntity<ResCodeModel> changeActReviewStatus(@ApiIgnore @RequestBody BrandReqStatusChangeModel reqModel) {
        log.info("changeActReviewStatus reqModel: {}", JSON.toJSONString(reqModel));
        ResponseEntity<ResCodeModel> result = actReviewFeignClient.changeActReviewStatus(reqModel);
        return new ResponseEntity(result.getBody(), result.getStatusCode());
    }

    /**
     * 获取活动回顾记录
     * @param reqModel
     * @return
     */

    @ApiOperation(value = "获取活动回顾")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "currentPage", dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "pageSize", dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "content", dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "pstatus", dataType = "Integer", paramType = "query", value = "0: 未发布，1： 已发布"),
        @ApiImplicitParam(name = "astatus", dataType = "Integer", paramType = "query", value = "0: 未推荐, 1: 推荐"),
        @ApiImplicitParam(name = "tstatus", dataType = "Integer", paramType = "query", value = "0: 未置顶, 1: 置顶"),
        @ApiImplicitParam(name = "labels", allowMultiple = true, dataType = "Integer", paramType = "query")})
    @GetMapping("/brand/actreview/retieve")
    public DivideQueryResModel<BrandResBusRecordModel> getActReview(@ApiIgnore @ModelAttribute(binding = false) DivideQueryReqModel<BrandReqFilterCondModel> reqModel) {
        log.info("getActReview reqModel: {}", JSON.toJSONString(reqModel));
        return actReviewFeignClient.getActReviewRecord(reqModel);
    }

}
