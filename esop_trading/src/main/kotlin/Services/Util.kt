package Services
import Models.Data
import Models.User
import Models.Order
import Models.OrderExecutionLogs
class Util {
    companion object{
        fun validateUser(userName: String):Boolean{
            if(Data.userList.containsKey(userName)){
                return true;
            }
            return false;
        }

        fun validateEmailIds(emailId: String):Boolean{
            if(Data.registeredEmails.contains(emailId)){
                return true;
            }
            return false;
        }

        fun validatePhoneNumber(phoneNumber: String):Boolean{
            if(Data.registeredPhoneNumbers.contains(phoneNumber)){
                return true;
            }
            return false;
        }

        fun createUser(userName: String, firstName: String, lastName: String, phoneNumber: String, emailId: String){
            Data.userList.put(userName,User(userName,firstName,lastName,phoneNumber,emailId));
            Data.registeredPhoneNumbers.add(phoneNumber);
            Data.registeredEmails.add(emailId);
        }

        @Synchronized
        fun generateOrderId():Long{
            return Data.orderId++;
        }

        @Synchronized
        fun generateOrderExecutionId():Long {
            return Data.orderExecutionId++;
        }

        @Synchronized
        fun addOrderToBuyList(order:Order){
            Data.buyList.add(order)
        }
        @Synchronized
        fun addOrderToSellList(order:Order){
            Data.sellList.add(order)
        }

        fun processOrder(){
            if(!Data.sellList.isEmpty() && !Data.buyList.isEmpty()){
                val sellOrders = Data.sellList.iterator()
                while(sellOrders.hasNext()){
                    val currentSellOrder = sellOrders.next()
                    if ((!Data.buyList.isEmpty() && currentSellOrder.orderPrice > Data.buyList.peek().orderPrice) || Data.buyList.isEmpty()){
                        break;
                    }
                    val buyOrders = Data.buyList.iterator()
                    while(buyOrders.hasNext()){
                        val currentBuyOrder = buyOrders.next()
                        if(currentSellOrder.orderPrice <= currentBuyOrder.orderPrice){
                            val sellQuantity = currentSellOrder.getRemainingOrderQuantity()
                            val buyQuantity = currentBuyOrder.getRemainingOrderQuantity()
                            val sellerAccount = Data.userList.get(currentSellOrder.userName)!!.account
                            val buyerAccount = Data.userList.get(currentBuyOrder.userName)!!.account
                            if( sellQuantity < buyQuantity ){
                                val orderExecutionPrice = currentSellOrder.orderPrice
                                sellerAccount.inventory.updateLockedInventory(sellQuantity)
                                sellerAccount.wallet.addMoneyToWallet((orderExecutionPrice*sellQuantity))
                                val orderExecutionLog = OrderExecutionLogs(generateOrderExecutionId(),orderExecutionPrice,sellQuantity)
                                currentSellOrder.addOrderExecutionLogs(orderExecutionLog)
                                //remove SellOrder From Priority Queue
                                sellOrders.remove();

                                buyerAccount.wallet.updateLockedMoney((orderExecutionPrice*sellQuantity))
                                buyerAccount.inventory.addEsopToInventory(sellQuantity)
                                currentBuyOrder.addOrderExecutionLogs(orderExecutionLog)
                                if(currentBuyOrder.orderPrice > orderExecutionPrice){
                                    val amountToBeMovedFromLockedWalletToFreeWallet = ((currentBuyOrder.orderQuantity*currentBuyOrder.orderPrice) - (orderExecutionPrice*sellQuantity) - (currentBuyOrder.orderPrice * currentBuyOrder.getRemainingOrderQuantity()))
                                    buyerAccount.wallet.updateLockedMoney(amountToBeMovedFromLockedWalletToFreeWallet)
                                    buyerAccount.wallet.addMoneyToWallet(amountToBeMovedFromLockedWalletToFreeWallet)
                                }
                            }
                            else if( sellQuantity > buyQuantity ){
                                val orderExecutionPrice = currentSellOrder.orderPrice
                                buyerAccount.wallet.updateLockedMoney((orderExecutionPrice*buyQuantity))
                                buyerAccount.inventory.addEsopToInventory(buyQuantity)
                                val orderExecutionLog = OrderExecutionLogs(generateOrderExecutionId(),orderExecutionPrice,buyQuantity)
                                currentBuyOrder.addOrderExecutionLogs(orderExecutionLog)
                                if(currentBuyOrder.orderPrice > orderExecutionPrice){
                                    val amountToBeMovedFromLockedWalletToFreeWallet = (currentBuyOrder.orderPrice - orderExecutionPrice) * buyQuantity
                                    buyerAccount.wallet.updateLockedMoney(amountToBeMovedFromLockedWalletToFreeWallet)
                                    buyerAccount.wallet.addMoneyToWallet(amountToBeMovedFromLockedWalletToFreeWallet)
                                }
                                //remove BuyOrder From Priority Queue
                                buyOrders.remove()

                                sellerAccount.inventory.updateLockedInventory(buyQuantity)
                                sellerAccount.wallet.addMoneyToWallet((orderExecutionPrice*buyQuantity))
                                currentSellOrder.addOrderExecutionLogs(orderExecutionLog)
                            }
                            else if( sellQuantity == buyQuantity ){
                                val orderExecutionPrice = currentSellOrder.orderPrice
                                sellerAccount.inventory.updateLockedInventory(sellQuantity)
                                sellerAccount.wallet.addMoneyToWallet((sellQuantity*orderExecutionPrice))
                                val orderExecutionLog = OrderExecutionLogs(generateOrderExecutionId(),orderExecutionPrice,sellQuantity)
                                currentSellOrder.addOrderExecutionLogs(orderExecutionLog)
                                //remove SellOrder From Priority Queue
                                sellOrders.remove()

                                buyerAccount.inventory.addEsopToInventory(sellQuantity)
                                buyerAccount.wallet.updateLockedMoney((sellQuantity*orderExecutionPrice))
                                if(currentBuyOrder.orderPrice > orderExecutionPrice){
                                    val amountToBeMovedFromLockedWalletToFreeWallet = (currentBuyOrder.orderPrice - orderExecutionPrice) * sellQuantity
                                    buyerAccount.wallet.updateLockedMoney(amountToBeMovedFromLockedWalletToFreeWallet)
                                    buyerAccount.wallet.addMoneyToWallet(amountToBeMovedFromLockedWalletToFreeWallet)
                                }
                                currentBuyOrder.addOrderExecutionLogs(orderExecutionLog)
                                //remove BuyOrder from Priority Queue
                                buyOrders.remove()
                            }
                        }

                    }
                }
            }
        }
    }
}