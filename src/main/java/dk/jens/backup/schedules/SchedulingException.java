package dk.jens.backup.schedules;

public class SchedulingException extends Exception {
    public SchedulingException(String msg) {
        super(msg);
    }
    public SchedulingException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
