package Services

import Models.DataStorage
import Models.Order
import Models.OrderExecutionLogs
import Models.User
import kotlin.math.min
import kotlin.math.roundToLong

class Util {
    companion object {
        fun validateUser(userName: String): Boolean {
            if (DataStorage.userList.containsKey(userName)) {
                return true
            }
            return false
        }

        fun validateEmailIds(emailId: String): Boolean {
            if (DataStorage.registeredEmails.contains(emailId)) {
                return true
            }
            return false
        }

        fun validatePhoneNumber(phoneNumber: String): Boolean {
            if (DataStorage.registeredPhoneNumbers.contains(phoneNumber)) {
                return true
            }
            return false
        }

        fun createUser(userName: String, firstName: String, lastName: String, phoneNumber: String, emailId: String) {
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
            if (!DataStorage.buyList.isEmpty()) {
                val buyOrders = DataStorage.buyList.iterator()
                while (buyOrders.hasNext()) {
                    val currentBuyOrder = buyOrders.next()
                    val performanceSellOrders = DataStorage.performanceSellList.iterator()
                    while (performanceSellOrders.hasNext() && currentBuyOrder.remainingOrderQuantity > 0) {
                        val currentPerformanceSellOrder = performanceSellOrders.next()
                        processOrder(currentBuyOrder, currentPerformanceSellOrder, true)
                        if (currentPerformanceSellOrder.remainingOrderQuantity <= currentBuyOrder.remainingOrderQuantity)
                            performanceSellOrders.remove()
                        if (currentBuyOrder.remainingOrderQuantity <= currentPerformanceSellOrder.remainingOrderQuantity)
                            DataStorage.buyList.remove(currentBuyOrder)
                    }

                    if (DataStorage.buyList.isEmpty() || DataStorage.sellList.isEmpty() || (DataStorage.sellList.isNotEmpty() && DataStorage.sellList.peek().orderPrice > currentBuyOrder.orderPrice)) break
                    val sellOrders = DataStorage.sellList.iterator()
                    while (sellOrders.hasNext() && currentBuyOrder.remainingOrderQuantity > 0) {
                        val currentSellOrder = sellOrders.next()
                        processOrder(currentBuyOrder, currentSellOrder, false)
                        if (currentSellOrder.remainingOrderQuantity <= currentBuyOrder.remainingOrderQuantity)
                            sellOrders.remove()
                        if (currentBuyOrder.remainingOrderQuantity <= currentSellOrder.remainingOrderQuantity)
                            DataStorage.buyList.remove(currentBuyOrder)
                    }
                }
            }
        }

        private fun processOrder(buyOrder: Order, sellOrder: Order, isPerformanceESOP: Boolean) {
            if (sellOrder.orderPrice <= buyOrder.orderPrice) {
                val sellQuantity = sellOrder.remainingOrderQuantity
                val buyQuantity = buyOrder.remainingOrderQuantity
                val sellerAccount = DataStorage.userList.get(sellOrder.userName)!!.account
                val buyerAccount = DataStorage.userList.get(buyOrder.userName)!!.account
                val orderExecutionPrice = sellOrder.orderPrice
                val orderQuantity = min(sellQuantity, buyQuantity)
                val orderAmount = orderQuantity * orderExecutionPrice
                sellerAccount.inventory.updateLockedInventory(orderQuantity, isPerformanceESOP)
                sellerAccount.wallet.addMoneyToWallet((orderAmount * (1 - DataStorage.COMMISSION_FEE_PERCENTAGE)).roundToLong())
                DataStorage.TOTAL_FEE_COLLECTED += (orderAmount * DataStorage.COMMISSION_FEE_PERCENTAGE * 0.01).roundToLong()
                buyerAccount.wallet.updateLockedMoney(orderAmount)
                buyerAccount.inventory.addEsopToInventory(orderQuantity)
                if (buyOrder.orderPrice > orderExecutionPrice) {
                    val amountToBeMovedFromLockedWalletToFreeWallet =
                        orderQuantity * (buyOrder.orderPrice - orderExecutionPrice)
                    buyerAccount.wallet.updateLockedMoney(amountToBeMovedFromLockedWalletToFreeWallet)
                    buyerAccount.wallet.addMoneyToWallet(amountToBeMovedFromLockedWalletToFreeWallet)
                }
                val orderExecutionLog =
                    OrderExecutionLogs(generateOrderExecutionId(), orderExecutionPrice, orderQuantity)
                sellOrder.addOrderExecutionLogs(orderExecutionLog)
                buyOrder.addOrderExecutionLogs(orderExecutionLog)
            }
        }
    }
}