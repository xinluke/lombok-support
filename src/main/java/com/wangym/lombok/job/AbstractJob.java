package com.wangym.lombok.job;

/**
 * @author wangym
 * @version 创建时间：2018年8月4日 下午5:33:47
 */
public abstract class AbstractJob implements Job {
    private String suffix;

    @Override
    public boolean canRead(String fileName) {
        if (fileName.endsWith(suffix)) {
            return true;
        } else {
            return false;
        }
    }

    public AbstractJob(String suffix) {
        super();
        this.suffix = suffix;
    }

}
