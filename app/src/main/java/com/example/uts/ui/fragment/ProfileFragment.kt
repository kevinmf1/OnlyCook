package com.example.uts.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.uts.databinding.FragmentProfileBinding
import com.example.uts.ui.activity.LoginActivity
import com.example.uts.ui.compose.AboutActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        displayData()
        onClick()
    }

    private fun displayData() {
        val user = auth.currentUser
        if (user?.displayName != "") {
            // Display user's photo
            Glide.with(requireContext())
                .load(user?.photoUrl)
                .into(binding.profileImage)

            // Display user's name
            binding.userName.text = user?.displayName

            // Display user's email
            binding.userEmail.text = user?.email
        } else {
            fetchAndDisplayUser()
        }
    }

    // fetch user name
    private fun fetchAndDisplayUser() {
        val user = Firebase.auth.currentUser
        val db = Firebase.firestore
        user?.uid?.let {
            db.collection("usersData").document(it)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d("Success Profile", "DocumentSnapshot data: ${document.data}")
                        binding.userName.text = document.getString("name")
                        binding.userEmail.text = document.getString("email")

                        Glide.with(requireContext())
                            .load(document.getString("gambar"))
                            .into(binding.profileImage)

                    } else {
                        Log.d("Failed Profile", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("Failure Profile", "get failed with ", exception)
                }
        }
    }

    private fun popUp() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Keluar Aplikasi")
        builder.setMessage("Apakah anda yakin ingin keluar dari aplikasi?")
        builder.setPositiveButton("Iya") { _, _ ->
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            activity?.finishAffinity()
        }
        builder.setNegativeButton("Tidak") { _, _ -> }
        builder.show()
    }

    private fun onClick() {
        binding.btnLogout.setOnClickListener {
            popUp()
        }
        binding.tentangAplikasi.setOnClickListener {
            val intent = Intent(requireContext(), AboutActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}