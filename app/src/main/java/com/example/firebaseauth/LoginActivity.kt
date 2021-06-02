package com.example.firebaseauth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.firebaseauth.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.log

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var loginBinding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)

        auth = FirebaseAuth.getInstance()
        loginBinding.apply {
            btnLogin.setOnClickListener {
                val emailUser = etEmail.text.toString().trim()
                val passUser = etPassword.text.toString().trim()
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
                if (passUser.isEmpty() || passUser.length < 6) {
                    etPassword.error = "Password must more tha 6 characters"
                    etPassword.requestFocus()
                    return@setOnClickListener
                }
                loginUser(emailUser, passUser)
            }
        }

        loginBinding.btnRegister.setOnClickListener {
            Intent(this, RegisterActivity::class.java).also {
                startActivity(it)
            }
        }
        loginBinding.btnForgotPassword.setOnClickListener {
            Intent(this, ResetPasswordActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    private fun loginUser(emailUser: String, passUser: String) {
        auth.signInWithEmailAndPassword(emailUser, passUser).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                Intent(this, HomeActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                }
            } else {
                Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        //jika sudah pernah login maka akan langsung masuk ke home
        if(auth.currentUser != null){
            Intent(this, RegisterActivity::class.java).also {
                startActivity(it)
            }
        }
    }
}