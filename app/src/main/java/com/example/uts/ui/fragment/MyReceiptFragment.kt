package com.example.uts.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uts.adapter.MyReceiptItemAdapter
import com.example.uts.data.Receipt
import com.example.uts.databinding.FragmentMyReceiptBinding
import com.example.uts.ui.activity.DetailReceiptActivity
import com.example.uts.ui.activity.EditReceiptActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyReceiptFragment : Fragment() {

    private var _binding: FragmentMyReceiptBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMyReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchReceipts()
    }

    private fun fetchReceipts() {

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        db.collection("receipts")
            .whereEqualTo("idUser", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING) // Add this line
            .get()
            .addOnSuccessListener { documents ->
                var docName = ""
                val receipt = documents.map { document ->
                    docName = document.id
                    Receipt(
                        description = document["description"] as String,
                        id = document["id"] as String,
                        idUser = document["idUser"] as String,
                        user = document["user"] as String,
                        title = document["title"] as String,
                        date = document["timestamp"] as Long,
                        image = document["image"] as String,
                    )
                }

                val adapter = MyReceiptItemAdapter(receipt, requireContext())
                val recyclerView = binding.myReceiptRecyclerView
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.setHasFixedSize(true)
                recyclerView.adapter = adapter

                adapter.setOnItemClickCallback(object : MyReceiptItemAdapter.ReceiptClicked {
                    override fun onReceiptClicked(receipt: Receipt, position: Int) {
                        navigateToDetailReceipt(receipt)
                    }

                    override fun onEditClicked(receipt: Receipt, position: Int) {
                        val intent = Intent(requireContext(), EditReceiptActivity::class.java)
                        intent.putExtra("receipt", receipt)
                        intent.putExtra("docName", docName)
                        startActivity(intent)
                    }

                    override fun onDeleteClicked(receipt: Receipt, position: Int) {
                        popupDelete(docName)
                    }
                })
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
            }
    }

    private fun popupDelete(docName: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Receipt")
        builder.setMessage("Are you sure want to delete this receipt?")
        builder.setPositiveButton("Yes") { dialog, which ->
            db.collection("receipts").document(docName)
                .delete()
                .addOnSuccessListener { document ->
                    Toast.makeText(
                        requireContext(),
                        "Receipt deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Receipt failed to delete",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
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