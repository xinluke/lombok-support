package cn.jpush.client;

import cn.jpush.portal.client.AppServiceClient;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "portal-base-data-server")
public interface AppFeignClient extends AppServiceClient{
}
