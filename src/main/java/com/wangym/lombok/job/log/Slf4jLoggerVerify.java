package com.wangym.lombok.job.log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class Slf4jLoggerVerify extends LoggerVerify {

	public Slf4jLoggerVerify() {
		super("import org.slf4j.LoggerFactory;");
	}

	public boolean test(String line, String className) {
		Pattern p = Pattern.compile("=\\s*LoggerFactory.getLogger\\(" + className + "\\.class(\\.getName\\(\\))*\\)");
		Matcher matcher = p.matcher(line);
		return matcher.find();
	}
}
