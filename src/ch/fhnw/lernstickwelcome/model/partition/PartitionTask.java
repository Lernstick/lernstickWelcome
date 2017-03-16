/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.partition;

import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.util.Partition;
import ch.fhnw.util.ProcessExecutor;
import ch.fhnw.util.StorageDevice;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

/**
 *
 * @author sschw
 */
public class PartitionTask extends Task<Boolean> {
    private static final Logger LOGGER = Logger.getLogger(PartitionTask.class.getName());
    private static final ProcessExecutor PROCESS_EXECUTOR = WelcomeModelFactory.getProcessExecutor();
    private Partition exchangePartition;
    
    private String oldExchangePartitionLabel;
    private StringProperty exchangePartitionLabel = new SimpleStringProperty();
    private BooleanProperty accessExchangePartition = new SimpleBooleanProperty();
    private BooleanProperty showReadOnlyInfo = new SimpleBooleanProperty();
    private BooleanProperty showReadWriteWelcome = new SimpleBooleanProperty();

    public PartitionTask(Properties properties) {
        StorageDevice sd = WelcomeModelFactory.getSystemStorageDevice();
        if(sd != null) {
            exchangePartition = sd.getExchangePartition();
            if(exchangePartition != null) {
                exchangePartitionLabel.set(exchangePartition.getIdLabel());
                oldExchangePartitionLabel = exchangePartitionLabel.get();
            }
        }
        accessExchangePartition.set("true".equals(
                properties.getProperty(WelcomeConstants.EXCHANGE_ACCESS)));
        showReadWriteWelcome.set("true".equals(
                properties.getProperty(WelcomeConstants.SHOW_WELCOME)));
        showReadOnlyInfo.set("true".equals(
                properties.getProperty(WelcomeConstants.SHOW_READ_ONLY_INFO, "true")));
        
    }

    @Override
    protected Boolean call() throws Exception {
        LOGGER.log(Level.INFO, "new exchange partition label: \"{0}\"",
                exchangePartitionLabel.get());
        if (!exchangePartitionLabel.get().isEmpty()
                && !exchangePartitionLabel.get().equals(oldExchangePartitionLabel)) {
            String binary = null;
            boolean umount = false;
            String idType = exchangePartition.getIdType();
            switch (idType) {
                case "vfat":
                    binary = "dosfslabel";
                    break;
                case "exfat":
                    binary = "exfatlabel";
                    break;
                case "ntfs":
                    binary = "ntfslabel";
                    // ntfslabel refuses to work on a mounted partition with the
                    // error message: "Cannot make changes to a mounted device".
                    // Therefore we have to try to umount the partition.
                    umount = true;
                    break;
                default:
                    LOGGER.log(Level.WARNING,
                            "no labeling binary for type \"{0}\"!", idType);
                    break;
            }
            if (binary != null) {
                boolean tmpUmount = umount && exchangePartition.isMounted();
                if (tmpUmount) {
                    exchangePartition.umount();
                }
                PROCESS_EXECUTOR.executeProcess(binary,
                        "/dev/" + exchangePartition.getDeviceAndNumber(),
                        exchangePartitionLabel.get());
                if (tmpUmount) {
                    exchangePartition.mount();
                }
            }
        }
        return true;
    }

    public StringProperty getExchangePartitionLabel() {
        return exchangePartitionLabel;
    }

    public BooleanProperty getAccessExchangePartition() {
        return accessExchangePartition;
    }

    public BooleanProperty getShowReadOnlyInfo() {
        return showReadOnlyInfo;
    }

    public BooleanProperty getShowReadWriteWelcome() {
        return showReadWriteWelcome;
    }
    
}
