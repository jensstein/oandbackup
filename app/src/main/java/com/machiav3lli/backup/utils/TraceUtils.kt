package com.machiav3lli.backup.utils

object TraceUtils {

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

