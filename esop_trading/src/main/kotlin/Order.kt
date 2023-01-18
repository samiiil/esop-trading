class Order (val userName: String,
             val orderId: Long,
             val orderQuantity: Long,
             val orderPrice: Long,
             val orderType: String,
             var orderStatus: String = "Unfilled"){
    val orderExecutionLogs: ArrayList<OrderExecutionLogs> = ArrayList<OrderExecutionLogs>();
    private var remainingOrderQuantity: Long = orderQuantity;
    fun addOrderExecutionLogs(orderExecuted: OrderExecutionLogs){
        if(orderExecuted.orderExecutionQuantity == this.remainingOrderQuantity){
            this.orderStatus = "Filled";
        }
        if(orderExecuted.orderExecutionQuantity < this.remainingOrderQuantity){
            this.orderStatus = "Partially Filled";
        }
        this.remainingOrderQuantity = this.remainingOrderQuantity - orderExecuted.orderExecutionQuantity;
        orderExecutionLogs.add(orderExecuted);
    }

    fun getRemainingOrderQuantity(): Long{
        return this.remainingOrderQuantity;
    }
}