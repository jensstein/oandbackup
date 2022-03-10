## In next major Release

- [ ] Full support for Special Backups:

  - [ ] Accounts: needs test of latest neo/pumpkin

  - [x] SMS/MMS (@dl200010)

  - [ ] Bluetooth: needs test of latest neo/pumpkin

  - [x] Wifi

  - [ ] Widgets: needs test of latest neo/pumpkin

  - [x] Call logs (@dl200010)

  - [ ] Data usage policy: Do we really need to keep it?

  - [ ] Fingerprints: needs further test of latest neo/pumpkin (@hg42)

  - [ ] Wallpaper: do we really need to keep it?

- [x] TargetSDK 31

- [ ] Restore saved permissions

- [ ] Refactor backup to use scripts (@hg42)

## Needed Fixes

- [ ] TargetSDK 33

- [ ] Fix GSF push notifications' issue: seems to work on a set of apps only.

- [ ] Compatibility with Work Profiles: making Backup/Restore-Task profiles-sensitive

- [ ] Restore to chosen user

## Possible Features

- [ ] -Rootless Backups: Off the plan respecting the stats que.-

- [ ] New functions/tools:

  - [ ] AppSheet: share backup

  - [ ] Sort/filter based on changed data (last open after last backup - using 'UsageStatistics')

  - [ ] (re)integrate OpenGPG

  - [ ] Legacy Backup mode

  - [ ] Add flashable Zip feature

- [ ] Revamp UI/UX:

    - [ ] MD3

    - [ ] Material You support

## Planned Restructure

- [ ] Refactor app/package classes

- [ ] Refactor backup/restore classes

- [ ] Enable caching/instant load on launch

- [ ] Migrate to Jetpack Compose instead of XML (where possible)



