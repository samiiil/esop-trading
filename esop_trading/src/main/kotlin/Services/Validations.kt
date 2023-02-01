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
            if(emailId == null){
                return errorList
            }

            if (DataStorage.registeredEmails.contains(emailId)) {
                errorList.add("Email already exists")
            }
            if(emailId.elementAt(0)=='.' || emailId.elementAt(0)=='-' || emailId.elementAt(emailId.length-1)=='.' || emailId.elementAt(emailId.length-1)=='-'){
                errorList.add("Invalid Email address")
            }
            val dots: String = ".."
            if( emailId.contains(dots)){
                errorList.add("Invalid Email address")
            }
            if (!emailId.matches(Regex("^[\\\\a-zA-Z0-9.!#\$%&'*+/=?^_`{|}\" ~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)+\$"))) {
                errorList.add("Invalid Email address")
            }
            else{
                val splitMail=emailId.split('@')
                val domain = splitMail[1]
                if(!validDomain(domain)){
                    errorList.add("Invalid Email address")
                }
                if(splitMail[0].elementAt(splitMail[0].length-1)=='.'){
                    errorList.add("Invalid Email address")
                }
                if(splitMail[0].length>64){
                    errorList.add("User name of email must be less than 64 characters long")
                }
                if(splitMail[1].length>255){
                    errorList.add("Domain name must be less than 255 characters long")
                }
                val subdomain=splitMail[1].split('.')
                if(subdomain[1].length<=1){
                    errorList.add("Subdomain should be more than 1 characters long ")
                }
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

        fun validDomain(domain: String):Boolean{
            val labels = domain.split('.')
            return labels.all{
                validLabel(it)
            }
        }

        fun validLabel(label: String):Boolean {
            val ldhStrRegex ="[a-zA-Z0-9-]+"
            val labelRegex = "[a-zA-Z]$ldhStrRegex[a-zA-Z0-9]"
            return label.matches(Regex(labelRegex))
        }
    }
}