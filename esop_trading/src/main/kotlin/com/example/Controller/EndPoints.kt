package com.example.Controller

import Util
import Util.*;
import io.micronaut.http.HttpStatus

import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.json.tree.JsonObject
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

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
}