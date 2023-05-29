package com.example.mangoapps.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.mangoapps.databinding.SmsCardBinding
import com.example.mangoapps.models.SMS

class SMSAdapter(private val listOfSMS: List<SMS>, private val context: Context?): RecyclerView.Adapter<SMSAdapter.SMSViewHolder>() {

    class SMSViewHolder(private val binding: SmsCardBinding): ViewHolder(binding.root) {
        fun bind(sms: SMS, context: Context?) {
            binding.smsBody.text = sms.message
            binding.smsSender.text = sms.senderName
            binding.root.setOnClickListener {
                context?.startActivity(Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:${sms.senderName}")
                })
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SMSViewHolder {
        val binding =
            SmsCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SMSViewHolder(binding)
    }

    override fun getItemCount() = listOfSMS.size

    override fun onBindViewHolder(holder: SMSViewHolder, position: Int) {
        holder.bind(listOfSMS[position], context)
    }
}