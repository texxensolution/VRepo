package com.spmadrid.vrepo.presentation.screens

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spmadrid.vrepo.R
import com.spmadrid.vrepo.presentation.components.ShiningFloatingNotification
import com.spmadrid.vrepo.presentation.ui.theme.Blue100
import com.spmadrid.vrepo.presentation.ui.theme.Blue400
import com.spmadrid.vrepo.presentation.ui.theme.Blue50
import com.spmadrid.vrepo.presentation.ui.theme.Blue600
import com.spmadrid.vrepo.presentation.ui.theme.Gray400
import com.spmadrid.vrepo.presentation.ui.theme.Roboto
import com.spmadrid.vrepo.presentation.viewmodel.AuthenticateViewModel
import com.spmadrid.vrepo.presentation.viewmodel.CameraViewModel
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.regular.Eye
import compose.icons.fontawesomeicons.solid.Eye
import compose.icons.fontawesomeicons.solid.Lock


@Composable
fun LoginScreen(
    context: Context,
    cameraViewModel: CameraViewModel,
    authViewModel: AuthenticateViewModel
) {
//    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val username by authViewModel.username.collectAsState()
    val password by authViewModel.password.collectAsState()
    val loading by authViewModel.loading.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }

    val activity = context as Activity
    val showNotification = cameraViewModel.showNotification.collectAsState()
    val notification = cameraViewModel.notification.collectAsState()
    val buttonColor = if (isPressed) Color.Blue.copy(0.5f) else Color.Blue.copy(0.02f)
    val currentCtx = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Blue50),
        contentAlignment = Alignment.Center
    ) {
        notification.value?.let {
            ShiningFloatingNotification(
                showNotification = showNotification.value,
                context = context,
                notificationEvent = it
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 16.dp, vertical = 32.dp)
                .shadow(1.dp, shape = RoundedCornerShape(12.dp))
                .background(color = Color.White.copy(alpha = 0.95f), shape = RoundedCornerShape(12.dp))
                .clip(shape = RoundedCornerShape(12.dp))
                .border(2.dp, Color.White, RoundedCornerShape(12.dp)), // Add a border,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.padding(0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    context.getString(R.string.app_name),
                    fontWeight = FontWeight.Bold,
                    fontSize = 42.sp,
                    fontFamily = Roboto,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp).padding(top = 10.dp),
                    color = Blue600
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { authViewModel.updateUsername(it) },
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User Icon") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = Gray400,
                        focusedTrailingIconColor = Gray400,
                        focusedLeadingIconColor = Gray400,
                        unfocusedBorderColor = Blue100,
                        unfocusedContainerColor = Blue50,
                        unfocusedLeadingIconColor = Gray400,
                        unfocusedLabelColor = Gray400,
                        unfocusedTextColor = Gray400,
                        unfocusedPlaceholderColor = Gray400,
                        focusedBorderColor = Blue400,
                        focusedTextColor = Blue400,
                        focusedLabelColor = Blue400
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { authViewModel.updatePassword(it) },
                    label = { Text("Password") },
                    leadingIcon = { Icon(FontAwesomeIcons.Solid.Lock, contentDescription = "Lock Icon", modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) FontAwesomeIcons.Solid.Eye else FontAwesomeIcons.Regular.Eye
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(image, contentDescription = "Toggle Password Visibility", modifier = Modifier.size(18.dp))
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = Gray400,
                        focusedTrailingIconColor = Gray400,
                        focusedLeadingIconColor = Gray400,
                        unfocusedBorderColor = Blue100,
                        unfocusedContainerColor = Blue50,
                        unfocusedLeadingIconColor = Gray400,
                        unfocusedLabelColor = Gray400,
                        unfocusedTextColor = Gray400,
                        unfocusedPlaceholderColor = Gray400,
                        focusedBorderColor = Blue400,
                        focusedTextColor = Blue400,
                        focusedLabelColor = Blue400
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))
                // Login Button
                Button(
                    onClick = {
                        authViewModel.signInWithUsernameAndPassword(currentCtx, username, password)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = username.isNotBlank() && password.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue600,
                        contentColor = Blue50,
                        disabledContainerColor = Blue600.copy(alpha = 0.7f),
                        disabledContentColor = Blue50
                    )
                ) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Blue100)
                    } else {
                        Text("LOGIN", fontWeight = FontWeight.Light)
                    }
                }

                Text("OR",
                    color = Gray400,
                    modifier = Modifier.padding(vertical = 6.dp),
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp
                )

                Button(
                    modifier = Modifier
                        .fillMaxWidth().padding(bottom = 16.dp),
//                        .padding(14.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = Color.Black,
                    ),
                    border = BorderStroke(1.dp, Blue600),
                    onClick = {
                        authViewModel.signInWithLark(activity)
                    },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.lark_logo),
                            contentDescription = "VRepo Logo",
                            modifier = Modifier.padding(vertical = 4.dp),
                            alignment = Alignment.Center
                        )
                        Spacer(modifier = Modifier.padding(end = 8.dp))
                        Text(
                            "LOGIN WITH LARK",
                            color = Blue600,
                            fontFamily = Roboto,
                            fontWeight = FontWeight.Light
                        )
                    }
                }
            }
        }
    }
}