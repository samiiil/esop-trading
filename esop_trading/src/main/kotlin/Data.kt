import java.util.PriorityQueue

class Data {
    companion object {
        val userList : HashMap<String, User> = HashMap<String, User> ();
        val registeredEmails = mutableSetOf<String>();
        val registeredPhoneNumbers = mutableSetOf<String>();

        var orderId: Long = 1L;
        var orderExecutionId = 1L;

        val buyList = PriorityQueue<Order>(BuyOrderingComparator);
        val sellList = PriorityQueue<Order>(SellOrderingComparator);
    }
}