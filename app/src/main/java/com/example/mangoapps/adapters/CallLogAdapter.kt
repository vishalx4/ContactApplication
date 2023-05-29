package com.example.mangoapps.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.mangoapps.R
import com.example.mangoapps.databinding.ContactCardBinding
import com.example.mangoapps.models.CallLogs

class CallLogAdapter(private val listOfCallLog: List<CallLogs>, private val context: Context?): RecyclerView.Adapter<CallLogAdapter.CallLogViewHolder>() {
    class CallLogViewHolder(private val binding: ContactCardBinding): ViewHolder(binding.root) {
        fun bind(callLog: CallLogs, context: Context?) {
            binding.name.text = callLog.name
            binding.number.text = callLog.number
            if (callLog.image != null) {
                binding.image.setImageBitmap(callLog.image)
            } else {
                binding.image.setImageResource(R.drawable.user)
            }
            binding.root.setOnClickListener {
                context?.startActivity(Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:" + callLog.number)
                })
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallLogViewHolder {
        val binding = ContactCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CallLogViewHolder(binding)
    }

    override fun getItemCount() = listOfCallLog.size

    override fun onBindViewHolder(holder: CallLogViewHolder, position: Int) {
        holder.bind(listOfCallLog[position], context)
    }
}