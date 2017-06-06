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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single help file which can be loaded.
 * <br>
 * Help entries can be ordered by its index.
 * @author sschw
 */
public class HelpEntry implements Comparable<HelpEntry> {
    private String title;
    private String path;
    private int index;
    private final List<HelpEntry> entries = new ArrayList<>();

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
    public int hashCode() {
        // Could be changed to path != null ? path.hashCode() : super.hashCode()
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof HelpEntry) {
            // If the help entry has same path, the help entries are the same.
            // If path is null the help entries can't be compared.
            return path != null && path.equals(((HelpEntry) obj).getPath());
        }
        return false;
    }

    @Override
    public int compareTo(HelpEntry o) {
        return index - o.index;
    }
    
}
