package com.machiav3lli.backup.items;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ActionResult {
    public final boolean succeeded;
    private final AppInfo app;
    private final Date occurrence;
    private final String message;

    public ActionResult(AppInfo app, @NotNull String message, boolean succeeded) {
        this.occurrence = Calendar.getInstance().getTime();
        this.app = app;
        this.succeeded = succeeded;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @NotNull
    @Override
    public String toString() {
        return String.format(
                "%s: %s%s",
                new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss", Locale.ENGLISH).format(this.occurrence),
                this.app != null ? this.app : "NoApp",
                this.message.isEmpty() ? "" : ' ' + this.message
        );
    }
}
