package models

import services.Util
import kotlin.math.roundToLong

class User(
    val username: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val emailId: String
) {
    private val account: Account = Account()
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
            if(account.inventory.getFreeInventory() + account.inventory.getLockedInventory() + orderQuantity > DataStorage.MAX_QUANTITY)
                errorList.add("Inventory threshold will be exceeded")
            val res = account.wallet.moveFreeMoneyToLockedMoney(amountRequiredToBuy)
            if(res != "Success") errorList.add(res)
        } else if (orderType == "SELL") {
            if(account.wallet.getFreeMoney() + account.wallet.getLockedMoney() + amountRequiredToBuy > DataStorage.MAX_AMOUNT)
                errorList.add("Wallet threshold will be exceeded")
            val res = if (typeOfESOP == "NON-PERFORMANCE")
                account.inventory.moveFreeInventoryToLockedInventory(orderQuantity)
            else
                account.inventory.moveFreePerformanceInventoryToLockedPerformanceInventory(orderQuantity)
            if(res != "Success") errorList.add(res)
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

    fun getOrderDetails(): Map<String, ArrayList<Map<String,Any>>> {
        if (orders.size == 0) {
            return mapOf("order_history" to ArrayList())
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

    fun addMoneyToWallet(amountToBeAdded: Long) {
        this.account.wallet.freeMoney = this.account.wallet.freeMoney + amountToBeAdded
    }

    fun getFreeMoney(): Long {
        return this.account.wallet.freeMoney
    }

    fun getLockedMoney(): Long {
        return this.account.wallet.lockedMoney
    }

    fun updateLockedMoney(amountToBeUpdated: Long) {
        this.account.wallet.lockedMoney = this.account.wallet.lockedMoney - amountToBeUpdated
    }

    fun moveFreeMoneyToLockedMoney(amountToBeLocked: Long) {
        this.account.wallet.freeMoney = this.account.wallet.freeMoney - amountToBeLocked
        this.account.wallet.lockedMoney = this.account.wallet.lockedMoney + amountToBeLocked
    }


    fun addEsopToInventory(esopsToBeAdded: Long, type: String = "NON-PERFORMANCE") {
        if (type == "PERFORMANCE") {
            this.account.inventory.freePerformanceInventory = this.account.inventory.freePerformanceInventory + esopsToBeAdded
        } else {
            this.account.inventory.freeInventory = this.account.inventory.freeInventory + esopsToBeAdded
        }

    }

    fun getFreeInventory(): Long {
        return this.account.inventory.freeInventory
    }

    fun getLockedInventory(): Long {
        return this.account.inventory.lockedInventory
    }

    fun getFreePerformanceInventory(): Long {
        return this.account.inventory.freePerformanceInventory
    }

    fun getLockedPerformanceInventory(): Long {
        return this.account.inventory.lockedPerformanceInventory
    }


    fun updateLockedInventory(inventoryToBeUpdated: Long, isPerformanceESOP: Boolean) {
        if (isPerformanceESOP)
            this.account.inventory.lockedPerformanceInventory = this.account.inventory.lockedPerformanceInventory - inventoryToBeUpdated
        else
            this.account.inventory.lockedInventory = this.account.inventory.lockedInventory - inventoryToBeUpdated
    }

    fun moveFreeInventoryToLockedInventory(esopsToBeLocked: Long): String {
        if (this.account.inventory.freeInventory < esopsToBeLocked) {
            return "Insufficient ESOPs in Inventory"
        }
        this.account.inventory.freeInventory = this.account.inventory.freeInventory - esopsToBeLocked
        this.account.inventory.lockedInventory = this.account.inventory.lockedInventory + esopsToBeLocked
        return "Success"
    }

    fun moveFreePerformanceInventoryToLockedPerformanceInventory(esopsToBeLocked: Long): String {
        if (this.account.inventory.freePerformanceInventory < esopsToBeLocked) {
            return "Insufficient ESOPs in Inventory"
        }
        this.account.inventory.freePerformanceInventory = this.account.inventory.freePerformanceInventory - esopsToBeLocked
        this.account.inventory.lockedPerformanceInventory = this.account.inventory.lockedPerformanceInventory + esopsToBeLocked
        return "Success"
    }
}