oandbackup
=======
a backup program for android. requires root and allows you to backup individual apps and their data.

a working busybox installation is required at the moment.   
a busybox executable can be obtained from various sources. here are instructions for building it from source: http://mobisocial.stanford.edu/news/2011/02/compile-busybox-on-android-os/   

an apk build of oandbackup is available on f-droid's servers: https://f-droid.org/repository/browse/?fdid=dk.jens.backup

if you have any questions, critique, bug reports or suggestions, please write me an email: j.stn.oab@gmail.com 

author
======
jens stein

special usage notes
===========
 * long press an item in the list of apps to get the context menu. here there is an option to uninstall the app which is somewhat more aggresive than a normal uninstall. in addition to doing a normal uninstall via android commands, uninstalling from oandbackup deletes files the app might have left over in /data/app-lib/. this is useful, as a normal uninstall via android settings in rare circumstances can leave files there making a reinstall of the same app impossible while they are there.
 * multiple users: multi-user is still somewhat experimental but should work. when restoring in a multi-user setting, `pm install -r $apk` gets called and subsequently the app is disabled for every user who has the app listed in /data/system/user/$user/package-restrictions.xml (unless the app is listed as enabled="1"). 

this can create problems for users installing the same app at some later point, but is necessary to prevent the app from being installed to all users at the same time. the context menu has an option to enable or disable apps which can be used if other users become unable to user a specific app due to disabling on restore. 

enabling and disabling only works after an initial install (not necessarily from oandbackup) or restore of the app.

building
========
to compile you just need the android sdk and apache ant:
```
    $path_to_sdk/tools/android update project --t $target_number --p $path_to_this_project
    cd $path_to_this_project
    ant debug 
```

licenses
=======
oandbackup is licensed under the MIT license (see LICENSE.txt)

android-support-v4 is written by The Android Open Source Project and licensed under the Apache License, Version 2.0 (see NOTICE.txt in the libs folder)
