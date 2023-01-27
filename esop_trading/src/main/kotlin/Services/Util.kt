package services

import models.DataStorage
import models.Order
import models.OrderExecutionLogs
import kotlin.math.min
import kotlin.math.roundToLong


class Util {
    companion object {
        const val MAX_AMOUNT = 1000000000

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
            val buyOrders = DataStorage.buyList
            if(buyOrders.isEmpty()) return
            val currentBuyOrder = buyOrders.poll()
            matchWithPerformanceSellOrders(currentBuyOrder)
            matchWithNonPerformanceSellOrders(currentBuyOrder)

            if(currentBuyOrder.remainingOrderQuantity > 0) buyOrders.add(currentBuyOrder)
            else matchOrders()
        }

        private fun matchWithPerformanceSellOrders(buyOrder: Order) {
            val performanceSellOrders = DataStorage.performanceSellList.iterator()
            while (performanceSellOrders.hasNext() && buyOrder.remainingOrderQuantity > 0) {
                val currentPerformanceSellOrder = performanceSellOrders.next()
                processOrder(buyOrder, currentPerformanceSellOrder, true)
                if (currentPerformanceSellOrder.remainingOrderQuantity == 0L)
                    performanceSellOrders.remove()
            }
        }

        private fun matchWithNonPerformanceSellOrders(buyOrder: Order) {
            val sellOrders = DataStorage.sellList
            while (sellOrders.isNotEmpty() && buyOrder.remainingOrderQuantity > 0) {
                val currentSellOrder = sellOrders.poll()

                //Sell list is sorted to have best deals come first.
                //If the top of the heap is not good enough, no point searching further
                if (currentSellOrder.orderPrice > buyOrder.orderPrice) break
                processOrder(buyOrder, currentSellOrder, false)
                if (currentSellOrder.remainingOrderQuantity > 0)
                    sellOrders.add(currentSellOrder)
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