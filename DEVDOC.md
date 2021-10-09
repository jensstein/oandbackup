## Possible Features

- [ ] Rootless Backups: Off the plan respecting the stats que.

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

- [x] Revamp UI/UX:

    - [x] Animations and elevation

    - [x] MD3: All the already available components from MD-Android.

## Needed Fixes

- [ ] Compatibility with Work Profiles: making Backup/Restore-Task profiles-sensitive. (Next Version)

- [x] Handle the error about disabling "verify apps over USB".

- [x] Replace deprecated Classes/Methods:

  - [x] AsyncTask

  - [x] ProgressDialog

- [x] kill or not kill: all system apps, uids < 10000 or blacklisting...

- [x] TargetSDK 30 & updated StorageManger

- [x] Improve on performance in aftermath of using SAF (maybe more can be done?)

- [x] Enable encryption direct shortcut

- [x] Not responding to clicks after long time in background

- [x] Fix GSF push notifications' issue: seems to work on a set of apps only.

## Planned Restructure

- [x] Improve UI/UX (on anniversary release as co-op with @opepp)

- [ ] Use DataBinding/BindingAdapter where useful

- [x] Refactor Schedules handling

- [x] Migrate to Kotlin

- [x] Use Kotlin style explicitly

- [x] ViewModel where useful

  - [x] Scheduler

  - [x] Main & Batch

  - [x] AppSheet

- [x] Rewrite Help text

- [x] Theming engine (with accent/primary and secondary)

- [x] Automatic languages integration/detection

- [x] Full separation of concerns of Activities and Fragments

- [x] Better management for filtering and modes

## In next major Release

- [ ] (re)integrate OpenGPG

- [ ] Fully support for Special Backups:

  - [ ] Accounts

  - [ ] SMS

  - [ ] Bluetooth

  - [ ] Wifi

  - [ ] and others…

- [ ] TargetSDK 31

- [ ] Restore to chosen user

- [ ] Sort/filter based on changed data (last open after last backup - using 'UsageStatistics')

## Low Priority(sorted)

- [ ] Legacy Backup mode

- [ ] Migrate to Jetpack Compose instead of XML (where possible)

- [ ] Add flashable Zip feature