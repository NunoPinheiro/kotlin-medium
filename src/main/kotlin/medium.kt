/**
 * Created by nuno on 20-05-2016.
 */

package medium.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kittinunf.fuel.httpPost
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.httpGet

class MediumAPI(val clientId: String, val clientSecret: String, val redirectURI: String, val scopes : List<MEDIUM_SCOPES>){
    val mapper = ObjectMapper().registerModule(KotlinModule())
    fun getOauthRedirect(state : String) : String? {
        val stringifiedScopes = scopes.map { it -> it.value }.joinToString(",")
        val url = "https://medium.com/m/oauth/authorize?client_id=$clientId&scope=$stringifiedScopes&state=$state&response_type=code&redirect_uri=$redirectURI"
        return url;
    }

    fun getToken(mediumResponse : MediumAuthScreenResponse) : MediumToken {
        if(mediumResponse.error != null)
            throw MediumException(mediumResponse.error);
        val tokenRequest = MediumTokenRequest(mediumResponse.code, clientId , clientSecret, GrantType.AUTH_CODE.value, redirectURI)
        val body = postData(tokenRequest)
        val request = "https://api.medium.com/v1/tokens".httpPost(body)
        //Need to use a different user agent since the one being used was not valid  (Java/1.8.0_65)
        request.httpHeaders.put("User-Agent", "Java")
        val (req, response, result) = request.responseString()
        return mapper.readValue(result.component1(), MediumToken::class.java)
    }

    fun getBasicInfo(token : MediumToken , authorId : String = "me") : String?{
        val request = "https://api.medium.com/v1/$authorId".httpGet()
        setupToken(token, request)
        return request.responseString().third.component1()
    }

    private fun setupToken(token : MediumToken, request : Request){
        request.httpHeaders.put("Authorization", token.token_type + " " + token.access_token)
        request.httpHeaders.put("User-Agent", "Java")
    }
}

enum class MEDIUM_SCOPES(val value : String){
    BASIC_PROFILE("basicProfile"),
    PUBLISH_POST("publishPost")
}

private enum class GrantType(val value : String){
    AUTH_CODE("authorization_code");
}

data class MediumAuthScreenResponse(val code : String, val state : String, val error : String?);
private data class MediumTokenRequest(val code : String, val client_id : String, val client_secret : String, val grant_type : String, val redirect_uri : String);
data class MediumToken(val token_type : String, val access_token : String, val refresh_token : String, val scope : List<String>, val expires_at : String);

class MediumException : Exception{
    constructor(error : String) : super(error)
}

private fun postData(data : MediumTokenRequest) : List<Pair<String, String>>{
    return listOf(
            "code" to data.code,
            "client_id" to data.client_id,
            "client_secret" to data.client_secret,
            "grant_type" to data.grant_type,
            "redirect_uri"  to data.redirect_uri
    );
}
