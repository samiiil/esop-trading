package controller

import com.fasterxml.jackson.core.JsonParseException
import io.micronaut.core.convert.exceptions.ConversionErrorException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.web.router.exceptions.UnsatisfiedBodyRouteException
import models.*
import services.Validations
import services.saveUser

@Controller("/")
class EndPoints {
    @Post("/user/register")
    fun register(@Body body: RegisterInput): HttpResponse<*> {
        val errorList = arrayListOf<String>()

//        if (errorList.isNotEmpty()) {
//            val response: Map<String, *>
//            response = mapOf("error" to errorList)
//            return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response)
//        }

        if (body.firstName == null) {
            errorList.add("firstName is missing")
        }
        if (body.lastName == null) {
            errorList.add("lastName is missing")
        }
        if (body.phoneNumber == null) {
            errorList.add("phoneNumber is missing")
        }
        if (body.email == null) {
            errorList.add("email is missing")
        }
        if (body.username == null) {
            errorList.add("username is missing")
        }

        val firstName: String? = body.firstName?.trim()
        val lastName: String? = body.lastName?.trim()
        val phoneNumber: String? = body.phoneNumber?.trim()
        val email: String? = body.email?.trim()
        val username: String? = body.username?.trim()

        for (error in Validations.validateFirstName(firstName)) errorList.add(error)
        for (error in Validations.validateLastName(lastName)) errorList.add(error)
        for (error in Validations.validatePhoneNumber(phoneNumber)) errorList.add(error)
        for (error in Validations.validateEmailIds(email)) errorList.add(error)
        for (error in Validations.validateUserName(username)) errorList.add(error)

        if (errorList.isEmpty()) {
            if (username != null && firstName != null && lastName != null && phoneNumber != null && email != null) {
                val newUser = User(username, firstName, lastName, phoneNumber, email)
                saveUser(newUser)
            }
        }

        val response: Map<String, *>
        if (errorList.isNotEmpty()) {
            response = mapOf("error" to errorList)
            return HttpResponse.status<Any>(HttpStatus.BAD_REQUEST).body(response)
        }
        val res = mutableMapOf<String, String>()
        res["firstName"] = firstName!!
        res["lastName"] = lastName!!
        res["phoneNumber"] = phoneNumber!!
        res["email"] = email!!
        res["username"] = username!!
        // response = mapOf("message" to "User created successfully!!")
        return HttpResponse.status<Any>(HttpStatus.OK).body(res)
    }

    @Post("/user/{username}/addToWallet")
    fun addToWallet(username: String, @Body body: AddToWalletInput): HttpResponse<*> {
        val errorMessages: ArrayList<String> = ArrayList()
        //Input Parsing


        val response: Map<String, *>
        if (!Validations.validateUser(username)) {
            errorMessages.add("Username does not exists.")
        }

        val amountToBeAdded: Long? = body.amount?.toLong()

        if (amountToBeAdded == null) {
            errorMessages.add("Amount field is missing")
        }

        if (amountToBeAdded!! <= 0) {
            errorMessages.add("Amount added to wallet has to be positive.")
        }

        if (errorMessages.isNotEmpty()) {
            response = mapOf("error" to errorMessages)
            if (errorMessages[0] == "Username does not exists.")
                return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response)
            return HttpResponse.status<Any>(HttpStatus.BAD_REQUEST).body(response)
        }

        val freeMoney = DataStorage.userList[username]!!.account.wallet.getFreeMoney()
        val lockedMoney = DataStorage.userList[username]!!.account.wallet.getLockedMoney()

        if (((amountToBeAdded + freeMoney + lockedMoney) <= 0) ||
            ((amountToBeAdded + freeMoney + lockedMoney) > DataStorage.MAX_AMOUNT)
        ) {
            errorMessages.add("Amount exceeds maximum wallet limit. Wallet range 0 to ${DataStorage.MAX_AMOUNT}")
        }
        if (errorMessages.isNotEmpty()) {
            response = mapOf("error" to errorMessages)
            return HttpResponse.status<Any>(HttpStatus.BAD_REQUEST).body(response)
        }
        DataStorage.userList[username]!!.account.wallet.addMoneyToWallet(amountToBeAdded)

        response = mapOf("message" to "$amountToBeAdded amount added to account")
        return HttpResponse.status<Any>(HttpStatus.OK).body(response)
    }

    @Post("/user/{username}/addToInventory")
    fun addToInventory(username: String, @Body body: AddToInventoryInput): HttpResponse<*> {

        //Input Parsing
        val quantityToBeAdded = body.quantity?.toLong()
        val typeOfESOP = body.esop_type?.uppercase() ?: "NON-PERFORMANCE"
        val errorMessages: ArrayList<String> = ArrayList()
        val response: Map<String, *>

        if (typeOfESOP !in arrayOf("PERFORMANCE", "NON-PERFORMANCE"))
            errorMessages.add("Invalid ESOP type")

        if (!Validations.validateUser(username))
            errorMessages.add("username does not exists.")
        if (quantityToBeAdded == null) {
            errorMessages.add("Quantity field is missing")

        }
        if (errorMessages.isNotEmpty()) {
            response = mapOf("error" to errorMessages)
            return HttpResponse.status<Any>(HttpStatus.OK).body(response)
        } else if (quantityToBeAdded != null) {
            if (typeOfESOP == "NON-PERFORMANCE") {
                val freeInventory = DataStorage.userList[username]!!.account.inventory.getFreeInventory()
                val lockedInventory = DataStorage.userList[username]!!.account.inventory.getLockedInventory()
                val totalQuantity = freeInventory + lockedInventory + quantityToBeAdded


                if (totalQuantity <= 0 || totalQuantity > DataStorage.MAX_QUANTITY) {
                    errorMessages.add("ESOP quantity out of range. Limit for ESOP quantity is 0 to ${DataStorage.MAX_QUANTITY}")
                }

            } else if (typeOfESOP == "PERFORMANCE") {
                val freePerformanceInventory =
                    DataStorage.userList[username]!!.account.inventory.getFreePerformanceInventory()
                val lockedPerformanceInventory =
                    DataStorage.userList[username]!!.account.inventory.getFreePerformanceInventory()
                val totalQuantity = freePerformanceInventory + lockedPerformanceInventory + quantityToBeAdded


                if (totalQuantity <= 0 || totalQuantity > DataStorage.MAX_QUANTITY) {
                    errorMessages.add("ESOP inventory out of range. Limit for ESOP inventory is 0 to ${DataStorage.MAX_QUANTITY}")
                }

            }
            if (errorMessages.isNotEmpty()) {
                response = mapOf("error" to errorMessages)
                return HttpResponse.badRequest(response)
            }
            DataStorage.userList[username]!!.account.inventory.addEsopToInventory(quantityToBeAdded, typeOfESOP)
        }
        if (errorMessages.size > 0) {
            response = mapOf("error" to errorMessages)
            return HttpResponse.badRequest(response)
        }

        response = mapOf("message" to "$quantityToBeAdded $typeOfESOP ESOPs added to account")
        return HttpResponse.status<Any>(HttpStatus.OK).body(response)
    }

    @Get("/user/{username}/accountInformation")
    fun accountInformation(username: String): HttpResponse<*> {

        val errorMessages: ArrayList<String> = ArrayList()

        val response: Map<String, *>
        if (!Validations.validateUser(username)) {
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
    fun createOrder(username: String, @Body body: CreateOrderInput): HttpResponse<*> {
        val errorMessages: ArrayList<String> = ArrayList()

        val response: Map<String, *>

        if (!Validations.validateUser(username))
            errorMessages.add("username does not exists.")
        if (body.order_type.isNullOrBlank())
            errorMessages.add("order_type is missing, order type should be BUY or SELL")
        if (body.price == null)
            errorMessages.add("price for the order is missing")
        if (body.quantity == null)
            errorMessages.add("quantity field for order is missing")
        if (body.order_type != null && body.order_type == "SELL" && body.esop_type.isNullOrBlank()) {
            errorMessages.add("esop_type is missing, SELL order requires esop_type")
        }
        if (errorMessages.isNotEmpty()) {
            response = mapOf("error" to errorMessages)
            return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response)
        }

        //Input Parsing
        val orderQuantity: Long? = body.quantity?.toLong()
        val orderType: String? = body.order_type?.trim()?.uppercase()
        val orderPrice: Long? = body.price?.toLong()
        val typeOfESOP: String = (body.esop_type ?: "NON-PERFORMANCE").trim().uppercase()

        if (orderType !in arrayOf("BUY", "SELL"))
            errorMessages.add("Invalid order type")
        if (typeOfESOP !in arrayOf("PERFORMANCE", "NON-PERFORMANCE"))
            errorMessages.add("Invalid type of ESOP, ESOP type should be PERFORMANCE or NON-PERFORMANCE")

        if (errorMessages.isEmpty() && orderPrice != null && orderType != null && orderQuantity != null) {
            //Create Order
            val result = DataStorage.userList[username]!!.addOrder(orderQuantity, orderType, orderPrice, typeOfESOP)
            if (result.isNotEmpty())
                errorMessages.addAll(result)
            else {
                val res = mutableMapOf<String, Any>()
                res["quantity"] = orderQuantity
                res["order_type"] = orderType
                res["price"] = orderPrice

                return HttpResponse.status<Any>(HttpStatus.OK).body(res)
            }

        }
        val res = mapOf("error" to errorMessages)
        return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(res)
    }

    @Get("/user/{username}/orderHistory")
    fun orderHistory(username: String): HttpResponse<*> {
        val errorMessages: ArrayList<String> = ArrayList()

        val response: Map<String, *>

        if (!Validations.validateUser(username)) {
            errorMessages.add("username does not exists.")
            response = mapOf("error" to errorMessages)
            return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response)
        }

        if (!Validations.validateUser(username)) {
            errorMessages.add("Username does not exists.")
            response = mapOf("error" to errorMessages)
            return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response)
        }
        response = DataStorage.userList[username]!!.getOrderDetails()
        return HttpResponse.status<Any>(HttpStatus.OK).body(response)
    }

    @Get("/fees")
    fun getFees(): HttpResponse<*> {
        return HttpResponse.status<Any>(HttpStatus.OK)
            .body(mapOf(Pair("TotalFees", DataStorage.TOTAL_FEE_COLLECTED)))
    }

    @Error
    fun handleJsonSyntaxError(request: HttpRequest<*>, e: JsonParseException): MutableHttpResponse<out Any>? {
        //handles errors in json syntax
        val errorMap = mutableMapOf<String, ArrayList<String>>()
        errorMap["error"] = arrayListOf("Invalid JSON: ${e.message}")
        return HttpResponse.badRequest(errorMap)
    }

    //for handling missing fields in json input
    @Error
    fun handleConversionError(request: HttpRequest<*>, e: ConversionErrorException): Any {
        val errorMessages = arrayOf("Add missing fields to the request")
        val response = mapOf("error" to errorMessages)
        return HttpResponse.status<Any>(HttpStatus.BAD_REQUEST).body(response)
    }

    @Error(exception = UnsatisfiedBodyRouteException::class)
    fun handleEmptyBody(
        request: HttpRequest<*>
    ): HttpResponse<Map<String, Array<String>>> {
        return HttpResponse.badRequest(mapOf("error" to arrayOf("Request body is missing")))
    }

    @Error(global = true, status = HttpStatus.NOT_FOUND)
    fun handleInvalidRoute(request: HttpRequest<*>): HttpResponse<ErrorResponse> {
        return HttpResponse.notFound(ErrorResponse(arrayListOf("Invalid URI - ${request.uri}")))
    }

    @Error(global = true, status = HttpStatus.METHOD_NOT_ALLOWED)
    fun handleWrongHttpMethod(request: HttpRequest<*>): HttpResponse<ErrorResponse> {
        val response = ErrorResponse(arrayListOf("${request.method} method is not allowed for ${request.uri}."))
        return HttpResponse.notAllowed<ErrorResponse>().body(response)
    }
}