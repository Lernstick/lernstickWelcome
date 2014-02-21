package ch.fhnw.lernstickwelcome;

import ch.fhnw.lernstickwelcome.IPTableEntry.Protocol;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import tools.PreferredSizesTableModel;

/**
 * the table model for the iptables table
 *
 * @author ronny
 */
public class IPTableModel extends PreferredSizesTableModel {

    private static final Logger LOGGER
            = Logger.getLogger(IPTableModel.class.getName());
    private static final ResourceBundle BUNDLE
            = ResourceBundle.getBundle("ch/fhnw/lernstickwelcome/Bundle");
    private final List<IPTableEntry> entries;

    /**
     * creates a new IPTableModel
     *
     * @param table the table where we want to set perfect sizes
     * @param maxDimension the maximum dimensions
     */
    public IPTableModel(JTable table, Dimension maxDimension) {
        super(table, maxDimension);

        entries = new ArrayList<IPTableEntry>();

        addColumn(BUNDLE.getString("Protocol"));
        addColumn(BUNDLE.getString("Target"));
        addColumn(BUNDLE.getString("Port"));
        addColumn(BUNDLE.getString("Description"));

        initSizes();
    }

    @Override
    public int getRowCount() {
        if (entries == null) {
            return 0;
        }
        return entries.size();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0:
                // Protocol (TCP / UDP)
                return Protocol.class;
            case 1:
                // target (IP address or hostname)
                return String.class;
            case 2:
                // port (0..65535) or port range (<minport>:<maxport>)
                return String.class;
            case 3:
                // description
                return String.class;
            default:
                LOGGER.log(Level.WARNING, "column {0} not supported", column);
                return null;
        }
    }

    @Override
    public Object getValueAt(int row, int column) {
        IPTableEntry entry = entries.get(row);
        switch (column) {
            case 0:
                return entry.getProtocol().toString();
            case 1:
                return entry.getTarget();
            case 2:
                return entry.getPortRange();
            case 3:
                return entry.getDescription();
            default:
                LOGGER.log(Level.WARNING, "column {0} not supported", column);
                return null;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        IPTableEntry entry = entries.get(row);
        switch (column) {
            case 0:
                Protocol protocol = (Protocol) value;
                entry.setProtocol(protocol);
                break;

            case 1:
                String target = (String) value;
                entry.setTarget(target);
                break;

            case 2:
                String portRange = (String) value;
                entry.setPortRange(portRange);
                break;

            case 3:
                String description = (String) value;
                entry.setDescription(description);
                break;

            default:
                LOGGER.log(Level.WARNING, "column {0} not supported", column);
        }
    }

    @Override
    public void removeRow(int row) {
        entries.remove(row);
        fireTableRowsDeleted(row, row);
    }

    public void addEntry() {
        addEntry(new IPTableEntry(Protocol.TCP, "", "", ""));
    }

    public void addEntry(IPTableEntry entry) {
        entries.add(entry);
        int newRow = entries.size() - 1;
        fireTableRowsInserted(newRow, newRow);
    }

    public void moveEntries(boolean up) {
        int[] selectedRows = table.getSelectedRows();
        int length = selectedRows.length;
        if (up) {
            // move up
            for (int i = 0; i < length; i++) {
                int index = selectedRows[i];
                entries.add(index - 1, entries.remove(index));
            }
            fireTableRowsUpdated(selectedRows[0] - 1, selectedRows[length - 1]);
            for (int i = 0; i < length; i++) {
                selectedRows[i]--;
            }
        } else {
            // move down
            for (int i = length - 1; i >= 0; i--) {
                int index = selectedRows[i];
                entries.add(index + 1, entries.remove(index));
            }
            fireTableRowsUpdated(selectedRows[0], selectedRows[length - 1] + 1);
            for (int i = 0; i < length; i++) {
                selectedRows[i]++;
            }
        }

        // restore selection
        ListSelectionModel listSelectionModel = table.getSelectionModel();
        listSelectionModel.clearSelection();
        for (int i = 0; i < length; i++) {
            int index = selectedRows[i];
            listSelectionModel.addSelectionInterval(index, index);
        }
    }
}
