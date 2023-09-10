package cn.jpush.client;

import com.wangym.portal.client.AppServiceClient;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "portal-base-data-server")
public interface AppFeignClient extends AppServiceClient{
}
