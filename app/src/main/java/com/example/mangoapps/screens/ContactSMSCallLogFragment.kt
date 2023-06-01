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

/**
 * this fragment will act as a container for the Contact, CallLog and SMS Screen
 * used same fragment for reusability.
 */

class ContactSMSCallLogFragment : Fragment() {

    private lateinit var binding: FragmentContactsBinding
    private lateinit var viewModel: ContactSMSCallLogViewModel

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
            viewModel = ViewModelProvider(it, MyViewModelFactory(it.application))[ContactSMSCallLogViewModel::class.java]
        }
        binding.contactsRecyclerView.layoutManager = LinearLayoutManager(activity)
        setUpObservables()
        setSwipeDownRefreshAction()
    }

    /**
     * this method will call refresh the screen when user swipes down the list.
     */
    private fun setSwipeDownRefreshAction() {
        binding.swipeToRefreshContacts.setOnRefreshListener {
            when (viewModel.selectedScreen) {
                SelectedScreen.CONTACT_SCREEN -> {
                    viewModel.refreshContacts()
                }
                SelectedScreen.CALL_LOG_SCREEN -> {
                    viewModel.refreshCallLogs()
                }
                SelectedScreen.SMS_SCREEN -> {
                    viewModel.refreshSMS()
                }
                else -> {
                    Log.d(ContactSMSCallLogViewModel::class.java.name, "setSwipeDownRefreshAction: selected screen error")
                }
            }
        }
    }

    /**
     * this method is used to set Observable on live data as per the Screen
     */
    private fun setUpObservables() {
        when (viewModel.selectedScreen) {
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

        viewModel.refreshContacts(true)

        viewModel.contactLiveData.observe(viewLifecycleOwner) {
            binding.contactsRecyclerView.adapter = ContactsAdapter(it, context)
        }

        // stop the refreshing progress bar if the refresh action is completed.
        viewModel.isContactFetchCompleted.observe(viewLifecycleOwner) {
            if (it) {
                binding.swipeToRefreshContacts.isRefreshing = false
            }
        }
    }

    private fun setUpCallLogScreenObservables() {

        viewModel.refreshCallLogs(true)

        arguments?.let {
            val selectedTab = when (it.getInt("selected_tab")) {
                1 -> CallLogType.INCOMING
                2 -> CallLogType.OUTGOING
                3 -> CallLogType.MISSED_CALL
                else -> CallLogType.UNKNOWN
            }

            // filter the list as per the selectedTab.
            viewModel.callLogLiveData.observe(viewLifecycleOwner) { listOfCallLog ->
                binding.contactsRecyclerView.adapter = CallLogAdapter(listOfCallLog.filter { it.callType == selectedTab }, context)
            }
        }

        // stop the refreshing progress bar if the refresh action is completed.
        viewModel.isCallLogsFetchCompleted.observe(viewLifecycleOwner) {
            if (it) {
                binding.swipeToRefreshContacts.isRefreshing = false
            }
        }
    }

    private fun setUpSMSScreenObservables() {

        viewModel.refreshSMS(true)

        viewModel.smsLiveData.observe(viewLifecycleOwner) {
            binding.contactsRecyclerView.adapter = SMSAdapter(it, context)
        }

        // stop the refreshing progress bar if the refresh action is completed.
        viewModel.smsFetchCompleted.observe(viewLifecycleOwner) {
            if (it) {
                binding.swipeToRefreshContacts.isRefreshing = false
            }
        }
    }

}
