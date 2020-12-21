package com.e4all.main;


import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostRequest extends SendRequest {
    private String response;

    public PostRequest(Double surPlus, String current_time_frame, int house_id, int timeslotId) throws Exception {
        if(surPlus < 0){
            double amount = Math.abs(surPlus); //set negative surplus to positive
            urlObject = new URL("http://127.0.0.1:5000/demand");
            response = sendRequest("demand", current_time_frame, house_id, timeslotId, amount);
        }
        else {
            urlObject = new URL("http://127.0.0.1:5000/supply");
            response = sendRequest("supply", current_time_frame, house_id, timeslotId, surPlus);
        }
    }

    public String sendRequest(String request_type, String current_time_frame, int house_id, int timeslotId, double amount) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        JSONObject json;

        if (request_type == "demand"){
            json = new JsonRequestBuilder().build("demand", current_time_frame, house_id, timeslotId, amount);
        }
        else {
            json = new JsonRequestBuilder().build("supply", current_time_frame, house_id, timeslotId, amount);
        }

        String query = json.toString();
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("content-length", Integer.toString(query.length()));
        connection.getOutputStream().write(query.getBytes("UTF8"));

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        //Close reader and connection
        in.close();
        connection.disconnect();    //Moet misschien weg. De socket kan hergebruikt worden als de connection niet wordt afgesloten.

        return response.toString();
    }

    public String getResponse() {
        return response;
    }
}
