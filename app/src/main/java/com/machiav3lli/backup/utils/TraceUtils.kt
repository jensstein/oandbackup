package com.machiav3lli.backup.utils

import com.machiav3lli.backup.preferences.pref_trace
import com.machiav3lli.backup.preferences.pref_traceFlows
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

object TraceUtils {

    fun traceBold(text: String = "") {
        if(pref_trace.value)
            Timber.w(text)
    }

    fun trace(text: String = "") {
        if(pref_trace.value)
            Timber.d(text)
    }

    fun <T> Flow<T>.trace(createText: suspend (T) -> String): Flow<T> {
        return if (pref_traceFlows.value)
            onEach { traceBold(createText(it)) }
        else
            this
    }

    fun stackFrame(skip: Int = 0): StackTraceElement? {
        // >= Java 9
        return try {
            //StackWalker.getInstance().walk { stream -> stream.skip(1).findFirst().get() }
            Throwable().stackTrace[skip+1]
        } catch (e: Throwable) {
            null
        }
    }

    fun methodName(skip: Int = 0): String {
        // >= Java 9
        return try {
            //StackWalker.getInstance().walk { stream -> stream.skip(1).findFirst().get() }
            //    .getMethodName()
            val frame = stackFrame(skip+2)
            frame?.methodName ?: ""
        } catch (e: Throwable) {
            ""
        }
    }

    fun classAndMethodName(skip: Int = 0): String {
        // >= Java 9
        return try {
            //StackWalker.getInstance().walk { stream -> stream.skip(1).findFirst().get() }
            //    .getMethodName()
            val frame = stackFrame(skip+1)
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

