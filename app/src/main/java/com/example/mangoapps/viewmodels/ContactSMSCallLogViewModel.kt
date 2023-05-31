package com.example.mangoapps.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangoapps.helper.SelectedScreen
import com.example.mangoapps.helper.retrieveCallLogs
import com.example.mangoapps.helper.retrieveContactNumber
import com.example.mangoapps.helper.retrieveContacts
import com.example.mangoapps.helper.retrieveImages
import com.example.mangoapps.helper.retrieveSMS
import com.example.mangoapps.models.CallLogs
import com.example.mangoapps.models.Contact
import com.example.mangoapps.models.SMS
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ContactSMSCallLogViewModel(private val application: Application): ViewModel() {

    // data for contact screen
    private val _contactLiveData = MutableLiveData<List<Contact>>()
    val contactLiveData: LiveData<List<Contact>>
        get() = _contactLiveData

    // data for call log screen
    private val _callLogLiveData = MutableLiveData<List<CallLogs>>()
    val callLogLiveData: LiveData<List<CallLogs>>
        get() = _callLogLiveData

    // data for sms screen
    private val _smsLiveData = MutableLiveData<List<SMS>>()
    val smsLiveData: LiveData<List<SMS>>
        get() = _smsLiveData

    // current screen
    private var _selectedScreen: SelectedScreen = SelectedScreen.CONTACT_SCREEN
    val selectedScreen: SelectedScreen
        get() = _selectedScreen

    // to refresh the data
    private val _isContactFetchCompleted = MutableLiveData<Boolean>()
    val isContactFetchCompleted: LiveData<Boolean>
        get() = _isContactFetchCompleted

    private val _isCallLogFetchCompleted = MutableLiveData<Boolean>()
    val isCallLogsFetchCompleted: LiveData<Boolean>
        get() = _isCallLogFetchCompleted

    private val _smsFetchCompleted = MutableLiveData<Boolean>()
    val smsFetchCompleted: LiveData<Boolean>
        get() = _smsFetchCompleted

    /**
     * This method will fetch all the contacts from your smartphone.
     * addDelay: this will update your UI smoothly.
     */
    fun fetchContacts(addDelay: Boolean = false) {
        viewModelScope.launch {
            _isContactFetchCompleted.value = false
            if (addDelay) {
                delay(5000)
            }
            val one = async { retrieveContacts(application.contentResolver) }
            val two = async { retrieveContactNumber(application.contentResolver) }
            val three = async { retrieveImages(application.contentResolver) }

            // names and images are fetched separately because it will dependent on the contactID
            // so to avoid nesting i have run all three methods parallelly.
            val contactList = one.await()
            val numberAndIDMap = two.await()
            val imageAndIDMap = three.await()


            _isContactFetchCompleted.value = true
            _contactLiveData.postValue(contactList
                .filter { !numberAndIDMap[it.id].isNullOrEmpty() && it.name.isNotEmpty() }
                .sortedBy { it.name }
                .onEach { contact ->
                    contact.number = numberAndIDMap[contact.id]
                    contact.image = imageAndIDMap[contact.id]
                })
        }
    }

    /**
     * This method will fetch call logs from your smartphone.
     * addDelay: this will update your UI smoothly.
     */
    fun fetchCallLogs(addDelay: Boolean = false) {
        viewModelScope.launch {
            _isCallLogFetchCompleted.value = false
            if (addDelay) {
                delay(5000)
            }
            val one = async { retrieveCallLogs(application.contentResolver) }
            val two = async { retrieveImages(application.contentResolver, true) }
            val list = one.await()
            val mapOfNameAndImage = two.await()

            // adding images associated with the name.
            list.forEach {
                if (mapOfNameAndImage.containsKey(it.name)) {
                    it.image = mapOfNameAndImage[it.name]?.get(0)
                }
            }
            _callLogLiveData.postValue(list)
            _isCallLogFetchCompleted.value = true
        }
    }

    /**
     * This method will fetch the sms from your smartphone.
     * addDelay: this will update your UI smoothly.
     */
    fun fetchSMS(addDelay: Boolean = false) {
        viewModelScope.launch {
            _smsFetchCompleted.value = false
            if (addDelay) {
                delay(5000)
            }
            val one = async {  retrieveSMS(application.contentResolver) }
            val listOfSMS = one.await()
            _smsFetchCompleted.value = true
            _smsLiveData.postValue(listOfSMS)
        }
    }

    fun updateSelectedScreen(selectedScreen: SelectedScreen) {
        _selectedScreen = selectedScreen
    }

    fun refreshCallLogs(addDelay: Boolean = false) {
        _isCallLogFetchCompleted.value?.let {
            if (it) {
                fetchCallLogs(addDelay)
            }
        }
    }
    fun refreshContacts(addDelay: Boolean = false) {
        _isContactFetchCompleted.value?.let {
            if (it) {
                fetchContacts(addDelay)
            }
        }
    }

    fun refreshSMS(addDelay: Boolean = false) {
        _smsFetchCompleted.value?.let {
            if (it) {
                fetchSMS(addDelay)
            }
        }
    }

}
