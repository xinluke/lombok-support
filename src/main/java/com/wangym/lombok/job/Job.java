package com.wangym.lombok.job;

import java.io.File;
import java.io.IOException;

/**
* @author  wangym
* @version 创建时间：2018年6月28日 下午1:20:55
*/
public interface Job {

    boolean canRead(String fileName);

    void exec(File file) throws IOException;

}