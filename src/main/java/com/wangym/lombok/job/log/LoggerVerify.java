package com.wangym.lombok.job.log;

import lombok.Getter;

@Getter
public abstract class LoggerVerify {
	private String importClassName;

	public LoggerVerify(String importClassName) {
		super();
		this.importClassName = importClassName;
	}

	public abstract boolean test(String line, String className);

}
