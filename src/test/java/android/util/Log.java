package android.util;

// this class mocks the android Log class
public class Log {
    public static int d(String tag, String msg) {
        System.out.println(String.format("log msg: %s, %s", tag, msg));
        return 0;
    }
    public static int e(String tag, String msg) {
        System.out.println(String.format("log msg: %s, %s", tag, msg));
        return 0;
    }
    public static int i(String tag, String msg) {
        System.out.println(String.format("log msg: %s, %s", tag, msg));
        return 0;
    }
    public static int w(String tag, String msg) {
        System.out.println(String.format("log msg: %s, %s", tag, msg));
        return 0;
    }
}
