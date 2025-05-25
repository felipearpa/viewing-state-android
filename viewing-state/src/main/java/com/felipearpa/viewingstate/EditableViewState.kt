package com.felipearpa.viewingstate

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class EditableViewState<out Value : Any> {
    data class Initial<Value : Any>(val value: Value) : EditableViewState<Value>()

    data class Loading<Value : Any>(val current: Value, val target: Value) :
        EditableViewState<Value>()

    data class Success<Value : Any>(val old: Value, val succeeded: Value) :
        EditableViewState<Value>()

    data class Failure<Value : Any>(
        val current: Value,
        val failed: Value,
        val exception: Throwable
    ) : EditableViewState<Value>()
}

@OptIn(ExperimentalContracts::class)
fun <Value : Any> EditableViewState<Value>.isInitial(): Boolean {
    contract {
        returns(true) implies (this@isInitial is EditableViewState.Initial)
    }

    return this is EditableViewState.Initial
}

@OptIn(ExperimentalContracts::class)
fun <Value : Any> EditableViewState<Value>.isLoading(): Boolean {
    contract {
        returns(true) implies (this@isLoading is EditableViewState.Loading)
    }

    return this is EditableViewState.Loading
}

@OptIn(ExperimentalContracts::class)
fun <Value : Any> EditableViewState<Value>.isSuccess(): Boolean {
    contract {
        returns(true) implies (this@isSuccess is EditableViewState.Success)
    }

    return this is EditableViewState.Success
}

@OptIn(ExperimentalContracts::class)
fun <Value : Any> EditableViewState<Value>.isFailure(): Boolean {
    contract {
        returns(true) implies (this@isFailure is EditableViewState.Failure)
    }

    return this is EditableViewState.Failure
}

fun <Value : Any> EditableViewState<Value>.onInitial(block: (value: Value) -> Unit): EditableViewState<Value> {
    if (this.isInitial()) {
        block(this.value)
    }
    return this
}

fun <Value : Any> EditableViewState<Value>.onLoading(block: (current: Value, target: Value) -> Unit): EditableViewState<Value> {
    if (this.isLoading()) {
        block(this.current, this.target)
    }
    return this
}

fun <Value : Any> EditableViewState<Value>.onSuccess(block: (old: Value, succeeded: Value) -> Unit): EditableViewState<Value> {
    if (this.isSuccess()) {
        block(this.old, this.succeeded)
    }
    return this
}

fun <Value : Any> EditableViewState<Value>.onFailure(block: (current: Value, failed: Value, exception: Throwable) -> Unit): EditableViewState<Value> {
    if (this.isFailure()) {
        block(this.current, this.failed, this.exception)
    }
    return this
}

fun <Value : Any> EditableViewState<Value>.exceptionOrNull(): Throwable? {
    return when (this) {
        is EditableViewState.Failure -> this.exception
        else -> null
    }
}

fun <Value : Any> EditableViewState<Value>.relevantValue() =
    when (this) {
        is EditableViewState.Initial -> this.value
        is EditableViewState.Loading -> this.current
        is EditableViewState.Success -> this.succeeded
        is EditableViewState.Failure -> this.current
    }