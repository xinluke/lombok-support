package cn.jpush.stat.bean.smart.operation.marketingplan.component.sms;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 运营计划-消息设置：短信推送请求入参
 *
 * 备注：创建运营计划也需要保存--当需要 发送任务时就可以查询出来组转透传给push
 */
@Data
public class PushSmsConfig {

    @ApiModelProperty(value = "签名id")
    private Long signId;

    @ApiModelProperty(value = "模板id")
    private Long tempId;


    @ApiModelProperty(value = "动态参数")
    private List<SmsTempAttributePara> tempPara;

    @Data
    public static class SmsTempAttributePara {

        @ApiModelProperty(value = "短信动态参数 key")
        private String key;

        @ApiModelProperty(value = "index")
        private Integer index;

        @ApiModelProperty(value = "短信动态参数值")
        private String value;

        @ApiModelProperty(value = "短信动态参数类型 1:普通属性 2：用户属性")
        private Integer valueType;

        @ApiModelProperty(value = "短信动态参数-属性备用值")
        private String backupValue;

    }
}
