package com.example.completefirebase

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

@Composable
fun NavGraph(modifier : Modifier)
{
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home" )
    {

        composable("home"){ HomeScreen(navController = navController)}
        composable("otp"){ OtpScreen(navController = navController) }
        composable("success"){ SuccessScreen(navController = navController) }
    }
}

val auth = FirebaseAuth.getInstance()
var storeVerificationId = ""


fun signInWithPhoneAuthCredential(
    context: Context,
    credential: PhoneAuthCredential,
    navController: NavController
)
{
    auth.signInWithCredential(credential)
        .addOnCompleteListener(context as Activity)
        {
                task ->
            if (task.isSuccessful)
            {
                Toast.makeText(context,"login successfully", Toast.LENGTH_LONG).show()
                navController.navigate("success")
                val user = task.result?.user
            }else
            {
                if (task.exception is FirebaseAuthInvalidCredentialsException)
                {
                    Toast.makeText(context,"wrong O.T.P", Toast.LENGTH_LONG).show()
                }
            }
        }
}

fun onLoginClicked(
    context: Context,
    navController: NavController,
    phoneNumber:String,
    onCodeSend:()->Unit) {
    auth.setLanguageCode("en")
    val callBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            Log.d("phoneBook", "verification completed")
            signInWithPhoneAuthCredential(context, p0, navController)
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            Log.d("phoneBook", "verification failed$p0")
        }

        override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
            Log.d("phoneBook", "code sent$p0")
            storeVerificationId = p0
            onCodeSend()
        }
    }

    val option = PhoneAuthOptions.newBuilder(auth)
        .setPhoneNumber("+91$phoneNumber")
        .setTimeout(60L, TimeUnit.SECONDS)
        .setActivity(context as Activity)
        .setCallbacks(callBack)
        .build()
    PhoneAuthProvider.verifyPhoneNumber(option)
}

fun verifyPhoneNumberWithCod(
    context: Context,
    verificationId:String,
    code:String,
    navController: NavController
)
{
    val p0 = PhoneAuthProvider.getCredential(verificationId,code)
    signInWithPhoneAuthCredential(context,p0,navController)
}