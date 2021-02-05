package com.e4all.main;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.*;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

public class MainController implements Initializable{


    Scheduler newScheduler;
    int days = 1;
    GlobalVariables globalVariables = new GlobalVariables();
    Double[] cumulativeSupplyPrices = new Double[10];
    Double[] cumulativeSupplyKWh = new Double[10];
    Double[] points = new Double[10];
    int[] batteryChargeCount = new int[10];
    ObservableList[] achievements = new ObservableList[10];
    ObservableList[] products = new ObservableList[10];
    int[] xp = new int[10];
    int[] ranks = new int[10];
    String[] rankLabels = new String[]{"Beginner", "Amateur", "Handelaar", "Gevorderde", "Koning"};
    int[] rankMilestones = new int[]{100, 200, 500, 1000};
    HashMap<Integer, HashMap<String, String>> currentMap = new HashMap<>();

    Boolean autoSelected = false;
    Boolean randomSelected = false;
    DecimalFormat decimalFormatter = new DecimalFormat("0.000");
    XYChart.Series demandSeries;
    XYChart.Series supplySeries;

    XYChart.Series consumptionSeries;
    XYChart.Series productionSeries;

    XYChart.Series surplusSeries;
    public static ObservableList<SingleTransaction> transactionObservableList = FXCollections.observableArrayList();
    public static ObservableList<Product> productObservableList = FXCollections.observableArrayList();
    Image moonImage = new Image("com/e4all/main/Resources/Assets/moon.jpeg");
    Image sunImage = new Image("com/e4all/main/Resources/Assets/sun.jpg");

    @FXML
    public ImageView imageView1, imageView2, imageView3, imageView4, imageView5, imageView6, imageView7, imageView8, imageView9, imageView10, EV1, EV2, EV3, EV4, EV5, EV6, EV7, EV8, EV9, EV10, sunIndicator;

    @FXML
    public Button pauseButton, payButton;

    @FXML
    public RadioButton autoSettings, randomSettings, exportButton;

    @FXML
    public CheckBox EVButton, solarButton, batteryButton;

    @FXML
    public ToggleGroup settings;

    @FXML
    public ComboBox houseChoice, residentChoice, seasonChoice, daysChoice, houseChoice11;

    @FXML
    public Label totDemand, totSupply, timeLabel, EVLabel, residentLabel, solarLabel, currentSurplus, totalSurplus, batteryLabel;
    public Label battery1, battery2, battery3, battery4, battery5, battery6, battery7, battery8, battery9, battery10;

    @FXML
    public Label currentDay;

    @FXML
    public Label experiencePoints;

    @FXML
    public ListView<Label> productsBoughtList;

    @FXML
    public Label currentRank;

    @FXML
    public Circle indicator1, indicator2, indicator3, indicator4, indicator5, indicator6, indicator7, indicator8, indicator9, indicator10;

    @FXML
    public Rectangle solar1, solar2, solar3, solar4, solar5, solar6, solar7, solar8, solar9, solar10;

    @FXML
    public MenuItem e4all;

    @FXML
    public VBox mainVBOX, transactionVBOX;

    @FXML
    public HBox infoHBOX;

    @FXML
    public ComboBox houseChoice1;

    @FXML
    public Label amountOfPoints, amountOfPoints1, amountOfRevenue, amountOfSoldEnergy, warningLabel;

    @FXML
    public AnchorPane transactionPane, achievementPane;

    @FXML
    private LineChart<String, Number> lineChart, prodConsLineChart, surplusLineChart;

    @FXML
    private TableView<SingleTransaction> transactionTable;

    @FXML
    private TableColumn timeColumn, buyerColumn, sellerColumn, amountColumn, priceColumn;

    @FXML
    private TableView<Product> shopItemsTable;

    @FXML
    private TableColumn productColumn, productCostColumn;

    @FXML
    private TableView<Achievement> achievementList;

    @FXML
    private TableColumn achievementNameColumn, achievementDescriptionColumn, achievementLevelColumn;

    public void addXP(int houseNumber, int level){
        switch(level){
            case 1:
                xp[houseNumber] += 5;
                break;
            case 2:
                xp[houseNumber] += 10;
                break;
            case 3:
                xp[houseNumber] += 20;
                break;
            case 4:
                xp[houseNumber] += 50;
                break;
            case 5:
                xp[houseNumber] += 100;
                break;
            case 6:
                xp[houseNumber] += 150;
                break;
            case 7:
                xp[houseNumber] += 200;
                break;
            default:
                // do nothing
                break;
        }
    }


    public int getDays(){
        return this.daysChoice.getSelectionModel().getSelectedIndex() + 1;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public void setKWh(){
        for(int i = 0; i < 10; i++){
            double total = 0.0;
            for(SingleTransaction transaction : transactionTable.getItems()){
                if(transaction.getSupplierID() == i+1){
                    total += transaction.getAmount();
                }
                this.cumulativeSupplyKWh[i] = total;
                Achievement trader = (Achievement) achievements[i].get(0);
                int[] traderMilestones = trader.getMilestones();
                if (trader.getLevel() < traderMilestones.length) {
                    if(total >= traderMilestones[trader.getLevel()]){
                        trader.setLevel(trader.getLevel() + 1);
                        if (trader.getLevel() < traderMilestones.length) {
                            trader.setDescription("Verhandel " + traderMilestones[trader.getLevel()] + " kWh aan energie.");
                            addXP(i, trader.getLevel());
                        }
                        else{
                            trader.setDescription("Hoogste niveau bereikt.");
                        }
                        points[i]++;
                        updateAchievementRank();
                    }
                }
            }
        }
    }

    public void fillPointsArray(){
        for(int i = 0; i < 10; i++){
            if(points[i] == null){
                points[i] = 0.0;
            }
        }
    }

    public void updateAchievementRank(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                achievementList.getColumns().get(2).setVisible(false);
                achievementList.getColumns().get(2).setVisible(true);
            }
        });
    }

    public void setPrices(){
        for(int i = 0; i < 10; i++){
            double total = 0.0;
            for(SingleTransaction transaction : transactionTable.getItems()){
                if(transaction.getSupplierID() == i + 1){
                    total += transaction.getPrice();
                }
                this.cumulativeSupplyPrices[i] = total;
                Achievement trader2 = (Achievement) achievements[i].get(2);
                int[] traderMilestones = trader2.getMilestones();
                if(trader2.getLevel() < traderMilestones.length){
                    if(total >= traderMilestones[trader2.getLevel()]){
                        trader2.setLevel(trader2.getLevel() + 1);
                        if(trader2.getLevel() < traderMilestones.length){
                            trader2.setDescription("Maak €" + traderMilestones[trader2.getLevel()] + " omzet.");
                            addXP(i, trader2.getLevel());
                        }
                        else{
                            trader2.setDescription("Hoogste niveau bereikt.");
                        }
                        points[i]++;
                        updateAchievementRank();
                    }
                }
            }
        }
    }

    public Double[] getPoints(){
        return points;
    }

    public Double[] getCumulativePrices(){
        return cumulativeSupplyPrices;
    }

    public Double[] getCumulativeKWh(){
        return cumulativeSupplyKWh;
    }

    public void startProgram(){
        newScheduler = new Scheduler(currentMap);
        newScheduler.run();
        setDisabled(true);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Controllers.setMainController(this);
        setButtonSettings();

        demandSeries = new XYChart.Series();
        supplySeries = new XYChart.Series();
        demandSeries.setName("Vraag");
        supplySeries.setName("Aanbod");

        consumptionSeries = new XYChart.Series();
        productionSeries = new XYChart.Series();
        consumptionSeries.setName("Gebruik");
        productionSeries.setName("Productie");

        surplusSeries = new XYChart.Series();
        surplusSeries.setName("Surplus");

        houseChoice1.getSelectionModel().selectFirst();
        houseChoice11.getSelectionModel().selectFirst();
        daysChoice.getSelectionModel().selectFirst();
        transactionTable.setItems(getTransaction());
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("timeslot"));
        buyerColumn.setCellValueFactory(new PropertyValueFactory<>("buyerID"));
        sellerColumn.setCellValueFactory(new PropertyValueFactory<>("supplierID"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        transactionTable.setPlaceholder(new Label("Deze tabel schijnt leeg te zijn."));
        lineChart.getData().addAll(supplySeries,demandSeries);
        lineChart.setCreateSymbols(false);
        prodConsLineChart.getData().addAll(productionSeries,consumptionSeries);
        prodConsLineChart.setCreateSymbols(false);

        days = getDays();

        surplusLineChart.getData().addAll(surplusSeries);
        surplusLineChart.setCreateSymbols(false);

        transactionTable.prefWidthProperty().bind(transactionPane.widthProperty());
        transactionTable.prefHeightProperty().bind(transactionPane.heightProperty());

        productObservableList.add(new Product("Wegwerpbestek-set", 1));
        productObservableList.add(new Product("Neleman bio-wijn", 2));
        productObservableList.add(new Product("15% korting op de volgende energierekening", 3));
        productObservableList.add(new Product("30% korting op de volgende energierekening", 5));
        productObservableList.add(new Product("Kartonnen afvalscheidingsprullenbakken", 6));
        productObservableList.add(new Product("50% korting op de volgende energierekening", 8));
        productObservableList.add(new Product("Bamboe ondergoed", 8));
        productObservableList.add(new Product("Fairphone 3", 15));
        productObservableList.add(new Product("Extra zonnepaneel", 20));

        shopItemsTable.setItems(productObservableList);
        productColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        productCostColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));
        shopItemsTable.setPlaceholder(new Label("Heel misschien zijn er geen items toegevoegd."));

        for(int i=0; i<10; i++){
            ObservableList<Achievement> achievementsPerHouse = FXCollections.observableArrayList();
            achievementsPerHouse.add(new Achievement(0, "Energiehandelaar", "Verhandel 1 kWh aan energie.", new int[]{1, 2, 5, 10, 20, 50, 100, 150}));
            achievementsPerHouse.add(new Achievement(0, "Oplader", "Laad de batterij 1 keer op.", new int[]{1, 2, 5, 10, 20, 50, 100, 150}));
            achievementsPerHouse.add(new Achievement(0, "Verkoper", "Maak €1 omzet.", new int[]{1, 2, 3, 5, 10, 20, 50, 100, 150}));
            achievements[i] = achievementsPerHouse;
            ObservableList<Label> productsPerHouse = FXCollections.observableArrayList();
            products[i] = productsPerHouse;
        }
        Arrays.fill(xp, 0);
        Arrays.fill(ranks, 0);
        int selectedHouse = houseChoice1.getSelectionModel().getSelectedIndex();
        achievementList.setItems(achievements[selectedHouse]);
        achievementNameColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        achievementDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        achievementLevelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        achievementList.setPlaceholder(new Label("Heel misschien zijn er geen achievements toegevoegd."));
    }

    public void setRanks(){
        for(int i = 0; i < 10; i++){
            int rank = ranks[i];
            if (rank < 4) {
                if(xp[i] >= rankMilestones[rank]){
                    ranks[i]++;
                }
            }
        }
    }

    public void setRankLabels(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                int selectedHouse = houseChoice1.getSelectionModel().getSelectedIndex();
                int rank = ranks[selectedHouse];
                switch(rank){
                    case 0:
                        currentRank.setText("Beginner");
                        break;
                    case 1:
                        currentRank.setText("Amateur");
                        break;
                    case 2:
                        currentRank.setText("Handelaar");
                        break;
                    case 3:
                        currentRank.setText("Gevorderde");
                        break;
                    default:
                        currentRank.setText("Koning");
                        break;
                }
            }
        });
    }

    public void setRankProgressBar(){

    }



    @FXML
    public void setTooltip(String text, int houseNumber){
        ImageView IVToUse;
        Map numberObjectMap = new HashMap();
        numberObjectMap.put(1, imageView1);
        numberObjectMap.put(2, imageView2);
        numberObjectMap.put(3, imageView3);
        numberObjectMap.put(4, imageView4);
        numberObjectMap.put(5, imageView5);
        numberObjectMap.put(6, imageView6);
        numberObjectMap.put(7, imageView7);
        numberObjectMap.put(8, imageView8);
        numberObjectMap.put(9, imageView9);
        numberObjectMap.put(10, imageView10);
        IVToUse = (ImageView) numberObjectMap.get(houseNumber);
        Tooltip tooltip = new Tooltip(text);
        Tooltip.install(IVToUse, tooltip);
    }

    @FXML
    public void pause() {
        Scheduler.pause = true;
    }

    @FXML
    public void setCurrentDay(int current){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                currentDay.setText(String.valueOf(current + 1));
            }
        });
    }

    @FXML
    public void startButton() {
        try {
            if (Scheduler.hasBeenStarted.equals(false)) {
                startProgram();
            } else {
                Scheduler.startThread();
            }
        }

        catch (RuntimeException e){
            if (seasonChoice.getSelectionModel().getSelectedItem() == null){
                AlertBox.displayRegular("Oops", "Stel in het seizoen in voor u de simulatie start.");
            }
            else if (settings.getSelectedToggle() == null){
                AlertBox.displayRegular("Oops", "Stel de straat in voor u de simulatie start.");
            }
            else {
                AlertBox.displayRegular("Oops", "De simulatie is klaar.");
            }
        }

    }

    @FXML
    public void setTimeLabel(String text){
        Platform.runLater(() -> timeLabel.setText(text));

    }
    @FXML
    public void setLabelsInAchievements(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                int selectedHouse = houseChoice1.getSelectionModel().getSelectedIndex();
                if(cumulativeSupplyKWh.length > 0){
                    amountOfSoldEnergy.setText(new DecimalFormat("#.##").format(cumulativeSupplyKWh[selectedHouse]));
                }
                if(cumulativeSupplyPrices.length > 0){
                    amountOfRevenue.setText(new DecimalFormat("#.##").format(cumulativeSupplyPrices[selectedHouse]));
                }
                if(xp.length > 0){
                    experiencePoints.setText(String.valueOf(xp[selectedHouse]));
                }
            }
        });
    }

    @FXML
    public void setItemsInAchievements(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                int selectedHouse = houseChoice1.getSelectionModel().getSelectedIndex();
                achievementList.setItems(achievements[selectedHouse]);
            }
        });
    }

    @FXML
    public void setLabelInShop(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                int selectedHouse = houseChoice11.getSelectionModel().getSelectedIndex();
                if(points.length > 0){
                    amountOfPoints1.setText(String.valueOf(points[selectedHouse]));
                }
            }
        });
    }

    @FXML
    public void setPoints(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                int selectedHouse = houseChoice1.getSelectionModel().getSelectedIndex();
                amountOfPoints.setText(String.valueOf(Math.floor(points[selectedHouse])));
            }
        });
    }

    public void loadSettings(){
        int selectedHouse = houseChoice.getSelectionModel().getSelectedIndex() + 1;
        HashMap<String, String> houseSpecs = currentMap.get(selectedHouse);

        if (houseSpecs.get("car") == "true"){
            EVButton.setSelected(true);
        }
        else {
            EVButton.setSelected(false);
        }

        if (houseSpecs.get("solar") == "true"){
            solarButton.setSelected(true);
        }
        else {
            solarButton.setSelected(false);
        }

        if (houseSpecs.get("battery") == "true"){
            batteryButton.setSelected(true);
        }
        else {
            batteryButton.setSelected(false);
        }

        residentChoice.setValue(houseSpecs.get("residents"));
    }

    public void setButtonSettings(){
        autoSettings.setOnAction(e -> {
            autoSelected=true;
            randomSelected=false;
            setDisabled(false);
            currentMap = globalVariables.getMap();
            try {
                loadSettings();
            }
            catch (NullPointerException npe){
                System.out.println("no house selected");
            }
        });
        randomSettings.setOnAction(e -> {
            autoSelected=false;
            randomSelected=true;
            setDisabled(false);
            globalVariables.fillRandomMap();
            currentMap = globalVariables.randomMap;
            try {

                loadSettings();
            }
            catch (NullPointerException npe){
                System.out.println("no house selected");
            }
        });

        setDisabled(true);

        houseChoice.setOnAction(e -> {
            loadSettings();
        });

        residentChoice.setOnAction(e -> {
            Integer houseNumber = houseChoice.getSelectionModel().getSelectedIndex() + 1;
            String newValue = (String) residentChoice.getSelectionModel().getSelectedItem();
            HashMap<String, String> temp = currentMap.get(houseNumber);

            try {
                temp.put("residents", newValue);
                currentMap.put(houseNumber, temp);
            }
            catch (NullPointerException n){
                AlertBox.displayRegular("Oops...", "Kies een huisnummer alvorens u het aantal bewoners aanpast.");
            }
        });

        EVButton.setOnAction(e -> {
            Integer houseNumber = houseChoice.getSelectionModel().getSelectedIndex() + 1;
            Boolean newValue = EVButton.isSelected();
            HashMap<String, String> temp = currentMap.get(houseNumber);


            if (newValue.equals(true)){
                try {
                    temp.put("car", "true");
                }
                catch (NullPointerException n){
                    AlertBox.displayRegular("Oops...", "Kies een huisnummer alvorens u instellingen aanpast.");
                }
            }
            else if (newValue.equals(false)){
                try {
                    temp.put("car", "false");
                }
                catch (NullPointerException n){
                    AlertBox.displayRegular("Oops...", "Kies een huisnummer alvorens u instellingen aanpast.");
                }
            }
            currentMap.put(houseNumber, temp);
        });

        solarButton.setOnAction(e -> {
            Integer houseNumber = houseChoice.getSelectionModel().getSelectedIndex() + 1;
            Boolean newValue = solarButton.isSelected();
            HashMap<String, String> temp = currentMap.get(houseNumber);


            if (newValue.equals(true)){
                try {
                    temp.put("solar", "true");
                }
                catch (NullPointerException n){
                    AlertBox.displayRegular("Oops...", "Kies een huisnummer alvorens u instellingen aanpast.");
                }
            }
            else if (newValue.equals(false)){
                try {
                    temp.put("solar", "false");
                }
                catch (NullPointerException n){
                    AlertBox.displayRegular("Oops...", "Kies een huisnummer alvorens u instellingen aanpast.");
                }
            }
            currentMap.put(houseNumber, temp);
        });

        batteryButton.setOnAction(e -> {
            Integer houseNumber = houseChoice.getSelectionModel().getSelectedIndex() + 1;
            Boolean newValue = batteryButton.isSelected();
            HashMap<String, String> temp = currentMap.get(houseNumber);


            if (newValue.equals(true)){
                try {
                    temp.put("battery", "true");
                }
                catch (NullPointerException n){
                    AlertBox.displayRegular("Oops...", "Kies een huisnummer alvorens u instellingen aanpast.");
                }
            }
            else if (newValue.equals(false)){
                try {
                    temp.put("battery", "false");
                }
                catch (NullPointerException n){
                    AlertBox.displayRegular("Oops...", "Kies een huisnummer alvorens u instellingen aanpast.");
                }
            }
            currentMap.put(houseNumber, temp);
        });

        exportButton.setOnAction(e ->{
            Scheduler.export = exportButton.isSelected();
        });
    }

    public void setDisabled(boolean b){
        houseChoice.setDisable(b);
        residentChoice.setDisable(b);
        solarButton.setDisable(b);
        batteryButton.setDisable(b);
        EVButton.setDisable(b);

        residentLabel.setDisable(b);
        EVLabel.setDisable(b);
        solarLabel.setDisable(b);
        batteryLabel.setDisable(b);
    }

    public void setDotColor(Integer houseNumber, Double surplus) {
        Map<Integer,Circle> circleMap = new HashMap<Integer, Circle>();

        circleMap.put(1, indicator1);
        circleMap.put(2, indicator2);
        circleMap.put(3, indicator3);
        circleMap.put(4, indicator4);
        circleMap.put(5, indicator5);
        circleMap.put(6, indicator6);
        circleMap.put(7, indicator7);
        circleMap.put(8, indicator8);
        circleMap.put(9, indicator9);
        circleMap.put(10, indicator10);

        Circle localCircle = circleMap.get(houseNumber);

        if (surplus < 0){
            //red
            localCircle.setFill(doRadialGradient(Color.web("#D8311C")));
        }
        else if(surplus > 0){
            //green
            localCircle.setFill(doRadialGradient(Color.web("#44C767")));
//            localCircle.setFill(doRadialGradient(Color.web("#7DA71B")));
        }
        else if(surplus == 0){
            //yellow
            localCircle.setFill(doRadialGradient(Color.web("#FFDA32")));
        }
    }

    private RadialGradient doRadialGradient(Color color){
        RadialGradient radialGradient = new RadialGradient(
                0,
                0,
                .5,
                .5,
                .5,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, color), new Stop(1.0, Color.web("#f4f4f4")));
        return radialGradient;

    }

    public void showStakeholders() throws IOException {
        AlertBox.displayStakeholders();
    }

    public void setSeason(){
        String selectedSeason = seasonChoice.getSelectionModel().getSelectedItem().toString();
        Scheduler.season = selectedSeason;
    }

    @FXML
    public void setTotalDemand(Double demand){
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                totDemand.setText(decimalFormatter.format(demand) + " kWh");
            }
        });
    }

    @FXML
    public void setTotalSupply(Double supply){
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                totSupply.setText(decimalFormatter.format(supply) + " kWh");
            }
        });
    }

    @FXML
    public void setCurrentSurplus(Double surplus){
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                currentSurplus.setText(decimalFormatter.format(surplus) + " kWh");
            }
        });
    }

    @FXML
    public void setTotalSurplus(Double surplus){
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                totalSurplus.setText(decimalFormatter.format(surplus) + " kWh");
            }
        });
    }

    @FXML
    public void setTotalSoldEnergy(Double amount){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                amountOfSoldEnergy.setText(decimalFormatter.format(amount));
            }
        });
    }

    public void setSolarPanel(Boolean solar, Integer houseNumber){
        Map<Integer,Rectangle> rectangleMap = new HashMap<Integer, Rectangle>();

        rectangleMap.put(1, solar1);
        rectangleMap.put(2, solar2);
        rectangleMap.put(3, solar3);
        rectangleMap.put(4, solar4);
        rectangleMap.put(5, solar5);
        rectangleMap.put(6, solar6);
        rectangleMap.put(7, solar7);
        rectangleMap.put(8, solar8);
        rectangleMap.put(9, solar9);
        rectangleMap.put(10, solar10);

        Rectangle localRectangle = rectangleMap.get(houseNumber);

        if (solar){
            localRectangle.setFill(Color.DODGERBLUE);
            localRectangle.setStyle("-fx-border-color:  #545454");
        }
        else {
            localRectangle.setFill(Color.TRANSPARENT);
            localRectangle.setStyle("-fx-border-color:  transparent");
        }
    }

    public void setEV(Boolean ev, Integer houseNumber){
        Map<Integer,ImageView> EVMap = new HashMap<Integer, ImageView>();

        EVMap.put(1, EV1);
        EVMap.put(2, EV2);
        EVMap.put(3, EV3);
        EVMap.put(4, EV4);
        EVMap.put(5, EV5);
        EVMap.put(6, EV6);
        EVMap.put(7, EV7);
        EVMap.put(8, EV8);
        EVMap.put(9, EV9);
        EVMap.put(10, EV10);

        ImageView EVImage = EVMap.get(houseNumber);

        if (ev){
            EVImage.setVisible(true);
        }
        else {
            EVImage.setVisible(false);
        }
    }

    @FXML
    public void addToSeries(Boolean type, String time, Double value){
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                //1 = demand    0 = supply
                if (type){
                    demandSeries.getData().add(new XYChart.Data(time, value));
                }
                else{
                    supplySeries.getData().add(new XYChart.Data(time, value));
                }
            }
        });

    }

    @FXML
    public void addToFirstSeries(Boolean type, String time, Double value){
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                //1 = demand    0 = supply
                if (type){
                    consumptionSeries.getData().add(new XYChart.Data(time, value));
                }
                else{
                    productionSeries.getData().add(new XYChart.Data(time, value));
                }
            }
        });

    }

    @FXML
    public void addToSurplusSeries(String time, Double value){
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                surplusSeries.getData().add(new XYChart.Data(time, value));
            }
        });

    }

    public void setBatteryPercentage(Integer houseNumber, Double percentage, Boolean battery) {
        Map<Integer,Label> batteryMap = new HashMap<Integer, Label>();

        batteryMap.put(1, battery1);
        batteryMap.put(2, battery2);
        batteryMap.put(3, battery3);
        batteryMap.put(4, battery4);
        batteryMap.put(5, battery5);
        batteryMap.put(6, battery6);
        batteryMap.put(7, battery7);
        batteryMap.put(8, battery8);
        batteryMap.put(9, battery9);
        batteryMap.put(10, battery10);

        Label localBattery = batteryMap.get(houseNumber);
        Achievement achievement = (Achievement) achievements[houseNumber - 1].get(1);

        if(percentage < 100){
            achievement.setAchieved(false);
        }
        if(percentage >= 100){
            batteryChargeCount[houseNumber - 1]++;
            int currentChargeCount = batteryChargeCount[houseNumber - 1];
            int[] milestones = achievement.getMilestones();
            if(achievement.getLevel() < milestones.length){
                if(currentChargeCount >= milestones[achievement.getLevel()] && !achievement.getAchieved()) {
                    achievement.setLevel(achievement.getLevel() + 1);
                    if(achievement.getLevel() < milestones.length){
                        achievement.setDescription("Laad de batterij " + milestones[achievement.getLevel()] + " keer op.");
                        if(ranks[houseNumber - 1] < 4){
                            addXP(houseNumber - 1, achievement.getLevel());
                        }
                    }
                    else{
                        achievement.setDescription("Hoogste niveau bereikt.");
                    }
                    achievement.setAchieved(true);
                    points[houseNumber - 1]++;
                }
            }
        }

        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                if (battery){
                    localBattery.setText(percentage.intValue()+"%");
                }
                else {
                    localBattery.setText("");
                }
            }
        });

    }

    public ObservableList<SingleTransaction> getTransaction(){
        return transactionObservableList;
    }

    @FXML
    public void setMoon(){
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                sunIndicator.setImage(moonImage);
                infoHBOX.setStyle("-fx-padding: 10;" +
                        "-fx-border-style: solid inside;" +
                        "-fx-border-width: 9;" +
                        "-fx-border-insets: 0;" +
                        "-fx-border-radius: 0;" +
                        "-fx-border-color: linear-gradient(to top, transparent, transparent, #8c91aa, #546588, #0a3d66);");
            }
        });
    }

    @FXML
    public void setSun(){
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                sunIndicator.setImage(sunImage);
                infoHBOX.setStyle("-fx-padding: 10;" +
                        "-fx-border-style: solid inside;" +
                        "-fx-border-width: 9;" +
                        "-fx-border-insets: 0;" +
                        "-fx-border-radius: 0;" +
                        "-fx-border-color: linear-gradient(to top, transparent, transparent, #95aac5, #659bbc, #0a8dae);");
            }
        });
    }

    @FXML
    public void setWarningLabel(String text){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                warningLabel.setText(text);
            }
        });
    }

    public void payManually(javafx.event.ActionEvent event) {
        Product product = shopItemsTable.getSelectionModel().getSelectedItem();
        int selectedHouse = houseChoice11.getSelectionModel().getSelectedIndex();
        if(points[selectedHouse] - product.getCost() >= 0){
            points[selectedHouse] -= product.getCost();
            setPoints();
            setLabelInShop();
            setWarningLabel("");
            addToProductsList(selectedHouse, new Label(product.getName()));
        }
        else{
            setWarningLabel("Niet genoeg punten.");
        }
    }

    public void payAutomatically(){
        Random random = new Random();
        int houseNumber = random.nextInt(10);
        boolean affordable = false;
        if(points[houseNumber] > 1){
            while(!affordable){
                Product product = productObservableList.get(random.nextInt(productObservableList.size()));
                if(product.getCost() <= points[houseNumber]){
                    affordable = true;
                    points[houseNumber] -= product.getCost();
                    setPoints();
                    setLabelInShop();
                    addToProductsList(houseNumber, new Label(product.getName()));
                }
            }
        }
    }

    public int[] getXP(){
        return xp;
    }

    public void addToProductsList(int number, Label item){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                products[number].add(item);
            }
        });
    }

    public void setProductObservableList(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                int selectedHouse = houseChoice11.getSelectionModel().getSelectedIndex();
                productsBoughtList.setItems(products[selectedHouse]);
            }
        });
    }
}

