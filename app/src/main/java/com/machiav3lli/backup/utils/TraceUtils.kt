package com.machiav3lli.backup.utils

import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.pref_trace
import com.machiav3lli.backup.traceFlows
import com.machiav3lli.backup.traceTiming
import com.machiav3lli.backup.ui.item.BooleanPref
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

object TraceUtils {

    // only use these in implementations of other trace functions

    fun traceImpl(text: String = "") {
        if (pref_trace.value)
            Timber.d(text)
    }

    fun traceBoldImpl(text: String = "") {
        if (pref_trace.value)
            Timber.w(text)
    }

    fun traceExtremeImpl(text: String = "") {
        if (pref_trace.value)
            Timber.e("# # # # # # # # # # # # # # # # # # # # " + text)
    }

    // use these instead to have lazy evaluation of the text in normal situations

    fun trace(lazyText: () -> String) {
        traceImpl(lazyText())
    }

    fun traceBold(lazyText: () -> String) {
        traceBoldImpl(lazyText())
    }

    fun traceExtreme(lazyText: () -> String) {
        traceExtremeImpl(lazyText())
    }

    open class TracePref(
        val name: String,
        summary: String,
        default: Boolean,
        enableIf: () -> Boolean = { true },
    ) {

        val pref = BooleanPref(
            key = "dev-trace.trace$name",
            summary = summary,
            defaultValue = default,
            enableIf = { pref_trace.value && enableIf() }
        )

        open operator fun invoke(lazyText: () -> String) {
            if (pref.value)
                traceImpl("[$name] ${lazyText()}")
        }
    }

    class TracePrefBold(name: String, summary: String, default: Boolean) :
        TracePref(name, summary, default) {

        override operator fun invoke(lazyText: () -> String) {
            if (pref.value)
                traceBoldImpl("[$name] ${lazyText()}")
        }
    }

    class TracePrefExtreme(name: String, summary: String, default: Boolean) :
        TracePref(name, summary, default) {

        override operator fun invoke(lazyText: () -> String) {
            if (pref.value)
                traceExtremeImpl("[$name] ${lazyText()}")
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


    // timers

    var nanoTimers = mutableMapOf<String, Long>()
    var nanoTime = mutableMapOf<String, Long>()
    var nanoTiming = mutableMapOf<String, Pair<Float, Long>>()

    fun beginNanoTimer(name: String) {
        synchronized(nanoTimers) {
            nanoTimers.put(name, System.nanoTime())
        }
    }

    val warmup = 3L

    fun endNanoTimer(name: String): Long {
        synchronized(nanoTimers) {
            var t = System.nanoTime()
            nanoTimers.get(name)?.let {
                t -= it
                nanoTime.put(name, t)
                val (average, n) = nanoTiming.getOrPut(name) { 0f to -warmup }
                if (n < 0L)
                    nanoTiming.put(name, t.toFloat() to (n + 1)) // on warmup use single time
                else
                    nanoTiming.put(name, ((average * n + t) / (n + 1)) to (n + 1)) // resets for n=0
                return t
            }
            return 0L
        }
    }

    fun clearNanoTiming(pattern: String = "") {
        synchronized(nanoTimers) {
            nanoTimers = nanoTimers.filterNot { it.key.contains(pattern) }.toMutableMap()
            nanoTime = nanoTime.filterNot { it.key.contains(pattern) }.toMutableMap()
            nanoTiming = nanoTiming.filterNot { it.key.contains(pattern) }.toMutableMap()
        }
    }

    fun listNanoTiming(pattern: String = ""): List<String> {
        return nanoTiming
            .toSortedMap()
            .filterKeys { it.contains(pattern) }
            .map { (name, average_n) ->
                val (average, n) = average_n
                "%-40s %6d x %12.6f ms".format(name, n, average / 1E6)
            }
    }

    fun logNanoTiming(pattern: String = "", title: String = "") {
        if (traceTiming.pref.value) {
            traceTiming { """$title -----\""" }
            listNanoTiming(pattern).forEach {
                traceTiming { "$title $it" }
            }
            traceTiming { """$title -----/""" }
        }
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


    // helpers

    fun formatBackups(backups: List<Backup>?): String {
        return "(${backups?.size ?: 0})${
            backups?.map {
                "${
                    it.backupDate
                }${
                    if (it.persistent) "🔒" else ""
                }"
            }
                ?: "<null>"
        }"
    }

    fun formatSortedBackups(backups: List<Backup>?): String {
        return "(${backups?.size ?: 0})${
            backups?.map {
                "${
                    it.backupDate
                }${
                    if (it.persistent) "🔒" else ""
                }"
            }
                ?.sortedDescending()
                ?: "<null>"
        }"
    }
}