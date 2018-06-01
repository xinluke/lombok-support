package com.wangym.lombok.job;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import com.wangym.lombok.job.log.LoggerPackage;
import com.wangym.lombok.job.log.LoggerVerify;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wangym
 * @version 创建时间：2018年5月14日 上午10:30:27
 */
@Component
@Slf4j
public class ReplaceLoggerJob {

	@Autowired
	private List<LoggerVerify> loggerVerify;

	public void handle(File file) throws IOException {
		// 以utf-8的编码方式读取文件
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"))) {
			br.mark((int) (file.length() + 1));
			String line;
			String className = null;
			String classLine = null;
			LoggerPackage lpkg = null;
			boolean annotationExist = false;
			while ((line = br.readLine()) != null) {
				className = getClassName(line);
				if (className != null) {
					classLine = line;
					log.info("class name:{}", className);
					break;
				}
			}
			br.reset();
			while ((line = br.readLine()) != null) {
				lpkg = getLoggerVal(line, className);
				if (lpkg != null) {
					log.info("logger config:{}", lpkg);
					break;
				}
			}
			br.reset();
			while ((line = br.readLine()) != null) {
				if (line.contains("@Slf4j")) {
					annotationExist = true;
					break;
				}
			}
			// 不符合条件立刻终止
			if (className == null || lpkg == null) {
				return;
			}
			StringBuffer newBody = new StringBuffer();
			String loggerName = lpkg.getLoggerName();
			log.info("当前文件符合转换,class name:{},logger name:{}", className, loggerName);
			String target = loggerName + ".";
			// 重置至mark点
			br.reset();
			while ((line = br.readLine()) != null) {
				if (line.contains(lpkg.getImportClassName())) {
					if (annotationExist) {
						continue;
					}
					newBody.append("import lombok.extern.slf4j.Slf4j;");
				} else if (line.contains(classLine)) {
					if (!annotationExist) {
						newBody.append("@Slf4j\n");
					}
					newBody.append(classLine);
				} else if (line.contains(lpkg.getLoggerdefined())) {
					continue;
				} else if (line.contains("import org.slf4j.Logger;")) {
					continue;
				} else {
					newBody.append(line.replace(target, "log."));
				}
				newBody.append("\n");

			}
			// 以utf-8编码的方式写入文件中
			FileCopyUtils.copy(newBody.toString().getBytes("utf-8"), file);
			// System.out.println(newBody.toString());
		}
	}

	private LoggerPackage getLoggerVal(String line, String className) {
		if (className == null) {
			return null;
		}
		for (LoggerVerify logger : loggerVerify) {
			if (logger.test(line, className)) {
				String leftText = line.split("=")[0];
				String[] arr = leftText.split("\\s+");
				// 取最后一个单词即是logger变量的名字
				LoggerPackage pkg =new LoggerPackage(logger.getImportClassName(), arr[arr.length - 1], line);
				return pkg;
			}
		}
		return null;
	}

	private String getClassName(String line) {
		if (testExistClassName(line)) {
			String[] arr = line.split("\\s+");
			// 第三个单词是类名
			String className = arr[2];
			if (className.endsWith("{")) {
				className = className.substring(0, className.length() - 1);
			}
			return className;
		} else {
			return null;
		}
	}

	private boolean testExistClassName(String line) {
		Pattern p = Pattern.compile("public\\s+(class|enum)\\s+\\w.*");
		Matcher matcher = p.matcher(line);
		return matcher.find();
	}
}
