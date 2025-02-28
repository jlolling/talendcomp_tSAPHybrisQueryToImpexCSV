/**
 * Copyright 2021 Jan Lolling jan.lolling@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.jlo.talendcomp.hac.query2impex;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Utility class to parse a String into a Date 
 * by testing a number of common pattern
 * This class is thread save.
 * 
 * @author jan.lolling@cimt-ag.de
 */
public class GenericDateUtil {
	
	private static ThreadLocal<DateParser> threadLocal = new ThreadLocal<DateParser>();
	public static final long ZERO_TIME = -62170160400000l;
	
    /**
     * parseDuration: returns the Date from the given text representation containing the time part as duration
     * Uses an internal list of patterns to parse the source.
     * 
     * @param source the formatted time as String
     * @return Date object representing the duration
     * @throws ParseException if the available patterns are not sufficient to parse the source
     */
	public static Date parseDuration(String source) throws ParseException {
		return parseDuration(source, (String[]) null);
	}

    /**
     * parseDuration: returns the Date from the given source as duration
     * Tolerates if the content does not fit to the given pattern and retries it
     * with build in patterns
     * @param source the formatted time as String
     * @param suggestedPattern an array of suggested patterns
     * @return Date object representing the duration
     * @throws ParseException if the available patterns are not sufficient to parse the source
     */
	public static Date parseDuration(String source, String ...suggestedPattern) throws ParseException {
		return getDateParser().parseDuration(source, suggestedPattern);
	}

    /**
     * parseDuration: returns the Date from the given source as duration
     * Tolerates if the content does not fit to the given pattern and retries it
     * with build in patterns
     * @param source date or time as Double in which a 1 is one day
     * @return Long object representing the duration
     */
	public static Long parseDuration(Double source) {
		return getDateParser().getDuration(source);
	}

    /**
     * parseDate: returns the Date from the given text representation
     * Uses an internal list of patterns to parse the source.
     * 
     * @param source the formatted date as String
     * @return Date object representing the Date
     * @throws ParseException if the available patterns are not sufficient to parse the source
     */
	public static Date parseDate(String source) throws ParseException {
		return parseDate(source, (String[]) null);
	}

	/**
     * parseDate: returns the Date from the given text representation
     * Tolerates if the content does not fit to the given pattern and retries it
     * with build in patterns
     * 
     * @param source the formatted time as String
     * @param suggestedPattern an array of suggested patterns
     * @return Date object representing the Date
     * @throws ParseException if the available patterns are not sufficient to parse the source
     */
	public static Date parseDate(String source, String ...suggestedPattern) throws ParseException {
		return getDateParser().parseDate(source, null, suggestedPattern);
	}
	
	/**
     * parseDate: returns the Date from the given text representation
     * Tolerates if the content does not fit to the given pattern and retries it
     * with build in patterns
     * 
     * @param source the formatted time as String
     * @param suggestedPattern an array of suggested patterns
     * @return Date object representing the Date
     * @throws ParseException if the available patterns are not sufficient to parse the source
     */
	public static Date parseDate(String source, Locale locale, String ...suggestedPattern) throws ParseException {
		return getDateParser().parseDate(source, locale, suggestedPattern);
	}

	public static DateParser getDateParser() {
		DateParser p = threadLocal.get();
		if (p == null) {
			p = new DateParser();
			threadLocal.set(p);
		}
		return p;
	}
	
	public static class DateParser {
		
		private List<String> datePatternList = null;
		private List<String> timePatternList = null;
		
		private static final int SECONDS_PER_MINUTE = 60;
		private static final int MINUTES_PER_HOUR = 60;
		private static final int HOURS_PER_DAY = 24;
		private static final int SECONDS_PER_DAY = (HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE);
		private static final long DAY_MILLISECONDS = SECONDS_PER_DAY * 1000L;
		private boolean lenient = true;
		
		DateParser() {
			datePatternList = new ArrayList<String>();
			datePatternList.add("yyyy-MM-dd");
			datePatternList.add("dd.MM.yyyy");
			datePatternList.add("d.MM.yyyy");
			datePatternList.add("d.M.yy");
			datePatternList.add("dd.MM.yy");
			datePatternList.add("dd.MMM.yyyy");
			datePatternList.add("MM/dd/yyyy");
			datePatternList.add("MM/dd/yy");
			datePatternList.add("M/d/yy");
			datePatternList.add("dd/MM/yyyy");
			datePatternList.add("dd/MM/yy");
			datePatternList.add("dd/MMM/yyyy");
			datePatternList.add("dd. MMMM yyyy");
			datePatternList.add("dd. MMM. yyyy");
			datePatternList.add("dd. MMM yyyy");
			datePatternList.add("MMMM dd'th' yyyy");
			datePatternList.add("MMM dd'th' yyyy");
			datePatternList.add("MMM dd, yyyy");
			datePatternList.add("dd'th' MMMM yyyy");
			datePatternList.add("dd'th' MMM yyyy");
			datePatternList.add("'KW' w/yyyy");
			datePatternList.add("'w/c' w.yyyy");
			datePatternList.add("'CW' w.yyyy");
			datePatternList.add("MMMM yyyy");
			datePatternList.add("dd-MM-yyyy");
			datePatternList.add("dd-MM-yy");
			datePatternList.add("dd-MMM-yyyy");
			datePatternList.add("d-M-yy");
			datePatternList.add("yyyyMMdd");
			datePatternList.add("yyyyMM");
			datePatternList.add("yyyy");
			timePatternList = new ArrayList<String>();
			timePatternList.add(" mm''ss'\"'");
			timePatternList.add(" mm''ss'“'");
			timePatternList.add(" mm''ss'”'");
			timePatternList.add(" mm'‘'ss'“'");
			timePatternList.add(" mm'’'ss'”'");
			timePatternList.add(" mm'′'ss'″'");
			timePatternList.add(" HH'h'mm'm'ss's'");
			timePatternList.add(" HH'h'mm'm'");
			timePatternList.add(" mm'm'ss's'");
			timePatternList.add("'T'HH:mm:ss.SSSZ");
			timePatternList.add("'T'HH:mm:ss.SSS");
			timePatternList.add(" hh:mm:ss aaa");
			timePatternList.add(" HH:mm:ss.SSS");
			timePatternList.add(" HH:mm:ss");
			timePatternList.add(" mm:ss");
			timePatternList.add(" HHmmss");
			timePatternList.add(" mmss");
		}
		
		private Date parseDate(String text, Locale locale, String ... userPattern) throws ParseException {
			if (text != null && text.trim().isEmpty() == false) {
				Date dateValue = null;
				if (userPattern != null) {
					for (int i = userPattern.length - 1; i >= 0; i--) {
						if (datePatternList.contains(userPattern[i])) {
							datePatternList.remove(userPattern[i]);
						}
						datePatternList.add(0, userPattern[i]);
					}
				}
				if (locale == null) {
					locale = Locale.ENGLISH;
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", locale);
				sdf.setLenient(lenient);
				for (String pattern : datePatternList) {
					if (pattern != null) {
						sdf.applyPattern(pattern.trim());
						try {
							dateValue = sdf.parse(text);
							// if we continue here the pattern fits
							// now we know the date is correct, lets try the time part:
							if (text.length() - pattern.length() >= 6) {
								// there is more in the text than only the date
								for (String timepattern : timePatternList) {
									String dateTimePattern = pattern + timepattern;
									sdf.applyPattern(dateTimePattern);
									try {
										dateValue = sdf.parse(text);
										// we got it
										pattern = dateTimePattern;
										break;
									} catch (ParseException e1) {
										// ignore parsing errors, we are trying
									}
								}
							}
							return dateValue;
						} catch (ParseException e) {
							// the pattern obviously does not work
							continue;
						}
					}
				}
				throw new ParseException("The value: " + text + " could not be parsed to a Date.", 0);
			} else {
				return null;
			}
		}

		private Date parseDuration(String text, String ... userPattern) throws ParseException {
			if (text != null && text.trim().isEmpty() == false) {
				Date timeValue = null;
				if (userPattern != null) {
					for (int i = userPattern.length - 1; i >= 0; i--) {
						if (timePatternList.contains(userPattern[i])) {
							timePatternList.remove(userPattern[i]);
						}
						timePatternList.add(0, userPattern[i]);
					}
				}
				SimpleDateFormat sdf = new SimpleDateFormat();
				sdf.setTimeZone(getUTCTimeZone());
				for (String pattern : timePatternList) {
					if (pattern != null) {
						sdf.applyPattern(pattern.trim());
						try {
							timeValue = sdf.parse(text);
							// if we continue here the pattern fits
							return timeValue;
						} catch (ParseException e) {
							// the pattern obviously does not work
							continue;
						}
					}
				}
				throw new ParseException("The value: " + text + " could not be parsed to a Date as duration.", 0);
			} else {
				return null;
			}
		}
		
		private Long getDuration(Double timeInExcel) {
			if (timeInExcel != null) {
		        int wholeDays = (int) Math.floor(timeInExcel);
		        int millisecondsInDay = (int) ((timeInExcel - wholeDays) * DAY_MILLISECONDS + 0.5);
		        Calendar cal = Calendar.getInstance(getUTCTimeZone());
		        cal.setTimeInMillis(0);
//		        cal.set(Calendar.DAY_OF_YEAR, wholeDays + 1);
		        cal.set(Calendar.MILLISECOND, millisecondsInDay);
		        return cal.getTimeInMillis();
			} else {
				return null;
			}
		}

		public boolean isLenient() {
			return lenient;
		}

		public void setLenient(boolean lenient) {
			this.lenient = lenient;
		}

	}
	
    private static java.util.TimeZone utcTimeZone = null;

    private static java.util.TimeZone getUTCTimeZone() {
    	if (utcTimeZone == null) {
    		utcTimeZone = java.util.TimeZone.getTimeZone("UTC");
    	}
    	return utcTimeZone;
    }
    
    /**
     * Checks if the date is pointing to first date of our era.
     * @param date the date to check
     * @return true if it is a zero date
     */
    public static boolean isZeroDate(Date date) {
    	if (date != null) {
    		Calendar cal = Calendar.getInstance(getUTCTimeZone());
    		cal.setTime(date);
    		cal.setLenient(false);
        	cal.set(java.util.Calendar.MINUTE, 0);
        	cal.set(java.util.Calendar.SECOND, 0);
        	cal.set(java.util.Calendar.MILLISECOND, 0);
        	cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
    		int year = cal.get(Calendar.YEAR);
    		int month = cal.get(Calendar.MONTH);
    		int day = cal.get(Calendar.DAY_OF_MONTH);
    		int era = cal.get(Calendar.ERA);
    		if (year == 2 && month == 10 && era == 0) {
    			if (day == 29 || day == 30) {
        			return true;
    			}
    		}
    		return false;
    	} else {
    		return false;
    	}
    }

}