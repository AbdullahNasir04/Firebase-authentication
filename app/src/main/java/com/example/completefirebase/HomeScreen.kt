package com.example.completefirebase

import android.content.Intent
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun HomeScreen(navController: NavController)
{

    var phoneNum by remember{ mutableStateOf("") }
    val bgColor = Color(0xFFECFADC)
    val tColor = Color(0xff284728)


    var user by remember{ mutableStateOf(Firebase.auth.currentUser) }
    val launcher = authLauncher(onAuthComplete = {result ->
        user = result.user },
        onAuthError = {user = null
        }
    )

    val token = stringResource(R.string.web_id)
    val context = LocalContext.current

    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary))
    {
        if (user == null)
        {
            Text(text = "Not Logged In",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White)

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Get logIn With Google",
                fontSize = 20.sp,
                color = Color.Gray)

            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {
                val gso =
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(token)
                        .requestEmail()
                        .build()
                val gsc = GoogleSignIn.getClient(context,gso)
                launcher.launch(gsc.signInIntent)
            },
                colors = ButtonDefaults.buttonColors(Color.White)
            ) {
                Row (verticalAlignment = Alignment.CenterVertically)
                {
                    Image(
                        painter = painterResource(id = R.drawable.img),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "SignIn With Google",
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Get logIn With OTP",
                fontSize = 20.sp,
                color = Color.Gray)

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(value = phoneNum, onValueChange = {if (it.length <=10)phoneNum = it },
                label = { Text(text = "enter phone number", color = tColor)},
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = bgColor,
                    unfocusedContainerColor = bgColor,
                    focusedIndicatorColor = tColor,
                    cursorColor = tColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                textStyle = TextStyle(color = tColor)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = {onLoginClicked(context,navController,phoneNum)
            {
                Log.d("phoneBook","sending opt")
                navController.navigate("otp")
            }},
                colors = ButtonDefaults.buttonColors(Color.White)
            ) {
                Row (verticalAlignment = Alignment.CenterVertically)
                {
                    Image(
                        painter = painterResource(id = R.drawable.img_1),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "SignIn With OTP",
                        color = Color.Black
                    )
                }
            }
        }


        else
        {
          SuccessScreen(navController = navController)
        }
    }
}




@Composable
fun authLauncher(
    onAuthComplete:(AuthResult)->Unit,
    onAuthError:(ApiException)->Unit): ManagedActivityResultLauncher<Intent, ActivityResult>
{
    val scope = rememberCoroutineScope()
    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
            result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val credential = GoogleAuthProvider.getCredential(account.idToken!!,null)
            scope.launch {
                val authResult = Firebase.auth.signInWithCredential(credential).await()
                onAuthComplete(authResult)
            }
        }catch (e: ApiException)
        {
            onAuthError(e)
        }
    }
}


