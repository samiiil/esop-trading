package Controller

import Models.Data
import Services.Util
import io.micronaut.http.HttpStatus

import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.json.tree.JsonObject
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.validation.Validated
import java.lang.Exception
import io.micronaut.http.annotation.Error

@Validated
@Controller("/")
class EndPoints {

    @Post("/user/register")
    fun register(@Body body: JsonObject): HttpResponse<*> {
        val errorList = arrayListOf<String>()
        //Input Parsing
        for(error in Util.validateBody(body)){
            errorList.add(error)

        }
        if(errorList.isNotEmpty()){
            val response: Map<String, *>;
            response = mapOf("error" to errorList);
            return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response);
        }

        val firstName: String = body.get("firstName")?.stringValue?.trim().toString()
        val lastName: String = body.get("lastName")?.stringValue?.trim().toString()
        val phoneNumber: String = body.get("phoneNumber")?.stringValue?.trim().toString()
        val email: String = body.get("email")?.stringValue?.trim().toString()
        val username:String = body.get("username")?.stringValue?.trim().toString()

        if(firstName.isEmpty())
            println("first name is null")
        println(username)

        var errorMessages: ArrayList<String> = ArrayList<String>();

        for(error in Util.validateNames(firstName, "firstName")) errorList.add(error)
        for(error in Util.validateNames(firstName, "lastName")) errorList.add(error)
        for(error in Util.validateNames(firstName, "username")) errorList.add(error)

        for(error in Util.validateEmailIds(email)) errorList.add(error)
        for(error in Util.validatePhoneNumber(phoneNumber, errorList)) errorList.add(error)

        if (errorList.isEmpty()) {
            Util.createUser(username, firstName, lastName, phoneNumber, email);
        }

        val response: Map<String, *>;
        return if (errorList.isNotEmpty()) {
            response = mapOf("error" to errorList);
            HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response);
        } else {
            response = mapOf("message" to "User created successfully!!");
            HttpResponse.status<Any>(HttpStatus.OK).body(response);
        }
    }

    @Post("/user/{user_name}/addToWallet")
    fun addToWallet(user_name: String, @Body body: JsonObject): HttpResponse<*> {
        var errorMessages: ArrayList<String> = ArrayList<String>();
        //Input Parsing
        var amountToBeAdded: Long = 0
        try {
            amountToBeAdded = body.get("amount").bigIntegerValue.toLong()
        }catch (e: Exception){
            errorMessages.add("Amount value is not integer")
        }


        val response: Map<String, *>;
        if (!Util.validateUser(user_name)) {
            errorMessages.add("Username does not exists.");

        }
        if(amountToBeAdded <= 0 || amountToBeAdded > Util.MAX_AMOUNT){
            errorMessages.add("Amount is out of range 0 ${Util.MAX_AMOUNT}")
        }

        if(errorMessages.isNotEmpty()){
            response = mapOf("error" to errorMessages);
            return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response);
        }

        Data.userList[user_name]!!.account.wallet.addMoneyToWallet(amountToBeAdded);
        response = mapOf("message" to "$amountToBeAdded amount added to account");
        return HttpResponse.status<Any>(HttpStatus.OK).body(response);
    }

    @Post("/user/{user_name}/addToInventory")
    fun addToInventory(user_name: String, @Body body: JsonObject): HttpResponse<*> {

        //Input Parsing
        val quantityToBeAdded = body.get("quantity").bigIntegerValue.toLong()

        var errorMessages: ArrayList<String> = ArrayList<String>();

        val response: Map<String, *>;
        if (Util.validateUser(user_name) == false) {
            errorMessages.add("Username does not exists.");
            response = mapOf("error" to errorMessages);
            return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response);
        }

        Data.userList.get(user_name)!!.account.inventory.addEsopToInventory(quantityToBeAdded);
        response = mapOf("message" to "$quantityToBeAdded ESOPs added to account");
        return HttpResponse.status<Any>(HttpStatus.OK).body(response);
    }
    @Get("/user/{user_name}/accountInformation")
    fun accountInformation(user_name: String) : HttpResponse<*> {

        var errorMessages : ArrayList<String> = ArrayList<String> ();

        val response: Map<String,*>;
        if(Util.validateUser(user_name) == false){
            errorMessages.add("Username does not exists.");
            response= mapOf("error" to errorMessages);
            return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response);
        }

        response = mapOf("FirstName" to Data.userList.get(user_name)!!.firstName,
            "LastName" to Data.userList.get(user_name)!!.lastName,
            "Phone" to Data.userList.get(user_name)!!.phoneNumber,
            "Email" to Data.userList.get(user_name)!!.emailId,
            "Inventory" to mapOf<String,Long>(
                "free" to Data.userList.get(user_name)!!.account.inventory.getFreeInventory(),
                "locked" to Data.userList.get(user_name)!!.account.inventory.getLockedInventory()
            ),
            "Wallet" to mapOf<String,Long>(
                "free" to Data.userList.get(user_name)!!.account.wallet.getFreeMoney(),
                "locked" to Data.userList.get(user_name)!!.account.wallet.getLockedMoney()
            ));
        return HttpResponse.status<Any>(HttpStatus.OK).body(response);
    }

    @Post("/user/{user_name}/createOrder")
    fun createOrder(user_name: String, @Body body:JsonObject) : HttpResponse<*>{

        var errorMessages : ArrayList<String> = ArrayList<String> ();

        val response: Map<String,*>;

        if(Util.validateUser(user_name) == false){
            errorMessages.add("Username does not exists.");
            response= mapOf("error" to errorMessages);
            return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response);
        }

        //Input Parsing
        val orderQuantity: Long = body.get("quantity").bigIntegerValue.toLong();
        val orderType: String = body.get("type").stringValue.trim();
        val orderAmount: Long = body.get("price").bigIntegerValue.toLong();

        //Create Order
        val result = Data.userList.get(user_name)!!.addOrder(orderQuantity,orderType,orderAmount);

        if (result != "Order Placed Successfully."){
            errorMessages.add(result);
            response= mapOf("error" to errorMessages);
            return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response);
        }

        response = mapOf("message" to result);
        return HttpResponse.status<Any>(HttpStatus.OK).body(response);
    }

    @Get("/user/{user_name}/orderHistory")
    fun orderHistory(user_name: String) : HttpResponse<*>{

        var errorMessages : ArrayList<String> = ArrayList<String> ();

        val response: Map<String,*>;

        if(!Util.validateUser(user_name)){
            errorMessages.add("Username does not exists.");
            response= mapOf("error" to errorMessages);
            return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response);
        }

        response = Data.userList.get(user_name)!!.getOrderDetails();
        return HttpResponse.status<Any>(HttpStatus.OK).body(response);
    }

    @Error
    fun handleError(): MutableHttpResponse<Any> {
        val response: Map<String,*>;
        return HttpResponse.serverError<Any>("Wrong url")
    }


}