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

licenses
=======
oandbackup is licensed under the MIT license (see LICENSE.txt)

android-support-v4 is written by The Android Open Source Project and licensed under the Apache License, Version 2.0 (see NOTICE.txt in the libs folder)

building
========
to compile you just need the android sdk and apache ant:
```
    $path_to_sdk/tools/android update project --t $target_number --p $path_to_this_project
    cd $path_to_this_project
    ant debug 
```