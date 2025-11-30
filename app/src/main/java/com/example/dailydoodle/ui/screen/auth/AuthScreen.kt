package com.example.dailydoodle.ui.screen.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailydoodle.ui.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    onSignInSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is com.example.dailydoodle.ui.viewmodel.AuthUiState.Authenticated) {
            onSignInSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Daily Doodle Chain",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        if (isSignUp) {
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Button(
            onClick = {
                if (isSignUp) {
                    viewModel.signUpWithEmail(email, password, displayName)
                } else {
                    viewModel.signInWithEmail(email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = uiState !is com.example.dailydoodle.ui.viewmodel.AuthUiState.Loading
        ) {
            if (uiState is com.example.dailydoodle.ui.viewmodel.AuthUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text(if (isSignUp) "Sign Up" else "Sign In")
            }
        }

        TextButton(
            onClick = { isSignUp = !isSignUp },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Sign Up")
        }

        if (uiState is com.example.dailydoodle.ui.viewmodel.AuthUiState.Error) {
            Text(
                text = (uiState as com.example.dailydoodle.ui.viewmodel.AuthUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
