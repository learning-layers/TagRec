package common;

public class TimeUtil {
    
    public static final int SECOND = 0;
    public static final int MINUTE = 1;
    public static final int HOUR = 2;
    public static final int DAY = 3;
    public static final int FIFTEEN_DAYS = 4;
    public static final int MONTH = 5;
    
    /**
     * get duration count for the dataset.
     * @param duration
     * @param granularityLevel
     * @return
     */
    public static int getDurationAtGranularity(int duration, int granularityLevel){
        int time_count = 0;
        int secondsInSeconds = 1;
        int secondsInMinute = 60;
        int secondsInHour = 60 * 60;
        int secondsInDay = 24 * 60 * 60;
        int secondsInWeek = 7 * 24 * 60 * 60;
        int secondsInFifteenDays = 15 * 24 * 60 * 60;
        int secondsInMonth = 30 * 24 * 60 * 60;
        switch(granularityLevel){
            case SECOND:
                time_count = duration / secondsInSeconds;
                break;
            case MINUTE:
                time_count = duration / secondsInMinute;
                break;
            case HOUR:
                time_count = duration / secondsInHour;
                break;
            case DAY:
                time_count = duration / secondsInDay;
                break;
            case FIFTEEN_DAYS:
                time_count = duration / secondsInFifteenDays;
                break;
            case MONTH:
                time_count = duration / secondsInMonth;
                break;
        }
        return time_count;
    }

}
