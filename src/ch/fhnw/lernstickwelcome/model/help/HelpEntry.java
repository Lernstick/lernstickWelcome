/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.help;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sschw
 */
public class HelpEntry implements Comparable<HelpEntry> {
    private String title;
    private String path;
    private int index;
    private List<HelpEntry> entries = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }
    
    public int getIndex() {
        return index;
    }

    public List<HelpEntry> getSubEntries() {
        return entries;
    }

    @Override
    public String toString() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPath(String path) {
        this.path = path;
    }
    
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public int compareTo(HelpEntry o) {
        return index - o.index;
    }
    
}
