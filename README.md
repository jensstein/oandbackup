# OAndBackupX

OAndBackupX is a fork of the infamous OAndBackup with the aim to bring OAndBackup to 2020. For now most of the functionality and UI of the app are rewritten, next steps would be making it stable and adding some features which could ease the backup/restore work with any device. Therefore all types of contribution are welcome.

Usecases: a combination with your favourite sync solution (e.g. Syncthing, Nextcloud...)  keeping a copy of your apps and data on your server or "stable" device could bring a lot of benefits and save you a lot of work while changing ROMs or just cleaning your mobile device.

Now on OAndBackup: a backup program for android. requires root and allows you to backup individual apps and their data.
both backup and restore of individual programs one at a time and batch backup and restore of multiple programs are supported (with silent / unattended restores). 
restoring system apps should be possible without requiring a reboot afterwards. OAndBackup is also able to uninstall system apps. handling system apps in this way depends on whether /system/ can be remounted as writeable though, so this will probably not work for all devices (e.g. htc devices with the security flag on).  
backups can be scheduled with no limit on the number of individual schedules and there is the possibility of creating custom lists from the list of installed apps.

## TODO

- [x] Fixing OAB-Utils build problem which was caused by a deprecated method in Rust
- [x] Adapt FastAdapter: for Main and Batch
- [x] Rewrite Batch-(Activity, Adapter and Sorter) 
- [x] Rewrite Main-(Activity, Adapter and Sorter)
- [x] Add more informative dialog when clicking an app in Main
- [x] Rewrite Scheduler
- [x] Modeling Sort/Filter
- [ ] Add some new filters
- [x] Rewrite Preferences
- [x] Rewrite backup folder selector
- [x] Integrate Tools and Help in Preferences
- [ ] New android scope storage permissions compatibility: fixed for Android 10 with legacy mode(fix priority: med)
- [x] Updating UI and UX: Design improvement proposals are always welcome
- [x] Add Dark/Light themes
- [ ] Update dialogs' UI: partially done
- [ ] Abstracting the structure of the app
- [ ] Fragmentize the Preferences: partially done
- [ ] Rewrite the logic of Backup/Restore
- [ ] Fix and Add a better encryption solution(fix priority: med)
- [ ] Add a flashable-ZIP feature
- [ ] You suggest!...


## screenshots

### Dark Theme
<p float="left">
 <img src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="170" />
 <img src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="170" />
 <img src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="170" />
 <img src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" width="170" />
 <img src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/phoneScreenshots/5.png" width="170" />
 <img src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/phoneScreenshots/6.png" width="170" />
</p>

### Light Theme
<p float="left">
 <img src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/phoneScreenshots/7.png" width="170" />
 <img src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/phoneScreenshots/8.png" width="170" />
 <img src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/phoneScreenshots/9.png" width="170" />
 <img src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/phoneScreenshots/10.png" width="170" />
 <img src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/phoneScreenshots/11.png" width="170" />
 <img src="https://raw.githubusercontent.com/machiav3lli/OAndBackupX/master/fastlane/metadata/android/en-US/images/phoneScreenshots/12.png" width="170" />
</p>

## building

OAndBackupX is built with gradle. you need the android sdk, rust for building the oab-utils binary, and bash or a compatible shell for executing the oab-utils build script (patches for making this buildable on windows are welcomed).

P.S: If you have any problem building OAB-Utils: you can find some helping notes in its Readme.md

```
./gradlew build
# building only debug
./gradlew assembleDebug
# building for a specific abi target
./gradlew assembleArm64
```

## version control

OAndBackupX is handled on Github:   
https://github.com/machiav3lli/oandbackupx

## busybox / toybox / oab-utils

a working busybox or toybox installation is required at the moment, but work is in progress to include all the needed functionality in a binary included in the apk. this program is called oab-utils and is written in rust.

you can get the source for busybox here: https://busybox.net/. you then need to cross-compile it for the architecture of your device (e.g. armv6). you can also try the binaries found here: https://busybox.net/downloads/binaries/.   
if you have a working toolchain for your target device, you should only need to run the following commands on the busybox source:

```
    make defconfig # makes a config file with the default options
    make menuconfig # brings up an ncurses-based menu for editing the options
        # set the prefix for your toolchain under busybox settings -> build options 
        # (remember the trailing dash, e.g. 'arm-unknown-linux-gnueabihf-')
        # build as a static binary if needed
    make
```

copy the busybox binary to your system, for example /system/xbin or /data/local, and make it executable. symlinking is not necessary for use with oandbackupx. in the oandbackupx preferences, provide the whole path to the busybox binary, including the binary's file name (e.g. /data/local/busybox).

translations of the original OAndBackup are currently being managed on transifex: https://www.transifex.com/projects/p/oandbackup/
so please come help us there or spread the link if you want the app available in your own language.

## special usage notes

* long press an item in the list of apps to get the context menu. 
  * delete backup: deletes the backup files for the chosen app.
  * uninstall: somewhat more aggresive than a normal uninstall. in addition to doing a normal uninstall via android commands (via pm and thereby uninstalling for all users of the device), uninstalling from OAndBackupX deletes files the app might have left over in /data/app-lib/. this is useful, as a normal uninstall via android settings in rare circumstances can leave files there making a reinstall of the same app impossible while they are there.
    this also works on system apps (although this is still somewhat experimental), which are deleted with a normal rm after the system partition has been remounted as read-write. it is afterwards remounted as read-only.
  * enable / disable: uses the android script `pm` to enable or disable an app. disabling an app removes it from the normal user interface without uninstalling. this can be used for enabling or disabling an app for muliple users at a time (if the device has multiple users enabled). users are identified with an id: 0 is the first user (owner).
* multiple users: multi-user is still somewhat experimental but should work. when restoring in a multi-user setting, `pm install -r $apk` gets called and subsequently the app is disabled for every user who has the app listed in /data/system/user/$user/package-restrictions.xml (unless the app is listed as enabled="1").   
  this can create problems for users installing the same app at some later point, but is necessary to prevent the app from being installed to all users at the same time. the context menu has an option to enable or disable apps which can be used if other users become unable to use a specific app due to disabling on restore.   
  enabling and disabling only works after an initial install (not necessarily from oandbackup) or restore of the app.

restoring data can also be done manually from the backup files. OAndBackupX stores the program data files in zip-compressed archives so they can be uncompressed and unpacked with any tool supporting that format (e.g. ```unzip com.machiav3lli.backup.zip```). the unpacked files should then be placed in the directory indicated by "dataDir" in the log file stored with the backup files. this directory will usually be in /data/data/.  
after restoring the files, the user and group id of the package need to be set. therefore data can only be restored for packages where an apk has been installed successfully. uid and gid can be obtained with the ```stat``` program (e.g. ```stat /data/data/com.machiav3lli.backup```) and set with ```chown```. finally, the correct permissions need to be set with ```chmod```. OAndBackupX does this by setting 771 for all data files although this is probably not the best method. the subdirectory lib/ needs to be excluded from both ```chown``` and ```chmod```.  
on android 6 / marshmallow (api 23) you would also need to use the ```restorecon``` command on the data directory (e.g. ```restorecon -R /data/data/com.machiav3lli.backup```) or use another method of restoring the file security contexts.  
the code which does these things are in the methods doRestore and setPermissions of ShellCommands.java.

## licenses

as a fork of OAndBackup, OAndBackupX is licensed under the MIT license (see LICENSE.txt)

openpgp-api-lib is written by Dominik Schürmann and licensed under Apache License, Version 2.0

App's icon is based on an Icon made by [Catalin Fertu](https://www.flaticon.com/authors/catalin-fertu) from [www.flaticon.com](https://www.flaticon.com)

Placeholders Icon made by [Smashicons](https://www.flaticon.com/authors/smashicons) from [www.flaticon.com](https://www.flaticon.com)

## credits

[Jens Stein](https://github.com/jensstein) for his unbelievably valuable work on OAndBackup.

[Rahul Patel](https://github.com/whyorean) whose work on AuroraStore and Design inspired this design.

Open-Source libs: [ButterKnife](https://github.com/JakeWharton/butterknife), [FastAdapter](https://github.com/mikepenz/FastAdapter)

## author

Antonios Hazim
