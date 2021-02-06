package com.example.businessapp

class Address(var street: String, var postCode: String, var city: String) {

    override fun toString(): String {
        return "$street $postCode $city"
    }

}