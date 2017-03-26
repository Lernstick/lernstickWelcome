/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.fxmlcontroller;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;


/**
 * FXML Controller class
 *
 * @author user
 */
public class WelcomeApplicationStartController implements Initializable {

    @FXML
    private Button FinishButton;
    @FXML
    private Button SaveButton;
    
    private ResourceBundle bundle;
    @FXML
    private ListView<MenuPaneItem> MenuPane;
    @FXML
    private Pane MainPane;
    @FXML
    private SplitPane SplitPane;

    public void initializeMenu(ObservableList<MenuPaneItem> list) {
        MenuPane.setCellFactory(lv -> new ListCell<MenuPaneItem>() {
            
            @Override
            protected void updateItem(MenuPaneItem item, boolean empty) { 
                if(!empty) {
                    setText(item.getDisplayText());
                    if(item.getImagePath() != null)
                        setGraphic(new ImageView(item.getImagePath()));
                }
            }
        });
        MenuPane.setItems(list);
        
        MenuPane.selectionModelProperty().addListener(cl -> { 
            MainPane.getChildren().clear();
            MainPane.getChildren().add(MenuPane.getSelectionModel().getSelectedItem().getParentScene());
        });
        MenuPane.getSelectionModel().selectFirst();
        MainPane.getChildren().add(MenuPane.getSelectionModel().getSelectedItem().getParentScene());
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    } 
    
    @FXML
     private void onFinishClickedAction(MouseEvent event) {
        ((Node)(event.getSource())).getScene().getWindow().hide();
        //create progress bar
    }
     
    @FXML
     private void onSaveClickedAction(MouseEvent event) {
        ((Node)(event.getSource())).getScene().getWindow().hide();
        //save all
    }

  /*   private void onClickShowSystem*/
    
    
}
