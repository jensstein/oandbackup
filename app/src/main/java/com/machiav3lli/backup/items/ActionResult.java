/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
