package com.example.uts.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uts.adapter.ReceiptItemAdapter
import com.example.uts.data.Receipt
import com.example.uts.databinding.FragmentReceiptBinding
import com.example.uts.ui.activity.AddReceiptActivity
import com.example.uts.ui.activity.DetailReceiptActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ReceiptFragment : Fragment() {

    private var _binding: FragmentReceiptBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onClick()
        fetchReceipts()
    }

    private fun fetchReceipts() {
        db.collection("receipts")
            .orderBy("timestamp", Query.Direction.DESCENDING) // Add this line
            .get()
            .addOnSuccessListener { documents ->
                val receipts = documents.map { document ->
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

                val adapter = ReceiptItemAdapter(receipts, requireContext())
                val recyclerView = binding.receiptList
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.setHasFixedSize(true)
                recyclerView.adapter = adapter
                adapter.setOnItemClickCallback(object : ReceiptItemAdapter.ReceiptClicked {
                    override fun onReceiptClicked(receipt: Receipt, position: Int) {
                        navigateToDetailReceipt(receipt)
                    }
                })
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
            }
    }

    private fun navigateToDetailReceipt(receipt: Receipt) {
        val intent = Intent(requireContext(), DetailReceiptActivity::class.java)
        intent.putExtra("receipt", receipt)
        startActivity(intent)
    }

    private fun onClick() {
        binding.fabAddReceipt.setOnClickListener {
            val intent = Intent(requireContext(), AddReceiptActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}