package com.machiav3lli.backup.items;

import android.view.View;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.machiav3lli.backup.R;
import com.machiav3lli.backup.schedules.HandleAlarms;
import com.machiav3lli.backup.schedules.db.Schedule;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SchedulerItemX extends AbstractItem<SchedulerItemX.ViewHolder> {
    Schedule sched;

    public SchedulerItemX(Schedule sched) {
        this.sched = sched;
    }

    public Schedule getSched() {
        return sched;
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
        return sched.getId() + sched.getInterval() * 24 + sched.getHour() + sched.getMode().getValue() + sched.getSubmode().getValue() + (sched.isEnabled() ? 1 : 0);
    }

    @Override
    public int getType() {
        return R.id.fastadapter_item;
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<SchedulerItemX> {
        AppCompatCheckBox checkbox = itemView.findViewById(R.id.enableCheckbox);
        AppCompatTextView timeLeft = itemView.findViewById(R.id.timeLeft);
        LinearLayoutCompat timeLeftLine = itemView.findViewById(R.id.timeLeftLine);
        AppCompatTextView schedMode = itemView.findViewById(R.id.schedMode);
        AppCompatTextView schedSubMode = itemView.findViewById(R.id.schedSubMode);

        public ViewHolder(View view) {
            super(view);
        }

        @Override
        public void bindView(@NotNull SchedulerItemX item, @NotNull List<?> list) {
            final Schedule sched = item.getSched();

            // TODO change/fix the strings
            setSchedMode(sched);
            setSchedSubMode(sched);
            checkbox.setChecked(sched.isEnabled());
            setTimeLeft(sched, System.currentTimeMillis());
            final long tag = sched.getId();
            checkbox.setTag(tag);
        }

        void setSchedMode(Schedule schedule) {
            switch (schedule.getMode().getValue()) {
                case 0:
                    schedMode.setText(R.string.radioAll);
                    break;
                case 1:
                    schedMode.setText(R.string.radioUser);
                    break;
                case 2:
                    schedMode.setText(R.string.radioSystem);
                    break;
                case 3:
                    schedMode.setText(R.string.showNewAndUpdated);
                    break;
                default:
                    schedMode.setText(R.string.customListTitle);
                    break;
            }
        }

        void setSchedSubMode(Schedule schedule) {
            switch (schedule.getSubmode().getValue()) {
                case 0:
                    schedSubMode.setText(R.string.radioApk);
                    break;
                case 1:
                    schedSubMode.setText(R.string.radioData);
                    break;
                default:
                    schedSubMode.setText(R.string.radioBoth);
                    break;
            }
        }

        void setTimeLeft(Schedule schedule, long now) {
            if (!schedule.isEnabled()) {
                timeLeft.setText("");
                timeLeftLine.setVisibility(View.INVISIBLE);
            } else {
                final long timeDiff = HandleAlarms.timeUntilNextEvent(schedule.getInterval(),
                        schedule.getHour(), schedule.getPlaced(), now);
                int sum = (int) (timeDiff / 1000f / 60f);
                int hours = sum / 60;
                int minutes = sum % 60;
                timeLeft.setText(String.format(" %s:%s", hours, minutes));
                timeLeftLine.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void unbindView(@NotNull SchedulerItemX item) {
            schedMode.setText(null);
            schedSubMode.setText(null);
            timeLeft.setText(null);
        }
    }
}
