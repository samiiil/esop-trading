package com.example.Controller

import com.example.Order
import com.example.controller.UserController
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.json.tree.JsonObject
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

@Controller("/")
class CreateOrderController {

    companion object {
        var orderId : Int = 1
        var orders : HashMap<String, JsonObject> = HashMap<String, JsonObject> ()
        val compareByDesc: Comparator<Pair<Int, Order>> = compareByDescending { it.first }
        val buyqueue = PriorityQueue<Pair<Int,Order>>(compareByDesc)
        val sellqueue = PriorityQueue<Pair<Int,Order>>(compareByDesc)

    }

    @Post("/user/{username}/order")
    fun createOrder(username:String,@Body body:JsonObject){

        var quantity = body.get("quantity").stringValue.trim()
        var type = body.get("type").stringValue.trim()
        var price = body.get("price").stringValue.trim().toInt()

//        val map = nums.groupBy { it }
//        val compareByDesc: Comparator<Pair<Int, Int>> = compareByDescending { it.first }
//        val pQueue = PriorityQueue<Pair<Int,Int>>(compareByDesc)
//
//        map.forEach { (key, value) ->
//            pQueue.add(value.size to key)
//        }
//
//        val answer = IntArray(k){0}
//        var i = 0
//        while(i<k && pQueue.isNotEmpty()) {
//            answer[i] = pQueue.remove().second
//            i++
//        }


        if(!UserController.users.containsKey(username)){

        }
        if(type=="BUY"){
            var i = 0
            while(i< sellqueue.size && sellqueue.isNotEmpty()) {
                var first = sellqueue.first()
                if(first.first <= price){

                }
                i++
            }
        }
        else{

        }


    }

}