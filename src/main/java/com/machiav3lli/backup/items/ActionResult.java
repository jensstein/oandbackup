package com.machiav3lli.backup.items;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ActionResult {
    public static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss", Locale.ENGLISH);
    public final AppInfo app;
    public final Date occurrence;
    public final String message;
    public final boolean succeeded;

    public ActionResult(@NotNull AppInfo app, @NotNull String message, boolean succeeded) {
        this.occurrence = Calendar.getInstance().getTime();
        this.app = app;
        this.succeeded = succeeded;
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format(
                "%s: %s%s",
                ActionResult.timeFormat.format(this.occurrence),
                this.app,
                this.message.isEmpty() ? "" : ' ' + this.message
        );
    }
}
