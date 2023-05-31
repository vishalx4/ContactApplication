package com.example.mangoapps.helper

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.view.Window
import com.example.mangoapps.R
import com.example.mangoapps.databinding.ContactDialogBinding
import com.example.mangoapps.models.Contact

fun contactDialog(context: Context, contact: Contact): Dialog {
    return Dialog(context).apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        val binding = ContactDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(900, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(R.color.transparent)
        binding.contactName.text = contact.name

        binding.callAction.setOnClickListener {
            context.startActivity(Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:" + contact.number?.get(0))
            })
        }

        binding.messageAction.setOnClickListener {
            context.startActivity(Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${contact.number?.get(0)}")
            })
        }
    }
}