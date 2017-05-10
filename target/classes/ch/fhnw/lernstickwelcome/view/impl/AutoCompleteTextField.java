/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.view.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

/**
 * This class is a TextField which implements an "autocomplete" functionality, based on a supplied list of entries.<br>
 * <small>This snipped is from gist and modified for the Lernstick.</small>
 * @author Caleb Brinkman & sschw
 */
public class AutoCompleteTextField extends TextField
{
  /** The existing autocomplete entries. */
  private final SortedSet<String> entries;
  /** The popup used to select an entry. */
  private ContextMenu entriesPopup;

  /** Construct a new AutoCompleteTextField. */
  public AutoCompleteTextField() {
    super();
    entries = new TreeSet<>();
    entriesPopup = new ContextMenu();
    entriesPopup.prefWidthProperty().bind(widthProperty());
    setOnKeyPressed(e -> { if(e.getCode() == KeyCode.DOWN) showEntries(""); });
    textProperty().addListener(new ChangeListener<String>()
    {
      @Override
      public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
        if (getText() == null || getText().length() == 0)
        {
          entriesPopup.hide();
        } else
        {
          showEntries(getText());
        }
      }
    });

    focusedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
        entriesPopup.hide();
      }
    });

  }

  private void showEntries(String text) {
	  LinkedList<String> searchResult = new LinkedList<>();
      final List<String> filteredEntries = entries.stream().filter(e -> e.toLowerCase().contains(text.toLowerCase())).collect(Collectors.toList());
      searchResult.addAll(filteredEntries);
      if (entries.size() > 0)
      {
        populatePopup(searchResult);
        if (!entriesPopup.isShowing())
        {
          entriesPopup.show(AutoCompleteTextField.this, Side.BOTTOM, 0, 0);
        }
      } else
      {
        entriesPopup.hide();
      }
}

/**
   * Get the existing set of autocomplete entries.
   * @return The existing autocomplete entries.
   */
  public SortedSet<String> getEntries() { return entries; }

  /**
   * Populate the entry set with the given search results.  Display is limited to 10 entries, for performance.
   * @param searchResult The set of matching strings.
   */
  private void populatePopup(List<String> searchResult) {
    List<CustomMenuItem> menuItems = new LinkedList<>();
    // If you'd like more entries, modify this line.
    int maxEntries = 10;
    int count = Math.min(searchResult.size(), maxEntries);
    for (int i = 0; i < count; i++)
    {
      final String result = searchResult.get(i);
      Label entryLabel = new Label(result);
      // Set the length of the popup to the length of the textfield
      DoubleBinding b = Bindings.createDoubleBinding(() -> widthProperty().get()-13, widthProperty());
      entryLabel.prefWidthProperty().bind(b);
      CustomMenuItem item = new CustomMenuItem(entryLabel, true);
      item.setOnAction(new EventHandler<ActionEvent>()
      {
        @Override
        public void handle(ActionEvent actionEvent) {
          setText(result);
          entriesPopup.hide();
        }
      });
      menuItems.add(item);
    }
    entriesPopup.getItems().clear();
    entriesPopup.getItems().addAll(menuItems);

  }
}