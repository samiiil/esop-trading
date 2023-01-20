package Controller

import Models.Data
import Services.Util
import io.micronaut.http.HttpStatus

import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.json.tree.JsonObject
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import TOTAL_FEE_COLLECTED
@Controller("/")
class EndPoints {

    @Post("/user/register")
    fun register(@Body body: JsonObject): HttpResponse<*> {

        //Input Parsing
        var firstName = body.get("firstName").stringValue.trim()
        var lastName = body.get("lastName").stringValue.trim()
        var phoneNumber = body.get("phoneNumber").stringValue.trim()
        var email = body.get("email").stringValue.trim()
        var username = body.get("username").stringValue.trim()


        var errorMessages: ArrayList<String> = ArrayList<String>();

        if (Util.validateUser(username)) {
            errorMessages.add("Username already exists.");
        }
        if (Util.validateEmailIds(email)) {
            errorMessages.add("Email Id already exists.");
        }
        if (Util.validatePhoneNumber(phoneNumber)) {
            errorMessages.add("Phone Number already exists.")
        }
        if (errorMessages.size == 0) {
            Util.createUser(username, firstName, lastName, phoneNumber, email);
        }

        val response: Map<String, *>;
        if (errorMessages.size > 0) {
            response = mapOf("error" to errorMessages);
            return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response);
        } else {
            response = mapOf("message" to "User created successfully!!");
            return HttpResponse.status<Any>(HttpStatus.OK).body(response);
        }
    }

    @Post("/user/{user_name}/addToWallet")
    fun addToWallet(user_name: String, @Body body: JsonObject): HttpResponse<*> {

        //Input Parsing
        val amountToBeAdded = body.get("amount").bigIntegerValue.toLong()

        var errorMessages: ArrayList<String> = ArrayList<String>();

        val response: Map<String, *>;
        if (Util.validateUser(user_name) == false) {
            errorMessages.add("Username does not exists.");
            response = mapOf("error" to errorMessages);
            return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response);
        }

        Data.userList.get(user_name)!!.account.wallet.addMoneyToWallet(amountToBeAdded);
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

        if(Util.validateUser(user_name) == false){
            errorMessages.add("Username does not exists.");
            response= mapOf("error" to errorMessages);
            return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(response);
        }

        response = Data.userList.get(user_name)!!.getOrderDetails();
        return HttpResponse.status<Any>(HttpStatus.OK).body(response);
    }
    @Get("/fees")
    fun getFees (): HttpResponse<*>{

        return HttpResponse.status<Any>(HttpStatus.OK).body(mapOf(Pair("TotalFees",TOTAL_FEE_COLLECTED)))


        }
}