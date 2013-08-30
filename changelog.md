changelog
=========

0.1.15 (development)
--------------------
 * compression of app data backup (tar + gzip)
 * new logfile format. as the old logs are converted on the first run after updating to this version, please notice that it will take a little longer to start up (on one of my devices each log is written in approximately half a second). all timestamps for last backup will be reset in the app interface due to changes to a more flexible reading method but the original values will remain as strings in the logs (look for "lastBackup") until they are overwritten by a new backup, so if the date displayed in the app is important you can change to value of "lastBackupMillis" (milliseconds since unix epoch for desired date). this is unfortunately not possible to do automatically due to the unpredictability of various date formats.
 * bug fixes:
    * refresh while searching
    * sorting 
    * searching after a restoring with apk or uninstalling
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