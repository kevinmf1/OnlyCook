package com.example.uts.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.uts.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityRegisterBinding
    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request permission
        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        onClick()
    }

    // Validate input
    private fun validateInput(): Boolean {
        if (binding.nameInput.text.toString().isEmpty()) {
            binding.nameInput.error = "Tolong masukkan nama"
            binding.nameInput.requestFocus()
            return false
        }

        if (binding.emailInput.text.toString().isEmpty()) {
            binding.emailInput.error = "Tolong masukkan email"
            binding.emailInput.requestFocus()
            return false
        }

        if (binding.passwordInput.text.toString().isEmpty()) {
            binding.passwordInput.error = "Tolong masukkan password"
            binding.passwordInput.requestFocus()
            return false
        }

        if (binding.imageInput.text.toString() != "Gambar Berhasil Dipilih") {
            binding.imageInput.error = "Tolong masukkan gambar anda"
            return false
        }

        return true
    }

    private fun onClick() {
        binding.registerButton.setOnClickListener {
            if (validateInput()) {
                createUser()
            }
        }

        binding.navigateToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.imagePreview.setOnClickListener {
            startGallery()
        }

        binding.imageInput.setOnClickListener {
            startGallery()
        }
    }

    private fun navigateToHomeScreen() {
        // Navigate to HomeActivity
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun createUser() {
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("Register", "createUserWithEmail:success")
                    val user = auth.currentUser

                    // Get user id
                    val id = user?.uid

                    // Create a reference to the location where you want to upload the image
                    val storageRef = Firebase.storage.reference.child("users/$id")

                    // Upload the image to Firebase Storage
                    storageRef.putFile(currentImageUri!!)
                        .addOnSuccessListener {
                            // After the upload is successful, get the download URL of the image
                            storageRef.downloadUrl.addOnSuccessListener { uri ->
                                // Create user object
                                val userMap = hashMapOf(
                                    "id" to id,
                                    "name" to binding.nameInput.text.toString(),
                                    "email" to email,
                                    "password" to password,
                                    "gambar" to uri.toString()
                                )

                                // Add user to Firestore
                                val db = Firebase.firestore
                                user?.uid?.let {
                                    db.collection("usersData").document(it)
                                        .set(userMap)
                                        .addOnSuccessListener {
                                            Log.d(
                                                "Success Register",
                                                "DocumentSnapshot successfully written!"
                                            )
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w(
                                                "Failed Register",
                                                "Error writing document",
                                                e
                                            )
                                        }
                                }
                                navigateToHomeScreen()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.w("Storage", "Error uploading image", e)
                            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT)
                                .show()
                        }
                } else {
                    Toast.makeText(this, "Register failed", Toast.LENGTH_LONG).show()
                }
            }
    }

    // Request permission
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(
                    this, "Permission request granted", Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this, "Permission request denied", Toast.LENGTH_LONG
                ).show()
            }
        }

    // Check if all permission specified in the manifest have been granted
    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    // Start Gallery
    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    // Get image from gallery
    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            binding.imagePreview.visibility = View.VISIBLE
            binding.imagePreview.setImageURI(uri)
            binding.imageInput.setText("Gambar Berhasil Dipilih")
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}