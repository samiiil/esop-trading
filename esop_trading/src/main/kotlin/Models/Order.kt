package Models
class Order (val userName: String,
             val orderId: Long,
             val orderQuantity: Long,
             val orderPrice: Long,
             val orderType: String,
             var orderStatus: String = "Unfilled"){
    val orderExecutionLogs: ArrayList<OrderExecutionLogs> = ArrayList()
    private var remainingOrderQuantity: Long = orderQuantity
    fun addOrderExecutionLogs(orderExecuted: OrderExecutionLogs){
        if(orderExecuted.orderExecutionQuantity == this.remainingOrderQuantity){
            this.orderStatus = "Filled"
        }
        if(orderExecuted.orderExecutionQuantity < this.remainingOrderQuantity){
            this.orderStatus = "Partially Filled"
        }
        this.remainingOrderQuantity = this.remainingOrderQuantity - orderExecuted.orderExecutionQuantity
        orderExecutionLogs.add(orderExecuted)
    }

    fun getRemainingOrderQuantity(): Long{
        return this.remainingOrderQuantity
    }

    override fun toString(): String{
        return "username:$userName\norderId:$orderId\norderQuantity:$orderQuantity\norderPrice:$orderPrice\norderType:$orderType\norderStatus:$orderStatus\nremainingQuantity:${getRemainingOrderQuantity()}\n\n"
    }
}