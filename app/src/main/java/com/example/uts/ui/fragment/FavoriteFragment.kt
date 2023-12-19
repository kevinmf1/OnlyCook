package com.example.uts.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uts.adapter.ReceiptItemAdapter
import com.example.uts.data.Receipt
import com.example.uts.databinding.FragmentFavoriteBinding
import com.example.uts.ui.activity.DetailReceiptActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchReceipts()
        displayData()
    }

    private val receiptsLiveData = MutableLiveData<List<Receipt>>()

    private fun fetchReceipts() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val userRef = db.collection("users").document(currentUserId!!)

            userRef.get()
                .addOnSuccessListener { document ->
                    if (document["productIdFavorite"] != null) {
                        binding.favoriteReceiptRecyclerView.visibility = View.VISIBLE
                        binding.favoriteReceiptEmpty.visibility = View.GONE

                        val favoriteProductIds =
                            document["productIdFavorite"] as? List<String> ?: listOf()
                        val receipts = mutableListOf<Receipt>()

                        for (id in favoriteProductIds) {
                            db.collection("receipts").whereEqualTo("id", id)
                                .get()
                                .addOnSuccessListener { receiptDocument ->

                                    val receipt = receiptDocument.map { document ->
                                        Receipt(
                                            id = document["id"] as String,
                                            idUser = document["idUser"] as String,
                                            user = document["user"] as String,
                                            title = document["title"] as String,
                                            date = document["timestamp"] as Long,
                                            description = document["description"] as String,
                                            image = document["image"] as String,
                                        )
                                    }

                                    receipts.addAll(receipt)
                                    receiptsLiveData.value = receipts
                                }
                                .addOnFailureListener { exception ->
                                    Log.w("Firestore", "Error getting document: ", exception)
                                }
                        }
                    } else {
                        binding.favoriteReceiptRecyclerView.visibility = View.GONE
                        binding.favoriteReceiptEmpty.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("Firestore", "Error getting document: ", exception)
                }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(activity?.applicationContext, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun displayData() {
        receiptsLiveData.observe(viewLifecycleOwner) { receipts ->
            val adapter = ReceiptItemAdapter(receipts, activity?.applicationContext!!)
            val recyclerView = binding.favoriteReceiptRecyclerView
            recyclerView.layoutManager = LinearLayoutManager(activity?.applicationContext)
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = adapter
            adapter.setOnItemClickCallback(object :
                ReceiptItemAdapter.ReceiptClicked {
                override fun onReceiptClicked(receipt: Receipt, position: Int) {
                    navigateToDetailReceipt(receipt)
                }
            })
        }
    }

    private fun navigateToDetailReceipt(receipt: Receipt) {
        val intent = Intent(requireContext(), DetailReceiptActivity::class.java)
        intent.putExtra("receipt", receipt)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}