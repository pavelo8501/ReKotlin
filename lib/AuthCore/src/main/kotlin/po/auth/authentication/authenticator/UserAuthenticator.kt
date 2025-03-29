package po.auth.authentication.authenticator

import po.auth.authentication.interfaces.AuthenticationPrincipal
import po.auth.sessions.classes.SessionFactory
import po.auth.sessions.models.AuthorizedPrincipal
import po.auth.sessions.models.AuthorizedSession

class UserAuthenticator(
    private  val validatorFn: (userName: String, password: String)-> AuthenticationPrincipal?,
    private var  principalBuilder: () ->  AuthorizedPrincipal,
    private val factory : SessionFactory
) {

    fun  builder (principal : AuthenticationPrincipal) : AuthorizedSession{
       return factory.createSession(principalBuilder())
    }

    fun authenticate(userName: String, password: String): AuthenticationPrincipal?{
        return this.validatorFn(userName, password)
    }

    fun setBuilderFn(fn: () ->  AuthorizedPrincipal){
        principalBuilder =  fn
    }


    fun authenticateAndConstruct(userName: String, password: String) : AuthorizedSession?{
        authenticate(userName, password)?.let {
          return  builder(it)
        }?:run { return  null }
    }

}