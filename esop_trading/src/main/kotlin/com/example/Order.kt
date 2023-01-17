package com.example

data class Order(
    var orderid:Int,
    var status:Char,
    var quantity:Int,
    var user: User
)
