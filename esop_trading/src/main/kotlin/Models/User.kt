package models

import services.Util

class User(
    val username: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val emailId: String
) {

    init {
        DataStorage.userList[username] = this
        DataStorage.registeredEmails.add(emailId)
        DataStorage.registeredPhoneNumbers.add(phoneNumber)
    }

    val account: Account = Account()
    val orders: ArrayList<Order> = ArrayList()

    fun addOrder(
        orderQuantity: Long,
        orderType: String,
        orderPrice: Long,
        typeOfESOP: String = "NON-PERFORMANCE"
    ): ArrayList<String> {
        val errorList = ArrayList<String>()
        val amountRequiredToBuy = orderQuantity * orderPrice
        if (orderType == "BUY") {
            if(account.inventory.getFreeInventory() + account.inventory.getLockedInventory() + orderQuantity > Util.MAX_AMOUNT)
                errorList.add("Inventory threshold will be exceeded")
            errorList.add(account.wallet.moveFreeMoneyToLockedMoney(amountRequiredToBuy))
        } else if (orderType == "SELL") {
            if(account.wallet.getFreeMoney() + account.wallet.getLockedMoney() + amountRequiredToBuy > Util.MAX_AMOUNT)
                errorList.add("Wallet threshold will be exceeded")
            if (typeOfESOP == "NON-PERFORMANCE")
                errorList.add(account.inventory.moveFreeInventoryToLockedInventory(orderQuantity))
            else if (typeOfESOP == "PERFORMANCE")
                errorList.add(account.inventory.moveFreePerformanceInventoryToLockedPerformanceInventory(orderQuantity))
        }
        if (errorList.isEmpty()) {
            val orderObj = Order(this.username, Util.generateOrderId(), orderQuantity, orderPrice, orderType)
            orders.add(orderObj)
            if (orderType == "BUY") {
                Util.addOrderToBuyList(orderObj)
            } else {
                if (typeOfESOP == "NON-PERFORMANCE")
                    Util.addOrderToSellList(orderObj)
                else {
                    Util.addOrderToPerformanceSellList(orderObj)
                }
            }
            Util.matchOrders()
        }
        return errorList
    }

    fun getOrderDetails(): Map<String, *> {
        if (orders.size == 0) {
            return mapOf("order_history" to "[]")
        }

        val orderDetails = ArrayList<Map<String, Any>>()
        orders.forEach { order ->
            val currentOrderDetails = mutableMapOf<String, Any>()
            currentOrderDetails["order_id"] = order.orderId
            currentOrderDetails["quantity"] = order.orderQuantity
            currentOrderDetails["type"] = order.orderType
            currentOrderDetails["price"] = order.orderPrice


            if (order.orderStatus == "Unfilled") {
                val unfilledOrderExecutionLogs = ArrayList<Map<String, Any>>()
                val currentOrderExecutionLogs = mutableMapOf<String, Any>()
                currentOrderExecutionLogs["price"] = order.orderPrice
                currentOrderExecutionLogs["quantity"] = order.orderQuantity
                unfilledOrderExecutionLogs.add(currentOrderExecutionLogs)
                currentOrderDetails["unfilled"] = unfilledOrderExecutionLogs
            } else {
                if (order.orderStatus == "Partially Filled") {
                    val partiallyFilledOrderExecutionLogs = ArrayList<Map<String, Any>>()
                    order.orderExecutionLogs.forEach {
                        val currentOrderExecutionLogs = mutableMapOf<String, Any>()
                        currentOrderExecutionLogs["price"] = it.orderExecutionPrice
                        currentOrderExecutionLogs["quantity"] = it.orderExecutionQuantity
                        partiallyFilledOrderExecutionLogs.add(currentOrderExecutionLogs)
                    }
                    currentOrderDetails["partially_filled"] = partiallyFilledOrderExecutionLogs

                    val unfilledOrderExecutionLogs = ArrayList<Map<String, Any>>()
                    val currentOrderExecutionLogs = mutableMapOf<String, Any>()
                    currentOrderExecutionLogs["price"] = order.orderPrice
                    currentOrderExecutionLogs["quantity"] = order.remainingOrderQuantity
                    unfilledOrderExecutionLogs.add(currentOrderExecutionLogs)
                    currentOrderDetails["unfilled"] = unfilledOrderExecutionLogs
                } else if (order.orderStatus == "Filled") {
                    val filledOrderExecutionLogs = ArrayList<Map<String, Any>>()
                    order.orderExecutionLogs.forEach {
                        val currentOrderExecutionLogs = mutableMapOf<String, Any>()
                        currentOrderExecutionLogs["price"] = it.orderExecutionPrice
                        currentOrderExecutionLogs["quantity"] = it.orderExecutionQuantity
                        filledOrderExecutionLogs.add(currentOrderExecutionLogs)
                    }
                    currentOrderDetails["filled"] = filledOrderExecutionLogs
                }
            }
            orderDetails.add(currentOrderDetails)
        }

        return mapOf("order_history" to orderDetails)
    }
}