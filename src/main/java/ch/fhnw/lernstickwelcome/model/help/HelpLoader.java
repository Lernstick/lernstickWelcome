/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.help;

import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads the Help Files from the data.
 * <p>
 * Help Files are found under 
 * {@link WelcomeConstants#HELP_FILE_PATH}{@code /language/std} or {@code /ex}.
 * <br>
 * The name of a help file has to be like the following examples:
 * <pre>
 * 1-Welcome_to_the_Lernstick.html
 * 1.2-Welcome_to_the_Exam_Env.html
 * 1.2.3-Details_about_Exam_Env.html
 * </pre>
 * </p>
 * @author sschw
 */
public class HelpLoader {
    private List<HelpEntry> entries = new ArrayList<>();
    
    /**
     * Loads the Help Files from the folder
     * @param language the language that the Help File should have.
     * @param isExamEnvironment change path if it's the exam environment.
     */
    public HelpLoader(String language, boolean isExamEnvironment) {
        loadHelpEntries(language, isExamEnvironment);
    }
    
    /**
     * Loads the Help Files from the folder
     * @param language the language that the Help File should have.
     * @param isExamEnvironment change path if it's the exam environment.
     */
    private void loadHelpEntries(String language, boolean isExamEnvironment) {
        // Look for path under HelpFilePath/Language/std or ex
        
        File rootDir = new File(HelpLoader.class.getResource(getHelpPath(language, isExamEnvironment)).getFile());
        
        // If language isn't supported use English as default
        if(!rootDir.exists())
            rootDir = new File(getHelpPath("en", isExamEnvironment));
        
        // Fill Help Entries recursive
        if(rootDir.exists())
            fillFoundEntriesIntoList(rootDir.listFiles(), "", entries);
    }
    
    /**
     * Filles the help entries into a list and tries to find children of each
     * help entry.
     * <br>
     * Sorts the help entries inside the list.
     * @param subfiles give all files of the subfolder of the help because all 
     * files are in the same folder and therefore {@link File#listFiles() } 
     * should only be called once.
     * @param prefixString gives the prefix String of the HelpFiles
     * @param parent the list in which the help entries should be saved.
     */
    private void fillFoundEntriesIntoList(File[] subfiles, String prefixString, List<HelpEntry> parent) {
        String regexPattern = "^" + prefixString + "([0-9]+)-([A-Za-z_0-9]+).html";
        for(File f : subfiles) {
            Pattern pattern = Pattern.compile(regexPattern);
            Matcher matcher = pattern.matcher(f.getName());
            
            if(matcher.matches()) {
                int index = Integer.valueOf(matcher.group(1));
                HelpEntry entry = new HelpEntry();
                entry.setTitle(matcher.group(2).replaceAll("_", " "));
                entry.setPath(f.toURI().toString());
                entry.setIndex(index);
                parent.add(entry);

                fillFoundEntriesIntoList(subfiles, prefixString + index + "\\.", entry.getSubEntries());
            }
        }
        Collections.sort(parent);
    }
    
    /**
     * Returns the path in which the help files are.
     * @param language the language of the help files
     * @param isExamEnvironment load exam help files or std help files
     * @return root folder of the help files.
     */
    private String getHelpPath(String language, boolean isExamEnvironment) {
        return WelcomeConstants.HELP_FILE_PATH + "/"
                + language + "/"
                + (isExamEnvironment ? "ex" : "std");
    }
    
    /**
     * @return list of help entries or an empty list if the user language and 
     * the english language doesn't exist. 
     */
    public List<HelpEntry> getHelpEntries() {
        return entries;
    }
}
