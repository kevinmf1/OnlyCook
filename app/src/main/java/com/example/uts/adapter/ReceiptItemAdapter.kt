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

class ReceiptItemAdapter(private val receipt: List<Receipt>, private val context: Context) :
    RecyclerView.Adapter<ReceiptItemAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val receiptImage: ImageView = view.findViewById(R.id.receiptImage)
        val receiptName: TextView = view.findViewById(R.id.receiptName)
        val receiptDesc: TextView = view.findViewById(R.id.receiptDesc)
        val receiptDate: TextView = view.findViewById(R.id.receiptDate)
    }

    private lateinit var onItemClickCallback: ReceiptClicked

    fun setOnItemClickCallback(onItemClickCallback: ReceiptClicked) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface ReceiptClicked {
        fun onReceiptClicked(receipt: Receipt, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_receipt, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val receiptData = receipt[position]

        holder.receiptName.text = receiptData.title
        holder.receiptDesc.text =
            receiptData.description?.replaceNewlineWithSpace()?.let { maxCharacter(it, 60) }
        holder.receiptDate.text = dateConverter(receiptData.date!!)
        Glide.with(context).load(receiptData.image).into(holder.receiptImage)

        holder.itemView.setOnClickListener {
            onItemClickCallback.onReceiptClicked(receipt[position], position)
        }
    }

    override fun getItemCount() = receipt.size
}