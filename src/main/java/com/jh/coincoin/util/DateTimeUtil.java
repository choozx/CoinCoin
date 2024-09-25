package com.jh.coincoin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by dale on 2024-09-12.
 */
public final class DateTimeUtil {

    private static final Logger log = LoggerFactory.getLogger(DateTimeUtil.class);

    //public static final ZoneId SYS_ZID = ZoneId.of("Asia/Seoul"); // ZoneId.systemDefault();
    public static final ZoneId SYS_ZONE_ID = ZoneId.of("UTC");
    public static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");
    public static final ZoneId KST_ZONE_ID = ZoneId.of("Asia/Seoul");
    public static final ZoneOffset SYS_ZONE_OFFSET = toZoneOffset();
    public static final ZoneOffset UTC_ZONE_OFFSET = ZoneOffset.UTC;
    public static final ZoneOffset KST_ZONE_OFFSET = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).getOffset();
    public static final DateTimeFormatter DATE_ID_FMT = DateTimeFormatter.ofPattern("yyMMdd").withLocale(Locale.ENGLISH).withZone(SYS_ZONE_ID);
    public static final DateTimeFormatter DATETIME_ID_FMT = DateTimeFormatter.ofPattern("yyMMddHH").withLocale(Locale.ENGLISH).withZone(SYS_ZONE_ID);
    public static final DateTimeFormatter YYYY_MM_FMT = DateTimeFormatter.ofPattern("yyyy-MM").withLocale(Locale.ENGLISH).withZone(SYS_ZONE_ID);
    public static final DateTimeFormatter YYYY_MM_DD_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd").withLocale(Locale.ENGLISH).withZone(SYS_ZONE_ID);
    public static final DateTimeFormatter YYYY_MM_UNDERSCORE_FMT = DateTimeFormatter.ofPattern("yyyy_MM").withLocale(Locale.ENGLISH).withZone(SYS_ZONE_ID);
    public static final DateTimeFormatter YYMMDDHHMM_FMT = DateTimeFormatter.ofPattern("yyMMddHHmm").withLocale(Locale.ENGLISH).withZone(SYS_ZONE_ID);
    public static final DateTimeFormatter YYMMDDHHMMSS_FMT = DateTimeFormatter.ofPattern("yyMMddHHmmss").withLocale(Locale.ENGLISH).withZone(SYS_ZONE_ID);
    public static final DateTimeFormatter YYYYMMDD_FMT = DateTimeFormatter.ofPattern("yyyyMMdd").withLocale(Locale.ENGLISH).withZone(SYS_ZONE_ID);
    public static final DateTimeFormatter YYYYMMDDHH_FMT = DateTimeFormatter.ofPattern("yyyyMMddHH").withLocale(Locale.ENGLISH).withZone(SYS_ZONE_ID);
    public static final DateTimeFormatter YYMM_FMT = DateTimeFormatter.ofPattern("yyMM").withLocale(Locale.ENGLISH).withZone(SYS_ZONE_ID);
    public static final DateTimeFormatter YYMMDD_FMT = DateTimeFormatter.ofPattern("yyMMdd").withLocale(Locale.ENGLISH).withZone(SYS_ZONE_ID);
    public static final DateTimeFormatter HH_FMT = DateTimeFormatter.ofPattern("HH").withLocale(Locale.ENGLISH).withZone(SYS_ZONE_ID);
    public static final DateTimeFormatter YYYYMMDDHHMMSS_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withLocale(Locale.ENGLISH).withZone(SYS_ZONE_ID);
    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withLocale(Locale.ENGLISH).withZone(SYS_ZONE_ID);
    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS_S_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S").withLocale(Locale.ENGLISH).withZone(SYS_ZONE_ID);
    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS_SSS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withLocale(Locale.ENGLISH).withZone(SYS_ZONE_ID);
    public static final DateTimeFormatter YYYYMMDDHHMMSSSSS_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").withLocale(Locale.ENGLISH).withZone(SYS_ZONE_ID);
    public static final DateTimeFormatter HH_MM_SS_SSS_FMT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withLocale(Locale.ENGLISH).withZone(SYS_ZONE_ID);

    // 각종 밀리세컨트 미리 계산
    public static final long ONE_MINUTE_IN_MILLIS = 60L * 1000L;
    public static final long ONE_HOUR_IN_MILLIS = 60L * ONE_MINUTE_IN_MILLIS;
    public static final long ONE_DAY_IN_MILLIS = 24L * ONE_HOUR_IN_MILLIS;
    public static long ONE_WEEK_IN_MILLIS = 7L * ONE_DAY_IN_MILLIS;

    public static final int MONDAY_IDX = 1;
    public static final int TUESDAY_IDX = 2;
    public static final int WEDNESDAY_IDX = 3;
    public static final int THURSDAY_IDX = 4;
    public static final int FRIDAY_IDX = 5;
    public static final int SATURDAY_IDX = 6;
    public static final int SUNDAY_IDX = 7;
    public static final String MONDAY = "MON";
    public static final String TUESDAY = "TUE";
    public static final String WEDNESDAY = "WED";
    public static final String THURSDAY = "THU";
    public static final String FRIDAY = "FRI";
    public static final String SATURDAY = "SAT";
    public static final String SUNDAY = "SUN";

    private static final long SYSTEM_MAX_DATE;
    private static final long DB_MAX_TIMESTAMP;
    private static final long EPOCH_2023;

    static {
        try {
            SYSTEM_MAX_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse("9999-12-31 23:59:59.999").getTime();

            // mariadb는 2038-01-19 05:14:07가 timestamp의 최대값임
            DB_MAX_TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse("2037-12-31 23:59:59.999").getTime();

            // 2023년 1월 1일로 하는 epoch
            EPOCH_2023 = new SimpleDateFormat("yyyy-MM-dd").parse("2023-01-01").getTime();  // 2023년 1월 1일을 epoch2023 기준으로 잡는다
            // log.debug("EPOCH_2022={}, {}", EPOCH_2022, new Date(EPOCH_2022));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static long getSystemMaxDate() {
        return SYSTEM_MAX_DATE;
    }

    public static long getDbMaxTimestamp() {
        return DB_MAX_TIMESTAMP;
    }

    public static long getEpoch() {
        return Instant.now().toEpochMilli();
    }
    public static long getEpoch2023() {
        return EPOCH_2023;
    }

    public static int getCacheMinute() {
        return LocalDateTime.now(SYS_ZONE_ID).getMinute();
    }

    public static LocalDate toLocalDate(long epochMilli) {
        return LocalDate.ofInstant(Instant.ofEpochMilli(epochMilli), SYS_ZONE_ID);
    }

    public static LocalDateTime toDateTime(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), SYS_ZONE_ID);
    }

    public static LocalDateTime toDateTime(int epochSeconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochSeconds * 1000L), SYS_ZONE_ID);
    }

    public static LocalDateTime toDateTime(long epochMilli, ZoneId zoneId) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), zoneId);
    }

    public static LocalDateTime toDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), SYS_ZONE_ID);
    }

    public static LocalDateTime toDateTime(Date date, ZoneId zoneId) {
        return LocalDateTime.ofInstant(date.toInstant(), zoneId);
    }

    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.toInstant(SYS_ZONE_OFFSET));
    }

    public static Date toDate(LocalDateTime localDateTime, ZoneId zoneId) {
        return Date.from(localDateTime.toInstant(toZoneOffset(zoneId)));
    }

    public static int toHour(long epochMilli) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), SYS_ZONE_ID);
        return Integer.parseInt(HH_FMT.format(dateTime));
    }

    public static int toHour(long epochMilli, ZoneId zoneId) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), zoneId);
        return Integer.parseInt(HH_FMT.format(dateTime));
    }

    public static long getBetweenDateToLong(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Duration duration  = Duration.between(startDateTime, endDateTime);
        return duration.getSeconds();
    }


    public static long hourToMillisecond(int hours) {
        return hours*60*60*1000;
    }

    public static long toEpochSecond(LocalDateTime localDateTime) {
        return localDateTime.toEpochSecond(SYS_ZONE_OFFSET);
    }

    public static long toEpochSecond(LocalDateTime localDateTime, ZoneId zoneId) {
        return localDateTime.toEpochSecond(toZoneOffset(zoneId));
    }

    public static long toEpochMilli(LocalDateTime localDateTime) {
        return localDateTime.toInstant(SYS_ZONE_OFFSET).toEpochMilli();
    }

    public static long toEpochMilli(LocalDateTime localDateTime, ZoneId zoneId) {
        return localDateTime.toInstant(toZoneOffset(zoneId)).toEpochMilli();
    }

    public static long toEpochMilli(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        return toEpochMilli(LocalDateTime.of(year, month, dayOfMonth, hour, minute, second), SYS_ZONE_ID);
    }

    public static long toEpochMilli(int year, int month, int dayOfMonth, int hour, int minute, int second, ZoneId zoneId) {
        return toEpochMilli(LocalDateTime.of(year, month, dayOfMonth, hour, minute, second), zoneId);
    }

    public static long toEpochMilli(int year, int month, int dayOfMonth, int hour, int minute, int second, int nanoOfSecond) {
        return toEpochMilli(LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond), SYS_ZONE_ID);
    }

    public static long toEpochMilli(int year, int month, int dayOfMonth, int hour, int minute, int second, int nanoOfSecond, ZoneId zoneId) {
        return toEpochMilli(LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond), zoneId);
    }

    public static ZoneOffset toZoneOffset(ZoneId zoneId) {
        return zoneId.getRules().getOffset(Instant.now());
    }

    public static ZoneOffset toZoneOffset() {
        // return ZoneId.systemDefault().getRules().getOffset(Instant.now());
        return SYS_ZONE_ID.getRules().getOffset(Instant.now());
    }

    /**
     * AppThreadLocal.getTimestamp() 리턴값을 Timestamp 로 변환
     * @param currentTimeMillis AppThreadLocal.getTimestamp()
     * @return Timestamp
     */
    public static Timestamp convertCurrentTimeMillisToTimeStamp(long currentTimeMillis) {
        LocalDateTime nowDateTime = DateTimeUtil.toDateTime(currentTimeMillis);
        return Timestamp.valueOf(nowDateTime);
    }

    public static LocalDateTime plus(Date date, int amountToAdd, TemporalUnit unit) {
        return LocalDateTime.ofInstant(date.toInstant(), SYS_ZONE_ID).plus(amountToAdd, unit);
    }

    public static LocalDateTime plus(Date date, ZoneId zoneId, int amountToAdd, TemporalUnit unit) {
        return LocalDateTime.ofInstant(date.toInstant(), zoneId).plus(amountToAdd, unit);
    }

    public static LocalDateTime plus(long epochMilli, int amountToAdd, TemporalUnit unit) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), SYS_ZONE_ID).plus(amountToAdd, unit);
    }

    public static LocalDateTime plus(long epochMilli, ZoneId zoneId, int amountToAdd, TemporalUnit unit) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), zoneId).plus(amountToAdd, unit);
    }

    /**
     * datetime2가 datetime1보다 이후 인가?
     */
    public static boolean isAfter(LocalDateTime datetime1, LocalDateTime datetime2) {
        return datetime2.isAfter(datetime1);
    }

    /**
     * date2가 date1보다 이후 인가?
     */
    public static boolean isAfter(Date date1, Date date2) {
        LocalDateTime datetime1 = toDateTime(date1);
        LocalDateTime datetime2 = toDateTime(date2);
        return datetime2.isAfter(datetime1);
    }

    /**
     * date2가 date1보다 이후 인가?
     */
    public static boolean isAfter(Date date1, ZoneId zoneId1, Date date2, ZoneId zoneId2) {
        LocalDateTime datetime1 = toDateTime(date1, zoneId1);
        LocalDateTime datetime2 = toDateTime(date2, zoneId2);
        return datetime2.isAfter(datetime1);
    }

    /**
     * now가 startDate, endDate 기간에 포함하는지 여부
     */
    public static boolean isPeriod(Date startDate, Date endDate, long now) {
        return isPeriod(toDateTime(startDate), toDateTime(endDate), toDateTime(now));
    }

    /**
     * now가 startDate, endDate 기간에 포함하는지 여부
     */
    public static boolean isPeriod(Date startDate, Date endDate, long now, ZoneId zoneId) {
        return isPeriod(toDateTime(startDate, zoneId), toDateTime(endDate, zoneId), toDateTime(now, zoneId));
    }

    /**
     * now가 startDate, endDate 기간에 포함하는지 여부
     */
    public static boolean isPeriod(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime nowTime) {
        if (nowTime.isAfter(startTime) && nowTime.isBefore(endTime)) {
            return true;
        }
        return false;
    }

    /**
     * 상점 구매 날짜가 이번달에 포함되는 지 확인
     * @param buyTime
     * @return
     */
//    public static boolean isThisMonth(Timestamp nowTimeStamp, Timestamp buyTime) {
//        LocalDateTime nowDate = DateTimeUtil.toDateTime(nowTimeStamp);
//        LocalDateTime firstDateOfMonth = nowDate.withDayOfMonth(1);
//        LocalDateTime lastDateOfMonth = nowDate.withDayOfMonth(nowDate.toLocalDate().lengthOfMonth());
//
//        LocalDateTime pickDate = DateTimeUtil.toDateTime(buyTime);
//
//        return isPeriod(firstDateOfMonth, lastDateOfMonth, pickDate);
//    }

    public static boolean isThisMonth(Timestamp nowTimeStamp, Timestamp buyTime) {
        LocalDateTime nowDateTime = toDateTime(nowTimeStamp);
        int nowYyMm = createYymm(nowDateTime);

        LocalDateTime buyDateTime = toDateTime(buyTime);
        int buyYyMm = createYymm(buyDateTime);
        log.debug("[TimeUtil] nowYyMm:{}, buyYyMm:{}, isSameMonth:{}", nowYyMm, buyYyMm, nowYyMm == buyYyMm);

        return nowYyMm == buyYyMm;
    }

    public static boolean isThisWeek(Timestamp nowTimeStamp, Timestamp buyTime) {
        LocalDateTime nowDate = DateTimeUtil.toDateTime(nowTimeStamp);
        LocalDateTime buyDate = DateTimeUtil.toDateTime(buyTime);
        int nowWeekOfYear = nowDate.get(WeekFields.ISO.weekOfYear());
        int buyWeekOfYear = buyDate.get(WeekFields.ISO.weekOfYear());

        return (nowWeekOfYear == buyWeekOfYear);
    }


    public static boolean isThisDay(Timestamp nowTimeStamp, Timestamp buyTime) {
        LocalDate nowDate = DateTimeUtil.toDateTime(nowTimeStamp).toLocalDate();
        LocalDate pickDate = DateTimeUtil.toDateTime(buyTime).toLocalDate();

        return nowDate.isEqual(pickDate);
    }

    public static boolean isToday(LocalDateTime targetTime) {
        LocalDate now  = DateTimeUtil.toDateTime(DateTimeUtil.getCurrentTimeMillis()).toLocalDate();
        LocalDate targetDate = targetTime.toLocalDate();

        return now.isEqual(targetDate);
    }

    public static int toDayOfWeek(String day) {
        if (MONDAY.equals(day)) {
            return MONDAY_IDX;
        } else if (TUESDAY.equals(day)) {
            return TUESDAY_IDX;
        } else if (WEDNESDAY.equals(day)) {
            return WEDNESDAY_IDX;
        } else if (THURSDAY.equals(day)) {
            return THURSDAY_IDX;
        } else if (FRIDAY.equals(day)) {
            return FRIDAY_IDX;
        } else if (SATURDAY.equals(day)) {
            return SATURDAY_IDX;
        } else if (SUNDAY.equals(day)) {
            return SUNDAY_IDX;
        }
        throw new RuntimeException();
    }

    public static int getDayOfWeek(long epochMilli) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), SYS_ZONE_ID);
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        return dayOfWeek.getValue();
    }

    public static int getDayOfWeek(long epochMilli, ZoneId zoneId) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), zoneId);
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        return dayOfWeek.getValue();
    }

    //yyMMddHH 포맷 int 형 시간형 아이디
    public static int getHourId(long now){
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(now), SYS_ZONE_ID);
        return Integer.parseInt(ZonedDateTime.of(dateTime, SYS_ZONE_ID).format(DATETIME_ID_FMT));
    }

    // yyMMdd 포맷 int 형 일자 아이디
    public static int getDateId(long now) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(now), SYS_ZONE_ID);
        return Integer.parseInt(ZonedDateTime.of(dateTime, SYS_ZONE_ID).format(DATE_ID_FMT));
    }

    public static int getDateId(LocalDateTime localDateTime) {
        return Integer.parseInt(ZonedDateTime.of(localDateTime, SYS_ZONE_ID).format(DATE_ID_FMT));
    }

    // 요청된 DateTimeFormatter 포맷 형식으로 다음달 첫 "일" 기준 일자 아이디
    public static long getDateIdFromStartDayOfNextMonth(long now, DateTimeFormatter formatter) {
        LocalDate nowDate = LocalDate.ofInstant(Instant.ofEpochMilli(now), SYS_ZONE_ID);
        LocalDateTime localDateTime = nowDate.withDayOfMonth(1).plusMonths(1).atStartOfDay();
        return Integer.parseInt(ZonedDateTime.of(localDateTime, SYS_ZONE_ID).format(formatter));
    }

    public static long getDateIdFromStartDayOfNextWeek(long now, DateTimeFormatter formatter) {
        LocalDate nowDate = LocalDate.ofInstant(Instant.ofEpochMilli(now), SYS_ZONE_ID);
        LocalDateTime localDateTime = nowDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atStartOfDay();
        return Integer.parseInt(ZonedDateTime.of(localDateTime, SYS_ZONE_ID).format(formatter));
    }

    /**
     * 요청된 DateTimeFormatter 포맷 형식으로 long 형 일자 아이디를 리턴.
     * ex) getDateId(localDateTime, DATE_ID_FMT);
     */
    public static long getDateId(LocalDateTime localDateTime, DateTimeFormatter formatter) {
        return Long.parseLong(ZonedDateTime.of(localDateTime, SYS_ZONE_ID).format(formatter));
    }
    public static long getDateId(Long now, DateTimeFormatter formatter) {
        return Long.parseLong(ZonedDateTime.of(toDateTime(now), SYS_ZONE_ID).format(formatter));
    }

    /**
     * 현 시점 기준 월요일 날짜를 요청된 DateTimeFormatter 포맷 형식의 정수 타입으로 리턴
     */
    public static int getWeekId(long now, DateTimeFormatter formatter) {
        long epochDay = TimeUnit.MILLISECONDS.toDays(now);
        LocalDate today = LocalDate.ofEpochDay(epochDay);
        LocalDate lastMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return Integer.parseInt(lastMonday.format(formatter));
    }

    /**
     * 현 시점 기준 월요일 날짜를 yyMMdd 정수 타입으로 리턴
     */
    public static int getWeekId(long time) {
        long epochDay = TimeUnit.MILLISECONDS.toDays(time);
        LocalDate today = LocalDate.ofEpochDay(epochDay);
        LocalDate lastMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        int weekId =
                (lastMonday.getYear() - 2000) * 10000
                        + lastMonday.getMonthValue() * 100
                        + lastMonday.getDayOfMonth();
        return weekId;
    }


    /**
     * yyyyMMdd 포맷의 일자 아이디를 key로 하고 value값은 일자 아이디의 월요일 포함 하는 Map
     */
    public static Map<Integer, Integer> getDayOfWeekMap(ZoneId zoneId, int size) {
        LocalDateTime dateTime = LocalDateTime.now(zoneId).minusWeeks(2).withNano(0).withSecond(0).withMinute(0)
                .withHour(0);
        dateTime = dateTime.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));

        Map<Integer, Integer> dayOfWeekMap = new HashMap<>();
        for (int i = 0; i < size; ++i) {
            dateTime = dateTime.plusDays(7);
            int mondayDateId = Integer.parseInt(YYYYMMDD_FMT.format(dateTime));
            dayOfWeekMap.put(mondayDateId, mondayDateId);
            for (int j = 1; j < 7; ++j) {
                dateTime = dateTime.plusDays(1);
                int dateId = Integer.parseInt(YYYYMMDD_FMT.format(dateTime));
                dayOfWeekMap.put(dateId, mondayDateId);
            }
            dateTime = dateTime.minusDays(6);
        }
        return dayOfWeekMap;
    }

    /**
     * yyyyMMdd 포맷의 월요일 일자 아이디와 일요일 일자 아이디를 포함 하는 List
     */
    public static List<String> getDayOfWeekList(ZoneId zoneId, int size) {
        LocalDateTime dateTime = LocalDateTime.now(zoneId).minusWeeks(2).withNano(0).withSecond(0).withMinute(0).withHour(0);
        dateTime = dateTime.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));

        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; ++i) {
            sb.setLength(0);
            sb.append(DateTimeUtil.YYYYMMDD_FMT.format(dateTime)).append("_");
            dateTime = dateTime.plusDays(6);
            sb.append(DateTimeUtil.YYYYMMDD_FMT.format(dateTime));
            result.add(sb.toString());
            dateTime = dateTime.plusDays(1);
        }
        return result;
    }

    /**
     * yyyy_MM 포맷의 월별 아이디 List
     */
    public static List<String> getMonthList(ZoneId zoneId, int size) {
        LocalDateTime dateTime = LocalDateTime.now(zoneId).withNano(0).withSecond(0).withMinute(0).withHour(0).withDayOfMonth(1);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            result.add(YYYY_MM_UNDERSCORE_FMT.format(dateTime));
            dateTime = dateTime.plusMonths(1);
        }
        return result;
    }

    /**
     * 현재 시간 기준 월요일 일자 아이디와 일요일 일자 아이디 리턴
     */
    public static String getDayOfWeek2(ZoneId zoneId) {
        LocalDateTime dateTime = LocalDateTime.now(zoneId).withNano(0).withSecond(0).withMinute(0).withHour(0);
        return getDayOfWeek2(dateTime);
    }

    public static String getDayOfWeek2(long epochMilli, ZoneId zoneId) {
        LocalDateTime dateTime = toDateTime(epochMilli, zoneId).withNano(0).withSecond(0).withMinute(0).withHour(0);
        return getDayOfWeek2(dateTime); // 20170710_20170716
    }

    private static String getDayOfWeek2(LocalDateTime dateTime) {
        for (int i = 0; i < 7; ++i) {
            DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
            if (dayOfWeek == DayOfWeek.MONDAY) {
                break;
            }
            dateTime = dateTime.minusDays(1);
        }
        StringBuilder sb = new StringBuilder(20);
        sb.append(DateTimeUtil.YYYYMMDD_FMT.format(dateTime)).append("_");
        dateTime = dateTime.plusDays(6);
        sb.append(DateTimeUtil.YYYYMMDD_FMT.format(dateTime));
        return sb.toString(); // 20170710_20170716
    }

    public static String getMonth2(long epochMilli, ZoneId zoneId) {
        LocalDateTime dateTime = toDateTime(epochMilli, zoneId).withNano(0).withSecond(0).withMinute(0).withHour(0).withDayOfMonth(1);
        return YYYY_MM_UNDERSCORE_FMT.format(dateTime); // 2017_10
    }

    /**
     * now 기준으로 가장 가까운 과거 화요일 LocalDateTime 리턴
     */
    public static LocalDateTime getLastTuesdayZeroHour(long now) {
        LocalDateTime dateTime = toDateTime(now).withNano(0).withSecond(0).withMinute(0).withHour(0);
        for (int i = 0; i < 7; ++i) {
            if (dateTime.getDayOfWeek() == DayOfWeek.TUESDAY) {
                break;
            }
            dateTime = dateTime.minusDays(1);
        }
        return dateTime;
    }

    /**
     * now 기준으로 가장 가까운 과거 월요일 LocalDateTime 리턴(오늘이 월요일인 경우 지난주 월요일을 찾는다.)
     */
    public static LocalDateTime getLastMondayZeroHour(long now) {
        //LocalDateTime dateTime = toDateTime(now).withNano(0).withSecond(0).withMinute(0).withHour(0).minusDays(1);  // tuesday 버전과 다르게 하루 전부터 찾는다.
        LocalDateTime dateTime = toDateTime(now).withNano(0).withSecond(0).withMinute(0).withHour(0).minusDays(7);
        for (int i = 0; i < 7; ++i) {
            //log.debug("getLastMondayZeroHour(): dateTime={}", dateTime);
            if (dateTime.getDayOfWeek() == DayOfWeek.MONDAY) {
                break;
            }
            dateTime = dateTime.plusDays(1);
        }
        return dateTime;
    }

    /**
     * RFC 822 time zone
     */
    public static String timezoneToGmt(String timezoneId) {
        TimeZone tz = TimeZone.getTimeZone(timezoneId);
        SimpleDateFormat fmt = new SimpleDateFormat("Z");
        fmt.setTimeZone(tz);
        return fmt.format(new Date(0L));
    }

    public static ZoneId getZoneId(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException e) {
            log.error("timezone:{} -> UTC", timezone);
            return ZoneId.of("UTC");
        }
    }

    /**
     * yyMMdd 포맷 일자 아이디
     */
    public static int createDateId(long now, String timezone) {
        ZoneId zoneId = getZoneId(timezone);
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(now), zoneId);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyMMdd").withLocale(Locale.ENGLISH).withZone(zoneId);
        return Integer.parseInt(ZonedDateTime.of(dateTime, zoneId).format(fmt));
    }

    public static int createYymm(long now) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(now), DateTimeUtil.SYS_ZONE_ID);
        return createYymm(dateTime);
    }

    public static int createYymm(LocalDateTime localDateTime) {
        return Integer.parseInt(ZonedDateTime.of(localDateTime, DateTimeUtil.SYS_ZONE_ID).format(DateTimeUtil.YYMM_FMT));
    }

    /**
     * endDateId - startDateId의 days를 계산한다.
     * 만약 yyMMdd 형식에 맞지 않는 dateId가 주어져서 날짜계산이 불가능한 경우,
     * 그냥 end-start의 단순 연산을 시행한다
     */
    public static int getDaysBetween(int startDateId, int endDateId) {
        try {
            LocalDate startDate = convertDateIdToLocalDate(startDateId);
            LocalDate endDate = convertDateIdToLocalDate(endDateId);
            return (int) ChronoUnit.DAYS.between(startDate, endDate); // long으로 차이가 날만큼의 days는 없음..
        } catch (Exception e) {
            return endDateId - startDateId;
        }
    }

    public static int getDaysBetween(LocalDate startDate, LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(startDate, endDate);
    }

    public static int getHoursBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return (int) ChronoUnit.HOURS.between(startDateTime, endDateTime);
    }

    public static int getMinutesBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return (int) ChronoUnit.MINUTES.between(startDateTime, endDateTime);
    }

    public static int getSecondBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return (int) ChronoUnit.SECONDS.between(startDateTime, endDateTime);
    }
    /**
     * yyMMdd 형식의 dateId(int)를 LocalDate 값으로 변환한다.
     *
     * @param dateId
     * @return
     */
    public static LocalDate convertDateIdToLocalDate(int dateId) {
        if(dateId > 991231) throw new IllegalStateException("dateId=" + dateId + " is too large.");

        int remain = dateId;
        int yy = dateId / 10000; // 190108 => 19
        remain = remain - yy * 10000; // 190108 - 190000 = 108
        int mm = remain / 100; // 108 => 1
        remain = remain - mm * 100; // 108 - 100 = 8
        int dd = remain;
        return LocalDate.of(2000 + yy, mm, dd);
    }

    /**
     * yyMMdd 형식의 dateId(int)를 LocalDateTime 값으로 변환한다.
     */
    public static LocalDateTime convertDateIdToLocalDateTime(int dateId) {
        if(dateId > 991231) throw new IllegalStateException("dateId=" + dateId + " is too large.");

        int remain = dateId;
        int yy = dateId / 10000; // 190108 => 19
        remain = remain - yy * 10000; // 190108 - 190000 = 108
        int mm = remain / 100; // 108 => 1
        remain = remain - mm * 100; // 108 - 100 = 8
        int dd = remain;
        return LocalDateTime.of(2000 + yy, mm, dd, 0, 0, 0);
    }

    /**
     * yyMMdd 형식의 dateId(int)를 epoch time 값으로 변환한다.
     */
    public static long convertDateIdToEpochTime(int dateId) {
        return toEpochMilli(convertDateIdToLocalDateTime(dateId));
    }

    /**
     * 밀리 세컨드 절삭
     */
    public static int toSecondsFrom(long time) {
        time = (time / 1000L);
        return (int) time;
    }

    /**
     * 밀리 세컨드 절삭
     */
    public static int toSecondsFrom(Date date) {
        long time = date.getTime();
        time = (time / 1000L);
        return (int) time;
    }

    private DateTimeUtil() {

    }

    public static Timestamp toTimestamp(long now) {
        return Timestamp.valueOf(toDateTime(now));
    }

//	public static long getMinDate() {
//		return MIN_DATE;
//	}
//
//	public static long getMaxDate() {
//		return MAX_DATE;
//	}

    public static Calendar getZeroTimeOfSameDay(Calendar theDay) {
        Calendar cal = Calendar.getInstance();
        cal.set(theDay.get(Calendar.YEAR), theDay.get(Calendar.MONTH), theDay.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    public static Calendar getFirstDayOfSameWeek(Calendar theDay, boolean setTimePartZero) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(theDay.getTime());
        // DEV NOTE : 일요일이 첫주의 시작으로 표현됨
        while (cal.get(Calendar.DAY_OF_WEEK) != cal.getFirstDayOfWeek()) {
            cal.add(Calendar.DATE, -1);
        }
        if (setTimePartZero) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }
        return cal;
    }

    public static Calendar getFirstDayOfSameMonth(Calendar theDay, boolean setTimePartZero) {
        Calendar cal = Calendar.getInstance();
        cal.set(theDay.get(Calendar.YEAR), theDay.get(Calendar.MONTH), 1);
        if (setTimePartZero) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }
        return cal;
    }

    public static Calendar getEndOfDateInPeriod(long reference, Period period) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(reference);

        if (period.getYears() > 0) cal.add(Calendar.YEAR, period.getYears());
        if (period.getMonths() > 0) cal.add(Calendar.MONTH, period.getMonths());
        if (period.getDays() > 0) cal.add(Calendar.DATE, period.getDays());

        return cal;
    }

    public static Period getPeriod(long startTime, long endTime) {
        LocalDate startDate = (new Date(startTime)).toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
        LocalDate endDate = (new Date(endTime)).toInstant().atZone(ZoneId.of("UTC")).toLocalDate();

        return Period.between(startDate, endDate);
    }

    public static Pair<Integer, Long> getIterationStartDateIdAndEndDate(long now, int iterationDays) {
        long iterationIndex = now / ONE_DAY_IN_MILLIS / iterationDays;
        long iterationStart = iterationIndex * ONE_DAY_IN_MILLIS * iterationDays;
        long iterationEnd = iterationStart + iterationDays * ONE_DAY_IN_MILLIS;
        return Pair.of(getDateId(iterationStart), iterationEnd);
    }

    /**
     * 2023-01-01 00:00:00 ( epoch: 1672531200000 ) 을 기점으로 경과한 초를 계산한다.
     * 해당 시점을 기준으로 30년 경과시 (2053-01-01 00:00:00 - epoch: 2619302400000) (2619302400000 - 1672531200000 = 946,771,200) 이므로,
     * 즉, 앞으로 30년 까지는 9 자리에서 여유있게 처리된다.
     */
    public static long convertToSecondsSinceEpoch2023(long now) {
        return (now - getEpoch2023()) / 1000L;
    }

    /**
     * 2023-01-01 00:00:00을 기점 으로 경과한 초(elapseSecondsSinceEpoch2023)가 실제 시간으로 언제인지 계산한다.
     */
    public static long convertFromSecondsSinceEpoch2023(long elapseSecondsSinceEpoch2023) {
        return getEpoch2023() + elapseSecondsSinceEpoch2023 * 1000L;
    }

    /**
     * t1과 t2 사이의 날짜수를 계산한다. 동일 날짜의 경우 0이 리턴된다.
     */
    public static int getDaysBetween(long t1, long t2) {
        LocalDate t1DateZeroHour = DateTimeUtil.toDateTime(t1).toLocalDate();
        LocalDate t2DateZeroHour = DateTimeUtil.toDateTime(t2).toLocalDate();
        int day = (int) ChronoUnit.DAYS.between(t1DateZeroHour, t2DateZeroHour);
        return day;
    }

    /**
     * @param currentDateTime   현 일시
     * @param endDateTime       대상 ( 종료, 만료.. ) 일시
     * @return 현 시점 기준 대상 일시가 몇 초 남았는지 리턴
     */
    public static long getRemainSeconds(LocalDateTime currentDateTime, LocalDateTime endDateTime) {
        if(endDateTime.isBefore(currentDateTime)) return 0;

        return (Timestamp.valueOf(endDateTime).getTime() - Timestamp.valueOf(currentDateTime).getTime()) / 1000;
    }

    public static int getCurrentTimeSeconds() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public static long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static int getCurrentTimeSeconds(long now) {
        return (int) (now / 1000);
    }

    public static long getYesterdayTimeMillis(long now){
        return toDateTime(now).minusDays(1).toEpochSecond(ZoneOffset.UTC) * 1000;
    }

    public static Long roundTimestamp(long timestamp, int n) {
        long nMinutesInMillis = (long) n * 60 * 1000;
        return (timestamp / nMinutesInMillis) * nMinutesInMillis;
    }

    /**
     * apache-common, spring.util 을 비롯하여 많은 라이브러리에서 Pair 클래스를 제공한다.
     * 다만 DateTimeUtil 클래스는 JDK 를 제외한 어떤 라이브러리에도 종속된 코드가 들어가는것을 지양하므로..
     * <p>
     * 코드 출처 : https://stackoverflow.com/questions/521171/a-java-collection-of-value-pairs-tuples
     */
    public static class Pair<L, R> {
        final L left;
        final R right;

        public Pair(L left, R right) {
            this.left = left;
            this.right = right;
        }

        static <L, R> Pair<L, R> of(L left, R right) {
            return new Pair<>(left, right);
        }
    }

}
