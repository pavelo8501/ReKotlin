package po.auth.authentication

import okio.Path

object Authenticator {

    var keyBasePath:  Path? = null

    fun validateCredentials(login: String, password: String){
        println("Login to validate: $login by password: $password")
    }

}