package cn.jpush.stat.service.model.sdk;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.DateTimeException;
import java.util.Date;

/**
 * Created by Allen on 2020/2/3.
 */
@Data
public class PluginUpdateForm {
    private Integer currentPage = 1;
    private Integer pageSize = 10;
    @ApiModelProperty("插件类型,1:jcore,2:jpush")
    private Integer pluginType;
    @ApiModelProperty("sdk类型，push,core")
    private String sdkType;
    @ApiModelProperty("插件版本,例：221")
    private String pluginVer;
    @ApiModelProperty( "开始时间日期,如：20190128")
    private Integer startTime;
    @ApiModelProperty( "结束时间日期,如：20190128")
    private Integer endTime;
    @ApiModelProperty(name= "状态类型，插件更新或者加载",hidden = true)
    private PluginType statusType;
    @ApiModelProperty(value = "插件版本,例：221",hidden = true)
    private Integer pluginVersion;
}
