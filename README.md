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
 * multiple users: multi-user is still somewhat experimental but should work. when restoring in a multi-user setting, `pm install -r $apk` gets called and subsequently the app is disabled for all users except the owner and the current user. this workaround is necessary as i haven't found a way programatically to discover which users already have the app installed.

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
