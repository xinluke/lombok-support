package com.wangym.lombok.job.impl;
public interface SdkBlackListServiceClient {


    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    int deleteSdkBlackList(@PathVariable("id") Integer id);

}