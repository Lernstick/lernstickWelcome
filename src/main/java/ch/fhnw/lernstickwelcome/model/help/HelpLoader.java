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
package ch.fhnw.lernstickwelcome.model.help;

import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
 *
 * @author sschw
 */
public class HelpLoader {

    private final static Logger LOGGER
            = Logger.getLogger(HelpLoader.class.getName());
    private final List<HelpEntry> entries = new ArrayList<>();

    /**
     * Loads the Help Files from the folder
     *
     * @param language the language that the Help File should have.
     * @param isExamEnvironment change path if it's the exam environment.
     */
    public HelpLoader(String language, boolean isExamEnvironment) {
        loadHelpEntries(language, isExamEnvironment);
    }

    /**
     * Loads the Help Files from the folder
     *
     * @param language the language that the Help File should have.
     * @param isExamEnvironment change path if it's the exam environment.
     */
    private void loadHelpEntries(String language, boolean isExamEnvironment) {
        // Look for path under HelpFilePath/Language/std or ex
        List<Path> files = getHelpFiles(getHelpPath(
                language, isExamEnvironment));

        // If language isn't supported use English as default
        if (files == null) {
            files = getHelpFiles(getHelpPath("en", isExamEnvironment));
        }

        // Fill Help Entries recursive
        if (files != null) {
            fillFoundEntriesIntoList(files, "", entries);
        }
    }

    /**
     * Filles the help entries into a list and tries to find children of each
     * help entry.
     * <br>
     * Sorts the help entries inside the list.
     *
     * @param subfiles give all files of the subfolder of the help because all
     * files are in the same folder and therefore {@link File#listFiles() }
     * should only be called once.
     * @param prefixString gives the prefix String of the HelpFiles
     * @param parent the list in which the help entries should be saved.
     */
    private void fillFoundEntriesIntoList(List<Path> subfiles,
            String prefixString, List<HelpEntry> parent) {
        String regexPattern = "^" + prefixString
                + "([\\p{Digit}]+)-([\\p{Alnum}_]+).html";
        Pattern pattern = Pattern.compile(regexPattern,
                Pattern.UNICODE_CHARACTER_CLASS);
        for (Path path : subfiles) {
            Matcher matcher = pattern.matcher(path.getFileName().toString());

            if (matcher.matches()) {
                int index = Integer.valueOf(matcher.group(1));
                HelpEntry entry = new HelpEntry();
                entry.setTitle(matcher.group(2).replaceAll("_", " "));
                entry.setPath(path.toUri().toString());
                entry.setIndex(index);
                parent.add(entry);

                fillFoundEntriesIntoList(subfiles,
                        prefixString + index + "\\.", entry.getSubEntries());
            }
        }
        Collections.sort(parent);
    }

    /**
     * Returns the path in which the help files are.
     *
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

    /**
     * This method reads out the files.
     * <br>
     * As the files might be packed into a jar we have to use
     * {@link Class#getResourceAsStream(java.lang.String) }
     *
     * @param path The relative help file path
     * @return list of url's which refer to the containing files of the given
     * path
     */
    public List<Path> getHelpFiles(String path) {
        // Path to file repository - might be jar file
        try {
            URI uri = HelpLoader.class.getResource(path).toURI();
            // If jar ! means jarfile.
            String[] split = uri.toString().split("!");
            if (split.length == 1) {
                return Files.list(Paths.get(uri)).collect(Collectors.toList());
            } else {
                FileSystem fs = FileSystems.newFileSystem(
                        URI.create(split[0]), new HashMap<>());
                return Files.list(fs.getPath(split[1])).collect(
                        Collectors.toList());
            }
        } catch (IOException | URISyntaxException ex) {
            LOGGER.log(Level.WARNING,
                    "Help files not found with path: " + path, ex);
        }
        return null;
    }
}
