/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.backup;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

/**
 *
 * @author user
 */
public class BackupTask extends Task<Boolean> {
    private BooleanProperty active = new SimpleBooleanProperty();
    private StringProperty sourcePath = new SimpleStringProperty();
    private StringProperty directoryPath = new SimpleStringProperty();
    private BooleanProperty partition = new SimpleBooleanProperty();
    private StringProperty partitionPath = new SimpleStringProperty();
    private BooleanProperty screenshot = new SimpleBooleanProperty();
    
    public BackupTask() {
    }

    @Override
    protected Boolean call() throws Exception {
        updateProgress(0, 1);
        if(active.get()) {
            
        }
        updateProgress(1, 1);
        return true;
    }
    
}
