package ch.fhnw.lernstickwelcome.controller.binder.standard;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.standard.InformationController;

/**
 * Binder class to init binings between view components and backend (model) properties
 * 
 * @author Line Stettler
 */
public class InformationBinder {
    private final WelcomeController controller;
    private final InformationController information;

    /**
     * Constructor of InformationBinder class
     * 
     * @param controller        is needed to provide access to the backend properties
     * @param information       FXML controller which prviedes the view properties
     */
    public InformationBinder(WelcomeController controller, InformationController information){
        this.controller = controller;
        this.information = information;
    }
    
    /**
     * Method to initialize the bidirectional bindings between the view and packend properties
     * (gets debian version)
     */
    public void initBindings(){
        information.getLbOs().textProperty().bind(controller.getSysconf().systemnameProperty());
        information.getLbVersion().textProperty().bind(controller.getSysconf().systemversionProperty());
    }
}
