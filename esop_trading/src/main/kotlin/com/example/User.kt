package com.example

data class User(
    var firstName:String,
    var lastName : String,
    var phoneNumber : String,
    var email : String,
    var username : String,
    var inventory: Inventory,
    var wallet: Wallet
)
