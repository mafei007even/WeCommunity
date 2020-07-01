package com.aatroxc.wecommunity.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author mafei007
 * @date 2020/5/17 22:21
 */


public final class DateUtils {

	public static String now(DateTimeFormatter formatter) {
		return LocalDateTime.now().format(formatter);
	}

	public static LocalDate date2LocalDate(Date date) {
		if(null == date) {
			return null;
		}
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

}
