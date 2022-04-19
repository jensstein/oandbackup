# Frequently Asked Questions:

* [What is Neo-Backup?](#what-is-neo-backup)
* [What is OAndBackupX?](#what-is-oandbackupx)
* [Which Android Versions are supported?](#which-android-versions-are-supported)
* [How do I use NB?](#how-do-i-use-nb)
* [What are all these backup-parts (icons)? / Which parts does a backup of an app consist of?](#what-are-all-these-backup-parts-icons--which-parts-does-a-backup-of-an-app-consist-of)
* [What are Special Backups?](#what-are-special-backups)
* [Do I need a rooted phone?](#do-i-need-a-rooted-phone)
* [What is root access used for?](#what-is-root-access-used-for)
* [Why is NB so slow?](#why-is-nb-so-slow)
* [So why use SAF then?](#so-why-use-saf-then)
* [I do not see any apps in the list. What can be the reason?](#i-do-not-see-any-apps-in-the-list-what-can-be-the-reason)
* [I do not see the app which is currently backed up in the notification during batch or scheduled backups.](#i-do-not-see-the-app-which-is-currently-backed-up-in-the-notification-during-batch-or-scheduled-backups)
* [At restore the data directory of the app does not exist.](#at-restore-the-data-directory-of-the-app-does-not-exist)
* [How can I backup SMS &amp; Call log?](#how-can-i-backup-sms--call-log)
* [Are you going to support older Android versions?](#are-you-going-to-support-older-android-versions)
* [Why do I have to login/register to app x y z again after restore?](#why-do-i-have-to-loginregister-to-app-x-y-z-again-after-restore)
* [Why is it not recommended to backup system apps?](#why-is-it-not-recommended-to-backup-system-apps)
* [Is there a roadmap / an overview of features planned to be implemented?](#is-there-a-roadmap--an-overview-of-features-planned-to-be-implemented)
* [What is the difference to implementations like Seedvault?](#what-is-the-difference-to-implementations-like-seedvault)
* [What is the difference to the famous Titanium Backup?](#what-is-the-difference-to-the-famous-titanium-backup)
* [How can I open encrypted backups on my computer?](#how-can-i-open-encrypted-backups-on-my-computer)
* [What does the notification of schedules and batch jobs tell me?](#what-does-the-notification-of-schedules-and-batch-jobs-tell-me)
* [Does NB support multi-user setups / work-profile?](#does-nb-support-multi-user-setups--work-profile) 

#### What is Neo-Backup?

Neo-Backup (short NB) is a fork of [OAndBackup](https://gitlab.com/jensstein/oandbackup) (which is inactive) with the aim to keep such a great and useful FOSS backup and recovery tool alive beyond 202x. 

Most of the functionality and UI of the app was rewritten und now it shoud get more stable. It is also a goal to add features which could ease the backup/restore workflow with any device.

NB requires root and allows you to backup individual apps and their data. Both backup and restore of individual programs one at a time, batch backup and restore of multiple programs, as well as scheduled backups are supported.

Neo-Backup is part of the NeoApplications: https://github.com/NeoApplications

#### What is OAndBackupX?

OAndBackupX (short OABX) was the former name of the project. You may find some reference in here or in the in-app usage notes, we missed to change yet. The initial OABX release was in March 2020. The rename to NB took place more or less exactly at the second birthday of the project (between version 7 and 8), so stable version 7.0.0 still has the name OAndBackupX.

#### Which Android Versions are supported?

Oldest supported version: Android  8 - "Oreo" </br>
Newest supported version: Android 12 - "Snow Cone"

See also - [Are you going to support older Android versions?](#are-you-going-to-support-older-android-versions)

#### How do I use NB?

The first start will guide you through the most important preferences. It still make sense to shortly check the prefs, to see what else can bet set. E.g. you can define the amount of kept backup revisions or decide which [app parts](#what-are-all-these-backup-parts-icons--which-parts-does-a-backup-of-an-app-consist-of) should be included.

There are 4 screens/tabs:<br/>
(navigate between them using the icons of the menu-bar on the bottom of the screen)

1. Main (AppList based on the current filter + access to AppSheet of each app)
2. Batch - Backup
3. Batch - Restore
4. Scheduled backups

The fifth and very right icon of the menu-bar will open the preferences.
A safe way to start is to do some backup and restore tests of an uncritical app via AppSheet.
Go forward and backup multiple apps via Batch and finally define schedules e.g. with Custom lists.

In case of any problems report it in [the Telegram or Matrix group](https://github.com/NeoApplications/Neo-Backup#community) first (sometimes there is an easy solution) and/or [raise an issue here in github](https://github.com/NeoApplications/Neo-Backup/issues).

#### What are all these backup-parts (icons)? / Which parts does a backup of an app consist of?

Each backup basically consists of the two different parts:

1. the software itself (stored in a so called **APK** file / some bigger apps have multiple - so called split APK files) and

2. its **data** (created while using an app, settings, etc.)
   The data can be split again into several data types:

   2.1. ***"normal" data*** 
   
      <details><summary>Show details ...</summary>
   
        - Stored usually in /data/data
        - Default is set to include it in the backup
   
      </details>     
   
   2.2. ***external*** data

      <details><summary>Show details ...</summary>

       - Stored usually in /Android/data/ in the external storage (internal storage in android current terminology)
       - Default is set to not include it in the backup (possibility to enable in prefs)

      </details>        
   
   2.3. ***obb*** files

      <details><summary>Show details ...</summary>
   
        - Stored usually in /Android/obb/ in the external storage (internal storage in android current terminology)
        - Default is set to not include it in the backup (possibility to enable in prefs)

      </details>     
   
   2.4. ***device protected*** data

      <details><summary>Show details ...</summary>

        - Stored usually in /data/user_de/
        - It's usual data, but can be accessed before even unlocking your phone (authentication).
          It's usually data that is needed for the pre-start phase of your device.
          E.g. a preference which decides if your app should start on device boot or not.
        - Default is set to include it in the backup

      </details>     

   2.5. ***media***

      <details><summary>Show details ...</summary>   
   
        - this type is related to a controversial change that originally was slated for Android 10
          it is mandatory for all new apps since August 2020 and/or every app targeting Android 11 (SDK 30)
        - the main thing here is called "Scoped Storage"
        - Storage is then divided into Shared Storage and Private Storage
        - as part of SAF this is Androids approach to secure the access to media files
          and limit it to the ones of each app individually
        - e.g. chat apps have/had to move their media data into data directories, instead of generic folders
          (e.g. WhatsApp's well known WhatsApp folder)
        - Android might wants to move all the data directories to /storage/emulated/0/Android.
          Below this folder you could find folders for "data", "obb", "media"
          ... an in them, a folder of each app's package name
        - Default is set to not include it in the backup (possibility to enable in prefs)

      </details>     

   2.5. cache

      <details><summary>Show details ...</summary>   

        - Default is set to not include it in backups
   
      </details>     
   
   
You can individually choose which parts you want to include in the backup --> as global setting in preferences, (beginning with version NB [OABX] 6.x) per schedule or even per App.

#### What are Special Backups?

Special backups describes system data that's bound to the user and not to certain apps. </br>***Enable special backups in advanced prefs to see them.***</br>
For the moment we don't provide them with full support, try with your own responsibility.

Beginning with version 8 some of them are supported. See also [How can I backup SMS &amp; Call log?](#how-can-i-backup-sms--call-log)

#### Do I need a rooted phone?

Yes, Oui, Si, Si, Ja, Ja, Da, Ay...

#### What is root access used for?

In short words:
Accessing the APK+data of all apps (including system apps and special backups), so to access [all the necessary paths in the filesystem](#what-are-all-these-backup-parts-icons--which-parts-does-a-backup-of-an-app-consist-of).

More Details?:<br/>
You need access to the data directories of the apps. Android creates user accounts for each installed app and assigns the permissions to the data directory just to the user and some system user(s). If the ROM doesn't bring a feature or interface to do backups like the deprecated adb backup or Google Cloud Backup does, it's impossible due to the security model.

Even more detailed?:<br/>
It's differently...depending on the app and probably also for different ROMs.

E.g. for one of the devs ROMs:
/data/app/* all belong to system:system, while
/data/data/* usually belong to appuser:appuser, but some system data belong to system users e.g. system or radio,
(*appuser* is used as a wildcard name, usually something like *u0_595*)
/system/app/* belong to root.

Most probably "system" (which is not necessarily the user "system") starts an app as the appuser. It uses root access to become the appuser to start the app, which then runs as appuser.

Naturally system apps and services run as several system users.

On a A10 ROM a user (in the terminal) cannot access /data/app and /data/data itself. Reading an app's apk directory is possible (normal read access for others via r attribute) if you know the name, e.g. You can do
ls -l /data/app/<app_package_name>-VP8zj7n2sqzHID5Oqfh88w== but I have no chance to find out the name because of the random code that changes on each installation (which also invalidates links to inside). Some directories cannot be read, but only be opened (only x attribute)

You cannot access /data/data/* at all, so app data is protected between apps.

#### Why is NB so slow?

Since rebasing the app on SAF(Storage Access Framework) the performance is bound to what Android's (or Google's) framework can provide.
Needless to say: This is how much love this framework receives from the developers... [Fuck-Storage-Access-Framework](https://github.com/K1rakishou/Fuck-Storage-Access-Framework/)

#### So why use SAF then?

[Access documents and other files from shared storage](https://developer.android.com/training/data-storage/shared/documents-files)
In the next Android versions Google will (most probably) force apps more and more to access the storage via SAF.

<ins>***Pro:***</ins>

- standardized way of accessing files on all storage providers
- more secure -> apps can only access their own data
- the ability of NB to backup to external SD card (or cloud providers) comes through SAF
- ...

<ins>***Con:***</ins>

- Performance, more of Performance and tons of Performance
- obfuscation of the classical path structure

##### Below some "performance" or time measuring infos from an older phone

*related to a lot of SAF comments in the chat*

<details><summary>Click here to show the test-details ...</summary>

###### General facts:
- Device: Fairphone 2 (SoC: Qualcomm MSM8974AB-AB)
- SoC's CPU: Snapdragon 801 (quad-core) 2.26 GHz
- OS: A10 / LOS17.1
- 238 Apps - system + user apps
- no gApps
- root via Magisk
- NB (OABX) - version used for the last test listed here: v7.0.0 stable
- Backup folder on internal storage

As this is a quite old SOC, it can be called low end benchmark. 😉

Prefs ...
(I list only the differences from default)<br/>
Service-Prefs:
- Encryption configured
- Back up external data - enabled
- Back up obb files - enabled
- Back up media files - enabled
- Number of backup revisions = 1

Advanced-Prefs:
- Restore with all permissions - enabled
- Allow downgrading - enabled
- STOP/CONT - enabled <- default now as well

###### Test description:
***Test 1***

Initial refresh (load the app list after the first Start - no backup exists at that point in time)

***Test 2***

Backing up all apps (two Apps excluded as too big + have their own in app bkp possibility) apk + all enabled data types via scheduled backup or batch.

Especially this test is a bit heat dependent with such an old device/SOC.
Phone heats up during such longer running stuff.

Also a second in this examples run takes longer, due to ensure the revision count (time is needed to delete the old backups).
If the default revision count (2) is used, of course, the first run, which takes longer, is the 3rd one. ;-)

***Test 3***

Refresh after starting NB with all those apps and their bkps created under Test 2

###### Measured times:

(they generally vary a lot - depending on what the device is doing in parallel)

***Test 1:***

11 Sek

Former versions tested:
5.0.0-beta1: 11 Sek
4.0.1-alpha4: 11 Sek

***Test 2:***

First run: 16m 45s; Second run: 21m 40s

Former versions tested:
5.0.0-beta1 (around 200 Apps that time): first run: 14m 40s; Second run: ---
4.0.1-alpha4: first run: 17m 49s; Second run: ---

***Test 3:***

20/21 Sek

Former versions tested:
5.0.0-beta1: 14 Sek
4.0.1-alpha4: 19,5 Sek

Side-Comment:
Full scheduled/batch backup with last SAF free release (v3.2.0.) took about 10 minutes (for 195 apps at that time)
--> so much faster
The time difference (for most of the test-tasks shown here) is due to:
- no [SAF](#why-is-nb-so-slow)
- no external data support
- no obb files support
- other folder structure
- different encryption
- (no external sd-card support )

</details>   
   
#### I do not see any apps in the list. What can be the reason?

In most cases the you choose a special Android folder for your backups. (e.g. common mistake is root '/storage/emulated/0' or the "Downloads" folder)
This is not supported by [SAF](#why-is-nb-so-slow). You find a full list which folders are not allowed [here](https://developer.android.com/training/data-storage/shared/documents-files/#document-tree-access-restrictions).

<ins>Solution:</ins><br/>
Create a separate/dedicated sub-folder and choose it in NB preferences (User preferences) as your "Backup folder".

Another mistake, which might happen is, that you set a filters, which lead to an empty result.

#### I do not see the app which is currently backed up in the notification during batch or scheduled backups.

*obsolate beginning with version 8* - see also [What does the notification of schedules and batch jobs tell me?](#what-does-the-notification-of-schedules-and-batch-jobs-tell-me)
   
To optimize the performance of scheduled and batch backups, these tasks are executed in parallel, based on the amount of cores, your SOC's CPU contains.
So notification always shows the last app backup, which was started on whatever free core of your SOC's CPU.

#### At restore the data directory of the app does not exist.

You got an error like "...failed root command...directory `/data/user/0/`*the.package.name* does not exist...".

You look into `/data/user/0/` with a root file manager and the directory *the.package.name* is missing
(and may be there are only a few directories, but there should be one directory for each of the packages in the system, even for system apps).

&rarr; Check if you use namespace isolation for root commands in Magisk (other root solutions might have this, too).

It's recommended to use something like "global namespace for all root commands".
Eventually something like "inherit namespace..." could also work, never tested this.

Background:
If mounts of root applications are isolated, each cannot see the mounts of others.
In newer Android versions every app directory is mounted and usually only visible to it's own user id.

#### How can I backup SMS & Call log?

Generally see * [What are Special Backups?](#what-are-special-backups) first.

**SMS/MMS and Call-logs** </br>
Those are saved in data providers like some other special data. The one you should go for is com.android.providers.telephony. Sometimes you would need to restart after restoring its data. Same goes for contacts too, with the only difference that they're kept in the data of com.android.providers.contacts.

NB starts supporting backup SMS/MMS and Call logs beginning with version 8. 
   
For **contacts, calendar** and todo-lists. 
We advice to use [DecSync](https://github.com/39aldo39/DecSync) with its diverse apps.
Alternatively use a CalDAV/CardDAV management app (like DavX5) and sync them with a trustworthy mail provider account.

#### Are you going to support older Android versions?

No, Non, No, No, Nein, Nej, Niet, La... in seeable future, maybe this would change in the far future... 
Android 7 "Nougat" and older Android version support dropped in NB (OABX) v3.1.0.
See also - [Which Android Versions are supported?](#which-android-versions-are-supported)

#### Why do I have to login/register to app x y z again after restore?

All apps which use the Android keystore can basically not be backup up, as the keystore is encrypted. Data restore might work but login have to be performed again (same for phone number registration for messengers)
Here are several examples - e.g.:

- Nextcloud
- Signal
- Threema
- Whatsapp (?)
- Facebook
- you name it

#### Why is it not recommended to backup system apps?

- ... as they change over the android version and restore might un-stabilize the system
- You've done your backup on 4.0.0, then you should place the data you want to restore at the same directory as when they got backed up.
  - --> In 5.0.0 this is already fixed. 

#### Is there a roadmap / an overview of features planned to be implemented?

A rough backlog (without any guarantee that the devs will 100% stick to it) can be found here as Kanban board: </br>
https://tree.taiga.io/project/machiav3lli-neo-backup/kanban

Not all features will be listed there, but maybe the bigger / frequently asked ones.

#### What is the difference to implementations like Seedvault?

The main difference is that NB uses root to create a copy of the apps [APK and it's data](#what-are-all-these-backup-parts-icons--which-parts-does-a-backup-of-an-app-consist-of) while Seedvault relies on Google's api to backup (without forcing the user to backup to the Google-Cloud).
Seedvault repo can be found here: https://github.com/seedvault-app/seedvault - check it for latest information as the infos/comparison in the table below might be outdated.

|  | NB  | Seedvault  |
| ---: | :--- | :--- |
| root necessary? | [yes](#do-i-need-a-rooted-phone) | no |
| encrypted backup | yes (optional) | yes |
| automatic backup | yes (individual schedules) | yes (daily) |
| usable For | [A8-A12](#which-android-versions-are-supported) | A11; but branches for from A9 to A12; also integrated into known Custom ROMs CalyxOS, GrapheneOS, [LOS](https://www.xda-developers.com/lineageos-seedvault-open-source-backup-solution/) |
| Considers 'allowbackup' flag of apps? | no | yes |
| OS-/Rom integrated | no (dedicated app) | yes |
| Choose backup location possibility? | yes | yes |

#### What is the difference to the famous Titanium Backup?

Users tend to compare NB and Titanium Backup (TB). There are a lot of comments in the chat.

Feel free to share your thoughts and edit this FAQ (e.g. the table below) to provide more details for this comparison.

The main points to are mention:
* TB did not get an update for some years now (since November 2019 - https://play.google.com/store/apps/details?id=com.keramidas.TitaniumBackup )
* TB does not use [SAF](#why-is-nb-so-slow) and so is probably faster as long as google does not improve it)
* TB is not Open-Source.

All the TB features are listed here: https://www.titaniumtrack.com/titanium-backup.html <br/>
Here is a quick status overview, what NB is capable of - to be edited.
<details><summary>Click here to show the comparison table ...</summary>

| TB feature  | NB status |
| :--- | :--- |
| Very fast app listing (~1 second for 300 apps) | this isn't because of SAF, it's probably because NB uses official interfaces of Android. I guess, TB simply scans the directories in /data/data/ or a similar technique. <br/>Reasoning: it sees com.google.android.trichromelibrary* which most backup solutions don't see. |
| Sort apps by name / last backup / backup frequency | YES / YES / NO (don't remember what frequency means) |
| Filter apps by name / type / status / Apps Organizer labels (also affects Batch operations) | YES / YES(?) / YES(?) / NO (not yet) |
| Backup/restore regular apps + their settings | YES |
| Backup/restore protected apps + their settings | PROBABLY (what is it exactly? - maybe device protected data? -> YES) |
| Backup/restore system apps + their settings (incl. Wi-Fi AP list) | YES (some system data, called special backups, "package" names special.*, label starts with $) |
| Backup/restore external app data | YES (external_data, obb, media) | 
| Restores the Market links when restoring apps | NO |
| Zero-click background batch backup | YES (batch backup, but what is zero click?) |
| Interactive batch restore | NO |
| Many batch scenarios (eg: if more than N days since last backup, etc) | NO (only backup and restore) |
| Zero-click app un-installer | YES (but again, what is zero click?) |
| Zero-click system app un-installer | YES (it's there, not sure if it works, never tried ... but again, what is zero click?) |
| Move app to/from SD card | NO |
| Move app data to/from SD card (needs ext2/3/4 partition) | NO |
| Desktop widgets | YES (but I guess it only works for stock (like) launchers) |
| A single weekly or biweekly scheduled backup | YES (any schedules) |
| User-defined apps lists with filtering, coloring and scheduling support | TB has better filtering support...e.g. tags |
| Built-in Android Market information viewer (Android 2.0+) | ??? | 
| Ability to remove orphan app data | NO (at least not as a batch, can TB do this? don't remember) |
| Multiple backups per app (history length can be chosen) | YES (TB can also throw backups for a filtered list) |
| Zero-click background batch restore | YES (but again, what is zero click?) |
| Encryption of your backups (asymmetric crypto: the passphrase is needed on restore only) | YES |
| Backup/restore SMS, MMS, call log, bookmarks and Wi-Fi networks in the portable XML format | NB > v7 ... YES, YES, YES, NO (for which browser?), NO |
| Multi-user support for some apps (eg: games) with a widget for quick switching! | NO |
| Batch verification of your backups | NO |
| Create an “update.zip” file containing apps+data, which can be flashed in recovery mode to restore everything in one shot | NO |
| Migrate some system data (eg: SMS/MMS) across incompatible ROMs | YES (only SMS/MMS/call logs) |
| Convert users apps to system apps | NO |
| Ultra fast HyperShell (much faster for almost everything) | NO (not sure how fast this is, NB currently uses toybox, which might also be fast) |
| App freezer can disable an app (and make it invisible) without un-installing it | YES |
| Batch app freezing/defrosting | NO (TB can do many operations in batch mode with all it's power of filtering. NB can't yet) | 
| Full support for paid apps that must normally be installed through the Market! | not sure, what is necessary for that? | 
| Market “auto updating” manager, to easily verify, enable or disable auto updates on several apps at once! | don't remember what this is |
| Unlimited, independent scheduled backups (each of which can run 1 to 7 times a week) | YES (TB is more flexible with it's filtering) |
| Market Doctor can retrieve or re-create any broken Market links (for your updates to appear in Market again) | NO |
| Dalvik cache cleaner can free up precious internal memory | NO |
| Integrate updated system apps directly into your ROM to free up even more internal memory | NO |
| Synchronize all (or some of) your backups to Dropbox, Box and Google Drive | NO (only via external methods like rclone and SAF providers) |
| Retrieve all your backups from Dropbox, Box and Google Drive (in case of lost phone or SD card failure) | NO (only via external methods like rclone and SAF providers) |
| TB Web Server: download/upload all your backups as a single ZIP file on your computer through a Web interface | NO |
| Load/Save a Filter (from the Filters screen) and use it in Widgets/Schedules | NO | 
| Change the device’s Android ID, restore it from a backup or after a factory reset | NO |
| Protect backup against deletion | NO |
| Send backup (by e-mail / Google Drive / Dropbox) which can be imported/restored very easily | NO (useful) |
| CSV data export from any app’s DB, to e-mail or Google Docs | NO (very useful) | 
| Convert app data to/from Gingerbread’s faster WAL DB format | NO (as far as I know, WAL was also used as a portable format) |
| Brand the app with your name | NO |

</details>

#### How can I open encrypted backups on my computer?

You can find the encryption algorithm and setup in this class: [Neo-Backup - Crypto.kt · GitHub](https://github.com/NeoApplications/Neo-Backup/blob/main/app/src/main/java/com/machiav3lli/backup/utils/CryptoUtils.kt) . The rest depends on the version you used.

#### What does the notification of schedules and batch jobs tell me?

*this decribes the notification beginning with version 8*

***While it is running...***
- the notification will show in its name / heading:
  - The NB-Logo followed by "Neo-Backup"
  - :runner: - app-backups currently running
  - :people_holding_hands: - app-backups queued/outstanding/not yet started
- below the notification headline you will see 
  - the name (in case it is a schedule) and the start-time
- when you expand the notification you will see more details of the parallel tasks which will run (parallelism is based on the amount of cores of the devices SoCs CPU)
  - the first 3 characters show what NB is currently working on (**pre**paration, **apk**, **ext**ernal data, **obb**, **med**ia, etc.)
  - after the dot you will see the package name 

***When it's finished...***
- heading:
  - green NB-Icon in case there were no error 
  - red NB-Icon in case some backups failed
  - count of processed apps and overall apps in the job
- below it will show
  - the name again (in case it is a schedule) 
  - start-time
  - "OK" (in case no errors)
- below that the overall time it took

#### Does NB support multi-user setups / work-profile?

Disabled / not support for now as it led to strange behavior that apk and/or data was overwritten in both profiles.
For now NB will only handle the main profile / user and ignore the others. 

So this also answers if it works together with Apps which utilize the work profile (like Shelter or Island).
You can try to clone NB into it, but it is not recommended.
