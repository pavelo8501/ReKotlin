package po.auth.authentication.extensions

import po.auth.authentication.Authenticator


fun validateCredentials(login: String, password: String){
    Authenticator.validateCredentials(login, password)
}