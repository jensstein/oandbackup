package research

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.system.measureTimeMillis

class Try_concurrent {

    val n = 10  // number of jobs to launch
    val t = 100L // time to wait in each coroutine

    private var theMap = mutableMapOf<Int, Int>()

    fun massiveCoroutines(dispatcher: CoroutineDispatcher, action: suspend () -> Unit) {
        val time = measureTimeMillis {
            runBlocking(dispatcher) {
                repeat(n) {
                    launch {
                        action()
                    }
                }
            }
        }
        println("----------> Completed actions in $n coroutines in $time ms")
    }

    fun massiveThreads(action: () -> Unit) {
        val jobs = mutableListOf<Thread>()
        val time = measureTimeMillis {
            repeat(n) { index ->
                jobs.add(
                    Thread {
                        action()
                    }.also { it.start() }
                )
            }
            jobs.forEach { it.join() }
        }
        println("----------> Completed actions in $n threads in $time ms")
    }

    fun theAction() {
        val x = counter
        //theMap[x] = (theMap[x] ?: 0) + 1
        Thread.sleep(t)
        counter = x+1
    }

    fun theAssertions() {
        assertEquals(n, counter)
        //repeat(n) {
        //    assertEquals(1, theMap[it])
        //}
    }

    var mutex = Mutex()
    object lock
    var counter = 0

    @Test
    fun test_coroutine_withLock(){
        counter = 0
        massiveCoroutines(Dispatchers.Default) {
                mutex.withLock {
                theAction()
            }
        }
        assertEquals(n, counter)
    }

    @Test
    fun test_coroutine_io_withLock() {
        counter = 0
        massiveCoroutines(Dispatchers.IO) {
            mutex.withLock {
                theAction()
            }
        }
        assertEquals(n, counter)
    }

    @Test
    fun test_thread_withLock() {
        counter = 0
        massiveThreads {
            runBlocking {
                mutex.withLock {
                    theAction()
                }
            }
        }
        assertEquals(n, counter)
    }

    @Test
    fun test_thread_synchronized(){
        counter = 0
        massiveThreads {
            synchronized(lock) {
                theAction()
            }
        }
        theAssertions()
    }

    @Test
    fun test_coroutine_synchronized(){
        counter = 0
        massiveCoroutines(Dispatchers.Default) {
            synchronized(lock) {
                theAction()
            }
        }
        theAssertions()
    }

    @Test
    fun test_coroutine_io_synchronized(){
        counter = 0
        massiveCoroutines(Dispatchers.IO) {
            synchronized(lock) {
                theAction()
            }
        }
        theAssertions()
    }

}
