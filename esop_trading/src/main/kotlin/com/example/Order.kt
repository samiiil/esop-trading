package com.example

data class Order(
    var orderid:Number,
    var status:Char,
    var quantity:Int,
    var price: Int,
    var totalPrice: Int,
    var user: User,
)
