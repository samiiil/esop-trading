package Services
import Models.Data
import Models.User
import Models.Order
import Models.OrderExecutionLogs
import kotlin.math.min
import kotlin.math.roundToLong
import COMMISSION_FEE_PERCENTAGE
import TOTAL_FEE_COLLECTED

class Util {
    companion object{
        fun validateUser(userName: String):Boolean{
            if(Data.userList.containsKey(userName)){
                return true
            }
            return false
        }

        fun validateEmailIds(emailId: String):Boolean{
            if(Data.registeredEmails.contains(emailId)){
                return true
            }
            return false
        }

        fun validatePhoneNumber(phoneNumber: String):Boolean{
            if(Data.registeredPhoneNumbers.contains(phoneNumber)){
                return true
            }
            return false
        }

        fun createUser(userName: String, firstName: String, lastName: String, phoneNumber: String, emailId: String){
            Data.userList.put(userName,User(userName,firstName,lastName,phoneNumber,emailId))
            Data.registeredPhoneNumbers.add(phoneNumber)
            Data.registeredEmails.add(emailId)
        }

        @Synchronized
        fun generateOrderId():Long{
            return Data.orderId++
        }

        @Synchronized
        fun generateOrderExecutionId():Long {
            return Data.orderExecutionId++
        }

        @Synchronized
        fun addOrderToBuyList(order:Order){
            Data.buyList.add(order)
        }
        @Synchronized
        fun addOrderToSellList(order:Order){
            Data.sellList.add(order)
        }

        @Synchronized
        fun addOrderToPerformanceSellList(order:Order){
            Data.performanceSellList.add(order)
        }

        fun matchOrders(){
            if(!Data.buyList.isEmpty()){
                val buyOrders = Data.buyList.iterator()
                while(buyOrders.hasNext()){
                    val currentBuyOrder = buyOrders.next()
                    val performanceSellOrders = Data.performanceSellList.iterator()
                    while(performanceSellOrders.hasNext() && currentBuyOrder.remainingOrderQuantity > 0){
                        val currentPerformanceSellOrder = performanceSellOrders.next()
                        processOrder(currentBuyOrder, currentPerformanceSellOrder, true)
                        if(currentPerformanceSellOrder.remainingOrderQuantity <= currentBuyOrder.remainingOrderQuantity)
                            performanceSellOrders.remove()
                        if(currentBuyOrder.remainingOrderQuantity <= currentPerformanceSellOrder.remainingOrderQuantity)
                            Data.buyList.remove(currentBuyOrder)
                    }

                    if(Data.buyList.isEmpty() || Data.sellList.isEmpty() || (Data.sellList.isNotEmpty() && Data.sellList.peek().orderPrice > currentBuyOrder.orderPrice)) break
                    val sellOrders = Data.sellList.iterator()
                    while(sellOrders.hasNext() && currentBuyOrder.remainingOrderQuantity > 0){
                        val currentSellOrder = sellOrders.next()
                        processOrder(currentBuyOrder, currentSellOrder,false)
                        if(currentSellOrder.remainingOrderQuantity <= currentBuyOrder.remainingOrderQuantity)
                            sellOrders.remove()
                        if(currentBuyOrder.remainingOrderQuantity <= currentSellOrder.remainingOrderQuantity)
                            Data.buyList.remove(currentBuyOrder)
                    }
                }
            }
        }

        private fun processOrder(buyOrder: Order, sellOrder: Order, isPerformanceESOP:Boolean){
            if(sellOrder.orderPrice <= buyOrder.orderPrice){
                val sellQuantity = sellOrder.remainingOrderQuantity
                val buyQuantity = buyOrder.remainingOrderQuantity
                val sellerAccount = Data.userList.get(sellOrder.userName)!!.account
                val buyerAccount = Data.userList.get(buyOrder.userName)!!.account
                val orderExecutionPrice = sellOrder.orderPrice
                val orderQuantity = min(sellQuantity,buyQuantity)
                val orderAmount = orderQuantity * orderExecutionPrice
                sellerAccount.inventory.updateLockedInventory(orderQuantity, isPerformanceESOP)
                sellerAccount.wallet.addMoneyToWallet((orderAmount*(1-COMMISSION_FEE_PERCENTAGE)).roundToLong())
                TOTAL_FEE_COLLECTED += (orderAmount*COMMISSION_FEE_PERCENTAGE*0.01).roundToLong()
                buyerAccount.wallet.updateLockedMoney(orderAmount)
                buyerAccount.inventory.addEsopToInventory(orderQuantity)
                if(buyOrder.orderPrice > orderExecutionPrice){
                    val amountToBeMovedFromLockedWalletToFreeWallet = orderQuantity * (buyOrder.orderPrice - orderExecutionPrice)
                    buyerAccount.wallet.updateLockedMoney(amountToBeMovedFromLockedWalletToFreeWallet)
                    buyerAccount.wallet.addMoneyToWallet(amountToBeMovedFromLockedWalletToFreeWallet)
                }
                val orderExecutionLog = OrderExecutionLogs(generateOrderExecutionId(), orderExecutionPrice, orderQuantity)
                sellOrder.addOrderExecutionLogs(orderExecutionLog)
                buyOrder.addOrderExecutionLogs(orderExecutionLog)
            }
        }
    }
}