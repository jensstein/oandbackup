# Troubleshooting

note:

* just a beginning
* only collecting what comes along when supporting users
* sometimes extracts of messages in the telegram group (more or less edited)

### backups are not visible (all or only some of them)

=> NB "seeing" a backup means, it finds a valid xxx.properties file for the backup.

So for backups that are not seen, first check the properties file.

It's in json format. It should start with a { and end with }

One obvious reason could be lack of storage,
e.g. the properties files are not there or they are zero length.

Basically the files could also be inaccessible,
e.g. read protected, or owned by the wrong user.

### damaged backups

there are basically two situations:

* folders without properties
* properties without folders

which includes damaged properties and damaged folders (and combinations of damages are also possible).

It seems that the most frequent problem is folders without properties.

This is definitely the most probable, because folders are written first and writing the properties is the last action only in case of a successful backup.

In case of an error the properties are not written and the folder is deleted.
So in theory you would never end up with either of them missing.

What is more probable, NB is killed before the backup is finished.

If this happens, it's usually for all app backups that are run in parallel (=numcores).
So, you usually end up with multiple lonely folders.
One of the archives in each of the folders is probably also incomplete.
Note, unlike zip or other archive formats with a central directory written as last action, a truncated tar archive is still usable, because it's a streaming format.
If a file is added, it's also with complete infos (path, attributes etc.).

### faking properties files if they are missing or damaged

Note: faking properties files does not help, if the backup is encrypted, because the file contains the iv entry,
that is used as salt (basically a second key to randomize data to prevent statistical analysis and other attacks).

You can take a correct properties file and change the important entries.

this is an extract of a ruby script, that creates the properties file:

```ruby
backupVersionCode: BACKUP_version,
backupDate: now.strftime('%Y-%m-%dT%H:%M:%S.%L'),
cpuArch: BACKUP_cpuArch,
packageName: package,
packageLabel: apks.package_label || package,
versionName: apks.version_name || '0.0.0',
versionCode: apks.version_code || 0,
hasApk: apks.count > 0,
hasAppData: size_data > 0,
hasDevicesProtectedData: size_dedata > 0,
hasExternalData: size_extdata > 0,
hasObbData: size_obb > 0,
hasMediaData: size_media > 0,
# iv: [],
# permissions: [],
size: nbytes
```

##### comments about those entries

not very important, but with older version code it would try to do compatibility measures:

```ruby
backupVersionCode: BACKUP_version,
```

not sure, should probably be correct:

```ruby
cpuArch: BACKUP_cpuArch,
```

very important, otherwise it would restore it as another app (I guess):

```ruby
packageName: package,
```

set these to true or false, corresponding to the existing backup data:

```ruby
hasApk: apks.count > 0,
hasAppData: size_data > 0,
hasDevicesProtectedData: size_dedata > 0,
hasExternalData: size_extdata > 0,
hasObbData: size_obb > 0,
hasMediaData: size_media > 0,
```

unimportant, but easy to set from the file date:

```ruby
backupDate: now.strftime('%Y-%m-%dT%H:%M:%S.%L'),
```

these are all unimportant, as you also see in the or (|| value), use just this value instead:

```ruby
packageLabel: apks.package_label || package,
versionName: apks.version_name || '0.0.0',
versionCode: apks.version_code || 0,
```

ommit these from the file and set "restore all permissions" instead or set the permissions manually later:

```ruby
# iv: [],
# permissions: [],
```

unimportant, only informative, it can even be zero (in fact older backups don't have this entry and then it defaults to zero):

```ruby
size: nbytes
```

### *.properties files are zero size, but corresponding archives are created (ANExplorer installed)

=> it seems ANExplorer replaced DocumentsUI and doesn't really work as DocumentsUI

this is not really proofed and it doesn't feel logical,
but using DocumentsUI again worked, which also results in reselecting the backup directory,
so it is possible that the permission got lost.

However, if access is not working, no file would have been created.

Though, ANExplorer seems to be the provider for the storage and this means it is at least
between a request and the execution by the system.

Perhaps text files are handled differently and don't work.

### Firefox: "couldn't update permission for data"

* restore Firefox apk and data from OAndBackupX. Ignore the error.
* force-close Firefox
* delete the lock file (/data/data/[package.name]/files/mozilla/[profile.id].default/lock).
* start Firefox

### "install failed verification"

=> Disable verify apps over USB   [x]

### I cannot select a backup directory

=> DocumentsUI a.k.a. Files app is used/necessary to grant access to the folder via SAF

* there seem to be ROMs that remove DocumentsUI or maybe you did it manually
  ("debloating" apps, you didn't know => not everything you don't know is "bloat")
* at some point there also was a Magisk module for MiXplorer that removed DocumentsUI.
* ANExplorer also seems to mess with it.
