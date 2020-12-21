package com.e4all.main;

import java.util.HashMap;
import java.util.Random;

public class GlobalVariables {
        public static HashMap<Integer, HashMap<String, String>> defaultMap = new HashMap<>();
        public static HashMap<Integer, HashMap<String, String>> randomMap = new HashMap<>();
        public static final long SCHEDULER_INTERVAL = 150;
        public static final Double ENERGYPRICE = 0.23;
        public static final Double DEFAULT_BATTERY_CAPACITY = 13.5;
        public static final Double BATTERY_CHARGINGRATE = 5.0;
        public static final Double TOP_LIMIT = -1.0;
        public static final Double BOTTOM_LIMIT = 0.5;

        GlobalVariables(){

                HashMap<String, String> map1 = new HashMap<>();
                HashMap<String, String> map2 = new HashMap<>();
                HashMap<String, String> map3 = new HashMap<>();
                HashMap<String, String> map4 = new HashMap<>();
                HashMap<String, String> map5 = new HashMap<>();
                HashMap<String, String> map6 = new HashMap<>();
                HashMap<String, String> map7 = new HashMap<>();
                HashMap<String, String> map8 = new HashMap<>();
                HashMap<String, String> map9 = new HashMap<>();
                HashMap<String, String> map10 = new HashMap<>();

                map1.put("residents", "4");
                map1.put("solar", "true");
                map1.put("pump", "false");
                map1.put("car", "true");
                map1.put("battery", "true");

                map2.put("residents", "5");
                map2.put("solar", "false");
                map2.put("pump", "false");
                map2.put("car", "false");
                map2.put("battery", "false");

                map3.put("residents", "3");
                map3.put("solar", "false");
                map3.put("pump", "false");
                map3.put("car", "false");
                map3.put("battery", "false");

                map4.put("residents", "3");
                map4.put("solar", "false");
                map4.put("pump", "true");
                map4.put("car", "false");
                map4.put("battery", "false");

                map5.put("residents", "4");
                map5.put("solar", "true");
                map5.put("pump", "false");
                map5.put("car", "true");
                map5.put("battery", "true");

                map6.put("residents", "5");
                map6.put("solar", "true");
                map6.put("pump", "false");
                map6.put("car", "false");
                map6.put("battery", "true");

                map7.put("residents", "4");
                map7.put("solar", "false");
                map7.put("pump", "false");
                map7.put("car", "false");
                map7.put("battery", "false");

                map8.put("residents", "3");
                map8.put("solar", "false");
                map8.put("pump", "false");
                map8.put("car", "false");
                map8.put("battery", "false");

                map9.put("residents", "4");
                map9.put("solar", "false");
                map9.put("pump", "false");
                map9.put("car", "false");
                map9.put("battery", "false");

                map10.put("residents", "4");
                map10.put("solar", "false");
                map10.put("pump", "true");
                map10.put("car", "false");
                map10.put("battery", "false");

                defaultMap.put(1, map1);
                defaultMap.put(2, map2);
                defaultMap.put(3, map3);
                defaultMap.put(4, map4);
                defaultMap.put(5, map5);
                defaultMap.put(6, map6);
                defaultMap.put(7, map7);
                defaultMap.put(8, map8);
                defaultMap.put(9, map9);
                defaultMap.put(10, map10);
        }

        public static HashMap<Integer, HashMap<String, String>> getMap(){
                return defaultMap;
        }

        public static void fillRandomMap(){
                Random rand = new Random();

                for (int i =1 ; i < 11 ; i++) {
                        HashMap<String, String> hm = new HashMap<>();
                        hm.put("residents", Integer.toString(rand.nextInt(5) + 1));
                        hm.put("solar", Boolean.toString(rand.nextBoolean()));
                        hm.put("pump", Boolean.toString(rand.nextBoolean()));
                        hm.put("car", Boolean.toString(rand.nextBoolean()));
                        hm.put("battery", Boolean.toString(rand.nextBoolean()));

                        randomMap.put(i , hm);
                }
        }


}
