package cn.jpush.jmlink.tracking.controller;

import com.wangym.jmlink.base.data.common.Constants;
import com.wangym.jmlink.base.data.model.shorturl.ShortUrlParam;
import com.wangym.jmlink.base.data.model.shorturl.ShortUrlResult;
import com.wangym.jmlink.tracking.cache.ShortUrlConfigCache;
import com.wangym.jmlink.tracking.common.RespCode;
import com.wangym.jmlink.tracking.helper.HttpContextHelper;
import com.wangym.jmlink.tracking.model.TrackingEventRequest;
import com.wangym.jmlink.tracking.model.TrackingPayload;
import com.wangym.jmlink.tracking.service.TrackingService;
import com.wangym.jmlink.tracking.utils.IpUtil;
import com.wangym.jmlink.tracking.utils.PlatformUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/v1/tracking")
@Slf4j
public class TrackingController {

    @Autowired
    private TrackingService trackingService;
    @Autowired
    private ShortUrlConfigCache shortUrlConfigCache;

    /**
     * JS上报接口
     *
     * @param param
     * @param request
     * @return
     */
    @ApiOperation(value = "JS上报接口")
    @GetMapping(value = "/i")
    public ResponseEntity track(@Valid @ModelAttribute TrackingEventRequest param,
                                HttpServletRequest request) {
        String shortUrl = param.getCid();
        if (StringUtils.isBlank(shortUrl)) {
            return null;
        }

        ShortUrlParam shortUrlParam = new ShortUrlParam();
        shortUrlParam.setShortUrl(shortUrl);
        shortUrlParam.setDeleted(Constants.NO);
        ShortUrlResult shortUrlResult = shortUrlConfigCache.getConfig(shortUrlParam);

        if (shortUrlResult == null) {
            return HttpContextHelper.buildResponse(RespCode.PARAMETER_ERROR);
        }

        TrackingPayload trackingPayload = new TrackingPayload();

        String ip = IpUtil.getRemoteIp(request);
        String ua = request.getHeader("User-Agent");
        trackingPayload.setIp(ip);
        trackingPayload.setUa(ua);
        trackingPayload.setPlatform(PlatformUtil.getPlatform(ua));
        trackingPayload.setItime(System.currentTimeMillis());

        trackingPayload.setAk(shortUrlResult.getAppKey());
        trackingPayload.setCid(shortUrl);
        trackingPayload.setCh(param.getCh().name());
        trackingPayload.setAction(param.getAction().name());

        log.info("上报数据,appKey:{},trackingPayload:{}", shortUrlResult.getAppKey(), trackingPayload);

        trackingService.tracking(trackingPayload);
        return HttpContextHelper.buildResponse(RespCode.OK);
    }

    //helloworld method
    private void helloworld() {
        log.info("helloworld");
    }

}
