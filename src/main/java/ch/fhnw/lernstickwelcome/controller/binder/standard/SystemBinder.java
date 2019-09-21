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
        system.getReadOnlyWarningToggleSwitch().selectedProperty().bindBidirectional(controller.getPartition().showReadOnlyInfoProperty());
        system.getStartWelcomeApplicationToggleSwitch().selectedProperty().bindBidirectional(controller.getPartition().showReadWriteWelcomeProperty());
        system.getTimeoutComboBox().valueProperty().bindBidirectional(controller.getSysconf().timeoutSecondsProperty());
        system.getExchangePartitionTextField().textProperty().bindBidirectional(controller.getPartition().exchangePartitionLabelProperty());
        system.getUserNameTextField().textProperty().bindBidirectional(controller.getSysconf().usernameProperty());
        system.getSystemNameTextField().textProperty().bindBidirectional(controller.getSysconf().systemnameProperty());
        system.getSystemVersionTextField().textProperty().bindBidirectional(controller.getSysconf().systemversionProperty());
        system.getProxyToggleSwitch().selectedProperty().bindBidirectional(controller.getProxy().proxyActiveProperty());
        system.getProxyHostTextField().textProperty().bindBidirectional(controller.getProxy().hostnameProperty());
        system.getProxyPortTextField().textProperty().bindBidirectional(controller.getProxy().portProperty());
        system.getProxyPasswordField().textProperty().bindBidirectional(controller.getProxy().passwordProperty());
        system.getProxyUserTextField().textProperty().bindBidirectional(controller.getProxy().usernameProperty());
        
        system.getExchangePartitionTextField().setDisable(!controller.getPartition().hasExchangePartition());
       
    }

    /**
     * Open other view by clicking on help button
     *
     * @param helpStage additional window showing help
     * @param help links to online user guide
     */
    public void initHelp(Stage helpStage, HelpBinder help) {
        system.getHelpButton().setOnAction(evt -> {
            help.setHelpEntryByChapter("3");
            helpStage.show();
        });
    }
    
}

