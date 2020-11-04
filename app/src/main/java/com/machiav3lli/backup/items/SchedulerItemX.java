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

import android.view.View;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.machiav3lli.backup.R;
import com.machiav3lli.backup.schedules.HandleAlarms;
import com.machiav3lli.backup.schedules.db.Schedule;
import com.machiav3lli.backup.utils.ItemUtils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SchedulerItemX extends AbstractItem<SchedulerItemX.ViewHolder> {
    Schedule schedule;

    public SchedulerItemX(Schedule schedule) {
        this.schedule = schedule;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_scheduler_x;
    }

    @NotNull
    @Override
    public ViewHolder getViewHolder(@NotNull View view) {
        return new ViewHolder(view);
    }

    @Override
    public long getIdentifier() {
        return ItemUtils.calculateScheduleID(schedule);
    }

    @Override
    public int getType() {
        return R.id.fastadapter_item;
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<SchedulerItemX> {
        AppCompatCheckBox checkbox = itemView.findViewById(R.id.enableCheckbox);
        AppCompatTextView timeLeft = itemView.findViewById(R.id.timeLeft);
        LinearLayoutCompat timeLeftLine = itemView.findViewById(R.id.timeLeftLine);
        AppCompatTextView scheduleMode = itemView.findViewById(R.id.schedMode);
        AppCompatTextView scheduleSubMode = itemView.findViewById(R.id.schedSubMode);

        public ViewHolder(View view) {
            super(view);
        }

        @Override
        public void bindView(@NotNull SchedulerItemX item, @NotNull List<?> list) {
            final Schedule schedule = item.getSchedule();
            setScheduleMode(schedule);
            setScheduleSubMode(schedule);
            checkbox.setChecked(schedule.getEnabled());
            setTimeLeft(schedule, System.currentTimeMillis());
            final long tag = schedule.getId();
            checkbox.setTag(tag);
        }

        void setScheduleMode(Schedule schedule) {
            switch (schedule.getMode().getValue()) {
                case 1:
                    scheduleMode.setText(R.string.radio_user);
                    break;
                case 2:
                    scheduleMode.setText(R.string.radio_system);
                    break;
                case 3:
                    scheduleMode.setText(R.string.showNewAndUpdated);
                    break;
                default:
                    scheduleMode.setText(R.string.radio_all);
                    break;
            }
        }

        void setScheduleSubMode(Schedule schedule) {
            switch (schedule.getSubMode().getValue()) {
                case 1:
                    scheduleSubMode.setText(R.string.radio_apk);
                    break;
                case 2:
                    scheduleSubMode.setText(R.string.radio_data);
                    break;
                default:
                    scheduleSubMode.setText(R.string.radio_both);
                    break;
            }
        }

        void setTimeLeft(Schedule schedule, long now) {
            if (!schedule.getEnabled()) {
                timeLeft.setText("");
                timeLeftLine.setVisibility(View.INVISIBLE);
            } else {
                final long timeDiff = HandleAlarms.timeUntilNextEvent(schedule.getInterval(),
                        schedule.getTimeHour(), schedule.getTimeMinute(), schedule.getTimePlaced(), now);
                int sum = (int) (timeDiff / 1000f / 60f);
                int hours = sum / 60;
                int minutes = sum % 60;
                timeLeft.setText(String.format(" %s:%s", hours, minutes));
                timeLeftLine.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void unbindView(@NotNull SchedulerItemX item) {
            scheduleMode.setText(null);
            scheduleSubMode.setText(null);
            timeLeft.setText(null);
        }
    }
}
