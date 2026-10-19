/*
 * Neo Backup: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.manager.tasks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/* adapted from with small changes to fit our usage:
 * https://github.com/ladrahul25/CoroutineAsyncTask/blob/master/app/src/main/java/com/example/background/CoroutinesAsyncTask.kt
 */
@OptIn(ExperimentalAtomicApi::class)
abstract class CoroutinesAsyncTask<Params, Progress, Result>(
    private val scope: CoroutineScope
) {

    enum class Status {
        PENDING,
        RUNNING,
        FINISHED
    }

    private val statusRef = AtomicReference(Status.PENDING)
    var status: Status
        get() = statusRef.load()
        private set(value) {
            statusRef.store(value)
        }

    private var job: Job? = null

    @Volatile
    private var isCancelled = false

    abstract suspend fun doInBackground(vararg params: Params?): Result?
    open fun onProgressUpdate(vararg values: Progress?) {}
    open fun onPostExecute(result: Result?) {}
    open fun onPreExecute() {}
    open fun onCancelled(result: Result?) {}

    fun execute(vararg params: Params) {
        val currentStatus = status
        statusRef.compareAndSet(Status.PENDING, Status.RUNNING)
        when (currentStatus) {
            Status.RUNNING  -> throw IllegalStateException(
                "Cannot execute task: ${this.javaClass.name} - task is already running"
            )

            Status.FINISHED -> throw IllegalStateException(
                "Cannot execute task: ${this.javaClass.name} - task has already been executed"
            )

            Status.PENDING  -> {} // Successfully transitioned to RUNNING
        }

        job = scope.launch(Dispatchers.Main.immediate) {
            try {
                onPreExecute()

                val result = withContext(Dispatchers.Default) {
                    doInBackground(*params)
                }

                statusRef.store(Status.FINISHED)

                if (!isCancelled) {
                    onPostExecute(result)
                } else {
                    onCancelled(result)
                }
            } catch (e: Exception) {
                Timber.e(e, "Task execution failed: ${this@CoroutinesAsyncTask.javaClass.name}")
                statusRef.store(Status.FINISHED)
                if (!isCancelled) {
                    onPostExecute(null)
                }
            }
        }
    }

    fun cancel(mayInterruptIfRunning: Boolean = true) {
        if (isCancelled) return

        isCancelled = true
        statusRef.store(Status.FINISHED)

        if (mayInterruptIfRunning) {
            job?.cancel()
        }

        scope.launch(Dispatchers.Main.immediate) {
            onCancelled(null)
        }
    }

    fun publishProgress(vararg progress: Progress) {
        if (isCancelled) return

        scope.launch(Dispatchers.Main.immediate) {
            if (!isCancelled) {
                onProgressUpdate(*progress)
            }
        }
    }
}