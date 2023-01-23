package Models

data class CreateOrderInput(val quantity: Int, val esop_type: String?, val order_type: String, val price: Int)