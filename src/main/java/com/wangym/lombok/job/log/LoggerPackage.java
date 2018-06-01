package com.wangym.lombok.job.log;

import lombok.Getter;

@Getter
public class LoggerPackage {

	private String importClassName;
	private String loggerName;
	private String loggerdefined;

	public LoggerPackage(String importClassName, String loggerName, String loggerdefined) {
		super();
		this.importClassName = importClassName;
		this.loggerName = loggerName;
		this.loggerdefined = loggerdefined;
	}

}
