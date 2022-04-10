# Reliability of schedules and WorkManager items

this is really a multipart problem. I'll try to list all the parts:

* alarm management can be
  * inexact -> alarmManager.setAndAllowWhileIdle(...)
  * exact -> alarmManager.setExactAndAllowWhileIdle(...)
  * alarmclock -> alarmManager.setAlarmClock(...)
  * reliability may mean as exact as possible or within certain contraints
  * current tests suggest that even inexact alarms work within 5 minutes
  * exact alarms may be equivalent to alarmclock (need to investiagte Android sources)
* when the service receives the alarm event, it must run uninterrupted until all work items are queued
  * the latest docs say this can/should be achieved by a wakelock
* work items are executed at will by the Android WorkManager
  * current docs say use setExpedited on WorkRequests (see below) 
* each work item must run uninterrupted
  * it is guaranteed that doWork isn't put to sleep, however we also set a wakelock   
* a single work item may be killed if it takes too long
  * see below
  * if WorkManager is using JobScheduler, there is a limit of 10 minutes
  * setExpedited should override that, but it's unclear if there are quotae


## WorkManager

https://developer.android.com/reference/androidx/work/Worker.html#doWork()

"A Worker has a well defined "execution window" to finish its execution and return a ListenableWorker.Result.
After this time has expired, the Worker will be signalled to stop."


### "execution window" links to JobScheduler:

https://developer.android.com/reference/android/app/job/JobScheduler

"While a job is running, the system holds a wakelock on behalf of your app.
For this reason, you do not need to take any action to guarantee that the device stays awake for the duration of the job."

"Prior to Android version Build.VERSION_CODES.S, jobs could only have a maximum of 100 jobs scheduled at a time.
Starting with Android version Build.VERSION_CODES.S, that limit has been increased to 150.
Expedited jobs also count towards the limit."

hg42: WorkManager probably doesn't have this limit. It can easily queue 600-800 jobs (tested many times)

"In Android version Build.VERSION_CODES.LOLLIPOP, jobs had a maximum execution time of one minute.
Starting with Android version Build.VERSION_CODES.M and ending with Android version Build.VERSION_CODES.R,
jobs had a maximum execution time of 10 minutes.
Starting from Android version Build.VERSION_CODES.S, jobs will still be stopped after 10 minutes
if the system is busy or needs the resources,
but if not, jobs may continue running longer than 10 minutes."

hg42: so there is an execution limit for each job if WorkManager uses JobScheduler (Android 5.0+)

also:

https://stackoverflow.com/questions/53734165/android-workmanager-10-minute-thread-timeout-coming-from-somewhere

> Does anyone know if a Worker, or the ThreadPoolExecutor it uses, or some other involved class has this 10 minute thread processing limit

JobScheduler does, and WorkManager delegates to JobScheduler on Android 5.0+ devices.


### WorkManager.getForegroundInfoAsync

public ListenableFuture<ForegroundInfo> getForegroundInfoAsync ()

Return an instance of ForegroundInfo if the WorkRequest is important to the user.
In this case, WorkManager provides a signal to the OS that the process should be kept alive while this work is executing.

Prior to Android S, WorkManager manages and runs a foreground service on your behalf to execute the WorkRequest,
showing the notification provided in the ForegroundInfo.
To update this notification subsequently, the application can use NotificationManager.

Starting in Android S and above, WorkManager manages this WorkRequest using an immediate job.

Returns  ListenableFuture<ForegroundInfo>
A ListenableFuture of ForegroundInfo instance if the WorkRequest is marked immediate.
For more information look at WorkRequest.Builder.setExpedited(OutOfQuotaPolicy).
