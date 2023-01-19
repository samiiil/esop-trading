package Services
import Models.Data
import Models.User
import Models.Order
import Models.OrderExecutionLogs
import io.micronaut.json.tree.JsonNode
import io.micronaut.json.tree.JsonObject
import org.jetbrains.annotations.NotNull
import java.lang.Exception

class Util {
    companion object{

        fun validateBody(body: JsonObject): ArrayList<String>{
            val errorList = arrayListOf<String>()

            try {
                val firstName: Boolean = body.get("firstName").isNull
                println(firstName)
            }catch (e: Exception){
                errorList.add("firstname is null")
            }

            try {
                val firstName: Boolean = body.get("username").isNull
                println(firstName)
            }catch (e: Exception){
                errorList.add("username is null")
            }

            try {
                val firstName: Boolean = body.get("lastName").isNull
                println(firstName)
            }catch (e: Exception){
                errorList.add("lastname is null")
            }

            try {
                val firstName: Boolean = body.get("email").isNull
                println(firstName)
            }catch (e: Exception){
                errorList.add("email is null")
            }

            try {
                val firstName: Boolean = body.get("phoneNumber").isNull
                println(firstName)
            }catch (e: Exception){
                errorList.add("phoneNumber is null")
            }
            return errorList
        }
        fun validateNames(name: String, type: String): ArrayList<String>{
            val errorList = arrayListOf<String>()

            if(name.matches(Regex("[A-Za-z]+"))){
                errorList.add("$type should only contain characters")
            }
            return errorList
        }
        fun validateUser(userName: String):Boolean{
            if(Data.userList.containsKey(userName)){
                return true;
            }
            return false;
        }

        fun validateEmailIds(emailId: String): ArrayList<String>{
            val errorList = arrayListOf<String>()
            //val pattern = Regex("(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])\n")
            if(!emailId.matches(Regex("^[A-Za-z](.*)([@]{1})(.{1,})(\\\\.)(.{1,})"))){
                    errorList.add("Invalid email address")
            }

            if(Data.registeredEmails.contains(emailId)){
                errorList.add("Email already exist")

            }
            return errorList
        }

        fun validatePhoneNumber(phoneNumber: String, errorList: ArrayList<String>): ArrayList<String>{
            val errorList = arrayListOf<String>()
            if(Data.registeredPhoneNumbers.contains(phoneNumber)){
                errorList.add("Phone number already exists")
            }

            if(phoneNumber.length != 10 || phoneNumber.matches(Regex("[0-9]+"))){
                errorList.add("Invalid phone number")
            }
            return errorList
        }

        fun createUser(@NotNull userName: String, firstName: String, lastName: String, phoneNumber: String, emailId: String){

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