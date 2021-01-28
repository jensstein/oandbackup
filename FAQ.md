# Frequently Asked Questions:

* [What is OAndBackupX?](#what-is-oandbackupx)
* [What are all these backup-parts (icons)? / Which parts does a backup of an app consist of?](#what-are-all-these-backup-parts-icons--which-parts-does-a-backup-of-an-app-consist-of)
* [What are Special Backups?](#what-are-special-backups)
* [Do I need a rooted phone?](#do-i-need-a-rooted-phone)
* [What is root access used for?](#what-is-root-access-used-for)
* [Why is OABX so slow?](#why-is-oabx-so-slow)
* [So why use SAF then?](#so-why-use-saf-then)
* [How can I backup SMS &amp; Call log?](#how-can-i-backup-sms--call-log)
* [Are you going to support older Android versions?](#are-you-going-to-support-older-android-versions)
* [Why do I have to login/register to app x y z again after restore?](#why-do-i-have-to-loginregister-to-app-x-y-z-again-after-restore)
* [Why is it not recommended to backup system apps?](#why-is-it-not-recommended-to-backup-system-apps)
* [What is the difference to implementations like Seedvault?](#What-is-the-difference-to-implementations-like-Seedvault)
* [How can I open encrypted backups on my computer?](#how-can-i-open-encrypted-backups-on-my-computer)

#### What is OAndBackupX?

OAndBackupX is a fork of the OAndBackup (which is inactive) with the aim to bring OAndBackup to 2020. For now most of the functionality and UI of the app are rewritten, next steps would be making it stable and adding some features which could ease the backup/restore workflow with any device.
Requires root and allows you to backup individual apps and their data. Both backup and restore of individual programs one at a time and batch backup and restore of multiple programs are supported.

#### What are all these backup-parts (icons)? / Which parts does a backup of an app consist of?  
  
Each backup basically consits of the two different parts:  
    
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
        - Default is set to include it in the backup
    
   2.5. cache
   
        - Default is set to not include it in backups

#### What are Special Backups?

Special backups describes system data that's bound to the user and not to certain apps.
For the moment we don't provide them with full support, try with your own responsibility

#### Do I need a rooted phone?  
  
Yes, Oui, Si, Si, Ja, Ja, Da, Ay...  
  
#### What is root access used for?  

Accessing the APK+data of all apps (including system apps and special backups)

#### Why is OABX so slow?  
  
Since rebasing the app on SAF(Storage Access Framework) the performance is bound to what Android's (or Google's) framework can provide. 
Needless to say: This is how much love this framework recieves from the developers... [Fuck-Storage-Access-Framework](https://github.com/K1rakishou/Fuck-Storage-Access-Framework/)  

#### So why use SAF then?
  
[Access documents and other files from shared storage](https://developer.android.com/training/data-storage/shared/documents-files)  
In the next Android Versoins Google will (most probably) force apps more and more to access the storage via SAF.  
  
<ins>***Pro:***</ins>

- standardized way of accessing files on all storage providers
- more secure -> apps can only access their own data
- the ability of OABX to backup to external SD card (or cloud providers) comes through SAF
- ... 

<ins>***Con:***</ins>

- Performance, more of Performance and tons of Performance
- obfuscation of the classical path structure

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
| usable For | [A8-A11](#are-you-going-to-support-older-android-versions) | A11 only (branches down to A9) |
| Considers 'allowbackup' flag of apps? | no | yes |
| OS-/Rom intergrated | no (dedicated app) | yes |
| Choose backup location possibility? | yes | yes |

#### How can I open encrypted backups on my computer?  
  
You can find the encryption algorithm and setup in this class: [oandbackupx/Crypto.kt at master · machiav3lli/oandbackupx · GitHub](https://github.com/machiav3lli/oandbackupx/blob/master/app/src/main/java/com/machiav3lli/backup/handler/Crypto.kt) . The rest depends on the version you used.
