# OAndBackupX  <img align="left" src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/icon.png" width="64" />

OAndBackupX is a fork of the infamous OAndBackup with the aim to bring OAndBackup to 2020. For now most of the functionality and UI of the app are rewritten, next steps would be making it stable and adding some features which could ease the backup/restore work with any device. Therefore all types of contribution are welcome.

Usecases: a combination with your favourite sync solution (e.g. Syncthing, Nextcloud...)  keeping a copy of your apps and data on your server or "stable" device could bring a lot of benefits and save you a lot of work while changing ROMs or just cleaning your mobile device.

Now on OAndBackup: a backup program for android. requires root and allows you to backup individual apps and their data.
both backup and restore of individual programs one at a time and batch backup and restore of multiple programs are supported (with silent / unattended restores). 
restoring system apps should be possible without requiring a reboot afterwards. OAndBackup is also able to uninstall system apps. handling system apps in this way depends on whether /system/ can be remounted as writeable though, so this will probably not work for all devices (e.g. htc devices with the security flag on).  
backups can be scheduled with no limit on the number of individual schedules and there is the possibility of creating custom lists from the list of installed apps.

## Community

There's a new room on Riot to discuss the development of the App:

[oandbackupx:matrix.org](https://matrix.to/#/!PiXJUneYCnkWAjekqX:matrix.org?via=matrix.org) 

## Compatibility with oandbackup backups

till the version 0.9.3 there's been no structural change in how the app handles backup/restore. So you could use that version to restore the old backups, then move to the newest version and renew your backups so that they'll stay compatible as long as the logic of the app doesn't radically change.

## Changes & TODOs

- [x] Fixing OAB-Utils build problem which was caused by a deprecated method in Rust
- [x] Adapt FastAdapter: for Main, Batch and Scheduler
- [x] Rewrite Main-, Batch- & Scheduler-(Activity, Adapter and Sorter)
- [x] Modeling the app's structure: for the most part now
- [x] Modeling Sort/Filter
- [ ] Add some new filters
- [ ] Add Exodus Report to AppSheet
- [x] Rewrite Preferences
- [x] Integrate Tools and Help in Preferences
- [x] New UI and UX: Design improvement proposals are always welcome
- [x] Add Dark/Light themes
- [ ] Update dialogs' UI: partially done
- [x] Add support for protected data backup
- [x] Exclude cache from data backups
- [x] Prompt to turn the Battery Optimization off
- [ ] Add in-app backup encryption (first: after 1.0)
- [ ] Rewrite the logic of Backup/Restore: is a PROCESS
- [ ] Switch to Storage Access Framework: moved to SAF but still supports only local storage for now/ connected to the previous goal
- [ ] New android Scope Storage permissions compatibility: fixed for Android 10 with legacy mode for now(first: after android 11 release)
- [ ] Add Split Apk Support
- [ ] Add a Flashable-ZIP feature
- [ ] You suggest!...

## Screenshots

### Dark Theme

<p float="left">
 <img src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="170" />
 <img src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="170" />
 <img src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="170" />
 <img src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" width="170" />
</p>

### Light Theme

<p float="left">
 <img src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/phoneScreenshots/5.png" width="170" />
 <img src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/phoneScreenshots/6.png" width="170" />
 <img src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/phoneScreenshots/7.png" width="170" />
 <img src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/phoneScreenshots/8.png" width="170" />
</p>

## Building

OAndBackupX is built with gradle. you need the android sdk, rust (for building the oab-utils binary), and bash (or a compatible shell for executing the oab-utils build script).

P.S: If you have any problem building OAB-Utils: you can find some helping notes in its Readme.md

## Busybox / OAB-Utils

a working busybox installation is required at the moment, but work is in progress to include all the needed functionality in a binary included in the apk. this program is called oab-utils and is written in rust.

Busybox is available on F-Droid or you can build it yourself from [here](https://busybox.net).

copy the busybox binary to your system, for example /system/xbin or /data/local, and make it executable. symlinking is not necessary for use with oandbackupx. in the oandbackupx preferences, provide the whole path to the busybox binary, including the binary's file name (e.g. /data/local/busybox).

## Licenses

as a fork of OAndBackup, OAndBackupX is licensed under the MIT license (see LICENSE.txt)

App's icon is based on an Icon made by [Catalin Fertu](https://www.flaticon.com/authors/catalin-fertu) from [www.flaticon.com](https://www.flaticon.com)

Placeholders Icon made by [Smashicons](https://www.flaticon.com/authors/smashicons) from [www.flaticon.com](https://www.flaticon.com)

## Credits

[Jens Stein](https://github.com/jensstein) for his unbelievably valuable work on OAndBackup.

[Rahul Patel](https://github.com/whyorean) whose hard work on AuroraStore inspired this work.

Open-Source libs: [ButterKnife](https://github.com/JakeWharton/butterknife), [FastAdapter](https://github.com/mikepenz/FastAdapter), [RootBeer](https://github.com/scottyab/rootbeer).

## author

Antonios Hazim
