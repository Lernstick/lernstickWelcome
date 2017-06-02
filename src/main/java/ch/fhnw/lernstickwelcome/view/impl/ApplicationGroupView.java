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
package ch.fhnw.lernstickwelcome.view.impl;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

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
        title.setFont(Font.font(null, FontWeight.BOLD, 14));
        setTop(title);
        
        setCenter(content);
        content.setAlignment(Pos.TOP_LEFT);
        
    }
    
    public void setTitle(String text) {
        title.setText(text);
    }
    
    public VBox getAppContainer() {
        return content;
    }
}
