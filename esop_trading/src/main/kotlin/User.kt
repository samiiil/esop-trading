class User(val userName: String,
           val firstName: String,
           val lastName: String,
           val phoneNumber: String,
           val emailId: String) {
    val account: Account = Account()
    val orders: ArrayList<Order> = ArrayList<Order>()

    fun addOrder(orderQuantity: Long, orderType: String, orderAmount: Long) : String{
        var response = ""
        if ( orderType == "BUY" ){
            val amountRequiredToBuy = orderQuantity*orderAmount
            response = account.wallet.moveFreeMoneyToLockedMoney(amountRequiredToBuy)
        }else if ( orderType == "SELL" ){
            response = account.inventory.moveFreeInventoryToLockedInventory(orderQuantity)
        }
        if (response == "Success"){
            val orderObj = Order(this.userName,Util.generateOrderId(),orderQuantity,orderAmount,orderType)
            orders.add(orderObj)
            if( orderType == "BUY" ){
                Util.addOrderToBuyList(orderObj)
            }else if ( orderType == "SELL" ){
                Util.addOrderToSellList(orderObj)
            }
            Util.processOrder()
            return "Order Placed Successfully."
        }else{
            return response
        }
    }

    fun getOrderDetails():Map<String,*>{
        if(orders.size == 0 ){
            return mapOf("order_history" to "[]")
        }

        val order_details = ArrayList<Map<String,Any>>()
        orders.forEach {
            val current_order_details = mutableMapOf<String,Any>()
            current_order_details.put("order_id",it.orderId)
            current_order_details.put("quantity",it.orderQuantity)
            current_order_details.put("type",it.orderType)
            current_order_details.put("price",it.orderPrice)


            if(it.orderStatus == "Unfilled"){
                val unfilled_order_execution_logs = ArrayList<Map<String,Any>>()
                val current_order_execution_logs = mutableMapOf<String,Any>()
                current_order_execution_logs.put("price",it.orderPrice)
                current_order_execution_logs.put("quantity",it.orderQuantity)
                unfilled_order_execution_logs.add(current_order_execution_logs)
                current_order_details.put("unfilled",unfilled_order_execution_logs)
            }else{
                if(it.orderStatus == "Partially Filled"){
                    val paritally_filled_order_execution_logs = ArrayList<Map<String,Any>>()
                    it.orderExecutionLogs.forEach {
                        val current_order_execution_logs = mutableMapOf<String,Any>()
                        current_order_execution_logs.put("price",it.orderExecutionPrice)
                        current_order_execution_logs.put("quantity",it.orderExecutionQuantity)
                        paritally_filled_order_execution_logs.add(current_order_execution_logs)
                    }
                    current_order_details.put("partially_filled",paritally_filled_order_execution_logs)

                    val unfilled_order_execution_logs = ArrayList<Map<String,Any>>()
                    val current_order_execution_logs = mutableMapOf<String,Any>()
                    current_order_execution_logs.put("price",it.orderPrice)
                    current_order_execution_logs.put("quantity",it.getRemainingOrderQuantity())
                    unfilled_order_execution_logs.add(current_order_execution_logs)
                    current_order_details.put("unfilled",unfilled_order_execution_logs)
                }
                else if(it.orderStatus == "Filled"){
                    val filled_order_execution_logs = ArrayList<Map<String,Any>>()
                    it.orderExecutionLogs.forEach {
                        val current_order_execution_logs = mutableMapOf<String,Any>()
                        current_order_execution_logs.put("price",it.orderExecutionPrice)
                        current_order_execution_logs.put("quantity",it.orderExecutionQuantity)
                        filled_order_execution_logs.add(current_order_execution_logs)
                    }
                    current_order_details.put("filled",filled_order_execution_logs)
                }
            }
            order_details.add(current_order_details)
        }

        return mapOf("order_history" to order_details)
    }
}