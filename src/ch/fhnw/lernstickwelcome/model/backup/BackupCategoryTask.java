/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.backup;

import ch.fhnw.lernstickwelcome.model.Category;
import javafx.concurrent.Task;

/**
 *
 * @author user
 */
public class BackupCategoryTask extends Task<Boolean> implements Category {
    private String name;
    
    public BackupCategoryTask(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected Boolean call() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
