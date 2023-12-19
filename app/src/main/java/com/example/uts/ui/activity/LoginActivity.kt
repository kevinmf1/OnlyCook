package com.example.uts.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uts.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if user is already logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is already logged in, navigate to HomeActivity
            navigateToHomeScreen()
        }

        onClick()
    }

    private fun onClick() {

        binding.login.setOnClickListener {
            if (validateInput()) {
                userLogin(binding.emailInput.text.toString(), binding.passwordInput.text.toString())
            }
        }

        binding.navigateToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun userLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        Toast.makeText(this, "Login berhasil", Toast.LENGTH_LONG).show()
                        navigateToHomeScreen()
                    }
                } else {
                    Toast.makeText(this, "Login failed", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun navigateToHomeScreen() {
        // Navigate to HomeActivity
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    // Validate input
    private fun validateInput(): Boolean {
        if (binding.emailInput.text.toString().isEmpty()) {
            binding.emailInput.error = "Tolong masukkan email"
            binding.emailInput.requestFocus()
            return false
        }

        if (binding.passwordInput.text.toString().isEmpty()) {
            binding.passwordInput.error = "Tolong masukkan email"
            binding.passwordInput.requestFocus()
            return false
        }

        return true
    }
}