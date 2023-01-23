package Services

import Models.DataStorage
import Models.Order
import Models.OrderExecutionLogs
import Models.User
import io.micronaut.json.tree.JsonObject
import org.jetbrains.annotations.NotNull
import kotlin.math.min
import kotlin.math.roundToLong


class Util {
    companion object {
        const val MAX_AMOUNT = 1000000000
        fun validateBody(body: JsonObject): ArrayList<String> {
            val errorList = arrayListOf<String>()

            try {
                val firstName: Boolean = body.get("firstName").isNull
                println(firstName)
            } catch (e: Exception) {
                errorList.add("firstname is null")
            }

            try {
                val firstName: Boolean = body.get("username").isNull
                println(firstName)
            } catch (e: Exception) {
                errorList.add("username is null")
            }

            try {
                val firstName: Boolean = body.get("lastName").isNull
                println(firstName)
            } catch (e: Exception) {
                errorList.add("lastname is null")
            }

            try {
                val firstName: Boolean = body.get("email").isNull
                println(firstName)
            } catch (e: Exception) {
                errorList.add("email is null")
            }

            try {
                val firstName: Boolean = body.get("phoneNumber").isNull
                println(firstName)
            } catch (e: Exception) {
                errorList.add("phoneNumber is null")
            }
            return errorList
        }

        fun validateFirstName(name: String?): ArrayList<String> {
            val errorList = arrayListOf<String>()
            if(name.isNullOrBlank()){
                return errorList
            }
            if (name.length < 3) {
                errorList.add("First name has to be at least three characters.")
            }
            if (!name.matches(Regex("([A-Za-z]+ ?)+"))) {
                errorList.add("Invalid FirstName.")
            }
            return errorList
        }

        fun validateLastName(name: String?): ArrayList<String> {
            val errorList = arrayListOf<String>()
            if(name.isNullOrBlank())
                return errorList
            if (name.isEmpty()) {
                errorList.add("Last name has to be at least one characters.")
            }
            if (!name.matches(Regex("([A-Za-z]+ ?)+"))) {
                errorList.add("Invalid LastName.")
            }
            for (i in errorList)
                println(i)
            return errorList
        }

        fun validateUserName(username: String?): ArrayList<String> {
            val errorList = arrayListOf<String>()
            if(username.isNullOrBlank()){
                return errorList
            }
            if (DataStorage.userList.contains(username)) {
                errorList.add("Username already taken")
            }
            if (username.length < 3) {
                errorList.add("Username has to be at least three characters.")
            }
            if (!username.matches(Regex("[_\\d]*[A-Za-z][\\w_]*"))) {
                errorList.add("Username can only contain characters,numbers and underscores and must have at least one character.")
            }
            return errorList

        }

        fun validateUser(userName: String): Boolean {
            if (DataStorage.userList.containsKey(userName)) {
                return true
            }
            return false
        }

        fun validateEmailIds(emailId: String?): Collection<String> {
            val errorList = mutableSetOf<String>()
            if(emailId.isNullOrBlank()){
                return errorList
            }
            if (DataStorage.registeredEmails.contains(emailId)) {
                errorList.add("Email already exists")
            }


//            val EMAIL_ADDRESS_PATTERN = Pattern.compile(
//                "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
//                        "\\@" +
//                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
//                        "(" +
//                        "\\." +
//                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
//                        ")+"
//            )
//            if(!EMAIL_ADDRESS_PATTERN.matcher(emailId).matches()){
//                errorList.add("Invalid email address")
//            }

            if (!emailId.matches(Regex("^[a-zA-Z0-9.!#\$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*\$"))) {
                errorList.add("Invalid Email address")
            }
            var index = 0
            if (emailId[index] == '.') {
                errorList.add("Invalid Email address")
            } else {
                while (index < emailId.length) {
                    if (emailId[index] == '.') {
                        if (emailId[index] == emailId[index + 1]) {
                            errorList.add("Invalid Email address")
                            break
                        }
                    }
                    index++
                }
            }
            return errorList.toList()
        }

        fun validatePhoneNumber(phoneNumber: String?, errorList: ArrayList<String>): ArrayList<String> {
            val errorList = arrayListOf<String>()
            if(phoneNumber.isNullOrBlank()){
                return errorList
            }
            if (DataStorage.registeredPhoneNumbers.contains(phoneNumber)) {
                errorList.add("Phone number already exists")
            }
            if (phoneNumber.length < 10) {
                errorList.add("Invalid phone number")
            }
            if (phoneNumber.length == 13) {
                if (!phoneNumber.substring(0, 3).matches(Regex("\\+?\\d\\d")) && phoneNumber.substring(3)
                        .matches(Regex("\\d*"))
                )
                    errorList.add("Invalid phone number")
            } else if (phoneNumber.length == 12) {
                if (phoneNumber[0] == '+') {
                    if (!phoneNumber.substring(1).matches(Regex("\\d*"))) {
                        errorList.add("Invalid Phone Number")
                        println("right here")
                    }
                } else {
                    if (!phoneNumber.matches(Regex("\\d*"))) {
                        errorList.add("Invalid Phone Number")
                        println("left here")
                    }
                }
            } else if (phoneNumber.length == 11 || phoneNumber.length == 10) {
                if (!phoneNumber.matches(Regex("\\d*"))) {
                    errorList.add("Invalid Phone Number")
                }
            }
            return errorList
        }

        fun createUser(
            @NotNull userName: String,
            firstName: String,
            lastName: String,
            phoneNumber: String,
            emailId: String
        ) {

            DataStorage.userList.put(userName, User(userName, firstName, lastName, phoneNumber, emailId))
            DataStorage.registeredPhoneNumbers.add(phoneNumber)
            DataStorage.registeredEmails.add(emailId)
        }

        @Synchronized
        fun generateOrderId(): Long {
            return DataStorage.orderId++
        }

        @Synchronized
        fun generateOrderExecutionId(): Long {
            return DataStorage.orderExecutionId++
        }

        @Synchronized
        fun addOrderToBuyList(order: Order) {
            DataStorage.buyList.add(order)
        }

        @Synchronized
        fun addOrderToSellList(order: Order) {
            DataStorage.sellList.add(order)
        }

        @Synchronized
        fun addOrderToPerformanceSellList(order: Order) {
            DataStorage.performanceSellList.add(order)
        }

        fun matchOrders() {
            val buyOrders = DataStorage.buyList.iterator()
            while (buyOrders.hasNext()) {
                val currentBuyOrder = buyOrders.next()
                matchWithPerformanceSellOrders(currentBuyOrder)
                matchWithNonPerformanceSellOrders(currentBuyOrder)
            }
        }

        private fun matchWithPerformanceSellOrders(buyOrder: Order) {
            val performanceSellOrders = DataStorage.performanceSellList.iterator()
            while (performanceSellOrders.hasNext() && buyOrder.remainingOrderQuantity > 0) {
                val currentPerformanceSellOrder = performanceSellOrders.next()
                processOrder(buyOrder, currentPerformanceSellOrder, true)
                if (currentPerformanceSellOrder.remainingOrderQuantity <= buyOrder.remainingOrderQuantity)
                    performanceSellOrders.remove()
                if (buyOrder.remainingOrderQuantity <= currentPerformanceSellOrder.remainingOrderQuantity)
                    DataStorage.buyList.remove(buyOrder)
            }
        }

        private fun matchWithNonPerformanceSellOrders(buyOrder: Order) {
            val sellOrders = DataStorage.sellList.iterator()
            while (sellOrders.hasNext() && buyOrder.remainingOrderQuantity > 0) {
                val currentSellOrder = sellOrders.next()

                //Sell list is sorted to have best deals come first.
                //If the top of the heap is not good enough, no point searching further
                if (currentSellOrder.orderPrice > buyOrder.orderPrice) break
                processOrder(buyOrder, currentSellOrder, false)
                if (currentSellOrder.remainingOrderQuantity <= buyOrder.remainingOrderQuantity)
                    sellOrders.remove()
                if (buyOrder.remainingOrderQuantity <= currentSellOrder.remainingOrderQuantity)
                    DataStorage.buyList.remove(buyOrder)
            }
        }

        private fun processOrder(buyOrder: Order, sellOrder: Order, isPerformanceESOP: Boolean) {
            if (sellOrder.orderPrice <= buyOrder.orderPrice) {
                val orderExecutionPrice = sellOrder.orderPrice
                val orderQuantity = findOrderQuantity(buyOrder, sellOrder)
                val orderAmount = orderQuantity * orderExecutionPrice

                updateSellerInventoryAndWallet(sellOrder, orderQuantity, orderExecutionPrice, isPerformanceESOP)
                updateBuyerInventoryAndWallet(buyOrder, orderQuantity, orderExecutionPrice)

                DataStorage.TOTAL_FEE_COLLECTED += (orderAmount * DataStorage.COMMISSION_FEE_PERCENTAGE * 0.01).roundToLong()

                val orderExecutionLog =
                    OrderExecutionLogs(generateOrderExecutionId(), orderExecutionPrice, orderQuantity)
                sellOrder.addOrderExecutionLogs(orderExecutionLog)
                buyOrder.addOrderExecutionLogs(orderExecutionLog)
            }
        }

        private fun findOrderQuantity(buyOrder: Order, sellOrder: Order): Long {
            return min(buyOrder.remainingOrderQuantity, sellOrder.remainingOrderQuantity)
        }

        private fun updateSellerInventoryAndWallet(
            sellOrder: Order,
            orderQuantity: Long,
            orderExecutionPrice: Long,
            isPerformanceESOP: Boolean
        ) {
            val sellerAccount = DataStorage.userList[sellOrder.userName]!!.account
            val orderAmount = orderQuantity * orderExecutionPrice
            sellerAccount.inventory.updateLockedInventory(orderQuantity, isPerformanceESOP)
            sellerAccount.wallet.addMoneyToWallet((orderAmount * (1 - DataStorage.COMMISSION_FEE_PERCENTAGE * 0.01)).roundToLong())
        }

        private fun updateBuyerInventoryAndWallet(buyOrder: Order, orderQuantity: Long, orderExecutionPrice: Long) {
            val buyerAccount = DataStorage.userList[buyOrder.userName]!!.account
            val orderAmount = orderQuantity * orderExecutionPrice
            buyerAccount.wallet.updateLockedMoney(orderAmount)
            buyerAccount.inventory.addEsopToInventory(orderQuantity)

            //Need to send difference back to free wallet when high buy and low sell are paired
            if (buyOrder.orderPrice > orderExecutionPrice) {
                val amountToBeMovedFromLockedWalletToFreeWallet =
                    orderQuantity * (buyOrder.orderPrice - orderExecutionPrice)
                buyerAccount.wallet.updateLockedMoney(amountToBeMovedFromLockedWalletToFreeWallet)
                buyerAccount.wallet.addMoneyToWallet(amountToBeMovedFromLockedWalletToFreeWallet)
            }
        }
    }
}