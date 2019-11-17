/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import errorhandling.ReeQuestException;

import errorhandling.ExceptionDTO;
import java.net.MalformedURLException;
import java.util.ArrayList;

import utils.ReeQuest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import javax.ws.rs.core.MediaType;

/**
 *
 * @author Niels Bang
 */
@Path("poormansgoogle")
public class GoogleResource {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    //ID of the custom search engine to use
    private static final String CX = "003739661964971794239:gvzfdunqtk6";
    private static final String CX2 = "003739661964971794239:gdyspwwocdq";

    //API Key
    private static final String KEY = "AIzaSyBXm8bLJjCtmNHDZ17xLMzd9rhkpjauPQk";
    private static final String KEY2 = "e1eaeeeb0f804cbca10c195c61881545";

    private final String URL = "https://www.googleapis.com";
    private final String PATH = "customsearch/v1";

    private final String URL2 = "https://api-eur.cognitive.microsofttranslator.com";
    private final String PATH2 = "translate";

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String demo() {
        return "{\"msg\":\"Poormansgoogle API\"}";
    }

    @Path("/{query}")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String search(@PathParam("query") String query) throws Exception {
        List<String> jsons = new ArrayList();
        ExecutorService executor = Executors.newWorkStealingPool();
        Callable<String> usedSearch = () -> {
            try {
                ReeQuest req = new ReeQuest("hvad fuck bruger du source til martin??", URL);
                Map<String, String> params = new HashMap();
                params.put("cx", CX);
                params.put("key", KEY);
                params.put("q", query);
                return req.getRequest(PATH, params, "", null);
            } catch (ReeQuestException | MalformedURLException e) {
                return e.getMessage();
            }
        };
        Future<String> usedSearchFuture = executor.submit(usedSearch);

        Callable<String> ebaySearch = () -> {
            try {
                ReeQuest req = new ReeQuest("Jeg ved stadig ikke hvad du bruger den her til martin", URL2);
                String body = "[{\"Text\":\"" + query + "\"}]";

                HashMap<String, String> param1 = new HashMap();
                param1.put("api-version", "3");
                param1.put("from", "da");
                param1.put("to", "en");

                HashMap<String, String> headers = new HashMap();
                headers.put("Content-Type", "application/json;charset=UTF-8");
                headers.put("Ocp-Apim-Subscription-Key", KEY2);

                HashMap<String, String> translatedQuery = GSON.fromJson(req.getRequest(PATH2, param1, body, headers), HashMap.class);

                HashMap<String, String> param2 = new HashMap();
                param2.put("cx", CX2);
                param2.put("key", KEY);
                param2.put("q", translatedQuery.get("text"));

                return req.getRequest(PATH2, param2, null, null);
            } catch (ReeQuestException | MalformedURLException e) {
                return e.getMessage();
            }
        };
        Future<String> ebaySearchFuture = executor.submit(ebaySearch);

        executor.shutdown();

        jsons.add(usedSearchFuture.get());
        jsons.add(ebaySearchFuture.get());

        return GSON.toJson(jsons);
    }
}
