## Apps:

* Which use android's keystore to identify users: Signal, 

* Some Termux plugins which have cyclic smylinks

* Some system apps aren't meant to be backup/restored just be careful not breaking the system when you play with those

* MIUI optimizations (and some other OSes) don't allow the restore in most scenarios

* VLC seems to have problem backing up

* Restore of apps that use Magisk's systemless installation could cause the system crashing fully.

## OABX specific:

* Backups from 4.0.0 can only be restored when they get placed at the same address/directory when they got backed up (this is fixed in 5.0.0)

* Asking for storage permissions(ask for backup directory) on A10/A11 on each launch or restart (fixed in 5.0.0)

* When your system has SELinux set to enforcing it may results restored apps unable to launch or restore successfully.

* Scheduled backups taking longer than 10 minutes get killed by the system, so try to partition your apps list in different scheduled actions