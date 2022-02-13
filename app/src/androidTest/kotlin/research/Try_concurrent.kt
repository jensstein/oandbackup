package research

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.system.measureTimeMillis

class Try_concurrent {

    val n = 10  // number of coroutines to launch
    val t = 100L // times an action is repeated by each coroutine

    suspend fun massiveCoroutines(action: suspend () -> Unit) {
        val time = measureTimeMillis {
            coroutineScope { // scope for coroutines
                repeat(n) {
                    launch {
                        action()
                    }
                }
            }
        }
        println("Completed $n actions in $time ms")
    }

    fun massiveThreads(action: () -> Unit) {
        val jobs = mutableListOf<Thread>()
        val time = measureTimeMillis {
            repeat(n) {
                jobs.add(
                    Thread {
                        action()
                    }.also { it.start() }
                )
            }
            jobs.forEach { it.join() }
        }
        println("Completed $n actions in $time ms")
    }

    var mutex = Mutex()
    var counter = 0

    @Test
    fun test_coroutine_withLock() = runBlocking {
        counter = 0
        withContext(Dispatchers.Default) {
            massiveCoroutines {
                // protect each increment
                mutex.withLock {
                    val x = counter
                    delay(1000)
                    counter = x+1
                }
            }
        }
        assertEquals(n, counter)
    }

    @Test
    fun test_thread_synchronized() = runBlocking {
        counter = 0
        massiveThreads {
            // protect each increment
            synchronized(counter) {
                val x = counter
                Thread.sleep(t)
                counter = x+1
            }
        }
        assertEquals(n, counter)
    }

}
