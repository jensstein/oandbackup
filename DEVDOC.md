## Possible Features

- [ ] Rootless Backups: maybe [Shizuku](https://github.com/RikkaApps/Shizuku) could be useful in this matter.

- [ ] More functionality for AppSheet: kill app, share backup, clear data (for now...).

- [x] Multi-Backups support

- [ ] Add options upon choosing Backup from AppSheet: external/obb/cache.

- [x] External Storage/SAF support: my idea on it is splitting Backup-Actions into more steps(vice versa for Restore)

- [x] SymLinks and "special" Files backup: fixing errors on some "unique" apps.

- [ ] Add Flashable Zip feature: lowest priority for now.

- [ ] Reformat Date handling in schedules

- [x] Customize tags icons' colors

- [x] Columnize batch backup preference

## Needed Fixes

- [ ] Fully support for Special Backups: Accounts, SMS, Bluetooth, Wifi (for now...).

- [ ] Compatibility with Work Profiles: making Backup/Restore-Task profiles-sensitive. NEEDS TESTING

- [ ] Handle the error about disabling "verify apps over USB".

- [ ] Replace deprecated Classes/Methods: AsyncTask, ProgressDialog (mostly with kotlin).

- [x] kil or not kill: all system apps, uids < 10000 or blacklisting...

- [ ] TargetSDK 30: storage access problems

- [x] Improve on performance in aftermath of using SAF (maybe more can be done?)

- [x] Enable encryption direct shortcut

## Planned Restructure

- [ ] Use DataBinding for Items.

- [ ] Migrate to Kotlin
