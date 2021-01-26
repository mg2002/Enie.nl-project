package com.e4all.main;
import util.StringArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

class Scheduler {

    private static StringArray stringArray;
    private static TransactionParser transactions = new TransactionParser();
    private static ExportFile exportFile;
    static Double totalDemand = 0.0;
    static Double totalSupply = 0.0;
    static Double currentSurplus = 0.0;
    static Double totalSurplus = 0.0;

    static Double cycleConsumption = 0.0;
    static Double cycleProduction = 0.0;
    static Double cycleDemand = 0.0;
    static Double cycleSupply = 0.0;
    static Double cycleSurplus = 0.0;
    static Double cycleProdConsSurplus = 0.0;

    static Double previousCycleSurplus = 0.0;
    static Double previousProdConsSurplus = 0.0;

    static Integer batteryAmount = 0;

    static String season;

    private static Thread thread;
    static Boolean hasBeenStarted = false;
    static Boolean pause = false;
    static Boolean export = false;

    private HashMap<Integer, HashMap<String, String>> currentMap;

    private static ArrayList<House> houses = new ArrayList<>();

    Scheduler(HashMap<Integer, HashMap<String, String>> mapParameter){
        this.currentMap = mapParameter;
    }

    void run() {
        // run in a second
        final long timeInterval = GlobalVariables.SCHEDULER_INTERVAL;
        stringArray = new StringArray();
        exportFile = new ExportFile();
        makeHouses();


        //build config export file and make battery amount
        for (House item : houses){
            exportFile.append_config(item);

            if (item.hasBattery()){
                batteryAmount += 1;
            }
        }

        Runnable runnable = () -> {
            //Set visualisations in the front end
            for (House item : houses){
                item.setPanelsAndEV();
            }

            int currentNumber = 0;
            int timeslotId = 0;
            int current_day = 0;
            hasBeenStarted = true;
            Controllers.getMainController().setCurrentDay(current_day);

            while (true) {
                try {
                    String timeToSend = getTime(currentNumber);
                    String end = stringArray.getLastTime();
//                    String end = "23:45";
                    System.out.println("EINDE: " + end);
                    if(timeToSend.equals(end)) {
                        current_day++;
                        currentNumber = 0;
                        Controllers.getMainController().setCurrentDay(current_day);
                        if(current_day == Controllers.getMainController().getDays()) {
                            if (export.equals(true)) {
                                transactions.getSingleTransactionList().forEach(transaction -> exportFile.append_transaction(transaction));
                                exportFile.export();
                                System.out.println("Exported to a external files, system will now exit.");
                            }
                            System.out.println("No export. System will now exit.");
                            System.out.println("Total prices of households: ");
                            Double[] priceArray = Controllers.getMainController().getCumulativePrices();
                            for (int i = 0; i < priceArray.length; i++) {
                                System.out.println("Household " + (i + 1) + ": " + priceArray[i]);
                            }
                            System.out.println("Total amounts of households: ");
                            Double[] amountArray = Controllers.getMainController().getCumulativeKWh();
                            for (int i = 0; i < amountArray.length; i++) {
                                System.out.println("Household " + (i + 1) + ": " + amountArray[i]);
                            }
                            System.out.println("Total points: ");
                            Double[] pointsArray = Controllers.getMainController().getPoints();
                            for (int i = 0; i < pointsArray.length; i++) {
                                System.out.println("Household " + (i + 1) + ": " + pointsArray[i]);
                            }
                            Scheduler.pause = true;
                            //System.exit(0);
                        }
                    }

                    for (House item : houses){
                        item.SendPostRequest(timeToSend, timeslotId);
                    }

                    timeslotId++;

                    for (House item : houses){
                        if (item.getHouse_response().contains("transaction_id")) {
                            transactions.addTransactionDataToList(timeToSend, item.getHouse_response());
                        }
                    }

                    for (House item : houses){
                        exportFile.append_housedata(item.getHouseNumber(), timeToSend, item.getProduction(), item.getConsumption(), item.getCurrentSurplus());
                    }

                    if (totalSupply > 0){
                        Controllers.getMainController().setSun();
                    }
                    else {
                        Controllers.getMainController().setMoon();
                    }
                    Arrays.fill(Controllers.getMainController().cumulativeSupplyKWh, 0.0);
                    Arrays.fill(Controllers.getMainController().cumulativeSupplyPrices, 0.0);
                    Controllers.getMainController().fillPointsArray();
                    Controllers.getMainController().setTimeLabel(timeToSend);
                    Controllers.getMainController().setTotalDemand(totalDemand);
                    Controllers.getMainController().setTotalSupply(totalSupply);
                    Controllers.getMainController().setCurrentSurplus(currentSurplus);
                    Controllers.getMainController().setTotalSurplus(totalSurplus);
                    Controllers.getMainController().setKWh();
                    Controllers.getMainController().setPrices();
                    Controllers.getMainController().setPoints();
                    Controllers.getMainController().setLabelInShop();
                    Controllers.getMainController().setLabelsInAchievements();
                    Controllers.getMainController().addToSurplusSeries(timeToSend, currentSurplus);
                    Controllers.getMainController().setDays(Controllers.getMainController().getDays());

                    Controllers.getMainController().addToSeries(false, timeToSend, cycleSupply);
                    Controllers.getMainController().addToSeries(true, timeToSend, cycleDemand);

                    Controllers.getMainController().addToFirstSeries(false, timeToSend, cycleProduction);
                    Controllers.getMainController().addToFirstSeries(true, timeToSend, cycleConsumption);

                    previousCycleSurplus = cycleSurplus;
                    previousProdConsSurplus = cycleProdConsSurplus;
                    currentSurplus = 0.0;
                    cycleDemand = 0.0;
                    cycleSupply = 0.0;
                    cycleProduction = 0.0;
                    cycleConsumption = 0.0;
                    cycleProdConsSurplus = 0.0;

                    Thread.sleep(timeInterval);

                    if (pause.equals(true)){
                        synchronized (thread){thread.wait();}
                    }
                    else {
                        synchronized (thread){thread.notify();}
                    }

                    currentNumber++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread = new Thread(runnable);
        thread.start();
    }

    private static String getTime(int i){
        return stringArray.getString(i);
    }


    static synchronized void startThread() {
        pause = false;
        synchronized (thread) {
            thread.notify();
        }
    }


    private void makeHouses(){
        //Make final houses for the simulation
        houses.add(new House(1, Boolean.valueOf(currentMap.get(1).get("car")), Boolean.valueOf(currentMap.get(1).get("solar")), Boolean.valueOf(currentMap.get(1).get("pump")), Integer.valueOf(currentMap.get(1).get("residents")), Boolean.valueOf(currentMap.get(1).get("battery"))));
        houses.add(new House(2, Boolean.valueOf(currentMap.get(2).get("car")), Boolean.valueOf(currentMap.get(2).get("solar")), Boolean.valueOf(currentMap.get(2).get("pump")),  Integer.valueOf(currentMap.get(2).get("residents")), Boolean.valueOf(currentMap.get(2).get("battery"))));
        houses.add(new House(3, Boolean.valueOf(currentMap.get(3).get("car")), Boolean.valueOf(currentMap.get(3).get("solar")), Boolean.valueOf(currentMap.get(3).get("pump")),  Integer.valueOf(currentMap.get(3).get("residents")), Boolean.valueOf(currentMap.get(3).get("battery"))));
        houses.add(new House(4, Boolean.valueOf(currentMap.get(4).get("car")), Boolean.valueOf(currentMap.get(4).get("solar")), Boolean.valueOf(currentMap.get(4).get("pump")),  Integer.valueOf(currentMap.get(4).get("residents")), Boolean.valueOf(currentMap.get(4).get("battery"))));
        houses.add(new House(5, Boolean.valueOf(currentMap.get(5).get("car")), Boolean.valueOf(currentMap.get(5).get("solar")), Boolean.valueOf(currentMap.get(5).get("pump")),  Integer.valueOf(currentMap.get(5).get("residents")), Boolean.valueOf(currentMap.get(5).get("battery"))));
        houses.add(new House(6, Boolean.valueOf(currentMap.get(6).get("car")), Boolean.valueOf(currentMap.get(6).get("solar")), Boolean.valueOf(currentMap.get(6).get("pump")),  Integer.valueOf(currentMap.get(6).get("residents")), Boolean.valueOf(currentMap.get(6).get("battery"))));
        houses.add(new House(7, Boolean.valueOf(currentMap.get(7).get("car")), Boolean.valueOf(currentMap.get(7).get("solar")), Boolean.valueOf(currentMap.get(7).get("pump")),  Integer.valueOf(currentMap.get(7).get("residents")), Boolean.valueOf(currentMap.get(7).get("battery"))));
        houses.add(new House(8, Boolean.valueOf(currentMap.get(8).get("car")), Boolean.valueOf(currentMap.get(8).get("solar")), Boolean.valueOf(currentMap.get(8).get("pump")),  Integer.valueOf(currentMap.get(8).get("residents")), Boolean.valueOf(currentMap.get(8).get("battery"))));
        houses.add(new House(9, Boolean.valueOf(currentMap.get(9).get("car")), Boolean.valueOf(currentMap.get(9).get("solar")), Boolean.valueOf(currentMap.get(9).get("pump")),  Integer.valueOf(currentMap.get(9).get("residents")), Boolean.valueOf(currentMap.get(9).get("battery"))));
        houses.add(new House(10, Boolean.valueOf(currentMap.get(10).get("car")), Boolean.valueOf(currentMap.get(10).get("solar")), Boolean.valueOf(currentMap.get(10).get("pump")),  Integer.valueOf(currentMap.get(10).get("residents")), Boolean.valueOf(currentMap.get(10).get("battery"))));
    }
}
