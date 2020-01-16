package com.wangym.lombok.job.impl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Api(description = "helloworld相关的API")
@RestController
@RequestMapping("/v1/validate/apps/{appKey}/helloworld")
@Slf4j
public class RequestMappingExample {

    // @ApiOperation(value = "删除信息")
    @DeleteMapping(value="/{id}")
    public ValidateSuccessResult delete(@PathVariable("appKey") String appKey, @RequestParam Integer id) {
        return null;
    }

    @ApiOperation("修改信息")
    @PutMapping(value="/{id}")
    public ValidateSuccessResult update(@PathVariable("appKey") String appKey, @RequestParam Integer id,
            @RequestBody AndroidAuthParam param) {
        return null;
    }

    // @ApiOperation(value = "提交信息")
    @PostMapping(value="/")
    public ValidateSuccessResult create(@PathVariable("appKey") String appKey, @RequestBody IOSAuthParam param) {
        return null;
    }

    // @ApiOperation(value = "查询信息")
    @GetMapping(value="/{id}")
    public AuthInfo get(@PathVariable("appKey") String appKey, @RequestParam Integer id) {
        return null;
    }

}
