## Possible Features

- [ ] Rootless Backups: maybe [Shizuku](https://github.com/RikkaApps/Shizuku) could be useful in this matter.

- [ ] More functionality for AppSheet:

  - [x] Kill app

  - [ ] share backup

  - [x] launch

  - [x] package info shortcut

- [x] Multi-Backups support

- [x] Add options upon choosing Backup from AppSheet: external/obb/cache.

- [x] External Storage/SAF support: my idea on it is splitting Backup-Actions into more steps(vice versa for Restore)

- [x] SymLinks and "special" Files backup: fixing errors on some "unique" apps.

- [x] Reformat Date handling in schedules

- [x] Customize tags icons' colors

- [x] Columnize batch backup preference

## Needed Fixes

- [ ] Fully support for Special Backups:

  - [ ] Accounts

  - [ ] SMS

  - [ ] Bluetooth

  - [ ] Wifi

  - [ ] and others…

- [ ] Compatibility with Work Profiles: making Backup/Restore-Task profiles-sensitive. (Next Version)

- [x] Handle the error about disabling "verify apps over USB".

- [x] Replace deprecated Classes/Methods:
  
  - [x] AsyncTask

  - [x] ProgressDialog

- [x] kill or not kill: all system apps, uids < 10000 or blacklisting...

- [ ] TargetSDK 30 & updated StorageManger on SDK29: on test

- [x] Improve on performance in aftermath of using SAF (maybe more can be done?)

- [x] Enable encryption direct shortcut

- [x] Not responding to clicks after long time in background

- [x] Fix GSF push notifications' issue. NEEDS SOME TESTING

## Planned Restructure

- [x] Improve UI/UX (on anniversary release as co-op with @opepp)

- [ ] Use DataBinding/BindingAdapter where useful

- [ ] Migrate to Jetpack Compose instead of XML (where possible)

- [x] Refactore Batch & Schedules handling

- [x] Migrate to Kotlin

- [x] Use Kotlin style explicitly: a process taking place with each class refactored

- [x] ViewModel where useful
  
  - [x] Scheduler
  
  - [x] Main & Batch
  
  - [x] AppSheet

- [x] Rewrite Help text

## Low Priority

- [ ] Add Flashable Zip feature

- [ ] (re)integrate OpenGPG

- [ ] Legacy Backup mode
