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
 * Describes a single website or pattern for a website which should be 
 * whitelisted.
 * @author sschw
 */
public class WebsiteFilter {
    /**
     * The predefined SearchPatterns which should help the user to configure websites.
     * <br>
     * The classes are constructed using a prefix and postfix which are wrapped
     * around the {@link #searchCriteria}.
     * <br>
     * {@link #getPattern(java.lang.String) } returns the string formatted with
     * the SearchPattern. If the SearchPattern isn't custom, all RegEx Literals 
     * are escaped.
     */
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
                searchCriteria = searchCriteria.replaceAll("([.^$*+?()\\[{\\\\|])", "\\\\$1"); // Pattern.quote doesnt work
            return pre + searchCriteria + post;
        }
        
        @Override
        public String toString() {
            switch(this) {
                case Exact: return "welcomeApplicationFirewall.filterExact";
                case Contains: return "welcomeApplicationFirewall.filterContains";
                case StartsWith: return "welcomeApplicationFirewall.filterStartsWith";
                case Custom: return "welcomeApplicationFirewall.filterCustom";
                default: return null;
            }
        }
    }
    
    private ObjectProperty<SearchPattern> searchPattern = new SimpleObjectProperty<>();
    private StringProperty searchCriteria = new SimpleStringProperty();
    
    /**
     * Creates a website filter from a whitelist entry.
     * <dl>
     * <dt>If the String has unescaped Regex Literals.</dt>
     * <dd>{@link SearchPattern#Custom}</dd>
     * <dt>If the String startsWith ^</dt>
     * <dd>Ends with $, {@link SearchPattern#Exact}<br>
     * Else, {@link SearchPattern#StartsWith}</dd>
     * <dt>Else</dt>
     * <dd>{@link SearchPattern#Contains}</dd>
     * </dl>
     * @param line 
     */
    public WebsiteFilter(String line) {
        // Set correct searchPattern according to the found RegEx.
        if(line.matches("[^\\\\][.*+?()\\[{\\\\|]")) { // Search for unescaped RegEx (without ^ and $)
            searchPattern.set(SearchPattern.Custom);
            searchCriteria.setValue(line);
        } else if(line.startsWith("^")) { // ^ at the beginning
            if(line.endsWith("$")) { // $ at the end
                searchPattern.set(SearchPattern.Exact);
                searchCriteria.setValue(line.substring(1, line.length() - 1).replaceAll("\\\\", ""));
            } else {
                searchPattern.set(SearchPattern.StartsWith);
                searchCriteria.setValue(line.substring(1).replaceAll("\\\\", ""));
            }
        } else {
            searchPattern.set(SearchPattern.Contains);
            searchCriteria.setValue(line.replaceAll("\\\\", ""));
        }
    }
    
    /**
     * Creates a website filter from the search pattern and search criteria
     * @param pattern the Search Pattern
     * @param criteria the Search Criteria (Website)
     */
    public WebsiteFilter(SearchPattern pattern, String criteria) {
        searchPattern.set(pattern);
        searchCriteria.set(criteria);
    }
    
    public ObjectProperty<SearchPattern> searchPatternProperty() {
        return searchPattern;
    }
    
    public StringProperty searchCriteriaProperty() {
        return searchCriteria;
    }
    
    /**
     * Returns the SearchPattern which should be written into the whitelist.
     * @return the searchCriteria - formatted with {@link SearchPattern#getPattern(java.lang.String) }
     */
    public String getSearchPattern() {
        return searchPattern.get().getPattern(searchCriteria.get());
    }
}
