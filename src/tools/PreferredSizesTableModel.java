/*
 * PreferredSizesTableModel.java
 *
 * Created on 11. April 2002, 08:19
 */
package tools;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * a table model which remembers the perfect sizes for all its cells and sets
 * the preferred sizes at the referenced table as necessary
 *
 * TODO: - fireTableCellUpdated(int row, int column)
 *
 * - insert(int firstRow, int lastRow) - change column widths only if new ones
 * are wider - remove() - change column widths only if old ones where the widest
 *
 * - column methods
 *
 * @author Ronny.Standtke@gmx.net
 */
public class PreferredSizesTableModel extends DefaultTableModel {

    protected final JTable table;
    private final Dimension maxDimension;
    // caches the table cell sizes for faster resizing operations
    // a "dimension cache matrix"
    private Dimension[][] dimensionCache;

    /**
     * Creates a new instance of PreferredSizesTableModel
     *
     * @param maxDimension the maximum dimensions
     * @param table the table where we want to set perfect sizes
     */
    public PreferredSizesTableModel(JTable table, Dimension maxDimension) {
        this.table = table;
        this.maxDimension = maxDimension;

        table.setModel(this);
    }

    /**
     * initializes all sizes
     */
    public void initSizes() {

        // initialize our dimension cache matrix
        int columns = getColumnCount();
        int rows = getRowCount();
        dimensionCache = new Dimension[columns][rows + 1];

        updateHeaderDimensions();
        updateCellDimensions(0, rows - 1);
        resetTableColumnWidths();
        resetViewPortSize();
    }

    /**
     * catches reloading of table data
     */
    @Override
    public void fireTableDataChanged() {

        super.fireTableDataChanged();

        // re-initialize our dimension cache matrix
        int columns = getColumnCount();
        int rows = getRowCount();
        dimensionCache = new Dimension[columns][rows + 1];

        updateHeaderDimensions();
        updateCellDimensions(0, rows - 1);
        resetTableColumnWidths();
        resetViewPortSize();
    }

    private void updateHeaderDimensions() {
        TableColumnModel columnModel = table.getColumnModel();
        TableCellRenderer headerRenderer =
                table.getTableHeader().getDefaultRenderer();

        for (int column = 0, columns = getColumnCount(); column < columns; column++) {

            Object headerValue = columnModel.getColumn(column).getHeaderValue();

            Component header = headerRenderer.getTableCellRendererComponent(
                    table, headerValue, false, false, 0, column);

            // the header gets stored at the row index "0"
            dimensionCache[column][0] = header.getPreferredSize();
        }
    }

    private void updateCellDimensions(int firstRow, int lastRow) {
        TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0, columns = getColumnCount(); column < columns; column++) {

            // get working renderer
            TableCellRenderer renderer =
                    columnModel.getColumn(column).getCellRenderer();
            if (renderer == null) {
                renderer = table.getDefaultRenderer(getColumnClass(column));
            }

            // get cell sizes & store them in internal array
            for (int row = firstRow; row <= lastRow; row++) {
                Object object = getValueAt(row, column);
                Component component = renderer.getTableCellRendererComponent(
                        table, object, false, false, row, column);
                Dimension cellSize = component.getPreferredSize();
                // the "+1" is because we also store the header dimension IN
                // cellDimensionList
                dimensionCache[column][row + 1] = cellSize;
            }
        }

        resetTableRowHeights(firstRow, lastRow);
    }

    private void resetTableRowHeights(int firstRow, int lastRow) {
        // collect max sizes
        int[] maxHeights = new int[lastRow - firstRow + 1];
        for (int column = 0, columns = getColumnCount(); column < columns; column++) {
            for (int row = firstRow; row <= lastRow; row++) {
                Dimension cellDimension = dimensionCache[column][row + 1];
                int arrayIndex = row - firstRow;
                maxHeights[arrayIndex] =
                        Math.max(maxHeights[arrayIndex], cellDimension.height);
            }
        }

        // set max sizes
        for (int row = firstRow; row <= lastRow; row++) {
            table.setRowHeight(row, maxHeights[row - firstRow]);
        }
    }

    private void resetTableColumnWidths() {
        int rows = getRowCount();
        TableColumnModel columnModel = table.getColumnModel();

        for (int column = 0, columns = getColumnCount(); column < columns; column++) {
            TableColumn tableColumn = columnModel.getColumn(column);
            // check all cell dimensions (including header)
            int preferredColumnWidth = 0;
            for (int row = 0; row < rows + 1; row++) {
                Dimension cellSize = dimensionCache[column][row];
                preferredColumnWidth =
                        Math.max(preferredColumnWidth, cellSize.width);
            }
            tableColumn.setPreferredWidth(preferredColumnWidth + 5);
        }
    }

    private void resetViewPortSize() {
        int perfectTableWidth = 0;
        int perfectTableHeight = 0;

        int columns = getColumnCount();
        TableColumnModel columnModel = table.getColumnModel();

        for (int column = 0; column < columns; column++) {
            perfectTableWidth +=
                    columnModel.getColumn(column).getPreferredWidth();
        }
        for (int row = 0, rows = getRowCount(); row < rows; row++) {
            perfectTableHeight += table.getRowHeight(row);
        }

        // add some pixels for every column we have
        perfectTableWidth =
                Math.min(perfectTableWidth + columns * 10, maxDimension.width);
        // add some height pixels too
        perfectTableHeight =
                Math.min(perfectTableHeight + 5, maxDimension.height);

        table.setPreferredScrollableViewportSize(
                new Dimension(perfectTableWidth, perfectTableHeight));
    }
}
