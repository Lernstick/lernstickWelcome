/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.view.impl;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * This class represents an ApplicationGroup in the view which can be 
 * dynamically added to a existing view.
 * 
 * @author sschw
 */
public class ApplicationGroupView extends BorderPane {
    private final Label title = new Label();
    private final VBox content = new VBox();
    
    public ApplicationGroupView() {
        super();
        init();
    }

    private void init() {
        content.setFillWidth(true);
        
        title.setAlignment(Pos.TOP_LEFT);
        setTop(title);
        
        setCenter(content);
        content.setAlignment(Pos.TOP_LEFT);
        
        content.setPadding(new Insets(20, 0, 10, 0));
        content.setSpacing(20);
    }
    
    public void setTitle(String text) {
        title.setText(text);
    }
    
    public VBox getAppContainer() {
        return content;
    }
}
