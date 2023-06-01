package com.example.mangoapps.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mangoapps.databinding.FragmentCallLogsBinding
import com.example.mangoapps.viewmodels.ContactSMSCallLogViewModel
import com.example.mangoapps.viewmodels.MyViewModelFactory
import com.google.android.material.tabs.TabLayoutMediator

class CallLogsViewPagerFragment : Fragment() {

    private lateinit var binding: FragmentCallLogsBinding
    private lateinit var viewModel: ContactSMSCallLogViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCallLogsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            viewModel = ViewModelProvider(it, MyViewModelFactory(it.application))[ContactSMSCallLogViewModel::class.java]
        }
        binding.callLogViewPager.adapter = CallLogViewPagerAdapter(this, viewModel)

        /*
            it will set the name of the tabs.
         */
        TabLayoutMediator(binding.callLogTabLayout, binding.callLogViewPager) { tab, position ->
            tab.text = when (position+1) {
                1 -> "INCOMING"
                2 -> "OUTGOING"
                else -> "MISSED"
            }
        }.attach()
    }
}

class CallLogViewPagerAdapter(fragment: Fragment, private val viewModel: ContactSMSCallLogViewModel): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {

        /*
            while creating CallLog Screen we must pass the tab number to sort the list of CallLogs.
         */

        val fragment =  ContactSMSCallLogFragment()
        when (position+1) {
            1 -> {
                fragment.arguments = Bundle().apply {
                    putInt("selected_tab", 1)
                }
            }
            2 -> {
                fragment.arguments = Bundle().apply {
                    putInt("selected_tab", 2)
                }
            }
            3 -> {
                fragment.arguments = Bundle().apply {
                    putInt("selected_tab", 3)
                }
            }
        }
        return fragment
    }
}
