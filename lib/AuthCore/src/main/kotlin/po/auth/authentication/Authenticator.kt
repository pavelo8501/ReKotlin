package po.auth.authentication

object Authenticator {

    fun validateCredentials(login: String, password: String){
        println("Login to validate: $login by password: $password")
    }

}