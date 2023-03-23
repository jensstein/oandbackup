## The experimental flatStructure scheme

where `the.package.name/YYYY-MM-DD-hh-mm-ss-mmm-user_x*`

is substituted by `the.package.name@YYYY-MM-DD-hh-mm-ss-mmm-user_x*`

so all backups are stored flat in the backup folder.

It is not enabled by default, because it may still change it's format and the last word isn't
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

- collect backups from subfolders (experimental, don't rely on it, though it's easy to chnage it
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

## Troubleshooting

- DevTools: for trouble shooting / power users that have showInfoLogBar enabled:
    - long press on Title will show the DevTools popup for faster access to dev settings, log,
      terminal and other tools
- DevTools: add log autoscroll
- option to autosave a log if an unexpected exception happens, even if it is catched later, so we
  get better info from the inner circles
    - → `advanced/devsettings/logging/autoLogExceptions`
- option to autosave a support log after each schedule, because it's difficult to do this manually
    - → `advanced/devsettings/logging/autoLogAfterSchedule`
- option to autosave a support log on suspicious events, e.g. duplicate schedules
    - situations that are not necessarily an error, only "interesting" sometimes to have a look at
    - → `advanced/devsettings/logging/autoLogSuspicious`
- `SUPPORT` button in the terminal, that can create the interesting infos as a new log item and
  opens share menu in one go
- `share` button saves the text in the terminal and opens share menu
    - if you want to review it, cancel the menu and goto the "View the log" tool instead, you can
      still share from there

## Reliability of schedules and WorkManager items

this is really a multipart problem. I'll try to list all the parts:

* alarm management can be
    * inexact -> alarmManager.setAndAllowWhileIdle(...)
    * exact -> alarmManager.setExactAndAllowWhileIdle(...)
    * alarmclock -> alarmManager.setAlarmClock(...)
    * reliability may mean as exact as possible or within certain contraints
    * current tests suggest that even inexact alarms work within 5 minutes
    * exact alarms may be equivalent to alarmclock (need to investiagte Android sources)
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
