/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.help;

import ch.fhnw.lernstickwelcome.controller.ProcessingException;
import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author sschw
 */
public class HelpLoader {
    private List<HelpEntry> entries = new ArrayList<>();
    
    public HelpLoader(String language) {
        loadHelpEntries(language);
    }
    
    private void loadHelpEntries(String language) {
        File rootDir = new File(WelcomeConstants.HELP_FILE_PATH + "/" + language);
        fillFoundEntriesIntoList(rootDir, "^", entries);
    }
    
    private void fillFoundEntriesIntoList(File rootDir, String levelString, List<HelpEntry> parent) {
        String regexPattern = levelString + "([1-9]+)-([A-Za-z_0-9]+).html";
        for(File f : rootDir.listFiles()) {
            Pattern pattern = Pattern.compile(regexPattern);
            Matcher matcher = pattern.matcher(f.getName());
            
            if(matcher.matches()) {
                int index = Integer.valueOf(matcher.group(1));
                HelpEntry entry = new HelpEntry();
                entry.setTitle(matcher.group(2).replaceAll("_", " "));
                entry.setPath(f.toURI().toString());
                entry.setIndex(index);
                parent.add(entry);

                fillFoundEntriesIntoList(rootDir, levelString + index + "\\.", entry.getSubEntries());
            }
        }
        Collections.sort(parent);
    }
    
    public List<HelpEntry> getHelpEntries() {
        return entries;
    }
}
