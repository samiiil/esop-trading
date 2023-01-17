package com.example.Controller

import com.example.Inventory
import com.example.Order
import com.example.User
import com.example.Wallet
import com.example.controller.UserController
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.json.tree.JsonObject
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Controller("/")
class CreateOrderController {

    companion object {
        var orderId : Number = 4
        var orders : HashMap<String, ArrayList<Order>> = HashMap<String, ArrayList<Order>> ()
        val compareByDesc: Comparator<Pair<Int, Order>> = compareByDescending { it.first }
        val buyqueue = PriorityQueue<Pair<Int,Order>>(compareByDesc)
        val compareByAsc: Comparator<Pair<Int, Order>> = compareByDescending { -1 * it.first }
        val sellqueue = PriorityQueue<Pair<Int,Order>>(compareByAsc)

    }

    fun addBuyOrder(order: Order){
        var buyer = order.user


//        println("wallet free: " + buyer.wallet.free)
//        println("wallet locked: " + buyer.wallet.locked)

        if (order.status == 'F') {
            buyer.wallet.locked -= order.totalPrice
            buyer.wallet.free += order.totalPrice

            return
        }


        if(!sellqueue.isNotEmpty() || sellqueue.first().first > order.price) {
            buyqueue.add(order.price to order)
            return
        }

        var sellOrder = sellqueue.poll().second
        var seller = sellOrder.user

        // partial/full buy order case
        if(order.quantity >= sellOrder.quantity){

            // add to seller order history
            sellOrder.status = 'F'

            if (orders.get(sellOrder.user.username) == null) {
                orders.put(sellOrder.user.username, arrayListOf())
            }

            orders[sellOrder.user.username]!!.add(sellOrder)

            // add to buyer order history
            order.status = 'P'

            if (order.quantity <= sellOrder.quantity) {
                order.status = 'F'

                if (order.quantity == sellOrder.quantity) {
                    sellOrder.status = 'F'
                }
            }


            var newOrder = order.copy()
            order.quantity = sellOrder.quantity
            if (orders[buyer.username] == null) {
                orders[buyer.username] = arrayListOf()
            }
            orders[buyer.username]?.add(order)


            newOrder.quantity -= sellOrder.quantity

            // inventory
            seller.inventory.locked -= sellOrder.quantity
            buyer.inventory.free += sellOrder.quantity

            //wallet
            var totalBoughtPrice = sellOrder.quantity * sellOrder.price
            seller.wallet.free += totalBoughtPrice
            buyer.wallet.locked -= totalBoughtPrice

            newOrder.totalPrice -= totalBoughtPrice


            addBuyOrder(newOrder)
        } else {
            // partial sell order

            // add to seller order history
            order.status = 'F'
            if (orders.get(order.user.username) == null) {
                orders.put(order.user.username, arrayListOf<Order>())
            }
            orders[order.user.username]!!.add(order)

            // add to buyer order history
            sellOrder.status = 'P'

            var newOrder = sellOrder.copy()

            sellOrder.quantity = order.quantity

            if (orders[seller.username] == null) {
                orders[seller.username] = arrayListOf()
            }
            orders[seller.username]?.add(sellOrder)


            newOrder.quantity -= order.quantity


            // inventory
            seller.inventory.locked -= order.quantity
            buyer.inventory.free += order.quantity

            //wallet
            var totalBoughtPrice = sellOrder.quantity * sellOrder.price
            seller.wallet.free += totalBoughtPrice
            buyer.wallet.locked -= totalBoughtPrice

            order.totalPrice -= totalBoughtPrice

            sellqueue.add(sellOrder.price to newOrder)
            addBuyOrder(order)
        }
    }


    fun addSellOrder(order: Order){
        var seller = order.user


//        println("wallet free: " + buyer.wallet.free)
//        println("wallet locked: " + buyer.wallet.locked)

        if (order.status == 'F') {
            return
        }


        if(!buyqueue.isNotEmpty() || buyqueue.first().first < order.price) {
            sellqueue.add(order.price to order)
            return
        }

        var buyOrder = buyqueue.poll().second
        var buyer = buyOrder.user

        // partial/full sell order case
        if(order.quantity >= buyOrder.quantity){

            // add to seller order history
            buyOrder.status = 'F'

            if (orders.get(buyOrder.user.username) == null) {
                orders.put(buyOrder.user.username, arrayListOf())
            }

            orders[buyOrder.user.username]!!.add(buyOrder)

            // add to buyer order history
            order.status = 'P'
            if (order.quantity <= buyOrder.quantity) {
                order.status = 'F'

                if (order.quantity == buyOrder.quantity) {
                    buyOrder.status = 'F'
                }
            }
            var newOrder = order.copy()
            order.quantity = buyOrder.quantity
            if (orders[seller.username] == null) {
                orders[seller.username] = arrayListOf()
            }
            orders[seller.username]?.add(order)


            newOrder.quantity -= buyOrder.quantity

            // inventory
            seller.inventory.locked -= buyOrder.quantity
            buyer.inventory.free += buyOrder.quantity

            //wallet
            var totalBoughtPrice = buyOrder.quantity * order.price
            seller.wallet.free += totalBoughtPrice
            buyer.wallet.locked -= totalBoughtPrice

            newOrder.totalPrice -= totalBoughtPrice

            addSellOrder(newOrder)
        } else {
            // partial buy order

            // add to seller order history
            order.status = 'F'
            if (orders.get(order.user.username) == null) {
                orders.put(order.user.username, arrayListOf<Order>())
            }
            orders[order.user.username]!!.add(order)

            // add to buyer order history
            buyOrder.status = 'P'

            var newOrder = buyOrder.copy()

            buyOrder.quantity = order.quantity

            if (orders[buyer.username] == null) {
                orders[buyer.username] = arrayListOf()
            }
            orders[buyer.username]?.add(buyOrder)


            newOrder.quantity -= order.quantity


            // inventory
            seller.inventory.locked -= order.quantity
            buyer.inventory.free += order.quantity

            //wallet
            var totalBoughtPrice = order.quantity * order.price
            seller.wallet.free += totalBoughtPrice
            buyer.wallet.locked -= totalBoughtPrice

            order.totalPrice -= totalBoughtPrice

            addBuyOrder(newOrder)
        }
    }



    @Post("/user/{username}/order")
    fun createOrder(username:String,@Body body:JsonObject) : HashMap<String, Any> {

        var quantity = body.get("quantity").intValue
        var type = body.get("type").stringValue.trim()
        var price = body.get("price").intValue

        var errors = ArrayList<String>()

//        var seller = User("ksjhdfkjs","sfd",37628,"sdjfhks", "sdhfs",
//            Inventory(0, 23), Wallet(0, 0))
//        var o1 = Order(1, 'U',  20, 5, 100, seller)
//        var o2 = Order(1, 'U', 3, 6, 18, seller)
//        sellqueue.add(5 to o1)
//        sellqueue.add(6 to o2)
//        UserController.users.put(seller.username, seller)
//        orders[seller.username] = arrayListOf()

        if(!UserController.users.containsKey(username)){

        }

        var user = UserController.users[username]!!


        var order = Order(orderId,'U', quantity, price, price*quantity, user)
        if(type=="BUY") {
            if (user.wallet.free < (order.totalPrice)) {
                errors.add("insufficient wallet amount")
            } else {

                order.user.wallet.free -= (order.totalPrice)
                order.user.wallet.locked += (order.totalPrice)

                addBuyOrder(order)
            }

        }
        else {

            if(user.inventory.free < quantity){
                errors.add("insufficient ESOP quantity")
            } else {
                order.user.inventory.free -= (order.quantity)
                order.user.inventory.locked += (order.quantity)

                addSellOrder(order)
            }
        }

        var response = HashMap<String, Any>()
        response["errors"] = errors
        response["orders"] = UserController.users.toMap()

        return response
    }

}