package com.wangym.lombok.job;

import lombok.Getter;

/**
 * @author wangym
 * @version 创建时间：2018年11月8日 上午10:12:24
 */
@Getter
public class Metadata {
    private String annName;
    private String ImportPkg;

    public Metadata(String annName, String importPkg) {
        super();
        this.annName = annName;
        ImportPkg = importPkg;
    }

}