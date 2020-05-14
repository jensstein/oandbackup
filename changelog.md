changelog
=========

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
 

