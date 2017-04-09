package dk.jens.backup;

public interface BlacklistListener {
    void onBlacklistChanged(CharSequence[] blacklist, int id);
}
