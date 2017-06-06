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
package ch.fhnw.lernstickwelcome.fxmlcontroller;

import ch.fhnw.lernstickwelcome.view.impl.MenuPaneItem;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

/**
 * FXML Controller class
 *
 * @author user
 */
public class MainController implements Initializable {

    @FXML
    private Button btFinishButton;
    @FXML
    private Button btSaveButton;
    @FXML
    private ListView<MenuPaneItem> lvMenuPane;
    @FXML
    private ScrollPane spMainPane;

    public void initializeMenu(ObservableList<MenuPaneItem> list) {
        lvMenuPane.setCellFactory(lv -> new MenuListCell());
        lvMenuPane.setItems(list);
        lvMenuPane.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Change and resize the content
        lvMenuPane.getSelectionModel().selectedItemProperty().addListener(cl -> {
            spMainPane.setContent(lvMenuPane.getSelectionModel().getSelectedItem().getParentScene());
            spMainPane.setVvalue(0);
            ((Region) (spMainPane.getContent())).setPrefWidth(spMainPane.getWidth());
            ((Region) (spMainPane.getContent())).setPrefHeight(spMainPane.getHeight());
        });
        // Resize the content
        spMainPane.widthProperty().addListener(cl -> ((Region) (spMainPane.getContent())).setPrefWidth(spMainPane.getWidth()));
        spMainPane.heightProperty().addListener(cl -> ((Region) (spMainPane.getContent())).setPrefHeight(spMainPane.getHeight()));

        // Select first node as start screen
        lvMenuPane.getSelectionModel().selectFirst();
    }

    private static class MenuListCell extends ListCell<MenuPaneItem> {

        @Override
        protected void updateItem(MenuPaneItem item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                setText(item.getDisplayText());
                if (item.getImagePath() != null) {
                    setGraphic(new ImageView(item.getImagePath()));
                }
            }
        }
    }

    public void setView(int i) {
        lvMenuPane.getSelectionModel().select(i);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public Button getBtFinishButton() {
        return btFinishButton;
    }

    public Button getBtSaveButton() {
        return btSaveButton;
    }

}
