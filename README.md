# viewing-state-android

The **viewing-state-android** package provides classes like `LoadableViewState` and
`EditableViewState` to handle common view state transitions.
`LoadableViewState` manages four states: initial, loading, success, and failure,
while `EditableViewState` extends this functionality allowing granular control over state changes.

## Why

Using `LoadableViewState` simplifies managing common UI states like loading, success, and failure.
It
centralizes state logic in your ViewModel, making your Composable functions cleaner and focused
solely on displaying the UI based on the current state. This leads to more predictable and
maintainable code by clearly defining and handling different stages of data presentation.

```kotlin
data class MyData(val id: Int, val name: String)

class MyViewModel : ViewModel() {
    private val _state = MutableStateFlow<LoadableViewState<MyData>>(LoadableViewState.Initial)
    val state: StateFlow<LoadableViewState<MyData>> = _uiState.asStateFlow()

    fun fetchData() {
        viewModelScope.launch {
            _state.value = LoadableViewState.Loading // Set to Loading state

            try {
                // Simulate a network call or long-running operation
                delay(2000)
                val result = MyData(1, "Hello, Android!") // Successful data retrieval
                _state.value = LoadableViewState.Success(result)
            } catch (exception: Exception) {
                _state.value = LoadableViewState.Failure(exception) // Set to Failure state
            }
        }
    }

    fun resetState() {
        _state.value = LoadableViewState.Initial
    }
}

@Composable
fun MyScreen(viewModel: MyViewModel = viewModel()) {
    val viewModelState by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    )
    when (val state = viewModelState) {
        is LoadableViewState.Initial -> {
            Text("Click the button to load data.")
            Button(onClick = { myViewModel.fetchData() }) {
                Text("Load Data")
            }
        }
        is LoadableViewState.Loading -> {
            CircularProgressIndicator()
            Text("Loading...")
        }
        is LoadableViewState.Success -> {
            Text("Data Loaded Successfully!")
            Text("ID: ${state().id}, Name: ${state().name}")
            Button(onClick = { myViewModel.resetState() }) {
                Text("Load Again")
            }
        }
        is LoadableViewState.Failure -> {
            Text("Error: ${state().message}")
            Button(onClick = { myViewModel.fetchData() }) {
                Text("Retry")
            }
        }
    }
}
```

EditableViewState provides a clear and structured way to manage the state of data that can be edited
within your UI. It distinctly represents the initial state before any changes, a loading state while
an edit is being processed (showing both the current and intended target values), a success state
confirming the edit and providing both old and new values, and a failure state if the edit doesn't
succeed, capturing the attempted change and the error.

```kotlin
data class UserProfile(val displayName: String)

class ProfileViewModel : ViewModel() {
    private val _state = MutableStateFlow<EditableViewState<UserProfile>>(
        EditableViewState.Initial(UserProfile("Initial Name"))
    )
    val state = _profileState.asStateFlow()

    fun updateDisplayName(newName: String) {
        viewModelScope.launch {
            val currentState = _state.value
            val currentUserProfile = currentState.relevantValue()

            val targetProfile = UserProfile(newName)
            _state.value =
                EditableViewState.Saving(current = currentUserProfile, target = targetProfile)

            try {
                // Simulate a network call or save operation
                delay(1500)
                if (newName.length < 3) { // Simulate a validation error
                    throw IllegalArgumentException("Name must be at least 3 characters long.")
                }
                // Simulate successful update
                _state.value =
                    EditableViewState.Success(old = currentUserProfile, succeeded = targetProfile)
            } catch (e: Exception) {
                _state.value = EditableViewState.Failure(
                    current = currentUserProfile,
                    failed = targetProfile,
                    e
                )
            }
        }
    }

    fun resetToInitial(initialProfile: UserProfile? = null) {
        val currentData = initialProfile ?: _state.value.relevantValue()
        _state.value = EditableViewState.Initial(currentData)
    }
}

@Composable
fun ProfileEditScreen(viewModel: ProfileViewModel = viewModel()) {
    val viewModelState by viewModel.state.collectAsState()
    var editingName by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf<String?>(null) }

    // Update editingName when the successful state changes the underlying data
    // or when resetting to initial
    LaunchedEffect(viewModelState) {
        when (val state = viewModelState) {
            is EditableViewState.Initial -> editingName = state.data.displayName
            is EditableViewState.Success -> editingName = state.succeeded.displayName
            else -> { /* Do nothing for loading/failure here for editingName init */
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Edit Your Profile", style = MaterialTheme.typography.headlineSmall)

        val currentDisplayName = when (val state = viewModelState) {
            is EditableViewState.Initial -> state.value.displayName
            is EditableViewState.Saving -> "Updating to: ${state.target.displayName}..."
            is EditableViewState.Success -> state.succeeded.displayName
            is EditableViewState.Failure -> "Failed to update to: ${state.failed.displayName} (was: ${state.current.displayName})"
        }
        Text("Current Name: $currentDisplayName")

        OutlinedTextField(
            value = editingName,
            onValueChange = { editingName = it },
            label = { Text("New Display Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = viewModelState.isFailure()
        )

        val isSaving = viewModelState.isSaving()

        Button(
            onClick = { viewModel.updateDisplayName(editingName) },
            enabled = !isSaving && editingName.isNotBlank()
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Saving...")
            } else {
                Text("Save Name")
            }
        }

        Button(onClick = {
            // Get the original initial name if possible for a true reset
            val initialProfile = (viewModel.profileState.value as? EditableViewState.Initial)?.data
                ?: UserProfile("Initial Name") // Fallback
            viewModel.resetToInitial(initialProfile)
            editingName = initialProfile.displayName // Reset text field
            showSnackbar = "State reset to initial."
        }) {
            Text("Reset to Initial")
        }


        when (val state = viewModelState) {
            is EditableViewState.Success -> {
                LaunchedEffect(state) { // Show snackbar only once per success
                    showSnackbar =
                        "Name updated from '${state.old.displayName}' to '${state.succeeded.displayName}'!"
                }
            }
            is EditableViewState.Failure -> {
                Text("Error: ${state.error.message}", color = MaterialTheme.colorScheme.error)
                LaunchedEffect(state) { // Show snackbar only once per failure
                    showSnackbar = "Update failed: ${state.error.message}"
                }
            }
            else -> { /* No specific message for Initial or Loading here */
            }
        }

        if (showSnackbar != null) {
            LaunchedEffect(showSnackbar) { // Auto-dismiss snackbar
                kotlinx.coroutines.delay(3000)
                showSnackbar = null
            }
            Snackbar(
                modifier = Modifier.padding(8.dp),
                action = { Button(onClick = { showSnackbar = null }) { Text("Dismiss") } }
            ) { Text(showSnackbar!!) }
        }
    }
}
```

## Installation

To install the dependency in your project, add the following to your `settings.gradle.kts` file in
the section `dependencyResolutionManagement -> repositories`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

then add the following to your `build.gradle.kts` file in the section `dependencies`:

```kotlin
implementation("com.github.felipearpa:viewing-state-android:main-SNAPSHOT")
```

## Contributing

If you would like to contribute, please open a pull request or submit an issue. We are happy to
review your changes or ideas!

## License

This project is licensed under the [MIT License](LICENSE).
You are free to use, modify, and distribute this software for both personal and commercial use.
There are no restrictions on usage.

## API

### `LoadableViewState<Value>`

Represents the different states involved in loading data of type Value.

#### `Initial`

Represents the initial state of the view, before any data has been loaded.

```kotlin
val initialState = LoadableViewState.Initial
```

#### `Loading`

Represents the loading state of the view, indicating that data is currently being loaded.

```kotlin
val loadingState = LoadableViewState.Loading
```

#### `Success<Value>`

Represents the successful state of loading data.

```kotlin
val successState = LoadableViewState.Success(Unit)
```

#### `Failure`

Represents the failure state of the view, containing an exception.

```kotlin
val failureState = LoadableViewState.Failure(RuntimeException())
```

#### `isInitial()`

Checks if the current state is `LoadableViewState.Initial`.

```kotlin
val state = LoadableViewState.Initial
state.isInitial() // Output: true
```

#### `isLoading()`

Checks if the current state is `LoadableViewState.Loading`.

```kotlin
val state = LoadableViewState.Loading
state.isLoading() // Output: true
```

#### `isSuccess()`

Checks if the current state is `LoadableViewState.Success`.

````kotlin
val state = LoadableViewState.Success(Unit)
state.isSuccess() // Output: true
````

#### `isFailure()`

Checks if the current state is `LoadableViewState.Failure`.

```kotlin
val state = LoadableViewState.Failure(RuntimeException())
state.isFailure() // Output: true
```

#### `onInitial(block: () -> Unit)`

Executes the given `block` if the current state is `LoadableViewState.Initial`.

```kotlin
val state = LoadableViewState.Initial
state.onInitial { println("Initial state reached") } // Output: Initial state reached
```

#### `onLoading(block: () -> Unit)`

Executes the given `block` if the current state is `LoadableViewState.Loading`.

```kotlin
val state = LoadableViewState.Loading
state.onLoading { println("Loading state reached") } // Output: Loading state reached
```

#### `onSuccess(block: (value: Value) -> Unit)`

Executes the given `block` if the current state is `LoadableViewState.Success`.

```kotlin
val state = LoadableViewState.Success(Unit)
state.onSuccess { println("Success state reached") } // Output: Success state reached
```

#### `onFailure(block: (throwable: Throwable) -> Unit)`

Executes the given `block` if the current state is `LoadableViewState.Failure`.

```kotlin
val state = LoadableViewState.Failure(RuntimeException())
state.onFailure { println("Failure state reached") } // Output: Failure state reached
```

#### `valueOrNull()`

Returns the value if the current state is `LoadableViewState.Success`, or `null` otherwise.

```kotlin
val state = LoadableViewState.Success("value")
state.valueOrNull() // Output: value
```

```kotlin
val state = LoadableViewState.Failure(RuntimeException())
state.valueOrNull() // Output: null
```

#### `valueOrThrow()`

Returns the value if the current state is `LoadableViewState.Success`, or throws an
`IllegalStateException` otherwise.

```kotlin
val state = LoadableViewState.Success("value")
state.valueOrThrow() // Output: value
```

```kotlin
val state = LoadableViewState.Failure(RuntimeException())
state.valueOrThrow() // Throws IllegalStateException
```

#### `exceptionOrNull()`

Returns the exception if the current state is `LoadableViewState.Failure`, or `null` otherwise.

```kotlin
val state = LoadableViewState.Failure(RuntimeException())
state.exceptionOrNull() // Output: RuntimeException
```

```kotlin
val state = LoadableViewState.Success(Unit)
state.exceptionOrNull() // Output: null
```

#### `exceptionOrThrow()`

Returns the exception if the current state is `LoadableViewState.Failure`, or throws an
`IllegalStateException` otherwise.

```kotlin
val state = LoadableViewState.Failure(RuntimeException())
state.exceptionOrThrow() // Output: RuntimeException
```

```kotlin
val state = LoadableViewState.Success(Unit)
state.exceptionOrThrow() // Throws IllegalStateException
```

### `EditableViewState<Value>`

Represents the different states involved in editing data of type Value.

#### `Initial(Value)`

Represents the initial state of the view, before any changes have been made.

```kotlin
val initialState = EditableViewState.Initial(Unit)
```

#### `Saving(Value, Value)`

Represents the saving state of the view, indicating that data is currently being edited.

```kotlin
val savingState = EditableViewState.Saving(current = Unit, target = Unit)
```

#### `Success(Value, Value)`

Represents the successful state of editing data.

```kotlin
val successState = EditableViewState.Success(old = Unit, succeeded = Unit)
```

#### `Failure(Value, Value, Throwable)`

Represents the failure state of the view, containing an exception.

```kotlin
val failureState =
    EditableViewState.Failure(current = Unit, failed = Unit, exception = RuntimeException())
```

#### `isInitial()`

Checks if the current state is `EditableViewState.Initial`.

```kotlin
val state = EditableViewState.Initial(Unit)
state.isInitial() // Output: true
```

#### `isSaving()`

Checks if the current state is `EditableViewState.Saving`.

```kotlin
val state = EditableViewState.Saving(current = Unit, target = Unit)
state.isSaving() // Output: true
```

#### `isSuccess()`

Checks if the current state is `EditableViewState.Success`.

```kotlin
val state = EditableViewState.Success(old = Unit, succeeded = Unit)
state.isSuccess() // Output: true
```

#### `isFailure()`

Checks if the current state is `EditableViewState.Failure`.

```kotlin
val state = EditableViewState.Failure(current = Unit, failed = Unit, exception = RuntimeException())
state.isFailure() // Output: true
```

#### `onInitial(block: (Value) -> Unit)`

Executes the given `block` if the current state is `EditableViewState.Initial`.

```kotlin
val state = EditableViewState.Initial(Unit)
state.onInitial { println("Initial state reached") } // Output: Initial state reached
```

#### `onSaving(block: (Value, Value) -> Unit)`

Executes the given `block` if the current state is `EditableViewState.Saving`.

```kotlin
val state = EditableViewState.Saving(current = Unit, target = Unit)
state.onSaving { current, target -> println("Saving state reached") } // Output: Saving state reached
```

#### `onSuccess(block: (Value, Value) -> Unit)`

Executes the given `block` if the current state is `EditableViewState.Success`.

```kotlin
val state = EditableViewState.Success(old = Unit, succeeded = Unit)
state.onSuccess { old, succeeded -> println("Success state reached") } // Output: Success state reached
```

#### `onFailure(block: (Value, Value, Throwable) -> Unit)`

Executes the given `block` if the current state is `EditableViewState.Failure`.

```kotlin
val state = EditableViewState.Failure(current = Unit, failed = Unit, exception = RuntimeException())
state.onFailure { current, failed, exception -> println("Failure state reached") } // Output: Failure state reached
```

#### `relevantValue()`

Returns the relevant value based on the current state.

```kotlin
val state = EditableViewState.Initial("initial")
state.relevantValue() // Output: initial
```

```kotlin
val state = EditableViewState.Saving(current = "current", target = "target")
state.relevantValue() // Output: current
```

```kotlin
val state = EditableViewState.Success(old = "old", succeeded = "succeeded")
state.relevantValue() // Output: succeeded
```

```kotlin
val state = EditableViewState.Failure(
    current = "current",
    failed = "failed",
    exception = RuntimeException()
)
state.relevantValue() // Output: current
```

#### `exceptionOrNull()`

Returns the exception if the current state is `EditableViewState.Failure`, or `null` otherwise.

```kotlin
val state = EditableViewState.Failure(current = Unit, failed = Unit, exception = RuntimeException())
state.exceptionOrNull() // Output: RuntimeException
```

```kotlin
val state = EditableViewState.Success(old = Unit, succeeded = Unit)
state.exceptionOrNull() // Output: null
```

#### `exceptionOrThrow()`

Returns the exception if the current state is `EditableViewState.Failure`, or throws an
`IllegalStateException` otherwise.

```kotlin
val state = EditableViewState.Failure(current = Unit, failed = Unit, exception = RuntimeException())
state.exceptionOrThrow() // Output: RuntimeException
```

```kotlin
val state = EditableViewState.Success(old = Unit, succeeded = Unit)
state.exceptionOrThrow() // Throws IllegalStateException
```
