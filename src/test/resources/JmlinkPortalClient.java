package com.wangym.portal.client;

import com.wangym.jmlink.base.data.model.env.BaseAppEnvParam;
import feign.Retryer;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "jmlink-portal",
        configuration = {
                JmlinkPortalClient.FeignRetryingConfig.class
        })
public interface JmlinkPortalClient {

    //这是一个单行注释
    @PostMapping(value = "/jmlink-portal/v1/jmlink/app/{appKey}/env/android/integrate")
    HttpEntity addAndroid(@PathVariable("appKey") String appKey,
                          @RequestBody AndroidParam param);
    /*
    这是一个多行注释
     */
    @PostMapping(value = "/jmlink-portal/v1/jmlink/app/{appKey}/env/android/integrate2")
    HttpEntity addAndroid2(@PathVariable("appKey") String appKey,
                          @RequestBody AndroidParam param);

    @EqualsAndHashCode(callSuper = true)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class AndroidParam extends BaseAppEnvParam {
        @ApiModelProperty(value = "配置id")
        private Integer id;
        @ApiModelProperty(value = "应用包名")
        private String packageName;
        @ApiModelProperty(value = "Android URL Scheme")
        private String androidUrlScheme;
        @ApiModelProperty(value = "Android下载地址")
        private String androidDownloadUrl;
        @ApiModelProperty(value = "sha256_cert_fingerprints配置")
        private String androidSha256CertFingerprints;
        @ApiModelProperty(value = "andriod是否开启应用宝跳转")
        private boolean yybAndroid;

        private String authorization;

    }




    class FeignRetryingConfig {
        @Bean
        public Retryer feignRetryer() {
            return new Retryer.Default(1000L, 5000L, 1);
        }
    }

}
