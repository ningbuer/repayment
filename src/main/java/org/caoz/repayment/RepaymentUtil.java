package org.caoz.repayment;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

public class RepaymentUtil {

	public static int diffDay(Date date1, Date date2) {
		Date d1 = DateUtils.truncate(date1, Calendar.DAY_OF_MONTH);
		Date d2 = DateUtils.truncate(date2, Calendar.DAY_OF_MONTH);
		int day = (int) ((d1.getTime() - d2.getTime()) / (24 * 60 * 60 * 1000));
		return day;
	}

	public static BigDecimal toleranceLimit = new BigDecimal(0.1);
	
	public static final SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
}
