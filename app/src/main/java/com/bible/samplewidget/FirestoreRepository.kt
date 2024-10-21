package com.bible.samplewidget

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import kotlin.random.Random

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    fun checkDate(callback: (Boolean) -> Unit) {
        db.collection("today")
            .document("ra3ee4s2NKHaWuQ9MkOC")
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val timestamp = document.getTimestamp("today")
                    if (timestamp != null) {
                        val isMoreThanADay = compareTimestampWithCurrentDate(timestamp)
                        callback(isMoreThanADay)
                    } else {
                        callback(false)
                    }
                } else {
                    callback(false)
                }
            }
    }

    private fun compareTimestampWithCurrentDate(timestamp: Timestamp): Boolean {
        val savedDate = timestamp.toDate()
        val currentDate = Calendar.getInstance().time
        val diffInMillis = currentDate.time - savedDate.time
        val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)
        return diffInDays >= 1
    }

    fun updateTimestamp() {
        db.collection("today")
            .document("ra3ee4s2NKHaWuQ9MkOC")
            .update("today", Timestamp.now())
    }

    fun updateVerse(callback: (String?) -> Unit) {
        db.collection("bible verse")
            .document("sPRnqSRqM8mIjFEqzaAe")
            .get()
            .addOnSuccessListener { verses ->
                val data = verses.data
                if (!data.isNullOrEmpty()) {
                    val fieldNames = data.keys.toList()
                    val randomField = fieldNames[Random.nextInt(fieldNames.size)]
                    db.collection("today")
                        .document("ra3ee4s2NKHaWuQ9MkOC")
                        .update("verse", randomField)
                    callback(randomField)
                } else {
                    callback(null)
                }
            }
    }

    fun getVerse(callback: (String?, String?) -> Unit) {
        db.collection("today")
            .document("ra3ee4s2NKHaWuQ9MkOC")
            .get()
            .addOnSuccessListener { document ->
                val verse = document.getString("verse")
                db.collection("bible verse")
                    .document("sPRnqSRqM8mIjFEqzaAe")
                    .get()
                    .addOnSuccessListener { verses ->
                        verses.data?.forEach { (key, value) ->
                            if (key == verse) {
                                callback(key, value.toString())
                            }
                        }
                    }
            }
    }
}
