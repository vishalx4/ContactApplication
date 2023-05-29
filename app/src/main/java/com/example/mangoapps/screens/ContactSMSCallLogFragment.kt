package com.example.mangoapps.screens

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mangoapps.adapters.CallLogAdapter
import com.example.mangoapps.adapters.ContactsAdapter
import com.example.mangoapps.adapters.SMSAdapter
import com.example.mangoapps.databinding.FragmentContactsBinding
import com.example.mangoapps.helper.CallLogType
import com.example.mangoapps.helper.SelectedScreen
import com.example.mangoapps.viewmodels.ContactSMSCallLogViewModel
import com.example.mangoapps.viewmodels.MyViewModelFactory

class ContactSMSCallLogFragment : Fragment() {

    private lateinit var binding: FragmentContactsBinding
    private lateinit var contactViewModel: ContactSMSCallLogViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            contactViewModel = ViewModelProvider(it, MyViewModelFactory(it.application))[ContactSMSCallLogViewModel::class.java]
        }
        binding.contactsRecyclerView.layoutManager = LinearLayoutManager(activity)
        setUpObservables()
        setSwipeDownRefreshAction()
    }

    private fun setSwipeDownRefreshAction() {
        binding.swipeToRefreshContacts.setOnRefreshListener {
            when (contactViewModel.selectedScreen) {
                SelectedScreen.CONTACT_SCREEN -> {
                    contactViewModel.refreshContacts()
                }
                SelectedScreen.CALL_LOG_SCREEN -> {
                    contactViewModel.refreshCallLogs()
                }
                SelectedScreen.SMS_SCREEN -> {
                    contactViewModel.refreshSMS()
                }
                else -> {
                    Log.d(ContactSMSCallLogViewModel::class.java.name, "setSwipeDownRefreshAction: selected screen error")
                }
            }
        }
    }

    private fun setUpObservables() {
        when (contactViewModel.selectedScreen) {
            SelectedScreen.CONTACT_SCREEN -> {
                setUpContactScreenObservables()
            }
            SelectedScreen.CALL_LOG_SCREEN -> {
                setUpCallLogScreenObservables()
            }
            SelectedScreen.SMS_SCREEN -> {
                setUpSMSScreenObservables()
            }
            else -> {
                Log.d(ContactSMSCallLogViewModel::class.java.name, "setUpObservables: selected screen error")
            }
        }
    }

    private fun setUpContactScreenObservables() {
        contactViewModel.refreshContacts()
        contactViewModel.contactLiveData.observe(viewLifecycleOwner) {
            binding.contactsRecyclerView.adapter = ContactsAdapter(it, context)
        }
        contactViewModel.isContactFetchJob.observe(viewLifecycleOwner) {
            if (it) {
                binding.swipeToRefreshContacts.isRefreshing = false
            }
        }
    }

    private fun setUpCallLogScreenObservables() {
        contactViewModel.refreshCallLogs()
        arguments?.let {
            val selectedTab = when (it.getInt("selected_tab")) {
                1 -> CallLogType.INCOMING
                2 -> CallLogType.OUTGOING
                3 -> CallLogType.MISSED_CALL
                else -> CallLogType.UNKNOWN
            }
            contactViewModel.callLogLiveData.observe(viewLifecycleOwner) { listOfCallLog ->
                binding.contactsRecyclerView.adapter = CallLogAdapter(listOfCallLog.filter { it.callType == selectedTab }, context)
            }
        }

        contactViewModel.isCallLogsFetchJob.observe(viewLifecycleOwner) {
            if (it) {
                binding.swipeToRefreshContacts.isRefreshing = false
            }
        }
    }

    private fun setUpSMSScreenObservables() {
        contactViewModel.refreshSMS()
        contactViewModel.smsLiveData.observe(viewLifecycleOwner) {
            binding.contactsRecyclerView.adapter = SMSAdapter(it, context)
        }
        contactViewModel.smsFetchCompleted.observe(viewLifecycleOwner) {
            if (it) {
                binding.swipeToRefreshContacts.isRefreshing = false
            }
        }
    }

}
