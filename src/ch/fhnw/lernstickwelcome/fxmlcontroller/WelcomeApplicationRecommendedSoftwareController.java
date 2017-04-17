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
public class WelcomeApplicationRecommendedSoftwareController implements Initializable {

    private ToggleButton tbtn_rs_flash;
    private ToggleButton tbtn_rs_reader;
    private ToggleButton tbtn_rs_font;
    private ToggleButton tbtn_rs_mmFormats;
    @FXML
    private Button btn_sys_help;
    @FXML
    private GridPane gp_recommended;
    
    ResourceBundle rb;

     public void initializeApps(ApplicationGroupTask recApps)
    {
       try{
            for(ApplicationTask app : recApps.getApps())
            {
                int i = 1;
                Image icon = app.getIcon();
                Label name = new Label(app.getName());
                String description = app.getDescription();
                ToggleButton tbutton = new ToggleButton(rb.getString("WelcomeApplicationRecommendedSoftware.t1"));
                tbutton.disableProperty().setValue(app.isInstalled());
                tbutton.selectedProperty().bindBidirectional(app.installingProperty());
                gp_recommended.add(new ImageView(icon), 0, i);
                gp_recommended.add(name, 1, i);
                gp_recommended.add(tbutton, 2, i);
                if(!"".equals(description))
                {
                    Label descript = new Label(description);
                    descript.setFont(new Font(11));
                    gp_recommended.add(descript, 0, ++i);
                }           
                ++i;      
            }
        }catch(NullPointerException e){
            Label error = new Label(rb.getString("WelcomeApplicationAdditionalSoftware.notAvailable"));
            gp_recommended.add(error, 0, 1);
        }
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.rb = rb;
    }    


    public ToggleButton getTbtn_rs_flash() {
        return tbtn_rs_flash;
    }

    public ToggleButton getTbtn_rs_reader() {
        return tbtn_rs_reader;
    }

    public ToggleButton getTbtn_rs_font() {
        return tbtn_rs_font;
    }

    public ToggleButton getTbtn_rs_mmFormats() {
        return tbtn_rs_mmFormats;
    }

    public Button getBtn_sys_help() {
        return btn_sys_help;
    }
    
    
    
}
