package com.e4all.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;

    private void setPrimaryStage(Stage stage) {
        Main.primaryStage = stage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        setPrimaryStage(primaryStage);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("model.fxml"));
        Parent root = loader.load();


        primaryStage.setOnCloseRequest(e -> {
            primaryStage.close();
            System.exit(0);
        });
        primaryStage.setTitle("  Energy4All");
        primaryStage.getIcons().add(new Image("com/e4all/main/Resources/Assets/stageIcon.png"));

        Scene scene = new Scene(root, 800, 500);
        primaryStage.setScene(scene);


        primaryStage.show();
    }

    public static void main(String[] args) {
            launch(args);
    }

}


