package com.example.mangoapps.helper

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Telephony
import com.example.mangoapps.models.CallLogs
import com.example.mangoapps.models.Contact
import com.example.mangoapps.models.SMS

fun retrieveContacts(contentResolver: ContentResolver): List<Contact> {
    val listOfContact = mutableListOf<Contact>()
    contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            CONTACT_PROJECTION,
            null,
            null,
            null
        )?.let { cursor ->
            if (cursor.count > 0) {
                val contactIdInd = cursor.getColumnIndex(CONTACT_PROJECTION[0])
                val contactNameInd = cursor.getColumnIndex(CONTACT_PROJECTION[1])
                val hasPhoneNumberInd = cursor.getColumnIndex(CONTACT_PROJECTION[2])

                while (cursor.moveToNext()) {
                    if (contactIdInd != -1 && contactNameInd != -1 && hasPhoneNumberInd != -1) {
                        val contactId = cursor.getString(contactIdInd)
                        val contactName = cursor.getString(contactNameInd)

                        if (contactId != null && contactName != null) {
                            listOfContact.add(Contact(contactId, contactName, null, null))
                        }
                    }
                }
            }
            cursor.close()
        }
    return listOfContact
}

fun retrieveContactNumber(contentResolver: ContentResolver): HashMap<String, ArrayList<String>> {
    val contactAndNumberMap = HashMap<String, ArrayList<String>>()
    contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        null,
        null,
        null
    )?.let { phoneCursor ->
        if (phoneCursor.count > 0) {
            val contactIdInd = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val contactNumberInd = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (phoneCursor.moveToNext()) {
                if (contactIdInd != -1 && contactNumberInd != -1) {
                    val contactId = phoneCursor.getString(contactIdInd)
                    val contactNumber = phoneCursor.getString(contactNumberInd)

                    if (contactId != null && contactNumber != null) {
                        if (contactAndNumberMap.containsKey(contactId)) {
                            contactAndNumberMap[contactId]?.add(contactNumber)
                        } else {
                            contactAndNumberMap[contactId] = arrayListOf(contactNumber)
                        }
                    }
                }
            }
        }
        phoneCursor.close()
    }
    return contactAndNumberMap
}

fun retrieveImages(contactResolver: ContentResolver, withName: Boolean = false): HashMap<String, ArrayList<Bitmap>> {
    val contactAndImageMap = HashMap<String, ArrayList<Bitmap>>()
    contactResolver.query(
        ContactsContract.Data.CONTENT_URI,
        arrayOf(
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.CommonDataKinds.Photo.PHOTO,
            ContactsContract.Data.DISPLAY_NAME
        ),
        ContactsContract.Data.MIMETYPE + " = ?",
        arrayOf(
            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
        ),
        null
    )?.let { imageCursor ->
        if (imageCursor.count > 0) {

            val contactIdInd = imageCursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
            val nameInd = imageCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)
            val imageInd = imageCursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO)

            while (imageCursor.moveToNext()) {
                if (contactIdInd != -1 && imageInd != -1) {
                    val photoBytes = imageCursor.getBlob(imageInd)
                    val contactId = imageCursor.getString(contactIdInd)
                    val name = imageCursor.getString(nameInd)
                    val key = if (withName) name else contactId

                    if (photoBytes != null && !key.isNullOrEmpty()) {
                        if (contactAndImageMap.containsKey(key)) {
                            contactAndImageMap[key]?.add(
                                BitmapFactory.decodeByteArray(
                                    photoBytes,
                                    0,
                                    photoBytes.size
                                )
                            )
                        } else {
                            contactAndImageMap[key] = arrayListOf(
                                BitmapFactory.decodeByteArray(
                                    photoBytes,
                                    0,
                                    photoBytes.size
                                )
                            )
                        }
                    }
                }
            }
        }
        imageCursor.close()
    }
    return contactAndImageMap
}

fun retrieveCallLogs(contentResolver: ContentResolver): List<CallLogs> {
    val callLogsList = mutableListOf<CallLogs>()
    contentResolver.query(
        CallLog.Calls.CONTENT_URI,
        CALL_LOGS_PROJECTION,
        null,
        null,
        null
    )?.let { logsCursor ->
        if (logsCursor.count > 0) {

            val numberInd = logsCursor.getColumnIndex(CALL_LOGS_PROJECTION[0])
            val nameInd = logsCursor.getColumnIndex(CALL_LOGS_PROJECTION[1])
            val dateInd = logsCursor.getColumnIndex(CALL_LOGS_PROJECTION[2])
            val typeInd = logsCursor.getColumnIndex(CALL_LOGS_PROJECTION[3])

            while (logsCursor.moveToNext()) {
                val number = logsCursor.getString(numberInd)
                val name = logsCursor.getString(nameInd)
                val type = logsCursor.getString(typeInd)
                val date = logsCursor.getLong(dateInd)

                if (number != null && name != null && type != null) {
                    callLogsList.add(CallLogs(name, number, getCallLogType(type.toInt()), null, date))
                }
            }
        }
        logsCursor.close()
    }
    callLogsList.sortWith { one, two ->
        (two.date - one.date).toInt()
    }
    return callLogsList
}

fun retrieveSMS(contentResolver: ContentResolver): List<SMS> {
    val smsList = mutableListOf<SMS>()
    val smsUri = Uri.parse("content://sms/inbox")
    contentResolver.query(
        smsUri,
        null,
        null,
        null,
        null
    )?.let {
        val bodyInd = it.getColumnIndex(Telephony.Sms.BODY)
        val addressInd = it.getColumnIndex(Telephony.Sms.ADDRESS)
        while (it.moveToNext()) {
            val body = it.getString(bodyInd)
            val address = it.getString(addressInd)
            if (body != null && address != null) {
                smsList.add(SMS(body, address))
            }
        }
        it.close()
    }
    return smsList
}

private fun getCallLogType(callLogID: Int): CallLogType {
    return when(callLogID) {
        1 -> CallLogType.INCOMING
        2 -> CallLogType.OUTGOING
        3 -> CallLogType.MISSED_CALL
        else -> CallLogType.UNKNOWN
    }
}

private val CALL_LOGS_PROJECTION = arrayOf(
    CallLog.Calls.NUMBER,
    CallLog.Calls.CACHED_NAME,
    CallLog.Calls.DATE,
    CallLog.Calls.TYPE
)

private val CONTACT_PROJECTION = arrayOf(
    ContactsContract.Contacts._ID,
    ContactsContract.Contacts.DISPLAY_NAME,
    ContactsContract.Contacts.HAS_PHONE_NUMBER
)

enum class SelectedScreen {
    CONTACT_SCREEN,
    CALL_LOG_SCREEN,
    SMS_SCREEN,
    NONE
}

enum class CallLogType {
    INCOMING,
    OUTGOING,
    MISSED_CALL,
    UNKNOWN
}

val CONTACTS_PERMISSION_REQUEST_CODE = 112
