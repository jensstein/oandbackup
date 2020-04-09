package com.machiav3lli.backup.schedules;

public interface BlacklistListener {
    void onBlacklistChanged(CharSequence[] blacklist, int id);
}
