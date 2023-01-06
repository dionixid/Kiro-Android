package id.dionix.kiro.utility

import java.util.concurrent.Executors

object Thread {
    private val executor = Executors.newCachedThreadPool()

    fun execute(runnable: Runnable) {
        executor.execute(runnable)
    }

    fun shutdown() {
        executor.shutdownNow()
    }
}