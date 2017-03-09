/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.partition;

import java.util.Properties;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;

/**
 *
 * @author sschw
 */
public class PartitionTask extends Task<Boolean> {
    private static final Logger LOGGER = Logger.getLogger(PartitionTask.class.getName());
    
    private BooleanProperty accessExchangePartition = new SimpleBooleanProperty();
    private BooleanProperty showReadOnlyInfo = new SimpleBooleanProperty();
    private BooleanProperty showReadWriteWelcome = new SimpleBooleanProperty();

    public PartitionTask(Properties properties) {
        accessExchangePartition.set("true".equals(
                properties.getProperty(EXCHANGE_ACCESS)));
        showReadWriteWelcome.set("true".equals(
                properties.getProperty(SHOW_WELCOME)));
        showReadOnlyInfo.set("true".equals(
                properties.getProperty(SHOW_READ_ONLY_INFO, "true")));
    }

    @Override
    protected Boolean call() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
