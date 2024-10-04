package bg.dalexiev.todo.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import bg.dalexiev.todo.resources.Res
import bg.dalexiev.todo.resources.login_email_hint
import bg.dalexiev.todo.resources.login_hide_password
import bg.dalexiev.todo.resources.login_password_hint
import bg.dalexiev.todo.resources.login_show_password
import bg.dalexiev.todo.resources.login_sign_in_button
import bg.dalexiev.todo.resources.login_title
import bg.dalexiev.todo.resources.visibility
import bg.dalexiev.todo.resources.visibility_off
import bg.dalexiev.todo.task.BackHandler
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginCompleted: () -> Unit,
    onExitApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onExitApp)

    val uiState by viewModel.uiState.collectAsState()

    if (uiState.loginResult is LoginResult.Success) {
        val currentOnLoginCompleted by rememberUpdatedState(onLoginCompleted)
        LaunchedEffect(Unit) {
            currentOnLoginCompleted()
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(text = stringResource(Res.string.login_title))

        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.onEmailChanged(it) },
            label = { Text(text = stringResource(Res.string.login_email_hint)) },
            isError = uiState.loginResult is LoginResult.Failure,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        var passwordVisible by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.onPasswordChanged(it) },
            label = { Text(text = stringResource(Res.string.login_password_hint)) },
            isError = uiState.loginResult is LoginResult.Failure,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Res.drawable.visibility_off else Res.drawable.visibility
                val description = if (passwordVisible) Res.string.login_hide_password else Res.string.login_show_password

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(image),
                        contentDescription = stringResource(description)
                    )
                }
            }
        )

        Button(
            enabled = uiState.isSignInButtonEnabled,
            onClick = { viewModel.onSignInButtonClicked() }
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            } else {
                Text(text = stringResource(Res.string.login_sign_in_button))
            }
        }
    }
}