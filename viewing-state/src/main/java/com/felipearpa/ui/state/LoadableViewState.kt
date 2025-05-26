package com.felipearpa.ui.state

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Represents the different states of a view that loads data.
 *
 * @param Value The type of data that the view loads.
 */
sealed class LoadableViewState<out Value : Any> {
    /**
     * Represents the initial state of the view, before any data has been loaded.
     */
    data object Initial : LoadableViewState<Nothing>()

    /**
     * Represents the loading state of the view, indicating that data is currently being fetched or processed.
     */
    data object Loading : LoadableViewState<Nothing>()

    /**
     * Represents the successful state of loading data.
     *
     * @param Value The type of data that was successfully loaded.
     * @property value The successfully loaded data.
     */
    data class Success<Value : Any>(val value: Value) : LoadableViewState<Value>() {
        operator fun invoke(): Value = value
    }

    /**
     * Represents the failure state of the view, containing an exception.
     *
     * @property exception The throwable that caused the failure.
     */
    data class Failure(val exception: Throwable) : LoadableViewState<Nothing>() {
        operator fun invoke(): Throwable = exception
    }
}

/**
 * Checks if the current state is [LoadableViewState.Initial].
 *
 * @param Value The type of data that the view loads.
 * @return `true` if the state is [LoadableViewState.Initial], `false` otherwise.
 */
@OptIn(ExperimentalContracts::class)
fun <Value : Any> LoadableViewState<Value>.isInitial(): Boolean {
    contract {
        returns(true) implies (this@isInitial is LoadableViewState.Initial)
    }

    return this is LoadableViewState.Initial
}

/**
 * Checks if the current state is [LoadableViewState.Loading].
 *
 * @param Value The type of data that the view loads.
 * @return `true` if the state is [LoadableViewState.Loading], `false` otherwise.
 */
@OptIn(ExperimentalContracts::class)
fun <Value : Any> LoadableViewState<Value>.isLoading(): Boolean {
    contract {
        returns(true) implies (this@isLoading is LoadableViewState.Loading)
    }

    return this is LoadableViewState.Loading
}

/**
 * Checks if the current state is [LoadableViewState.Success].
 *
 * @param Value The type of data that the view loads.
 * @return `true` if the state is [LoadableViewState.Success], `false` otherwise.
 */
@OptIn(ExperimentalContracts::class)
fun <Value : Any> LoadableViewState<Value>.isSuccess(): Boolean {
    contract {
        returns(true) implies (this@isSuccess is LoadableViewState.Success)
    }

    return this is LoadableViewState.Success
}

/**
 * Checks if the current state is [LoadableViewState.Failure].
 *
 * @param Value The type of data that the view loads.
 * @return `true` if the state is [LoadableViewState.Failure], `false` otherwise.
 */
@OptIn(ExperimentalContracts::class)
fun <Value : Any> LoadableViewState<Value>.isFailure(): Boolean {
    contract {
        returns(true) implies (this@isFailure is LoadableViewState.Failure)
    }

    return this is LoadableViewState.Failure
}

/**
 * Executes the given [block] if the current state is [LoadableViewState.Initial].
 *
 * @param Value The type of data that the view loads.
 * @param block The block of code to execute.
 * @return The original [LoadableViewState] instance.
 */
inline fun <Value : Any> LoadableViewState<Value>.onInitial(block: () -> Unit): LoadableViewState<Value> {
    if (isInitial()) {
        block()
    }
    return this
}

/**
 * Executes the given [block] if the current state is [LoadableViewState.Loading].
 *
 * @param Value The type of data that the view loads.
 * @param block The block of code to execute.
 * @return The original [LoadableViewState] instance.
 */
inline fun <Value : Any> LoadableViewState<Value>.onLoading(block: () -> Unit): LoadableViewState<Value> {
    if (isLoading()) {
        block()
    }
    return this
}

/**
 * Executes the given [block] if the current state is [LoadableViewState.Success].
 *
 * @param Value The type of data that the view loads.
 * @param block The block of code to execute.
 * @return The original [LoadableViewState] instance.
 */
inline fun <Value : Any> LoadableViewState<Value>.onSuccess(block: (value: Value) -> Unit): LoadableViewState<Value> {
    if (isSuccess()) {
        block(this.invoke())
    }
    return this
}

/**
 * Executes the given [block] if the current state is [LoadableViewState.Failure].
 *
 * @param Value The type of data that the view loads.
 * @param block The block of code to execute.
 * @return The original [LoadableViewState] instance.
 */
inline fun <Value : Any> LoadableViewState<Value>.onFailure(block: (throwable: Throwable) -> Unit): LoadableViewState<Value> {
    if (isFailure()) {
        block(this.invoke())
    }
    return this
}

/**
 * Returns the value if the current state is [LoadableViewState.Success], or `null` otherwise.
 *
 * @param Value The type of data that the view loads.
 * @return The value if the state is [LoadableViewState.Success], `null` otherwise.
 */
fun <Value : Any> LoadableViewState<Value>.valueOrNull(): Value? {
    if (isSuccess())
        return this.invoke()
    return null
}

/**
 * Returns the value if the current state is [LoadableViewState.Success], or throws an [IllegalStateException] otherwise.
 *
 * @param Value The type of data that the view loads.
 * @return The value if the state is [LoadableViewState.Success], or throws an [IllegalStateException] otherwise.
 */
fun <Value : Any> LoadableViewState<Value>.valueOrThrow(): Value {
    if (isSuccess())
        return this.invoke()
    throw IllegalStateException("Expected value in Success state but not found")
}

/**
 * Returns the exception if the current state is [LoadableViewState.Failure], or `null` otherwise.
 *
 * @param Value The type of data that the view loads.
 * @return The exception if the state is [LoadableViewState.Failure], `null` otherwise.
 */
fun <Value : Any> LoadableViewState<Value>.exceptionOrNull(): Throwable? {
    if (isFailure())
        return this.invoke()
    return null
}

/**
 * Returns the exception if the current state is [LoadableViewState.Failure], or throws an [IllegalStateException] otherwise.
 *
 * @param Value The type of data that the view loads.
 * @return The exception if the state is [LoadableViewState.Failure], or throws an [IllegalStateException] otherwise.
 */
fun <Value : Any> LoadableViewState<Value>.exceptionOrThrow(): Throwable {
    if (isFailure())
        return this.invoke()
    throw IllegalStateException("Expected exception in Failure state but not found")
}
