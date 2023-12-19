package com.example.uts.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uts.R
import com.example.uts.data.Receipt
import com.example.uts.utils.dateConverter
import com.example.uts.utils.maxCharacter
import com.example.uts.utils.replaceNewlineWithSpace

class MyReceiptItemAdapter(private val receipt: List<Receipt>, private val context: Context) :
    RecyclerView.Adapter<MyReceiptItemAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val receiptImage: ImageView = view.findViewById(R.id.receiptImage)
        val receiptName: TextView = view.findViewById(R.id.receiptName)
        val receiptDesc: TextView = view.findViewById(R.id.receiptDesc)
        val receiptDate: TextView = view.findViewById(R.id.receiptDate)
        val receiptEdit: ImageView = view.findViewById(R.id.editButton)
        val receiptDelete: ImageView = view.findViewById(R.id.deleteButton)
    }

    private lateinit var onItemClickCallback: ReceiptClicked
    private lateinit var onEditClickCallback: ReceiptClicked
    private lateinit var onDeleteClickCallback: ReceiptClicked

    fun setOnItemClickCallback(onItemClickCallback: ReceiptClicked) {
        this.onItemClickCallback = onItemClickCallback
        this.onEditClickCallback = onItemClickCallback
        this.onDeleteClickCallback = onItemClickCallback
    }

    interface ReceiptClicked {
        fun onReceiptClicked(receipt: Receipt, position: Int)
        fun onEditClicked(receipt: Receipt, position: Int)
        fun onDeleteClicked(receipt: Receipt, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_my_receipt, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val receiptData = receipt[position]

        holder.receiptName.text = receiptData.title
        holder.receiptDesc.text = receiptData.description?.replaceNewlineWithSpace()?.let { maxCharacter(it, 60) }
        holder.receiptDate.text = dateConverter(receiptData.date!!)
        Glide.with(context).load(receiptData.image).into(holder.receiptImage)

        holder.itemView.setOnClickListener {
            onItemClickCallback.onReceiptClicked(receipt[position], position)
        }

        holder.receiptEdit.setOnClickListener {
            onEditClickCallback.onEditClicked(receipt[position], position)
        }

        holder.receiptDelete.setOnClickListener {
            onDeleteClickCallback.onDeleteClicked(receipt[position], position)
        }
    }

    override fun getItemCount() = receipt.size
}