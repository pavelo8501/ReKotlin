package po.auth.authentication.jwt.models

import com.auth0.jwk.JwkProvider
import po.auth.authentication.jwt.interfaces.JwtConfigInterface
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

data class JwtConfig(
  //  override var claimFieldName:String,
    override var realm: String,
    override var issuer: String,
    override var secret: String,
    override var kid: String?  = null,
    override var audience: String = "jwt-audience",
    override val privateKey: RSAPrivateKey,
    override val publicKey: RSAPublicKey
): JwtConfigInterface {


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


    var jwkProvider: JwkProvider? = null
    private  var privateKeyValue: String? = null
    override fun setJwkProvider(provider: JwkProvider, privateKey: String, kid : String){
        jwkProvider = provider
        privateKeyValue = privateKey
        this.kid = kid
        keysSet()
    }
}