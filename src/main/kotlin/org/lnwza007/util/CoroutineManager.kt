package org.lnwza007.util

import io.micronaut.context.annotation.Factory
import kotlinx.coroutines.*


@Factory
@OptIn(ExperimentalCoroutinesApi::class)
object CoroutineManager {

    suspend fun <T> parallelIO(parallelism: Int = 32, block: suspend CoroutineScope.() -> T): T {
        return withContext(Dispatchers.IO.limitedParallelism(parallelism) + SupervisorJob() + CoroutineName("ParallelIO")) {
            block.invoke(this)
        }
    }


    suspend fun <T> parallelDefault(parallelism: Int = 32, block: suspend CoroutineScope.() -> T): T {
        return withContext(Dispatchers.Default.limitedParallelism(parallelism) + SupervisorJob()) {
            block.invoke(this)
        }
    }

    suspend fun <T> parallelMain(block: suspend CoroutineScope.() -> T): T {
        return withContext(Dispatchers.Main) {
            block.invoke(this)
        }
    }

    suspend fun <T> parallelUnconfined(block: suspend CoroutineScope.() -> T): T {
        return withContext(Dispatchers.Unconfined) {
            block.invoke(this)
        }
    }

}
