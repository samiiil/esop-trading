package services

import io.micronaut.json.tree.JsonObject
import models.DataStorage

class Validations {
    companion object{
        fun validateBody(body: JsonObject): ArrayList<String> {
            val errorList = arrayListOf<String>()

            try {
                val firstName: Boolean = body.get("firstName").isNull
                println(firstName)
            } catch (e: Exception) {
                errorList.add("firstname is null")
            }

            try {
                val firstName: Boolean = body.get("username").isNull
                println(firstName)
            } catch (e: Exception) {
                errorList.add("username is null")
            }

            try {
                val firstName: Boolean = body.get("lastName").isNull
                println(firstName)
            } catch (e: Exception) {
                errorList.add("lastname is null")
            }

            try {
                val firstName: Boolean = body.get("email").isNull
                println(firstName)
            } catch (e: Exception) {
                errorList.add("email is null")
            }

            try {
                val firstName: Boolean = body.get("phoneNumber").isNull
                println(firstName)
            } catch (e: Exception) {
                errorList.add("phoneNumber is null")
            }
            return errorList
        }

        fun validateFirstName(name: String?): ArrayList<String> {
            val errorList = arrayListOf<String>()
            if(name == null){
                return errorList
            }

            if (name!!.length < 3) {
                errorList.add("firstName has to be at least three characters.")
                return errorList
            }
            if (!name.trim().matches(Regex("([A-Za-z]+ ?)+"))) {
                errorList.add("Invalid firstName. firstName should only contain characters and cannot have more than one continuous space.")
            }
            return errorList
        }

        fun validateLastName(name: String?): ArrayList<String> {
            val errorList = arrayListOf<String>()
            if(name == null)
                return errorList
            if (name.isEmpty()) {
                errorList.add("Last name has to be at least one character.")
            }
            if (!name.matches(Regex("([A-Za-z]+ ?)+"))) {
                errorList.add("Invalid lastName. lastName should only contain characters and cannot have more than one continuous space.")
            }

            return errorList
        }

        fun validateUserName(username: String?): ArrayList<String> {
            val errorList = arrayListOf<String>()
            if(username == null){
                return errorList
            }
            if (DataStorage.userList.contains(username)) {
                errorList.add("Username already taken")
            }
            if (username.length < 3) {
                errorList.add("Username has to be at least three characters.")
            }
            if (!username.matches(Regex("[_\\d]*[A-Za-z][\\w_]*"))) {
                errorList.add("Username can only contain characters,numbers and underscores and must have at least one character.")
            }
            return errorList

        }

        fun validateUser(userName: String): Boolean {
            if (DataStorage.userList.containsKey(userName)) {
                return true
            }
            return false
        }

        fun validateEmailIds(emailId: String?): Collection<String> {
            val errorList = mutableSetOf<String>()
            if(emailId.isNullOrBlank()){
                return errorList
            }
            if (DataStorage.registeredEmails.contains(emailId)) {
                errorList.add("Email already exists")
            }

            val hyphens: String = "--"
            val dots: String = ".."
            if(emailId.contains(hyphens) || emailId.contains(dots)){
                errorList.add("Invalid Email address")
            }
            if (!emailId.matches(Regex("^[\\\\a-zA-Z0-9.!#\$%&'*+/=?^_`{|}\" ~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)+\$"))) {
                errorList.add("Invalid Email address")
            }

            return errorList.toList()
        }

        fun validatePhoneNumber(phoneNumber: String?, errorList: ArrayList<String>): ArrayList<String> {
            val errorList = arrayListOf<String>()
            if(phoneNumber == null){
                return errorList
            }
            if (DataStorage.registeredPhoneNumbers.contains(phoneNumber)) {
                errorList.add("Phone number already exists")
            }
            if (phoneNumber.length < 10) {
                errorList.add("Invalid phone number. Accepted phoneNumber formats: 10 digits, +{two digit country code} 10 digits, {one/two digit country code} 10 digits")
            }
            if (phoneNumber.length == 13) {
                if (!phoneNumber.substring(0, 3).matches(Regex("\\+?\\d\\d")) && phoneNumber.substring(3)
                        .matches(Regex("\\d*"))
                )
                    errorList.add("Invalid phone number. Accepted phoneNumber formats: 10 digits, +{two digit country code} 10 digits, {one/two digit country code} 10 digits")
            } else if (phoneNumber.length == 12) {
                if (phoneNumber[0] == '+') {
                    if (!phoneNumber.substring(1).matches(Regex("\\d*"))) {
                        errorList.add("Invalid phone number. Accepted phoneNumber formats: 10 digits, +{two digit country code} 10 digits, {one/two digit country code} 10 digits")
                    }
                } else {
                    if (!phoneNumber.matches(Regex("\\d*"))) {
                        errorList.add("Invalid phone number. Accepted phoneNumber formats: 10 digits, +{two digit country code} 10 digits, {one/two digit country code} 10 digits")
                    }
                }
            } else if (phoneNumber.length == 11 || phoneNumber.length == 10) {
                if (!phoneNumber.matches(Regex("\\d*"))) {
                    errorList.add("Invalid phone number. Accepted phoneNumber formats: 10 digits, +{two digit country code} 10 digits, {one/two digit country code} 10 digits")
                }
            }
            return errorList
        }
    }
}