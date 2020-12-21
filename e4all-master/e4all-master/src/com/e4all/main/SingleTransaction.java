package com.e4all.main;


public class SingleTransaction {
    private Integer supplierID;
    private Integer buyerID;
    private double amount;
    private double price;
    private String time;


    SingleTransaction(Integer supplierID, Integer buyerID, double amount, String time) {
        this.supplierID = supplierID;
        this.buyerID = buyerID;
        this.amount = amount;
        this.time = time;

        this.price = Math.round(amount * GlobalVariables.ENERGYPRICE*1e3)/1e3;
    }

    //Necessary for TableView CellFactory, needs to be public
    public Integer getBuyerID() {
        return buyerID;
    }

    public Integer getSupplierID() {
        return supplierID;
    }

    public Double getAmount() {
        return amount;
    }

    public Double getPrice() { return price; }

    public String getTimeslot() { return time; }


    //Necessary for TableView CellFactory
    public void setBuyerID(Integer buyerID) {
        this.buyerID = buyerID;
    }

    public void setSupplierID(Integer supplierID) {
        this.supplierID = supplierID;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setPrice(Double amount) {
        this.amount = amount;
    }

    public void setTimeslot(String time) {
        this.time = time;
    }



}
