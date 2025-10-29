package com.ordonnancement.service.parser.implementation.process;

import java.util.List;

import com.ordonnancement.model.Process;
import com.ordonnancement.service.parser.FileParser;
import com.ordonnancement.service.parser.FileParserStrategy;

/**
 * Classe permettant de parser les fichiers listants les processus, selon une strétégie de parsing
 */

public class ProcessParser implements FileParser<Process>{
    
    private final FileParserStrategy<Process> strategie; //La stratégie de parsing

    /**
     * Constructeur du process parseur
     * @param strategie : la stratégie de parsing à appliquer
     */
    public ProcessParser(FileParserStrategy<Process> strategie){
        this.strategie = strategie;
    }
    /**
     * Permet de parser les fichiers listant des processus
     * @param cheminFichier : le chemin du fichier à parser
     * @return La liste des processus du fichiers
     * @throws Exception : si le format du fichier est incompatible
     */
    @Override
    public List<Process> parse(String cheminFichier){
        return strategie.parse(cheminFichier);
        
    }

}
