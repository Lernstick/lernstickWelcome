/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.view.impl;

import javafx.scene.Parent;

/**
 *
 * @author sschw
 */
public class MenuPaneItem {
    private Parent parentScene;
    private String displayText;
    private String imagePath;

    public MenuPaneItem(Parent parentScene, String displayText, String imagePath) {
        this.parentScene = parentScene;
        this.displayText = displayText;
        if (imagePath != null) {
            this.imagePath = MenuPaneItem.class.getResource(imagePath).toExternalForm();
        }
    }

    public Parent getParentScene() {
        return parentScene;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getImagePath() {
        return imagePath;
    }
}
