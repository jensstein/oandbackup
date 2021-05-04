# OAndBackupX  <img align="left" src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/icon.png" width="64" />

[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-v2.0%20adopted-ff69b4.svg)](COC.md) [![Main CI Workflow](https://github.com/machiav3lli/oandbackupx/workflows/Main%20CI%20Workflow/badge.svg?branch=master)](https://github.com/machiav3lli/oandbackupx/actions?query=workflow%3A%22Main+CI+Workflow%22)

OAndBackupX is a fork of the famous OAndBackup with the aim to bring OAndBackup to 2020. For now the app is already fully rewritten, coming up would be making it robust and adding some lengthily planned features which could ease the backup/restore workflow with any device. Therefore all types of contribution are always welcome.

Now on functionality of our App:

* It requires root and allows you to backup individual apps and their data.
* Both backup and restore of individual programs one at a time and batch backup and restore of multiple programs are supported.
* Restoring system apps should be possible without requiring a reboot afterwards.
* Backups can be scheduled with no limit on the number of individual schedules and there is the possibility of creating custom lists from the list of installed apps.

And here's the project's [FAQ](FAQ.md).

## Installation

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/com.machiav3lli.backup/)
[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" alt="Get it on IzzyOnDroid" height="80">](https://apt.izzysoft.de/fdroid/index/apk/com.machiav3lli.backup)
[<img src="badge_github.png" alt="Get it on GitHub" height="80">](https://github.com/machiav3lli/oandbackupx/releases)

## Recommendation

A combination with your favourite sync solution (e.g. Syncthing, Nextcloud...)  keeping an encrypted copy of your apps and their data on your server or "stable" device could bring a lot of benefits and save you a lot of work while changing ROMs or just cleaning your mobile device.

## Community

In those groups we discuss the development of the App and test new versions:

##### On Matrix: [OAndBackupX:Matrix.ORG](https://matrix.to/#/!PiXJUneYCnkWAjekqX:matrix.org?via=matrix.org)

##### On Telegram: [t.me/OAndBackupX](https://t.me/OAndBackupX)

Our **[Code of Conduct](COC.md)** applies to the communication in the community same as for all contributers.

## Encryption

If enabled the data backup will be encrypted with AES256 based on a password you can set in the settings, which you'll have to use when you want to restore the data. This way you can store your backups more securely, worrying less about their readability.

## Compatibility

Version 5.0.0 uses new encryption, new databases, fixes most of reported bugs in 4.0.0 and boost the performance to something near the 3.2.0's. With that said, it's incompatible with the older versions.

Version 4.0.0 marks a full overhaul of the app structure and thus breaks compatibility with previous versions.

Till the version 0.9.3 there's been no structural change in how the app handles backup/restore. So you could use that version to restore the old backups, then move to the newest version and renew your backups so that they'll stay compatible as long as the logic of the app doesn't radically change.

## Changes & TODOs

#### [Changelog](changelog.md)  &  [Known Issues](ISSUES.md)

if you have some kotlin and android knowledge and like to contribute to the project, see the following **[development document](DEVDOC.md)** to see what is still need to be done, where a help could be needed or if you'd like to take on one of the fixes.

The communication and each contribution in the project community should follow our **[Code of Conduct](COC.md)**.

## Screenshots

<p float="left">
 <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="170" />
 <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="170" />
 <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="170" />
 <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" width="170" />
</p>

## Building

OAndBackupX is built with gradle, for that you need the android sdk.

## Licenses <img align="right" src="agplv3.png" width="64" />

OAndBackupX is licensed under the [GNU's Affero GPL v3](LICENSE.md).

App's icon is based on an Icon made by [Catalin Fertu](https://www.flaticon.com/authors/catalin-fertu) from [www.flaticon.com](https://www.flaticon.com)

Placeholders icon foreground made by [Smashicons](https://www.flaticon.com/authors/smashicons) from [www.flaticon.com](https://www.flaticon.com)

## Credits

[Jens Stein](https://github.com/jensstein) for his unbelievably valuable work on OAndBackup.

[Nils](https://github.com/Tiefkuehlpizze), [Harald](https://github.com/hg42) and [Martin](https://github.com/Smojo) for their active contribution to the project.

[Oliver Pepperell](https://github.com/opepp) for his contribution to the new anniversary design.

Open-Source libs: [FastAdapter](https://github.com/mikepenz/FastAdapter), [RootBeer](https://github.com/scottyab/rootbeer), [NumberPicker](https://github.com/ShawnLin013/NumberPicker), [Apache Commons](https://commons.apache.org).

### Languages: [<img align="right" src="https://hosted.weblate.org/widgets/oandbackupx/-/287x66-white.png" alt="Übersetzungsstatus" />](https://hosted.weblate.org/engage/oandbackupx/?utm_source=widget)

[<img src="https://hosted.weblate.org/widgets/oandbackupx/-/multi-auto.svg" alt="Übersetzungsstatus" />](https://hosted.weblate.org/engage/oandbackupx/)

The Translations are now being hosted by [Weblate.org](https://hosted.weblate.org/engage/oandbackupx/).

Before that, translations were done analog/offline by those great people:

[Kostas Giapis](https://github.com/tsiflimagas), [Urnyx05](https://github.com/Urnyx05), [Atrate](https://github.com/Atrate), [Tuchit](https://github.com/tuchit), [Linsui](https://github.com/linsui), [scrubjay55](https://github.com/scrubjay55), [Antyradek](https://github.com/Antyradek), [Ninja1998](https://github.com/NiNjA1998), [elea11](https://github.com/elea11).

## Author

Antonios Hazim
