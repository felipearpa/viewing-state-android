package com.felipearpa.ui.state

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class SavableViewStateTest {
    @Test
    fun `given an initial state when checked if initial then initial is confirmed`() {
        val viewState: SavableViewState<String> = SavableViewState.Initial
        val isInitial = viewState.isInitial()
        isInitial.shouldBeTrue()
    }

    @TestFactory
    fun `given a not initial state when checked if initial then initial is not confirmed`() =
        listOf(
            SavableViewState.Saving(savingValue),
            SavableViewState.Failure(failureException),
            SavableViewState.Success,
        ).map { viewState ->
            dynamicTest("given a state of $viewState when checked if initial then initial is not confirmed") {
                val isInitial = viewState.isInitial()
                isInitial.shouldBeFalse()
            }
        }

    @Test
    fun `given a saving state when checked if saving then saving is confirmed`() {
        val viewState: SavableViewState<String> = SavableViewState.Saving(savingValue)
        val isSaving = viewState.isSaving()
        isSaving.shouldBeTrue()
    }

    @TestFactory
    fun `given a not saving state when checked if saving then saving is not confirmed`() =
        listOf<SavableViewState<String>>(
            SavableViewState.Initial,
            SavableViewState.Failure(failureException),
            SavableViewState.Success,
        ).map { viewState ->
            dynamicTest("given a state of $viewState when checked if saving then saving is not confirmed") {
                val isSaving = viewState.isSaving()
                isSaving.shouldBeFalse()
            }
        }

    @Test
    fun `given a failure state when checked if failure then failure is confirmed`() {
        val viewState: SavableViewState<String> = SavableViewState.Failure(failureException)
        val isFailure = viewState.isFailure()
        isFailure.shouldBeTrue()
    }

    @TestFactory
    fun `given a not failure state when checked if failure then failure is not confirmed`() =
        listOf(
            SavableViewState.Initial,
            SavableViewState.Saving(savingValue),
            SavableViewState.Success,
        ).map { viewState ->
            dynamicTest("given a state of $viewState when checked if failure then failure is not confirmed") {
                val isFailure = viewState.isFailure()
                isFailure.shouldBeFalse()
            }
        }

    @Test
    fun `given a success state when checked if success then success is confirmed`() {
        val viewState: SavableViewState<String> = SavableViewState.Success
        val isSuccess = viewState.isSuccess()
        isSuccess.shouldBeTrue()
    }

    @TestFactory
    fun `given a not success state when checked if success then success is not confirmed`() =
        listOf(
            SavableViewState.Initial,
            SavableViewState.Saving(savingValue),
            SavableViewState.Failure(failureException),
        ).map { viewState ->
            dynamicTest("given a state of $viewState when checked if success then success is not confirmed") {
                val isSuccess = viewState.isSuccess()
                isSuccess.shouldBeFalse()
            }
        }

    @Test
    fun `given an initial state when checked with isInitial then state is smart cast to Initial`() {
        val state: SavableViewState<String> = SavableViewState.Initial

        if (state.isInitial()) {
            // If it compiles, the contract is working correctly
            val castedState: SavableViewState.Initial = state
            castedState.shouldBeInstanceOf<SavableViewState.Initial>()
        }
    }

    @Test
    fun `given a saving state when checked with isSaving then state is smart cast to Saving`() {
        val state: SavableViewState<String> = SavableViewState.Saving(savingValue)

        if (state.isSaving()) {
            // If it compiles, the contract is working correctly
            val castedState: SavableViewState.Saving<String> = state
            castedState.shouldBeInstanceOf<SavableViewState.Saving<String>>()
        }
    }

    @Test
    fun `given a success state when checked with isSuccess then state is smart cast to Success`() {
        val state: SavableViewState<String> = SavableViewState.Success

        if (state.isSuccess()) {
            // If it compiles, the contract is working correctly
            val castedState: SavableViewState.Success = state
            castedState.shouldBeInstanceOf<SavableViewState.Success>()
        }
    }

    @Test
    fun `given a failure state when checked with isFailure then state is smart cast to Failure`() {
        val state: SavableViewState<String> = SavableViewState.Failure(failureException)

        if (state.isFailure()) {
            // If it compiles, the contract is working correctly
            val castedState: SavableViewState.Failure = state
            castedState.shouldBeInstanceOf<SavableViewState.Failure>()
        }
    }

    @Test
    fun `given a failure when checked for exception then the exception is found`() {
        val viewState: SavableViewState<String> = SavableViewState.Failure(failureException)
        val exception = viewState.exceptionOrNull()
        exception.shouldBeInstanceOf<RuntimeException>()
    }

    @TestFactory
    fun `given a no failure when checked for exception then the exception is not found`() =
        listOf(
            SavableViewState.Initial,
            SavableViewState.Saving(savingValue),
            SavableViewState.Success,
        ).map { viewState ->
            dynamicTest("given a $viewState when checked for exception then the exception is not found") {
                val exception = viewState.exceptionOrNull()
                exception.shouldBeNull()
            }
        }

    @Test
    fun `given a failure when checked for exception then the exception is not thrown`() {
        val viewState: SavableViewState<String> = SavableViewState.Failure(failureException)
        val exception = viewState.exceptionOrThrow()
        exception.shouldBeInstanceOf<RuntimeException>()
        exception shouldBe failureException
    }

    @TestFactory
    fun `given a no failure when checked for exception then an exception is thrown`() =
        listOf(
            SavableViewState.Initial,
            SavableViewState.Saving(savingValue),
            SavableViewState.Success,
        ).map { viewState ->
            dynamicTest("given a $viewState when checked for exception then the exception is not thrown") {
                shouldThrowExactly<IllegalStateException> {
                    viewState.exceptionOrThrow()
                }
            }
        }

    @Test
    fun `given a saving when checked for value then the value is found`() {
        val viewState: SavableViewState<String> = SavableViewState.Saving(savingValue)
        val value = viewState.valueOrNull()
        value shouldBe savingValue
    }

    @TestFactory
    fun `given a no saving when checked for value then the value is not found`() =
        listOf<SavableViewState<String>>(
            SavableViewState.Initial,
            SavableViewState.Success,
            SavableViewState.Failure(failureException),
        ).map { viewState ->
            dynamicTest("given a $viewState when checked for value then the value is not found") {
                val value = viewState.valueOrNull()
                value.shouldBeNull()
            }
        }

    @Test
    fun `given a saving when checked for value then the value is not thrown`() {
        val viewState: SavableViewState<String> = SavableViewState.Saving(savingValue)
        val value = viewState.valueOrThrow()
        value shouldBe savingValue
    }

    @TestFactory
    fun `given a no saving when checked for value then an exception is thrown`() =
        listOf<SavableViewState<String>>(
            SavableViewState.Initial,
            SavableViewState.Success,
            SavableViewState.Failure(failureException),
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
        val viewState: SavableViewState<String> = SavableViewState.Initial
        viewState.onInitial(block)
        verify { block() }
    }

    @TestFactory
    fun `given a not initial state when reaction to initial then the action does not run`() =
        listOf(
            SavableViewState.Saving(savingValue),
            SavableViewState.Failure(failureException),
            SavableViewState.Success,
        ).map { viewState ->
            dynamicTest("given a $viewState when reaction to initial then the action does not run") {
                val block = mockk<() -> Unit>()
                justRun { block() }
                viewState.onInitial(block)
                verify(exactly = 0) { block() }
            }
        }

    @Test
    fun `given a saving state when reacting to it then the expected action runs`() {
        val block = mockk<(String) -> Unit>()
        justRun { block(savingValue) }
        val viewState: SavableViewState<String> = SavableViewState.Saving(savingValue)
        viewState.onSaving(block)
        verify { block(savingValue) }
    }

    @TestFactory
    fun `given a not saving state when reaction to loading then the action does not run`() =
        listOf(
            SavableViewState.Initial,
            SavableViewState.Failure(failureException),
            SavableViewState.Success,
        ).map { viewState ->
            dynamicTest("given a $viewState when reaction to loading then the action does not run") {
                val block = mockk<(String) -> Unit>()
                justRun { block(savingValue) }
                viewState.onSaving(block)
                verify(exactly = 0) { block(savingValue) }
            }
        }

    @Test
    fun `given a success state when reacting to it then the expected action runs`() {
        val block = mockk<() -> Unit>()
        justRun { block() }
        val viewState: SavableViewState<String> = SavableViewState.Success
        viewState.onSuccess(block)
        verify { block() }
    }

    @TestFactory
    fun `given a not success state when reaction to success then the action does not run`() =
        listOf(
            SavableViewState.Initial,
            SavableViewState.Saving(savingValue),
            SavableViewState.Failure(failureException),
        ).map { viewState ->
            dynamicTest("given a $viewState when reaction to success then the action does not run") {
                val block = mockk<() -> Unit>()
                justRun { block() }
                viewState.onSuccess(block)
                verify(exactly = 0) { block() }
            }
        }

    @Test
    fun `given a failure state when reacting to it then the expected action runs`() {
        val block = mockk<(throwable: Throwable) -> Unit>()
        justRun { block(failureException) }
        val viewState: SavableViewState<String> = SavableViewState.Failure(failureException)
        viewState.onFailure(block)
        verify { block(failureException) }
    }

    @TestFactory
    fun `given a not failure state when reaction to failure then the action does not run`() =
        listOf(
            SavableViewState.Initial,
            SavableViewState.Saving(savingValue),
            SavableViewState.Success,
        ).map { viewState ->
            dynamicTest("given a $viewState when reaction to failure then the action does not run") {
                val block = mockk<(throwable: Throwable) -> Unit>()
                justRun { block(any()) }
                viewState.onFailure(block)
                verify(exactly = 0) { block(any()) }
            }
        }

    @Test
    fun `given a saving state when mapping then the state is transformed`() {
        val block = mockk<(value: String) -> String>()
        every { block(savingValue) } returns transformSuccessValue
        val viewState: SavableViewState<String> = SavableViewState.Saving(savingValue)
        val newState = viewState.map { block(it) }
        verify { block(savingValue) }
        newState.shouldBeInstanceOf<SavableViewState.Saving<String>>()
        newState.value shouldBe transformSuccessValue
    }

    @TestFactory
    fun `given a not saving state when mapping then the state is not transformed`() =
        listOf<SavableViewState<String>>(
            SavableViewState.Initial,
            SavableViewState.Success,
            SavableViewState.Failure(failureException),
        ).map { viewState ->
            dynamicTest("given a $viewState when mapping then the expected value is not returned") {
                val block = mockk<(value: String) -> String>()
                every { block(savingValue) } returns transformSuccessValue
                viewState.map { block(it) }
                verify(exactly = 0) { block(savingValue) }
            }
        }

    @Test
    fun `given an initial state when folding then the expected value is returned`() {
        val viewState: SavableViewState<String> = SavableViewState.Initial
        val value = viewState.fold(
            onInitial = { transformedInitialValue },
            onSaving = { transformedSavingValue },
            onSuccess = { transformSuccessValue },
            onFailure = { transformedFailureValue },
        )
        value shouldBe transformedInitialValue
    }

    @Test
    fun `given a saving state when folding then the expected value is returned`() {
        val viewState: SavableViewState<String> = SavableViewState.Saving(savingValue)
        val value = viewState.fold(
            onInitial = { transformedInitialValue },
            onSaving = { transformedSavingValue },
            onSuccess = { transformSuccessValue },
            onFailure = { transformedFailureValue },
        )
        value shouldBe transformedSavingValue
    }

    @Test
    fun `given a success state when folding then the expected value is returned`() {
        val viewState: SavableViewState<String> = SavableViewState.Success
        val value = viewState.fold(
            onInitial = { transformedInitialValue },
            onSaving = { transformedSavingValue },
            onSuccess = { transformSuccessValue },
            onFailure = { transformedFailureValue },
        )
        value shouldBe transformSuccessValue
    }

    @Test
    fun `given a failure state when folding then the expected value is returned`() {
        val viewState: SavableViewState<String> = SavableViewState.Failure(failureException)
        val value = viewState.fold(
            onInitial = { transformedInitialValue },
            onSaving = { transformedSavingValue },
            onSuccess = { transformSuccessValue },
            onFailure = { transformedFailureValue },
        )
        value shouldBe transformedFailureValue
    }
}

private const val savingValue = "saving value"
private const val transformedInitialValue = "transformed initial value"
private const val transformedSavingValue = "transformed saving value"
private const val transformSuccessValue = "transformed success value"
private const val transformedFailureValue = "transformed failure value"
private val failureException = RuntimeException()
