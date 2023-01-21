package Models
import Services.Util
class User(val username: String,
           val firstName: String,
           val lastName: String,
           val phoneNumber: String,
           val emailId: String) {
    val account: Account = Account()
    val orders: ArrayList<Order> = ArrayList()

    fun addOrder(orderQuantity: Long, orderType: String, orderAmount: Long,typeOfESOP: String="NON-PERFORMANCE") : String{
        var response = ""
        if ( orderType == "BUY" ){
            val amountRequiredToBuy = orderQuantity*orderAmount
            response = account.wallet.moveFreeMoneyToLockedMoney(amountRequiredToBuy)
        }else if ( orderType == "SELL" ){
            if(typeOfESOP == "NON-PERFORMANCE")
                response = account.inventory.moveFreeInventoryToLockedInventory(orderQuantity)
            else
                response = account.inventory.moveFreePerformanceInventoryToLockedPerformanceInventory(orderQuantity)
        }
        if (response == "Success"){
            val orderObj = Order(this.username,Util.generateOrderId(),orderQuantity,orderAmount,orderType)
            orders.add(orderObj)
            if( orderType == "BUY" ){
                Util.addOrderToBuyList(orderObj)
            }else{ 
                if(typeOfESOP == "NON-PERFORMANCE")
                    Util.addOrderToSellList(orderObj)
                else{
                    Util.addOrderToPerformanceSellList(orderObj)
                }
            }
            Util.matchOrders()
            return "Order Placed Successfully."
        }else{
            return response
        }
    }

    fun getOrderDetails():Map<String,*>{
        if(orders.size == 0 ){
            return mapOf("order_history" to "[]")
        }

        val orderDetails = ArrayList<Map<String,Any>>()
        orders.forEach { order ->
            val currentOrderDetails = mutableMapOf<String,Any>()
            currentOrderDetails["order_id"] = order.orderId
            currentOrderDetails["quantity"] = order.orderQuantity
            currentOrderDetails["type"] = order.orderType
            currentOrderDetails["price"] = order.orderPrice


            if(order.orderStatus == "Unfilled"){
                val unfilledOrderExecutionLogs = ArrayList<Map<String,Any>>()
                val currentOrderExecutionLogs = mutableMapOf<String,Any>()
                currentOrderExecutionLogs["price"] = order.orderPrice
                currentOrderExecutionLogs["quantity"] = order.orderQuantity
                unfilledOrderExecutionLogs.add(currentOrderExecutionLogs)
                currentOrderDetails["unfilled"] = unfilledOrderExecutionLogs
            }else{
                if(order.orderStatus == "Partially Filled"){
                    val paritallyFilledOrderExecutionLogs = ArrayList<Map<String,Any>>()
                    order.orderExecutionLogs.forEach {
                        val currentOrderExecutionLogs = mutableMapOf<String,Any>()
                        currentOrderExecutionLogs["price"] = it.orderExecutionPrice
                        currentOrderExecutionLogs["quantity"] = it.orderExecutionQuantity
                        paritallyFilledOrderExecutionLogs.add(currentOrderExecutionLogs)
                    }
                    currentOrderDetails["partially_filled"] = paritallyFilledOrderExecutionLogs

                    val unfilledOrderExecutionLogs = ArrayList<Map<String,Any>>()
                    val currentOrderExecutionLogs = mutableMapOf<String,Any>()
                    currentOrderExecutionLogs["price"] = order.orderPrice
                    currentOrderExecutionLogs["quantity"] = order.remainingOrderQuantity
                    unfilledOrderExecutionLogs.add(currentOrderExecutionLogs)
                    currentOrderDetails["unfilled"] = unfilledOrderExecutionLogs
                }
                else if(order.orderStatus == "Filled"){
                    val filledOrderExecutionLogs = ArrayList<Map<String,Any>>()
                    order.orderExecutionLogs.forEach {
                        val currentOrderExecutionLogs = mutableMapOf<String,Any>()
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