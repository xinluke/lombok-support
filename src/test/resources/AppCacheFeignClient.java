package com.xinluke.validate.client;

import com.xinluke.cache.service.AppCacheService;
import org.springframework.cloud.netflix.feign.FeignClient;

@FeignClient(value = "appdev-cache-service", path = "/v1/appdev-cache")
public interface AppCacheFeignClient extends AppCacheService {
}