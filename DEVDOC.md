## Possible Features

- [ ] Rootless Backups: maybe [Shizuku](https://github.com/RikkaApps/Shizuku) could be useful in this matter.

- [ ] More functionality for AppSheet: kill app, share backup, clear data (for now...).

- [x] Multi-Backups support

- [ ] Add options upon choosing Backup from AppSheet: external/obb/cache.

- [x] External Storage/SAF support: my idea on it is splitting Backup-Actions into more steps(vice versa for Restore)

- [x] SymLinks and "special" Files backup: fixing errors on some "unique" apps.

- [x] Reformat Date handling in schedules

- [x] Customize tags icons' colors

- [x] Columnize batch backup preference

## Needed Fixes

- [ ] Fully support for Special Backups: Accounts, SMS, Bluetooth, Wifi (for now...).

- [ ] Compatibility with Work Profiles: making Backup/Restore-Task profiles-sensitive. NEEDS TESTING

- [x] Handle the error about disabling "verify apps over USB".

- [x] Replace deprecated Classes/Methods:
    - [x] AsyncTask
    - [x] ProgressDialog

- [x] kill or not kill: all system apps, uids < 10000 or blacklisting...

- [ ] TargetSDK 30 & updated StorageManger on SDK29: storage access problems

- [x] Improve on performance in aftermath of using SAF (maybe more can be done?)

- [x] Enable encryption direct shortcut

- [x] Not responding to clicks after long time in background

## Planned Restructure

- [ ] Use DataBinding/BindingAdapter where useful

- [x] Migrate to Kotlin

- [ ] Use Kotlin style explicitly: a process taking place with each class refactored

- [ ] ViewModel where useful

    - [x] Scheduler

    - [ ] Main & Batch

## Low Priority

- [ ] Add Flashable Zip feature: lowest priority for now.

- [ ] (re)add OpenGPG

- [ ] Legacy Backup mode
