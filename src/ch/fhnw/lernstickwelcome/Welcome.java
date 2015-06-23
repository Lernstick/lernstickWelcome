/*
 * Welcome.java
 *
 * Created on 01.04.2009, 14:11:23
 */
package ch.fhnw.lernstickwelcome;

import ch.fhnw.lernstickwelcome.IPTableEntry.Protocol;
import ch.fhnw.util.MountInfo;
import ch.fhnw.util.Partition;
import ch.fhnw.util.ProcessExecutor;
import ch.fhnw.util.StorageDevice;
import ch.fhnw.util.StorageTools;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.text.AbstractDocument;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.freedesktop.dbus.exceptions.DBusException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The welcome window of the lernstick
 *
 * @author Ronny Standtke <Ronny.Standtke@gmx.net>
 */
public class Welcome extends javax.swing.JFrame {

    private static final Logger LOGGER
            = Logger.getLogger(Welcome.class.getName());
    private static final ResourceBundle BUNDLE
            = ResourceBundle.getBundle("ch/fhnw/lernstickwelcome/Bundle");
    private static final String SHOW_WELCOME = "ShowWelcome";
    private static final String SHOW_READ_ONLY_INFO = "ShowReadOnlyInfo";
    private static final String BACKUP = "Backup";
    private static final String BACKUP_SOURCE = "BackupSource";
    private static final String BACKUP_DIRECTORY_ENABLED = "BackupDirectoryEnabled";
    private static final String BACKUP_DIRECTORY = "BackupDirectory";
    private static final String BACKUP_PARTITION_ENABLED = "BackupPartitionEnabled";
    private static final String BACKUP_PARTITION = "BackupPartition";
    private static final String BACKUP_SCREENSHOT = "BackupScreenshot";
    private static final String BACKUP_FREQUENCY = "BackupFrequency";
    private static final String EXCHANGE_ACCESS = "ExchangeAccess";
    private static final String KDE_LOCK = "LockKDE";
    // !!! NO trailing slash at the end (would break comparison later) !!!
    private static final String IMAGE_DIRECTORY = "/lib/live/mount/medium";
    private static final String IP_TABLES_FILENAME
            = "/etc/lernstick-firewall/net_whitelist";
    private static final String URL_WHITELIST_FILENAME
            = "/etc/lernstick-firewall/url_whitelist";
    // !!! processExecutor must be instanciated before the next constants !!!
    private static final ProcessExecutor processExecutor
            = new ProcessExecutor();
    private static final boolean IMAGE_IS_WRITABLE = isImageWritable();
    // mapping of checkboxes to package collections
    // "ttf-pelikan-schulschriften" are currently unavailable
    private static final String[] FONTS_PACKAGES = new String[]{
        "ttf-mscorefonts-installer"
    };
    // "mplayer-codecs" are currently unavailable
    private static final String[] MULTIMEDIA_PACKAGES = new String[]{
        "libdvdcss2", "libmp3lame0", "lame"
    };
    private static final String[] FLASH_PACKAGES = new String[]{
        "flashplugin-nonfree", "pepperflashplugin-nonfree"
    };
    private static final String USER_HOME = System.getProperty("user.home");
    private static final Path APPLETS_CONFIG_FILE = Paths.get(
            "/home/user/.kde/share/config/plasma-desktop-appletsrc");
    private static final Path ALSA_PULSE_CONFIG_FILE = Paths.get(
            "/usr/share/alsa/alsa.conf.d/pulse.conf");
    private final File propertiesFile;
    private final Properties properties;
    private final Toolkit toolkit = Toolkit.getDefaultToolkit();
    private final DefaultListModel menuListModel = new DefaultListModel();
    private final boolean examEnvironment;
    private String fullName;
    private int menuListIndex = 0;
    private StorageDevice systemStorageDevice;
    private Partition exchangePartition;
    private String exchangePartitionLabel;
    private Partition bootPartition;
    private MountInfo bootMountInfo;
    private String aptGetOutput;
    private IPTableModel ipTableModel;
    private MainMenuListEntry firewallEntry;
    private MainMenuListEntry backupEntry;
    private boolean firewallRunning;

    /**
     * Creates new form Welcome
     *
     * @param examEnvironment if <tt>true</tt>, show the version for the exam
     * environment, otherwise for the learning environment
     */
    public Welcome(boolean examEnvironment) {
        this.examEnvironment = examEnvironment;

        // log everything...
        Logger globalLogger = Logger.getLogger("ch.fhnw");
        globalLogger.setLevel(Level.ALL);
        SimpleFormatter formatter = new SimpleFormatter();

        // log to console
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(formatter);
        consoleHandler.setLevel(Level.ALL);
        globalLogger.addHandler(consoleHandler);

        // log into a rotating temporaty file of max 5 MB
        try {
            FileHandler fileHandler
                    = new FileHandler("%t/lernstickWelcome", 5000000, 2, true);
            fileHandler.setFormatter(formatter);
            fileHandler.setLevel(Level.ALL);
            globalLogger.addHandler(fileHandler);
        } catch (IOException | SecurityException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        LOGGER.info("*********** Starting lernstick Welcome ***********");

        initComponents();
        ToolTipManager.sharedInstance().setDismissDelay(60000);
        setBordersEnabled(false);

        // load and apply all properties
        properties = new Properties();
        propertiesFile = new File("/etc/lernstickWelcome");
        try {
            properties.load(new FileInputStream(propertiesFile));
        } catch (IOException ex) {
            LOGGER.log(Level.INFO,
                    "can not load properties from " + propertiesFile, ex);
        }
        backupCheckBox.setSelected("true".equals(
                properties.getProperty(BACKUP)));
        backupSourceTextField.setText(properties.getProperty(
                BACKUP_SOURCE, "/home/user/"));
        backupDirectoryCheckBox.setSelected("true".equals(
                properties.getProperty(BACKUP_DIRECTORY_ENABLED, "true")));
        backupPartitionCheckBox.setSelected("true".equals(
                properties.getProperty(BACKUP_PARTITION_ENABLED)));
        backupPartitionTextField.setText(
                properties.getProperty(BACKUP_PARTITION));
        screenShotCheckBox.setSelected("true".equals(
                properties.getProperty(BACKUP_SCREENSHOT)));
        exchangeAccessCheckBox.setSelected("true".equals(
                properties.getProperty(EXCHANGE_ACCESS)));
        kdePlasmaLockCheckBox.setSelected("true".equals(
                properties.getProperty(KDE_LOCK)));
        String frequencyString = properties.getProperty(
                BACKUP_FREQUENCY, "5");
        try {
            backupFrequencySpinner.setValue(new Integer(frequencyString));
        } catch (NumberFormatException ex) {
            LOGGER.log(Level.WARNING,
                    "could not parse backup frequency \"{0}\"",
                    frequencyString);
        }
        readWriteCheckBox.setSelected("true".equals(
                properties.getProperty(SHOW_WELCOME)));
        readOnlyCheckBox.setSelected("true".equals(
                properties.getProperty(SHOW_READ_ONLY_INFO, "true")));

        menuList.setModel(menuListModel);

        menuListModel.addElement(new MainMenuListEntry(
                "/ch/fhnw/lernstickwelcome/icons/messagebox_info.png",
                BUNDLE.getString("Information"), "infoPanel"));
        if (examEnvironment) {
            menuListModel.addElement(new MainMenuListEntry(
                    "/ch/fhnw/lernstickwelcome/icons/32x32/dialog-password.png",
                    BUNDLE.getString("Password"), "passwordChangePanel"));
            firewallEntry = new MainMenuListEntry(
                    "/ch/fhnw/lernstickwelcome/icons/32x32/firewall.png",
                    BUNDLE.getString("Firewall"), "firewallPanel");
            menuListModel.addElement(firewallEntry);
            backupEntry = new MainMenuListEntry(
                    "/ch/fhnw/lernstickwelcome/icons/32x32/backup.png",
                    BUNDLE.getString("Backup"), "backupPanel");
            menuListModel.addElement(backupEntry);
        } else {
            menuListModel.addElement(new MainMenuListEntry(
                    "/ch/fhnw/lernstickwelcome/icons/32x32/copyright.png",
                    BUNDLE.getString("Nonfree_Software"), "nonfreePanel"));
            menuListModel.addElement(new MainMenuListEntry(
                    "/ch/fhnw/lernstickwelcome/icons/32x32/LinuxAdvanced.png",
                    BUNDLE.getString("Teaching_System"), "teachingPanel"));
            menuListModel.addElement(new MainMenuListEntry(
                    "/ch/fhnw/lernstickwelcome/icons/32x32/list-add.png",
                    BUNDLE.getString("Additional_Applications"), "additionalPanel"));
            menuListModel.addElement(new MainMenuListEntry(
                    "/ch/fhnw/lernstickwelcome/icons/32x32/network-server.png",
                    BUNDLE.getString("Proxy"), "proxyPanel"));
            exchangeAccessCheckBox.setVisible(false);
            exchangeRebootLabel.setVisible(false);

            checkAllPackages();
        }
        menuListModel.addElement(new MainMenuListEntry(
                "/ch/fhnw/lernstickwelcome/icons/32x32/system-run.png",
                BUNDLE.getString("System"), "systemPanel"));
        menuListModel.addElement(new MainMenuListEntry(
                "/ch/fhnw/lernstickwelcome/icons/32x32/partitionmanager.png",
                BUNDLE.getString("Partitions"), "partitionsPanel"));

        menuList.setCellRenderer(new MyListCellRenderer());
        menuList.setSelectedIndex(0);

        getFullUserName();

        AbstractDocument exchangePartitionNameDocument
                = (AbstractDocument) exchangePartitionNameTextField.getDocument();
        exchangePartitionNameDocument.setDocumentFilter(
                new DocumentSizeFilter());

        try {
            systemStorageDevice = StorageTools.getSystemStorageDevice();
            if (systemStorageDevice != null) {
                exchangePartition = systemStorageDevice.getExchangePartition();
                bootPartition = systemStorageDevice.getBootPartition();
                if (bootPartition != null) {
                    bootMountInfo = bootPartition.mount();
                }
            }
            LOGGER.log(Level.INFO, "\nsystemStorageDevice: {0}\n"
                    + "exchangePartition: {1}\nbootPartition: {2}",
                    new Object[]{
                        systemStorageDevice, exchangePartition, bootPartition});
        } catch (DBusException | IOException ex) {
            LOGGER.log(Level.SEVERE, "", ex);
        }

        if (exchangePartition == null) {
            exchangePartitionNameLabel.setEnabled(false);
            exchangePartitionNameTextField.setEnabled(false);
        } else {
            exchangePartitionLabel = exchangePartition.getIdLabel();
            exchangePartitionNameTextField.setText(exchangePartitionLabel);

            try {
                String exchangeMountPath = exchangePartition.getMountPath();
                LOGGER.log(Level.INFO,
                        "exchangeMountPath: {0}", exchangeMountPath);
                backupDirectoryTextField.setText(properties.getProperty(
                        BACKUP_DIRECTORY, exchangeMountPath + '/'
                        + BUNDLE.getString("Backup_Directory")));
            } catch (DBusException ex) {
                LOGGER.log(Level.SEVERE, "", ex);
            }
        }

        // *** determine some boot config properties ***
        // timeout
        ((JSpinner.DefaultEditor) bootTimeoutSpinner.getEditor()).getTextField().setColumns(2);
        ((JSpinner.DefaultEditor) backupFrequencySpinner.getEditor()).getTextField().setColumns(2);
        try {
            bootTimeoutSpinner.setValue(getTimeout());
        } catch (IOException | DBusException ex) {
            LOGGER.log(Level.WARNING, "could not set boot timeout value", ex);
        }
        updateSecondsLabel();
        // system strings
        String systemName = null;
        String systemVersion = null;
        try {
            File xmlBootConfigFile = getXmlBootConfigFile();
            if (xmlBootConfigFile != null) {
                Document xmlBootDocument = parseXmlFile(xmlBootConfigFile);
                xmlBootDocument.getDocumentElement().normalize();
                Node systemNode = xmlBootDocument.getElementsByTagName(
                        "system").item(0);
                Element systemElement = (Element) systemNode;
                Node node = systemElement.getElementsByTagName("text").item(0);
                if (node != null) {
                    systemName = node.getTextContent();
                }
                node = systemElement.getElementsByTagName("version").item(0);
                if (node != null) {
                    systemVersion = node.getTextContent();
                }
            }
        } catch (ParserConfigurationException | SAXException |
                IOException | DBusException ex) {
            LOGGER.log(Level.WARNING, "could not parse xmlboot config", ex);
        }
        systemNameTextField.setText(systemName);
        systemVersionTextField.setText(systemVersion);
        if (!IMAGE_IS_WRITABLE) {
            bootTimeoutSpinner.setEnabled(false);
            systemNameTextField.setEditable(false);
            systemVersionTextField.setEditable(false);
        }

        Image image = toolkit.getImage(getClass().getResource(
                "/ch/fhnw/lernstickwelcome/icons/messagebox_info.png"));
        setIconImage(image);

        Color background = UIManager.getDefaults().getColor("Panel.background");
        infoEditorPane.setBackground(background);
        teachingEditorPane.setBackground(background);

        // firewall tables
        ipTableModel = new IPTableModel(firewallIPTable,
                new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        firewallIPTable.setModel(ipTableModel);
        JComboBox protocolCombobox = new JComboBox();
        protocolCombobox.addItem(Protocol.TCP);
        protocolCombobox.addItem(Protocol.UDP);
        TableColumn protocolColumn
                = firewallIPTable.getColumnModel().getColumn(0);
        protocolColumn.setCellEditor(new DefaultCellEditor(protocolCombobox));
        firewallIPTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        if (e.getValueIsAdjusting()) {
                            return;
                        }
                        int[] selectedRows = firewallIPTable.getSelectedRows();
                        boolean selected = selectedRows.length > 0;
                        removeIPButton.setEnabled(selected);
                        moveUpIPButton.setEnabled(selected && selectedRows[0] > 0);
                        moveDownIPButton.setEnabled(selected
                                && (selectedRows[selectedRows.length - 1]
                                < ipTableModel.getRowCount() - 1));
                    }
                });

        try {
            parseNetWhiteList();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "", ex);
        }

        try {
            parseURLWhiteList();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "", ex);
        }

        // start periodic firewall status check
        javax.swing.Timer firewallStatusTimer = new javax.swing.Timer(
                3000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        updateFirewallState();
                    }
                });
        firewallStatusTimer.setInitialDelay(0);
        firewallStatusTimer.start();

        noPulseAudioCheckbox.setSelected(!Files.exists(ALSA_PULSE_CONFIG_FILE));

        helpTextPane.setCaretPosition(0);

        // fix some size issues
        infoScrollPane.setMinimumSize(infoScrollPane.getPreferredSize());
        nonfreeLabel.setMinimumSize(nonfreeLabel.getPreferredSize());
        teachingScrollPane.setMinimumSize(
                teachingScrollPane.getPreferredSize());
        pack();

        Dimension preferredSize = getPreferredSize();
        preferredSize.height = 450;
        setSize(preferredSize);
        // center on screen
        setLocationRelativeTo(null);

        // enforce minimal size of list
        menuScrollPane.setMinimumSize(menuScrollPane.getPreferredSize());

        setVisible(true);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        boolean examEnvironment = false;
        for (String arg : args) {
            if (arg.equals("examEnvironment")) {
                examEnvironment = true;
                break;
            }
        }
        final boolean examEnv = examEnvironment;

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Welcome(examEnv);
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        menuScrollPane = new javax.swing.JScrollPane();
        menuList = new javax.swing.JList();
        mainCardPanel = new javax.swing.JPanel();
        infoPanel = new javax.swing.JPanel();
        welcomeLabel = new javax.swing.JLabel();
        infoScrollPane = new javax.swing.JScrollPane();
        infoEditorPane = new javax.swing.JEditorPane();
        passwordChangePanel = new javax.swing.JPanel();
        passwordChangeInfoLabel = new javax.swing.JLabel();
        label1 = new javax.swing.JLabel();
        passwordField1 = new javax.swing.JPasswordField();
        label2 = new javax.swing.JLabel();
        passwordField2 = new javax.swing.JPasswordField();
        passwordChangeButton = new javax.swing.JButton();
        backupPanel = new javax.swing.JPanel();
        backupCheckBox = new javax.swing.JCheckBox();
        backupFrequencyEveryLabel = new javax.swing.JLabel();
        backupFrequencySpinner = new javax.swing.JSpinner();
        backupFrequencyMinuteLabel = new javax.swing.JLabel();
        backupSourcePanel = new javax.swing.JPanel();
        backupSourceLabel = new javax.swing.JLabel();
        backupSourceTextField = new javax.swing.JTextField();
        backupSourceButton = new javax.swing.JButton();
        backupDestinationsPanel = new javax.swing.JPanel();
        backupDirectoryCheckBox = new javax.swing.JCheckBox();
        backupDirectoryLabel = new javax.swing.JLabel();
        backupDirectoryTextField = new javax.swing.JTextField();
        backupDirectoryButton = new javax.swing.JButton();
        backupPartitionCheckBox = new javax.swing.JCheckBox();
        backupPartitionLabel = new javax.swing.JLabel();
        backupPartitionTextField = new javax.swing.JTextField();
        screenShotCheckBox = new javax.swing.JCheckBox();
        nonfreePanel = new javax.swing.JPanel();
        nonfreeLabel = new javax.swing.JLabel();
        recommendedPanel = new javax.swing.JPanel();
        flashCheckBox = new javax.swing.JCheckBox();
        flashLabel = new javax.swing.JLabel();
        additionalFontsCheckBox = new javax.swing.JCheckBox();
        fontsLabel = new javax.swing.JLabel();
        multimediaCheckBox = new javax.swing.JCheckBox();
        multimediaLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        miscPanel = new javax.swing.JPanel();
        googleEarthCheckBox = new javax.swing.JCheckBox();
        googleEarthLabel = new javax.swing.JLabel();
        skypeCheckBox = new javax.swing.JCheckBox();
        skypeLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        fillPanel = new javax.swing.JPanel();
        teachingPanel = new javax.swing.JPanel();
        teachingScrollPane = new javax.swing.JScrollPane();
        teachingEditorPane = new javax.swing.JEditorPane();
        laCheckBox = new javax.swing.JCheckBox();
        laLabel = new javax.swing.JLabel();
        additionalPanel = new javax.swing.JPanel();
        additionalInfoLabel = new javax.swing.JLabel();
        additionalTabbedPane = new javax.swing.JTabbedPane();
        additionalScrollPane = new javax.swing.JScrollPane();
        additionalMiscPanel = new ScrollableJPanel();
        netbeansPanel = new ch.fhnw.lernstickwelcome.GamePanel();
        processingPanel = new ch.fhnw.lernstickwelcome.GamePanel();
        rStudioPanel = new ch.fhnw.lernstickwelcome.GamePanel();
        lazarusPanel = new ch.fhnw.lernstickwelcome.GamePanel();
        openClipartPanel = new ch.fhnw.lernstickwelcome.GamePanel();
        sweetHome3DPanel = new ch.fhnw.lernstickwelcome.GamePanel();
        gnucashPanel = new ch.fhnw.lernstickwelcome.GamePanel();
        rosegardenPanel = new ch.fhnw.lernstickwelcome.GamePanel();
        wizbeePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        calcularisPanel = new ch.fhnw.lernstickwelcome.GamePanel();
        lehrerOfficePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        gamesScrollPane = new javax.swing.JScrollPane();
        gamesScrollPanel = new ScrollableJPanel();
        colobotGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        riliGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        filletsGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        neverballGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        neverputtGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        freecolGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        minetestGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        frogattoGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        supertuxGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        supertuxkartGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        xmotoGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        triggerGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        openClonkPanel = new ch.fhnw.lernstickwelcome.GamePanel();
        wesnothGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        flareGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        hedgewarsGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        megaglestGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        astromenaceGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        proxyPanel = new javax.swing.JPanel();
        proxyCheckBox = new javax.swing.JCheckBox();
        proxyHostLabel = new javax.swing.JLabel();
        proxyHostTextField = new javax.swing.JTextField();
        proxyPortLabel = new javax.swing.JLabel();
        proxyPortTextField = new javax.swing.JFormattedTextField();
        proxyUserNameLabel = new javax.swing.JLabel();
        proxyUserNameTextField = new javax.swing.JTextField();
        proxyPasswordLabel = new javax.swing.JLabel();
        proxyPasswordField = new javax.swing.JPasswordField();
        proxyInfoLabel = new javax.swing.JLabel();
        systemPanel = new javax.swing.JPanel();
        bootMenuPanel = new javax.swing.JPanel();
        bootTimeoutLabel = new javax.swing.JLabel();
        bootTimeoutSpinner = new javax.swing.JSpinner();
        secondsLabel = new javax.swing.JLabel();
        systemNameLabel = new javax.swing.JLabel();
        systemNameTextField = new javax.swing.JTextField();
        systemVersionLabel = new javax.swing.JLabel();
        systemVersionTextField = new javax.swing.JTextField();
        userNameLabel = new javax.swing.JLabel();
        userNameTextField = new javax.swing.JTextField();
        kdePlasmaLockCheckBox = new javax.swing.JCheckBox();
        noPulseAudioCheckbox = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        partitionsPanel = new javax.swing.JPanel();
        exchangePartitionPanel = new javax.swing.JPanel();
        exchangePartitionNameLabel = new javax.swing.JLabel();
        exchangePartitionNameTextField = new javax.swing.JTextField();
        exchangeAccessCheckBox = new javax.swing.JCheckBox();
        exchangeRebootLabel = new javax.swing.JLabel();
        dataPartitionPanel = new javax.swing.JPanel();
        readWritePanel = new javax.swing.JPanel();
        readWriteCheckBox = new javax.swing.JCheckBox();
        readOnlyPanel = new javax.swing.JPanel();
        readOnlyCheckBox = new javax.swing.JCheckBox();
        firewallPanel = new javax.swing.JPanel();
        firewallInfoLabel = new javax.swing.JLabel();
        firewallStartStopButton = new javax.swing.JButton();
        firewallStatusLabel = new javax.swing.JLabel();
        firewallTabbedPane = new javax.swing.JTabbedPane();
        firewallipv4Panel = new javax.swing.JPanel();
        firewallIPButtonPanel = new javax.swing.JPanel();
        addIPButton = new javax.swing.JButton();
        removeIPButton = new javax.swing.JButton();
        moveUpIPButton = new javax.swing.JButton();
        moveDownIPButton = new javax.swing.JButton();
        firewallIPScrollPane = new javax.swing.JScrollPane();
        firewallIPTable = new javax.swing.JTable();
        firewallURLPanel = new javax.swing.JPanel();
        firewallURLScrollPane = new javax.swing.JScrollPane();
        firewallURLTextArea = new javax.swing.JTextArea();
        helpScrollPane = new javax.swing.JScrollPane();
        helpTextPane = new javax.swing.JTextPane();
        bottomPanel = new javax.swing.JPanel();
        navigaionPanel = new javax.swing.JPanel();
        previousButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        applyButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("ch/fhnw/lernstickwelcome/Bundle"); // NOI18N
        setTitle(bundle.getString("Welcome.title")); // NOI18N
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        menuList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        menuList.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                menuListMouseWheelMoved(evt);
            }
        });
        menuList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                menuListValueChanged(evt);
            }
        });
        menuScrollPane.setViewportView(menuList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        getContentPane().add(menuScrollPane, gridBagConstraints);

        mainCardPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        mainCardPanel.setLayout(new java.awt.CardLayout());

        infoPanel.setLayout(new java.awt.GridBagLayout());

        welcomeLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/lernstick_usb.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 0);
        infoPanel.add(welcomeLabel, gridBagConstraints);

        infoScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        infoEditorPane.setEditable(false);
        infoEditorPane.setContentType(bundle.getString("Welcome.editorPane.contentType")); // NOI18N
        infoEditorPane.setText(bundle.getString("Welcome.infoEditorPane.text")); // NOI18N
        infoEditorPane.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                infoEditorPaneHyperlinkUpdate(evt);
            }
        });
        infoScrollPane.setViewportView(infoEditorPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 20, 10);
        infoPanel.add(infoScrollPane, gridBagConstraints);

        mainCardPanel.add(infoPanel, "infoPanel");

        passwordChangePanel.setLayout(new java.awt.GridBagLayout());

        passwordChangeInfoLabel.setText(bundle.getString("Welcome.passwordChangeInfoLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        passwordChangePanel.add(passwordChangeInfoLabel, gridBagConstraints);

        label1.setText(bundle.getString("Welcome.label1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 0, 0);
        passwordChangePanel.add(label1, gridBagConstraints);

        passwordField1.setColumns(15);
        passwordField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordField1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 0, 5);
        passwordChangePanel.add(passwordField1, gridBagConstraints);

        label2.setText(bundle.getString("Welcome.label2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 0, 0);
        passwordChangePanel.add(label2, gridBagConstraints);

        passwordField2.setColumns(15);
        passwordField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordField2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 0, 5);
        passwordChangePanel.add(passwordField2, gridBagConstraints);

        passwordChangeButton.setText(bundle.getString("Welcome.passwordChangeButton.text")); // NOI18N
        passwordChangeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordChangeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        passwordChangePanel.add(passwordChangeButton, gridBagConstraints);

        mainCardPanel.add(passwordChangePanel, "passwordChangePanel");

        backupPanel.setLayout(new java.awt.GridBagLayout());

        backupCheckBox.setText(bundle.getString("Welcome.backupCheckBox.text")); // NOI18N
        backupCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                backupCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        backupPanel.add(backupCheckBox, gridBagConstraints);

        backupFrequencyEveryLabel.setText(bundle.getString("Welcome.backupFrequencyEveryLabel.text")); // NOI18N
        backupFrequencyEveryLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 0, 0);
        backupPanel.add(backupFrequencyEveryLabel, gridBagConstraints);

        backupFrequencySpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        backupFrequencySpinner.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 3, 0, 0);
        backupPanel.add(backupFrequencySpinner, gridBagConstraints);

        backupFrequencyMinuteLabel.setText(bundle.getString("Welcome.backupFrequencyMinuteLabel.text")); // NOI18N
        backupFrequencyMinuteLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 3, 0, 0);
        backupPanel.add(backupFrequencyMinuteLabel, gridBagConstraints);

        backupSourcePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Welcome.backupSourcePanel.border.title"))); // NOI18N
        backupSourcePanel.setLayout(new java.awt.GridBagLayout());

        backupSourceLabel.setText(bundle.getString("Welcome.backupSourceLabel.text")); // NOI18N
        backupSourceLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        backupSourcePanel.add(backupSourceLabel, gridBagConstraints);

        backupSourceTextField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 5, 10);
        backupSourcePanel.add(backupSourceTextField, gridBagConstraints);

        backupSourceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/16x16/document-open-folder.png"))); // NOI18N
        backupSourceButton.setEnabled(false);
        backupSourceButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        backupSourceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backupSourceButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 10);
        backupSourcePanel.add(backupSourceButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        backupPanel.add(backupSourcePanel, gridBagConstraints);

        backupDestinationsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Welcome.backupDestinationsPanel.border.title"))); // NOI18N
        backupDestinationsPanel.setLayout(new java.awt.GridBagLayout());

        backupDirectoryCheckBox.setText(bundle.getString("Welcome.backupDirectoryCheckBox.text")); // NOI18N
        backupDirectoryCheckBox.setEnabled(false);
        backupDirectoryCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                backupDirectoryCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        backupDestinationsPanel.add(backupDirectoryCheckBox, gridBagConstraints);

        backupDirectoryLabel.setText(bundle.getString("Welcome.backupDirectoryLabel.text")); // NOI18N
        backupDirectoryLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        backupDestinationsPanel.add(backupDirectoryLabel, gridBagConstraints);

        backupDirectoryTextField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        backupDestinationsPanel.add(backupDirectoryTextField, gridBagConstraints);

        backupDirectoryButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/16x16/document-open-folder.png"))); // NOI18N
        backupDirectoryButton.setEnabled(false);
        backupDirectoryButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        backupDirectoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backupDirectoryButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        backupDestinationsPanel.add(backupDirectoryButton, gridBagConstraints);

        backupPartitionCheckBox.setText(bundle.getString("Welcome.backupPartitionCheckBox.text")); // NOI18N
        backupPartitionCheckBox.setEnabled(false);
        backupPartitionCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                backupPartitionCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        backupDestinationsPanel.add(backupPartitionCheckBox, gridBagConstraints);

        backupPartitionLabel.setText(bundle.getString("Welcome.backupPartitionLabel.text")); // NOI18N
        backupPartitionLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 15, 5, 0);
        backupDestinationsPanel.add(backupPartitionLabel, gridBagConstraints);

        backupPartitionTextField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 5, 0);
        backupDestinationsPanel.add(backupPartitionTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        backupPanel.add(backupDestinationsPanel, gridBagConstraints);

        screenShotCheckBox.setText(bundle.getString("Welcome.screenShotCheckBox.text")); // NOI18N
        screenShotCheckBox.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 13, 0, 0);
        backupPanel.add(screenShotCheckBox, gridBagConstraints);

        mainCardPanel.add(backupPanel, "backupPanel");

        nonfreePanel.setLayout(new java.awt.GridBagLayout());

        nonfreeLabel.setText(bundle.getString("Welcome.nonfreeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 10);
        nonfreePanel.add(nonfreeLabel, gridBagConstraints);

        recommendedPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Welcome.recommendedPanel.border.title"))); // NOI18N
        recommendedPanel.setLayout(new java.awt.GridBagLayout());

        flashCheckBox.setSelected(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        recommendedPanel.add(flashCheckBox, gridBagConstraints);

        flashLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/Adobe_Flash_cs3.png"))); // NOI18N
        flashLabel.setText(bundle.getString("Welcome.flashLabel.text")); // NOI18N
        flashLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                flashLabelMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        recommendedPanel.add(flashLabel, gridBagConstraints);

        additionalFontsCheckBox.setSelected(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        recommendedPanel.add(additionalFontsCheckBox, gridBagConstraints);

        fontsLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/fonts.png"))); // NOI18N
        fontsLabel.setText(bundle.getString("Welcome.fontsLabel.text")); // NOI18N
        fontsLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fontsLabelMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        recommendedPanel.add(fontsLabel, gridBagConstraints);

        multimediaCheckBox.setSelected(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        recommendedPanel.add(multimediaCheckBox, gridBagConstraints);

        multimediaLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/package_multimedia.png"))); // NOI18N
        multimediaLabel.setText(bundle.getString("Welcome.multimediaLabel.text")); // NOI18N
        multimediaLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                multimediaLabelMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        recommendedPanel.add(multimediaLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        recommendedPanel.add(jLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 10, 5);
        nonfreePanel.add(recommendedPanel, gridBagConstraints);

        miscPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Welcome.miscPanel.border.title"))); // NOI18N
        miscPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        miscPanel.add(googleEarthCheckBox, gridBagConstraints);

        googleEarthLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/googleearth-icon.png"))); // NOI18N
        googleEarthLabel.setText(bundle.getString("Welcome.googleEarthLabel.text")); // NOI18N
        googleEarthLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                googleEarthLabelMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        miscPanel.add(googleEarthLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        miscPanel.add(skypeCheckBox, gridBagConstraints);

        skypeLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/skype.png"))); // NOI18N
        skypeLabel.setText(bundle.getString("Welcome.skypeLabel.text")); // NOI18N
        skypeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                skypeLabelMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        miscPanel.add(skypeLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        miscPanel.add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 5, 10, 10);
        nonfreePanel.add(miscPanel, gridBagConstraints);

        javax.swing.GroupLayout fillPanelLayout = new javax.swing.GroupLayout(fillPanel);
        fillPanel.setLayout(fillPanelLayout);
        fillPanelLayout.setHorizontalGroup(
            fillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 677, Short.MAX_VALUE)
        );
        fillPanelLayout.setVerticalGroup(
            fillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 87, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        nonfreePanel.add(fillPanel, gridBagConstraints);

        mainCardPanel.add(nonfreePanel, "nonfreePanel");

        teachingPanel.setLayout(new java.awt.GridBagLayout());

        teachingScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        teachingEditorPane.setEditable(false);
        teachingEditorPane.setContentType("text/html"); // NOI18N
        teachingEditorPane.setText(bundle.getString("Welcome.teachingEditorPane.text")); // NOI18N
        teachingEditorPane.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                teachingEditorPaneHyperlinkUpdate(evt);
            }
        });
        teachingScrollPane.setViewportView(teachingEditorPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        teachingPanel.add(teachingScrollPane, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        teachingPanel.add(laCheckBox, gridBagConstraints);

        laLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/LinuxAdvanced.png"))); // NOI18N
        laLabel.setText(bundle.getString("Welcome.laLabel.text")); // NOI18N
        laLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                laLabelMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        teachingPanel.add(laLabel, gridBagConstraints);

        mainCardPanel.add(teachingPanel, "teachingPanel");

        additionalPanel.setLayout(new java.awt.GridBagLayout());

        additionalInfoLabel.setText(bundle.getString("Welcome.additionalInfoLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 10);
        additionalPanel.add(additionalInfoLabel, gridBagConstraints);

        additionalMiscPanel.setLayout(new java.awt.GridBagLayout());

        netbeansPanel.setDescription(bundle.getString("Welcome.netbeansPanel.description")); // NOI18N
        netbeansPanel.setGameName("NetBeans"); // NOI18N
        netbeansPanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/netbeans.png"))); // NOI18N
        netbeansPanel.setWebsite("https://netbeans.org"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        additionalMiscPanel.add(netbeansPanel, gridBagConstraints);

        processingPanel.setDescription(bundle.getString("Welcome.processingPanel.description")); // NOI18N
        processingPanel.setGameName("Processing"); // NOI18N
        processingPanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/processing.png"))); // NOI18N
        processingPanel.setWebsite("http://processing.org"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        additionalMiscPanel.add(processingPanel, gridBagConstraints);

        rStudioPanel.setDescription(bundle.getString("Welcome.rStudioPanel.description")); // NOI18N
        rStudioPanel.setGameName("RStudio"); // NOI18N
        rStudioPanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/rstudio.png"))); // NOI18N
        rStudioPanel.setWebsite("http://www.rstudio.com"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        additionalMiscPanel.add(rStudioPanel, gridBagConstraints);

        lazarusPanel.setDescription(bundle.getString("Welcome.lazarusPanel.description")); // NOI18N
        lazarusPanel.setGameName("Lazarus"); // NOI18N
        lazarusPanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/lazarus.png"))); // NOI18N
        lazarusPanel.setWebsite("http://lazarus.freepascal.org"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        additionalMiscPanel.add(lazarusPanel, gridBagConstraints);

        openClipartPanel.setDescription(bundle.getString("Welcome.openClipartPanel.description")); // NOI18N
        openClipartPanel.setGameName("Openclipart"); // NOI18N
        openClipartPanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/openclipart.png"))); // NOI18N
        openClipartPanel.setWebsite("http://openclipart.org"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        additionalMiscPanel.add(openClipartPanel, gridBagConstraints);

        sweetHome3DPanel.setDescription(bundle.getString("Welcome.sweetHome3DPanel.description")); // NOI18N
        sweetHome3DPanel.setGameName("Sweet Home 3D"); // NOI18N
        sweetHome3DPanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/sweethome3d.png"))); // NOI18N
        sweetHome3DPanel.setWebsite("http://www.sweethome3d.com"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        additionalMiscPanel.add(sweetHome3DPanel, gridBagConstraints);

        gnucashPanel.setDescription(bundle.getString("Welcome.gnucashPanel.description")); // NOI18N
        gnucashPanel.setGameName("GnuCash"); // NOI18N
        gnucashPanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/gnucash.png"))); // NOI18N
        gnucashPanel.setWebsite("http://www.gnucash.org"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        additionalMiscPanel.add(gnucashPanel, gridBagConstraints);

        rosegardenPanel.setDescription(bundle.getString("Welcome.rosegardenPanel.description")); // NOI18N
        rosegardenPanel.setGameName("Rosegarden"); // NOI18N
        rosegardenPanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/rosegarden.png"))); // NOI18N
        rosegardenPanel.setWebsite("http://www.rosegardenmusic.com"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        additionalMiscPanel.add(rosegardenPanel, gridBagConstraints);

        wizbeePanel.setDescription(bundle.getString("Welcome.wizbeePanel.description")); // NOI18N
        wizbeePanel.setGameName("Wizbee"); // NOI18N
        wizbeePanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/wizbee.png"))); // NOI18N
        wizbeePanel.setWebsite("https://www.wizbee.ch"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        additionalMiscPanel.add(wizbeePanel, gridBagConstraints);

        calcularisPanel.setDescription(bundle.getString("Welcome.calcularisPanel.description")); // NOI18N
        calcularisPanel.setGameName("Dybuster Calcularis"); // NOI18N
        calcularisPanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/calcularis.png"))); // NOI18N
        calcularisPanel.setWebsite("http://www.calcularis.ch"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        additionalMiscPanel.add(calcularisPanel, gridBagConstraints);

        lehrerOfficePanel.setDescription(bundle.getString("Welcome.lehrerOfficePanel.description")); // NOI18N
        lehrerOfficePanel.setGameName("LehrerOffice"); // NOI18N
        lehrerOfficePanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/lehreroffice.png"))); // NOI18N
        lehrerOfficePanel.setWebsite("http://www.lehreroffice.ch"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        additionalMiscPanel.add(lehrerOfficePanel, gridBagConstraints);

        additionalScrollPane.setViewportView(additionalMiscPanel);

        additionalTabbedPane.addTab(bundle.getString("Welcome.additionalScrollPane.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/applications-other.png")), additionalScrollPane); // NOI18N

        gamesScrollPanel.setLayout(new java.awt.GridBagLayout());

        colobotGamePanel.setDescription(bundle.getString("Welcome.colobotGamePanel.description")); // NOI18N
        colobotGamePanel.setGameName("Colobot"); // NOI18N
        colobotGamePanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/colobot.png"))); // NOI18N
        colobotGamePanel.setWebsite("http://www.colobot.info"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        gamesScrollPanel.add(colobotGamePanel, gridBagConstraints);

        riliGamePanel.setDescription(bundle.getString("Welcome.riliGamePanel.description")); // NOI18N
        riliGamePanel.setGameName("Ri-li"); // NOI18N
        riliGamePanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/ri-li.png"))); // NOI18N
        riliGamePanel.setWebsite("http://ri-li.sourceforge.net"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        gamesScrollPanel.add(riliGamePanel, gridBagConstraints);

        filletsGamePanel.setDescription(bundle.getString("Welcome.filletsGamePanel.description")); // NOI18N
        filletsGamePanel.setGameName("Fish Fillets NG"); // NOI18N
        filletsGamePanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/fillets.png"))); // NOI18N
        filletsGamePanel.setWebsite("http://fillets.sourceforge.net"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        gamesScrollPanel.add(filletsGamePanel, gridBagConstraints);

        neverballGamePanel.setDescription(bundle.getString("Welcome.neverballGamePanel.description")); // NOI18N
        neverballGamePanel.setGameName("Neverball"); // NOI18N
        neverballGamePanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/neverball.png"))); // NOI18N
        neverballGamePanel.setWebsite("http://neverball.org"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        gamesScrollPanel.add(neverballGamePanel, gridBagConstraints);

        neverputtGamePanel.setDescription(bundle.getString("Welcome.neverputtGamePanel.description")); // NOI18N
        neverputtGamePanel.setGameName("Neverputt"); // NOI18N
        neverputtGamePanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/neverputt.png"))); // NOI18N
        neverputtGamePanel.setWebsite("http://neverball.org"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        gamesScrollPanel.add(neverputtGamePanel, gridBagConstraints);

        freecolGamePanel.setDescription(bundle.getString("Welcome.freecolGamePanel.description")); // NOI18N
        freecolGamePanel.setGameName("FreeCol"); // NOI18N
        freecolGamePanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/freecol.png"))); // NOI18N
        freecolGamePanel.setWebsite("http://www.freecol.org"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        gamesScrollPanel.add(freecolGamePanel, gridBagConstraints);

        minetestGamePanel.setDescription(bundle.getString("Welcome.minetestGamePanel.description")); // NOI18N
        minetestGamePanel.setGameName("Minetest"); // NOI18N
        minetestGamePanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/minetest.png"))); // NOI18N
        minetestGamePanel.setWebsite("http://minetest.net"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        gamesScrollPanel.add(minetestGamePanel, gridBagConstraints);

        frogattoGamePanel.setDescription(bundle.getString("Welcome.frogattoGamePanel.description")); // NOI18N
        frogattoGamePanel.setGameName("Frogatto & Friends"); // NOI18N
        frogattoGamePanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/frogatto.png"))); // NOI18N
        frogattoGamePanel.setWebsite("http://www.frogatto.com"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        gamesScrollPanel.add(frogattoGamePanel, gridBagConstraints);

        supertuxGamePanel.setDescription(bundle.getString("Welcome.supertuxGamePanel.description")); // NOI18N
        supertuxGamePanel.setGameName("SuperTux"); // NOI18N
        supertuxGamePanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/supertux.png"))); // NOI18N
        supertuxGamePanel.setWebsite("http://supertux.lethargik.org"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        gamesScrollPanel.add(supertuxGamePanel, gridBagConstraints);

        supertuxkartGamePanel.setDescription(bundle.getString("Welcome.supertuxkartGamePanel.description")); // NOI18N
        supertuxkartGamePanel.setGameName("SuperTuxKart"); // NOI18N
        supertuxkartGamePanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/supertuxkart.png"))); // NOI18N
        supertuxkartGamePanel.setWebsite("http://supertuxkart.sourceforge.net"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        gamesScrollPanel.add(supertuxkartGamePanel, gridBagConstraints);

        xmotoGamePanel.setDescription(bundle.getString("Welcome.xmotoGamePanel.description")); // NOI18N
        xmotoGamePanel.setGameName("XMoto"); // NOI18N
        xmotoGamePanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/xmoto.png"))); // NOI18N
        xmotoGamePanel.setWebsite("http://xmoto.tuxfamily.org"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        gamesScrollPanel.add(xmotoGamePanel, gridBagConstraints);

        triggerGamePanel.setDescription(bundle.getString("Welcome.triggerGamePanel.description")); // NOI18N
        triggerGamePanel.setGameName("Trigger Rally"); // NOI18N
        triggerGamePanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/trigger.png"))); // NOI18N
        triggerGamePanel.setWebsite("http://trigger-rally.sourceforge.net"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        gamesScrollPanel.add(triggerGamePanel, gridBagConstraints);

        openClonkPanel.setDescription(bundle.getString("Welcome.openClonkPanel.description")); // NOI18N
        openClonkPanel.setGameName("OpenClonk"); // NOI18N
        openClonkPanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/openclonk.png"))); // NOI18N
        openClonkPanel.setWebsite("http://www.openclonk.org"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        gamesScrollPanel.add(openClonkPanel, gridBagConstraints);

        wesnothGamePanel.setDescription(bundle.getString("Welcome.wesnothGamePanel.description")); // NOI18N
        wesnothGamePanel.setGameName("The Battle for Wesnoth"); // NOI18N
        wesnothGamePanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/wesnoth.png"))); // NOI18N
        wesnothGamePanel.setWebsite("http://wesnoth.org"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        gamesScrollPanel.add(wesnothGamePanel, gridBagConstraints);

        flareGamePanel.setDescription(bundle.getString("Welcome.flareGamePanel.description")); // NOI18N
        flareGamePanel.setGameName("FLARE"); // NOI18N
        flareGamePanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/flare.png"))); // NOI18N
        flareGamePanel.setWebsite("http://flarerpg.org"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        gamesScrollPanel.add(flareGamePanel, gridBagConstraints);

        hedgewarsGamePanel.setDescription(bundle.getString("Welcome.hedgewarsGamePanel.description")); // NOI18N
        hedgewarsGamePanel.setGameName("Hedgewars"); // NOI18N
        hedgewarsGamePanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/hedgewars.png"))); // NOI18N
        hedgewarsGamePanel.setWebsite("http://hedgewars.org"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        gamesScrollPanel.add(hedgewarsGamePanel, gridBagConstraints);

        megaglestGamePanel.setDescription(bundle.getString("Welcome.megaglestGamePanel.description")); // NOI18N
        megaglestGamePanel.setGameName("MegaGlest"); // NOI18N
        megaglestGamePanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/megaglest.png"))); // NOI18N
        megaglestGamePanel.setWebsite("http://megaglest.org"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        gamesScrollPanel.add(megaglestGamePanel, gridBagConstraints);

        astromenaceGamePanel.setDescription(bundle.getString("Welcome.astromenaceGamePanel.description")); // NOI18N
        astromenaceGamePanel.setGameName("Astromenace"); // NOI18N
        astromenaceGamePanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/astromenace.png"))); // NOI18N
        astromenaceGamePanel.setWebsite("http://www.viewizard.com/astromenace/index_linux.php"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        gamesScrollPanel.add(astromenaceGamePanel, gridBagConstraints);

        gamesScrollPane.setViewportView(gamesScrollPanel);

        additionalTabbedPane.addTab(bundle.getString("Welcome.gamesScrollPane.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/input-gaming.png")), gamesScrollPane); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        additionalPanel.add(additionalTabbedPane, gridBagConstraints);

        mainCardPanel.add(additionalPanel, "additionalPanel");

        proxyCheckBox.setText(bundle.getString("Welcome.proxyCheckBox.text")); // NOI18N
        proxyCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                proxyCheckBoxItemStateChanged(evt);
            }
        });

        proxyHostLabel.setText(bundle.getString("Welcome.proxyHostLabel.text")); // NOI18N
        proxyHostLabel.setEnabled(false);

        proxyHostTextField.setEnabled(false);

        proxyPortLabel.setText(bundle.getString("Welcome.proxyPortLabel.text")); // NOI18N
        proxyPortLabel.setEnabled(false);

        proxyPortTextField.setColumns(5);
        proxyPortTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#####"))));
        proxyPortTextField.setEnabled(false);

        proxyUserNameLabel.setText(bundle.getString("Welcome.proxyUserNameLabel.text")); // NOI18N
        proxyUserNameLabel.setEnabled(false);

        proxyUserNameTextField.setEnabled(false);

        proxyPasswordLabel.setText(bundle.getString("Welcome.proxyPasswordLabel.text")); // NOI18N
        proxyPasswordLabel.setEnabled(false);

        proxyPasswordField.setEnabled(false);

        proxyInfoLabel.setText(bundle.getString("Welcome.proxyInfoLabel.text")); // NOI18N

        javax.swing.GroupLayout proxyPanelLayout = new javax.swing.GroupLayout(proxyPanel);
        proxyPanel.setLayout(proxyPanelLayout);
        proxyPanelLayout.setHorizontalGroup(
            proxyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(proxyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(proxyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(proxyInfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(proxyPanelLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(proxyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(proxyPanelLayout.createSequentialGroup()
                                .addComponent(proxyPortLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(proxyPortTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(proxyPanelLayout.createSequentialGroup()
                                .addComponent(proxyUserNameLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(proxyUserNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(proxyPanelLayout.createSequentialGroup()
                                .addComponent(proxyPasswordLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(proxyPasswordField))
                            .addGroup(proxyPanelLayout.createSequentialGroup()
                                .addComponent(proxyHostLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(proxyHostTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(proxyCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        proxyPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {proxyHostLabel, proxyPasswordLabel, proxyPortLabel, proxyUserNameLabel});

        proxyPanelLayout.setVerticalGroup(
            proxyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(proxyPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(proxyInfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(proxyCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(proxyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(proxyHostLabel)
                    .addComponent(proxyHostTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(proxyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(proxyPortLabel)
                    .addComponent(proxyPortTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(proxyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(proxyUserNameLabel)
                    .addComponent(proxyUserNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(proxyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(proxyPasswordLabel)
                    .addComponent(proxyPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(121, Short.MAX_VALUE))
        );

        mainCardPanel.add(proxyPanel, "proxyPanel");

        systemPanel.setLayout(new java.awt.GridBagLayout());

        bootMenuPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Welcome.bootMenuPanel.border.title"))); // NOI18N
        bootMenuPanel.setLayout(new java.awt.GridBagLayout());

        bootTimeoutLabel.setText(bundle.getString("Welcome.bootTimeoutLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        bootMenuPanel.add(bootTimeoutLabel, gridBagConstraints);

        bootTimeoutSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(10), Integer.valueOf(1), null, Integer.valueOf(1)));
        bootTimeoutSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                bootTimeoutSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        bootMenuPanel.add(bootTimeoutSpinner, gridBagConstraints);

        secondsLabel.setText(bundle.getString("Welcome.secondsLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 5);
        bootMenuPanel.add(secondsLabel, gridBagConstraints);

        systemNameLabel.setText(bundle.getString("Welcome.systemNameLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        bootMenuPanel.add(systemNameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        bootMenuPanel.add(systemNameTextField, gridBagConstraints);

        systemVersionLabel.setText(bundle.getString("Welcome.systemVersionLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        bootMenuPanel.add(systemVersionLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        bootMenuPanel.add(systemVersionTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 10);
        systemPanel.add(bootMenuPanel, gridBagConstraints);

        userNameLabel.setText(bundle.getString("Welcome.userNameLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(25, 10, 0, 0);
        systemPanel.add(userNameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(25, 10, 0, 10);
        systemPanel.add(userNameTextField, gridBagConstraints);

        kdePlasmaLockCheckBox.setText(bundle.getString("Welcome.kdePlasmaLockCheckBox.text")); // NOI18N
        kdePlasmaLockCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                kdePlasmaLockCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 6, 0, 0);
        systemPanel.add(kdePlasmaLockCheckBox, gridBagConstraints);

        noPulseAudioCheckbox.setText(bundle.getString("Welcome.noPulseAudioCheckbox.text")); // NOI18N
        noPulseAudioCheckbox.setToolTipText(bundle.getString("Welcome.noPulseAudioCheckbox.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        systemPanel.add(noPulseAudioCheckbox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        systemPanel.add(jLabel3, gridBagConstraints);

        mainCardPanel.add(systemPanel, "systemPanel");

        partitionsPanel.setLayout(new java.awt.GridBagLayout());

        exchangePartitionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Welcome.exchangePartitionPanel.border.title"))); // NOI18N
        exchangePartitionPanel.setLayout(new java.awt.GridBagLayout());

        exchangePartitionNameLabel.setText(bundle.getString("Welcome.exchangePartitionNameLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 3, 0);
        exchangePartitionPanel.add(exchangePartitionNameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 3, 10);
        exchangePartitionPanel.add(exchangePartitionNameTextField, gridBagConstraints);

        exchangeAccessCheckBox.setText(bundle.getString("Welcome.exchangeAccessCheckBox.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 0, 0);
        exchangePartitionPanel.add(exchangeAccessCheckBox, gridBagConstraints);

        exchangeRebootLabel.setText(bundle.getString("Welcome.exchangeRebootLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 5, 10);
        exchangePartitionPanel.add(exchangeRebootLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        partitionsPanel.add(exchangePartitionPanel, gridBagConstraints);

        dataPartitionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Welcome.dataPartitionPanel.border.title"))); // NOI18N
        dataPartitionPanel.setLayout(new java.awt.GridBagLayout());

        readWritePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Welcome.readWritePanel.border.title"))); // NOI18N
        readWritePanel.setLayout(new java.awt.GridBagLayout());

        readWriteCheckBox.setText(bundle.getString("Welcome.readWriteCheckBox.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        readWritePanel.add(readWriteCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        dataPartitionPanel.add(readWritePanel, gridBagConstraints);

        readOnlyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Welcome.readOnlyPanel.border.title"))); // NOI18N
        readOnlyPanel.setLayout(new java.awt.GridBagLayout());

        readOnlyCheckBox.setText(bundle.getString("Welcome.readOnlyCheckBox.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        readOnlyPanel.add(readOnlyCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        dataPartitionPanel.add(readOnlyPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        partitionsPanel.add(dataPartitionPanel, gridBagConstraints);

        mainCardPanel.add(partitionsPanel, "partitionsPanel");

        firewallPanel.setLayout(new java.awt.GridBagLayout());

        firewallInfoLabel.setText(bundle.getString("Welcome.firewallInfoLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 0, 5);
        firewallPanel.add(firewallInfoLabel, gridBagConstraints);

        firewallStartStopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/16x16/start.png"))); // NOI18N
        firewallStartStopButton.setText(bundle.getString("Welcome.firewallStartStopButton.text")); // NOI18N
        firewallStartStopButton.setToolTipText(bundle.getString("Firewall_toolTip_start")); // NOI18N
        firewallStartStopButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        firewallStartStopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firewallStartStopButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        firewallPanel.add(firewallStartStopButton, gridBagConstraints);

        firewallStatusLabel.setForeground(java.awt.Color.red);
        firewallStatusLabel.setLabelFor(firewallStartStopButton);
        firewallStatusLabel.setText(bundle.getString("Welcome.firewallStatusLabel.text_stopped")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        firewallPanel.add(firewallStatusLabel, gridBagConstraints);

        firewallipv4Panel.setLayout(new java.awt.GridBagLayout());

        firewallIPButtonPanel.setLayout(new java.awt.GridBagLayout());

        addIPButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/16x16/list-add.png"))); // NOI18N
        addIPButton.setToolTipText(bundle.getString("Welcome.addIPButton.toolTipText")); // NOI18N
        addIPButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        addIPButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addIPButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        firewallIPButtonPanel.add(addIPButton, gridBagConstraints);

        removeIPButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/16x16/list-remove.png"))); // NOI18N
        removeIPButton.setToolTipText(bundle.getString("Welcome.removeIPButton.toolTipText")); // NOI18N
        removeIPButton.setEnabled(false);
        removeIPButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        removeIPButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeIPButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        firewallIPButtonPanel.add(removeIPButton, gridBagConstraints);

        moveUpIPButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/16x16/arrow-up.png"))); // NOI18N
        moveUpIPButton.setToolTipText(bundle.getString("Welcome.moveUpIPButton.toolTipText")); // NOI18N
        moveUpIPButton.setEnabled(false);
        moveUpIPButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        moveUpIPButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpIPButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        firewallIPButtonPanel.add(moveUpIPButton, gridBagConstraints);

        moveDownIPButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/16x16/arrow-down.png"))); // NOI18N
        moveDownIPButton.setToolTipText(bundle.getString("Welcome.moveDownIPButton.toolTipText")); // NOI18N
        moveDownIPButton.setEnabled(false);
        moveDownIPButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        moveDownIPButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownIPButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        firewallIPButtonPanel.add(moveDownIPButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 5, 0, 0);
        firewallipv4Panel.add(firewallIPButtonPanel, gridBagConstraints);

        firewallIPScrollPane.setViewportView(firewallIPTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        firewallipv4Panel.add(firewallIPScrollPane, gridBagConstraints);

        firewallTabbedPane.addTab(bundle.getString("Welcome.firewallipv4Panel.TabConstraints.tabTitle"), firewallipv4Panel); // NOI18N

        firewallURLPanel.setLayout(new java.awt.GridBagLayout());

        firewallURLTextArea.setColumns(20);
        firewallURLTextArea.setRows(5);
        firewallURLScrollPane.setViewportView(firewallURLTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        firewallURLPanel.add(firewallURLScrollPane, gridBagConstraints);

        firewallTabbedPane.addTab(bundle.getString("Welcome.firewallURLPanel.TabConstraints.tabTitle"), firewallURLPanel); // NOI18N

        helpTextPane.setEditable(false);
        helpTextPane.setContentType("text/html"); // NOI18N
        helpTextPane.setText(bundle.getString("Welcome.helpTextPane.text")); // NOI18N
        helpScrollPane.setViewportView(helpTextPane);

        firewallTabbedPane.addTab(bundle.getString("Welcome.helpScrollPane.TabConstraints.tabTitle"), helpScrollPane); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        firewallPanel.add(firewallTabbedPane, gridBagConstraints);

        mainCardPanel.add(firewallPanel, "firewallPanel");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(mainCardPanel, gridBagConstraints);

        bottomPanel.setLayout(new java.awt.GridBagLayout());

        navigaionPanel.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        previousButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/16x16/go-previous.png"))); // NOI18N
        previousButton.setText(bundle.getString("Welcome.previousButton.text")); // NOI18N
        previousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousButtonActionPerformed(evt);
            }
        });
        navigaionPanel.add(previousButton);

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/16x16/go-next.png"))); // NOI18N
        nextButton.setText(bundle.getString("Welcome.nextButton.text")); // NOI18N
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });
        navigaionPanel.add(nextButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        bottomPanel.add(navigaionPanel, gridBagConstraints);

        applyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/16x16/dialog-ok-apply.png"))); // NOI18N
        applyButton.setText(bundle.getString("Welcome.applyButton.text")); // NOI18N
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        bottomPanel.add(applyButton, gridBagConstraints);

        cancelButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/exit.png"))); // NOI18N
        cancelButton.setText(bundle.getString("Welcome.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        bottomPanel.add(cancelButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 10);
        getContentPane().add(bottomPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void googleEarthLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_googleEarthLabelMouseClicked
        toggleCheckBox(googleEarthCheckBox);
}//GEN-LAST:event_googleEarthLabelMouseClicked

    private void skypeLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_skypeLabelMouseClicked
        toggleCheckBox(skypeCheckBox);
    }//GEN-LAST:event_skypeLabelMouseClicked

    private void flashLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_flashLabelMouseClicked
        toggleCheckBox(flashCheckBox);
    }//GEN-LAST:event_flashLabelMouseClicked

    private void fontsLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fontsLabelMouseClicked
        toggleCheckBox(additionalFontsCheckBox);
    }//GEN-LAST:event_fontsLabelMouseClicked

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        try {
            apply();
        } catch (DBusException ex) {
            LOGGER.log(Level.SEVERE, "", ex);
        }
    }//GEN-LAST:event_applyButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        if ((bootMountInfo != null) && (!bootMountInfo.alreadyMounted())) {
            try {
                bootPartition.umount();
            } catch (DBusException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        System.exit(0);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void multimediaLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_multimediaLabelMouseClicked
        toggleCheckBox(multimediaCheckBox);
}//GEN-LAST:event_multimediaLabelMouseClicked

    private void infoEditorPaneHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_infoEditorPaneHyperlinkUpdate
        openLinkInBrowser(evt);
    }//GEN-LAST:event_infoEditorPaneHyperlinkUpdate

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        LOGGER.info("exiting program");
        System.exit(0);
    }//GEN-LAST:event_formWindowClosed

    private void proxyCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_proxyCheckBoxItemStateChanged
        setProxyEnabled(proxyCheckBox.isSelected());
    }//GEN-LAST:event_proxyCheckBoxItemStateChanged

    private void laLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_laLabelMouseClicked
        toggleCheckBox(laCheckBox);
    }//GEN-LAST:event_laLabelMouseClicked

    private void menuListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_menuListValueChanged
        if (evt.getValueIsAdjusting()) {
            return;
        }

        int selectedIndex = menuList.getSelectedIndex();
        if (selectedIndex == -1) {
            menuList.setSelectedIndex(menuListIndex);
            return;
        } else {
            menuListIndex = selectedIndex;
        }

        MainMenuListEntry entry
                = (MainMenuListEntry) menuList.getSelectedValue();
        selectCard(entry.getPanelID());

        previousButton.setEnabled(selectedIndex > 0);
        nextButton.setEnabled(
                (selectedIndex + 1) < menuList.getModel().getSize());
    }//GEN-LAST:event_menuListValueChanged

    private void menuListMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_menuListMouseWheelMoved
        if (evt.getWheelRotation() < 0) {
            if (menuListIndex > 0) {
                menuList.setSelectedIndex(menuListIndex - 1);
            }
        } else {
            if (menuListIndex < (menuListModel.getSize() - 1)) {
                menuList.setSelectedIndex(menuListIndex + 1);
            }
        }
    }//GEN-LAST:event_menuListMouseWheelMoved

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        menuList.setSelectedIndex(menuListIndex + 1);
    }//GEN-LAST:event_nextButtonActionPerformed

    private void previousButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousButtonActionPerformed
        menuList.setSelectedIndex(menuListIndex - 1);
    }//GEN-LAST:event_previousButtonActionPerformed

    private void teachingEditorPaneHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_teachingEditorPaneHyperlinkUpdate
        openLinkInBrowser(evt);
    }//GEN-LAST:event_teachingEditorPaneHyperlinkUpdate

    private void bootTimeoutSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_bootTimeoutSpinnerStateChanged
        updateSecondsLabel();
    }//GEN-LAST:event_bootTimeoutSpinnerStateChanged

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // enforce visibility of top applications
        additionalScrollPane.getVerticalScrollBar().setValue(0);
        gamesScrollPane.getVerticalScrollBar().setValue(0);
    }//GEN-LAST:event_formWindowOpened

    private void passwordChangeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordChangeButtonActionPerformed
        changePassword();
    }//GEN-LAST:event_passwordChangeButtonActionPerformed

    private void passwordField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordField1ActionPerformed
        passwordField2.selectAll();
        passwordField2.requestFocusInWindow();
    }//GEN-LAST:event_passwordField1ActionPerformed

    private void passwordField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordField2ActionPerformed
        changePassword();
    }//GEN-LAST:event_passwordField2ActionPerformed

    private void addIPButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addIPButtonActionPerformed
        ipTableModel.addEntry();
    }//GEN-LAST:event_addIPButtonActionPerformed

    private void removeIPButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeIPButtonActionPerformed
        TableCellEditor editor = firewallIPTable.getCellEditor();
        if (editor != null) {
            editor.stopCellEditing();
        }
        int[] selectedRows = firewallIPTable.getSelectedRows();
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            ipTableModel.removeRow(selectedRows[i]);
        }
    }//GEN-LAST:event_removeIPButtonActionPerformed

    private void moveUpIPButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpIPButtonActionPerformed
        ipTableModel.moveEntries(true);
    }//GEN-LAST:event_moveUpIPButtonActionPerformed

    private void moveDownIPButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownIPButtonActionPerformed
        ipTableModel.moveEntries(false);
    }//GEN-LAST:event_moveDownIPButtonActionPerformed

    private void backupCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_backupCheckBoxItemStateChanged
        setBackupEnabled(backupCheckBox.isSelected());
    }//GEN-LAST:event_backupCheckBoxItemStateChanged

    private void backupSourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backupSourceButtonActionPerformed
        showFileSelector(backupSourceTextField);
    }//GEN-LAST:event_backupSourceButtonActionPerformed

    private void backupDirectoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backupDirectoryButtonActionPerformed
        showFileSelector(backupDirectoryTextField);
    }//GEN-LAST:event_backupDirectoryButtonActionPerformed

    private void backupDirectoryCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_backupDirectoryCheckBoxItemStateChanged
        updateBackupDirectoryEnabled();
    }//GEN-LAST:event_backupDirectoryCheckBoxItemStateChanged

    private void backupPartitionCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_backupPartitionCheckBoxItemStateChanged
        updateBackupPartitionEnabled();
    }//GEN-LAST:event_backupPartitionCheckBoxItemStateChanged

    private void kdePlasmaLockCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_kdePlasmaLockCheckBoxItemStateChanged
        if (!kdePlasmaLockCheckBox.isSelected()) {
            try {
                PosixFileAttributes attributes = Files.readAttributes(
                        APPLETS_CONFIG_FILE, PosixFileAttributes.class
                );
                Set<PosixFilePermission> permissions = attributes.permissions();

                permissions.add(PosixFilePermission.OWNER_WRITE);

                Files.setPosixFilePermissions(APPLETS_CONFIG_FILE, permissions);
            } catch (IOException iOException) {
                LOGGER.log(Level.WARNING, "", iOException);
            }
        }
    }//GEN-LAST:event_kdePlasmaLockCheckBoxItemStateChanged

    private void firewallStartStopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firewallStartStopButtonActionPerformed
        toggleFirewallState();
    }//GEN-LAST:event_firewallStartStopButtonActionPerformed

    private void toggleFirewallState() {
        String action = firewallRunning ? "stop" : "start";
        int ret = processExecutor.executeProcess(
                true, true, "lernstick-firewall", action);

        if (ret == 0) {
            firewallRunning = !firewallRunning;
            // update widget
            updateFirewallState();
        } else {
            LOGGER.log(Level.WARNING,
                    action + "ing lernstick-firewall failed, return code {0} "
                    + "stdout: '{1}', stderr: '{2}'",
                    new Object[]{
                        ret,
                        processExecutor.getStdOut(),
                        processExecutor.getStdErr()
                    });
            String messageId = firewallRunning
                    ? "Stop_firewall_error"
                    : "Start_firewall_error";
            JOptionPane.showMessageDialog(this,
                    BUNDLE.getString(messageId),
                    BUNDLE.getString("Error"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateFirewallState() {
        // check firewall state
        int ret = processExecutor.executeProcess("lernstick-firewall", "status");
        firewallRunning = ret == 0;

        // update button icon
        String iconBasePath = "/ch/fhnw/lernstickwelcome/icons/16x16/";
        String iconPath = firewallRunning
                ? iconBasePath + "stop.png"
                : iconBasePath + "start.png";
        firewallStartStopButton.setIcon(
                new ImageIcon(getClass().getResource(iconPath)));
        String tooltipString = firewallRunning
                ? BUNDLE.getString("Firewall_toolTip_stop")
                : BUNDLE.getString("Firewall_toolTip_start");
        firewallStartStopButton.setToolTipText(tooltipString);

        // update label text and color
        String labelString = firewallRunning
                ? BUNDLE.getString("Welcome.firewallStatusLabel.text_running")
                : BUNDLE.getString("Welcome.firewallStatusLabel.text_stopped");
        firewallStatusLabel.setText(labelString);
        firewallStatusLabel.setForeground(firewallRunning
                ? Color.green
                : Color.red);
    }

    private void getFullUserName() {
        AbstractDocument userNameDocument
                = (AbstractDocument) userNameTextField.getDocument();
        userNameDocument.setDocumentFilter(new FullUserNameFilter());
        processExecutor.executeProcess(true, true, "getent", "passwd", "user");
        List<String> stdOut = processExecutor.getStdOutList();
        if (stdOut.isEmpty()) {
            LOGGER.warning("getent returned no result!");
            fullName = null;
        } else {
            // getent passwd returns a line with the following pattern:
            // login:encrypted_password:id:gid:gecos_field:home:shell
            String line = stdOut.get(0);
            String[] tokens = line.split(":");
            if (tokens.length < 5) {
                LOGGER.log(Level.WARNING,
                        "can not parse getent line:\n{0}", line);
                fullName = null;
            } else {
                String gecosField = line.split(":")[4];
                // the "gecos_field" has the following syntax:
                // full_name,room_nr,phone_work,phone_private,misc
                fullName = gecosField.split(",")[0];
                userNameTextField.setText(fullName);
            }
        }
    }

    private void showFileSelector(JTextField textField) {
        UIManager.put("FileChooser.readOnly", Boolean.TRUE);
        File selectedDirectory = new File(textField.getText());
        JFileChooser fileChooser
                = new JFileChooser(selectedDirectory.getParent());
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setSelectedFile(selectedDirectory);
        fileChooser.showOpenDialog(this);
        selectedDirectory = fileChooser.getSelectedFile();
        textField.setText(selectedDirectory.getPath());
    }

    private void setBackupEnabled(boolean enabled) {
        backupSourceLabel.setEnabled(enabled);
        backupSourceTextField.setEnabled(enabled);
        backupSourceButton.setEnabled(enabled);

        backupDirectoryCheckBox.setEnabled(enabled);
        updateBackupDirectoryEnabled();

        backupPartitionCheckBox.setEnabled(enabled);
        updateBackupPartitionEnabled();

        screenShotCheckBox.setEnabled(enabled);

        backupFrequencyEveryLabel.setEnabled(enabled);
        backupFrequencySpinner.setEnabled(enabled);
        backupFrequencyMinuteLabel.setEnabled(enabled);

        setBordersEnabled(enabled);
    }

    private void updateBackupDirectoryEnabled() {
        boolean enabled = backupCheckBox.isSelected()
                && backupDirectoryCheckBox.isSelected();
        backupDirectoryLabel.setEnabled(enabled);
        backupDirectoryTextField.setEnabled(enabled);
        backupDirectoryButton.setEnabled(enabled);
    }

    private void updateBackupPartitionEnabled() {
        boolean enabled = backupCheckBox.isSelected()
                && backupPartitionCheckBox.isSelected();
        backupPartitionLabel.setEnabled(enabled);
        backupPartitionTextField.setEnabled(enabled);
    }

    private void setBordersEnabled(boolean enabled) {
        Color color = enabled ? Color.BLACK : Color.GRAY;
        TitledBorder border = (TitledBorder) backupSourcePanel.getBorder();
        border.setTitleColor(color);
        border = (TitledBorder) backupDestinationsPanel.getBorder();
        border.setTitleColor(color);
        backupSourcePanel.repaint();
        backupDestinationsPanel.repaint();
    }

    private void parseURLWhiteList() throws IOException {
        try (FileReader fileReader = new FileReader(URL_WHITELIST_FILENAME);
                BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            StringBuilder builder = new StringBuilder();
            for (String line = bufferedReader.readLine(); line != null;) {
                builder.append(line);
                builder.append('\n');
                line = bufferedReader.readLine();
            }
            firewallURLTextArea.setText(builder.toString());
        }
    }

    private void parseNetWhiteList() throws IOException {
        FileReader fileReader = new FileReader(IP_TABLES_FILENAME);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String lastComment = "";
        for (String line = bufferedReader.readLine(); line != null;) {
            if (line.startsWith("#")) {
                lastComment = line.substring(1).trim();
            } else {
                // try parsing "protocol target port"
                String[] tokens = line.split(" ");
                if (tokens.length == 3) {
                    Protocol protocol;
                    if (tokens[0].equalsIgnoreCase("TCP")) {
                        protocol = Protocol.TCP;
                    } else if (tokens[0].equalsIgnoreCase("UDP")) {
                        protocol = Protocol.UDP;
                    } else {
                        LOGGER.log(Level.WARNING,
                                "could not parse protocol \"{0}\"", tokens[0]);
                        continue;
                    }
                    String target = tokens[1];
                    String portRange = tokens[2];
                    ipTableModel.addEntry(new IPTableEntry(
                            protocol, target, portRange, lastComment));
                } else {
                    LOGGER.log(Level.WARNING,
                            "unsupported net whitelist:\n{0}", line);
                }
            }

            line = bufferedReader.readLine();
        }
        ipTableModel.fireTableDataChanged();
    }

    private void changePassword() {
        // check, if both passwords are the same
        char[] password1 = passwordField1.getPassword();
        char[] password2 = passwordField2.getPassword();
        if (!Arrays.equals(password1, password2)) {
            JOptionPane.showMessageDialog(this,
                    BUNDLE.getString("Password_Mismatch"),
                    BUNDLE.getString("Warning"), JOptionPane.WARNING_MESSAGE);
            passwordField1.selectAll();
            passwordField1.requestFocusInWindow();
            return;
        }

        // ok, passwords match, change password
        String passwordChangeScript = "#!/bin/sh\n"
                + "echo \"user:" + new String(password1) + "\""
                + " | /usr/sbin/chpasswd\n";
        ProcessExecutor executor = new ProcessExecutor();
        try {
            int returnValue = executor.executeScript(
                    true, true, passwordChangeScript);
            if (returnValue == 0) {
                JOptionPane.showMessageDialog(this,
                        BUNDLE.getString("Password_Changed"),
                        BUNDLE.getString("Information"),
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        BUNDLE.getString("Password_Change_Error"),
                        BUNDLE.getString("Error"), JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "", ex);
        }
    }

    private static boolean isImageWritable() {
        processExecutor.executeProcess(
                "mount", "-o", "remount,rw", IMAGE_DIRECTORY);
        String testPath = IMAGE_DIRECTORY + "/lernstickWelcome.tmp";
        processExecutor.executeProcess("touch", testPath);
        File testFile = new File(testPath);
        try {
            if (testFile.exists()) {
                LOGGER.info("image is writable");
                return true;
            } else {
                LOGGER.info("image is not writable");
                return false;
            }
        } finally {
            processExecutor.executeProcess("rm", testPath);
            processExecutor.executeProcess(
                    "mount", "-o", "remount,ro", IMAGE_DIRECTORY);
        }
    }

    private File getXmlBootConfigFile() throws DBusException {

        if (bootPartition == null) {
            // legacy system
            File configFile = getXmlBootConfigFile(new File(IMAGE_DIRECTORY));
            if (configFile != null) {
                return configFile;
            }
        } else {
            // system with a separate boot partition
            File configFile = bootPartition.executeMounted(
                    new Partition.Action<File>() {

                        @Override
                        public File execute(File mountPath) {
                            return getXmlBootConfigFile(mountPath);
                        }
                    });
            if (configFile != null) {
                return configFile;
            }
        }

        return null;
    }

    private File getXmlBootConfigFile(File directory) {
        // search through all known variants
        String[] dirs = new String[]{"isolinux", "syslinux"};
        String[] subdirs = new String[]{"/", "/bootlogo.dir/"};
        for (String dir : dirs) {
            for (String subdir : subdirs) {
                File configFile = new File(
                        directory, dir + subdir + "xmlboot.config");
                if (configFile.exists()) {
                    LOGGER.log(Level.INFO,
                            "xmlboot config file: {0}", configFile);
                    return configFile;
                }
            }
        }
        return null;
    }

    private Document parseXmlFile(File file)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // External DTD loading will most probably fail because of the local
        // firewall rules. Therefore we must disable this feature, otherwise
        // the call to parse() below will just throw an IOException.
        factory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false);
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(file);
    }

    private List<File> getSyslinuxConfigFiles(File directory) {

        List<File> configFiles = new ArrayList<>();

        // check all known locations of syslinux config files
        String[] syslinuxDirs = new String[]{
            "isolinux",
            "syslinux"
        };
        String[] syslinuxFiles = new String[]{
            "isolinux.cfg",
            "syslinux.cfg",
            "boot_486.cfg",
            "boot_686.cfg"
        };
        for (String syslinuxDir : syslinuxDirs) {
            for (String syslinuxFile : syslinuxFiles) {
                File configDir = new File(directory, syslinuxDir);
                File configFile = new File(configDir, syslinuxFile);
                if (configFile.exists()) {
                    configFiles.add(configFile);
                }
            }
        }

        return configFiles;
    }

    private int getTimeout() throws IOException, DBusException {

        // use syslinux configuration as reference for the timeout setting
        List<File> syslinuxConfigFiles;
        if (bootPartition == null) {
            // legacy system
            syslinuxConfigFiles = getSyslinuxConfigFiles(
                    new File(IMAGE_DIRECTORY));
        } else {
            // system with a separate boot partition
            syslinuxConfigFiles = bootPartition.executeMounted(
                    new Partition.Action<List<File>>() {
                        @Override
                        public List<File> execute(File mountPath) {
                            return getSyslinuxConfigFiles(mountPath);
                        }
                    });
        }

        Pattern timeoutPattern = Pattern.compile("timeout (.*)");
        for (File syslinuxConfigFile : syslinuxConfigFiles) {
            List<String> configFileLines = readFile(syslinuxConfigFile);
            for (String configFileLine : configFileLines) {
                Matcher matcher = timeoutPattern.matcher(configFileLine);
                if (matcher.matches()) {
                    String timeoutString = matcher.group(1);
                    try {
                        return Integer.parseInt(timeoutString) / 10;
                    } catch (NumberFormatException e) {
                        LOGGER.log(Level.WARNING,
                                "could not parse timeout value \"{0}\"",
                                timeoutString);
                    }
                }
            }
        }

        return -1;
    }

    private void updateSecondsLabel() {
        SpinnerNumberModel model
                = (SpinnerNumberModel) bootTimeoutSpinner.getModel();
        if (model.getNumber().intValue() == 1) {
            secondsLabel.setText(BUNDLE.getString("second"));
        } else {
            secondsLabel.setText(BUNDLE.getString("seconds"));
        }
    }

    /**
     * opens a clicked link in a browser
     *
     * @param evt the corresponding HyperlinkEvent
     */
    public static void openLinkInBrowser(HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
//            try {
//                Desktop.getDesktop().browse(evt.getURL().toURI());
//            } catch (IOException ex) {
//                logger.log(Level.SEVERE, "could not open URL", ex);
//            } catch (URISyntaxException ex) {
//                logger.log(Level.SEVERE, "could not open URL", ex);
//            }

            // as long as Konqueror sucks so bad, we enforce iceweasel
            // (this is a quick and dirty solution, if konqueror starts to be
            // usable, switch back to the code above)
            final HyperlinkEvent finalEvent = evt;
            Thread browserThread = new Thread() {
                @Override
                public void run() {
                    processExecutor.executeProcess(new String[]{
                        "iceweasel", finalEvent.getURL().toString()});
                }
            };
            browserThread.start();
        }
    }

    private void selectCard(String cardName) {
        CardLayout cardLayout = (CardLayout) mainCardPanel.getLayout();
        cardLayout.show(mainCardPanel, cardName);
    }

    private void setProxyEnabled(boolean enabled) {
        proxyHostLabel.setEnabled(enabled);
        proxyHostTextField.setEnabled(enabled);
        proxyPortLabel.setEnabled(enabled);
        proxyPortTextField.setEnabled(enabled);
        proxyUserNameLabel.setEnabled(enabled);
        proxyUserNameTextField.setEnabled(enabled);
        proxyPasswordLabel.setEnabled(enabled);
        proxyPasswordField.setEnabled(enabled);
    }

    private String getWgetProxyLine() {
        if (proxyCheckBox.isSelected()) {
            String proxyHost = proxyHostTextField.getText();
            int proxyPort = ((Number) proxyPortTextField.getValue()).intValue();
            String proxyUserName = proxyUserNameTextField.getText();
            String proxyPassword
                    = String.valueOf(proxyPasswordField.getPassword());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(" -e http_proxy=http://");
            stringBuilder.append(proxyHost);
            if (proxyPort != 0) {
                stringBuilder.append(':');
                stringBuilder.append(proxyPort);
            }
            if (!proxyUserName.isEmpty()) {
                stringBuilder.append(" --proxy-user=");
                stringBuilder.append(proxyUserName);
            }
            if (!proxyPassword.isEmpty()) {
                stringBuilder.append(" --proxy-password=");
                stringBuilder.append(proxyPassword);
            }
            stringBuilder.append(' ');
            return stringBuilder.toString();
        } else {
            return " ";
        }
    }

    private String getAptGetProxyLine() {
        if (proxyCheckBox.isSelected()) {
            return " -o " + getAptGetAcquireLine() + ' ';
        } else {
            return " ";
        }
    }

    private String getAptGetAcquireLine() {
        String proxyHost = proxyHostTextField.getText();
        int proxyPort = ((Number) proxyPortTextField.getValue()).intValue();
        String proxyUserName = proxyUserNameTextField.getText();
        String proxyPassword = String.valueOf(proxyPasswordField.getPassword());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Acquire::http::proxy=http://");
        if (!proxyUserName.isEmpty()) {
            stringBuilder.append(proxyUserName);
            if (!proxyPassword.isEmpty()) {
                stringBuilder.append(':');
                stringBuilder.append(proxyPassword);
            }
            stringBuilder.append('@');
        }
        stringBuilder.append(proxyHost);
        if (proxyPort != 0) {
            stringBuilder.append(':');
            stringBuilder.append(proxyPort);
        }
        return stringBuilder.toString();
    }

    private void apply() throws DBusException {
        // make sure that all edits are applied to the IP table
        // and so some firewall sanity checks
        TableCellEditor editor = firewallIPTable.getCellEditor();
        if (editor != null) {
            editor.stopCellEditing();
        }
        if (examEnvironment) {
            if (!checkFirewall()) {
                return;
            }
            if (!checkBackupDirectory()) {
                return;
            }
        }

        // update full user name (if necessary)
        String newFullName = userNameTextField.getText();
        if (!newFullName.equals(fullName)) {
            LOGGER.log(Level.INFO,
                    "updating full user name to \"{0}\"", newFullName);
            processExecutor.executeProcess("chfn", "-f", newFullName, "user");
        }

        // update exchange partition label
        String newExchangePartitionLabel
                = exchangePartitionNameTextField.getText();
        LOGGER.log(Level.INFO, "new exchange partition label: \"{0}\"",
                newExchangePartitionLabel);
        if (!newExchangePartitionLabel.isEmpty()
                && !newExchangePartitionLabel.equals(exchangePartitionLabel)) {
            String binary = null;
            boolean umount = false;
            String idType = exchangePartition.getIdType();
            switch (idType) {
                case "vfat":
                    binary = "dosfslabel";
                    break;
                case "exfat":
                    binary = "exfatlabel";
                    break;
                case "ntfs":
                    binary = "ntfslabel";
                    // ntfslabel refuses to work on a mounted partition with the
                    // error message: "Cannot make changes to a mounted device".
                    // Therefore we have to try to umount the partition.
                    umount = true;
                    break;
                default:
                    LOGGER.log(Level.WARNING,
                            "no labeling binary for type \"{0}\"!", idType);
                    break;
            }
            if (binary != null) {
                boolean tmpUmount = umount && exchangePartition.isMounted();
                if (tmpUmount) {
                    exchangePartition.umount();
                }
                processExecutor.executeProcess(binary,
                        "/dev/" + exchangePartition.getDeviceAndNumber(),
                        newExchangePartitionLabel);
                if (tmpUmount) {
                    exchangePartition.mount();
                }
            }
        }

        String backupSource = backupSourceTextField.getText();
        String backupDirectory = backupDirectoryTextField.getText();
        String backupPartition = backupPartitionTextField.getText();

        if (examEnvironment) {

            updateFirewall();

            if (!backupDirectoryCheckBox.isSelected()
                    || backupDirectory.isEmpty()) {
                if (backupPartitionCheckBox.isSelected()
                        && !backupPartition.isEmpty()) {
                    updateJBackpackProperties(backupSource, "/mnt/backup/"
                            + backupPartition + "/lernstick_backup");
                }
            } else {
                updateJBackpackProperties(backupSource, backupDirectory);
            }

        } else {
            installSelectedPackages();
        }

        // update lernstickWelcome properties
        try {
            properties.setProperty(SHOW_WELCOME,
                    readWriteCheckBox.isSelected() ? "true" : "false");
            properties.setProperty(SHOW_READ_ONLY_INFO,
                    readOnlyCheckBox.isSelected() ? "true" : "false");
            properties.setProperty(BACKUP,
                    backupCheckBox.isSelected() ? "true" : "false");
            properties.setProperty(BACKUP_SCREENSHOT,
                    screenShotCheckBox.isSelected() ? "true" : "false");
            properties.setProperty(EXCHANGE_ACCESS,
                    exchangeAccessCheckBox.isSelected() ? "true" : "false");
            properties.setProperty(BACKUP_DIRECTORY_ENABLED,
                    backupDirectoryCheckBox.isSelected() ? "true" : "false");
            properties.setProperty(BACKUP_PARTITION_ENABLED,
                    backupPartitionCheckBox.isSelected() ? "true" : "false");
            properties.setProperty(BACKUP_SOURCE, backupSource);
            properties.setProperty(BACKUP_DIRECTORY, backupDirectory);
            properties.setProperty(BACKUP_PARTITION, backupPartition);
            Number backupFrequency = (Number) backupFrequencySpinner.getValue();
            properties.setProperty(BACKUP_FREQUENCY,
                    backupFrequency.toString());
            properties.setProperty(KDE_LOCK,
                    kdePlasmaLockCheckBox.isSelected() ? "true" : "false");
            properties.store(new FileOutputStream(propertiesFile),
                    "lernstick Welcome properties");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        if (IMAGE_IS_WRITABLE) {
            updateBootloaders();
        }

        if (Files.exists(ALSA_PULSE_CONFIG_FILE)) {
            if (noPulseAudioCheckbox.isSelected()) {
                // divert alsa pulse config file
                processExecutor.executeProcess("dpkg-divert",
                        "--rename", ALSA_PULSE_CONFIG_FILE.toString());
            }
        } else {
            if (!noPulseAudioCheckbox.isSelected()) {
                // restore original alsa pulse config file
                processExecutor.executeProcess("dpkg-divert", "--remove",
                        "--rename", ALSA_PULSE_CONFIG_FILE.toString());
            }
        }

        // show "done" message
        // toolkit.beep();
        URL url = getClass().getResource(
                "/ch/fhnw/lernstickwelcome/KDE_Notify.wav");
        AudioClip clip = Applet.newAudioClip(url);
        clip.play();
        String infoMessage = BUNDLE.getString("Info_Success");
        JOptionPane.showMessageDialog(this, infoMessage,
                BUNDLE.getString("Information"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateBootloaders() throws DBusException {

        SpinnerNumberModel spinnerNumberModel
                = (SpinnerNumberModel) bootTimeoutSpinner.getModel();
        final int timeout = spinnerNumberModel.getNumber().intValue();
        final String systemName = systemNameTextField.getText();
        final String systemVersion = systemVersionTextField.getText();

        Partition.Action<Void> updateBootloaderAction
                = new Partition.Action<Void>() {

                    @Override
                    public Void execute(File mountPath) {
                        try {
                            updateBootloaders(mountPath, timeout,
                                    systemName, systemVersion);
                        } catch (DBusException ex) {
                            LOGGER.log(Level.SEVERE, "", ex);
                        }
                        return null;
                    }
                };

        if (bootPartition == null) {
            // legacy system without separate boot partition

            // make image temporarily writable
            processExecutor.executeProcess(
                    "mount", "-o", "remount,rw", IMAGE_DIRECTORY);

            updateBootloaders(new File(IMAGE_DIRECTORY),
                    timeout, systemName, systemVersion);

            // remount image read-only
            processExecutor.executeProcess(
                    "mount", "-o", "remount,ro", IMAGE_DIRECTORY);
        } else {
            // system with a separate boot partition
            bootPartition.executeMounted(updateBootloaderAction);
        }
        if (exchangePartition != null) {
            exchangePartition.executeMounted(updateBootloaderAction);
        }
    }

    private void updateBootloaders(File directory, int timeout,
            String systemName, String systemVersion) throws DBusException {

        // *** update timeout ***
        // in syslinux
        for (File syslinuxConfigFile : getSyslinuxConfigFiles(directory)) {
            processExecutor.executeProcess("sed", "-i", "-e",
                    "s|timeout .*|timeout " + (timeout * 10) + "|1",
                    syslinuxConfigFile.getPath());
        }
        // in grub
        processExecutor.executeProcess("sed", "-i", "-e",
                "s|set timeout=.*|set timeout=" + timeout + "|1",
                directory + "/boot/grub/grub_main.cfg");
        processExecutor.executeProcess("sed", "-i", "-e",
                "s|num_ticks = .*|num_ticks = " + timeout + "|1",
                directory + "/boot/grub/themes/lernstick/theme.txt");

        // *** update system name and version ***
        // in xmlboot config
        File xmlBootConfigFile = getXmlBootConfigFile(directory);
        try {
            Document xmlBootDocument = parseXmlFile(xmlBootConfigFile);
            xmlBootDocument.getDocumentElement().normalize();
            Node systemNode = xmlBootDocument.
                    getElementsByTagName("system").item(0);
            Element systemElement = (Element) systemNode;
            Node node = systemElement.getElementsByTagName("text").item(0);
            if (node != null) {
                node.setTextContent(systemName);
            }
            node = systemElement.getElementsByTagName("version").item(0);
            if (node != null) {
                node.setTextContent(systemVersion);
            }

            // write changes back to config file
            File tmpFile = File.createTempFile("lernstickWelcome", "tmp");
            TransformerFactory transformerFactory
                    = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(xmlBootDocument);
            StreamResult result = new StreamResult(tmpFile);
            transformer.transform(source, result);
            processExecutor.executeProcess("mv", tmpFile.getPath(),
                    xmlBootConfigFile.getPath());

        } catch (ParserConfigurationException | SAXException | IOException |
                DOMException | TransformerException ex) {
            LOGGER.log(Level.WARNING, "can not update xmlboot config", ex);
        }
        // in grub theme
        processExecutor.executeProcess("sed", "-i", "-e",
                "s|title-text: .*|title-text: \""
                + systemName + ' ' + systemVersion + "\"|1",
                directory + "/boot/grub/themes/lernstick/theme.txt");
    }

    private void updateFirewall() {
        // save IP tables
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < ipTableModel.getRowCount(); i++) {
            // comment
            stringBuilder.append("# ");
            stringBuilder.append(ipTableModel.getValueAt(i, 3));
            stringBuilder.append('\n');
            // protocol
            stringBuilder.append(ipTableModel.getValueAt(i, 0));
            stringBuilder.append(' ');
            // target
            stringBuilder.append(ipTableModel.getValueAt(i, 1));
            stringBuilder.append(' ');
            // port
            stringBuilder.append(ipTableModel.getValueAt(i, 2));
            stringBuilder.append('\n');
        }
        String ipTables = stringBuilder.toString();
        try (FileOutputStream fileOutputStream
                = new FileOutputStream(IP_TABLES_FILENAME)) {
            fileOutputStream.write(ipTables.getBytes());
            fileOutputStream.flush();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "", ex);
        }

        // save URL whitelist
        try (FileOutputStream fileOutputStream
                = new FileOutputStream(URL_WHITELIST_FILENAME)) {
            fileOutputStream.write(firewallURLTextArea.getText().getBytes());
            fileOutputStream.flush();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "", ex);
        }
        processExecutor.executeProcess(
                "/etc/init.d/lernstick-firewall", "reload");
    }

    private boolean checkBackupDirectory() {

        if ((!backupCheckBox.isSelected())
                || (!backupDirectoryCheckBox.isSelected())) {
            // As long as the directory option is not selected we just don't
            // care what is configured there...
            return true;
        }

        String backupDirectory = backupDirectoryTextField.getText();

        if (backupDirectory.isEmpty()) {
            String errorMessage = BUNDLE.getString("Error_No_Backup_Directory");
            showBackupDirectoryError(errorMessage);
            return false;
        }

        File dirFile = new File(backupDirectory);
        if (dirFile.exists()) {
            if (!dirFile.isDirectory()) {
                String errorMessage = BUNDLE.getString(
                        "Error_Backup_Directory_No_Directory");
                errorMessage = MessageFormat.format(
                        errorMessage, backupDirectory);
                showBackupDirectoryError(errorMessage);
                return false;
            }

            String[] files = dirFile.list();
            if ((files != null) && (files.length != 0)) {
                int returnValue = processExecutor.executeProcess(
                        "rdiff-backup", "-l", dirFile.getAbsolutePath());
                if (returnValue != 0) {
                    String errorMessage = BUNDLE.getString(
                            "Error_Backup_Directory_Invalid");
                    errorMessage = MessageFormat.format(
                            errorMessage, backupDirectory);
                    showBackupDirectoryError(errorMessage);
                    return false;
                }
            }
        }

        // determine device where the directory is located
        // (df takes care for symlinks etc.)
        processExecutor.executeProcess(true, true, "df", backupDirectory);
        List<String> stdOut = processExecutor.getStdOutList();
        String device = null;
        for (String line : stdOut) {
            if (line.startsWith("/dev/")) {
                String[] tokens = line.split(" ");
                device = tokens[0];
            }
        }
        if (device == null) {
            LOGGER.log(Level.WARNING,
                    "could not determine device of directory {0}",
                    backupDirectory);
            return true;
        }

        // check, if device is exFAT
        try {
            Partition partition = Partition.getPartitionFromDeviceAndNumber(
                    device.substring(5), StorageTools.getSystemSize());
            String idType = partition.getIdType();
            if (idType.equals("exfat")) {
                // rdiff-backup does not work (yet) on exfat partitions!
                String errorMessage = BUNDLE.getString("Error_Backup_on_exFAT");
                errorMessage = MessageFormat.format(
                        errorMessage, backupDirectory);
                showBackupDirectoryError(errorMessage);
                return false;
            }
        } catch (DBusException ex) {
            LOGGER.log(Level.WARNING, "", ex);
        }

        return true;
    }

    private void showBackupDirectoryError(String errorMessage) {
        menuList.setSelectedValue(backupEntry, true);
        backupDirectoryTextField.requestFocusInWindow();
        showErrorMessage(errorMessage);
    }

    private boolean checkFirewall() {
        for (int i = 0; i < ipTableModel.getRowCount(); i++) {
            if (!checkTarget((String) ipTableModel.getValueAt(i, 1), i)) {
                return false;
            }
            if (!checkPortRange((String) ipTableModel.getValueAt(i, 2), i)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkPortRange(String portRange, int index) {
        String[] tokens = portRange.split(":");
        switch (tokens.length) {
            case 1:
                // simple port
                if (!(checkPortString(tokens[0], index))) {
                    return false;
                }
                return true;

            case 2:
                // port range
                if (!(checkPortString(tokens[0], index))) {
                    return false;
                }
                if (!(checkPortString(tokens[1], index))) {
                    return false;
                }
                return true;

            default:
                // invalid syntax
                portRangeError(index);
                return false;
        }
    }

    private boolean checkPortString(String portString, int index) {
        try {
            int portNumber = Integer.parseInt(portString);
            if ((portNumber < 0) || (portNumber > 65535)) {
                portRangeError(index);
                return false;
            }
        } catch (NumberFormatException ex) {
            portRangeError(index);
            return false;
        }
        return true;
    }

    private void portRangeError(int index) {
        String errorMessage = BUNDLE.getString("Error_PortRange");
        firewallError(errorMessage, index, 2);
    }

    private boolean checkTarget(String target, int index) {
        // a CIDR block has the syntax: <IP address>\<prefix length>
        String octetP = "\\p{Digit}{1,3}";
        String ipv4P = "(?:" + octetP + "\\.){3}" + octetP;
        Pattern cidrPattern = Pattern.compile("(" + ipv4P + ")/(\\p{Digit}*)");
        Matcher matcher = cidrPattern.matcher(target);
        if (matcher.matches()) {
            // check CIDR block syntax
            if (!checkIPv4Address(matcher.group(1), index)) {
                return false;
            }

            String prefixLengthString = matcher.group(2);
            try {
                int prefixLength = Integer.parseInt(prefixLengthString);
                if (prefixLength < 0 || prefixLength > 32) {
                    String errorMessage
                            = BUNDLE.getString("Error_PrefixLength");
                    errorMessage = MessageFormat.format(
                            errorMessage, prefixLengthString);
                    firewallError(errorMessage, index, 1);
                    return false;
                }
            } catch (NumberFormatException ex) {
                LOGGER.log(Level.WARNING,
                        "could not parse " + prefixLengthString, ex);
            }
            return true;

        } else {
            // check validity of plain IPv4 address or hostname
            Pattern ipv4Pattern = Pattern.compile(ipv4P);
            matcher = ipv4Pattern.matcher(target);
            if (matcher.matches()) {
                if (!checkIPv4Address(target, index)) {
                    return false;
                }
            } else {
                if (!checkHostName(target, index)) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean checkHostName(String string, int index) {
        // Hostnames are composed of series of labels concatenated with dots, as
        // are all domain names. For example, "en.wikipedia.org" is a hostname.
        // Each label must be between 1 and 63 characters long, and the entire
        // hostname (including the delimiting dots) has a maximum of 255
        // characters.
        // The Internet standards (Request for Comments) for protocols mandate
        // that component hostname labels may contain only the ASCII letters
        // 'a' through 'z' (in a case-insensitive manner), the digits '0'
        // through '9', and the hyphen ('-').

        if (string.isEmpty()) {
            String errorMessage = BUNDLE.getString("Error_No_Hostname");
            firewallError(errorMessage, index, 1);
            return false;
        }

        if (string.length() > 255) {
            String errorMessage = BUNDLE.getString("Error_HostnameLength");
            errorMessage = MessageFormat.format(errorMessage, string);
            firewallError(errorMessage, index, 1);
            return false;
        }

        String[] labels = string.split("\\.");
        for (String label : labels) {
            if (label.length() > 63) {
                String errorMessage = BUNDLE.getString("Error_LabelLength");
                errorMessage = MessageFormat.format(errorMessage, label);
                firewallError(errorMessage, index, 1);
                return false;
            }
            for (int i = 0, length = label.length(); i < length; i++) {
                char c = label.charAt(i);
                if ((c != '-')
                        && ((c < '0') || (c > '9'))
                        && ((c < 'A') || (c > 'Z'))
                        && ((c < 'a') || (c > 'z'))) {
                    String errorMessage = BUNDLE.getString(
                            "Error_Invalid_Hostname_Character");
                    errorMessage = MessageFormat.format(errorMessage, c);
                    firewallError(errorMessage, index, 1);
                    return false;
                }
            }
        }

        return true;
    }

    private boolean checkIPv4Address(String string, int index) {
        String[] octetStrings = string.split("\\.");
        for (String octetString : octetStrings) {
            int octet = Integer.parseInt(octetString);
            if (octet < 0 || octet > 255) {
                String errorMessage = BUNDLE.getString("Error_Octet");
                errorMessage = MessageFormat.format(
                        errorMessage, string, octetString);
                firewallError(errorMessage, index, 1);
                return false;
            }
        }
        return true;
    }

    private void firewallError(String errorMessage, int row, int column) {
        menuList.setSelectedValue(firewallEntry, true);
        firewallTabbedPane.setSelectedIndex(0);
        firewallIPTable.clearSelection();
        firewallIPTable.addRowSelectionInterval(row, row);
        firewallIPTable.editCellAt(row, column);
        firewallIPTable.getEditorComponent().requestFocus();
        showErrorMessage(errorMessage);
    }

    private void updateJBackpackProperties(
            String backupSource, String backupDestination) {
        // update JBackpack preferences of the default user
        File prefsDirectory = new File(
                "/home/user/.java/.userPrefs/ch/fhnw/jbackpack/");
        updateJBackpackProperties(
                prefsDirectory, backupSource, backupDestination, true);

        // update JBackpack preferences of the root user
        prefsDirectory = new File(
                "/root/.java/.userPrefs/ch/fhnw/jbackpack/");
        updateJBackpackProperties(
                prefsDirectory, backupSource, backupDestination, false);
    }

    private void updateJBackpackProperties(File prefsDirectory,
            String backupSource, String backupDestination, boolean chown) {
        File prefsFile = new File(prefsDirectory, "prefs.xml");
        String prefsFilePath = prefsFile.getPath();
        if (prefsFile.exists()) {
            try {
                Document xmlBootDocument = parseXmlFile(prefsFile);
                xmlBootDocument.getDocumentElement().normalize();
                Node mapNode
                        = xmlBootDocument.getElementsByTagName("map").item(0);
                Element mapElement = (Element) mapNode;
                NodeList entries = mapElement.getElementsByTagName("entry");
                for (int i = 0, length = entries.getLength(); i < length; i++) {
                    Element entry = (Element) entries.item(i);
                    String key = entry.getAttribute("key");
                    switch (key) {
                        case "destination":
                            entry.setAttribute("value", "local");
                            break;
                        case "local_destination_directory":
                            entry.setAttribute("value", backupDestination);
                            break;
                        case "source":
                            entry.setAttribute("value", backupSource);
                            break;
                    }
                }

                // write changes back to config file
                File tmpFile = File.createTempFile("lernstickWelcome", "tmp");
                TransformerFactory transformerFactory
                        = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
                        "http://java.sun.com/dtd/preferences.dtd");
                DOMSource source = new DOMSource(xmlBootDocument);
                StreamResult result = new StreamResult(tmpFile);
                transformer.transform(source, result);
                processExecutor.executeProcess(
                        "mv", tmpFile.getPath(), prefsFilePath);
                processExecutor.executeProcess(
                        "chown", "user.user", prefsFilePath);

            } catch (ParserConfigurationException | SAXException |
                    IOException | DOMException | TransformerException ex) {
                LOGGER.log(Level.WARNING, "can not update xmlboot config", ex);
            }

        } else {
            if (!prefsDirectory.exists() && !prefsDirectory.mkdirs()) {
                LOGGER.log(Level.WARNING,
                        "could not create directory {0}", prefsDirectory);
                return;
            }
            // create mininal JBackpack preferences
            String preferences
                    = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                    + "<!DOCTYPE map SYSTEM \"http://java.sun.com/dtd/preferences.dtd\">\n"
                    + "<map MAP_XML_VERSION=\"1.0\">\n"
                    + "  <entry key=\"destination\" value=\"local\"/>\n"
                    + "  <entry key=\"local_destination_directory\" value=\"" + backupDestination + "\"/>\n"
                    + "  <entry key=\"source\" value=\"" + backupSource + "\"/>\n"
                    + "</map>\n";

            try (FileWriter fileWriter = new FileWriter(prefsFile)) {
                fileWriter.write(preferences);
                fileWriter.flush();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "", ex);
            }
            if (chown) {
                processExecutor.executeProcess(
                        "chown", "-R", "user.user", "/home/user/.java/");
            }
        }
    }

    private static List<String> readFile(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            for (String line = reader.readLine(); line != null;
                    line = reader.readLine()) {
                lines.add(line);
            }
        }
        return lines;
    }

    private void updatePackagesLists() {
        // make sure that update-notifier does not get into our way
        String script = "#!/bin/sh\n"
                + "mykill() {\n"
                + "   ID=`ps -u 0 | grep \"${1}\" | awk '{ print $1 }'`\n"
                + "   if [ -n \"${ID}\" ]\n"
                + "   then\n"
                + "       kill -9 ${ID}\n"
                + "   fi\n"
                + "}\n"
                + "mykill /usr/lib/update-notifier/apt-check\n"
                + "mykill update-notifier";

        try {
            int exitValue = processExecutor.executeScript(script);
            if (exitValue != 0) {
                LOGGER.log(Level.WARNING, "Could not kill update-notifier: {0}",
                        processExecutor.getOutput());
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        // update packaging information
        ProgressDialog updateDialog = new ProgressDialog(this,
                new ImageIcon(getClass().getResource(
                                "/ch/fhnw/lernstickwelcome/icons/download_anim.gif")));
        updateDialog.setProgressBarVisible(false);
        updateDialog.setTitle(null);
        PackageListUpdater packageListUpdater
                = new PackageListUpdater(updateDialog);
        packageListUpdater.execute();
        updateDialog.setVisible(true);
        try {
            if (!packageListUpdater.get()) {
                UpdateErrorDialog dialog
                        = new UpdateErrorDialog(this, aptGetOutput);
                dialog.setVisible(true);
            }
        } catch (InterruptedException | ExecutionException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void installSelectedPackages() {

        // calculate number of packages
        int numberOfPackages = 0;

        // non-free software
        numberOfPackages += flashCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += additionalFontsCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += multimediaCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += googleEarthCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += skypeCheckBox.isSelected() ? 1 : 0;

        // LA teaching tools
        numberOfPackages += laCheckBox.isSelected() ? 1 : 0;

        // miscellaneous
        numberOfPackages += netbeansPanel.isSelected() ? 1 : 0;
        numberOfPackages += processingPanel.isSelected() ? 1 : 0;
        numberOfPackages += rStudioPanel.isSelected() ? 1 : 0;
        numberOfPackages += lazarusPanel.isSelected() ? 1 : 0;
        numberOfPackages += openClipartPanel.isSelected() ? 1 : 0;
        numberOfPackages += sweetHome3DPanel.isSelected() ? 1 : 0;
        numberOfPackages += gnucashPanel.isSelected() ? 1 : 0;
        numberOfPackages += rosegardenPanel.isSelected() ? 1 : 0;
        numberOfPackages += wizbeePanel.isSelected() ? 1 : 0;
        numberOfPackages += calcularisPanel.isSelected() ? 1 : 0;
        numberOfPackages += lehrerOfficePanel.isSelected() ? 1 : 0;

        // games
        numberOfPackages += colobotGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += riliGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += filletsGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += neverballGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += neverputtGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += freecolGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += minetestGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += frogattoGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += supertuxGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += supertuxkartGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += xmotoGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += triggerGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += openClonkPanel.isSelected() ? 1 : 0;
        numberOfPackages += wesnothGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += flareGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += hedgewarsGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += megaglestGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += astromenaceGamePanel.isSelected() ? 1 : 0;

        LOGGER.log(Level.INFO, "number of packages = {0}", numberOfPackages);

        if (numberOfPackages > 0) {

            updatePackagesLists();

            final ProgressDialog progressDialog = new ProgressDialog(this,
                    new ImageIcon(getClass().getResource(
                                    "/ch/fhnw/lernstickwelcome/icons/download_anim.gif")));

            Installer installer
                    = new Installer(progressDialog, numberOfPackages);
            installer.addPropertyChangeListener(
                    new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            if ("progress".equals(evt.getPropertyName())) {
                                Integer progress = (Integer) evt.getNewValue();
                                progressDialog.setProgress(progress);
                            }
                        }
                    });
            installer.execute();
            progressDialog.setVisible(true);
            checkAllPackages();
        }
    }

    private void checkAllPackages() {
        // check which applications are already installed

        // nonfree software
        checkInstall(flashCheckBox, flashLabel,
                "Welcome.flashLabel.text", FLASH_PACKAGES);
        checkInstall(additionalFontsCheckBox, fontsLabel,
                "Welcome.fontsLabel.text", FONTS_PACKAGES);
        checkInstall(multimediaCheckBox, multimediaLabel,
                "Welcome.multimediaLabel.text", MULTIMEDIA_PACKAGES);
        checkInstall(googleEarthCheckBox, googleEarthLabel,
                "Welcome.googleEarthLabel.text", "google-earth-stable");
        checkInstall(skypeCheckBox, skypeLabel,
                "Welcome.skypeLabel.text", "skype");

        // LA Teaching System
        checkInstall(laCheckBox, laLabel,
                "Welcome.laLabel.text", "lateaching");

        // miscellaneous
        checkAppInstall(netbeansPanel, "lernstick-netbeans-wheezy");
        checkAppInstall(processingPanel, "processing");
        checkAppInstall(rStudioPanel, "rstudio");
        checkAppInstall(lazarusPanel, "lazarus");
        checkAppInstall(openClipartPanel, "openclipart-libreoffice");
        checkAppInstall(sweetHome3DPanel, "sweethome3d");
        checkAppInstall(gnucashPanel, "gnucash");
        checkAppInstall(rosegardenPanel, "rosegarden");
        checkAppInstall(wizbeePanel, "wizbee");
        checkAppInstall(calcularisPanel, "calcularis-de");
        checkAppInstall(lehrerOfficePanel, "lehreroffice");

        // games
        checkAppInstall(colobotGamePanel, "colobot");
        checkAppInstall(riliGamePanel, "ri-li");
        checkAppInstall(filletsGamePanel, "lernstick-fillets-ng");
        checkAppInstall(neverballGamePanel, "live-neverball2");
        checkAppInstall(neverputtGamePanel, "live-neverputt2");
        checkAppInstall(frogattoGamePanel, "frogatto");
        checkAppInstall(freecolGamePanel, "freecol");
        checkAppInstall(minetestGamePanel, "minetest");
        checkAppInstall(supertuxGamePanel, "live-supertux");
        checkAppInstall(supertuxkartGamePanel, "supertuxkart");
        checkAppInstall(xmotoGamePanel, "xmoto");
        checkAppInstall(triggerGamePanel, "lernstick-trigger-rally");
        checkAppInstall(openClonkPanel, "openclonk");
        checkAppInstall(wesnothGamePanel, "wesnoth-1.12");
        checkAppInstall(flareGamePanel, "flare-game");
        checkAppInstall(hedgewarsGamePanel, "hedgewars");
        checkAppInstall(megaglestGamePanel, "megaglest");
        checkAppInstall(astromenaceGamePanel, "lernstick-astromenace");
    }

    private void checkInstall(JCheckBox checkBox, JLabel label,
            String guiKey, String... packages) {
        if (arePackagesInstalled(packages)) {
            checkBox.setSelected(false);
            String newString = BUNDLE.getString("Already_Installed_Template");
            newString = MessageFormat.format(
                    newString, BUNDLE.getString(guiKey));
            label.setText(newString);
        }
    }

    private void checkAppInstall(GamePanel gamePanel, String... packages) {
        gamePanel.setInstalled(arePackagesInstalled(packages));
    }

    private boolean arePackagesInstalled(String... packages) {
        int length = packages.length;
        String[] commandArray = new String[length + 2];
        commandArray[0] = "dpkg";
        commandArray[1] = "-l";
        System.arraycopy(packages, 0, commandArray, 2, length);
        processExecutor.executeProcess(true, true, commandArray);
        List<String> stdOut = processExecutor.getStdOutList();
        for (String packageName : packages) {
            LOGGER.log(Level.INFO, "checking package {0}", packageName);
            Pattern pattern = Pattern.compile("^ii  " + packageName + ".*");
            boolean found = false;
            for (String line : stdOut) {
                if (pattern.matcher(line).matches()) {
                    LOGGER.info("match");
                    found = true;
                    break;
                } else {
                    LOGGER.info("no match");
                }
            }
            if (!found) {
                LOGGER.log(Level.INFO,
                        "package {0} not installed", packageName);
                return false;
            }
        }
        return true;
    }

    private void toggleCheckBox(JCheckBox checkBox) {
        if (checkBox.isEnabled()) {
            checkBox.setSelected(!checkBox.isSelected());
        }
    }

    private void showErrorMessage(String errorMessage) {
        JOptionPane.showMessageDialog(this, errorMessage,
                BUNDLE.getString("Error"), JOptionPane.ERROR_MESSAGE);

    }

    private class PackageListUpdater
            extends SwingWorker<Boolean, ProgressAction> {

        private final ProgressDialog progressDialog;

        public PackageListUpdater(ProgressDialog progressDialog) {
            this.progressDialog = progressDialog;
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            String infoString = BUNDLE.getString("Updating_Packagelist");
            Icon icon = new ImageIcon(getClass().getResource(
                    "/ch/fhnw/lernstickwelcome/icons/package.png"));
            publish(new ProgressAction(infoString, icon));

            String updateScript = "cd " + USER_HOME + '\n'
                    + "apt-get" + getAptGetProxyLine() + "update";
            int exitValue = processExecutor.executeScript(
                    true, true, updateScript);
            if (exitValue != 0) {
                aptGetOutput = processExecutor.getOutput();
                String logMessage = "apt-get failed with the following "
                        + "output:\n" + aptGetOutput;
                LOGGER.severe(logMessage);
                return false;
            }
            return true;
        }

        @Override
        protected void process(List<ProgressAction> appInfos) {
            progressDialog.setProgressAction(appInfos.get(0));
        }

        @Override
        protected void done() {
            progressDialog.setVisible(false);
        }
    }

    private class Installer extends SwingWorker<Boolean, ProgressAction> {

        private final ProgressDialog progressDialog;
        private final int numberOfPackages;
        private int currentPackage;

        public Installer(
                ProgressDialog progressDialog, int numberOfPackages) {
            this.progressDialog = progressDialog;
            this.numberOfPackages = numberOfPackages;
        }

        @Override
        protected Boolean doInBackground() throws Exception {

            // nonfree packages
            installNonFreeApplication(flashCheckBox, "Welcome.flashLabel.text",
                    "/ch/fhnw/lernstickwelcome/icons/48x48/Adobe_Flash_cs3.png",
                    FLASH_PACKAGES);
            installNonFreeApplication(additionalFontsCheckBox,
                    "Welcome.fontsLabel.text",
                    "/ch/fhnw/lernstickwelcome/icons/48x48/fonts.png",
                    FONTS_PACKAGES);
            installNonFreeApplication(multimediaCheckBox,
                    "Welcome.multimediaLabel.text",
                    "/ch/fhnw/lernstickwelcome/icons/48x48/package_multimedia.png",
                    MULTIMEDIA_PACKAGES);
            installGoogleEarth();
            installSkype();

            // teaching system
            installNonFreeApplication(laCheckBox, "LA_Teaching_System",
                    "/ch/fhnw/lernstickwelcome/icons/48x48/LinuxAdvanced.png",
                    "lateaching", "lateachingtools");

            // miscellaneous
            installApplication(netbeansPanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/netbeans.png",
                    "lernstick-netbeans-wheezy",
                    "openjdk-7-source", "openjdk-7-doc");
            installApplication(processingPanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/processing.png",
                    "processing");
            installApplication(rStudioPanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/rstudio.png",
                    "rstudio");
            installApplication(lazarusPanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/lazarus.png",
                    "lazarus", "fpc-source", "lcl",
                    "fp-units-gfx", "fp-units-gtk", "fp-units-misc");
            installApplication(openClipartPanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/openclipart.png",
                    "openclipart-libreoffice");
            installApplication(sweetHome3DPanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/sweethome3d.png",
                    "sweethome3d", "sweethome3d-furniture",
                    "sweethome3d-furniture-nonfree");
            installApplication(gnucashPanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/gnucash.png",
                    "gnucash", "gnucash-docs");
            installApplication(rosegardenPanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/rosegarden.png",
                    "rosegarden", "fluid-soundfont-gm",
                    "fluid-soundfont-gs", "fluidsynth-dssi");
            installApplication(wizbeePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/wizbee.png",
                    "wizbee");
            installApplication(calcularisPanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/calcularis.png",
                    "calcularis-de");
            installApplication(lehrerOfficePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/lehreroffice.png",
                    "lehreroffice");

            // games
            installApplication(colobotGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/colobot.png",
                    "colobot");
            installApplication(riliGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/ri-li.png",
                    "ri-li");
            installApplication(filletsGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/fillets.png",
                    "lernstick-fillets-ng", "fillets-ng-data-cs");
            installApplication(neverballGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/neverball.png",
                    "live-neverball2");
            installApplication(neverputtGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/neverputt.png",
                    "live-neverputt2");
            installApplication(freecolGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/freecol.png",
                    "freecol");
            installApplication(minetestGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/minetest.png",
                    "minetest", "minetest-mod-moreblocks",
                    "minetest-mod-moreores", "minetest-mod-pipeworks",
                    "minetest-mod-worldedit", "minetest-mod-mobf");
            installApplication(frogattoGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/frogatto.png",
                    "frogatto");
            installApplication(supertuxGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/supertux.png",
                    "live-supertux");
            installApplication(supertuxkartGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/supertuxkart.png",
                    "live-supertuxkart");
            installApplication(xmotoGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/xmoto.png",
                    "live-xmoto");
            installApplication(triggerGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/trigger.png",
                    "lernstick-trigger-rally");
            installApplication(openClonkPanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/openclonk.png",
                    "openclonk");
            installApplication(wesnothGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/wesnoth.png",
                    "wesnoth-1.12", "wesnoth-1.12-music");
            installApplication(flareGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/flare.png",
                    "flare-game");
            installApplication(hedgewarsGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/hedgewars.png",
                    "hedgewars");
            installApplication(megaglestGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/megaglest.png",
                    "megaglest");
            installApplication(astromenaceGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/astromenace.png",
                    "lernstick-astromenace");

            return null;
        }

        @Override
        protected void process(List<ProgressAction> appInfos) {
            progressDialog.setProgressAction(appInfos.get(0));
        }

        @Override
        protected void done() {
            progressDialog.setVisible(false);
        }

        private void installSkype() throws IOException {
            if (!skypeCheckBox.isSelected()) {
                return;
            }
            String infoString = BUNDLE.getString("Installing");
            infoString = MessageFormat.format(infoString,
                    BUNDLE.getString("Welcome.skypeLabel.text"));
            Icon icon = new ImageIcon(getClass().getResource(
                    "/ch/fhnw/lernstickwelcome/icons/48x48/skype.png"));
            ProgressAction progressAction
                    = new ProgressAction(infoString, icon);
            publish(progressAction);
            String skypeInstallScript = "#!/bin/sh\n"
                    + "wget -O skype-install.deb http://www.skype.com/go/getskype-linux-deb\n"
                    + "dpkg -i skype-install.deb\n"
                    + "apt-get -f install\n"
                    + "rm skype-install.deb";
            int exitValue = processExecutor.executeScript(
                    true, true, skypeInstallScript);
            if (exitValue != 0) {
                String errorMessage = "Installation of Skype failed"
                        + "with the following error message:\n"
                        + processExecutor.getOutput();
                LOGGER.severe(errorMessage);
                showErrorMessage(errorMessage);
            }
            updateProgress();
        }

        private void installGoogleEarth() throws IOException {
            if (!googleEarthCheckBox.isSelected()) {
                return;
            }
            String infoString = BUNDLE.getString("Installing");
            infoString = MessageFormat.format(infoString,
                    BUNDLE.getString("Welcome.googleEarthLabel.text"));
            Icon icon = new ImageIcon(getClass().getResource(
                    "/ch/fhnw/lernstickwelcome/icons/48x48/googleearth-icon.png"));
            ProgressAction progressAction
                    = new ProgressAction(infoString, icon);
            publish(progressAction);

            // old version with googleearth-package
//            String googleEarthInstallScript = "cd " + USER_HOME + '\n'
//                    + "apt-get -y --force-yes install googleearth-package lsb-core\n"
//                    + "make-googleearth-package --force\n"
//                    + "dpkg -i googleearth_*\n"
//                    + "rm googleearth_*";
            // new version with direct download link
            String debName = "google-earth-stable_current_i386.deb";
            String googleEarthInstallScript = "apt-get" + getAptGetProxyLine()
                    + "-y --force-yes install lsb-core\n"
                    + "cd " + USER_HOME + '\n'
                    + "wget" + getWgetProxyLine()
                    + "http://dl.google.com/dl/earth/client/current/" + debName + '\n'
                    + "dpkg -i " + debName + '\n'
                    + "rm " + debName;
            int exitValue = processExecutor.executeScript(
                    true, true, googleEarthInstallScript);
            if (exitValue != 0) {
                String errorMessage = "Installation of GoogleEarth failed"
                        + "with the following error message:\n"
                        + processExecutor.getOutput();
                LOGGER.severe(errorMessage);
                showErrorMessage(errorMessage);
            }
            updateProgress();
        }

        private void installGoogleChrome() throws IOException {
            String infoString = BUNDLE.getString("Installing");
            infoString = MessageFormat.format(infoString,
                    BUNDLE.getString("Welcome.googleChromeLabel.text"));
            Icon icon = new ImageIcon(getClass().getResource(
                    "/ch/fhnw/lernstickwelcome/icons/48x48/chrome.png"));
            ProgressAction progressAction
                    = new ProgressAction(infoString, icon);
            publish(progressAction);

            String debName = "google-chrome-stable_current_i386.deb";
            String googleEarthInstallScript = "wget" + getWgetProxyLine()
                    + "https://dl.google.com/linux/direct/" + debName + '\n'
                    + "dpkg -i " + debName + '\n'
                    + "rm " + debName;
            int exitValue = processExecutor.executeScript(
                    true, true, googleEarthInstallScript);
            if (exitValue != 0) {
                String errorMessage = "Installation of Google Chrome failed"
                        + "with the following error message:\n"
                        + processExecutor.getOutput();
                LOGGER.severe(errorMessage);
                showErrorMessage(errorMessage);
            }
            updateProgress();
        }

        private void installNonFreeApplication(JCheckBox checkBox, String key,
                String iconPath, String... packageNames) {
            if (!checkBox.isSelected()) {
                LOGGER.log(Level.INFO, "checkBox not selected: {0}", checkBox);
                return;
            }
            String infoString = MessageFormat.format(
                    BUNDLE.getString("Installing"), BUNDLE.getString(key));
            installPackage(infoString, iconPath, packageNames);
        }

        private void installApplication(GamePanel gamePanel, String iconPath,
                String... packageNames) {
            if (!gamePanel.isSelected()) {
                LOGGER.log(Level.INFO,
                        "gamePanel not selected: {0}", gamePanel.getGameName());
                return;
            }
            String infoString = MessageFormat.format(
                    BUNDLE.getString("Installing"), gamePanel.getGameName());
            installPackage(infoString, iconPath, packageNames);
        }

        private void installPackage(String infoString,
                String iconPath, String... packageNames) {
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            ProgressAction progressAction
                    = new ProgressAction(infoString, icon);
            publish(progressAction);

            for (String packageName : packageNames) {
                LOGGER.log(Level.INFO,
                        "installing package \"{0}\"", packageName);
            }
            StringBuilder builder = new StringBuilder();
            builder.append("#!/bin/sh\n"
                    + "export DEBIAN_FRONTEND=noninteractive\n");
            builder.append("apt-get ");
            if (proxyCheckBox.isSelected()) {
                builder.append("-o ");
                builder.append(getAptGetAcquireLine());
                builder.append(' ');
            }
            builder.append("-y --force-yes install ");
            for (String packageName : packageNames) {
                builder.append(packageName);
                builder.append(' ');
            }
            String script = builder.toString();

//            // enforce non-interactive installs
//            Map<String,String> environment = new HashMap<String, String>();
//            environment.put("DEBIAN_FRONTEND", "noninteractive");
//            processExecutor.setEnvironment(environment);
            int exitValue = -1;
            try {
                exitValue = processExecutor.executeScript(true, true, script);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "", ex);
            } finally {
                if (exitValue != 0) {
                    String errorMessage = "apt-get failed with the following "
                            + "output:\n" + processExecutor.getOutput();
                    LOGGER.severe(errorMessage);
                    showErrorMessage(errorMessage);
                }
            }
            updateProgress();
        }

        private void updateProgress() {
            currentPackage++;
            setProgress((100 * currentPackage) / numberOfPackages);
        }
    }

    private class MyListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            MainMenuListEntry entry = (MainMenuListEntry) value;
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, entry.getText(), index, isSelected, cellHasFocus);
            label.setIcon(entry.getIcon());
            label.setBorder(new EmptyBorder(5, 5, 5, 5));
            return label;
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addIPButton;
    private javax.swing.JCheckBox additionalFontsCheckBox;
    private javax.swing.JLabel additionalInfoLabel;
    private javax.swing.JPanel additionalMiscPanel;
    private javax.swing.JPanel additionalPanel;
    private javax.swing.JScrollPane additionalScrollPane;
    private javax.swing.JTabbedPane additionalTabbedPane;
    private javax.swing.JButton applyButton;
    private ch.fhnw.lernstickwelcome.GamePanel astromenaceGamePanel;
    private javax.swing.JCheckBox backupCheckBox;
    private javax.swing.JPanel backupDestinationsPanel;
    private javax.swing.JButton backupDirectoryButton;
    private javax.swing.JCheckBox backupDirectoryCheckBox;
    private javax.swing.JLabel backupDirectoryLabel;
    private javax.swing.JTextField backupDirectoryTextField;
    private javax.swing.JLabel backupFrequencyEveryLabel;
    private javax.swing.JLabel backupFrequencyMinuteLabel;
    private javax.swing.JSpinner backupFrequencySpinner;
    private javax.swing.JPanel backupPanel;
    private javax.swing.JCheckBox backupPartitionCheckBox;
    private javax.swing.JLabel backupPartitionLabel;
    private javax.swing.JTextField backupPartitionTextField;
    private javax.swing.JButton backupSourceButton;
    private javax.swing.JLabel backupSourceLabel;
    private javax.swing.JPanel backupSourcePanel;
    private javax.swing.JTextField backupSourceTextField;
    private javax.swing.JPanel bootMenuPanel;
    private javax.swing.JLabel bootTimeoutLabel;
    private javax.swing.JSpinner bootTimeoutSpinner;
    private javax.swing.JPanel bottomPanel;
    private ch.fhnw.lernstickwelcome.GamePanel calcularisPanel;
    private javax.swing.JButton cancelButton;
    private ch.fhnw.lernstickwelcome.GamePanel colobotGamePanel;
    private javax.swing.JPanel dataPartitionPanel;
    private javax.swing.JCheckBox exchangeAccessCheckBox;
    private javax.swing.JLabel exchangePartitionNameLabel;
    private javax.swing.JTextField exchangePartitionNameTextField;
    private javax.swing.JPanel exchangePartitionPanel;
    private javax.swing.JLabel exchangeRebootLabel;
    private javax.swing.JPanel fillPanel;
    private ch.fhnw.lernstickwelcome.GamePanel filletsGamePanel;
    private javax.swing.JPanel firewallIPButtonPanel;
    private javax.swing.JScrollPane firewallIPScrollPane;
    private javax.swing.JTable firewallIPTable;
    private javax.swing.JLabel firewallInfoLabel;
    private javax.swing.JPanel firewallPanel;
    private javax.swing.JButton firewallStartStopButton;
    private javax.swing.JLabel firewallStatusLabel;
    private javax.swing.JTabbedPane firewallTabbedPane;
    private javax.swing.JPanel firewallURLPanel;
    private javax.swing.JScrollPane firewallURLScrollPane;
    private javax.swing.JTextArea firewallURLTextArea;
    private javax.swing.JPanel firewallipv4Panel;
    private ch.fhnw.lernstickwelcome.GamePanel flareGamePanel;
    private javax.swing.JCheckBox flashCheckBox;
    private javax.swing.JLabel flashLabel;
    private javax.swing.JLabel fontsLabel;
    private ch.fhnw.lernstickwelcome.GamePanel freecolGamePanel;
    private ch.fhnw.lernstickwelcome.GamePanel frogattoGamePanel;
    private javax.swing.JScrollPane gamesScrollPane;
    private javax.swing.JPanel gamesScrollPanel;
    private ch.fhnw.lernstickwelcome.GamePanel gnucashPanel;
    private javax.swing.JCheckBox googleEarthCheckBox;
    private javax.swing.JLabel googleEarthLabel;
    private ch.fhnw.lernstickwelcome.GamePanel hedgewarsGamePanel;
    private javax.swing.JScrollPane helpScrollPane;
    private javax.swing.JTextPane helpTextPane;
    private javax.swing.JEditorPane infoEditorPane;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JScrollPane infoScrollPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JCheckBox kdePlasmaLockCheckBox;
    private javax.swing.JCheckBox laCheckBox;
    private javax.swing.JLabel laLabel;
    private javax.swing.JLabel label1;
    private javax.swing.JLabel label2;
    private ch.fhnw.lernstickwelcome.GamePanel lazarusPanel;
    private ch.fhnw.lernstickwelcome.GamePanel lehrerOfficePanel;
    private javax.swing.JPanel mainCardPanel;
    private ch.fhnw.lernstickwelcome.GamePanel megaglestGamePanel;
    private javax.swing.JList menuList;
    private javax.swing.JScrollPane menuScrollPane;
    private ch.fhnw.lernstickwelcome.GamePanel minetestGamePanel;
    private javax.swing.JPanel miscPanel;
    private javax.swing.JButton moveDownIPButton;
    private javax.swing.JButton moveUpIPButton;
    private javax.swing.JCheckBox multimediaCheckBox;
    private javax.swing.JLabel multimediaLabel;
    private javax.swing.JPanel navigaionPanel;
    private ch.fhnw.lernstickwelcome.GamePanel netbeansPanel;
    private ch.fhnw.lernstickwelcome.GamePanel neverballGamePanel;
    private ch.fhnw.lernstickwelcome.GamePanel neverputtGamePanel;
    private javax.swing.JButton nextButton;
    private javax.swing.JCheckBox noPulseAudioCheckbox;
    private javax.swing.JLabel nonfreeLabel;
    private javax.swing.JPanel nonfreePanel;
    private ch.fhnw.lernstickwelcome.GamePanel openClipartPanel;
    private ch.fhnw.lernstickwelcome.GamePanel openClonkPanel;
    private javax.swing.JPanel partitionsPanel;
    private javax.swing.JButton passwordChangeButton;
    private javax.swing.JLabel passwordChangeInfoLabel;
    private javax.swing.JPanel passwordChangePanel;
    private javax.swing.JPasswordField passwordField1;
    private javax.swing.JPasswordField passwordField2;
    private javax.swing.JButton previousButton;
    private ch.fhnw.lernstickwelcome.GamePanel processingPanel;
    private javax.swing.JCheckBox proxyCheckBox;
    private javax.swing.JLabel proxyHostLabel;
    private javax.swing.JTextField proxyHostTextField;
    private javax.swing.JLabel proxyInfoLabel;
    private javax.swing.JPanel proxyPanel;
    private javax.swing.JPasswordField proxyPasswordField;
    private javax.swing.JLabel proxyPasswordLabel;
    private javax.swing.JLabel proxyPortLabel;
    private javax.swing.JFormattedTextField proxyPortTextField;
    private javax.swing.JLabel proxyUserNameLabel;
    private javax.swing.JTextField proxyUserNameTextField;
    private ch.fhnw.lernstickwelcome.GamePanel rStudioPanel;
    private javax.swing.JCheckBox readOnlyCheckBox;
    private javax.swing.JPanel readOnlyPanel;
    private javax.swing.JCheckBox readWriteCheckBox;
    private javax.swing.JPanel readWritePanel;
    private javax.swing.JPanel recommendedPanel;
    private javax.swing.JButton removeIPButton;
    private ch.fhnw.lernstickwelcome.GamePanel riliGamePanel;
    private ch.fhnw.lernstickwelcome.GamePanel rosegardenPanel;
    private javax.swing.JCheckBox screenShotCheckBox;
    private javax.swing.JLabel secondsLabel;
    private javax.swing.JCheckBox skypeCheckBox;
    private javax.swing.JLabel skypeLabel;
    private ch.fhnw.lernstickwelcome.GamePanel supertuxGamePanel;
    private ch.fhnw.lernstickwelcome.GamePanel supertuxkartGamePanel;
    private ch.fhnw.lernstickwelcome.GamePanel sweetHome3DPanel;
    private javax.swing.JLabel systemNameLabel;
    private javax.swing.JTextField systemNameTextField;
    private javax.swing.JPanel systemPanel;
    private javax.swing.JLabel systemVersionLabel;
    private javax.swing.JTextField systemVersionTextField;
    private javax.swing.JEditorPane teachingEditorPane;
    private javax.swing.JPanel teachingPanel;
    private javax.swing.JScrollPane teachingScrollPane;
    private ch.fhnw.lernstickwelcome.GamePanel triggerGamePanel;
    private javax.swing.JLabel userNameLabel;
    private javax.swing.JTextField userNameTextField;
    private javax.swing.JLabel welcomeLabel;
    private ch.fhnw.lernstickwelcome.GamePanel wesnothGamePanel;
    private ch.fhnw.lernstickwelcome.GamePanel wizbeePanel;
    private ch.fhnw.lernstickwelcome.GamePanel xmotoGamePanel;
    // End of variables declaration//GEN-END:variables
}
