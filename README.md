# OAndBackupX  <img align="left" src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/icon.png" width="64" />

OAndBackupX is a fork of the infamous OAndBackup with the aim to bring OAndBackup to 2020. For now most of the functionality and UI of the app are rewritten, next steps would be making it stable and adding some features which could ease the backup/restore workflow with any device. Therefore all types of contribution are welcome.

Now on OAndBackup: a backup program for android. requires root and allows you to backup individual apps and their data.
both backup and restore of individual programs one at a time and batch backup and restore of multiple programs are supported (with silent / unattended restores). 
restoring system apps should be possible without requiring a reboot afterwards. OAndBackup is also able to uninstall system apps. handling system apps in this way depends on whether /system/ can be remounted as writeable though, so this will probably not work for all devices (e.g. htc devices with the security flag on).  
backups can be scheduled with no limit on the number of individual schedules and there is the possibility of creating custom lists from the list of installed apps.

## Installation

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/com.machiav3lli.backup/)
[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" alt="Get it on IzzyOnDroid" height="80">](https://apt.izzysoft.de/fdroid/index/apk/com.machiav3lli.backup)
[<img src="badge_github.png" alt="Get it on GitHub" height="80">](https://github.com/machiav3lli/oandbackupx/releases)

## Recommendation

A combination with your favourite sync solution (e.g. Syncthing, Nextcloud...)  keeping an encrypted copy of your apps and their data on your server or "stable" device could bring a lot of benefits and save you a lot of work while changing ROMs or just cleaning your mobile device.

## Community

There's a new room on Riot and a group on Telegram to discuss the development of the App and test new versions:

[oandbackupx:matrix.org](https://matrix.to/#/!PiXJUneYCnkWAjekqX:matrix.org?via=matrix.org) 

[t.me/OAndBackupX](https://t.me/OAndBackupX)

## Encryption

If enabled the data backup will be encrypted with AES 256 based on a password you can set in the settings, which you'll have to use when you want to restore the data. This way you can store your backups more securely, worrying less about their readability.  

## Compatibility with oandbackup backups

till the version 0.9.3 there's been no structural change in how the app handles backup/restore. So you could use that version to restore the old backups, then move to the newest version and renew your backups so that they'll stay compatible as long as the logic of the app doesn't radically change.

## Changes & TODOs

#### [Changelog](https://github.com/machiav3lli/oandbackupx/blob/master/changelog.md)

- [x] Whole new UI and UX (Design improvement proposals are always welcome)
- [x] Support for latest android versions
- [x] Add Split Apk Support
- [x] Modeling Sort/Filter and added some new filters
- [x] Add App Info and Exodus Report shortcuts to AppSheet
- [x] Extend Preferences
- [x] Add support for protected data, external data and obb backups
- [x] Clear cache before data backups
- [x] Add in-app backup encryption
- [ ] Rewrite the logic of Backup/Restore: is a PROCESS (~30% done)
- [ ] Switch to Storage Access Framework (after achieving the previous one)
- [ ] New android Scope Storage permissions compatibility (first: after android 11 release - if forced to)
- [ ] Add a Flashable-ZIP feature
- [ ] You suggest!...

## Screenshots

### Dark Theme

<p float="left">
 <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="170" />
 <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="170" />
 <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="170" />
 <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" width="170" />
</p>

### Light Theme

<p float="left">
 <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/5.png" width="170" />
 <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/6.png" width="170" />
 <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/7.png" width="170" />
 <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/8.png" width="170" />
</p>

## Building

OAndBackupX is built with gradle, for that you need the android sdk.

## Licenses

as a fork of OAndBackup, OAndBackupX is licensed under the MIT license (see LICENSE.txt)

App's icon is based on an Icon made by [Catalin Fertu](https://www.flaticon.com/authors/catalin-fertu) from [www.flaticon.com](https://www.flaticon.com)

Placeholders Icon made by [Smashicons](https://www.flaticon.com/authors/smashicons) from [www.flaticon.com](https://www.flaticon.com)

## Contribution

[Jens Stein](https://github.com/jensstein) for his unbelievably valuable work on OAndBackup. 

Languages: Greek: [Kostas Giapis](https://github.com/tsiflimagas), Italian: [Urnyx05](https://github.com/Urnyx05), English: [Atrate](https://github.com/Atrate), Chinese: [Tuchit](https://github.com/tuchit) and [Linsui](https://github.com/linsui), Turkish [scrubjay55](https://github.com/scrubjay55), Polish [Antyradek](https://github.com/Antyradek).

Features: Split-APK: [Tiefkuehlpizze](https://github.com/Tiefkuehlpizze).

Open-Source libs: [ButterKnife](https://github.com/JakeWharton/butterknife), [FastAdapter](https://github.com/mikepenz/FastAdapter), [Zip4J](https://github.com/srikanth-lingala/zip4j), [RootBeer](https://github.com/scottyab/rootbeer), [NumberPicker](https://github.com/ShawnLin013/NumberPicker).

## Author

Antonios Hazim
