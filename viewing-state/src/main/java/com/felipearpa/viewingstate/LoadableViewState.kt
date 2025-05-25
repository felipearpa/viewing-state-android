package com.felipearpa.viewingstate

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class LoadableViewState<out Value : Any> {
    data object Initial : LoadableViewState<Nothing>()

    data object Loading : LoadableViewState<Nothing>()

    data class Success<Value : Any>(val value: Value) : LoadableViewState<Value>() {
        operator fun invoke(): Value = value
    }

    data class Failure(val exception: Throwable) : LoadableViewState<Nothing>() {
        operator fun invoke(): Throwable = exception
    }
}

@OptIn(ExperimentalContracts::class)
fun <Value : Any> LoadableViewState<Value>.isInitial(): Boolean {
    contract {
        returns(true) implies (this@isInitial is LoadableViewState.Initial)
    }

    return this is LoadableViewState.Initial
}

@OptIn(ExperimentalContracts::class)
fun <Value : Any> LoadableViewState<Value>.isLoading(): Boolean {
    contract {
        returns(true) implies (this@isLoading is LoadableViewState.Loading)
    }

    return this is LoadableViewState.Loading
}

@OptIn(ExperimentalContracts::class)
fun <Value : Any> LoadableViewState<Value>.isSuccess(): Boolean {
    contract {
        returns(true) implies (this@isSuccess is LoadableViewState.Success)
    }

    return this is LoadableViewState.Success
}

@OptIn(ExperimentalContracts::class)
fun <Value : Any> LoadableViewState<Value>.isFailure(): Boolean {
    contract {
        returns(true) implies (this@isFailure is LoadableViewState.Failure)
    }

    return this is LoadableViewState.Failure
}

inline fun <T : Any> LoadableViewState<T>.onInitial(block: () -> Unit): LoadableViewState<T> {
    if (isInitial()) {
        block()
    }
    return this
}

inline fun <Value : Any> LoadableViewState<Value>.onLoading(block: () -> Unit): LoadableViewState<Value> {
    if (isLoading()) {
        block()
    }
    return this
}

inline fun <Value : Any> LoadableViewState<Value>.onSuccess(block: (value: Value) -> Unit): LoadableViewState<Value> {
    if (isSuccess()) {
        block(this.invoke())
    }
    return this
}

inline fun <Value : Any> LoadableViewState<Value>.onFailure(block: (throwable: Throwable) -> Unit): LoadableViewState<Value> {
    if (isFailure()) {
        block(this.invoke())
    }
    return this
}

fun <Value : Any> LoadableViewState<Value>.valueOrNull(): Value? {
    if (isSuccess())
        return this.invoke()
    return null
}

fun <Value : Any> LoadableViewState<Value>.valueOrThrow(): Value {
    if (isSuccess())
        return this.invoke()
    throw IllegalStateException("Expected value in Success state but not found")
}

fun <Value : Any> LoadableViewState<Value>.exceptionOrNull(): Throwable? {
    if (isFailure())
        return this.invoke()
    return null
}

fun <Value : Any> LoadableViewState<Value>.exceptionOrThrow(): Throwable {
    if (isFailure())
        return this.invoke()
    throw IllegalStateException("Expected exception in Failure state but not found")
}
