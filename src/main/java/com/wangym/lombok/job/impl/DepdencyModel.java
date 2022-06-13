package com.wangym.lombok.job.impl;

import lombok.Data;
import org.apache.maven.model.Dependency;

@Data
public class DepdencyModel {
    private String groupId;

    private String artifactId;

    private String version;

    public Dependency build(){
        Dependency dependency =new Dependency();
        dependency.setArtifactId(artifactId);
        dependency.setGroupId(groupId);
        dependency.setVersion(version);
        return dependency;
    }
}
