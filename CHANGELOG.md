CHANGELOG
=========

6.0.1 (03.06.2021)
------------------
* Fixed: Exclude system apps checkbox in the schedule sheet
* Fixed: Import the val of exclude system apps on schedules' import
* Added: Vietnamese, Hindi & Ukranian
* Updated translations:Arabic, Spanish, Italian, Portuguese, Russian, Catalan, Turkish, Indonesian

6.0.0 (05.05.2021)
------------------
CLEAR DATA NEEDED

* Added: Encrypt private preferences including encryption password (using Android keystore)
* Added: Export/import of schedules
* Added: Global blocklist applies to Main
* Added: Ascending/descending sort support
* Added: Warning text to backup directory picker
* Added: Direct share button for logs
* Added: Better error handling
* Updated: Make backup dialog checks response to the service's active options
* Updated: Use modes' checkboxes in the backup/restore/schedule dialogs
* Updated: Schedule action on first day if possible
* Updated: Replace Blacklist with Blocklist
* Fixed: Don't schedule when disabled
* Fixed: Crash when scheduled list of apps is empty
* Fixed: Schedules not firing on time
* Fixed: Delayed notifications of some restored apps (exclude the push notifications' ID from backups)
* Fixed: Backup instance folder left untouched after backup failing
* Fixed: Hiding device lock option when there's no lock
* Added: Catalan language
* Updated translation: Arabic, Spanish, Indonesian, Polish, Russian, Japanese, Chinese, Dutch, French

5.1.0 (18.03.2021)
------------------

* Added: Tool to save apps' list to a file
* Added: Option to use device credentials as lock
* Added: Names to schedules
* Added: Force kill and launch to App Sheet
* Added: Updated apps bar
* Added: Snackbar on single Backup/Restore actions
* Added: Warning text to enable special backups preference
* Updated: UI/UX revamp (in co-op with @opepp)
* Updated: STOP-CONT default is on
* Fixed: Scheduling backups
* Fixed: Log items sdk release name
* Fixed: Crash on batch actions with nothing checked
* Updated Translations: Norwegian, German, Chinese, Spanish, Polish, Indonesian, Malayalam, French, Turkish, Arabic
* A ton of other small fixes

5.0.2 (24.02.2021)
------------------

* Fixed: Message disply on error (@hg42)
* Fixed: Quote issue in backup process (@hg42)

5.0.1 (13.02.2021)
------------------

* Updated: Improving shell commands function (@hg42)
* Fixed: Correct SELinux context (@jakeler & @hg42)
* Fixed: Missing files for some special backups (@hg42)
* Fixed: Throwing exception on sockets and other special files (@hg42)
* Fixed: Failed backups with files with names with double spaces (@jakeler)
* Fixed: STOP-CONT log messages (@hg42)
* Updated: Gradle & Dependencies
* Updated translations: Polish, Portuguese, Chinese, Greek, German, French, Norwegian, Dutch, Spanish, Italian, Indonesian, Japanese, Russian
* Clean up

5.0.0 (03.01.2021)
------------------

MIRGATED TO KOTLIN, NEW ENCRYPTION ALGORITHM: clean install & doesn't support restore of previous encrypted backups.
SCHEDULED ACTIONS CAN ONLY LAST 10 MINUTES (SYSTEM CONSTRAINT): so partition your group of apps. will be fixed in next minor releases.

* Added: Individual modes for apps in batch action dialog
* Added: Launchable apps filter
* Added: Ability to install test builds
* Added: Option to restore with all asked permissions
* Added: Option to allow downgrading apps
* Added: Progress indicator for batch actions
* Added: Filter black list based on schedule's filter
* Added: Refresh after scheduled actions or changing backup folder (no visual indicator)
* Added: Copy app's own APK to tools
* Added: Stats to sort/filter sheet
* Added: Blacklists to scheduled actions
* Added: Shortcut to known issues in Help
* Added: Notification on missing storage permission of scheduled backups
* Added: Separate Option to backup obb files
* Updated: Multi-threading batch & scheduled actions (big improvement to performance)
* Updated: Encryption algorithm and parameters
* Updated: Log the errors on most actions
* Updated: New log viewer system
* Updated: Choosing a backup directory doesn't create a child folder anymore
* Updated: Migrate from Java to Kotlin (99.99%)
* Updated: Far less unneeded refreshing
* Updated: Schedules mode/sub-mode to filter/mode
* Updated: Standardize modes and filters variables
* Updated: Scheduling from Alarm-based to Job-based
* Updated: Migrated to Gradle Kotlinscript
* Updated: Batch & scheduled actions to WorkManager-based
* Updated: Don't use cmd package anymore
* Updated: Migrate from Log to Timber
* Updated: Turn some utils and handlers classes to objects
* Updated: Integrate Backup- & RestoreFragment in BatchFragment
* Updated: Migrate custom list to DialogFragment
* Updated: Color the shortcuts icons
* Updated: Migrate blacklist DB to Room
* Updated: Using ViewModel to manage data in Main and Scheduler
* Updated: Batch items reflect their backup mode
* Updated: Show Blacklist's & Customlist's apps with labels (@hg42)
* Updated: Format of the time left for scheduled backups
* Updated: Return special backups setting to advanced
* Updated: Set default disable usb verification to true
* Updated: Improve the details in start schedule now dialog
* Updated: Safer backup history initialization (@hg42)
* Fixed: APK/data not found error
* Fixed: Persistant storage permission
* Fixed: Ability to query for all apps (on A11)
* Fixed: Asking for permission to manage all files (on A11)
* Fixed: Backup directory being asked again after a reboot
* Fixed: Crashing on picking up new backup directory from settings
* Fixed: Enable/disable apps
* Fixed: Running scheduled backup with empty list gives an error
* Fixed: Backup items restoring the latest backup not the chosen one
* Fixed: Crash when deleting an app that has no backups
* Fixed: Ghost clicking when the app is long in the background
* Fixed: Not able to read the full text of error notifications
* Fixed: AppInfo's latestBackup & isUpdated
* Fixed: RememberFiltering option
* Fixed: Length of the time left string
* Fixed: Updated tag not updating
* Fixed: Scheduling repeated actions
* Fixed: Try to read logs as backups
* Fixed: CheckApk,-Data & -All behavior
* Fixed: Blacklist saving wrong selected apps
* Fixed: AppInfo constructor missing AppMetaInfo
* Fixed: Exclude system apps not showing correctly
* Fixed: Crashing on delete all backups
* Fixed: Restore of apps with device-protected data (@fantostisch)
* Fixed: Exception hardening (@hg42)
* Removed: Copying app's own apk on backup actions
* Removed: Quick reboot tool
* Removed: Toasts on batch actions
* Removed: Usage of Wakelocks
* Removed: HandleMessages
* Updated Translations: Estonian, Indonesian, Hindi, Greek, Italian, Norwegian, Russian, Chinese, German, French, Spanish, Dutch, Arabic, Polish, Portuguese

4.0.0 (02.11.2020)
------------------

* Added: Allow parallel batch tasks
* Added: Disable verification of apps over USB before installing and re-enable it afterwards
* Added: APK & Data checkboxes in Batch
* Added: Storage Access Framework support (Credits @tiefkuehlpizze)
* Added: Symlink/Pipe Support (Credits @hg42 and @tiefkuehlpizze)
* Added: Multi Backup support (Credits @tiefkuehlpizze)
* Added: HelpSheet to deliver help easily
* Added: More clear messages informing user about running processes
* Added: Arabic, Bosnian and Malayalam
* Updated: Tags system
* Updated: Use STOP/CONT to make sure no background processes ruin the backup (Credits @hg42 & @tiefkuehlpizze)
* Updated: Restructure Schedules
* Updated: Backups directory is /OABackupX now
* Updated: Backup Structure (Credits @tiefkuehlpizze)
* Updated: Whole new layouts for UI elements
* Updated: Encryption warning leads directly to its preferences
* Updated: Translations
* Fixed: Log viewer
* Fixed: Create Log when errors occur on backups
* Removed: Annimon stream dependency
* Too many other improvements, fixes and cleanups

3.2.0 (23.09.2020)
------------------

* Added: new Navigation
* Added: badge for updated apps in Main
* Fixed: save checked items in Batch
* Fixed: sync of check all in Batch
* Fixed: search filtering unexpected behavior(caused by caching)
* Fixed: resume behavior
* Updated: improve refresh performance
* Updated: improve on visual performance (Action instances and Sheets launch speed)
* Updated: Translations (Norwegian, Dutch, Turkish, Chinese, Portuguese)

3.1.1 (11.09.2020)
------------------

* Added: the Intro
* Added: encryption password check field
* Removed: Busybox path preference
* Fixed: showing the present date instead of the last backup
* Fixed: not updating the items' update tag after new backup
* Updated: Translations (Norwegian, Russian, Italian, French, Dutch, German)
* Updated: set remember sort/filter default to true
* Updated: set biometric lock default to off
* Updated: hide log viewer

3.1.0 (25.08.2020)
-------------------

* Removed: dropped Android Nougat support
* Added: new tags system
* Added: biometric lock preference
* Added: persistent checked items in Batch
* Updated: the whole UI
* Updated: change schedule custom app list to show labels instead of package names
* Updated: reduce repeatability of encryption AlertDialog
* Updated: set default external data and obb preference to off
* Updated: show only the Date of latest backup in Main
* Fixed: crash on launching batch fast
* Fixed: crashing when tapping outside of ProgressDialog in Main
* Refactored the code improving quality and performance
* Moved to Affero GPL v3

3.0.0 (04.08.2020)
-------------------

**THIS RELEASE ISN'T COMPATIBLE WITH THE VERSIONS BEFORE**

* Changed: default backup directory to /OABX
* Rewritten Encryption Solution (Credits @Pizze)
* Added: sort by Data Size
* Added: Alert Dialog about enabling Encrylption
* Updated: revamped Help UI
* Added: tooltips for all Buttons' Icons
* Updated: AppSheet adaptive UI
* Added: wipe Cache button to AppSheet
* Changed: clearCache preference to excludeCache
* Fixed: edge cases for all Adapters
* Fixed: fluid Checked in Batch
* Fixed: not connected Prefs
* Fixed: some Filters not working
* Update: rewritten the whole shell commands functions (Credits @Pizze)
* Updated: German (Credits @elea11)
* a lot of performance and UI tweaks

2.0.0 (06.07.2020)
-------------------

* updated: switched to sulib (Credits @Tiefkuehlpizze)
* removed oab-utils for good (Credits @Tiefkuehlpizze)
* update: new UI elments for App-, Filter- & ScheduleSheet
* fixed scheduled backups for data(alone)
* fixed zipping external/deData/obb according to Prefs
* fixed: zipping empty external/deData/obb folders
* updated: clean cache function
* some other UI and Performance tweaks

1.3.0 (25.06.2020)
-------------------

* updated: new Filters System
* updated: reduced Splash waiting time
* added: Split-APK support (needs more testing, Credits @Tiefkuehlpizze)
* updated: Search supports Package Name
* fixed: cache not found/ Symlinks errors
* added: OBB support(needs more testing)
* added languages: Italian, Chinese, Turkish, Polish (Credits @Urnyx05 @tuchit @scrubjay55 @Antyradek)
* other: UI and performance tweaks

1.2.0 (10.06.2020)
-------------------

* updated: UI
* fixed: share crashing
* added: options to backup Device Protected Data and External Data

1.1.1 (04.06.2020)
-------------------

* added: Exodus report shortcut to AppSheet
* fixed: not launching if there's an app with icon dimensions <= 0
* changed: default is set now to copy OAndBackupX to parent backup folder
* added: Greek (Credits @tsiflimagas)
* update: Capitalisation and English Strings (Credits: @atrate)
* update: generalize Chinese, Dutch, Polish and Swedish

1.1.0 (29.05.2020)
-------------------

* added: data backup encryption
* fixed: schedules
* updated: notifications' format
* tweaked some UI elements
* and improved performance

1.0.2 (24.05.2020)
-------------------

* added: Item click feedback
* updated: scrolling eXperience
* fixed: Splash screen
* fixed: swipe to refresh behavior
* fixed: Statusbar theme
* and some other UI tweaks

1.0.0 (18.05.2020)
-------------------

* added: option to clean cache before data backup

0.9.14 (14.05.2020)
-------------------

* reverted: back to cleaning cache

0.9.13 (14.05.2020)
-------------------

* reverted: back to cp (rsync isn't bundled with all ROMs)

0.9.12 (14.05.2020)
-------------------

* added: app's info shortcut in App Sheet
* updated: punch of UI elements
* updated: new default backup directory /OAndBackupX
* fixed: behaviour of adapter after doing actions
* fixed: adapter's crashing
* switched: from cleaning app's cache to excluding it from backup(Credit @icewind1991)
* tweaked: behaviour of different UI elements

0.9.11 (11.05.2020)
-------------------

* added: clear cache before backing up
* updated: chips styling
* added: prompt for battery optimization exclusion
* fixed: the profiles bug (needs testing)
* switched: from FilePicker to SAF
* tweaked up the code

0.9.10 (07.05.2020)
-------------------

* rewritten: Scheduler (need to be tested extensively)
* fixed: links in help
* clean up the code
* UI tweaks here and there

0.9.9 (06.05.2020)
-------------------

* removed: check for Update (preperaing for F-Droid Release)

0.9.8 (01.05.2020)
-------------------

* fixed: remember filter
* fixed: picking the value for old backups
* fixed: empty filtered list causing crash
* tweaked: Scheduler UI
* small other tweaks

0.9.7 (27.04.2020)
-------------------

* fixed: update logic
* removed: PGP encryption and follow symbolic links
* cleaned up

0.9.6 (25.04.2020)
-------------------

* added: check for Update
* fixed: theme consistency
* fixed: some strings

0.9.5 (23.04.2020)
-------------------

* fixed refresh issue after restore and backup from AppSheet
* made the UI more simple
* fixed press back on Main
* some other small tweaks here and there

0.9.4 (20.04.2020)
-------------------

* added support for protected data backup
* switched to local date format for apps in Main
* some other small changes

0.9.3 (18.04.2020)
-------------------

* added Theme's settings: Dark/Light/System
* fixed Settings padding
* other small tweaks

0.9.2 (13.04.2020)
-------------------

* added fast scroll bars to Main & Batch
* converted the icons .png to .xml

0.9.1 (10.04.2020)
-------------------

* fixed the reported Oab-utils-test error

0.9.0 (09.04.2020)
-------------------

* adapted FastAdapter for Main and Batch
* rewritten Main and Batch
* added AppSheet for apps
* added Intro to handle permissions
* restructured the app
* UI tweaks all over the app
* Androidx-ify the Dialogs
* rewritten some Handlers
* clean up job
* shot the encryption down for now
* other small tweaks

0.2.7 (31.03.2020)
-------------------

* optimized sort/filter chips' visual behavior
* other small UI tweaks

0.2.6 (29.03.2020)
-------------------

* rewritten Preferences fully
* replaced the file browser with a more initiative one
* fixed toolbar issue for Scheduler and Preferences
* integrated Help and Tools in the Preferences

0.1.3 (27.03.2020)
-------------------

* new UI for Main, Batch and Scheduler
* fixed: missing "both" as description of backed up mode

0.1.2 (27.03.2020)
-------------------

* new Main UI
* fixed refresh in Main

0.1.1 (25.03.2020)
-------------------

* initial X release
* restructured and cleaned the app: adding Bottombar-based navigation and Sort and Filter FAB(for now: only the basic filters)
* rewritten Batch-(Activity, Adapter and Sorter) and most the layouts
* partially rewritten Main-(Activity and Sorter)
* completed the German translation
* had to shut the preferences out for now: to solve the compatibility issues, I have to fully restructure and rewrite the preferences and that'll be one of the next steps(fix priority high).
* other drawback: setting a toolbar on some activities(scheduler, help and tools) is causing a crash, so they have to do with no toolbar for now(fix priority minimal).
