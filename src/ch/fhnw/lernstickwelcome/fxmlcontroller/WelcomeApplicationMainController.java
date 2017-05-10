/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.fxmlcontroller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;


/**
 * FXML Controller class
 *
 * @author user
 */
public class WelcomeApplicationMainController implements Initializable {

    @FXML
    private Button FinishButton;
    @FXML
    private Button SaveButton;
    @FXML
    private ListView<MenuPaneItem> MenuPane;
    @FXML
    private ScrollPane MainPane;

    public void initializeMenu(ObservableList<MenuPaneItem> list) {
        MenuPane.setCellFactory(lv -> new ListCell<MenuPaneItem>() {
            
            @Override
            protected void updateItem(MenuPaneItem item, boolean empty) { 
                super.updateItem(item, empty);
                if(!empty) {
                    setText(item.getDisplayText());
                    if(item.getImagePath() != null)
                        setGraphic(new ImageView(item.getImagePath()));
                }
            }
        });
        MenuPane.setItems(list);
        MenuPane.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // Change and resize the content
        MenuPane.getSelectionModel().selectedItemProperty().addListener(cl -> { 
            MainPane.setContent(MenuPane.getSelectionModel().getSelectedItem().getParentScene());
            MainPane.setVvalue(0);
            ((Region)(MainPane.getContent())).setPrefWidth(MainPane.getWidth());
            ((Region)(MainPane.getContent())).setPrefHeight(MainPane.getHeight());
        });
        // Resize the content
        MainPane.widthProperty().addListener(cl -> ((Region)(MainPane.getContent())).setPrefWidth(MainPane.getWidth()));
        MainPane.heightProperty().addListener(cl -> ((Region)(MainPane.getContent())).setPrefHeight(MainPane.getHeight()));
        
        // Select first node as start screen
        MenuPane.getSelectionModel().selectFirst();
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    } 
    
    @FXML
     private void onFinishClickedAction(MouseEvent event) {
        ((Stage)((Node)(event.getSource())).getScene().getWindow()).close();
        //create progress bar
    }
     
    @FXML
     private void onSaveClickedAction(MouseEvent event) {
        //save all
        ((Stage)((Node)(event.getSource())).getScene().getWindow()).close();
    }

    public Button getFinishButton() {
        return FinishButton;
    }

    public Button getSaveButton() {
        return SaveButton;
    }
    
    
}
