package com.example

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/")
class userController {
    @Get()
    fun sample():String{
        return "Hello world"
    }
}