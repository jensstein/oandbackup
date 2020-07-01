package com.machiav3lli.backup;

public interface BlacklistListener {
    void onBlacklistChanged(CharSequence[] blacklist, int id);
}
