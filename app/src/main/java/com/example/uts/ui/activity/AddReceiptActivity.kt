package com.example.uts.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.uts.databinding.ActivityAddReceiptBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.UUID

class AddReceiptActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityAddReceiptBinding
    private var currentImageUri: Uri? = null
    private var userName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request permission
        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        auth = Firebase.auth
        if (auth.currentUser?.displayName != "") {
            userName = auth.currentUser?.displayName ?: ""
        } else {
            fetchUserName()
        }

        onClick()
    }

    // fetch user name
    private fun fetchUserName() {
        val user = Firebase.auth.currentUser
        val db = Firebase.firestore
        user?.uid?.let {
            db.collection("usersData").document(it)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        userName = document.getString("name").toString()
                    } else {
                        Log.d("Failed Profile", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("Failure Profile", "get failed with ", exception)
                }
        }
    }

    // Validate input
    private fun validateInput(): Boolean {
        if (binding.receiptNameEdit.text.toString().isEmpty()) {
            binding.receiptNameEdit.error = "Tolong masukkan nama resep"
            binding.receiptNameEdit.requestFocus()
            return false
        }

        if (binding.receiptDescEdit.text.toString().isEmpty()) {
            binding.receiptDescEdit.error = "Tolong masukkan deskripsi resep"
            binding.receiptDescEdit.requestFocus()
            return false
        }

        if (binding.receiptImageEdit.text.toString() != "Gambar Berhasil Dipilih") {
            binding.receiptImageEdit.error = "Tolong masukkan gambar resep"
            return false
        }

        return true
    }

    // click handler
    private fun onClick() {

        binding.addReceiptButton.setOnClickListener {
            if (validateInput()) {
                // Get data from input
                val title = binding.receiptNameEdit.text.toString()
                val description = binding.receiptDescEdit.text.toString()
                val idUser = auth.currentUser?.uid ?: ""

                // Generate a random UUID for the image and the document
                val id = UUID.randomUUID().toString()

                // Create a reference to the location where you want to upload the image
                val storageRef = Firebase.storage.reference.child("receipts/$id")

                // Upload the image to Firebase Storage
                storageRef.putFile(currentImageUri!!)
                    .addOnSuccessListener {
                        // After the upload is successful, get the download URL of the image
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            // Create receipt object
                            val receipt = hashMapOf(
                                "id" to id,
                                "idUser" to idUser,
                                "user" to userName,
                                "title" to title,
                                "timestamp" to System.currentTimeMillis(),
                                "description" to description,
                                "image" to uri.toString()
                            )

                            // Add receipt to Firestore
                            val db = FirebaseFirestore.getInstance()
                            db.collection("receipts")
                                .add(receipt)
                                .addOnSuccessListener { documentReference ->
                                    Log.d(
                                        "Firestore",
                                        "DocumentSnapshot added with ID: ${documentReference.id}"
                                    )
                                    Toast.makeText(
                                        this,
                                        "Receipt added successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navigateToHomeScreen()
                                }
                                .addOnFailureListener { e ->
                                    Log.w("Firestore", "Error adding document", e)
                                    Toast.makeText(
                                        this,
                                        "Failed to add receipt",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w("Storage", "Error uploading image", e)
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Select image from gallery
        binding.receiptImageInput.setOnClickListener {
            startGallery()
        }

        binding.receiptImageEdit.setOnClickListener {
            startGallery()
        }
    }

    // Navigate to HomeActivity
    private fun navigateToHomeScreen() {
        // Navigate to HomeActivity
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
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
            binding.receiptImagePreview.visibility = VISIBLE
            binding.receiptImagePreview.setImageURI(uri)
            binding.receiptImageEdit.setText("Gambar Berhasil Dipilih")
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}