package com.example.uts.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.uts.data.Receipt
import com.example.uts.databinding.ActivityEditReceiptBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.UUID

class EditReceiptActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityEditReceiptBinding
    private var currentImageUri: Uri? = null
    private var userName = ""
    private var userId = ""
    private var receipt = Receipt()
    private var docName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request permission
        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        receipt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("receipt", Receipt::class.java)!!
        } else {
            intent.getParcelableExtra<Receipt>("receipt") as Receipt
        }
        docName = intent.getStringExtra("docName").toString()

        binding.receiptNameEdit.setText(receipt.title)
        binding.receiptDescEdit.setText(receipt.description)
        Glide.with(this)
            .load(receipt.image)
            .into(binding.receiptImagePreview)

        binding.receiptImageEdit.setText("Gambar Berhasil Dipilih")

        auth = Firebase.auth
        if (auth.currentUser?.displayName != "") {
            userName = auth.currentUser?.displayName ?: ""
            userId = auth.currentUser?.uid ?: ""
        } else {
            fetchUserData()
        }

        onClick()
    }

    // fetch user name
    private fun fetchUserData() {
        val user = Firebase.auth.currentUser
        val db = Firebase.firestore
        user?.uid?.let {
            db.collection("usersData").document(it)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        userName = document.getString("name").toString()
                        userId = document.getString("id").toString()
                    } else {
                        Log.d("Failed Profile", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("Failure Profile", "get failed with ", exception)
                }
        }
    }

    private fun fetchDataField() {
        val db = Firebase.firestore
        userId.let {
            db.collection("receipts")
                .whereEqualTo("id", it)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d("Success Profile", "DocumentSnapshot data: ${document.documents[0]}")

                        receipt = Receipt(
                            id = document.documents[0].getString("id").toString(),
                            idUser = document.documents[0].getString("idUser").toString(),
                            user = document.documents[0].getString("user").toString(),
                            title = document.documents[0].getString("title").toString(),
                            date = document.documents[0].getLong("timestamp")!!,
                            description = document.documents[0].getString("description").toString(),
                            image = document.documents[0].getString("image").toString(),
                        )

                        binding.receiptNameEdit.setText(document.documents[0].getString("title"))
                        binding.receiptDescEdit.setText(document.documents[0].getString("description"))

                        val image = document.documents[0].getString("image")
                        Glide.with(this)
                            .load(image)
                            .into(binding.receiptImagePreview)
                        binding.receiptImageEdit.setText("Gambar Berhasil Dipilih")

                    } else {
                        Log.d("Failed Profile", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("Failure Profile", "get failed with ", exception)
                }
        }
    }

    private fun onClick() {
        binding.receiptImageEdit.setOnClickListener {
            startGallery()
        }

        binding.receiptImageInput.setOnClickListener {
            startGallery()
        }

        binding.editReceiptButton.setOnClickListener {
            if (validateInput()) {
                // Get data from input
                val title = binding.receiptNameEdit.text.toString()
                val description = binding.receiptDescEdit.text.toString()

                if (binding.receiptImageEdit.text.toString() == "Gambar Berhasil Dipilih") {
                    receipt.id?.let { it1 -> updateReceipt(it1, title, description, receipt.image!!) }
                } else {
                    // Generate a random UUID for the image
                    val id = UUID.randomUUID().toString()

                    // Create a reference to the location where you want to upload the image
                    val storageRef = Firebase.storage.reference.child("receipts/$id")

                    // Upload the image to Firebase Storage
                    storageRef.putFile(currentImageUri!!)
                        .addOnSuccessListener {
                            // After the upload is successful, get the download URL of the image
                            storageRef.downloadUrl.addOnSuccessListener { uri ->
                                receipt.id?.let { it1 -> updateReceipt(it1, title, description, uri.toString()) }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.w("Storage", "Error uploading image", e)
                            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            }
        }
    }

    private fun updateReceipt(id: String, title: String, description: String, imageUri: String) {
        // Create receipt object
        val receipts = hashMapOf(
            "id" to id,
            "idUser" to receipt.idUser,
            "user" to userName,
            "title" to title,
            "timestamp" to System.currentTimeMillis(),
            "description" to description,
            "image" to imageUri
        )

        // Add receipt to Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("receipts").document(docName)
            .update(receipts as Map<String, Any>)
            .addOnSuccessListener {
                Log.d("Firestore", "DocumentSnapshot added with ID: $id")
                Toast.makeText(this, "Receipt update successfully", Toast.LENGTH_SHORT).show()
                navigateToHomeScreen()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding document", e)
                Toast.makeText(this, "Failed to update receipt", Toast.LENGTH_SHORT).show()
            }
    }

    // Navigate to HomeActivity
    private fun navigateToHomeScreen() {
        // Navigate to HomeActivity
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
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

        return true
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
            binding.receiptImagePreview.visibility = View.VISIBLE
            binding.receiptImagePreview.setImageURI(uri)
            binding.receiptImageEdit.setText("Gambar Berhasil Diubah")
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}