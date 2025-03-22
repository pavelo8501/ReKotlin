package po.auth.authentication.authenticator

import po.auth.authentication.interfaces.AuthenticatedPrincipal

class UserAuthenticator(private  val validatorFn: (userName: String, password: String)-> AuthenticatedPrincipal?) {


    fun authenticate(userName: String, password: String): Boolean{
        val result =  this.validatorFn(userName, password)
        return result != null
    }

}