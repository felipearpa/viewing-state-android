package com.felipearpa.ui.state

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Represents the different states of a view that edits data.
 *
 * @param Value The type of data that the view edits.
 */
sealed class EditableViewState<out Value : Any> {
    /**
     * Represents the initial state of the view, before any data has been edited.
     *
     * @param Value The type of data that the view edits.
     * @property value The initial data value.
     */
    data class Initial<Value : Any>(val value: Value) : EditableViewState<Value>()

    /**
     * Represents the saving state of the view, indicating that data is currently being edited.
     *
     * @param Value The type of data that the view edits.
     * @property current The current data value.
     * @property target The target data value to be edited.
     */
    data class Saving<Value : Any>(val current: Value, val target: Value) :
        EditableViewState<Value>()

    /**
     * Represents the successful state of editing data.
     *
     * @param Value The type of data that the view edits.
     * @property old The original data value before editing.
     * @property succeeded The edited data value.
     */
    data class Success<Value : Any>(val old: Value, val succeeded: Value) :
        EditableViewState<Value>()

    /**
     * Represents the failure state of the view, containing an exception.
     *
     * @param Value The type of data that the view edits.
     * @property current The current data value.
     * @property failed The failed data value.
     */
    data class Failure<Value : Any>(
        val current: Value,
        val failed: Value,
        val exception: Throwable,
    ) : EditableViewState<Value>()
}

/**
 * Checks if the current state is [EditableViewState.Initial].
 *
 * @param Value The type of data that the view edits.
 * @return `true` if the state is [EditableViewState.Initial], `false` otherwise.
 */
@OptIn(ExperimentalContracts::class)
fun <Value : Any> EditableViewState<Value>.isInitial(): Boolean {
    contract {
        returns(true) implies (this@isInitial is EditableViewState.Initial)
    }

    return this is EditableViewState.Initial
}

/**
 * Checks if the current state is [EditableViewState.Saving].
 *
 * @param Value The type of data that the view edits.
 * @return `true` if the state is [EditableViewState.Saving], `false` otherwise.
 */
@OptIn(ExperimentalContracts::class)
fun <Value : Any> EditableViewState<Value>.isSaving(): Boolean {
    contract {
        returns(true) implies (this@isSaving is EditableViewState.Saving)
    }

    return this is EditableViewState.Saving
}

/**
 * Checks if the current state is [EditableViewState.Success].
 *
 * @param Value The type of data that the view edits.
 * @return `true` if the state is [EditableViewState.Success], `false` otherwise.
 */
@OptIn(ExperimentalContracts::class)
fun <Value : Any> EditableViewState<Value>.isSuccess(): Boolean {
    contract {
        returns(true) implies (this@isSuccess is EditableViewState.Success)
    }

    return this is EditableViewState.Success
}

/**
 * Checks if the current state is [EditableViewState.Failure].
 *
 * @param Value The type of data that the view edits.
 * @return `true` if the state is [EditableViewState.Failure], `false` otherwise.
 */
@OptIn(ExperimentalContracts::class)
fun <Value : Any> EditableViewState<Value>.isFailure(): Boolean {
    contract {
        returns(true) implies (this@isFailure is EditableViewState.Failure)
    }

    return this is EditableViewState.Failure
}

/**
 * Executes the given [block] if the current state is [EditableViewState.Initial].
 *
 * @param Value The type of data that the view edits.
 * @param block The block of code to execute.
 * @return The original [EditableViewState] instance.
 */
fun <Value : Any> EditableViewState<Value>.onInitial(block: (value: Value) -> Unit): EditableViewState<Value> {
    if (this.isInitial()) {
        block(this.value)
    }
    return this
}

/**
 * Executes the given [block] if the current state is [EditableViewState.Saving].
 *
 * @param Value The type of data that the view edits.
 * @param block The block of code to execute.
 * @return The original [EditableViewState] instance.
 */
fun <Value : Any> EditableViewState<Value>.onSaving(block: (current: Value, target: Value) -> Unit): EditableViewState<Value> {
    if (this.isSaving()) {
        block(this.current, this.target)
    }
    return this
}

/**
 * Executes the given [block] if the current state is [EditableViewState.Success].
 *
 * @param Value The type of data that the view edits.
 * @param block The block of code to execute.
 * @return The original [EditableViewState] instance.
 */
fun <Value : Any> EditableViewState<Value>.onSuccess(block: (old: Value, succeeded: Value) -> Unit): EditableViewState<Value> {
    if (this.isSuccess()) {
        block(this.old, this.succeeded)
    }
    return this
}

/**
 * Executes the given [block] if the current state is [EditableViewState.Failure].
 *
 * @param Value The type of data that the view edits.
 * @param block The block of code to execute.
 * @return The original [EditableViewState] instance.
 */
fun <Value : Any> EditableViewState<Value>.onFailure(block: (current: Value, failed: Value, exception: Throwable) -> Unit): EditableViewState<Value> {
    if (this.isFailure()) {
        block(this.current, this.failed, this.exception)
    }
    return this
}

/**
 * Returns the value if the current state is [EditableViewState.Success], or `null` otherwise.
 *
 * @param Value The type of data that the view edits.
 * @return The value if the state is [EditableViewState.Success], `null` otherwise.
 */
fun <Value : Any> EditableViewState<Value>.exceptionOrNull(): Throwable? {
    return when (this) {
        is EditableViewState.Failure -> this.exception
        else -> null
    }
}

/**
 * Returns the value if the current state is [EditableViewState.Success], or throws an [IllegalStateException] otherwise.
 *
 * @param Value The type of data that the view edits.
 * @return The value if the state is [EditableViewState.Success], or throws an [IllegalStateException] otherwise.
 */
fun <Value : Any> EditableViewState<Value>.relevantValue() =
    when (this) {
        is EditableViewState.Initial -> this.value
        is EditableViewState.Saving -> this.current
        is EditableViewState.Success -> this.succeeded
        is EditableViewState.Failure -> this.current
    }
