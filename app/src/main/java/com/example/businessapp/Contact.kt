package com.example.businessapp

class Contact(
    var type: String,
    var value: String,
    var isWebsite: Boolean,
    var isPhoneNumber: Boolean
) {

    override fun toString(): String {
        return "$type : $value"
    }
}