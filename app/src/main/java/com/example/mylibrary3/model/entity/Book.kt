package com.example.mylibrary3.model.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*


@Parcelize
data class Book(
    var bookId: String?= null,
    var bookName: String? = null,
    var bookAuthor: String? = null,
    var launchYear: Date? = Date(),
    var price: Double? = null,
    var rate: Float? = null
) : Parcelable{

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "bookId" to bookId,
            "bookName" to bookName,
            "bookAuthor" to bookAuthor,
            "launchYear" to launchYear,
            "price" to price,
            "rate" to rate
        )
    }
}
