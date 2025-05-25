package com.felipearpa.ui.state

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class LoadableViewStateTest {
    @Test
    fun `given an initial state when checked if initial then initial is confirmed`() {
        val viewState = LoadableViewState.Initial
        val isInitial = viewState.isInitial()
        isInitial.shouldBeTrue()
    }

    @TestFactory
    fun `given a not initial state when checked if initial then initial is not confirmed`() =
        listOf(
            LoadableViewState.Loading,
            LoadableViewState.Failure(RuntimeException()),
            LoadableViewState.Success(Unit)
        ).map { viewState ->
            dynamicTest("given a state of $viewState when checked if initial then initial is not confirmed") {
                val isInitial = viewState.isInitial()
                isInitial.shouldBeFalse()
            }
        }

    @Test
    fun `given a loading state when checked if loading then loading is confirmed`() {
        val viewState = LoadableViewState.Loading
        val isLoading = viewState.isLoading()
        isLoading.shouldBeTrue()
    }

    @TestFactory
    fun `given a not loading state when checked if loading then loading is not confirmed`() =
        listOf(
            LoadableViewState.Initial,
            LoadableViewState.Failure(RuntimeException()),
            LoadableViewState.Success(Unit)
        ).map { viewState ->
            dynamicTest("given a state of $viewState when checked if loading then loading is not confirmed") {
                val isLoading = viewState.isLoading()
                isLoading.shouldBeFalse()
            }
        }

    @Test
    fun `given a failure state when checked if failure then failure is confirmed`() {
        val viewState = LoadableViewState.Failure(RuntimeException())
        val isFailure = viewState.isFailure()
        isFailure.shouldBeTrue()
    }

    @TestFactory
    fun `given a not failure state when checked if failure then failure is not confirmed`() =
        listOf(
            LoadableViewState.Initial,
            LoadableViewState.Loading,
            LoadableViewState.Success(Unit)
        ).map { viewState ->
            dynamicTest("given a state of $viewState when checked if failure then failure is not confirmed") {
                val isFailure = viewState.isFailure()
                isFailure.shouldBeFalse()
            }
        }

    @Test
    fun `given a success state when checked if success then success is confirmed`() {
        val viewState = LoadableViewState.Success(Unit)
        val isSuccess = viewState.isSuccess()
        isSuccess.shouldBeTrue()
    }

    @TestFactory
    fun `given a not success state when checked if success then success is not confirmed`() =
        listOf(
            LoadableViewState.Initial,
            LoadableViewState.Loading,
            LoadableViewState.Failure(RuntimeException())
        ).map { viewState ->
            dynamicTest("given a state of $viewState when checked if success then success is not confirmed") {
                val isSuccess = viewState.isSuccess()
                isSuccess.shouldBeFalse()
            }
        }

    @Test
    fun `given an initial state when checked with isInitial then state is smart cast to Initial`() {
        val state: LoadableViewState<String> = LoadableViewState.Initial

        if (state.isInitial()) {
            // If it compiles, the contract is working correctly
            val castedState: LoadableViewState.Initial = state
            castedState.shouldBeInstanceOf<LoadableViewState.Initial>()
        }
    }

    @Test
    fun `given a loading state when checked with isLoading then state is smart cast to Loading`() {
        val state: LoadableViewState<String> = LoadableViewState.Loading

        if (state.isLoading()) {
            // If it compiles, the contract is working correctly
            val castedState: LoadableViewState.Loading = state
            castedState.shouldBeInstanceOf<LoadableViewState.Loading>()
        }
    }

    @Test
    fun `given a success state when checked with isSuccess then state is smart cast to Success`() {
        val state: LoadableViewState<String> = LoadableViewState.Success("value")

        if (state.isSuccess()) {
            // If it compiles, the contract is working correctly
            val castedState: LoadableViewState.Success<String> = state
            castedState.shouldBeInstanceOf<LoadableViewState.Success<String>>()
        }
    }

    @Test
    fun `given a failure state when checked with isFailure then state is smart cast to Failure`() {
        val state: LoadableViewState<String> = LoadableViewState.Failure(RuntimeException())

        if (state.isFailure()) {
            // If it compiles, the contract is working correctly
            val castedState: LoadableViewState.Failure = state
            castedState.shouldBeInstanceOf<LoadableViewState.Failure>()
        }
    }

    @Test
    fun `given a failure when checked for exception then the exception is found`() {
        val viewState = LoadableViewState.Failure(RuntimeException())
        val exception = viewState.exceptionOrNull()
        exception.shouldBeInstanceOf<RuntimeException>()
    }

    @TestFactory
    fun `given a no failure when checked for exception then the exception is not found`() =
        listOf(
            LoadableViewState.Initial,
            LoadableViewState.Loading,
            LoadableViewState.Success(Unit)
        ).map { viewState ->
            dynamicTest("given a $viewState when checked for exception then the exception is not found") {
                val exception = viewState.exceptionOrNull()
                exception.shouldBeNull()
            }
        }

    @Test
    fun `given a failure when checked for exception then the exception is not thrown`() {
        val viewState = LoadableViewState.Failure(RuntimeException())
        val exception = viewState.exceptionOrThrow()
        exception.shouldBeInstanceOf<RuntimeException>()
    }

    @TestFactory
    fun `given a no failure when checked for exception then an exception is thrown`() =
        listOf(
            LoadableViewState.Initial,
            LoadableViewState.Loading,
            LoadableViewState.Success(Unit)
        ).map { viewState ->
            dynamicTest("given a $viewState when checked for exception then the exception is not thrown") {
                shouldThrowExactly<IllegalStateException> {
                    viewState.exceptionOrThrow()
                }
            }
        }

    @Test
    fun `given a success when checked for value then the value is found`() {
        val viewState = LoadableViewState.Success(Unit)
        val value = viewState.valueOrNull()
        value.shouldBeInstanceOf<Unit>()
    }

    @TestFactory
    fun `given a no success when checked for value then the value is not found`() =
        listOf(
            LoadableViewState.Initial,
            LoadableViewState.Loading,
            LoadableViewState.Failure(RuntimeException())
        ).map { viewState ->
            dynamicTest("given a $viewState when checked for value then the value is not found") {
                val value = viewState.valueOrNull()
                value.shouldBeNull()
            }
        }

    @Test
    fun `given a success when checked for value then the value is not thrown`() {
        val viewState = LoadableViewState.Success(Unit)
        val value = viewState.valueOrThrow()
        value.shouldBeInstanceOf<Unit>()
    }

    @TestFactory
    fun `given a no success when checked for value then an exception is thrown`() =
        listOf(
            LoadableViewState.Initial,
            LoadableViewState.Loading,
            LoadableViewState.Failure(RuntimeException())
        ).map { viewState ->
            dynamicTest("given a $viewState when checked for value then the value is not thrown") {
                shouldThrowExactly<IllegalStateException> {
                    viewState.valueOrThrow()
                }
            }
        }

    @Test
    fun `given an initial state when reacting to it then the expected action runs`() {
        val block = mockk<() -> Unit>()
        justRun { block() }
        val viewState = LoadableViewState.Initial
        viewState.onInitial(block)
        verify { block() }
    }

    @TestFactory
    fun `given a not initial state when reaction to initial then the action does not run`() =
        listOf(
            LoadableViewState.Loading,
            LoadableViewState.Failure(RuntimeException()),
            LoadableViewState.Success(Unit)
        ).map { viewState ->
            dynamicTest("given a $viewState when reaction to initial then the action does not run") {
                val block = mockk<() -> Unit>()
                justRun { block() }
                viewState.onInitial(block)
                verify(exactly = 0) { block() }
            }
        }

    @Test
    fun `given a loading state when reacting to it then the expected action runs`() {
        val block = mockk<() -> Unit>()
        justRun { block() }
        val viewState = LoadableViewState.Loading
        viewState.onLoading(block)
        verify { block() }
    }

    @TestFactory
    fun `given a not loading state when reaction to loading then the action does not run`() =
        listOf(
            LoadableViewState.Initial,
            LoadableViewState.Failure(RuntimeException()),
            LoadableViewState.Success(Unit)
        ).map { viewState ->
            dynamicTest("given a $viewState when reaction to loading then the action does not run") {
                val block = mockk<() -> Unit>()
                justRun { block() }
                viewState.onLoading(block)
                verify(exactly = 0) { block() }
            }
        }

    @Test
    fun `given a success state when reacting to it then the expected action runs`() {
        val block = mockk<(value: Unit) -> Unit>()
        justRun { block(Unit) }
        val viewState = LoadableViewState.Success(Unit)
        viewState.onSuccess(block)
        verify { block(Unit) }
    }

    @TestFactory
    fun `given a not success state when reaction to success then the action does not run`() =
        listOf(
            LoadableViewState.Initial,
            LoadableViewState.Loading,
            LoadableViewState.Failure(RuntimeException())
        ).map { viewState ->
            dynamicTest("given a $viewState when reaction to success then the action does not run") {
                val block = mockk<(value: Unit) -> Unit>()
                justRun { block(Unit) }
                viewState.onSuccess(block)
                verify(exactly = 0) { block(Unit) }
            }
        }

    @Test
    fun `given a failure state when reacting to it then the expected action runs`() {
        val block = mockk<(throwable: Throwable) -> Unit>()
        justRun { block(any()) }
        val viewState = LoadableViewState.Failure(RuntimeException())
        viewState.onFailure(block)
        verify { block(any()) }
    }

    @TestFactory
    fun `given a not failure state when reaction to failure then the action does not run`() =
        listOf(
            LoadableViewState.Initial,
            LoadableViewState.Loading,
            LoadableViewState.Success(Unit)
        ).map { viewState ->
            dynamicTest("given a $viewState when reaction to failure then the action does not run") {
                val block = mockk<(throwable: Throwable) -> Unit>()
                justRun { block(any()) }
                viewState.onFailure(block)
                verify(exactly = 0) { block(any()) }
            }
        }
}