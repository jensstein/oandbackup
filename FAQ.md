# Frequently Asked Questions:

* [What is OAndBackupX?](#what-is-oandbackupx)
* [How do I use OABX?](#how-do-i-use-oabx)
* [What are all these backup-parts (icons)? / Which parts does a backup of an app consist of?](#what-are-all-these-backup-parts-icons--which-parts-does-a-backup-of-an-app-consist-of)
* [What are Special Backups?](#what-are-special-backups)
* [Do I need a rooted phone?](#do-i-need-a-rooted-phone)
* [What is root access used for?](#what-is-root-access-used-for)
* [Why is OABX so slow?](#why-is-oabx-so-slow)
* [So why use SAF then?](#so-why-use-saf-then)
* [I do not see any apps in the list. What can be the reason?](#i-do-not-see-any-apps-in-the-list-what-can-be-the-reason)
* [How can I backup SMS &amp; Call log?](#how-can-i-backup-sms--call-log)
* [Are you going to support older Android versions?](#are-you-going-to-support-older-android-versions)
* [Why do I have to login/register to app x y z again after restore?](#why-do-i-have-to-loginregister-to-app-x-y-z-again-after-restore)
* [Why is it not recommended to backup system apps?](#why-is-it-not-recommended-to-backup-system-apps)
* [What is the difference to implementations like Seedvault?](#what-is-the-difference-to-implementations-like-seedvault)
* [How can I open encrypted backups on my computer?](#how-can-i-open-encrypted-backups-on-my-computer)

#### What is OAndBackupX?

OAndBackupX (short OABX) is a fork of [OAndBackup](https://gitlab.com/jensstein/oandbackup) (which is inactive) with the aim to bring it to 202x. For now most of the functionality and UI of the app are rewritten, next steps would be making it stable and adding some features which could ease the backup/restore workflow with any device.
Requires root and allows you to backup individual apps and their data. Both backup and restore of individual programs one at a time and batch backup and restore of multiple programs are supported.

#### How do I use OABX?

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

In case of any problems report it in [the Telegram group](https://github.com/machiav3lli/oandbackupx#on-telegram-tmeoandbackupx) first (sometimes there is an easy solution) and/or [raise an issue here in github](https://github.com/machiav3lli/oandbackupx/issues).

#### What are all these backup-parts (icons)? / Which parts does a backup of an app consist of?  
  
Each backup basically consists of the two different parts:  
    
1. the software itself (stored in a so called APK file) and  
    
2. its data (created while using an app, settings, etc.)
   The data can be split again into:
   
   2.1. normal data
   
        - Stored usually in /data/data
        - Default is set to include it in the backup
   
   2.2. external data
   
       - Stored usually in /Android/data/ in the external storage (internal storage in android current terminology)
       - Default is set to not include it in the backup
   
   2.3. obb files
   
        - Stored usually in /Android/obb/ in the external storage (internal storage in android current terminology)
        - Default is set to not include it in the backup
    
   2.4. device protected data
   
        - Stored usually in /data/user_de/
        - It's usual data, but can be accessed before even unlocking your phone (authentication).
          It's usually data that is needed for the pre-start phase of your device. 
          E.g. a preference which decides if your app should start on device boot or not. 
        - Default is set to include it in the backup
    
   2.5. cache
   
        - Default is set to not include it in backups

You can individually choose which parts you want to include in the backup --> as global setting in preferences, (beginning with verison OABX 6.x) per schedule or even per App.

#### What are Special Backups?

Special backups describes system data that's bound to the user and not to certain apps.
For the moment we don't provide them with full support, try with your own responsibility

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

#### Why is OABX so slow?  
  
Since rebasing the app on SAF(Storage Access Framework) the performance is bound to what Android's (or Google's) framework can provide. 
Needless to say: This is how much love this framework receives from the developers... [Fuck-Storage-Access-Framework](https://github.com/K1rakishou/Fuck-Storage-Access-Framework/)  

#### So why use SAF then?
  
[Access documents and other files from shared storage](https://developer.android.com/training/data-storage/shared/documents-files)  
In the next Android versions Google will (most probably) force apps more and more to access the storage via SAF.  
  
<ins>***Pro:***</ins>

- standardized way of accessing files on all storage providers
- more secure -> apps can only access their own data
- the ability of OABX to backup to external SD card (or cloud providers) comes through SAF
- ... 

<ins>***Con:***</ins>

- Performance, more of Performance and tons of Performance
- obfuscation of the classical path structure

#### I do not see any apps in the list. What can be the reason?

In most cases the you chose a special Android folder for your backups. (e.g. common mistake is root '/storage/emulated/0' or the "Downloads" folder)
This is not supported by SAF. You find a full list which folders are not allowed [here](https://developer.android.com/training/data-storage/shared/documents-files/#document-tree-access-restrictions).

<ins>Solution:</ins><br/>
Create a separate folder and choose it in oabx preferences (User preferences) as your "Backup folder".

Another mistake which might happen is, that you set special filters which lead to an empty result.

#### How can I backup SMS & Call log?

Those are saved in data providers like some other special data. The one you should go for is com.android.providers.telephony. Sometimes you would need to restart after restoring its data.
Same goes for contacts too, with the only difference that they're kept in the data of com.android.providers.contacts.
For contacts, calendar and todo-lists. We advice to use [DecSync](https://github.com/39aldo39/DecSync) with its diverse apps.

#### Are you going to support older Android versions?  

No, Non, No, No, Nein, Nej, Niet, La... in seable future, maybe this would change in the far future...   
Oldest supported version:  A8 - "Oreo" (A7 "Nougat" and older Android version support dropped in OABX v3.1.0)  
Newest supported version: A11 - „Red Velvet Cake“ 

#### Why do I have to login/register to app x y z again after restore?
  
All apps which use the Android keystore can basically not be backup up, as the keystore is encrypted. Data restore might work but login have to be performed again (same for phone number registration for messengers)  
Here are several examples - e.g.: 

- Nextcloud
- Signal
- Threema
- Whatsapp
- Facebook
- you name it

#### Why is it not recommended to backup system apps?

- ... as they change over the android version and restore might un-stabilize the system
- You've done your backup on 4.0.0, then you should place the data you want to restore at the same directory as when they got backed up.
  - --> In 5.0.0 this's already fixed. 

#### What is the difference to implementations like Seedvault?

The main difference is that OABX uses root to create a copy of the apps [APK and it's data](#what-are-all-these-backup-parts-icons--which-parts-does-a-backup-of-an-app-consist-of) while Seedvault relies on Google's api to backup (without forcing the user to backup to the Google-Cloud). 
Seedvault repo can be found here: https://github.com/seedvault-app/seedvault - check it for latest information as the infos/comparison in the table below might be outdated.

|  | OABX  | Seedvault  |
| ---: | :--- | :--- |
| root necessary? | [yes](#do-i-need-a-rooted-phone) | no |
| encrypted backup | yes (optional) | yes |
| automatic backup | yes (individual schedules) | yes (daily) |
| usable For | [A8-A11](#are-you-going-to-support-older-android-versions) | A11 only (but [integrated into LOS now](https://www.xda-developers.com/lineageos-seedvault-open-source-backup-solution/) ) |
| Considers 'allowbackup' flag of apps? | no | yes |
| OS-/Rom intergrated | no (dedicated app) | yes |
| Choose backup location possibility? | yes | yes |

#### How can I open encrypted backups on my computer?  
  
You can find the encryption algorithm and setup in this class: [oandbackupx/Crypto.kt at master · machiav3lli/oandbackupx · GitHub](https://github.com/machiav3lli/oandbackupx/blob/master/app/src/main/java/com/machiav3lli/backup/handler/Crypto.kt) . The rest depends on the version you used.
