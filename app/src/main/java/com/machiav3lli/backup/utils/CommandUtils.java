package com.machiav3lli.backup.utils;

import android.os.Build;

import com.machiav3lli.backup.Constants;

import java.util.Arrays;

public class CommandUtils {
    private static final String TAG = Constants.classTag(".CommandUtils");

    public static String iterableToString(String[] array) {
        return iterableToString(Arrays.asList(array));
    }

    public static String iterableToString(CharSequence delimiter, String[] array) {
        return iterableToString(delimiter, Arrays.asList(array));
    }

    public static String iterableToString(Iterable<String> iterable) {
        return iterableToString("", iterable);
    }

    public static String iterableToString(CharSequence delimiter, Iterable<String> iterable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return String.join(delimiter, iterable);
        } else {
            StringBuilder sb = new StringBuilder();
            for (String s : iterable) {
                sb.append(s);
            }
            return sb.toString();
        }
    }

    public interface Command {
        void execute();
    }
}
