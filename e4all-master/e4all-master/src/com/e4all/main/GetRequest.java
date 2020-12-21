package com.e4all.main;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class GetRequest extends SendRequest {
    private URL urlObject;
    private JSONParser parser;

    GetRequest(String getType, String id) throws Exception{
        urlObject = new URL(String.format("http://127.0.0.1:5000/%s/%s", getType, id));
        parser = new JSONParser();
        sendRequest();
    }

    JSONObject sendRequest() throws Exception {

        connection = (HttpURLConnection) urlObject.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");

        //Create reader
        in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));

        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        //Close reader and connection
        in.close();
        connection.disconnect();    //Moet misschien weg. De socket kan hergebruikt worden als de connection niet wordt afgesloten.

        return (JSONObject) parser.parse(response.toString());
    }
}