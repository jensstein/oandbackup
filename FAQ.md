Frequently Asked Questions:  

- What are all these backup-parts (icons)? / Which parts does a backup of an app consist of?  
  
  - Each backup basically consits of the two different parts:  
    
    - the software itself (stored in a so called APK file) and  
    
    - its data (created while using an app, settings, etc.)  
  
  - as there is a limit for the sizes of APK files set by Google, additional software parts can be delivered via additional obb-files (common for Games -> bigger parts like videos etc.)   
    [APK Expansion Files &nbsp;|&nbsp; Android Developers](https://developer.android.com/google/play/expansion-files.html)  
  
  - The data part can be split again into:
    
    - normal data
      
      - Stored usually in /data/data
      
      - Default is set to include it in the backup
    
    - external data
      
      - Stored usually in /Android/data/ in the external storage (internal storage in android current terminology)
      
      - Default is set to not include it in the backup
    
    - obb files
      
      - Stored usually in /Android/obb/ in the external storage (internal storage in android current terminology)
      
      - Default is set to not include it in the backup
    
    - device protected data
      
      - Stored usually in /data/user_de/
      
      - Default is set to include it in the backup
    
    - cache
      
      - Default is set to not include it in backups

- What are Special Backups?
  
  - System data that's bound to the user and not to certain apps.
  - For them moment we don't provide them with full support, try with your own responsibility

- Do I need a rooted phone?  
  
  - Yes, Oui, Si, Si, Ja, Ja, Da, Ay...  
  
  - What is it used for?  
    
    - Accessing the APK+data of all apps (including system apps and special backups)

- Why is OABX so slow?  
  
  - Since rebasing the app on SAF(Storage Access Framework) the performance is bound to what Android's (or Google's) framework can provide. Needless to say: This is how much love this framework recieves from the developers... [Fuck-Storage-Access-Framework](https://github.com/K1rakishou/Fuck-Storage-Access-Framework/)  

- So why use SAF then?
  
  - [Access documents and other files from shared storage](https://developer.android.com/training/data-storage/shared/documents-files)  
    
    - In the next Android Versoins Google will force apps more and more to access the storage via SAF (???)  
  
  - Pro:  
    
    - standardized way of accessing files on all storage providers
    
    - more secure -> apps can only access their own data
    
    - Ability of OABX to backup to external SD card (or cloud providers) comes through SAF
    
    - ... 
  
  - Con:  
    
    - Performance, more of Performance and tons of Performance

- How can I backup SMS & Call log?
  
  - Those are saved in data providers like some other special data. The one you should go for is com.android.providers.telephony. Sometimes you would need to restart after restoring its data.
  - Same goes for contacts too, with the only difference that they're kept in the data of com.android.providers.contacts.
  - For contacts, calendar and todo-lists. We advice to use [DecSync](https://github.com/39aldo39/DecSync) with its diverse apps.

- Are you going to support older Android versions?  
  
  - No, Non, No, No, Nein, Nej, Niet, La... in seable future, maybe this would change in the far future...   
  
  - Oldest supported version: A8 - "Oreo" (A7 - "Nougat" support dropped in v3.1.0)  
  
  - Newest supported version: A11 - „Red Velvet Cake“ 

- Why do I have to login/register to app x y z again after restore?
  
  - All apps which use the Android keystore can basically not be backup up, as the keystore is encrypted. Data restore might work but login have to be performed again (same for phone number registration for messengers)  
  
  - there are several examples - e.g.: Nextcloud, Signal, Threema, Whatsapp, Facebook, ... ???  

- Why is it not recommended to backup system apps?
  
  - *... as they change over the android version and restore might un-stabi**lize the system*

- - You've done your backup on 4.0.0, then you should place the data you want to restore at the same directory as when they got backed up.
  - In 5.0.0 this's already fixed. 

- How can I open encrypted backups on my computer?  
  
  - You can find the encryption algorithm and setup in this class: [oandbackupx/Crypto.kt at master · machiav3lli/oandbackupx · GitHub](https://github.com/machiav3lli/oandbackupx/blob/master/app/src/main/java/com/machiav3lli/backup/handler/Crypto.kt) . The rest depends on the version you used.
