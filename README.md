oandbackup
=======
a backup program for android. requires root and allows you to backup individual apps and their data.
both backup and restore of individual programs one at a time and batch backup and restore of multiple programs are supported (with silent / unattended restores).  
restoring system apps should be possible without requiring a reboot afterwards. oandbackup is also able to uninstall system apps. handling system apps in this way depends on whether /system/ can be remounted as writeable though, so this will probably not work for all devices (e.g. htc devices with the security flag on).  
backups can be scheduled with no limit on the number of individual schedules and there is the possibility of creating custom lists from the list of installed apps.

a working busybox installation is required at the moment.   
you can get the source for busybox here: http://busybox.net/. you then need to cross-compile it for the architecture of your device (e.g. armv6). you can also try the binaries found here: http://busybox.net/downloads/binaries/latest/.   
if you have a working toolchain for your target device, you should only need to run the following commands on the busybox source:
```
    make defconfig # makes a config file with the default options
    make menuconfig # brings up an ncurses-based menu for editing the options
        # set the prefix for your toolchain under busybox settings -> build options 
        # (remember the trailing dash, e.g. 'arm-unknown-linux-gnueabihf-')
        # build as a static binary if needed
    make
```
copy the busybox binary to your system, for example /system/xbin or /data/local, and make it executable. symlinking is not necessary for use with oandbackup.

an apk build of oandbackup is available on f-droid's servers: https://f-droid.org/repository/browse/?fdid=dk.jens.backup

translations are currently being managed on transifex: https://www.transifex.com/projects/p/oandbackup/
so please come help us there or spread the link if you want the app available in your own language.

if you have any questions, critique, bug reports or suggestions, please write me an email: j.stn.oab@gmail.com 

author
======
jens stein

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

building
========
to compile you just need the android sdk and apache ant:
```
    cd $path_to_this_project
    git submodule update --init
    # or obtain the code for openpgp-api-lib in some other way and place it in the libs directory
    $path_to_sdk/tools/android update project -t $target_number -p . --library libs/openpgp-api-lib
    ant debug
```

licenses
=======
oandbackup is licensed under the MIT license (see LICENSE.txt)

android-support-v4 is written by The Android Open Source Project and licensed under the Apache License, Version 2.0 (see NOTICE.txt in the libs directory)

openpgp-api-lib is written by Dominik Schürmann and licensed under Apache License, Version 2.0
