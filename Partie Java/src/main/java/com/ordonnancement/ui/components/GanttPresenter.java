package com.ordonnancement.ui.components;

import java.util.List;

import com.ordonnancement.model.gantt.IGanttTask;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;

public class GanttPresenter extends TitledPane{

    private final GanttPane ganttPane;
    private final ScrollPane scrollPane;

    public GanttPresenter(){
        scrollPane = new ScrollPane();
        ganttPane = new GanttPane();
        //Mettre le gantt pane dans le scroll pane
        scrollPane.setContent(ganttPane);
        scrollPane.setPannable(true);
        //Mettre le scroll bane dans la titled pane
        this.setContent(scrollPane);
        scrollPane.setMaxHeight(400); //Hauteur max de chaque gantt dans le gantt

        // INTERCEPTER le scroll AVANT qu'il arrive au ScrollPane
        // et le consommer pour qu'il ne remonte pas au parent
        scrollPane.setOnScroll(event -> {
            event.consume(); //Pour pas propager au parent
        });




    }


    public GanttPresenter(String titre){
        this();  //Constructeur sans param
        this.setText(titre); //On défini son titre
        
    }

    public void presentGantt(List<IGanttTask> tachesGantt,int dateFinMax, List<String> listeCategories ){
        ganttPane.dessinerGanttProcessor(tachesGantt, dateFinMax, listeCategories);
        
    
    
    };

}
