package com.e4all.main;

import org.json.simple.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


public class JsonRequestBuilder {
    //post request json for demand only
    private List<String> energyTypes = Arrays.asList("buffer", "generator", "shifter");
    private String time_frame;

    JSONObject build(String request_type, String current_time_frame, int house_id, int timeslotId, double amount) {
        String energyType = energyTypes.get(ThreadLocalRandom.current().nextInt(0, 2 + 1));
        time_frame = current_time_frame;
        String start = createDateTime();
        int duration = ThreadLocalRandom.current().nextInt(5, 250 + 1);
        double price = ThreadLocalRandom.current().nextDouble(0.25, 20.0);


        JSONObject jsonDemandObject = new JSONObject();
        jsonDemandObject.put("timeslot_id", timeslotId);
        jsonDemandObject.put("amount", amount);
        jsonDemandObject.put("energy_type", energyType);
        jsonDemandObject.put("start", start);
        jsonDemandObject.put("residence_id", house_id);
        jsonDemandObject.put("duration", duration);
        if(request_type.equals("supply")) {
            jsonDemandObject.put("price", price);
        }

        return jsonDemandObject;
    }

    private String createDateTime() {
        Date date = Calendar.getInstance().getTime();
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String today_date = formatter.format(date);
        return today_date + " " + time_frame;
    }

}