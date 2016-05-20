/**
 * Created by nuno on 19-05-2016.
 */
import static spark.Spark.*;

import medium.api.MEDIUM_SCOPES;
import medium.api.MediumAPI;
import medium.api.MediumAuthScreenResponse;
import medium.api.MediumToken;

import java.util.List;

import static kotlin.collections.CollectionsKt.listOf;


public class EndPoint {
    public static void main(String[] args) {
        String clientId = "<clientID>";
        String clientSecret = "<clientSecret>";
        //Medium does not accept localhost : you may need to add a random domain and map it on /etc/hosts
        String redirectURI = "http://<host>:4567/mediumResponse";
        List<MEDIUM_SCOPES> medium_scopes = listOf(MEDIUM_SCOPES.BASIC_PROFILE, MEDIUM_SCOPES.BASIC_PROFILE);

        MediumAPI medium = new MediumAPI(clientId, clientSecret, redirectURI, medium_scopes);

        get("/mediumAuth", (req, res) -> {
            res.redirect(medium.getOauthRedirect("randomString"));
            return "";
        });

        get("/mediumResponse", (req, res) ->  {
            MediumAuthScreenResponse authScreenResponse = new MediumAuthScreenResponse(req.queryParams("code"), req.queryParams("state"), req.queryParams("error"));
            MediumToken token = medium.getToken(authScreenResponse);
            return medium.getBasicInfo(token, "me");
        });
        post("/", (req, res) -> {
            System.out.println(req.headers("User-Agent"));
            return "";
        });
    }
}
