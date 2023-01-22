package Controller

import Models.DataStorage
import Services.Util
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.json.tree.JsonObject

@Controller("/")
class EndPoints {
    @Post("/user/register")
    fun register(@Body body: JsonObject): HttpResponse<*> {

        //Input Parsing
        val firstName = body.get("firstName").stringValue.trim()
        val lastName = body.get("lastName").stringValue.trim()
        val phoneNumber = body.get("phoneNumber").stringValue.trim()
        val email = body.get("email").stringValue.trim()
        val username = body.get("username").stringValue.trim()


        val errorMessages: ArrayList<String> = ArrayList()

        if (Util.validateUser(username)) {
            errorMessages.add("username already exists.")
        }
        if (Util.validateEmailIds(email)) {
            errorMessages.add("Email Id already exists.")
        }
        if (Util.validatePhoneNumber(phoneNumber)) {
            errorMessages.add("Phone Number already exists.")
        }
        if (errorMessages.size == 0) {
            Util.createUser(username, firstName, lastName, phoneNumber, email)
        }

        val response: Map<String, *>
        if (errorMessages.size > 0) {
            response = mapOf("error" to errorMessages)
            return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response)
        } else {
            response = mapOf("message" to "User created successfully!!")
            return HttpResponse.status<Any>(HttpStatus.OK).body(response)
        }
    }

    @Post("/user/{username}/addToWallet")
    fun addToWallet(username: String, @Body body: JsonObject): HttpResponse<*> {

        //Input Parsing
        val amountToBeAdded = body.get("amount").bigIntegerValue.toLong()

        val errorMessages: ArrayList<String> = ArrayList()

        val response: Map<String, *>
        if (!Util.validateUser(username)) {
            errorMessages.add("username does not exists.")
            response = mapOf("error" to errorMessages)
            return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response)
        }

        DataStorage.userList[username]!!.account.wallet.addMoneyToWallet(amountToBeAdded)
        response = mapOf("message" to "$amountToBeAdded amount added to account")
        return HttpResponse.status<Any>(HttpStatus.OK).body(response)
    }

    @Post("/user/{username}/addToInventory")
    fun addToInventory(username: String, @Body body: JsonObject): HttpResponse<*> {

        //Input Parsing
        val quantityToBeAdded = body.get("quantity")?.bigIntegerValue?.toLong() ?: 0
        val typeOfESOP = body.get("esop_type")?.stringValue?.uppercase() ?: "NON-PERFORMANCE"
        val errorMessages: ArrayList<String> = ArrayList()

        val response: Map<String, *>
        if (!Util.validateUser(username))
            errorMessages.add("username does not exists.")
        if (quantityToBeAdded <= 0 || quantityToBeAdded > 1000)
            errorMessages.add("Invalid quantity of ESOPs")
        if (typeOfESOP !in arrayOf("PERFORMANCE", "NON-PERFORMANCE"))
            errorMessages.add("Invalid ESOP type")
        if (errorMessages.size > 0) {
            response = mapOf("error" to errorMessages)
            return HttpResponse.badRequest(response)
        }
        DataStorage.userList[username]!!.account.inventory.addEsopToInventory(quantityToBeAdded, typeOfESOP)

        response = mapOf("message" to "$quantityToBeAdded $typeOfESOP ESOPs added to account")
        return HttpResponse.status<Any>(HttpStatus.OK).body(response)
    }

    @Get("/user/{username}/accountInformation")
    fun accountInformation(username: String): HttpResponse<*> {

        val errorMessages: ArrayList<String> = ArrayList()

        val response: Map<String, *>
        if (!Util.validateUser(username)) {
            errorMessages.add("username does not exists.")
            response = mapOf("error" to errorMessages)
            return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response)
        }

        response = mapOf(
            "FirstName" to DataStorage.userList[username]!!.firstName,
            "LastName" to DataStorage.userList[username]!!.lastName,
            "Phone" to DataStorage.userList[username]!!.phoneNumber,
            "Email" to DataStorage.userList[username]!!.emailId,
            "Wallet" to mapOf(
                "free" to DataStorage.userList[username]!!.account.wallet.getFreeMoney(),
                "locked" to DataStorage.userList[username]!!.account.wallet.getLockedMoney()
            ),
            "Inventory" to arrayListOf<Any>(
                mapOf(
                    "esop_type" to "PERFORMANCE",
                    "free" to DataStorage.userList[username]!!.account.inventory.getFreePerformanceInventory(),
                    "locked" to DataStorage.userList[username]!!.account.inventory.getLockedPerformanceInventory()
                ),
                mapOf(
                    "esop_type" to "NON-PERFORMANCE",
                    "free" to DataStorage.userList[username]!!.account.inventory.getFreeInventory(),
                    "locked" to DataStorage.userList[username]!!.account.inventory.getLockedInventory()
                )
            )
        )
        return HttpResponse.status<Any>(HttpStatus.OK).body(response)
    }

    @Post("/user/{username}/createOrder")
    fun createOrder(username: String, @Body body: JsonObject): HttpResponse<*> {
        val errorMessages: ArrayList<String> = ArrayList()

        val response: Map<String, *>

        if (!Util.validateUser(username)) {
            errorMessages.add("username does not exists.")
            response = mapOf("error" to errorMessages)
            return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response)
        }

        //Input Parsing
        val orderQuantity: Long = body.get("quantity").bigIntegerValue.toLong()
        val orderType: String = body.get("order_type").stringValue.trim()
        val orderAmount: Long = body.get("price").bigIntegerValue.toLong()
        val typeOfESOP: String = body.get("esop_type")?.stringValue ?: "NON-PERFORMANCE".trim().uppercase()
        //Create Order
        val result = DataStorage.userList[username]!!.addOrder(orderQuantity, orderType, orderAmount, typeOfESOP)

        if (result != "Order Placed Successfully.") {
            errorMessages.add(result)
            response = mapOf("error" to errorMessages)
            return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response)
        }

        response = mapOf("message" to result)

        return HttpResponse.status<Any>(HttpStatus.OK).body(response)
    }

    @Get("/user/{username}/orderHistory")
    fun orderHistory(username: String): HttpResponse<*> {
        val errorMessages: ArrayList<String> = ArrayList()

        val response: Map<String, *>

        if (!Util.validateUser(username)) {
            errorMessages.add("username does not exists.")
            response = mapOf("error" to errorMessages)
            return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response)
        }

        response = DataStorage.userList[username]!!.getOrderDetails()
        return HttpResponse.status<Any>(HttpStatus.OK).body(response)
    }

    @Get("/fees")
    fun getFees(): HttpResponse<*> {
        return HttpResponse.status<Any>(HttpStatus.OK).body(mapOf(Pair("TotalFees", DataStorage.TOTAL_FEE_COLLECTED)))
    }
}