package com.example.mangoapps

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mangoapps.databinding.ActivityMainBinding
import com.example.mangoapps.helper.CONTACTS_PERMISSION_REQUEST_CODE
import com.example.mangoapps.helper.SelectedScreen
import com.example.mangoapps.screens.CallLogsFragment
import com.example.mangoapps.screens.ContactSMSCallLogFragment
import com.example.mangoapps.screens.PermissionFragment
import com.example.mangoapps.viewmodels.ContactSMSCallLogViewModel
import com.example.mangoapps.viewmodels.MyViewModelFactory


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var contactViewModel: ContactSMSCallLogViewModel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = this.getSharedPreferences(getString(R.string.shared_preferences_screen), Context.MODE_PRIVATE)
        contactViewModel = ViewModelProvider(this, MyViewModelFactory(this.application))[ContactSMSCallLogViewModel::class.java]
        setUpDrawer()
        if (hasContactsPermission()) {
            startFlow()
        } else {
            requestContactsPermission()
        }
    }

    private fun startFlow() {
        contactViewModel.fetchContacts()
        contactViewModel.fetchCallLogs()
        contactViewModel.fetchSMS()

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.contact_menu -> {
                    navigateToTheFragment(ContactSMSCallLogFragment(), SelectedScreen.CONTACT_SCREEN)
                }
                R.id.call_logs_menu -> {
                    navigateToTheFragment(CallLogsFragment(), SelectedScreen.CALL_LOG_SCREEN)
                }
                R.id.sms_menu -> {
                    navigateToTheFragment(ContactSMSCallLogFragment(), SelectedScreen.SMS_SCREEN)
                }
            }
            binding.drawerLayout.close()
            true
        }
        openLastVisitedScreen(sharedPreferences.getInt(getString(R.string.last_visited_screen), 0))
    }

    private fun openLastVisitedScreen(lastVisitedScreenInd: Int) {
        when (lastVisitedScreenInd) {
            SelectedScreen.CONTACT_SCREEN.ordinal -> {
                navigateToTheFragment(ContactSMSCallLogFragment(), SelectedScreen.CONTACT_SCREEN)
            }
            SelectedScreen.CALL_LOG_SCREEN.ordinal -> {
                navigateToTheFragment(CallLogsFragment(), SelectedScreen.CALL_LOG_SCREEN)
            }
            SelectedScreen.SMS_SCREEN.ordinal -> {
                navigateToTheFragment(ContactSMSCallLogFragment(), SelectedScreen.SMS_SCREEN)
            }
        }
    }

    private fun setUpDrawer() {
        val drawerToggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }

    private fun navigateToTheFragment(fragment: Fragment, selectedScreen: SelectedScreen) {
        contactViewModel.updateSelectedScreen(selectedScreen)
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.host_layout, fragment)
            commit()
        }
    }

    private fun hasContactsPermission(): Boolean {
        val readPermission =
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
        val writePermission =
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CONTACTS)
        val callLogReadPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
        val callLogWritePermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG)
        val smsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)

        return readPermission == PackageManager.PERMISSION_GRANTED &&
                writePermission == PackageManager.PERMISSION_GRANTED &&
                callLogReadPermission == PackageManager.PERMISSION_GRANTED &&
                callLogWritePermission == PackageManager.PERMISSION_GRANTED &&
                smsPermission == PackageManager.PERMISSION_GRANTED

    }

    private fun requestContactsPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_CALL_LOG,
                Manifest.permission.READ_SMS
            ),
            CONTACTS_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CONTACTS_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                var flag = true;
                grantResults.forEach {
                    if (it != PackageManager.PERMISSION_GRANTED) {
                        flag = false
                        return@forEach
                    }
                }
                if (flag) {
                    startFlow()
                } else {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.host_layout, PermissionFragment())
                        commit()
                    }
                }

            } else {
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.host_layout, PermissionFragment())
                    commit()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        with(sharedPreferences.edit()) {
            putInt(getString(R.string.last_visited_screen), contactViewModel.selectedScreen.ordinal)
            apply()
        }
    }

}
