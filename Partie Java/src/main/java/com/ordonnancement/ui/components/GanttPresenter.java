package com.ordonnancement.ui.components;

import java.util.List;

import com.ordonnancement.model.gantt.IGanttTask;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;

/**
 * Classe correspondant à un objet graphique
 * permettant de présenter un GanttPane facilement
 * @author ROMA Quentin
 */
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
        ganttPane.setPadding(new Insets(0, 0, 30, 0)); //Padding bottom pour laisser petit espace evec le gantt
    }


    public GanttPresenter(String titre){
        this();  //Constructeur sans param
        this.setText(titre); //On défini son titre
        
    }

    /**
     * Permet de lancer l'affichage du gantt dans le presenter
     * @param tachesGantt : Les taches à afficher
     * @param dateFinMax : La date de fin max des taches
     * @param listeCategories : La liste des catégories
     */
    public void presentGantt(List<IGanttTask> tachesGantt,int dateFinMax, List<String> listeCategories ){
        ganttPane.dessinerGanttProcessor(tachesGantt, dateFinMax, listeCategories);
        
    
    
    };

}
