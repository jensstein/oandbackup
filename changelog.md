changelog
=========

0.2.10 (2015-01-27)
-------------------
 * encryption is now implemented through the openpgp api library
 * handle different display densities when showing application icons
 * also restore compiled libraries when restoring system packages
 * package data in the external files directory is now backed up
 * the error log is now placed in the backup directory and the option to set a custom path for it has been removed. if the logfile already exists, it will be moved to the new destination.
 * bluetooth directory is added to the special backups on older versions of android
 * added japanese translation. thanks to Naofumi
 * added norwegian bokmål translation. thanks to Daniel
 * fixed bug where the data limit on ipc transfers where exceeded on api 9
 * fixed bug that caused crash when search button was pushed before package list was loaded
 * fixed bug that caused a crash when getting application icons on android 5. and thanks a lot to أحمد المحمودي (Ahmed El-Mahmoudy) for additional handling of icons on android 5
 * fixed out of memory bug when the received application icon was too big. thanks a lot to Daniel / https://github.com/DaPa for this

0.2.9 (2014-07-08)
-------------------
 * more robust method of getting uid and gid of a package
 * count the day a scheduled backup was set when calculating when it should run
 * added option to disable notifications
 * added (experimental) support for special backup - accounts, widgets, etc.
 * added file browser for choosing backup directory
 * added check for sanity of busybox path when it is set in the preferences
 * added french translation. thanks to gwenhael.
 * better handling of symlinks when backing up
 * better handling of configuration changes

0.2.8 (2014-03-08)
-------------------
 * context menu now has a share option - the backup files for data and apk can be shared here
 * backup directory can now be in oandbackup's own data directory (/data/data/dk.jens.backup) - sharing does not yet work when the backup files are here
 * icons in app list
 * better filtering
   * filtering is now shared and consistent between the different modes (activities)
   * added option to remember filtering and sorting
 * better feedback when deleting backups of uninstalled apps
 * added italian translation. thanks to Marco Bonifacio (mbonix).
 * added serbian translation. thanks to operationDIE.
 * added spanish translation. thanks to isaacluz.
 * fixed bug where compressed data was deleted if an apk-only backup was made after backing up data
 * fixed bug where restores failed for packages which have packagenames and data paths which differ (mainly the core android package, android system)

0.2.7 (2014-01-13)
-------------------
 * backups can now be specified to include either apk, data or both
 * scheduled backups of new and updated apps can now be specified to exclude system apps
 * added austrian german translation. thanks to user_99_gmx.at
 * added an activity for various tasks (rebooting userspace and deleting backups)
 * fixed bug where a fifo special file blocked compression. thanks to lisandro for reporting and testing.
 * fixed restoring system apps for android 4.4

0.2.6 (2013-12-11)
-------------------
 * added option to change language
 * display errors in ui
 * scheduled backups can have custom lists
 * added function to reboot user space quickly
 * fix restore of system apps
 * system apps don't need rebooting after a restore now (at least on android 4.2)
 * fix zip creation of /data/system/ on newer apis

0.2.5 (2013-11-05)
------------------
 * (unlimited) multiple schedules are now supported
 * added german translation. thanks to lightonflux
 * new option to filter older backups
 * new option to copy own apk to top level of backup directory
 * searching now works for older versions of android too
 * fixed issue where clicking a notification while a message was displayed would lead to windowleaked
 * fixed sorting bug where sorting state was forgotten after restoring an app
 * fixed bug where output of pm install wasn't read properly on android versions 2.3.3 - 2.3.7
 * fixed crash in preferences when blanking out old backups

0.2.4 (2013-09-30)
------------------
 * fixed uninstalling and restoring system apps
 * fixed missing error handling on folder creation
 * fixed issue with path to backup folder on 4.3 (/storage/emulated/$userid/ not visible to root)
 * better message feedback on errors (notifications)
 * beginning work on handling multiple users
 * long press / context menu now has option to enable or disable app
 * new sorting option for new and updated apps

0.2.3 (2013-09-13)
------------------
 * added portuguese translation. thanks to Sérgio Marques (smarquespt).
 * new icon. thanks to Anil Gulecha.
 * beginning fix of multi-user issue
 * fix bug where having no backup of an app would result in an indexoutofboundsexception.
 * reverting back to cp instead of rsync
 * batch mode now has separate sorting and filtering
 
0.2.2 (2013-09-10)
-------------------
 * compression has changed from gzip to zip (please redo your backups - any apps no longer installed having backups in tar format should manually be recompressed to zip format. support for uncompressing tar will be deprecated in the near future.) 
    * if the data folder of the app doesn't contain any files (or only empty directories and the lib/ folder) no zip will be created since java.util.zip can't handle empty zip files
 * option for rsync output disabled due to deadlocking on apps with a high number of files
 * batch operations now has sorting and filtering too
 
0.2.1 (2013-09-03)
------------------
 * fixing bug where busybox tar is not compiled with support for gzip

0.2.0 (2013-08-30)
--------------------
 * compression of app data backup (tar + gzip)
 * new logfile format. as the old logs are converted on the first run after updating to this version, please notice that it will take a little longer to start up (on one of my devices each log is written in approximately half a second). all timestamps for last backup will be reset in the app interface due to changes to a more flexible reading method but the original values will remain as strings in the logs (look for "lastBackup") until they are overwritten by a new backup, so if the date displayed in the app is important you can change to value of "lastBackupMillis" (milliseconds since unix epoch for desired date). this is unfortunately not possible to do automatically due to the unpredictability of various date formats.
 * bug fixes:
    * refresh while searching
    * sorting 
    * searching after restoring with apk or uninstalling
    * chown bug if uid less than five characters
    * chown and chmod bug where certain packages were not recognised by the system after restore due to the lib folder  

0.1.14 (2013-08-04)
--------------------
 * option to acquire wakelock. new permission: android.permission.WAKE_LOCK
 * started scheduling
 * new permission: RECEIVE_BOOT_COMPLETED to have schedules persist across reboots
 * smarter rsync arguments
 * now killing apps before restore on all platforms (needed when rsync is used for restore instead of cp)
 * started preferences: custom paths for backupfolder, logfile, rsync binary and busybox binary
 * detect changes made from batchactivity
 
0.1.13.1 (2013-07-23)
--------------------
 * fixed folder-naming issue
 * calling busybox in a simpler way
 
0.1.13 (2013-07-18)
------------------
 * change of application name
 * batchactivity is now a listview too
 * custom adapter and custom class for gathered application data
 * killing package before restore on pre-ics devices

0.1.12 (2013-07-13)
------------------
 * initial release
