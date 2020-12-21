package com.e4all.main;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import util.StringArray;

import java.io.FileReader;
import java.util.Random;

class House {
    private JSONParser parser = new JSONParser();
    private String houseType;
    private int houseNumber;
    private double consumption;
    private double production;
    private double supply;

    private static double currentSurplus;
    private String time;

    private boolean e_car;
    private boolean s_panels;
    private boolean h_pump;

    private boolean battery;
    private Double total_capacity = GlobalVariables.DEFAULT_BATTERY_CAPACITY;
    private Double current_capacity = 6.75;

    private int r_amount;

    private String house_response;
    private JSONObject consumptionJsonObject = new JSONObject();
    private JSONObject productionJsonObject = new JSONObject();

    House(int houseNo, boolean electric_Car, boolean solar_panels, boolean heat_pump, int resident_amount, boolean battery) {
        houseNumber = houseNo;
        this.e_car = electric_Car;
        this.s_panels = solar_panels;
        this.h_pump = heat_pump;
        this.r_amount = resident_amount;
        this.battery = battery;
        houseType = pickH1H1();
        setPanelsAndEV();

    }


    void SendPostRequest(String currentTime, int timeslotId) throws Exception {
        Double surplus = getSurplus(currentTime);

        PostRequest postRequest = new PostRequest(surplus , currentTime, this.houseNumber, timeslotId);
        house_response = postRequest.getResponse();


        Controllers.getMainController().setTooltip(
                "Vraag: " + Math.round(consumption*1e3)/1e3 + "\n" +
                "Aanbod: " + Math.round(production*1e3)/1e3,
                houseNumber
        );


    }

    private void setConsumptionJSONObject(String fileName) {
        //Volledige file
        try {
            Object obj = parser.parse(new FileReader(fileName));
            consumptionJsonObject =  (JSONObject) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setProductionJSONObject(String fileName) {
        //Volledige file
        try {
            Object obj = parser.parse(new FileReader(fileName));
            productionJsonObject =  (JSONObject) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Double getSurplus(String time){
        double surplus = 0;
        try {
            //Volledige consFile
            String consFile = pickConsumptionPattern();
            setConsumptionJSONObject(consFile);
            this.time = time;

            //JSONObject met tijd
            JSONObject consJsonByTime =  (JSONObject) consumptionJsonObject.get(time);
            production = calculateProduction();
            supply = production;
            consumption = (Double) consJsonByTime.get(houseType);
            consumption = countCarUsage(consumption);
            consumption = countHeatPump(consumption);
            consumption = residentRefactor(consumption);
            Scheduler.cycleConsumption += consumption;

            if (this.battery) {
                consumption = demandAfterBattery(consumption);
            }

            surplus = supply - consumption;

            Scheduler.totalSurplus += surplus;
            Scheduler.totalDemand += consumption;
            Scheduler.cycleDemand += consumption;
            Scheduler.cycleProduction += production;
            Scheduler.totalSupply += supply;
            Scheduler.cycleSupply += supply;
            Scheduler.cycleSurplus = Scheduler.cycleSupply - Scheduler.cycleDemand;
            Scheduler.cycleProdConsSurplus = Scheduler.cycleProduction- Scheduler.cycleConsumption;
//            Double tempValue =  (double) Math.round(Scheduler.cycleSurplus * 100) / 100;
            Scheduler.currentSurplus += surplus;

            Controllers.getMainController().setDotColor(this.houseNumber, surplus);
            currentSurplus = surplus;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return surplus;
    }

    int getHouseNumber() {
        return houseNumber;
    }

    boolean getE_car() {
        return e_car;
    }

    boolean getS_panels() {
        return s_panels;
    }

    int getR_amount() {
        return r_amount;
    }

    public boolean hasBattery() {
        return battery;
    }

    Double getProduction() {
        return production;
    }

    Double getConsumption() {
        return consumption;
    }

    String getHouse_response() { return house_response; }

    private String pickConsumptionPattern() {
        switch (Scheduler.season) {
            case "Winter":
                return "src/resources/cons-winter.json";
            case "Lente":
                return "src/resources/cons-lente.json";
            case "Zomer":
                return "src/resources/cons-zomer.json";
            case "Herfst":
                return "src/resources/cons-herfst.json";
        }
        return null;
    }

    private String pickProductionPattern() {
        switch (Scheduler.season) {
            case "Winter":
                return "src/resources/prod-winter.json";
            case "Lente":
                return "src/resources/prod-lente.json";
            case "Zomer":
                return "src/resources/prod-zomer.json";
            case "Herfst":
                return "src/resources/prod-herfst.json";
        }
        return null;
    }

    private String pickH1H1(){
        Random rand = new Random();

        int n = rand.nextInt(2) + 1;

        if (n==1){
            return "h1";
        }
        else{
            return "h2";
        }

    }

    private Double residentRefactor(Double consumption){
        //Calculate consumption by using factors for amount of residents
        switch (r_amount) {
            case 1:
                return consumption * 0.61;
            case 2:
                return consumption * 1;
            case 3:
                return consumption * 1.036;
            case 4:
                return consumption * 1.056;
            case 5:
                return consumption * 1.097;
        }
        return null;
    }

    private Double countCarUsage(Double cons){
        if (e_car){
            for(String str: StringArray.EVStringArray) {
                if(str.trim().contains(time)){
                    return cons + 0.725;
                }
            }
        }

        return cons;
    }

    private Double countHeatPump(Double cons){
        if (h_pump){
            return cons + 0.10779;
        }

        return cons;
    }

    private Double calculateProduction(){
        if (this.s_panels) {

            String prodFile = pickProductionPattern();
            setProductionJSONObject(prodFile);
            JSONObject prodJsonByTime = (JSONObject) productionJsonObject.get(time);
            Object production = prodJsonByTime.get("opbrengst");

            if (production instanceof Long) {
                return ((Long) production).doubleValue();
            } else if (production instanceof Double) {

                //returnvalue is Watt / 1000 / 4  ======  van W naar kW naar kW per kwartier
                return ((Double) production / 1000 / 4);
            }
            return null;
        }
        else{
            return 0.0;
        }
    }

    void setPanelsAndEV(){
        Controllers.getMainController().setSolarPanel(this.s_panels, this.houseNumber);
        Controllers.getMainController().setEV(this.e_car, this.houseNumber);
    }

    Double getCurrentSurplus() {
        return currentSurplus;
    }

    private Double demandAfterBattery(Double consumption){
        Double demand = consumption;

        System.out.println("Previous cyclesurplus" + Scheduler.previousCycleSurplus);
        if (Scheduler.previousProdConsSurplus > GlobalVariables.BOTTOM_LIMIT) {
            System.out.println("De vorige cycleSurplus (" +Scheduler.previousCycleSurplus + ") is groter dan de bottom limit.");

            //Als de zon schijnt en de accu is niet vol. De accu kan NIET in een kwartier geladen worden.
            if (current_capacity < total_capacity - 1.25) {
                //Demand verhogen en huidige capaciteit verhogen
                Double temp = (Scheduler.previousCycleSurplus + (GlobalVariables.BOTTOM_LIMIT )) / Scheduler.batteryAmount;
                System.out.println(temp);
                demand += temp;
                current_capacity += temp;
                current_capacity += production;
                supply = production - temp;
                Double percentage = current_capacity / total_capacity * 100;

                Controllers.getMainController().setBatteryPercentage(houseNumber, percentage, battery);
                return demand;
            }
            //Als de zon schijnt en de accu is niet vol. De accu kan WEL in een kwartier geladen worden.
            else {
                Double leftOver = total_capacity - current_capacity;
                demand += leftOver;
                current_capacity += leftOver;

                Double percentage = current_capacity / total_capacity * 100;
                Controllers.getMainController().setBatteryPercentage(houseNumber, percentage, battery);

                return demand;
            }
        }

        //Als 's nachts
        else {
            //Als de accu voldoende vol is om het gebruik te voorzien
            if (demand < current_capacity) {
                Double tempDemand = demand;
                demand = 0.0;
                current_capacity -= tempDemand;

                Double percentage = current_capacity / total_capacity * 100;
                Controllers.getMainController().setBatteryPercentage(houseNumber, percentage, battery);

                return demand;
            } else {
                demand -= current_capacity;
                current_capacity = 0.0;

                Double percentage = current_capacity / total_capacity * 100;
                Controllers.getMainController().setBatteryPercentage(houseNumber, percentage, battery);

                return demand;
            }
        }
    }
}
