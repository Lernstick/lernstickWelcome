/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.firewall;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author sschw
 */
public class WebsiteFilter {
    public enum SearchPattern {
        Exact("^", "$"),
        Contains("", ""),
        StartsWith("^", ""),
        Custom("","");
        
        private String pre, post;
        
        SearchPattern(String pre, String post) {
            this.pre = pre;
            this.post = post;
        }
        
        public String getPattern(String searchCriteria) {
            // Escape the search criteria if it isn't Custom
            if(this != Custom)
                searchCriteria = searchCriteria.replaceAll("([.^$*+?()[{\\|])", "\\$1"); // Pattern.quote doesnt work
            return pre + searchCriteria + post;
        }
    }
    
    private ObjectProperty<SearchPattern> searchPattern = new SimpleObjectProperty<>();
    private StringProperty searchCriteria = new SimpleStringProperty();
    
    public WebsiteFilter() {
        // Initialize with exact as default value
        searchPattern.set(SearchPattern.Exact);
    }
    
    public WebsiteFilter(String line) {
        // Set correct searchPattern according to the found RegEx.
        if(line.matches("[^\\][.*+?()[{\\|]")) // Search for unescaped RegEx (without ^ and $)
            searchPattern.set(SearchPattern.Custom);
        else if(line.startsWith("^")) { // ^ at the beginning
            if(line.endsWith("$")) // $ at the end
                searchPattern.set(SearchPattern.Exact);
            else
                searchPattern.set(SearchPattern.StartsWith);
        }
        else {
            searchPattern.set(SearchPattern.Contains);
        }
    }
    
    public ObjectProperty<SearchPattern> searchPatternProperty() {
        return searchPattern;
    }
    
    public StringProperty searchCriteriaProperty() {
        return searchCriteria;
    }
    
    public String getSearchPattern() {
        return searchPattern.get().getPattern(searchCriteria.get());
    }
}
