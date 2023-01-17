package com.example.Controller


import com.example.Controller.UserController.Companion.users
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import java.io.Serializable

@Controller("/")
class AccountInformation {
    @Get("/user/{username}/accountinfo")
    fun accountInformation(username:String): Map<*,*> {
        var response : Map<*,*>
        if (users.containsKey(username)) {
            var userinfo = users.get(username)

            print(userinfo)
            response = mapOf(
                "FirstName" to userinfo!!.firstName.toString(),
                "LastName" to userinfo!!.lastName.toString(),
                "Phone" to userinfo!!.phoneNumber.toString(),
                "Email" to userinfo!!.email.toString(),
                "Inventory" to mapOf<String,String>(
                    "free" to userinfo!!.inventory.free.toString(),
                    "locked" to userinfo!!.inventory.locked.toString()
                ),
                "Wallet" to mapOf<String,String>(
                    "free" to userinfo!!.wallet.free.toString(),
                    "locked" to userinfo!!.wallet.locked.toString()
                )
            )
            return response
        }
        return mapOf("message" to "Invalid user!")
    }
}


