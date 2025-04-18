package po.auth.authentication.tokens.jwt

import com.auth0.jwk.JwkProvider
import po.auth.authentication.tokens.interfaces.JwtConfigInterface

data class JwtConfig(
    override var claimFieldName:String,
    override var realm: String,
    override var issuer: String,
    override var secret: String,
    override var kid: String?  = null,
    override var audience: String = "jwt-audience",
): JwtConfigInterface {

    val privateKey: String by lazy{ this.keysSet().first }
    val publicKey: String by lazy{ this.keysSet().second }

    private fun keysSet(): Pair<String, String>{
        if(privateKeyValue == null || publicKeyValue == null){
            throw Exception("None of the keys present. Before use call setKeys or setJwkProvider")
        }else{
            return Pair(privateKeyValue!!, publicKeyValue!!)
        }
    }

   private var publicKeyValue: String? = null

    fun setKeys(keyRetrievalFn : () -> Pair<String, String>){
        val keyPair by lazy(keyRetrievalFn)
        this.privateKeyValue  = keyPair.first
        this.publicKeyValue  =  keyPair.second
        keysSet()
    }


    var jwkProvider:JwkProvider? = null
    private  var privateKeyValue: String? = null
    override fun setJwkProvider(provider: JwkProvider, privateKey: String, kid : String){
        jwkProvider = provider
        privateKeyValue = privateKey
        this.kid = kid
        keysSet()
    }
}