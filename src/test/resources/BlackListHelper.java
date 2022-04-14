package cn.jpush.http.report.helper;

import cn.jpush.http.report.bean.ExceptionEnum;
import cn.jpush.http.report.exception.BaseException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Allen on 2020/10/15.
 */
@Component
public class BlackListHelper {
    private static HashSet<String> blackListHashSet = new HashSet<>();

    @Value(value="${black.list}")
    public String blackList;

    @PostConstruct
    public void initBlackList(){
        if(StringUtils.isNotEmpty(blackList)){
            String[] blackListArray = blackList.split(",");
            blackListHashSet.addAll(Arrays.asList(blackListArray));
        }
    }

    public void verifyBlackListByAppKey(String appKey){
        if(StringUtils.isEmpty(appKey)){
            throw new BaseException(ExceptionEnum.FORBIDDEN);
        }
        if(blackListHashSet.contains(appKey)){
            throw new BaseException(ExceptionEnum.FORBIDDEN);
        }
    }
}
