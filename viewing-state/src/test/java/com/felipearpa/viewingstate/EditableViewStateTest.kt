package com.felipearpa.viewingstate

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class EditableViewStateTest {
    @Test
    fun `given an initial state when checked if initial then initial is confirmed`() {
        val viewState = EditableViewState.Initial(Unit)
        val isInitial = viewState.isInitial()
        isInitial.shouldBeTrue()
    }

    @TestFactory
    fun `given a not initial state when checked if initial then initial is not confirmed`() =
        listOf(
            EditableViewState.Loading(current = Unit, target = Unit),
            EditableViewState.Failure(
                current = Unit,
                failed = Unit,
                exception = RuntimeException()
            ),
            EditableViewState.Success(old = Unit, succeeded = Unit)
        ).map { viewState ->
            dynamicTest("given a state of $viewState when checked if initial then initial is not confirmed") {
                val isInitial = viewState.isInitial()
                isInitial.shouldBeFalse()
            }
        }

    @Test
    fun `given a loading state when checked if loading then loading is confirmed`() {
        val viewState = EditableViewState.Loading(current = Unit, target = Unit)
        val isLoading = viewState.isLoading()
        isLoading.shouldBeTrue()
    }

    @TestFactory
    fun `given a not loading state when checked if loading then loading is not confirmed`() =
        listOf(
            EditableViewState.Initial(Unit),
            EditableViewState.Failure(
                current = Unit,
                failed = Unit,
                exception = RuntimeException()
            ),
            EditableViewState.Success(old = Unit, succeeded = Unit)
        ).map { viewState ->
            dynamicTest("given a state of $viewState when checked if loading then loading is not confirmed") {
                val isLoading = viewState.isLoading()
                isLoading.shouldBeFalse()
            }
        }

    @Test
    fun `given a success state when checked if success then success is confirmed`() {
        val viewState = EditableViewState.Success(old = Unit, succeeded = Unit)
        val isSuccess = viewState.isSuccess()
        isSuccess.shouldBeTrue()
    }

    @TestFactory
    fun `given a not success state when checked if success then success is not confirmed`() =
        listOf(
            EditableViewState.Initial(Unit),
            EditableViewState.Loading(current = Unit, target = Unit),
            EditableViewState.Failure(
                current = Unit,
                failed = Unit,
                exception = RuntimeException()
            )
        ).map { viewState ->
            dynamicTest("given a state of $viewState when checked if success then success is not confirmed") {
                val isSuccess = viewState.isSuccess()
                isSuccess.shouldBeFalse()
            }
        }

    @Test
    fun `given a failure state when checked if failure then failure is confirmed`() {
        val viewState = EditableViewState.Failure(
            current = Unit,
            failed = Unit,
            exception = RuntimeException()
        )
        val isFailure = viewState.isFailure()
        isFailure.shouldBeTrue()
    }

    @TestFactory
    fun `given a not failure state when checked if failure then failure is not confirmed`() =
        listOf(
            EditableViewState.Initial(Unit),
            EditableViewState.Loading(current = Unit, target = Unit),
            EditableViewState.Success(old = Unit, succeeded = Unit)
        ).map { viewState ->
            dynamicTest("given a state of $viewState when checked if failure then failure is not confirmed") {
                val isFailure = viewState.isFailure()
                isFailure.shouldBeFalse()
            }
        }

    @Test
    fun `given an initial state when checked with isInitial then state is smart cast to Initial`() {
        val state: EditableViewState<Unit> = EditableViewState.Initial(Unit)
        if (state.isInitial()) {
            // If it compiles, the contract is working correctly
            val castedState: EditableViewState.Initial<Unit> = state
            castedState.shouldBeInstanceOf<EditableViewState.Initial<Unit>>()
        }
    }

    @Test
    fun `given a loading state when checked with isLoading then state is smart cast to Loading`() {
        val state: EditableViewState<Unit> =
            EditableViewState.Loading(current = Unit, target = Unit)
        if (state.isLoading()) {
            // If it compiles, the contract is working correctly
            val castedState: EditableViewState.Loading<Unit> = state
            castedState.shouldBeInstanceOf<EditableViewState.Loading<Unit>>()
        }
    }

    @Test
    fun `given a success state when checked with isSuccess then state is smart cast to Success`() {
        val state: EditableViewState<Unit> = EditableViewState.Success(old = Unit, succeeded = Unit)
        if (state.isSuccess()) {
            // If it compiles, the contract is working correctly
            val castedState: EditableViewState.Success<Unit> = state
            castedState.shouldBeInstanceOf<EditableViewState.Success<Unit>>()
        }
    }

    @Test
    fun `given a failure state when checked with isFailure then state is smart cast to Failure`() {
        val state: EditableViewState<Unit> = EditableViewState.Failure(
            current = Unit,
            failed = Unit,
            exception = RuntimeException()
        )
        if (state.isFailure()) {
            // If it compiles, the contract is working correctly
            val castedState: EditableViewState.Failure<Unit> = state
            castedState.shouldBeInstanceOf<EditableViewState.Failure<Unit>>()
        }
    }

    @Test
    fun `given a failure when checked for exception then the exception is found`() {
        val viewState = EditableViewState.Failure(
            current = Unit,
            failed = Unit,
            exception = RuntimeException()
        )
        val exception = viewState.exceptionOrNull()
        exception.shouldBeInstanceOf<RuntimeException>()
    }

    @TestFactory
    fun `given a no failure when checked for exception then the exception is not found`() =
        listOf(
            EditableViewState.Initial(Unit),
            EditableViewState.Loading(current = Unit, target = Unit),
            EditableViewState.Success(old = Unit, succeeded = Unit)
        ).map { viewState ->
            dynamicTest("given a $viewState when checked for exception then the exception is not found") {
                val exception = viewState.exceptionOrNull()
                exception.shouldBeNull()
            }
        }

    @Test
    fun `given an initial state when checked for value then the value is found`() {
        val viewState = EditableViewState.Initial("value")
        val value = viewState.relevantValue()
        value.shouldBe("value")
    }

    @Test
    fun `given a loading state when checked for value then the current value is found`() {
        val viewState = EditableViewState.Loading(current = "current", target = "target")
        val value = viewState.relevantValue()
        value.shouldBe("current")
    }

    @Test
    fun `given a success state when checked for value then the succeeded value is found`() {
        val viewState = EditableViewState.Success(old = "old", succeeded = "succeeded")
        val value = viewState.relevantValue()
        value.shouldBe("succeeded")
    }

    @Test
    fun `given a failure state when checked for value then the current value is found`() {
        val viewState = EditableViewState.Failure(
            current = "current",
            failed = "failed",
            exception = RuntimeException()
        )
        val value = viewState.relevantValue()
        value.shouldBe("current")
    }

    @Test
    fun `given an initial state when reacting to it then the expected action runs`() {
        val block = mockk<(Unit) -> Unit>()
        justRun { block(Unit) }
        val viewState = EditableViewState.Initial(Unit)
        viewState.onInitial(block)
        verify { block(Unit) }
    }

    @TestFactory
    fun `given a not initial state when reaction to initial then the action does not run`() =
        listOf(
            EditableViewState.Loading(current = Unit, target = Unit),
            EditableViewState.Failure(
                current = Unit,
                failed = Unit,
                exception = RuntimeException()
            ),
            EditableViewState.Success(old = Unit, succeeded = Unit)
        ).map { viewState ->
            dynamicTest("given a $viewState when reaction to initial then the action does not run") {
                val block = mockk<(Unit) -> Unit>()
                justRun { block(Unit) }
                viewState.onInitial(block)
                verify(exactly = 0) { block(Unit) }
            }
        }

    @Test
    fun `given a loading state when reacting to it then the expected action runs`() {
        val block = mockk<(Unit, Unit) -> Unit>()
        justRun { block(Unit, Unit) }
        val viewState = EditableViewState.Loading(current = Unit, target = Unit)
        viewState.onLoading(block)
        verify { block(Unit, Unit) }
    }

    @TestFactory
    fun `given a not loading state when reaction to loading then the action does not run`() =
        listOf(
            EditableViewState.Initial(Unit),
            EditableViewState.Failure(
                current = Unit,
                failed = Unit,
                exception = RuntimeException()
            ),
            EditableViewState.Success(old = Unit, succeeded = Unit)
        ).map { viewState ->
            dynamicTest("given a $viewState when reaction to loading then the action does not run") {
                val block = mockk<(Unit, Unit) -> Unit>()
                justRun { block(Unit, Unit) }
                viewState.onLoading(block)
                verify(exactly = 0) { block(Unit, Unit) }
            }
        }

    @Test
    fun `given a success state when reacting to it then the expected action runs`() {
        val block = mockk<(Unit, Unit) -> Unit>()
        justRun { block(Unit, Unit) }
        val viewState = EditableViewState.Success(old = Unit, succeeded = Unit)
        viewState.onSuccess(block)
        verify { block(Unit, Unit) }
    }

    @TestFactory
    fun `given a not success state when reaction to success then the action does not run`() =
        listOf(
            EditableViewState.Initial(Unit),
            EditableViewState.Loading(current = Unit, target = Unit),
            EditableViewState.Failure(
                current = Unit,
                failed = Unit,
                exception = RuntimeException()
            )
        ).map { viewState ->
            dynamicTest("given a $viewState when reaction to success then the action does not run") {
                val block = mockk<(Unit, Unit) -> Unit>()
                justRun { block(Unit, Unit) }
                viewState.onSuccess(block)
                verify(exactly = 0) { block(Unit, Unit) }
            }
        }

    @Test
    fun `given a failure state when reacting to it then the expected action runs`() {
        val block = mockk<(Unit, Unit, Throwable) -> Unit>()
        val exception = RuntimeException()
        justRun { block(Unit, Unit, exception) }
        val viewState = EditableViewState.Failure(
            current = Unit,
            failed = Unit,
            exception = exception
        )
        viewState.onFailure(block)
        verify { block(Unit, Unit, exception) }
    }

    @TestFactory
    fun `given a not failure state when reaction to failure then the action does not run`() =
        listOf(
            EditableViewState.Initial(Unit),
            EditableViewState.Loading(current = Unit, target = Unit),
            EditableViewState.Success(old = Unit, succeeded = Unit)
        ).map { viewState ->
            dynamicTest("given a $viewState when reaction to failure then the action does not run") {
                val block = mockk<(Unit, Unit, Throwable) -> Unit>()
                justRun { block(Unit, Unit, RuntimeException()) }
                viewState.onFailure(block)
                verify(exactly = 0) { block(Unit, Unit, RuntimeException()) }
            }
        }
}