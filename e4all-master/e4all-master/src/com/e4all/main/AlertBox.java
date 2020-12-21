package com.e4all.main;

import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;

import java.io.IOException;

public class AlertBox {

    public static void displayRegular(String title, String message) {
        Stage window = new Stage();

        //Block events to other windows
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setMinWidth(500);
        window.setMinHeight(100);
        window.getIcons().add(new Image("com/e4all/main/Resources/Assets/stageIcon.png"));

        //set label
        Label label = new Label();
        label.setText(message);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(label);
        layout.setAlignment(Pos.CENTER);

        //Display window and wait for it to be closed before returning
        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();
    }

    static void displayStakeholders() throws IOException {
        Stage window = new Stage();

        FXMLLoader loader = new FXMLLoader(AlertBox.class.getResource("stakeHolder.fxml"));
        Parent root = loader.load();
        window.setScene(new Scene(root, 600, 400));
        window.getIcons().add(new Image("com/e4all/main/Resources/Assets/stageIcon.png"));
        window.setTitle("Stakeholders");
        window.show();


        window.setOnCloseRequest(e -> {
            window.close();
        });
    }

}