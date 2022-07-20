package com.wangym.lombok.job.impl;

import lombok.Data;

/**
 * @Author: wangym
 * @Date: 2022/7/20 19:21
 */
@Data
public class ReplaceModel {
    //原始依赖
    private DepdencyModel source;
    //目标依赖
    private DepdencyModel target;

}
