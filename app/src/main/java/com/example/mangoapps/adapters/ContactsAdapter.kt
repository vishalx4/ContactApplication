package com.example.mangoapps.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.mangoapps.R
import com.example.mangoapps.databinding.ContactCardBinding
import com.example.mangoapps.helper.contactDialog
import com.example.mangoapps.models.Contact

class ContactsAdapter(private val listOfContact: List<Contact>,private val context: Context?): RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    class ContactViewHolder(private val binding: ContactCardBinding) : ViewHolder(binding.root) {
        fun bind(contact: Contact, context: Context?) {
            binding.name.text = contact.name
            contact.number?.let {
                binding.number.text = it[0]
            }
            if (contact.image.isNullOrEmpty()) {
                binding.image.setImageResource(R.drawable.user)
            } else {
                binding.image.setImageBitmap(contact.image!![0])
            }
            binding.root.setOnClickListener {
                setUpDialog(context, contact)
            }
        }

        private fun setUpDialog(context: Context?, contact: Contact) {
            context?.let {
                contactDialog(it, contact).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ContactCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(listOfContact[position], context)
    }

    override fun getItemCount() = listOfContact.size
}