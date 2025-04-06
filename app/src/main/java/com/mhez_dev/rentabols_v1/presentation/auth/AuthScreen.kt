package com.mhez_dev.rentabols_v1.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mhez_dev.rentabols_v1.R
import com.mhez_dev.rentabols_v1.ui.components.RentabolsButton
import com.mhez_dev.rentabols_v1.ui.components.RentabolsTextField
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthScreen(
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    var isSignIn by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onNavigateToHome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFC107), // colorPrimary
                        Color(0xFFd6a000)  // colorPrimaryDark
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo and App Name
            Image(
                painter = painterResource(id = R.drawable.logo_rentabols),
                contentDescription = "Rentabols Logo",
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Rentabols",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )

            Text(
                text = "Connect, Rent, Share",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isSignIn) "Welcome Back!" else "Create Account",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    RentabolsTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        leadingIcon = { Icon(Icons.Default.Email, "Email") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    RentabolsTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        leadingIcon = { Icon(Icons.Default.Lock, "Password") },
                        visualTransformation = PasswordVisualTransformation()
                    )

                    if (!isSignIn) {
                        Spacer(modifier = Modifier.height(16.dp))
                        RentabolsTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = "Name",
                            leadingIcon = { Icon(Icons.Default.Person, "Name") }
                        )
                    }

                    if (authState is AuthState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (authState as AuthState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    RentabolsButton(
                        text = if (isSignIn) "Sign In" else "Sign Up",
                        onClick = {
                            if (isSignIn) {
                                viewModel.signIn(email, password)
                            } else {
                                viewModel.signUp(email, password, name)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = when {
                            authState is AuthState.Loading -> false
                            isSignIn -> email.isNotBlank() && password.isNotBlank()
                            else -> email.isNotBlank() && password.isNotBlank() && name.isNotBlank()
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { isSignIn = !isSignIn }
                    ) {
                        Text(
                            text = if (isSignIn) {
                                "Don't have an account? Sign Up"
                            } else {
                                "Already have an account? Sign In"
                            },
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
