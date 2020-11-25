package com.wangym.community.brush.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DateUtil {

    public static Date fixedTimezone(Date d) {
        return new Date(d.getTime() + 8 * 60 * 60_000L);
    }

    public static void main(String[] args) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date origin = sdf.parse("2020-01-09 02:58:22");
        Date target = fixedTimezone(origin);
        log.info("print:{}", sdf.format(target));
    }

    public static Date parseDate(String time) throws ParseException {
        return new SimpleDateFormat("yyyyMMdd").parse(time);
    }
}
