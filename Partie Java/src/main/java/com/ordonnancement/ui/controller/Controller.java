package com.ordonnancement.ui.controller;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class Controller extends Application{

    @FXML
    ListView<String> listView;
    @FXML 
    TextArea textArea;
    @FXML
    Button button;
    
    ObservableList<String> listeTexte = FXCollections.observableArrayList();


    public void initialize(){
        listView.setItems(listeTexte);
    }



    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Controller.class.getResource("/fxml/vue.fxml"));
        Scene scene = new Scene(loader.load()); //Mettre la vue dans la scene
        primaryStage.setScene(scene); //Mettre la scene dans la pripmmary stage
        primaryStage.setTitle("Application");
        primaryStage.show();

    }


    public static void runApp(String[] args){
        launch(args);
    }


    public void onClick(){
        String texte = textArea.getText();
        listeTexte.add(texte);
    }

}
