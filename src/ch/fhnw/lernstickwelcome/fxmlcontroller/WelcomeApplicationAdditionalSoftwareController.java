/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.fxmlcontroller;

import ch.fhnw.lernstickwelcome.model.application.ApplicationGroupTask;
import ch.fhnw.lernstickwelcome.model.application.ApplicationTask;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;

/**
 * FXML Controller class
 *
 * @author user
 */
public class WelcomeApplicationAdditionalSoftwareController implements Initializable {

    private ToggleButton tbtn_as_teaching;
    private ToggleButton tbtn_as_skype;
    private ToggleButton tbtn_as_gComris1;
    private ToggleButton tbtn_as_gComris2;
    @FXML
    private Button btn_sys_help;
    private ToggleButton tbtn_as_gComris3;
    @FXML
    private GridPane gp_teaching;
    @FXML
    private GridPane gp_softw;
    @FXML
    private GridPane gp_games;

    ResourceBundle rb;
    
    public void initializeApps(ApplicationGroupTask teachApps, ApplicationGroupTask softwApps, ApplicationGroupTask gameApps)
    {
       try{
            for(ApplicationTask app : teachApps.getApps())
            {
                int i = 1;
                Image icon = app.getIcon();
                Label name = new Label(app.getName());
                String description = app.getDescription();
                ToggleButton tbutton = new ToggleButton(rb.getString("WelcomeApplicationRecommendedSoftware.t1"));
                tbutton.disableProperty().setValue(app.isInstalled());
                tbutton.selectedProperty().bindBidirectional(app.installingProperty());
                gp_teaching.add(new ImageView(icon), i, 0);
                gp_teaching.add(name, i, 1);
                gp_teaching.add(tbutton, i, 2);
                if(!"".equals(description))
                {
                    Label descript = new Label(description);
                    descript.setFont(new Font(11));
                    gp_teaching.add(descript, ++i, 0);
                }           
                ++i;      
            }
        }catch(NullPointerException e){
            Label error = new Label(rb.getString("WelcomeApplicationAdditionalSoftware.notAvailable"));
            gp_teaching.add(error, 1, 0);
        }
        
        
        try{        
            for(ApplicationTask app : softwApps.getApps())
            {
                int i = 1;
                Image icon = app.getIcon();
                Label name = new Label(app.getName());
                String description = app.getDescription();
                ToggleButton tbutton = new ToggleButton(rb.getString("WelcomeApplicationRecommendedSoftware.t1"));
                tbutton.disableProperty().setValue(app.isInstalled());
                tbutton.selectedProperty().bindBidirectional(app.installingProperty());
                gp_softw.add(new ImageView(icon), i, 0);
                gp_softw.add(name, i, 1);
                gp_softw.add(tbutton, i, 2);
                if(!"".equals(description))
                {
                    Label descript = new Label(description);
                    descript.setFont(new Font(11));
                    gp_softw.add(descript, ++i, 0);
                }
                ++i;      
            }
        }catch(NullPointerException e){
            Label error = new Label(rb.getString("WelcomeApplicationAdditionalSoftware.notAvailable"));
            gp_softw.add(error, 1, 0);
        }
         
        try{        
            for(ApplicationTask app : gameApps.getApps())
            {
                int i = 1;
                Image icon = app.getIcon();
                Label name = new Label(app.getName());
                String description = app.getDescription();
                ToggleButton tbutton = new ToggleButton(rb.getString("WelcomeApplicationRecommendedSoftware.t1"));
                tbutton.disableProperty().setValue(app.isInstalled());
                tbutton.selectedProperty().bindBidirectional(app.installingProperty());
                gp_games.add(new ImageView(icon), i, 0);
                gp_games.add(name, i, 1);
                gp_games.add(tbutton, i, 2);
                if(!"".equals(description))
                {
                    Label descript = new Label(description);
                    descript.setFont(new Font(11));
                    gp_games.add(descript, ++i, 0);
                }
                ++i;     
            }
        }catch(NullPointerException e){
            Label error = new Label(rb.getString("WelcomeApplicationAdditionalSoftware.notAvailable"));
            gp_games.add(error, 1, 0);
        }
    }
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.rb = rb; 
    }    


    public ToggleButton getTbtn_as_teaching() {
        return tbtn_as_teaching;
    }

    public ToggleButton getTbtn_as_skype() {
        return tbtn_as_skype;
    }

    public ToggleButton getTbtn_as_gComris1() {
        return tbtn_as_gComris1;
    }

    public ToggleButton getTbtn_as_gComris2() {
        return tbtn_as_gComris2;
    }

    public Button getBtn_sys_help() {
        return btn_sys_help;
    }

    public ToggleButton getTbtn_as_gComris3() {
        return tbtn_as_gComris3;
    }
    
    
    
}
