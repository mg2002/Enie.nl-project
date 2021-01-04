package com.e4all.main;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class TransactionParser {
    private GetRequest getRequest;
    private JSONParser parser;
    private List<SingleTransaction> singleTransactionList;

    TransactionParser() {
        this.parser = new JSONParser();
        this.singleTransactionList = new ArrayList<>();
    }

    //get request on transactionId
    JSONObject getTransaction(String transactionId) {
        try {
            if(transactionId != null) {
                getRequest = new GetRequest("transactions", transactionId);
                return getRequest.sendRequest();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //get transaction data to transactions hashmaps
    void addTransactionDataToList(String timeToSend, String response) {
        String transactionId = getTransactionIdFromResponse(response);

        //check if there is a transaction_id in de response
        if(transactionId != null) {
            JSONObject jsonObject = getTransaction(transactionId);

            //get supply and demand id out of the Transactions
            String supply_id = (String)jsonObject.get("supply_id");
            String demand_id = (String)jsonObject.get("demand_id");
            try {
                Double amount = Double.valueOf(jsonObject.get("amount").toString());
                amount = Math.round(amount*1e2)/1e2;

                //get transaction supply
                getRequest = new GetRequest("supply", supply_id);
                JSONObject supplyResponse = getRequest.sendRequest();

                //get transaction demand
                getRequest = new GetRequest("demand", demand_id);
                JSONObject demandResponse = getRequest.sendRequest();

                Long supplyResidence = (Long) supplyResponse.get("residence_id");
                Long demandResidence = (Long) demandResponse.get("residence_id");

                //add above data to the transaction list
                SingleTransaction singleTransaction = new SingleTransaction(supplyResidence.intValue(), demandResidence.intValue(), amount, timeToSend);
                MainController.transactionObservableList.add(singleTransaction);
                singleTransactionList.add(singleTransaction);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //no transaction
            System.out.println("No transaction_id found for " + response);
        }


    }

    //get transaction id out of the response string
    private String getTransactionIdFromResponse(String response) {
        try {
            JSONObject responseToJson = (JSONObject) parser.parse(response);
            if(responseToJson.containsKey("transaction_id")) {
                return (String) responseToJson.get("transaction_id");
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    List<SingleTransaction> getSingleTransactionList() {
        return singleTransactionList;
    }
}
