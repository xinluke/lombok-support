package com.wangym.lombok.job.log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class Log4jLoggerVerify extends LoggerVerify {

	public Log4jLoggerVerify() {
		super("import org.apache.log4j.Logger;");
	}

	@Override
	public boolean test(String line, String className) {
		Pattern p = Pattern.compile("=\\s*Logger.getLogger\\(" + className + "\\.class(\\.getName\\(\\))*\\)");
		Matcher matcher = p.matcher(line);
		return matcher.find();
	}
}
