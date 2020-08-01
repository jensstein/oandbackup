## Possible Features

- [ ] Rootless Backups: maybe [Shizuku](https://github.com/RikkaApps/Shizuku) could be useful in this matter.

- [ ] More functionality for AppSheet: kill app, clear data (for now...).

- [ ] Multi-Backups support: [details left for Nils (@Tiefkuehlpizze) to fill]...

- [ ] Add options upon choosing Backup from AppSheet: external/obb/cache.

- [ ] External Storage/SAF support: my idea on it is splitting Backup-Actions into more steps(vice versa for Restore):
  
  1. CPing the to be backed up files into OABX's External Data folder
  
  2. (un)zipping them and cleaning up
  
  3. using DocumentFile/SAF to move them into the Backup Directory 
     
     - in Batch Backup/Restore-Tasks: step 3 happens at the end/start of all of operations. And though each step could be mediated to the user.

- [ ] Add Flashable Zip feature: lowest priority for now.

## Needed Fixes

* [ ] SymLinks and "special" Files backup: fixing errors on some "unique" apps.
- [ ] Fully support for Special Backups: Accounts, SMS, Bluetooth, Wifi (for now...).

- [ ] Compatibility with Work Profiles: making Backup/Restore-Task profiles-sensetive.

- [ ] Add AlertDialog about disabling "verify apps over USB" when needed.

## Planned Restructure

* [ ] Fragmentize Batch: to clean up the need for double use of Batch Activity and seperate Backup and Restore logic more clearly.  
- [ ] Main-Batch Direct Navigation: with the aim to remove the need to recreate the app list anew each time navigating between Main and Batch.

- [ ] More clear visual hints on Items: Sys/User/Special, App/Data/Both.

- [ ] Add (Backup/Restore)Swipe-Gestures to Main.
