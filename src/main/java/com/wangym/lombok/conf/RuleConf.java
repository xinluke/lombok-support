package com.wangym.lombok.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "rule")
public class RuleConf {
    /**
     * 是否开启feign name声明配置
     */
    private boolean feignNameEnable;
    /**
     * 是否开启request path声明配置
     */
    private boolean requestMappingPathEnable;
}
