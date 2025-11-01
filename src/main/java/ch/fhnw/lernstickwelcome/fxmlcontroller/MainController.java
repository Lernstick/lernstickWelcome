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

import ch.fhnw.lernstickwelcome.view.impl.MainMenuItem;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * FXML Controller class
 *
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class MainController implements Initializable {

    @FXML
    private ListView<MainMenuItem> menuListView;
    @FXML
    private StackPane stackPane;
    @FXML
    private Button saveButton;
    @FXML
    private Button finishButton;

    public void initializeMenu(ObservableList<MainMenuItem> mainMenuItems) {

        menuListView.setCellFactory(lv -> new MenuListCell());
        menuListView.setItems(mainMenuItems);
        menuListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // add all nodes to stackPane
        for (MainMenuItem mainMenuItem : mainMenuItems) {
            Node node = mainMenuItem.getNode();
            node.setVisible(false);
            stackPane.getChildren().add(node);
        }

        // react to selections in menu list
        ReadOnlyObjectProperty selectedItemProperty
                = menuListView.getSelectionModel().selectedItemProperty();
        selectedItemProperty.addListener(cl -> {
            ObservableList<Node> children = stackPane.getChildren();
            children.get(children.size() - 1).setVisible(false);
            MainMenuItem selectedItem
                    = menuListView.getSelectionModel().getSelectedItem();
            Node node = selectedItem.getNode();
            node.toFront();
            node.setVisible(true);
        });

        // Select first node as start screen
        menuListView.getSelectionModel().selectFirst();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public void setView(int i) {
        menuListView.getSelectionModel().select(i);
    }

    public Button getFinishButton() {
        return finishButton;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    private static class MenuListCell extends ListCell<MainMenuItem> {

        @Override
        protected void updateItem(MainMenuItem item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                setText(item.getText());
                if (item.getImagePath() != null) {
                    setGraphic(new ImageView(item.getImagePath()));
                }
            }
        }
    }
}
