package com.flex.adapter.helper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DateUtil {

	private static Logger LOG = LoggerFactory.getLogger(DateUtil.class);

	private static String messageException = new String();

	public static String getCurrentDate(String format) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(cal.getTime());
	}

	public static Date getDate(String dateStr, String format) throws ParseException {
		DateFormat dateFormat = new SimpleDateFormat(format);
		return dateStr != null ? (Date) dateFormat.parse(dateStr) : null;
	}

	public static Date strToDate(String format, String dateStr) {
		Date date = null;
		try {
			DateFormat df = new SimpleDateFormat(format);
			date = df.parse(dateStr);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public static Date strToDate(String format, String dateStr, TimeZone timeZone) throws ParseException {
		DateFormat df = new SimpleDateFormat(format);
		df.setTimeZone(timeZone);
		return df.parse(dateStr);
	}

	public static Date xmlGregorianToDate(XMLGregorianCalendar xmlGregorian) {
		GregorianCalendar gregorianCalendar = xmlGregorian.toGregorianCalendar();
		return gregorianCalendar.getTime();
	}

	public static String dateToStr(String format, Date date) {

		DateFormat df = new SimpleDateFormat(format);
		return df.format(date);

	}

	public static String dateToStr(String format, Date date, TimeZone timeZone) throws ParseException {

		DateFormat df = new SimpleDateFormat(format);
		df.setTimeZone(timeZone);
		return df.format(date);

	}

	public static Date getPstDate(Date date) {
		if (date == null)
			return null;
		Calendar c1 = Calendar.getInstance();
		c1.setTime(date);
		Calendar c2 = Calendar.getInstance(TimeZone.getTimeZone("PST"));
		c2.setTimeInMillis(c1.getTimeInMillis());
		Calendar c3 = Calendar.getInstance();
		c3.set(Calendar.MILLISECOND, c1.get(Calendar.MILLISECOND));
		c3.set(Calendar.SECOND, c2.get(Calendar.SECOND));
		c3.set(Calendar.MINUTE, c2.get(Calendar.MINUTE));
		c3.set(Calendar.HOUR_OF_DAY, c2.get(Calendar.HOUR_OF_DAY));
		c3.set(Calendar.DATE, c2.get(Calendar.DATE));
		c3.set(Calendar.MONTH, c2.get(Calendar.MONTH));
		c3.set(Calendar.YEAR, c2.get(Calendar.YEAR));
		return c3.getTime();

	}

	public static Date getTimeZoneDate(Date date, String timeZone) {
		if (date == null)
			return null;
		Calendar c1 = Calendar.getInstance();
		c1.setTime(date);
		Calendar c2 = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		c2.setTimeInMillis(c1.getTimeInMillis());
		Calendar c3 = Calendar.getInstance();
		c3.set(Calendar.MILLISECOND, c1.get(Calendar.MILLISECOND));
		c3.set(Calendar.SECOND, c2.get(Calendar.SECOND));
		c3.set(Calendar.MINUTE, c2.get(Calendar.MINUTE));
		c3.set(Calendar.HOUR_OF_DAY, c2.get(Calendar.HOUR_OF_DAY));
		c3.set(Calendar.DATE, c2.get(Calendar.DATE));
		c3.set(Calendar.MONTH, c2.get(Calendar.MONTH));
		c3.set(Calendar.YEAR, c2.get(Calendar.YEAR));
		return c3.getTime();

	}

	public static Date cvtToGmt(Date date) {
		TimeZone tz = TimeZone.getDefault();
		Date ret = new Date(date.getTime() - tz.getRawOffset());

		// if we are now in DST, back off by the delta. Note that we are checking the
		// GMT date, this is the KEY.
		if (tz.inDaylightTime(ret)) {
			Date dstDate = new Date(ret.getTime() - tz.getDSTSavings());

			// check to make sure we have not crossed back into standard time
			// this happens when we are on the cusp of DST (7pm the day before the change
			// for PDT)
			if (tz.inDaylightTime(dstDate)) {
				ret = dstDate;
			}
		}
		return ret;
	}

	public static String getMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		String dia = new Integer(c.get(Calendar.MONTH) + 1).toString();
		return dia;
	}

	public static String getYear(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		String dia = new Integer(c.get(Calendar.YEAR)).toString();
		return dia;
	}

	public static Date forever() {
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		Date today = new Date();
		try {
			today = df.parse("01/01/3005");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return today;
	}

	public static XMLGregorianCalendar dateToXMLGregorian(Date date) {
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(date);
		XMLGregorianCalendar xmlGregorian = null;
		try {
			xmlGregorian = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
		} catch (DatatypeConfigurationException e) {
			messageException = "Error while trying to convert date: " + date + "to XMLGregorianCalendar";
			LOG.error(messageException, e);

		}
		return xmlGregorian;
	}

	public static XMLGregorianCalendar dateToXMLGregorianComplete(Date date) {
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(date);
		XMLGregorianCalendar xmlGregorian = null;
		try {
			xmlGregorian = DatatypeFactory.newInstance().newXMLGregorianCalendar();
			xmlGregorian.setYear(gregorianCalendar.get(Calendar.YEAR));
			xmlGregorian.setMonth(gregorianCalendar.get(Calendar.MONTH) + 1);
			xmlGregorian.setDay(gregorianCalendar.get(Calendar.DAY_OF_MONTH));
			xmlGregorian.setHour(gregorianCalendar.get(Calendar.HOUR_OF_DAY));
			xmlGregorian.setMinute(gregorianCalendar.get(Calendar.MINUTE));
			xmlGregorian.setSecond(gregorianCalendar.get(Calendar.SECOND));
		} catch (DatatypeConfigurationException e) {

		}

		return xmlGregorian;
	}

	public static XMLGregorianCalendar convertDatesToClientTimeZone(String effectivityDate, String effectivityTime,
			TimeZone timeZone) throws ParseException, DatatypeConfigurationException {
		Date effectivityTimestampDate = null;
		Calendar gregorianCalendar = GregorianCalendar.getInstance(timeZone);
		Date currentClientDate = gregorianCalendar.getTime();

		if (effectivityDate != null) {
			try {
				effectivityTimestampDate = strToDate("yyyy-MM-dd  HH:mm:ss", effectivityDate + "  " + effectivityTime,
						timeZone);

			} catch (ParseException e1) {
				throw e1;
			}
			gregorianCalendar.setTime(effectivityTimestampDate);
		} else {
			gregorianCalendar.setTime(currentClientDate);
		}

		XMLGregorianCalendar xmlGregorian = null;
		try {
			xmlGregorian = DatatypeFactory.newInstance().newXMLGregorianCalendar();

			xmlGregorian.setYear(gregorianCalendar.get(Calendar.YEAR));
			xmlGregorian.setMonth(gregorianCalendar.get(Calendar.MONTH) + 1);
			xmlGregorian.setDay(gregorianCalendar.get(Calendar.DAY_OF_MONTH));
			xmlGregorian.setHour(gregorianCalendar.get(Calendar.HOUR_OF_DAY));
			xmlGregorian.setMinute(gregorianCalendar.get(Calendar.MINUTE));
			xmlGregorian.setSecond(gregorianCalendar.get(Calendar.SECOND));

		} catch (DatatypeConfigurationException e) {
			throw e;
		}

		return xmlGregorian;
	}

	public static XMLGregorianCalendar convertDatesToClientTimeZone(String effectivityDate, TimeZone timeZone)
			throws ParseException, DatatypeConfigurationException {
		Date effectivityTimestampDate = null;
		Calendar gregorianCalendar = GregorianCalendar.getInstance(timeZone);
		Date currentClientDate = gregorianCalendar.getTime();

		if (effectivityDate != null) {
			try {
				effectivityTimestampDate = strToDate("yyyy-MM-dd  HH:mm:ss", effectivityDate, timeZone);

			} catch (ParseException e1) {
				throw e1;
			}
			gregorianCalendar.setTime(effectivityTimestampDate);
		} else {
			gregorianCalendar.setTime(currentClientDate);
		}

		XMLGregorianCalendar xmlGregorian = null;
		try {
			xmlGregorian = DatatypeFactory.newInstance().newXMLGregorianCalendar();

			xmlGregorian.setYear(gregorianCalendar.get(Calendar.YEAR));
			xmlGregorian.setMonth(gregorianCalendar.get(Calendar.MONTH) + 1);
			xmlGregorian.setDay(gregorianCalendar.get(Calendar.DAY_OF_MONTH));
			xmlGregorian.setHour(gregorianCalendar.get(Calendar.HOUR_OF_DAY));
			xmlGregorian.setMinute(gregorianCalendar.get(Calendar.MINUTE));
			xmlGregorian.setSecond(gregorianCalendar.get(Calendar.SECOND));

		} catch (DatatypeConfigurationException e) {
			throw e;
		}

		return xmlGregorian;
	}

	public static XMLGregorianCalendar getCurrentTimeAtClientTimeZone(TimeZone timeZone) {
		GregorianCalendar gregorianCalendar = new GregorianCalendar(timeZone);

		XMLGregorianCalendar xmlGregorian = null;
		try {
			xmlGregorian = DatatypeFactory.newInstance().newXMLGregorianCalendar();
			xmlGregorian.setYear(gregorianCalendar.get(Calendar.YEAR));
			xmlGregorian.setMonth(gregorianCalendar.get(Calendar.MONTH) + 1);
			xmlGregorian.setDay(gregorianCalendar.get(Calendar.DAY_OF_MONTH));
			xmlGregorian.setHour(gregorianCalendar.get(Calendar.HOUR_OF_DAY));
			xmlGregorian.setMinute(gregorianCalendar.get(Calendar.MINUTE));
			xmlGregorian.setSecond(gregorianCalendar.get(Calendar.SECOND));
		} catch (DatatypeConfigurationException e) {

		}

		return xmlGregorian;
	}

	public static Date tomorrow(Date dt) {
		Calendar c = Calendar.getInstance();
		c.setTime(dt);
		c.add(Calendar.DATE, 1);
		dt = c.getTime();
		return dt;
	}

	public static long getJmsMessageDuration(ObjectMessage objectMessage) {
		// Get message timestamp to besure is not processing messages older that 2
		// minutes,
		// JMSTimestamp will be set automatically by Active MQ..This revalidation is
		// added because sometimes Active MQ does not expire the message automatically
		// after 2 minutes
		Date messageTimeStamp;
		try {
			messageTimeStamp = new Date(objectMessage.getJMSTimestamp());
		} catch (JMSException e) {
			messageTimeStamp = new Date();
		}
		long diff = new Date().getTime() - messageTimeStamp.getTime();

		return TimeUnit.MILLISECONDS.toMinutes(diff);
	}

	public static boolean checkDateWithinRange(Date runUntil, long noOfDays) {

		boolean isValid = false;

		Date currentDate = new Date();

		double timeDiffInDays = (double) (runUntil.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24);

		if (timeDiffInDays <= noOfDays) {

			isValid = true;

		}

		return isValid;
	}

	public static boolean compareDateWithCurrentDate(Date inputDate) {

		boolean inputDateBeforeCurrentDate = false;

		if (inputDate != null) {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			Date today = new Date();

			Long currentDateLong = Long.valueOf(sdf.format(today));
			Long inputDateLong = Long.valueOf(sdf.format(inputDate));

			if (inputDateLong < currentDateLong) {
				inputDateBeforeCurrentDate = true;
			}

		} else {
			/*
			 * Consider NULL input as a date before current date
			 */
			inputDateBeforeCurrentDate = true;
		}

		return inputDateBeforeCurrentDate;

	}

	public static XMLGregorianCalendar getXMLGregorianCalendarNow() throws DatatypeConfigurationException {

		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
		XMLGregorianCalendar now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
		return now;
	}

}