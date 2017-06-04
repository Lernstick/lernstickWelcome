/*
 * Copyright (C) 2017 FHNW
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.fhnw.lernstickwelcome.controller.binder.standard;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.controller.binder.HelpBinder;
import ch.fhnw.lernstickwelcome.fxmlcontroller.standard.SystemController;
import javafx.stage.Stage;

/**
 * Binder class to init binings between view components and backend (model) properties
 * 
 * @author Line Stettler
 */
public class SystemBinder {

    private WelcomeController controller;
    private SystemController system;
    
   /**
     * Constructor of ExamInformationBinder class
     * 
     * @param controller        is needed to provide access to the backend properties
     * @param system            FXML controller which prviedes the view properties
     */
    public SystemBinder(WelcomeController controller, SystemController system){
        this.controller = controller;
        this.system = system;
    }
    
    /**
     * Method to initialize the bidirectional bindings between the view and packend properties
     */
    public void initBindings(){
        system.getTsBlockKde().selectedProperty().bindBidirectional(controller.getSysconf().blockKdeDesktopAppletsProperty());
        system.getTsDirectSound().selectedProperty().bindBidirectional(controller.getSysconf().directSoundOutputProperty());
        system.getTsShowWarning().selectedProperty().bindBidirectional(controller.getPartition().showReadOnlyInfoProperty());
        system.getTsStartWa().selectedProperty().bindBidirectional(controller.getPartition().showReadWriteWelcomeProperty());
        system.getCbVisibleFor().valueProperty().bindBidirectional(controller.getSysconf().timeoutSecondsProperty());
        system.getTfExchangePartition().textProperty().bindBidirectional(controller.getPartition().exchangePartitionLabelProperty());
        system.getTfUsername().textProperty().bindBidirectional(controller.getSysconf().usernameProperty());
        system.getTfSystemname().textProperty().bindBidirectional(controller.getSysconf().systemnameProperty());
        system.getTfSystemversion().textProperty().bindBidirectional(controller.getSysconf().systemversionProperty());
        system.getTsProxy().selectedProperty().bindBidirectional(controller.getProxy().proxyActiveProperty());
        system.getTfHost().textProperty().bindBidirectional(controller.getProxy().hostnameProperty());
        system.getTfPort().textProperty().bindBidirectional(controller.getProxy().portProperty());
        system.getTfPwd().textProperty().bindBidirectional(controller.getProxy().passwordProperty());
        system.getTfUser().textProperty().bindBidirectional(controller.getProxy().usernameProperty());
        
        system.getTfExchangePartition().setDisable(!controller.getPartition().hasExchangePartition());
       
    }

    /**
     * Open other view by clicking on help button
     *
     * @param helpStage additional window showing help
     * @param help links to online user guide
     */
    public void initHelp(Stage helpStage, HelpBinder help) {
        system.getBtHelp().setOnAction(evt -> {
            help.setHelpEntryByChapter("3");
            helpStage.show();
        });
    }
    
}

