package com.felipearpa.ui.state

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Represents the different states of a view that saves data.
 *
 * @param Value The type of data that the view saves.
 */
sealed class SavableViewState<out Value : Any> {
    /**
     * Represents the initial state of the view, before any data has been saved.
     */
    data object Initial : SavableViewState<Nothing>()

    /**
     * Represents the saving state of the view, indicating that data is currently being stored or processed.
     *
     * @param Value The type of data that is being saved.
     * @property value The data that is being saved.
     */
    data class Saving<Value : Any>(val value: Value) : SavableViewState<Value>() {
        operator fun invoke(): Value = value
    }

    /**
     * Represents the successful state of saving data.
     */
    data object Success : SavableViewState<Nothing>()

    /**
     * Represents the failure state of the view, containing an exception.
     *
     * @property exception The throwable that caused the failure.
     */
    data class Failure(val exception: Throwable) : SavableViewState<Nothing>() {
        operator fun invoke(): Throwable = exception
    }
}

/**
 * Checks if the current state is [SavableViewState.Initial].
 *
 * @param Value The type of data that the view saves.
 * @return `true` if the state is [SavableViewState.Initial], `false` otherwise.
 */
@OptIn(ExperimentalContracts::class)
fun <Value : Any> SavableViewState<Value>.isInitial(): Boolean {
    contract {
        returns(true) implies (this@isInitial is SavableViewState.Initial)
    }

    return this is SavableViewState.Initial
}

/**
 * Checks if the current state is [SavableViewState.Saving].
 *
 * @param Value The type of data that the view saves.
 * @return `true` if the state is [SavableViewState.Saving], `false` otherwise.
 */
@OptIn(ExperimentalContracts::class)
fun <Value : Any> SavableViewState<Value>.isSaving(): Boolean {
    contract {
        returns(true) implies (this@isSaving is SavableViewState.Saving)
    }

    return this is SavableViewState.Saving
}

/**
 * Checks if the current state is [SavableViewState.Success].
 *
 * @param Value The type of data that the view saves.
 * @return `true` if the state is [SavableViewState.Success], `false` otherwise.
 */
@OptIn(ExperimentalContracts::class)
fun <Value : Any> SavableViewState<Value>.isSuccess(): Boolean {
    contract {
        returns(true) implies (this@isSuccess is SavableViewState.Success)
    }

    return this is SavableViewState.Success
}

/**
 * Checks if the current state is [SavableViewState.Failure].
 *
 * @param Value The type of data that the view saves.
 * @return `true` if the state is [SavableViewState.Failure], `false` otherwise.
 */
@OptIn(ExperimentalContracts::class)
fun <Value : Any> SavableViewState<Value>.isFailure(): Boolean {
    contract {
        returns(true) implies (this@isFailure is SavableViewState.Failure)
    }

    return this is SavableViewState.Failure
}

/**
 * Executes the given [block] if the current state is [SavableViewState.Initial].
 *
 * @param Value The type of data that the view saves.
 * @param block The block of code to execute.
 * @return The original [SavableViewState] instance.
 */
inline fun <Value : Any> SavableViewState<Value>.onInitial(block: () -> Unit): SavableViewState<Value> {
    if (isInitial()) {
        block()
    }
    return this
}

/**
 * Executes the given [block] if the current state is [SavableViewState.Saving].
 *
 * @param Value The type of data that the view saves.
 * @param block The block of code to execute.
 * @return The original [SavableViewState] instance.
 */
inline fun <Value : Any> SavableViewState<Value>.onSaving(block: (Value) -> Unit): SavableViewState<Value> {
    if (isSaving()) {
        block(this())
    }
    return this
}

/**
 * Executes the given [block] if the current state is [SavableViewState.Success].
 *
 * @param Value The type of data that the view saves.
 * @param block The block of code to execute.
 * @return The original [SavableViewState] instance.
 */
inline fun <Value : Any> SavableViewState<Value>.onSuccess(block: () -> Unit): SavableViewState<Value> {
    if (isSuccess()) {
        block()
    }
    return this
}

/**
 * Executes the given [block] if the current state is [SavableViewState.Failure].
 *
 * @param Value The type of data that the view saves.
 * @param block The block of code to execute.
 * @return The original [SavableViewState] instance.
 */
inline fun <Value : Any> SavableViewState<Value>.onFailure(block: (throwable: Throwable) -> Unit): SavableViewState<Value> {
    if (isFailure()) {
        block(this())
    }
    return this
}

/**
 * Returns the value if the current state is [SavableViewState.Saving], or `null` otherwise.
 *
 * @param Value The type of data that the view saves.
 * @return The value if the state is [SavableViewState.Saving], `null` otherwise.
 */
fun <Value : Any> SavableViewState<Value>.valueOrNull(): Value? {
    if (isSaving())
        return this()
    return null
}

/**
 * Returns the value if the current state is [SavableViewState.Saving], or throws an [IllegalStateException] otherwise.
 *
 * @param Value The type of data that the view saves.
 * @return The value if the state is [SavableViewState.Saving], or throws an [IllegalStateException] otherwise.
 */
fun <Value : Any> SavableViewState<Value>.valueOrThrow(): Value {
    if (isSaving())
        return this()
    throw IllegalStateException("Expected value in Saving state but not found")
}

/**
 * Returns the exception if the current state is [SavableViewState.Failure], or `null` otherwise.
 *
 * @param Value The type of data that the view saves.
 * @return The exception if the state is [SavableViewState.Failure], `null` otherwise.
 */
fun <Value : Any> SavableViewState<Value>.exceptionOrNull(): Throwable? {
    if (isFailure())
        return this()
    return null
}

/**
 * Returns the exception if the current state is [SavableViewState.Failure], or throws an [IllegalStateException] otherwise.
 *
 * @param Value The type of data that the view saves.
 * @return The exception if the state is [SavableViewState.Failure], or throws an [IllegalStateException] otherwise.
 */
fun <Value : Any> SavableViewState<Value>.exceptionOrThrow(): Throwable {
    if (isFailure())
        return this()
    throw IllegalStateException("Expected exception in Failure state but not found")
}

/**
 * Maps the value of the [SavableViewState] to a new value of type [NewValue].
 *
 * @param Value The type of data that the view saves.
 * @param NewValue The type of data that the view saves.
 * @param block The function to apply to the value.
 * @return A new [SavableViewState] with the mapped value.
 */
inline fun <Value : Any, NewValue : Any> SavableViewState<Value>.map(block: (value: Value) -> NewValue): SavableViewState<NewValue> {
    return when (this) {
        SavableViewState.Initial -> SavableViewState.Initial
        is SavableViewState.Saving -> SavableViewState.Saving(block(value))
        SavableViewState.Success -> SavableViewState.Success
        is SavableViewState.Failure -> SavableViewState.Failure(exception)
    }
}

/**
 * Folds the [SavableViewState] into a new value of type [NewValue].
 *
 * @param Value The type of data that the view saves.
 * @param NewValue The type of data that the view saves.
 * @param onInitial The function to apply to the initial state.
 * @param onSaving The function to apply to the saving state.
 * @param onSuccess The function to apply to the success state.
 * @param onFailure The function to apply to the failure state.
 * @return A new value of type [NewValue].
 */
inline fun <Value : Any, NewValue> SavableViewState<Value>.fold(
    onInitial: () -> NewValue,
    onSaving: (Value) -> NewValue,
    onSuccess: () -> NewValue,
    onFailure: (throwable: Throwable) -> NewValue,
): NewValue {
    return when (this) {
        SavableViewState.Initial -> onInitial()
        is SavableViewState.Saving -> onSaving(value)
        SavableViewState.Success -> onSuccess()
        is SavableViewState.Failure -> onFailure(exception)
    }
}
