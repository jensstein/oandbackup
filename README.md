oandbackup
=======
[![pipeline status](https://gitlab.com/jensstein/oandbackup/badges/master/pipeline.svg)](https://gitlab.com/jensstein/oandbackup/commits/master)

a backup program for android. requires root and allows you to backup individual apps and their data.
both backup and restore of individual programs one at a time and batch backup and restore of multiple programs are supported (with silent / unattended restores).  
restoring system apps should be possible without requiring a reboot afterwards. oandbackup is also able to uninstall system apps. handling system apps in this way depends on whether /system/ can be remounted as writeable though, so this will probably not work for all devices (e.g. htc devices with the security flag on).  
backups can be scheduled with no limit on the number of individual schedules and there is the possibility of creating custom lists from the list of installed apps.

building
========
oandbackup is built with gradle. you need the android sdk, rust for building the oab-utils binary, and bash or a compatible shell for executing the oab-utils build script (patches for making this buildable on windows are welcomed).
```
./gradlew build
# building only debug
./gradlew assembleDebug
# building for a specific abi target
./gradlew assembleArm64
```

version control
==============
oandbackup is handled on both gitlab and github:   
https://gitlab.com/jensstein/oandbackup/   
https://github.com/jensstein/oandbackup   
debug apks are built by gitlab for each commit on every branch. the latest successful build can be found here (substitute $branch for the desired branch, e.g. master):   
https://gitlab.com/jensstein/oandbackup/-/jobs/artifacts/$branch/browse/apks?job=build  
(e.g. https://gitlab.com/jensstein/oandbackup/-/jobs/artifacts/master/browse/apks?job=build)  
and signed release apks are built for every commit on the master branch:  
https://gitlab.com/jensstein/oandbackup/-/jobs/artifacts/master/browse?job=sign

busybox / toybox / oab-utils
======

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
copy the busybox binary to your system, for example /system/xbin or /data/local, and make it executable. symlinking is not necessary for use with oandbackup. in the oandbackup preferences, provide the whole path to the busybox binary, including the binary's file name (e.g. /data/local/busybox).

an apk build of oandbackup is available on f-droid's servers: https://f-droid.org/repository/browse/?fdid=dk.jens.backup

translations are currently being managed on transifex: https://www.transifex.com/projects/p/oandbackup/
so please come help us there or spread the link if you want the app available in your own language.

if you have any questions, critique, bug reports or suggestions, please write me an email: j.stn.oab@gmail.com

Cryptography
============
oandbackup supports encrypting the backups using an external cryptography provider.
First you need to install an app which implements the openpgp-api, e.g. OpenKeychain: https://www.openkeychain.org/, and set up an identity.
The "Cryptography" section of the preferences of oandbackup is then enabled and here you can choose which openpgp and identity to use.

special usage notes
===========
 * long press an item in the list of apps to get the context menu. 
   * delete backup: deletes the backup files for the chosen app.
   * uninstall: somewhat more aggresive than a normal uninstall. in addition to doing a normal uninstall via android commands (via pm and thereby uninstalling for all users of the device), uninstalling from oandbackup deletes files the app might have left over in /data/app-lib/. this is useful, as a normal uninstall via android settings in rare circumstances can leave files there making a reinstall of the same app impossible while they are there.
   this also works on system apps (although this is still somewhat experimental), which are deleted with a normal rm after the system partition has been remounted as read-write. it is afterwards remounted as read-only.
   * enable / disable: uses the android script `pm` to enable or disable an app. disabling an app removes it from the normal user interface without uninstalling. this can be used for enabling or disabling an app for muliple users at a time (if the device has multiple users enabled). users are identified with an id: 0 is the first user (owner).
 * multiple users: multi-user is still somewhat experimental but should work. when restoring in a multi-user setting, `pm install -r $apk` gets called and subsequently the app is disabled for every user who has the app listed in /data/system/user/$user/package-restrictions.xml (unless the app is listed as enabled="1").   
this can create problems for users installing the same app at some later point, but is necessary to prevent the app from being installed to all users at the same time. the context menu has an option to enable or disable apps which can be used if other users become unable to use a specific app due to disabling on restore.   
enabling and disabling only works after an initial install (not necessarily from oandbackup) or restore of the app.

restoring data can also be done manually from the backup files. oandbackup stores the program data files in zip-compressed archives so they can be uncompressed and unpacked with any tool supporting that format (e.g. ```unzip dk.jens.backup.zip```). the unpacked files should then be placed in the directory indicated by "dataDir" in the log file stored with the backup files. this directory will usually be in /data/data/.  
after restoring the files, the user and group id of the package need to be set. therefore data can only be restored for packages where an apk has been installed successfully. uid and gid can be obtained with the ```stat``` program (e.g. ```stat /data/data/dk.jens.backup```) and set with ```chown```. finally, the correct permissions need to be set with ```chmod```. oandbackup does this by setting 771 for all data files although this is probably not the best method. the subdirectory lib/ needs to be excluded from both ```chown``` and ```chmod```.  
on android 6 / marshmallow (api 23) you would also need to use the ```restorecon``` command on the data directory (e.g. ```restorecon -R /data/data/dk.jens.backup```) or use another method of restoring the file security contexts.  
the code which does these things are in the methods doRestore and setPermissions of ShellCommands.java.

licenses
=======
oandbackup is licensed under the MIT license (see LICENSE.txt)

android-support-v4 is written by The Android Open Source Project and licensed under the Apache License, Version 2.0 (see NOTICE.txt in the libs directory)

openpgp-api-lib is written by Dominik Schürmann and licensed under Apache License, Version 2.0

author
======
jens stein
