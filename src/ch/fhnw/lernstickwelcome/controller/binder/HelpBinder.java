/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller.binder;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationHelpController;
import ch.fhnw.lernstickwelcome.model.help.HelpEntry;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;

/**
 *
 * @author sschw
 */
public class HelpBinder {

    private final WelcomeController controller;
    private final WelcomeApplicationHelpController help;
    // Save all tree items to ensure an fast search for the needed help entry.
    private List<TreeItem<HelpEntry>> treeItems = new ArrayList<>();

    public HelpBinder(WelcomeController controller, WelcomeApplicationHelpController help) {
        this.controller = controller;
        this.help = help;
    }

    public void initBindings() {
        TreeItem<HelpEntry> root = new TreeItem<>(null);
        root.getChildren().addAll(getTreeItemsFromList(controller.getHelpLoader().getHelpEntries()));
        help.getTvHelpList().setRoot(root);
        help.getTvHelpList().setShowRoot(false);
    }

    private List<TreeItem<HelpEntry>> getTreeItemsFromList(List<HelpEntry> entries) {
        if (entries.isEmpty()) {
            return null;
        }

        List<TreeItem<HelpEntry>> list = entries.stream().map(he -> new TreeItem<>(he)).collect(Collectors.toList());
        list.forEach(he -> {
            if (!he.getValue().getSubEntries().isEmpty()) {
                he.getChildren().addAll(getTreeItemsFromList(he.getValue().getSubEntries()));
            }
            he.setExpanded(true);
        });
        treeItems.addAll(list);
        return list;
    }

    public void initHandlers() {
        help.getTvHelpList().getSelectionModel().selectedItemProperty().addListener(evt
                -> help.getWvHelpView().getEngine().load(
                        help.getTvHelpList().getSelectionModel().getSelectedItem().getValue().getPath()
                )
        );

        help.getBtnOk().setOnAction(evt
                -> ((Stage) ((Node) evt.getSource()).getScene().getWindow()).close());
    }

    public void setHelpEntry(String chapter) {
        String[] chapters = chapter.split("\\.");
        TreeItem<HelpEntry> entry = help.getTvHelpList().getRoot();
        for(String c : chapters) {
            entry = entry.getChildren().stream().
                    filter(t -> t.getValue().getIndex() == Integer.parseInt(c)).
                    findFirst().
                    get();
        }
        help.getTvHelpList().getSelectionModel().select(entry);
    }
}
