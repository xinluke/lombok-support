package com.wangym.lombok.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "rule")
@Configuration
public class RuleConf {
    /**
     * 是否开启feign name声明配置
     */
    private boolean feignNameEnable;
    /**
     * 是否开启request path声明配置
     */
    private boolean requestMappingPathEnable;

    /**
     * 是否开启javadoc注释配置
     * 如果开启，则会在方法上面，将单行注释转化为javadoc注释
     */
    private boolean singleComment2javadocEnable;
    /**
     * 是否要将类上面的url和方法拼接到一起
     */
    private boolean enableMergeRequestUrl;
    /**
     * 有些开发者希望api返回的格式是固定的，所以直接在注解声明只支持json格式的数据响应
     * 避免有些开发者不清楚自己使用了Http的Accept协商头，而我们的api假如是多种返回格式的话，可能会返回xml或者其他格式的数据，而重点在于开发者不太了解http的协商机制，从而认为是我们的问题。
     * 现阶段使用json格式的数据应该是满足绝大部分的用户的需求
     */
    private boolean onlySupportJson;
    /**
     * 是否需要在每一个方法上面添加@ApiOperation注解
     */
    private boolean apiOperation;
    private boolean synchronizedAnnotationSupport;
    private boolean fullSearch;
    /**
     * 去除日志打印参数中的"JSON.toJSONString(list)"
     */
    private boolean deleteJsonWraped;
}
