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
package ch.fhnw.lernstickwelcome.controller.binder;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.HelpController;
import ch.fhnw.lernstickwelcome.model.help.HelpEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;

/**
 * Binder class to init binings between view components and backend (model)
 * properties
 *
 * @author sschw
 */
public class HelpBinder {

    private final WelcomeController controller;
    private final HelpController help;

    /**
     * Constructor of HelpBinder class
     *
     * @param controller is needed to provide access to the backend properties
     * @param help FXML controller which proviedes the view properties
     */
    public HelpBinder(WelcomeController controller, HelpController help) {
        this.controller = controller;
        this.help = help;
    }

    /**
     * Method to initialize the bidirectional bindings between the view and
     * packend properties
     */
    public void initBindings() {
        TreeItem<HelpEntry> root = new TreeItem<>(null);
        root.getChildren().addAll(getTreeItemsFromList(controller.getHelpLoader().getHelpEntries()));
        help.getTvList().setRoot(root);
        help.getTvList().setShowRoot(false);
    }

    /**
     * Loads the backend items into the tree list recursively.
     *
     * @param entries the backend items that should be loaded.
     * @return
     */
    private List<TreeItem<HelpEntry>> getTreeItemsFromList(List<HelpEntry> entries) {
        if (entries.isEmpty()) {
            return new ArrayList<>();
        }

        List<TreeItem<HelpEntry>> list = entries.stream().map(he -> new TreeItem<>(he)).collect(Collectors.toList());
        list.forEach(he -> {
            if (!he.getValue().getSubEntries().isEmpty()) {
                he.getChildren().addAll(getTreeItemsFromList(he.getValue().getSubEntries()));
            }
            he.setExpanded(true);
        });
        return list;
    }

    /**
     * Method to initialize the handlers for this class.
     */
    public void initHandlers() {
        help.getTvList().getSelectionModel().selectedItemProperty().addListener(evt
                -> help.getWvView().getEngine().load(
                        help.getTvList().getSelectionModel().getSelectedItem().getValue().getPath()
                )
        );

        help.getBtOk().setOnAction(evt
                -> ((Stage) ((Node) evt.getSource()).getScene().getWindow()).close());
    }

    /**
     * Method to set the chapter that should be shown. <br>
     * In order to be able to be independant of the used language, only the
     * chapter has to be given. <br>
     * e.g.: 1.1_Information can be opened by give {@code 1.1} as string to the 
     * application.
     * 
     * @param chapter the chapter that should be shown.
     */
    public void setHelpEntryByChapter(String chapter) {
        String[] chapters = chapter.split("\\.");
        TreeItem<HelpEntry> entry = help.getTvList().getRoot();
        for (String c : chapters) {
            entry = entry.getChildren().stream().
                    filter(t -> t.getValue().getIndex() == Integer.parseInt(c)).
                    findFirst().
                    get();
        }
        help.getTvList().getSelectionModel().select(entry);
    }
}
