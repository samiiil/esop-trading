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
        const val MAX_AMOUNT = 1000000000
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
        fun validateFirstName(name: String): ArrayList<String>{
            val errorList = arrayListOf<String>()

            if(name.length<3){
                errorList.add("First name has to be at least three characters.")
            }
            if(!name.matches(Regex("([A-Za-z]+ ?)+"))){
                errorList.add("Invalid FirstName.")
            }
            return errorList
        }
        fun validateLastName(name: String): ArrayList<String>{
            val errorList = arrayListOf<String>()
            if(name.length<1){
                errorList.add("Last name has to be at least one characters.")
            }
            if(!name.matches(Regex("([A-Za-z]+ ?)+"))){
                errorList.add("Invalid LastName.")
            }
            for(i in errorList)
            println(i)
            return errorList
        }
        fun validateUserName(username: String): ArrayList<String>{
            val errorList = arrayListOf<String>()
            if(Data.userList.contains(username)){
                errorList.add("Username already taken")
            }
            if(username.length<3){
                errorList.add("Username has to be at least three characters.")
            }
            if(!username.matches(Regex("_*[A-Za-z][\\w_]*"))){
                errorList.add("Username can only contain characters,numbers and underscores and must have at least one character.")
            }
            return errorList

        }
        fun validateUser(userName: String):Boolean{
            if(Data.userList.containsKey(userName)) {
                return true;
            }
               return  false;
        }

        fun validateEmailIds(emailId: String): ArrayList<String>{
            val errorList = arrayListOf<String>()
            if(Data.registeredEmails.contains(emailId)){
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

            if(!emailId.matches(Regex("^[a-zA-Z0-9.!#\$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*\$"))){
                errorList.add("Invalid email address")
            }
            var index=0
            if(emailId[index]=='.'){
                errorList.add("Invalid Email address")
            }
            else {
                while (index < emailId.length) {
                    if (emailId[index] == '.') {
                        if (emailId[index] == emailId[index + 1]) {
                            errorList.add("Invalid Email address")
                            break
                        }
                    }
                    index++;
                }
            }
            return errorList
        }

        fun validatePhoneNumber(phoneNumber: String, errorList: ArrayList<String>): ArrayList<String>{
            val errorList = arrayListOf<String>()
            if(Data.registeredPhoneNumbers.contains(phoneNumber)){
                errorList.add("Phone number already exists")
            }
            if(phoneNumber.length<10){
                errorList.add("Invalid phone number")
            }
            if(phoneNumber.length==13 ){
                if(!phoneNumber.substring(0,3).matches(Regex("\\+?\\d\\d"))  && phoneNumber.substring(3).matches(Regex("\\d*")))
                   errorList.add("Invalid phone number")
            }
            else if(phoneNumber.length==12){
                if(phoneNumber[0]=='+'){
                    if(!phoneNumber.substring(1).matches(Regex("\\d*"))){
                        errorList.add("Invalid Phone Number")
                        println("right here")
                    }
                }
                else{
                    if(!phoneNumber.matches(Regex("\\d*"))){
                        errorList.add("Invalid Phone Number")
                        println("left here")
                    }
                }
            }
            else if(phoneNumber.length==11 || phoneNumber.length==10){
                      if(!phoneNumber.matches(Regex("\\d*"))){
                          errorList.add("Invalid Phone Number")
                      }
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