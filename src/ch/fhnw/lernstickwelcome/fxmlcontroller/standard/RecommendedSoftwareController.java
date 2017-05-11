package ch.fhnw.lernstickwelcome.fxmlcontroller.standard;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author Line Stettler
 */
public class RecommendedSoftwareController implements Initializable {

    @FXML
    private Button btHelp;
    @FXML
    private VBox vbApps;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }
    
    public Button getBtHelp() {
        return btHelp;
    }
    
    public VBox getVbApps() {
        return vbApps;
    }
    
}
