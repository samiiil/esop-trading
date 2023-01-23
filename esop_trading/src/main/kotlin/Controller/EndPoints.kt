package Controller

import Models.DataStorage
import Models.RegisterInput
import Services.Util
import com.fasterxml.jackson.core.JsonParseException
import io.micronaut.core.convert.exceptions.ConversionErrorException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.http.hateoas.JsonError
import io.micronaut.json.tree.JsonObject

@Controller("/")
class EndPoints {
    @Post("/user/register")
    fun register(@Body body: RegisterInput): HttpResponse<*> {
        val errorList = arrayListOf<String>()
        //Input Parsing
//        for(error in Util.validateBody(body)){
//            errorList.add(error)
//        }
        if (errorList.isNotEmpty()) {
            val response: Map<String, *>
            response = mapOf("error" to errorList)
            return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response)
        }

        val firstName: String = body.firstName
        val lastName: String = body.lastName
        val phoneNumber: String = body.phoneNumber
        val email: String = body.email
        val username: String = body.username

        if (firstName.isEmpty())
            println("first name is null")
        if (lastName.isEmpty())
            println("Last name is null")
        // println(username)

        for (error in Util.validateFirstName(firstName)) errorList.add(error)
        for (error in Util.validateLastName(lastName)) errorList.add(error)
        for (error in Util.validatePhoneNumber(phoneNumber, errorList)) errorList.add(error)
        for (error in Util.validateEmailIds(email)) errorList.add(error)
        for (error in Util.validateUserName(username)) errorList.add(error)

        if (errorList.isEmpty()) {
            Util.createUser(username, firstName, lastName, phoneNumber, email)
        }

        val response: Map<String, *>
        return if (errorList.isNotEmpty()) {
            response = mapOf("error" to errorList)
            HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response)
        } else {
            response = mapOf("message" to "User created successfully!!")
            HttpResponse.status<Any>(HttpStatus.OK).body(response)
        }
    }

    @Post("/user/{username}/addToWallet")
    fun addToWallet(username: String, @Body body: JsonObject): HttpResponse<*> {
        val errorMessages: ArrayList<String> = ArrayList<String>()
        //Input Parsing
        var amountToBeAdded: Long = 0
        try {
            amountToBeAdded = body.get("amount").bigIntegerValue.toLong()
        } catch (e: Exception) {
            errorMessages.add("Amount value is not integer")
        }


        val response: Map<String, *>
        if (!Util.validateUser(username)) {
            errorMessages.add("Username does not exists.")

        }
        if (amountToBeAdded <= 0 || amountToBeAdded > Util.MAX_AMOUNT) {
            errorMessages.add("Amount is out of range 0 ${Util.MAX_AMOUNT}")
        }

        if (errorMessages.isNotEmpty()) {
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

        if (!Util.validateUser(username)) {
            errorMessages.add("Username does not exists.")
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

    @Error
    fun handleJsonSyntaxError(request: HttpRequest<*>, e: JsonParseException): MutableHttpResponse<out Any>? {
        //handles errors in json syntax
        val errorMap = mutableMapOf<String, ArrayList<String>>()
        val error = JsonError("Invalid JSON: ${e.message}")
        errorMap["error"] = arrayListOf<String>("Invalid JSON: ${e.message}")
        return HttpResponse.badRequest(errorMap)
    }

    //for handling missing fields in json input
    @Error
    fun handleBadRequest(request: HttpRequest<*>, e: Any): MutableHttpResponse<ConversionErrorException>? {
        val errorList = arrayOf(e)
        println(errorList.size)
        for (error in errorList)
            println(error)
//        e.message?.let { errorList.add(it) }
        return HttpResponse.status<ConversionErrorException>(HttpStatus.BAD_REQUEST, "add missing properties")
    }
}