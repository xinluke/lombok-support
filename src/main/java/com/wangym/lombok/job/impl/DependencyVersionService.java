package com.wangym.lombok.job.impl;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
@ConfigurationProperties("pom.version")
@Getter
@Setter
public class DependencyVersionService {

    // 升级的版本
    private List<VerModelData> updateList = new ArrayList<>();
    // 升级的版本
    private List<VerModelData> parentList = new ArrayList<>();
    // 删除的版本
    private List<VerModelData> deleteList = new ArrayList<>();
    // 这个版本托管为统一依赖的方式，声明受管理的ArtifactId
    private List<String> hiddenVersionArtifactIdList = new ArrayList<>();
    // 是否删除构建库配置
    private boolean deleteDistributionManagement;
    // 是否删除DependencyManagement配置
    private boolean deleteDependencyManagement;

}
