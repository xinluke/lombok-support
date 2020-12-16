package com.wangym.portal.client;

import com.wangym.jmlink.base.data.enums.UniversalLinkType;
import com.wangym.jmlink.base.data.model.env.BaseAppEnvParam;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestTemplate;
import feign.Retryer;
import feign.codec.EncodeException;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Type;

@FeignClient(name = "jmlink-portal",
        configuration = {
                JmlinkPortalClient.FeignRetryingConfig.class
        })
public interface JmlinkPortalClient {

    @PostMapping(value = "/jmlink-portal/v1/jmlink/app/{appKey}/env/android/integrate")
    HttpEntity addAndroid(@PathVariable("appKey") String appKey,
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
