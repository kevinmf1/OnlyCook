package com.example.uts.ui.activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.uts.R
import com.example.uts.data.Receipt
import com.example.uts.databinding.ActivityDetailReceiptBinding
import com.example.uts.utils.dateConverter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class DetailReceiptActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailReceiptBinding
    private val db = FirebaseFirestore.getInstance()
    private var receipt = Receipt()
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        receipt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("receipt", Receipt::class.java)!!
        } else {
            intent.getParcelableExtra<Receipt>("receipt")!!
        }

        checkFavoriteItem(receipt)

        binding.receiptTitle.text = receipt.title
        binding.dateReceipt.text = dateConverter(receipt.date!!)
        binding.authorReceipt.text = receipt.user
        Glide.with(this)
            .load(receipt.image)
            .into(binding.receiptImage)
        binding.descReceipt.text = receipt.description

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.favoriteButton.setOnClickListener {
            favoriteItem(receipt)
        }
    }

    private fun checkFavoriteItem(receipt: Receipt) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = db.collection("users").document(currentUserId!!)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val productIdFavorite = document["productIdFavorite"] as? List<String> ?: listOf()
                    if (productIdFavorite.contains(receipt.id)) {
                        binding.favoriteButton.setImageResource(R.drawable.baseline_favorite_red_24)
                        isFavorite = true
                    } else {
                        binding.favoriteButton.setImageResource(R.drawable.baseline_favorite_border_24)
                        isFavorite = false
                    }
                }
            }
    }

    private fun favoriteItem(receipt: Receipt) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = db.collection("users").document(currentUserId!!)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    if (isFavorite) {
                        binding.favoriteButton.setImageResource(R.drawable.baseline_favorite_border_24)
                        isFavorite = false
                        userRef.update("productIdFavorite", FieldValue.arrayRemove(receipt.id))
                            .addOnSuccessListener {
                                Log.d("Firestore", "DocumentSnapshot successfully updated!")
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error updating document", e)
                            }
                    } else {
                        binding.favoriteButton.setImageResource(R.drawable.baseline_favorite_red_24)
                        isFavorite = true
                        userRef.update("productIdFavorite", FieldValue.arrayUnion(receipt.id))
                            .addOnSuccessListener {
                                Log.d("Firestore", "DocumentSnapshot successfully updated!")
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error updating document", e)
                            }
                    }
                } else {
                    // If the document doesn't exist, create a new document
                    val user = hashMapOf(
                        "userId" to currentUserId,
                        "productIdFavorite" to listOf(receipt.id)
                    )

                    userRef.set(user)
                        .addOnSuccessListener {
                            Log.d("Firestore", "DocumentSnapshot successfully written!")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error writing document", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error getting document", e)
            }
    }
}