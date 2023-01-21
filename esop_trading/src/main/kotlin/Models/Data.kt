package Models

import Services.BuyOrderingComparator
import Services.SellOrderingComparator
import java.util.*

class Data {
    companion object {
        val userList: HashMap<String, User> = HashMap()
        val registeredEmails = mutableSetOf<String>()
        val registeredPhoneNumbers = mutableSetOf<String>()

        var orderId: Long = 1L
        var orderExecutionId = 1L

        val buyList = PriorityQueue<Order>(BuyOrderingComparator)
        val sellList = PriorityQueue<Order>(SellOrderingComparator)
        val performanceSellList = LinkedList<Order>()
    }
}