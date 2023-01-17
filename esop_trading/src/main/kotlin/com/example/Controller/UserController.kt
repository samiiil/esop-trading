package com.example.controller

import com.example.Inventory
import com.example.User
import com.example.Wallet
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.json.tree.JsonObject


@Controller("/")

class UserController {

    companion object {
        var users : HashMap<String, User> = HashMap<String, User> ()
        var emails = mutableSetOf<String>()
        var phonenos = mutableSetOf<Number>()
    }

    @Post("/user/register")
    fun register(@Body body:JsonObject):HashMap<String,String>{
        var user = body.toString().trim()
        println(body)
        println(body.get("firstName").stringValue.trim())
        var firstName = body.get("firstName").stringValue.trim()
        var lastName = body.get("lastName").stringValue.trim()
        var phoneNumber = body.get("phoneNumber").numberValue
        var email = body.get("email").stringValue.trim()
        var username = body.get("username").stringValue.trim()

        var error : HashMap<String, String> = HashMap<String, String> ()
        var flg:Int = 0
        if(users.containsKey(username))
        {
            flg += 1
            error.put("msg "+flg,"Username already exist")
        }
        if(emails.contains(email)){
            flg += 1
            error.put("msg "+flg,"User with given mail already exist")
        }
        if(phonenos.contains(phoneNumber)){
            flg += 1
            error.put("msg "+flg,"User with given phone number already exist")
        }
        if(flg==0){
            error.put("msg","User created successfully!!")
            var inventory = Inventory(0,0)
            var wallet = Wallet(0,0)
            var newuser = User(firstName,lastName,phoneNumber,email,username, inventory , wallet )
            users.put(username,newuser)
            emails.add(email)
            phonenos.add(phoneNumber)
        }

        println(emails)

        return error

    }

}