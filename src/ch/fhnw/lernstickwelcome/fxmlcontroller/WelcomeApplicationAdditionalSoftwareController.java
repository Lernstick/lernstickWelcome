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
 * @author LineStettler
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

    //needed to get value of key for description and error message
    ResourceBundle rb;
    
    /**
     * Method to get all available applikation tasks, display them in 
     * groups with icon, name and (optional) description.
     * 
     * @param teachApps     list of applications usable for lectures/learning
     * @param softwApps     list of additional apps with different purposes
     * @param gameApps      list of additional games (learning games)
     */
    public void initializeApps(ApplicationGroupTask teachApps, ApplicationGroupTask softwApps, ApplicationGroupTask gameApps)
    {
       //Add teach apps to view and add bindings
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
                gp_teaching.add(new ImageView(icon), 0, i);
                gp_teaching.add(name, 1, i);
                gp_teaching.add(tbutton, 2, i);
                if(!"".equals(description))
                {
                    Label descript = new Label(description);
                    descript.setFont(new Font(11));
                    gp_teaching.add(descript, 0, ++i);
                }           
                ++i;      
            }
        }catch(NullPointerException e){
            //If no applications are available/configured with these tag in xml file
            Label error = new Label(rb.getString("WelcomeApplicationAdditionalSoftware.notAvailable"));
            gp_teaching.add(error, 0, 1);
        }
        
        //Add other software apps to view and add bindings
        try{
            // iterate throug all applikation tasks and get the information of each class to display them
            for(ApplicationTask app : softwApps.getApps())
            {
                int i = 1;
                Image icon = app.getIcon();
                Label name = new Label(app.getName());
                String description = app.getDescription();
                ToggleButton tbutton = new ToggleButton(rb.getString("WelcomeApplicationRecommendedSoftware.t1"));
                //disable installation button for this application if allready installed
                tbutton.disableProperty().setValue(app.isInstalled());
                //bind the installation button for this application to the equivalent backend property
                tbutton.selectedProperty().bindBidirectional(app.installingProperty());
                gp_softw.add(new ImageView(icon), 0, i);
                gp_softw.add(name, 1, i);
                gp_softw.add(tbutton, 2, i);
                if(!"".equals(description))
                {
                    Label descript = new Label(description);
                    descript.setFont(new Font(11));
                    gp_softw.add(descript, 0, ++i);
                }
                ++i;      
            }
        }catch(NullPointerException e){
            //If no applications are available/configured with these tag in xml file
            Label error = new Label(rb.getString("WelcomeApplicationAdditionalSoftware.notAvailable"));
            gp_softw.add(error, 0, 1);
        }
         
        //Add game apps to view and add bindings
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
                gp_games.add(new ImageView(icon), 0, i);
                gp_games.add(name, 1, i);
                gp_games.add(tbutton, 2, i);
                if(!"".equals(description))
                {
                    Label descript = new Label(description);
                    descript.setFont(new Font(11));
                    gp_games.add(descript, 0, ++i);
                }
                ++i;     
            }
        }catch(NullPointerException e){
            //If no applications are available/configured with these tag in xml file
            Label error = new Label(rb.getString("WelcomeApplicationAdditionalSoftware.notAvailable"));
            gp_games.add(error, 0, 1);
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
