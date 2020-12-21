package com.e4all.main;

import java.io.File;
import java.io.PrintWriter;

public class ExportFile {
    //TODO: make a export between a startdate and enddate
    private StringBuilder dataBuilder;
    private StringBuilder configBuilder;
    private StringBuilder transactionBuilder;
    private File directory;
    private File dataFile;
    private File configFile;
    private File transactionFile;

    ExportFile() {
        this.dataBuilder = new StringBuilder();
        this.configBuilder = new StringBuilder();
        this.transactionBuilder = new StringBuilder();
        this.dataBuilder.append("Housenumber,time,production,consumption,surplus \n");
        this.configBuilder.append("Housenumber,e_car,s_panels,r_amount\n");
        this.transactionBuilder.append("seller,buyer,amount,time");
        this.directory = new File("C:\\Energy4All");
        this.dataFile = new File("C:\\Energy4All\\data.csv");
        this.configFile = new File("C:\\Energy4All\\config.csv");
        this.transactionFile = new File("C:\\Energy4All\\transactions.csv");
    }

    void append_housedata(int houseNumber, String time, double production, double consumption, double surplus) {
        dataBuilder.append(houseNumber).append(",");
        dataBuilder.append(time).append(",");
        dataBuilder.append(production).append(",");
        dataBuilder.append(consumption).append(",");
        dataBuilder.append(surplus);
        dataBuilder.append("\n");
    }

    void append_config(House house) {
        String houseSchema = String.format("%s,%s,%s,%s\n",house.getHouseNumber(), house.getE_car(), house.getS_panels(), house.getR_amount());
        configBuilder.append(houseSchema);
    }

    void append_transaction(SingleTransaction singleTransaction) {
        int supplyResidence = singleTransaction.getSupplierID();
        int demandResidence = singleTransaction.getBuyerID();
        double amount = singleTransaction.getAmount();
        String timeToSend = singleTransaction.getTimeslot();

        String transactionSchema = String.format("%s,%s,%s,%s\n",supplyResidence, demandResidence, amount, timeToSend);
        transactionBuilder.append(transactionSchema);
    }

    void export() {
        PrintWriter pw;

        if(!directory.exists()) {
            directory.mkdir();
        }

        try {
            pw = new PrintWriter(dataFile);
            pw.write(dataBuilder.toString());
            pw.close();

            pw = new PrintWriter(configFile);
            pw.write(configBuilder.toString());
            pw.close();

            pw = new PrintWriter(transactionFile);
            pw.write(transactionBuilder.toString());
            pw.close();

            System.out.println(dataFile.getName() + " and " + configFile.getName() + " and " + transactionFile.getName() + " saved to directory: " + directory.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void delete_file(File file) {
//        if(file.exists() && !file.isDirectory() && file.isFile()) {
//            file.delete();
//            System.out.println(String.format("File name %s deleted", file.getName()));
//        }
//    }

    public void setFilePath(String path) {
        dataFile = new File(path);
    }

    public String getFilePath() {
        return dataFile.getAbsolutePath();
    }
}
