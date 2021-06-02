package com.example.firebaseauth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.firebaseauth.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var resetPasswordBinding: ActivityResetPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resetPasswordBinding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(resetPasswordBinding.root)
        resetPasswordBinding.apply {
            btnResetPassword.setOnClickListener {
                val emailUser = etEmail.text.toString().trim()
                if (emailUser.isEmpty()) {
                    etEmail.error = "Please fill the email!"
                    etEmail.requestFocus()
                    return@setOnClickListener
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(emailUser).matches()) {
                    etEmail.error = "Email is not valid!"
                    etEmail.requestFocus()
                    return@setOnClickListener
                }
                FirebaseAuth.getInstance().sendPasswordResetEmail(emailUser).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(
                            this@ResetPasswordActivity,
                            "Verify your email to reset password",
                            Toast.LENGTH_LONG
                        ).show()
                        Intent(
                            this@ResetPasswordActivity,
                            LoginActivity::class.java
                        ).also { intent ->
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(
                            this@ResetPasswordActivity,
                            "${it.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}