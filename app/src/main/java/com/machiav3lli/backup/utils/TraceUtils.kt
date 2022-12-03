package com.machiav3lli.backup.utils

import com.machiav3lli.backup.pref_trace
import com.machiav3lli.backup.traceFlows
import com.machiav3lli.backup.ui.item.BooleanPref
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

object TraceUtils {
    // only use these in implementations of other trace functions

    fun trace(text: String = "") {
        if (pref_trace.value)
            Timber.d(text)
    }

    fun traceBold(text: String = "") {
        if (pref_trace.value)
            Timber.w(text)
    }

    // use these instead to have lazy evaluation of the text in normal situations

    fun trace(lazyText: () -> String) {
        trace(lazyText())
    }

    fun traceBold(lazyText: () -> String) {
        traceBold(lazyText())
    }

    open class TracePref(name: String, summary: String, default: Boolean) {

        val pref = BooleanPref(
            key = "dev-trace.trace$name",
            summary = summary,
            defaultValue = default,
            enableIf = { pref_trace.value }
        )

        open operator fun invoke(lazyText: () -> String) {
            if (pref.value)
                trace(lazyText)
        }
    }

    class TracePrefBold(name: String, summary: String, default: Boolean) :
        TracePref(name, summary, default) {

        override operator fun invoke(lazyText: () -> String) {
            if (pref.value)
                traceBold(lazyText)
        }
    }

    fun <T> Flow<T>.trace(lazyText: (T) -> String): Flow<T> {
        return if (traceFlows.pref.value)
            onEach { traceFlows { lazyText(it) } }
        else
            this

        // speed doesn't really matter, but toggling the pref only has an effect when recreating the flow
        //return onEach { traceFlows { lazyText(it) } }

        //return this
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
            val stackTrace = Throwable().stackTrace
            var i = skip + 2
            var methodName = ""
            do {
                val frame = stackTrace[++i]
                methodName = frame?.methodName ?: ""
            } while (frame != null && (
                        methodName.contains("trace") ||
                        methodName.contains("log")
                        )
            )
            methodName
        } catch (e: Throwable) {
            ""
        }
    }

    fun classAndMethodName(skip: Int = 0): String {
        return try {
            val stackTrace = Throwable().stackTrace
            var i = skip + 2
            var className = ""
            var methodName = ""
            do {
                val frame = stackTrace[++i]
                className = frame?.className ?: ""
                methodName = frame?.methodName ?: ""
            } while (frame != null && (
                        className.contains("Log") ||
                        methodName.contains("trace") ||
                        methodName.contains("log")
                        )
            )
            "${className}::${methodName}"
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