package powerpanelserver;

public class Utils {

    protected static String formatSeconds(Integer seconds) {
        Integer hours = seconds / 3600;
        seconds -= 3600 * hours;
        Integer minutes = seconds / 60;
        seconds -= 60 * minutes;
        if (hours.equals(0)) {
            if (minutes.equals(0)) {
                return Integer.toString(seconds) + " secs";
            } else {
                return Integer.toString(minutes) + " mins " + Integer.toString(seconds) + " secs";
            }
        } else {
            return Integer.toString(hours) + " hours " + Integer.toString(minutes) + " mins " + Integer.toString(seconds) + " secs";
        }
    }
}
