package com.elpassion.weather.utils

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Suppress("unused")
val Any?.unit
    get() = Unit

operator fun StringBuilder.plusAssign(string: String) = append(string).unit

fun <T> List<T>.changes(destination: MutableList<Pair<T, T>> = ArrayList(size))
        : MutableList<Pair<T, T>> {
    for (i in 0..size - 2)
        destination += get(i) to get(i + 1)
    return destination
}

/**
 * @throws Throwable
 */
suspend fun <T> Call<T>.await(): T = suspendCancellableCoroutine { continuation ->

    continuation.invokeOnCancellation { cancel() }

    val callback = object : Callback<T> {
        override fun onFailure(call: Call<T>, t: Throwable) = continuation.tryResume { throw t }
        override fun onResponse(call: Call<T>, response: Response<T>) = continuation.tryResume {
            response.isSuccessful || throw IllegalStateException("Http error ${response.code()}")
            response.body() ?: throw IllegalStateException("Response body is null")
        }
    }

    enqueue(callback)
}

private fun <T> CancellableContinuation<T>.tryResume(getter: () -> T) {
    try {
        resume(getter())
    } catch (exception: Throwable) {
        resumeWithException(exception)
    }
}
