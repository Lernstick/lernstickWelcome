/*
 * Welcome.java
 *
 * Created on 01.04.2009, 14:11:23
 */
package ch.fhnw.lernstickwelcome;

import ch.fhnw.lernstickwelcome.IPTableEntry.Protocol;
import ch.fhnw.util.DbusTools;
import ch.fhnw.util.ProcessExecutor;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
import org.xml.sax.SAXException;

/**
 * The welcome window of the lernstick
 *
 * @author Ronny Standtke <Ronny.Standtke@gmx.net>
 */
public class Welcome extends javax.swing.JFrame {

    private static final Logger LOGGER =
            Logger.getLogger(Welcome.class.getName());
    private static final ResourceBundle BUNDLE =
            ResourceBundle.getBundle("ch/fhnw/lernstickwelcome/Bundle");
    private static final String SHOW_WELCOME = "ShowWelcome";
    private static final String SHOW_READ_ONLY_INFO = "ShowReadOnlyInfo";
    // !!! NO trailing slash at the end (would break comparison later) !!!
    private static final String IMAGE_DIRECTORY = "/lib/live/mount/medium";
    private static final String IP_TABLES_FILENAME =
            "/etc/lernstick-firewall/net_whitelist";
    private static final String URL_WHITELIST_FILENAME =
            "/etc/lernstick-firewall/url_whitelist";
    // !!! processExecutor must be instanciated before the next constants !!!
    private final static ProcessExecutor processExecutor =
            new ProcessExecutor();
    private static final boolean IMAGE_IS_WRITABLE = isImageWritable();
    private static final File SYSLINUX_CONFIG_FILE = getSyslinuxConfigFile();
    private static final File XMLBOOT_CONFIG_FILE = getXmlBootConfigFile();
    // mapping of checkboxes to package collections
//    private static final String[] ACROREAD_PACKAGES = new String[]{
//        "acroread", "acroread-l10n-de", "acroread-l10n-es", "acroread-l10n-fr",
//        "acroread-l10n-it", "acroread-dictionary-de", "acroread-dictionary-es",
//        "acroread-dictionary-fr", "acroread-dictionary-it", "acroread-doc-de",
//        "acroread-doc-es", "acroread-doc-fr", "acroread-doc-it",
//        "acroread-escript", "acroread-plugin-speech"
//    };
    // "ttf-pelikan-schulschriften" are currently unavailable    
    private static final String[] FONTS_PACKAGES = new String[]{
        "ttf-mscorefonts-installer"
    };
    // "mplayer-codecs" are currently unavailable
    private static final String[] MULTIMEDIA_PACKAGES = new String[]{
        "libdvdcss2", "libmp3lame0", "lame"
    };
    private static final String FLASH_PACKAGE =
            "flashplugin-nonfree";
    private final static String USER_HOME = System.getProperty("user.home");
    private final String adobeLanguageCode;
    private final File propertiesFile;
    private final Properties properties;
    private final Toolkit toolkit = Toolkit.getDefaultToolkit();
    private final String fullName;
    private final DefaultListModel menuListModel = new DefaultListModel();
    private final boolean examEnvironment;
    private int menuListIndex = 0;
    private String exchangePartition;
    private String exchangePartitionLabel;
    private String aptGetOutput;
    private IPTableModel ipTableModel;

    /**
     * Creates new form Welcome
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
            FileHandler fileHandler =
                    new FileHandler("%t/lernstickWelcome", 5000000, 2, true);
            fileHandler.setFormatter(formatter);
            fileHandler.setLevel(Level.ALL);
            globalLogger.addHandler(fileHandler);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        LOGGER.info("*********** Starting lernstick Welcome ***********");

        // determine adobe package name
        String language = Locale.getDefault().getLanguage();
        if (language.equals(new Locale("de").getLanguage())) {
            adobeLanguageCode = "deu";
        } else if (language.equals(new Locale("fr").getLanguage())) {
            adobeLanguageCode = "fra";
        } else {
            adobeLanguageCode = "enu";
        }

        boolean showAtStartup = false;
        boolean showReadOnlyInfo = true;
        String propertiesFileName = "/home/user" + File.separatorChar
                + ".config" + File.separatorChar + "lernstickWelcome";
        propertiesFile = new File(propertiesFileName);
        properties = new Properties();
        try {
            properties.load(new FileInputStream(propertiesFile));
            showAtStartup = "true".equals(
                    properties.getProperty(SHOW_WELCOME));
            showReadOnlyInfo = "true".equals(
                    properties.getProperty(SHOW_READ_ONLY_INFO));
        } catch (IOException ex) {
            LOGGER.log(Level.INFO,
                    "can not load properties from " + propertiesFile, ex);
        }

        initComponents();

        menuList.setModel(menuListModel);

        menuListModel.addElement(new MainMenuListEntry(
                "/ch/fhnw/lernstickwelcome/icons/messagebox_info.png",
                BUNDLE.getString("Information"), "infoPanel"));
        if (examEnvironment) {
            menuListModel.addElement(new MainMenuListEntry(
                    "/ch/fhnw/lernstickwelcome/icons/32x32/dialog-password.png",
                    BUNDLE.getString("Password"), "passwordChangePanel"));
            menuListModel.addElement(new MainMenuListEntry(
                    "/ch/fhnw/lernstickwelcome/icons/32x32/firewall.png",
                    BUNDLE.getString("Firewall"), "firewallPanel"));
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
        }
        menuListModel.addElement(new MainMenuListEntry(
                "/ch/fhnw/lernstickwelcome/icons/32x32/system-run.png",
                BUNDLE.getString("System"), "systemPanel"));
        menuListModel.addElement(new MainMenuListEntry(
                "/ch/fhnw/lernstickwelcome/icons/32x32/partitionmanager.png",
                BUNDLE.getString("Partitions"), "partitionsPanel"));

        menuList.setCellRenderer(new MyListCellRenderer());
        menuList.setSelectedIndex(0);

        checkAllPackages();

        // determine current full user name
        AbstractDocument userNameDocument =
                (AbstractDocument) userNameTextField.getDocument();
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

        AbstractDocument exchangePartitionNameDocument =
                (AbstractDocument) exchangePartitionNameTextField.getDocument();
        exchangePartitionNameDocument.setDocumentFilter(
                new DocumentSizeFilter());
        try {
            exchangePartition = getExchangePartition();
            if (exchangePartition == null) {
                exchangePartitionNameLabel.setEnabled(false);
                exchangePartitionNameTextField.setEnabled(false);
            } else {
                exchangePartitionLabel = getPartitionLabel(exchangePartition);
                exchangePartitionNameTextField.setText(exchangePartitionLabel);
            }
        } catch (DBusException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        // determine some boot properties
        // [sys/iso]linux timeout
        ((JSpinner.DefaultEditor) bootTimeoutSpinner.getEditor()).getTextField().setColumns(2);
        try {
            bootTimeoutSpinner.setValue(getTimeout());
        } catch (IOException ex) {
            LOGGER.warning("could not set boot timeout value");
        }
        updateSecondsLabel();
        // xmlboot system strings
        String systemName = null;
        String systemVersion = null;
        try {
            if (XMLBOOT_CONFIG_FILE != null) {
                Document xmlBootDocument = parseXmlFile(XMLBOOT_CONFIG_FILE);
                xmlBootDocument.getDocumentElement().normalize();
                Node systemNode = xmlBootDocument.getElementsByTagName("system").item(0);
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
        } catch (ParserConfigurationException ex) {
            LOGGER.log(Level.WARNING, "could not parse xmlboot config", ex);
        } catch (SAXException ex) {
            LOGGER.log(Level.WARNING, "could not parse xmlboot config", ex);
        } catch (IOException ex) {
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
        readWriteCheckBox.setSelected(showAtStartup);
        readOnlyCheckBox.setSelected(showReadOnlyInfo);

        // firewall tables
        ipTableModel = new IPTableModel(firewallIPTable,
                new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        firewallIPTable.setModel(ipTableModel);
        JComboBox protocolCombobox = new JComboBox();
        protocolCombobox.addItem(Protocol.TCP);
        protocolCombobox.addItem(Protocol.UDP);
        TableColumn protocolColumn =
                firewallIPTable.getColumnModel().getColumn(0);
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

        helpTextPane.setCaretPosition(0);

        // fix some size issues
        infoScrollPane.setMinimumSize(infoScrollPane.getPreferredSize());
        nonfreeLabel.setMinimumSize(nonfreeLabel.getPreferredSize());
        teachingScrollPane.setMinimumSize(teachingScrollPane.getPreferredSize());
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
        nonfreePanel = new javax.swing.JPanel();
        nonfreeLabel = new javax.swing.JLabel();
        recommendedPanel = new javax.swing.JPanel();
        flashCheckBox = new javax.swing.JCheckBox();
        flashLabel = new javax.swing.JLabel();
        readerCheckBox = new javax.swing.JCheckBox();
        readerLabel = new javax.swing.JLabel();
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
        additionalMiscPanel = new javax.swing.JPanel();
        netbeansPanel = new ch.fhnw.lernstickwelcome.GamePanel();
        processingPanel = new ch.fhnw.lernstickwelcome.GamePanel();
        lazarusPanel = new ch.fhnw.lernstickwelcome.GamePanel();
        openClipartPanel = new ch.fhnw.lernstickwelcome.GamePanel();
        gamesScrollPane = new javax.swing.JScrollPane();
        gamesScrollPanel = new ScrollableJPanel();
        riliGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        filletsGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        neverballGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        neverputtGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        freecolGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        minetestGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        frogattoGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        supertuxkartGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
        xmotoGamePanel = new ch.fhnw.lernstickwelcome.GamePanel();
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
        jLabel3 = new javax.swing.JLabel();
        partitionsPanel = new javax.swing.JPanel();
        exchangePartitionPanel = new javax.swing.JPanel();
        exchangePartitionNameLabel = new javax.swing.JLabel();
        exchangePartitionNameTextField = new javax.swing.JTextField();
        dataPartitionPanel = new javax.swing.JPanel();
        readWritePanel = new javax.swing.JPanel();
        readWriteCheckBox = new javax.swing.JCheckBox();
        readOnlyPanel = new javax.swing.JPanel();
        readOnlyCheckBox = new javax.swing.JCheckBox();
        firewallPanel = new javax.swing.JPanel();
        firewallInfoLabel = new javax.swing.JLabel();
        firewallTabbedPane = new javax.swing.JTabbedPane();
        firewallIPPanel = new javax.swing.JPanel();
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

        readerCheckBox.setSelected(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        recommendedPanel.add(readerCheckBox, gridBagConstraints);

        readerLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/Adobe_Reader_8_icon.png"))); // NOI18N
        readerLabel.setText(bundle.getString("Welcome.readerLabel.text")); // NOI18N
        readerLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                readerLabelMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        recommendedPanel.add(readerLabel, gridBagConstraints);

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
            .addGap(0, 553, Short.MAX_VALUE)
        );
        fillPanelLayout.setVerticalGroup(
            fillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 64, Short.MAX_VALUE)
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
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        additionalMiscPanel.add(openClipartPanel, gridBagConstraints);

        additionalScrollPane.setViewportView(additionalMiscPanel);

        additionalTabbedPane.addTab(bundle.getString("Welcome.additionalScrollPane.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/applications-other.png")), additionalScrollPane); // NOI18N

        gamesScrollPanel.setLayout(new java.awt.GridBagLayout());

        riliGamePanel.setDescription(bundle.getString("Welcome.riliGamePanel.description")); // NOI18N
        riliGamePanel.setGameName("Ri-li"); // NOI18N
        riliGamePanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/ri-li.png"))); // NOI18N
        riliGamePanel.setWebsite("http://ri-li.sourceforge.net"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
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

        userNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userNameTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(25, 10, 0, 10);
        systemPanel.add(userNameTextField, gridBagConstraints);
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
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        exchangePartitionPanel.add(exchangePartitionNameLabel, gridBagConstraints);

        exchangePartitionNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exchangePartitionNameTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        exchangePartitionPanel.add(exchangePartitionNameTextField, gridBagConstraints);

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
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        partitionsPanel.add(dataPartitionPanel, gridBagConstraints);

        mainCardPanel.add(partitionsPanel, "partitionsPanel");

        firewallPanel.setLayout(new java.awt.GridBagLayout());

        firewallInfoLabel.setText(bundle.getString("Welcome.firewallInfoLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 0, 5);
        firewallPanel.add(firewallInfoLabel, gridBagConstraints);

        firewallIPPanel.setLayout(new java.awt.GridBagLayout());

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
        firewallIPPanel.add(firewallIPButtonPanel, gridBagConstraints);

        firewallIPScrollPane.setViewportView(firewallIPTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        firewallIPPanel.add(firewallIPScrollPane, gridBagConstraints);

        firewallTabbedPane.addTab(bundle.getString("Welcome.firewallIPPanel.TabConstraints.tabTitle"), firewallIPPanel); // NOI18N

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
        apply();
    }//GEN-LAST:event_applyButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        System.exit(0);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void multimediaLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_multimediaLabelMouseClicked
        toggleCheckBox(multimediaCheckBox);
}//GEN-LAST:event_multimediaLabelMouseClicked

    private void infoEditorPaneHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_infoEditorPaneHyperlinkUpdate
        openLinkInBrowser(evt);
    }//GEN-LAST:event_infoEditorPaneHyperlinkUpdate

    private void readerLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_readerLabelMouseClicked
        toggleCheckBox(readerCheckBox);
    }//GEN-LAST:event_readerLabelMouseClicked

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        LOGGER.info("exiting program");
        System.exit(0);
    }//GEN-LAST:event_formWindowClosed

    private void userNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userNameTextFieldActionPerformed
        if (exchangePartitionNameTextField.isEnabled()) {
            exchangePartitionNameTextField.requestFocusInWindow();
        } else {
            apply();
        }
    }//GEN-LAST:event_userNameTextFieldActionPerformed

    private void exchangePartitionNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exchangePartitionNameTextFieldActionPerformed
        apply();
    }//GEN-LAST:event_exchangePartitionNameTextFieldActionPerformed

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

        MainMenuListEntry entry =
                (MainMenuListEntry) menuList.getSelectedValue();
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
        // enforce visibility of top game
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

    private void parseURLWhiteList() throws IOException {
        FileReader fileReader = new FileReader(URL_WHITELIST_FILENAME);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder builder = new StringBuilder();
        for (String line = bufferedReader.readLine(); line != null;) {
            builder.append(line);
            builder.append('\n');
            line = bufferedReader.readLine();
        }
        firewallURLTextArea.setText(builder.toString());
        bufferedReader.close();
        fileReader.close();
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
                    Protocol protocol = null;
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

                    int port;
                    try {
                        port = Integer.parseInt(tokens[2]);
                        if (port < 0) {
                            LOGGER.log(Level.WARNING,
                                    "negative port number \"{0}\"", tokens[2]);
                            continue;
                        }
                        if (port > 65535) {
                            LOGGER.log(Level.WARNING,
                                    "port number out of range \"{0}\"", tokens[2]);
                            continue;
                        }
                    } catch (NumberFormatException ex) {
                        LOGGER.log(Level.WARNING,
                                "could not parse port \"{0}\"", tokens[2]);
                        continue;
                    }

                    ipTableModel.addEntry(new IPTableEntry(
                            protocol, target, port, lastComment));
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

    private static File getXmlBootConfigFile() {
        File imageDirectory = new File(IMAGE_DIRECTORY);
        File configFile = new File(imageDirectory, "isolinux/xmlboot.config");
        if (configFile.exists()) {
            LOGGER.log(Level.INFO, "xmlboot config file: {0}", configFile);
            return configFile;
        } else {
            configFile = new File(imageDirectory, "syslinux/xmlboot.config");
            if (configFile.exists()) {
                LOGGER.log(Level.INFO, "xmlboot config file: {0}", configFile);
                return configFile;
            } else {
                LOGGER.warning("xmlboot config file not found!");
                return null;
            }
        }
    }

    private Document parseXmlFile(File file)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(file);
    }

    private static File getSyslinuxConfigFile() {
        // determine which config file to use
        File imageDirectory = new File(IMAGE_DIRECTORY);
        File configFile = new File(imageDirectory, "isolinux/isolinux.cfg");
        if (configFile.exists()) {
            return configFile;
        } else {
            configFile = new File(imageDirectory, "syslinux/syslinux.cfg");
            if (configFile.exists()) {
                return configFile;
            } else {
                LOGGER.warning("syslinux config file not found!");
                return null;
            }
        }
    }

    private int getTimeout() throws IOException {
        if (SYSLINUX_CONFIG_FILE != null) {
            Pattern timeoutPattern = Pattern.compile("timeout (.*)");
            List<String> configFileLines = readFile(SYSLINUX_CONFIG_FILE);
            for (String configFileLine : configFileLines) {
                Matcher matcher = timeoutPattern.matcher(configFileLine);
                if (matcher.matches()) {
                    String timeoutString = matcher.group(1);
                    try {
                        return Integer.parseInt(timeoutString) / 10;
                    } catch (Exception e) {
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
        SpinnerNumberModel model =
                (SpinnerNumberModel) bootTimeoutSpinner.getModel();
        if (model.getNumber().intValue() == 1) {
            secondsLabel.setText(BUNDLE.getString("second"));
        } else {
            secondsLabel.setText(BUNDLE.getString("seconds"));
        }
    }

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
            int proxyPort =
                    ((Number) proxyPortTextField.getValue()).intValue();
            String proxyUserName = proxyUserNameTextField.getText();
            String proxyPassword =
                    String.valueOf(proxyPasswordField.getPassword());
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
        int proxyPort =
                ((Number) proxyPortTextField.getValue()).intValue();
        String proxyUserName = proxyUserNameTextField.getText();
        String proxyPassword =
                String.valueOf(proxyPasswordField.getPassword());
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

    private void apply() {
        // update full user name (if necessary)
        String newFullName = userNameTextField.getText();
        if (!newFullName.equals(fullName)) {
            LOGGER.log(Level.INFO,
                    "updating full user name to \"{0}\"", newFullName);
            processExecutor.executeProcess("chfn", "-f", newFullName, "user");
        }

        // update exchange partition label
        String newExchangePartitionLabel =
                exchangePartitionNameTextField.getText();
        LOGGER.log(Level.INFO, "new exchange partition label: \"{0}\"",
                newExchangePartitionLabel);
        if (!newExchangePartitionLabel.isEmpty()
                && !newExchangePartitionLabel.equals(exchangePartitionLabel)) {
            processExecutor.executeProcess("dosfslabel",
                    exchangePartition, newExchangePartitionLabel);
        }

        if (examEnvironment) {
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
            try {
                FileOutputStream fileOutputStream =
                        new FileOutputStream(IP_TABLES_FILENAME);
                fileOutputStream.write(ipTables.getBytes());
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "", ex);
            }

            // save URL whitelist
            try {
                FileOutputStream fileOutputStream =
                        new FileOutputStream(URL_WHITELIST_FILENAME);
                fileOutputStream.write(
                        firewallURLTextArea.getText().getBytes());
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "", ex);
            }

            processExecutor.executeProcess(
                    "/etc/init.d/lernstick-firewall", "reload");
        } else {
            installSelectedPackages();
        }

        // update properties
        try {
            properties.setProperty(SHOW_WELCOME,
                    readWriteCheckBox.isSelected() ? "true" : "false");
            properties.setProperty(SHOW_READ_ONLY_INFO,
                    readOnlyCheckBox.isSelected() ? "true" : "false");
            properties.store(new FileOutputStream(propertiesFile),
                    "lernstick Welcome properties");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        if (IMAGE_IS_WRITABLE) {
            // make image (temporarily) writable
            processExecutor.executeProcess(
                    "mount", "-o", "remount,rw", IMAGE_DIRECTORY);

            // update timeout...
            SpinnerNumberModel spinnerNumberModel =
                    (SpinnerNumberModel) bootTimeoutSpinner.getModel();
            int timeoutValue = spinnerNumberModel.getNumber().intValue();
            // ... in syslinux ...
            processExecutor.executeProcess("sed", "-i", "-e",
                    "s|timeout .*|timeout " + (timeoutValue * 10) + "|1",
                    SYSLINUX_CONFIG_FILE.getPath());
            // ... and grub
            processExecutor.executeProcess("sed", "-i", "-e",
                    "s|set timeout=.*|set timeout=" + timeoutValue + "|1",
                    IMAGE_DIRECTORY + "/boot/grub/grub_main.cfg");
            processExecutor.executeProcess("sed", "-i", "-e",
                    "s|num_ticks = .*|num_ticks = " + timeoutValue + "|1",
                    IMAGE_DIRECTORY + "/boot/grub/themes/lernstick/theme.txt");

            // update system name and version...
            String systemName = systemNameTextField.getText();
            String systemVersion = systemVersionTextField.getText();
            // ... in xmlboot config
            try {
                Document xmlBootDocument = parseXmlFile(XMLBOOT_CONFIG_FILE);
                xmlBootDocument.getDocumentElement().normalize();
                Node systemNode =
                        xmlBootDocument.getElementsByTagName("system").item(0);
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
                TransformerFactory transformerFactory =
                        TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                DOMSource source = new DOMSource(xmlBootDocument);
                StreamResult result = new StreamResult(tmpFile);
                transformer.transform(source, result);
                processExecutor.executeProcess("mv", tmpFile.getPath(),
                        XMLBOOT_CONFIG_FILE.getPath());

            } catch (ParserConfigurationException ex) {
                LOGGER.log(Level.WARNING, "can not update xmlboot config", ex);
            } catch (SAXException ex) {
                LOGGER.log(Level.WARNING, "can not update xmlboot config", ex);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "can not update xmlboot config", ex);
            } catch (DOMException ex) {
                LOGGER.log(Level.WARNING, "can not update xmlboot config", ex);
            } catch (TransformerException ex) {
                LOGGER.log(Level.WARNING, "can not update xmlboot config", ex);
            }

            // ... and in grub theme
            processExecutor.executeProcess("sed", "-i", "-e",
                    "s|title-text: .*|title-text: \""
                    + systemName + ' ' + systemVersion + "\"|1",
                    IMAGE_DIRECTORY + "/boot/grub/themes/lernstick/theme.txt");


            // remount image read-only
            processExecutor.executeProcess(
                    "mount", "-o", "remount,ro", IMAGE_DIRECTORY);
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

    private static List<String> readFile(File file) throws IOException {
        List<String> lines = new ArrayList<String>();
        BufferedReader reader =
                new BufferedReader(new FileReader(file));
        for (String line = reader.readLine(); line != null;
                line = reader.readLine()) {
            lines.add(line);
        }
        reader.close();
        return lines;
    }

    private static void writeFile(File file, List<String> lines)
            throws IOException {
        // delete old version of file
        if (file.exists()) {
            file.delete();
        }
        // write new version of file
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            String lineSeparator = System.getProperty("line.separator");
            for (String line : lines) {
                outputStream.write((line + lineSeparator).getBytes());
            }
            outputStream.flush();
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    private String getExchangePartition() throws IOException {

        // determine system partition
        String systemPartition = null;
        List<String> mounts = readFile(new File("/proc/mounts"));
        for (String mount : mounts) {
            String[] tokens = mount.split(" ");
            if (tokens[0].startsWith("/dev/")
                    && (tokens[1].equals("/live/image")
                    || tokens[1].equals(IMAGE_DIRECTORY))) {
                systemPartition = tokens[0];
                break;
            }
        }
        if (systemPartition == null) {
            LOGGER.warning("could not determine system partition");
            return null;
        } else {
            LOGGER.log(Level.INFO, "system partition: {0}", systemPartition);
        }

        // determine system storage device
        Pattern partitionPattern = Pattern.compile("(.*)\\d+");
        Matcher matcher = partitionPattern.matcher(systemPartition);
        String systemDevice = null;
        if (matcher.matches()) {
            systemDevice = matcher.group(1);
        }
        if (systemDevice == null) {
            LOGGER.warning("could not determine system device");
            return null;
        } else {
            LOGGER.log(Level.INFO, "system device: {0}", systemDevice);
        }

        // check if there is an exchange partition
        // (must be the first partition and must be FAT32)
        String firstPartition = systemDevice + "1";
        try {
            String idType = DbusTools.getStringProperty(
                    firstPartition.substring(5), "IdType");
            LOGGER.log(Level.INFO, "first partition id type: {0}", idType);
            if (idType.equals("vfat")) {
                return firstPartition;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Could not check first partition of "
                    + systemDevice, ex);
        }

        return null;
    }

    private String getPartitionLabel(String partition) throws DBusException {
        String label = DbusTools.getStringProperty(
                partition.substring(5), "IdLabel");
        LOGGER.log(Level.INFO, "label of partition {0}: \"{1}\"",
                new Object[]{partition, label});
        return label;
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
        PackageListUpdater packageListUpdater =
                new PackageListUpdater(updateDialog);
        packageListUpdater.execute();
        updateDialog.setVisible(true);
        try {
            if (!packageListUpdater.get()) {
                UpdateErrorDialog dialog =
                        new UpdateErrorDialog(this, aptGetOutput);
                dialog.setVisible(true);
            }
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void installSelectedPackages() {

        // calculate number of packages
        int numberOfPackages = 0;

        // non-free software
        numberOfPackages += flashCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += readerCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += additionalFontsCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += multimediaCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += googleEarthCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += skypeCheckBox.isSelected() ? 1 : 0;

        // LA teaching tools
        numberOfPackages += laCheckBox.isSelected() ? 1 : 0;

        // miscellaneous
        numberOfPackages += netbeansPanel.isSelected() ? 1 : 0;
        numberOfPackages += processingPanel.isSelected() ? 1 : 0;
        numberOfPackages += lazarusPanel.isSelected() ? 1 : 0;
        numberOfPackages += openClipartPanel.isSelected() ? 1 : 0;

        // games
        numberOfPackages += riliGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += filletsGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += neverballGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += neverputtGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += freecolGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += minetestGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += frogattoGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += supertuxkartGamePanel.isSelected() ? 1 : 0;
        numberOfPackages += xmotoGamePanel.isSelected() ? 1 : 0;
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

            Installer installer =
                    new Installer(progressDialog, numberOfPackages);
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
                "Welcome.flashLabel.text", FLASH_PACKAGE);
        checkInstall(readerCheckBox, readerLabel,
                "Welcome.readerLabel.text", "adobereader-" + adobeLanguageCode);
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
        checkAppInstall(lazarusPanel, "lazarus");
        checkAppInstall(openClipartPanel, "openclipart-libreoffice");

        // games
        checkAppInstall(riliGamePanel, "lernstick-ri-li");
        checkAppInstall(filletsGamePanel, "lernstick-fillets-ng");
        checkAppInstall(neverballGamePanel, "live-neverball2");
        checkAppInstall(neverputtGamePanel, "live-neverputt2");
        checkAppInstall(frogattoGamePanel, "frogatto");
        checkAppInstall(freecolGamePanel, "freecol");
        checkAppInstall(minetestGamePanel, "minetest");
        checkAppInstall(supertuxkartGamePanel, "supertuxkart");
        checkAppInstall(xmotoGamePanel, "xmoto");
        checkAppInstall(wesnothGamePanel, "wesnoth");
        checkAppInstall(flareGamePanel, "flare");
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
                    FLASH_PACKAGE);
            installAdobeReader();
//            installPackage(readerCheckBox, "Welcome.readerLabel.text",
//                    "/ch/fhnw/lernstickwelcome/icons/Adobe_Reader_8_icon.png",
//                    ACROREAD_PACKAGES);
            installNonFreeApplication(additionalFontsCheckBox, "Welcome.fontsLabel.text",
                    "/ch/fhnw/lernstickwelcome/icons/48x48/fonts.png",
                    FONTS_PACKAGES);
            installNonFreeApplication(multimediaCheckBox, "Welcome.multimediaLabel.text",
                    "/ch/fhnw/lernstickwelcome/icons/48x48/package_multimedia.png",
                    MULTIMEDIA_PACKAGES);
            installGoogleEarth();
//            installNonFreeApplication(skypeCheckBox, "Welcome.skypeLabel.text",
//                    "/ch/fhnw/lernstickwelcome/icons/48x48/skype.png",
//                    "skype");
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
            installApplication(lazarusPanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/lazarus.png",
                    "lazarus", "fpc-source", "lcl",
                    "fp-units-gfx", "fp-units-gtk", "fp-units-misc");
            installApplication(openClipartPanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/openclipart.png",
                    "openclipart-libreoffice");

            // games
            installApplication(riliGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/ri-li.png",
                    "lernstick-ri-li");
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
                    "minetest-mod-worldedit");
            installApplication(frogattoGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/frogatto.png",
                    "frogatto");
            installApplication(supertuxkartGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/supertuxkart.png",
                    "live-supertuxkart");
            installApplication(xmotoGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/xmoto.png",
                    "live-xmoto");
            installApplication(wesnothGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/wesnoth.png",
                    "wesnoth", "wesnoth-music");
            installApplication(flareGamePanel,
                    "/ch/fhnw/lernstickwelcome/icons/48x48/flare.png",
                    "flare");
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

        private void installAdobeReader() throws IOException {
            if (!readerCheckBox.isSelected()) {
                return;
            }
            String infoString = BUNDLE.getString("Installing");
            infoString = MessageFormat.format(infoString,
                    BUNDLE.getString("Welcome.readerLabel.text"));
            Icon icon = new ImageIcon(getClass().getResource(
                    "/ch/fhnw/lernstickwelcome/icons/48x48/Adobe_Reader_8_icon.png"));
            ProgressAction progressAction =
                    new ProgressAction(infoString, icon);
            publish(progressAction);
            /**
             * example download link:
             * http://ardownload.adobe.com/pub/adobe/reader/unix/9.x/9.2/fra/AdbeRdr9.2-1_i386linux_fra.deb
             */
            int majorVersion = 9;
            String fullVersion = majorVersion + ".4.2";
            String fileName = "AdbeRdr" + fullVersion + "-1_i386linux_"
                    + adobeLanguageCode + ".deb";
            String adobeReaderInstallScript = "cd " + USER_HOME + '\n'
                    + "wget" + getWgetProxyLine()
                    + "http://ardownload.adobe.com/pub/adobe/reader/unix/"
                    + majorVersion + ".x/" + fullVersion + '/'
                    + adobeLanguageCode + '/' + fileName + '\n'
                    + "dpkg -i " + fileName + '\n'
                    + "rm " + fileName;
            int exitValue = processExecutor.executeScript(
                    true, true, adobeReaderInstallScript);
            if (exitValue != 0) {
                String errorMessage = "Installation of Adobe Reader failed"
                        + "with the following error message:\n"
                        + processExecutor.getOutput();
                LOGGER.severe(errorMessage);
                showErrorMessage(errorMessage);
            }
            updateProgress();
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
            ProgressAction progressAction =
                    new ProgressAction(infoString, icon);
            publish(progressAction);
            String skypeInstallScript = "#!/bin/sh\n"
                    + "wget -O skype-install.deb http://www.skype.com/go/getskype-linux-deb\n"
                    + "dpkg -i skype-install.deb\n"
                    + "apt-get -f install";
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
            ProgressAction progressAction =
                    new ProgressAction(infoString, icon);
            publish(progressAction);

            // old version with googleearth-package
//            String googleEarthInstallScript = "cd " + USER_HOME + '\n'
//                    + "apt-get -y --force-yes install googleearth-package lsb-core\n"
//                    + "make-googleearth-package --force\n"
//                    + "dpkg -i googleearth_*\n"
//                    + "rm googleearth_*";

            // new version with direct download link
            String debName = "google-earth-stable_current_i386.deb";
            String googleEarthInstallScript =
                    "apt-get" + getAptGetProxyLine()
                    + "-y --force-yes install lsb-core\n"
                    + "cd " + USER_HOME + '\n'
                    + "wget" + getWgetProxyLine()
                    + "https://dl-ssl.google.com/linux/direct/" + debName + '\n'
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
            ProgressAction progressAction =
                    new ProgressAction(infoString, icon);
            publish(progressAction);

            for (String packageName : packageNames) {
                LOGGER.log(Level.INFO, "installing package \"{0}\"", packageName);
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
        public Component getListCellRendererComponent(JList list,
                Object value, int index, boolean isSelected, boolean cellHasFocus) {
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
    private javax.swing.JPanel bootMenuPanel;
    private javax.swing.JLabel bootTimeoutLabel;
    private javax.swing.JSpinner bootTimeoutSpinner;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel dataPartitionPanel;
    private javax.swing.JLabel exchangePartitionNameLabel;
    private javax.swing.JTextField exchangePartitionNameTextField;
    private javax.swing.JPanel exchangePartitionPanel;
    private javax.swing.JPanel fillPanel;
    private ch.fhnw.lernstickwelcome.GamePanel filletsGamePanel;
    private javax.swing.JPanel firewallIPButtonPanel;
    private javax.swing.JPanel firewallIPPanel;
    private javax.swing.JScrollPane firewallIPScrollPane;
    private javax.swing.JTable firewallIPTable;
    private javax.swing.JLabel firewallInfoLabel;
    private javax.swing.JPanel firewallPanel;
    private javax.swing.JTabbedPane firewallTabbedPane;
    private javax.swing.JPanel firewallURLPanel;
    private javax.swing.JScrollPane firewallURLScrollPane;
    private javax.swing.JTextArea firewallURLTextArea;
    private ch.fhnw.lernstickwelcome.GamePanel flareGamePanel;
    private javax.swing.JCheckBox flashCheckBox;
    private javax.swing.JLabel flashLabel;
    private javax.swing.JLabel fontsLabel;
    private ch.fhnw.lernstickwelcome.GamePanel freecolGamePanel;
    private ch.fhnw.lernstickwelcome.GamePanel frogattoGamePanel;
    private javax.swing.JScrollPane gamesScrollPane;
    private javax.swing.JPanel gamesScrollPanel;
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
    private javax.swing.JCheckBox laCheckBox;
    private javax.swing.JLabel laLabel;
    private javax.swing.JLabel label1;
    private javax.swing.JLabel label2;
    private ch.fhnw.lernstickwelcome.GamePanel lazarusPanel;
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
    private javax.swing.JLabel nonfreeLabel;
    private javax.swing.JPanel nonfreePanel;
    private ch.fhnw.lernstickwelcome.GamePanel openClipartPanel;
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
    private javax.swing.JCheckBox readOnlyCheckBox;
    private javax.swing.JPanel readOnlyPanel;
    private javax.swing.JCheckBox readWriteCheckBox;
    private javax.swing.JPanel readWritePanel;
    private javax.swing.JCheckBox readerCheckBox;
    private javax.swing.JLabel readerLabel;
    private javax.swing.JPanel recommendedPanel;
    private javax.swing.JButton removeIPButton;
    private ch.fhnw.lernstickwelcome.GamePanel riliGamePanel;
    private javax.swing.JLabel secondsLabel;
    private javax.swing.JCheckBox skypeCheckBox;
    private javax.swing.JLabel skypeLabel;
    private ch.fhnw.lernstickwelcome.GamePanel supertuxkartGamePanel;
    private javax.swing.JLabel systemNameLabel;
    private javax.swing.JTextField systemNameTextField;
    private javax.swing.JPanel systemPanel;
    private javax.swing.JLabel systemVersionLabel;
    private javax.swing.JTextField systemVersionTextField;
    private javax.swing.JEditorPane teachingEditorPane;
    private javax.swing.JPanel teachingPanel;
    private javax.swing.JScrollPane teachingScrollPane;
    private javax.swing.JLabel userNameLabel;
    private javax.swing.JTextField userNameTextField;
    private javax.swing.JLabel welcomeLabel;
    private ch.fhnw.lernstickwelcome.GamePanel wesnothGamePanel;
    private ch.fhnw.lernstickwelcome.GamePanel xmotoGamePanel;
    // End of variables declaration//GEN-END:variables
}
