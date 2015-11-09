package processing;

public class TimeUtil {
    
    private static final int SECOND = 0;
    private static final int MINUTE = 1;
    private static final int HOUR = 2;
    private static final int DAY = 3;
    private static final int FIFTEEN_DAYS = 4;
    private static final int MONTH = 5;
    
    /**
     * get duration count for the dataset.
     * @param duration
     * @param granularityLevel
     * @return
     */
    private static int getDurationAtGranularity(int duration, int granularityLevel){
        int time_count = 0;
        int secondsInMinute = 60;
        int secondsInHour = 60 * 60;
        int secondsInDay = 24 * 60 * 60;
        int secondsInWeek = 7 * 24 * 60 * 60;
        int secondsInFifteenDays = 15 * 24 * 60 * 60;
        int secondsInMonth = 30 * 24 * 60 * 60;
        switch(granularityLevel){
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
