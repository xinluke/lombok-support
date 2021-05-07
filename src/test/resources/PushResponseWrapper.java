package cn.jpush.portal.bean.push.callback;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Allen on 2016/9/27.
 */
@Slf4j
public class PushResponseWrapper {

    private static final int RESPONSE_CODE_NONE = -1;

    private static Gson _gson = new Gson();

    public int responseCode = RESPONSE_CODE_NONE;
    public String responseContent;

    public ErrorObject error; // error for non-200 response, used by new API

    public int rateLimitQuota;
    public int rateLimitRemaining;
    public int rateLimitReset;

    public String exceptionString;

    public String address;

    public void setRateLimit(String quota, String remaining, String reset) {
        if (null == quota) {
            return;
        }

        try {
            rateLimitQuota = Integer.parseInt(quota);
            rateLimitRemaining = Integer.parseInt(remaining);
            rateLimitReset = Integer.parseInt(reset);

            log.debug("JPush API Rate Limiting params - quota:{}, remaining:{}, reset:{}", quota, remaining, reset);
        } catch (NumberFormatException e) {
            log.debug("Unexpected - parse rate limiting headers error.");
        }
    }

    public void setErrorObject() {
        error = _gson.fromJson(responseContent, ErrorObject.class);
    }

    public boolean isServerResonse() {
        if (responseCode == 200) {
            return true;
        }
        if (responseCode > 0 && null != error && error.error.code > 0) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return _gson.toJson(this);
    }

    public class ErrorObject {
        public ErrorEntity error;
    }

    public class ErrorEntity {
        public int code;
        public String message;

        @Override
        public String toString() {
            return _gson.toJson(this);
        }
    }
}
