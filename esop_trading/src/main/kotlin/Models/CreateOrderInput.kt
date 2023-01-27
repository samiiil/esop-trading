package Models

data class CreateOrderInput(val quantity: Int? = null,
                            val esop_type: String?,
                            val order_type: String? = null,
                            val price: Int? = null)