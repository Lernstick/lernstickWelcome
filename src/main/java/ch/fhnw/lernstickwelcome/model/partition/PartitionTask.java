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
package ch.fhnw.lernstickwelcome.model.partition;

import ch.fhnw.lernstickwelcome.model.Processable;
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
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * This class handles changes of the exchange partition or changes on behalf of
 * configurations on partitions for the Lernstick.
 * <br>
 * In order to process a backend task multiple times it extends Processable
 *
 * @see Processable
 *
 * @author sschw
 */
public class PartitionTask implements Processable<String> {

    private static final Logger LOGGER
            = Logger.getLogger(PartitionTask.class.getName());
    private static final ProcessExecutor PROCESS_EXECUTOR
            = WelcomeModelFactory.getProcessExecutor();
    private Partition exchangePartition;
    private Properties properties;

    private String oldExchangePartitionLabel;
    private StringProperty exchangePartitionLabel = new SimpleStringProperty();
    private BooleanProperty accessExchangePartition = new SimpleBooleanProperty();
    private BooleanProperty showReadOnlyInfo = new SimpleBooleanProperty();
    private BooleanProperty startWelcomeApplication = new SimpleBooleanProperty();

    /**
     * Loads the partitions with 
     * {@link WelcomeModelFactory#getSystemStorageDevice() } and loads data from
     * the properties-file.
     *
     * @param properties Property File of the Welcome Application
     */
    public PartitionTask(Properties properties) {
        this.properties = properties;

        StorageDevice sd = WelcomeModelFactory.getSystemStorageDevice();
        if (sd != null) {
            exchangePartition = sd.getExchangePartition();
            if (exchangePartition != null) {
                exchangePartitionLabel.set(exchangePartition.getIdLabel());
                oldExchangePartitionLabel = exchangePartitionLabel.get();
            }
        }

        accessExchangePartition.set("true".equals(properties.getProperty(
                WelcomeConstants.EXCHANGE_ACCESS)));
        startWelcomeApplication.set("true".equals(properties.getProperty(
                WelcomeConstants.SHOW_WELCOME)));
        showReadOnlyInfo.set("true".equals(properties.getProperty(
                WelcomeConstants.SHOW_READ_ONLY_INFO, "true")));

    }

    /**
     * Updates the exchange partition label.
     *
     * @throws DBusException
     */
    private void updateExchangePartitionLabel() throws DBusException {
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
            // Change label by calling binary /dev/device_name new_label
            PROCESS_EXECUTOR.executeProcess(binary,
                    "/dev/" + exchangePartition.getDeviceAndNumber(),
                    exchangePartitionLabel.get());
            if (tmpUmount) {
                exchangePartition.mount();
            }
        }
    }

    /**
     * If there is no exchange partition, some functions might wan't to be
     * deactivated.
     *
     * @return boolean describing if the exchange partition could be loaded.
     */
    public boolean hasExchangePartition() {
        return exchangePartition != null;
    }

    public StringProperty exchangePartitionLabelProperty() {
        return exchangePartitionLabel;
    }

    public BooleanProperty accessExchangePartitionProperty() {
        return accessExchangePartition;
    }

    public BooleanProperty showReadOnlyInfoProperty() {
        return showReadOnlyInfo;
    }

    public BooleanProperty startWelcomeApplicationProperty() {
        return startWelcomeApplication;
    }

    @Override
    public Task<String> newTask() {
        return new InternalTask();
    }

    /**
     * Task for {@link #newTask() }
     *
     * @see Processable
     */
    private class InternalTask extends Task<String> {

        @Override
        protected String call() throws Exception {
            updateProgress(0, 2);
            updateTitle("PartitionTask.title");
            updateMessage("PartitionTask.message");

            LOGGER.log(Level.INFO, "new exchange partition label: \"{0}\"",
                    exchangePartitionLabel.get());

            // If exchange partition label has changed - modify it on call
            if (exchangePartitionLabel.get() != null
                    && !exchangePartitionLabel.get().isEmpty()
                    && !exchangePartitionLabel.get().equals(
                            oldExchangePartitionLabel)) {
                updateExchangePartitionLabel();
            }

            updateProgress(1, 2);

            // Edit the properties
            properties.setProperty(WelcomeConstants.SHOW_WELCOME,
                    startWelcomeApplication.get() ? "true" : "false");
            properties.setProperty(WelcomeConstants.SHOW_READ_ONLY_INFO,
                    showReadOnlyInfo.get() ? "true" : "false");
            properties.setProperty(WelcomeConstants.EXCHANGE_ACCESS,
                    accessExchangePartition.get() ? "true" : "false");

            updateProgress(2, 2);
            return null;
        }
    }
}
