
* [The experimental flatStructure scheme](#the-experimental-flatstructure-scheme)
  * [Why?](#why)
  * [Technicalities](#technicalities)
* [Reliability of schedules and WorkManager items](#reliability-of-schedules-and-workmanager-items)
  * [WorkManager](#workmanager)
* [nsenter](#nsenter)
* [keystore](#keystore)

## The experimental flatStructure scheme

where `the.package.name/YYYY-MM-DD-hh-mm-ss-mmm-user_x*`

is substituted by `the.package.name@YYYY-MM-DD-hh-mm-ss-mmm-user_x*`

so all backups are stored flat in the backup folder.

It is not enabled by default, because it may still change its format and the last word isn't
spoken, maybe official in 9.0 or even dropped, there are also some possible alternatives.

You find flatStructure under

- → `advanced/devsettings/alternatives`

### Why?

Theoretically, this should result in faster scanning because it reduces the number of directory
scans, and it actually worked. `flatStructure` might be especially helpful, if you are using remote
backup locations.

However, current measurements are more like it doesn't matter much, because the parallel processing
changed the game and remote access seems to focus more on file reading (properties files) instead of
directory scanning.
Though this might be heavily dependent on the remote file service. In my (@hg42) tests it was ssh on
local network using extRact (which uses rclone).

### Technicalities

Note: it uses `@` as separator instead of the former `-`

the backups are now scanned differently, which allows to

- collect backups from subfolders (experimental, don't rely on it, though it's easy to change it
  back)
- backups that were renamed (e.g. by bug + SAF design problem)
- handle different backups schemes

**Note: all backups found are handled by housekeeping. If you used renaming backups to protect
them (which was never supported inside the backup location, because it disturbs file management),
they may now be subject to housekeeping, so save them elsewhere.**

The different variants of backups are marked (in AppSheet), basically by

- replacing `the.package.name` by `📦` and
- removing the `YYYY-MM-DD-hh-mm-ss-mmm-user_x` part

so it looks like this:

- *nothing shown*

  a "standard" flat backup

  → `the.package.name@YYYY-MM-DD-hh-mm-ss-mmm-user_x`

- **`somefolder/`**

  a flat backup, but in a folder

  → `somefolder/the.package.name@YYYY-MM-DD-hh-mm-ss-mmm-user_x`

- **`📦-`**

  a flat backup with the former "-" separator

  → `the.package.name-YYYY-MM-DD-hh-mm-ss-mmm-user_x`

- **`📦/`**

  classic backup with package folder

  → `the.package.name/YYYY-MM-DD-hh-mm-ss-mmm-user_x`

- **`somefolder/📦/`**

  the same inside a folder

  → `somefolder/the.package.name/YYYY-MM-DD-hh-mm-ss-mmm-user_x`

- **`pre%📦%suf/`**

  the same with "pre%" before the package name and "%suf" after it

  → `pre%the.package.name%suf/YYYY-MM-DD-hh-mm-ss-mmm-user_x`

- **`📦 (1)/`**

  a package folder with a duplicate created (falsely) by SAF problem

  → `the.package.name (1)/YYYY-MM-DD-hh-mm-ss-mmm-user_x`


## Reliability of schedules and WorkManager items

this is really a multipart problem. I'll try to list all the parts:

* alarm management can be
    * inexact -> alarmManager.setAndAllowWhileIdle(...)
    * exact -> alarmManager.setExactAndAllowWhileIdle(...)
    * alarmclock -> alarmManager.setAlarmClock(...)
    * reliability may mean as exact as possible or within certain contraints
    * current tests suggest that even inexact alarms work within 5 minutes
    * exact alarms may be equivalent to alarmclock (need to investigate Android sources)
* when the service receives the alarm event, it must run uninterrupted until all work items are
  queued
    * the latest docs say this can/should be achieved by a wakelock
* work items are executed at will by the Android WorkManager
    * current docs say use setExpedited on WorkRequests (see below)
* each work item must run uninterrupted
    * it is guaranteed that doWork isn't put to sleep, however we also set a wakelock
* a single work item may be killed if it takes too long
    * see below
    * if WorkManager is using JobScheduler, there is a limit of 10 minutes
    * setExpedited should override that, but it's unclear if there are quotae

### WorkManager

https://developer.android.com/reference/androidx/work/Worker.html#doWork()

"A Worker has a well defined "execution window" to finish its execution and return a
ListenableWorker.Result.
After this time has expired, the Worker will be signalled to stop."

#### "execution window" links to JobScheduler:

https://developer.android.com/reference/android/app/job/JobScheduler

"While a job is running, the system holds a wakelock on behalf of your app.
For this reason, you do not need to take any action to guarantee that the device stays awake for the
duration of the job."

"Prior to Android version Build.VERSION_CODES.S, jobs could only have a maximum of 100 jobs
scheduled at a time.
Starting with Android version Build.VERSION_CODES.S, that limit has been increased to 150.
Expedited jobs also count towards the limit."

hg42: WorkManager probably doesn't have this limit. It can easily queue 600-800 jobs (tested many
times)

"In Android version Build.VERSION_CODES.LOLLIPOP, jobs had a maximum execution time of one minute.
Starting with Android version Build.VERSION_CODES.M and ending with Android version
Build.VERSION_CODES.R,
jobs had a maximum execution time of 10 minutes.
Starting from Android version Build.VERSION_CODES.S, jobs will still be stopped after 10 minutes
if the system is busy or needs the resources,
but if not, jobs may continue running longer than 10 minutes."

hg42: so there is an execution limit for each job if WorkManager uses JobScheduler (Android 5.0+)

also:

https://stackoverflow.com/questions/53734165/android-workmanager-10-minute-thread-timeout-coming-from-somewhere

> Does anyone know if a Worker, or the ThreadPoolExecutor it uses, or some other involved class has
> this 10 minute thread processing limit

JobScheduler does, and WorkManager delegates to JobScheduler on Android 5.0+ devices.

#### WorkManager.getForegroundInfoAsync

public ListenableFuture<ForegroundInfo> getForegroundInfoAsync ()

Return an instance of ForegroundInfo if the WorkRequest is important to the user.
In this case, WorkManager provides a signal to the OS that the process should be kept alive while
this work is executing.

Prior to Android S, WorkManager manages and runs a foreground service on your behalf to execute the
WorkRequest,
showing the notification provided in the ForegroundInfo.
To update this notification subsequently, the application can use NotificationManager.

Starting in Android S and above, WorkManager manages this WorkRequest using an immediate job.

Returns ListenableFuture<ForegroundInfo>
A ListenableFuture of ForegroundInfo instance if the WorkRequest is marked immediate.
For more information look at WorkRequest.Builder.setExpedited(OutOfQuotaPolicy).

## nsenter

Use `nsenter` to run commands in the global mount namespace (of init process -> pid=1)

- This probably works with all superuser solutions (tested: Magisk, KernelSU, phhsu)
- The only condition is piping commands into `su` command and existence of `nsenter`.
- According
  to https://android.googlesource.com/platform/system/core/+/master/shell_and_utilities/README.md it
  is available since Andorid 10
- For older android versions it falls back to the --mount-master method if available
- The availability of each option is logged at start and in support log

## keystore
I once predicted, that the number of apps using keystore will increase over time...at least for my apps I see this effect, the percentage is growing.

as we experienced with NB itself, the crashes probably originate from the common java strategy to throw an exception instead of returning values.
It also seems like security people prefer this strategy over informing the user instead.

If an app uses the keystore, it often does not expect that using it may fail with an exception, but it does in certain cases (e.g. changing the device). So instead of a failing login (or only a missing password in case of NB) the app just crashes.

If the app would catch the exception like NB does since we fixed it, then it would often only need a new login instead of making the whole app unusable, which leads to clearing the whole data.
In case of NB you only end up in an unset password, which is not dramatic.

Some developers learned how to do it, but most seem to ignore it or don't even know about it.
Maybe it's only happening in case of restores.
That's one aspect of "we don't support root".

The bad thing is, the user cannot do anything about it, but clear the data, so the backup is useless.

I guess it would be possible to write a magisk/ksu module to intercept the function call and catch that exception and return some nonsense instead (garbage in -> garbage out, instead of garbage in -> crash).
