package com.machiav3lli.backup.utils

import com.machiav3lli.backup.preferences.pref_trace
import com.machiav3lli.backup.preferences.pref_traceDebug
import com.machiav3lli.backup.preferences.pref_traceFlows
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

object TraceUtils {

    // use these in implementations of other trace functions

    fun traceBold(text: String = "") {
        if (pref_trace.value)
            Timber.w(text)
    }

    fun trace(text: String = "") {
        if (pref_trace.value)
            Timber.d(text)
    }

    // use these instead to have lazy evaluation of the text

    fun traceBold(lazyText: () -> String) {
        if (pref_trace.value)
            Timber.w(lazyText())
    }

    fun trace(lazyText: () -> String) {
        if (pref_trace.value)
            Timber.d(lazyText())
    }

    fun traceDebug(lazyText: () -> String) {
        if (pref_trace.value && pref_traceDebug.value)
            Timber.d(lazyText())
    }

    fun <T> Flow<T>.trace(lazyText: (T) -> String): Flow<T> {
        return if (pref_traceFlows.value)
            onEach { traceBold { lazyText(it) } }
        else
            this
    }

    // reflection

    fun stackFrame(skip: Int = 0): StackTraceElement? {
        return try {
            // >= Java 9  StackWalker.getInstance().walk { stream -> stream.skip(1).findFirst().get() }
            Throwable().stackTrace[skip + 1]
        } catch (e: Throwable) {
            null
        }
    }

    fun methodName(skip: Int = 0): String {
        return try {
            val frame = stackFrame(skip + 2)
            frame?.methodName ?: ""
        } catch (e: Throwable) {
            ""
        }
    }

    fun classAndMethodName(skip: Int = 0): String {
        return try {
            val frame = stackFrame(skip + 1)
            "${frame?.className}::${frame?.methodName}"
        } catch (e: Throwable) {
            ""
        }
    }

    fun classAndId(obj: Any?): String {
        return if (obj != null)
            "${obj.javaClass.simpleName}@${Integer.toHexString(obj.hashCode())}"
        else
            "<null>"
    }
}

