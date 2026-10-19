package com.machiav3lli.backup.utils.extensions

import androidx.compose.ui.graphics.painter.Painter
import com.machiav3lli.backup.data.entity.Package
import com.machiav3lli.backup.data.preferences.traceDebug

object IconCache {

    private var painterCache = mutableMapOf<Any, Painter>()

    fun getIcon(key: Any): Painter? {
        return synchronized(painterCache) {
            painterCache[key]
        }
    }

    fun putIcon(key: Any, painter: Painter) {
        //traceDebug { "icon put $key" }
        synchronized(painterCache) {
            painterCache.put(key, painter)
        }
    }

    fun removeIcon(key: Any) {
        traceDebug { "icon remove $key" }
        synchronized(painterCache) {
            painterCache.remove(key)
        }
    }

    fun clear() {
        synchronized(painterCache) {
            painterCache.clear()
        }
    }

    fun dropAllButUsed(pkgs: List<Package>) {
        val used = pkgs.map { it.iconData }.toSet()
        //beginNanoTimer("limitIconCache")
        val keys = synchronized(painterCache) { painterCache.keys.toSet() }
        (keys - used).forEach {
            if (it !is Int) {
                removeIcon(it)
            }
        }
        //endNanoTimer("limitIconCache")
    }

    val size: Int
        get() {
            return synchronized(painterCache) {
                painterCache.size
            }
        }
}