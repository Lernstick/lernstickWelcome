/*
 * Welcome.java
 *
 * Created on 01.04.2009, 14:11:23
 */
package ch.fhnw.lernstickwelcome;

import ch.fhnw.util.DbusTools;
import ch.fhnw.util.ProcessExecutor;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.CardLayout;
import java.awt.Component;
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
import javax.swing.text.AbstractDocument;
import org.freedesktop.dbus.exceptions.DBusException;

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
    private static final String SHOW_STARTUP = "ShowAtStartup";
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
    private final static ProcessExecutor processExecutor =
            new ProcessExecutor();
    private final static String USER_HOME = System.getProperty("user.home");
    private final String adobeLanguageCode;
    private final File propertiesFile;
    private final Properties properties;
    private final Toolkit toolkit = Toolkit.getDefaultToolkit();
    private final String fullName;
    private final DefaultListModel menuListModel = new DefaultListModel();
    private int menuListIndex = 0;
    private String exchangePartition;
    private String exchangePartitionLabel;
    private String aptGetOutput;

    /**
     * Creates new form Welcome
     */
    public Welcome() {

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

        boolean showAtStartup = true;
        String propertiesFileName = USER_HOME + File.separatorChar
                + ".config" + File.separatorChar + "lernstickWelcome";
        propertiesFile = new File(propertiesFileName);
        properties = new Properties();
        try {
            properties.load(new FileInputStream(propertiesFile));
            showAtStartup = "true".equals(properties.getProperty(SHOW_STARTUP));
        } catch (IOException ex) {
            LOGGER.log(Level.INFO,
                    "can not load properties from " + propertiesFile, ex);
        }

        initComponents();

        menuList.setModel(menuListModel);

        menuListModel.addElement(BUNDLE.getString("Information"));
        menuListModel.addElement(BUNDLE.getString("Nonfree_Software"));
        menuListModel.addElement(BUNDLE.getString("Teaching_System"));
        menuListModel.addElement(BUNDLE.getString("Games"));
        menuListModel.addElement(BUNDLE.getString("Proxy"));
        menuListModel.addElement(BUNDLE.getString("Names"));

        menuList.setCellRenderer(new MyListCellRenderer());

        menuList.setSelectedIndex(0);

        checkAllPackages();

        // determine current full user name
        AbstractDocument userNameDocument =
                (AbstractDocument) userNameTextField.getDocument();
        userNameDocument.setDocumentFilter(new FullUserNameFilter());
        String userName = System.getProperty("user.name");
        processExecutor.executeProcess(true, true, "getent", "passwd", userName);
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

        // set app icon
        Image image = toolkit.getImage(getClass().getResource(
                "/ch/fhnw/lernstickwelcome/icons/messagebox_info.png"));
        setIconImage(image);

        UIDefaults defaults = UIManager.getDefaults();
        infoEditorPane.setBackground(defaults.getColor("Panel.background"));
        teachingEditorPane.setBackground(defaults.getColor("Panel.background"));
        showCheckBox.setSelected(showAtStartup);

        // center on screen
        pack();
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

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Welcome();
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
        gamesPanel = new javax.swing.JPanel();
        gamesInfoLabel = new javax.swing.JLabel();
        riliCheckBox = new javax.swing.JCheckBox();
        riliLabel = new javax.swing.JLabel();
        frogattoCheckBox = new javax.swing.JCheckBox();
        frogattoLabel = new javax.swing.JLabel();
        filletsCheckBox = new javax.swing.JCheckBox();
        filletsLabel = new javax.swing.JLabel();
        supertuxkartCheckBox = new javax.swing.JCheckBox();
        supertuxkartLabel = new javax.swing.JLabel();
        wesnothCheckBox = new javax.swing.JCheckBox();
        wesnothLabel = new javax.swing.JLabel();
        flareCheckBox = new javax.swing.JCheckBox();
        flareLabel = new javax.swing.JLabel();
        hedgewarsCheckBox = new javax.swing.JCheckBox();
        hedgewarsLabel = new javax.swing.JLabel();
        astromenaceCheckBox = new javax.swing.JCheckBox();
        astromenaceLabel = new javax.swing.JLabel();
        dummyPanel = new javax.swing.JPanel();
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
        namesPanel = new javax.swing.JPanel();
        namesInfoLabel = new javax.swing.JLabel();
        userNameLabel = new javax.swing.JLabel();
        userNameTextField = new javax.swing.JTextField();
        exchangePartitionNameLabel = new javax.swing.JLabel();
        exchangePartitionNameTextField = new javax.swing.JTextField();
        bottomPanel = new javax.swing.JPanel();
        navigaionPanel = new javax.swing.JPanel();
        previousButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        applyButton = new javax.swing.JButton();
        showCheckBox = new javax.swing.JCheckBox();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("ch/fhnw/lernstickwelcome/Bundle"); // NOI18N
        setTitle(bundle.getString("Welcome.title")); // NOI18N
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        addWindowListener(new java.awt.event.WindowAdapter() {
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
            .addGap(0, 561, Short.MAX_VALUE)
        );
        fillPanelLayout.setVerticalGroup(
            fillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 19, Short.MAX_VALUE)
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

        gamesPanel.setLayout(new java.awt.GridBagLayout());

        gamesInfoLabel.setText(bundle.getString("Welcome.gamesInfoLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 10);
        gamesPanel.add(gamesInfoLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 0);
        gamesPanel.add(riliCheckBox, gridBagConstraints);

        riliLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/ri-li.png"))); // NOI18N
        riliLabel.setText(bundle.getString("Welcome.riliLabel.text")); // NOI18N
        riliLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                riliLabelMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        gamesPanel.add(riliLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 0);
        gamesPanel.add(frogattoCheckBox, gridBagConstraints);

        frogattoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/frogatto.png"))); // NOI18N
        frogattoLabel.setText(bundle.getString("Welcome.frogattoLabel.text")); // NOI18N
        frogattoLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                frogattoLabelMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        gamesPanel.add(frogattoLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 0, 0);
        gamesPanel.add(filletsCheckBox, gridBagConstraints);

        filletsLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/fillets.png"))); // NOI18N
        filletsLabel.setText(bundle.getString("Welcome.filletsLabel.text")); // NOI18N
        filletsLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                filletsLabelMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        gamesPanel.add(filletsLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 0, 0);
        gamesPanel.add(supertuxkartCheckBox, gridBagConstraints);

        supertuxkartLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/supertuxkart.png"))); // NOI18N
        supertuxkartLabel.setText(bundle.getString("Welcome.supertuxkartLabel.text")); // NOI18N
        supertuxkartLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                supertuxkartLabelMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        gamesPanel.add(supertuxkartLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 0, 0);
        gamesPanel.add(wesnothCheckBox, gridBagConstraints);

        wesnothLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/wesnoth.png"))); // NOI18N
        wesnothLabel.setText(bundle.getString("Welcome.wesnothLabel.text")); // NOI18N
        wesnothLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                wesnothLabelMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        gamesPanel.add(wesnothLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 0, 0);
        gamesPanel.add(flareCheckBox, gridBagConstraints);

        flareLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/flare.png"))); // NOI18N
        flareLabel.setText(bundle.getString("Welcome.flareLabel.text")); // NOI18N
        flareLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                flareLabelMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        gamesPanel.add(flareLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 20, 0);
        gamesPanel.add(hedgewarsCheckBox, gridBagConstraints);

        hedgewarsLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/hedgewars.png"))); // NOI18N
        hedgewarsLabel.setText(bundle.getString("Welcome.hedgewarsLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 20, 0);
        gamesPanel.add(hedgewarsLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 20, 0);
        gamesPanel.add(astromenaceCheckBox, gridBagConstraints);

        astromenaceLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/32x32/astromenace.png"))); // NOI18N
        astromenaceLabel.setText(bundle.getString("Welcome.astromenaceLabel.text")); // NOI18N
        astromenaceLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                astromenaceLabelMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 20, 0);
        gamesPanel.add(astromenaceLabel, gridBagConstraints);

        javax.swing.GroupLayout dummyPanelLayout = new javax.swing.GroupLayout(dummyPanel);
        dummyPanel.setLayout(dummyPanelLayout);
        dummyPanelLayout.setHorizontalGroup(
            dummyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        dummyPanelLayout.setVerticalGroup(
            dummyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weighty = 1.0;
        gamesPanel.add(dummyPanel, gridBagConstraints);

        mainCardPanel.add(gamesPanel, "gamesPanel");

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
                .addContainerGap(75, Short.MAX_VALUE))
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
                .addContainerGap(117, Short.MAX_VALUE))
        );

        mainCardPanel.add(proxyPanel, "proxyPanel");

        namesPanel.setLayout(new java.awt.GridBagLayout());

        namesInfoLabel.setText(bundle.getString("Welcome.namesInfoLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 10);
        namesPanel.add(namesInfoLabel, gridBagConstraints);

        userNameLabel.setText(bundle.getString("Welcome.userNameLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(25, 10, 0, 0);
        namesPanel.add(userNameLabel, gridBagConstraints);

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
        namesPanel.add(userNameTextField, gridBagConstraints);

        exchangePartitionNameLabel.setText(bundle.getString("Welcome.exchangePartitionNameLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        namesPanel.add(exchangePartitionNameLabel, gridBagConstraints);

        exchangePartitionNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exchangePartitionNameTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        namesPanel.add(exchangePartitionNameTextField, gridBagConstraints);

        mainCardPanel.add(namesPanel, "namesPanel");

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
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        bottomPanel.add(applyButton, gridBagConstraints);

        showCheckBox.setSelected(true);
        showCheckBox.setText(bundle.getString("Welcome.showCheckBox.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        bottomPanel.add(showCheckBox, gridBagConstraints);

        cancelButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ch/fhnw/lernstickwelcome/icons/exit.png"))); // NOI18N
        cancelButton.setText(bundle.getString("Welcome.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
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
        exitProgram();
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
        exitProgram();
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

        switch (selectedIndex) {
            case 0:
                selectCard("infoPanel");
                previousButton.setEnabled(false);
                nextButton.setEnabled(true);
                break;

            case 1:
                selectCard("nonfreePanel");
                previousButton.setEnabled(true);
                nextButton.setEnabled(true);
                break;

            case 2:
                selectCard("teachingPanel");
                previousButton.setEnabled(true);
                nextButton.setEnabled(true);
                break;

            case 3:
                selectCard("gamesPanel");
                previousButton.setEnabled(true);
                nextButton.setEnabled(true);
                break;

            case 4:
                selectCard("proxyPanel");
                previousButton.setEnabled(true);
                nextButton.setEnabled(true);
                break;

            case 5:
                selectCard("namesPanel");
                previousButton.setEnabled(true);
                nextButton.setEnabled(false);
        }
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

    private void supertuxkartLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_supertuxkartLabelMouseClicked
        toggleCheckBox(supertuxkartCheckBox);
    }//GEN-LAST:event_supertuxkartLabelMouseClicked

    private void frogattoLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_frogattoLabelMouseClicked
        toggleCheckBox(frogattoCheckBox);
    }//GEN-LAST:event_frogattoLabelMouseClicked

    private void wesnothLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_wesnothLabelMouseClicked
        toggleCheckBox(wesnothCheckBox);
    }//GEN-LAST:event_wesnothLabelMouseClicked

    private void flareLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_flareLabelMouseClicked
        toggleCheckBox(flareCheckBox);
    }//GEN-LAST:event_flareLabelMouseClicked

    private void riliLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_riliLabelMouseClicked
        toggleCheckBox(riliCheckBox);
    }//GEN-LAST:event_riliLabelMouseClicked

    private void astromenaceLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_astromenaceLabelMouseClicked
        toggleCheckBox(astromenaceCheckBox);
    }//GEN-LAST:event_astromenaceLabelMouseClicked

    private void filletsLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_filletsLabelMouseClicked
        toggleCheckBox(filletsCheckBox);
    }//GEN-LAST:event_filletsLabelMouseClicked

    private void openLinkInBrowser(HyperlinkEvent evt) {
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
            processExecutor.executeProcess("sudo", "chfn", "-f", newFullName,
                    System.getProperty("user.name"));
        }

        // update exchange partition label
        String newExchangePartitionLabel =
                exchangePartitionNameTextField.getText();
        LOGGER.log(Level.INFO, "new exchange partition label: \"{0}\"",
                newExchangePartitionLabel);
        if (!newExchangePartitionLabel.isEmpty()
                && !newExchangePartitionLabel.equals(exchangePartitionLabel)) {
            processExecutor.executeProcess("sudo", "dosfslabel",
                    exchangePartition, newExchangePartitionLabel);
        }

        installSelectedPackages();

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

    private String getExchangePartition() throws IOException {

        // determine system partition
        String systemPartition = null;
        List<String> mounts = readFile(new File("/proc/mounts"));
        for (String mount : mounts) {
            String[] tokens = mount.split(" ");
            if (tokens[0].startsWith("/dev/")
                    && (tokens[1].equals("/live/image")
                    || tokens[1].equals("/lib/live/mount/medium"))) {
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
                + "       sudo kill -9 ${ID}\n"
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
        numberOfPackages += flashCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += readerCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += additionalFontsCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += multimediaCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += googleEarthCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += skypeCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += laCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += riliCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += frogattoCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += supertuxkartCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += wesnothCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += flareCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += astromenaceCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += filletsCheckBox.isSelected() ? 1 : 0;
        numberOfPackages += hedgewarsCheckBox.isSelected() ? 1 : 0;
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
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("progress".equals(evt.getPropertyName())) {
                        Integer progress = (Integer) evt.getNewValue();
                        progressDialog.setProgress(progress);
                    }
                }
            });
            installer.execute();
            progressDialog.setVisible(true);
        }

        checkAllPackages();
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

        // games
        checkInstall(riliCheckBox, riliLabel,
                "Welcome.riliLabel.text", "lernstick-ri-li");
        checkInstall(frogattoCheckBox, frogattoLabel,
                "Welcome.frogattoLabel.text", "frogatto");
        checkInstall(filletsCheckBox, filletsLabel,
                "Welcome.filletsLabel.text", "lernstick-fillets-ng");
        checkInstall(supertuxkartCheckBox, supertuxkartLabel,
                "Welcome.supertuxkartLabel.text", "supertuxkart");
        checkInstall(wesnothCheckBox, wesnothLabel,
                "Welcome.wesnothLabel.text", "wesnoth");
        checkInstall(flareCheckBox, flareLabel,
                "Welcome.flareLabel.text", "flare");
        checkInstall(hedgewarsCheckBox, hedgewarsLabel,
                "Welcome.hedgewarsLabel.text", "hedgewars");
        checkInstall(astromenaceCheckBox, astromenaceLabel,
                "Welcome.astromenaceLabel.text", "lernstick-astromenace");
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

    private void exitProgram() {
        // update "show dialog at startup" property
        try {
            properties.setProperty(SHOW_STARTUP,
                    showCheckBox.isSelected() ? "true" : "false");
            properties.store(new FileOutputStream(propertiesFile),
                    "lernstick Welcome dialog properties");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        System.exit(0);
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
                    + "sudo apt-get" + getAptGetProxyLine() + "update";
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
            installPackage(flashCheckBox, "Welcome.flashLabel.text",
                    "/ch/fhnw/lernstickwelcome/icons/48x48/Adobe_Flash_cs3.png",
                    FLASH_PACKAGE);
            installAdobeReader();
//            installPackage(readerCheckBox, "Welcome.readerLabel.text",
//                    "/ch/fhnw/lernstickwelcome/icons/Adobe_Reader_8_icon.png",
//                    ACROREAD_PACKAGES);
            installPackage(additionalFontsCheckBox, "Welcome.fontsLabel.text",
                    "/ch/fhnw/lernstickwelcome/icons/48x48/fonts.png",
                    FONTS_PACKAGES);
            installPackage(multimediaCheckBox, "Welcome.multimediaLabel.text",
                    "/ch/fhnw/lernstickwelcome/icons/48x48/package_multimedia.png",
                    MULTIMEDIA_PACKAGES);
            installGoogleEarth();
            installPackage(skypeCheckBox, "Welcome.skypeLabel.text",
                    "/ch/fhnw/lernstickwelcome/icons/48x48/skype.png",
                    "skype");

            // teaching system
            installPackage(laCheckBox, "LA_Teaching_System",
                    "/ch/fhnw/lernstickwelcome/icons/48x48/LinuxAdvanced.png",
                    "lateaching", "lateachingtools");

            // games
            installPackage(riliCheckBox, "Welcome.riliLabel.text",
                    "/ch/fhnw/lernstickwelcome/icons/48x48/ri-li.png",
                    "lernstick-ri-li");
            installPackage(frogattoCheckBox, "Welcome.frogattoLabel.text",
                    "/ch/fhnw/lernstickwelcome/icons/48x48/frogatto.png",
                    "frogatto");
            installPackage(filletsCheckBox, "Welcome.filletsLabel.text",
                    "/ch/fhnw/lernstickwelcome/icons/48x48/fillets.png",
                    "lernstick-fillets-ng", "fillets-ng-data-cs");
            installPackage(supertuxkartCheckBox, "Welcome.supertuxkartLabel.text",
                    "/ch/fhnw/lernstickwelcome/icons/48x48/supertuxkart.png",
                    "live-supertuxkart");
            installPackage(wesnothCheckBox, "Welcome.wesnothLabel.text",
                    "/ch/fhnw/lernstickwelcome/icons/48x48/wesnoth.png",
                    "wesnoth", "wesnoth-music");
            installPackage(flareCheckBox, "Welcome.flareLabel.text",
                    "/ch/fhnw/lernstickwelcome/icons/48x48/flare.png",
                    "flare");
            installPackage(hedgewarsCheckBox, "Welcome.hedgewarsLabel.text",
                    "/ch/fhnw/lernstickwelcome/icons/48x48/hedgewars.png",
                    "hedgewars");
            installPackage(astromenaceCheckBox, "Welcome.astromenaceLabel.text",
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
                    + "sudo dpkg -i " + fileName + '\n'
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
                    "sudo apt-get" + getAptGetProxyLine()
                    + "-y --force-yes install lsb-core\n"
                    + "cd " + USER_HOME + '\n'
                    + "wget" + getWgetProxyLine()
                    + "https://dl-ssl.google.com/linux/direct/" + debName + '\n'
                    + "sudo dpkg -i " + debName + '\n'
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

        private void installPackage(JCheckBox checkBox, String key,
                String iconPath, String... packageNames) {
            if (!checkBox.isSelected()) {
                LOGGER.log(Level.INFO, "checkBox not selected: {0}", checkBox);
                return;
            }
            String infoString = MessageFormat.format(
                    BUNDLE.getString("Installing"), BUNDLE.getString(key));
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            ProgressAction progressAction =
                    new ProgressAction(infoString, icon);
            publish(progressAction);

            for (String packageName : packageNames) {
                LOGGER.log(Level.INFO, "installing package \"{0}\"", packageName);
            }
            List<String> commandList = new ArrayList<String>();
            commandList.add("sudo");
            commandList.add("DEBIAN_FRONTEND=noninteractive");
            commandList.add("apt-get");
            if (proxyCheckBox.isSelected()) {
                commandList.add("-o");
                commandList.add(getAptGetAcquireLine());
            }
            commandList.add("-y");
            commandList.add("--force-yes");
            commandList.add("install");
            commandList.addAll(Arrays.asList(packageNames));
            String[] commandArray = new String[commandList.size()];
            commandArray = commandList.toArray(commandArray);

//            // enforce non-interactive installs
//            Map<String,String> environment = new HashMap<String, String>();
//            environment.put("DEBIAN_FRONTEND", "noninteractive");
//            processExecutor.setEnvironment(environment);

            int exitValue = processExecutor.executeProcess(
                    true, true, commandArray);
            if (exitValue != 0) {
                String errorMessage = "apt-get failed with the following "
                        + "output:\n" + processExecutor.getOutput();
                LOGGER.severe(errorMessage);
                showErrorMessage(errorMessage);
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
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            switch (index) {
                case 0:
                    label.setIcon(new ImageIcon(getClass().getResource(
                            "/ch/fhnw/lernstickwelcome/icons/messagebox_info.png")));
                    break;

                case 1:
                    label.setIcon(new ImageIcon(getClass().getResource(
                            "/ch/fhnw/lernstickwelcome/icons/32x32/copyright.png")));
                    break;

                case 2:
                    label.setIcon(new ImageIcon(getClass().getResource(
                            "/ch/fhnw/lernstickwelcome/icons/32x32/LinuxAdvanced.png")));
                    break;

                case 3:
                    label.setIcon(new ImageIcon(getClass().getResource(
                            "/ch/fhnw/lernstickwelcome/icons/32x32/input-gaming.png")));
                    break;

                case 4:
                    label.setIcon(new ImageIcon(getClass().getResource(
                            "/ch/fhnw/lernstickwelcome/icons/32x32/network-server.png")));
                    break;

                case 5:
                    label.setIcon(new ImageIcon(getClass().getResource(
                            "/ch/fhnw/lernstickwelcome/icons/32x32/edit-rename.png")));
                    break;

                default:
                    label.setIcon(null);
            }
            label.setBorder(new EmptyBorder(5, 5, 5, 5));
            return label;
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox additionalFontsCheckBox;
    private javax.swing.JButton applyButton;
    private javax.swing.JCheckBox astromenaceCheckBox;
    private javax.swing.JLabel astromenaceLabel;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel dummyPanel;
    private javax.swing.JLabel exchangePartitionNameLabel;
    private javax.swing.JTextField exchangePartitionNameTextField;
    private javax.swing.JPanel fillPanel;
    private javax.swing.JCheckBox filletsCheckBox;
    private javax.swing.JLabel filletsLabel;
    private javax.swing.JCheckBox flareCheckBox;
    private javax.swing.JLabel flareLabel;
    private javax.swing.JCheckBox flashCheckBox;
    private javax.swing.JLabel flashLabel;
    private javax.swing.JLabel fontsLabel;
    private javax.swing.JCheckBox frogattoCheckBox;
    private javax.swing.JLabel frogattoLabel;
    private javax.swing.JLabel gamesInfoLabel;
    private javax.swing.JPanel gamesPanel;
    private javax.swing.JCheckBox googleEarthCheckBox;
    private javax.swing.JLabel googleEarthLabel;
    private javax.swing.JCheckBox hedgewarsCheckBox;
    private javax.swing.JLabel hedgewarsLabel;
    private javax.swing.JEditorPane infoEditorPane;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JScrollPane infoScrollPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JCheckBox laCheckBox;
    private javax.swing.JLabel laLabel;
    private javax.swing.JPanel mainCardPanel;
    private javax.swing.JList menuList;
    private javax.swing.JScrollPane menuScrollPane;
    private javax.swing.JPanel miscPanel;
    private javax.swing.JCheckBox multimediaCheckBox;
    private javax.swing.JLabel multimediaLabel;
    private javax.swing.JLabel namesInfoLabel;
    private javax.swing.JPanel namesPanel;
    private javax.swing.JPanel navigaionPanel;
    private javax.swing.JButton nextButton;
    private javax.swing.JLabel nonfreeLabel;
    private javax.swing.JPanel nonfreePanel;
    private javax.swing.JButton previousButton;
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
    private javax.swing.JCheckBox readerCheckBox;
    private javax.swing.JLabel readerLabel;
    private javax.swing.JPanel recommendedPanel;
    private javax.swing.JCheckBox riliCheckBox;
    private javax.swing.JLabel riliLabel;
    private javax.swing.JCheckBox showCheckBox;
    private javax.swing.JCheckBox skypeCheckBox;
    private javax.swing.JLabel skypeLabel;
    private javax.swing.JCheckBox supertuxkartCheckBox;
    private javax.swing.JLabel supertuxkartLabel;
    private javax.swing.JEditorPane teachingEditorPane;
    private javax.swing.JPanel teachingPanel;
    private javax.swing.JScrollPane teachingScrollPane;
    private javax.swing.JLabel userNameLabel;
    private javax.swing.JTextField userNameTextField;
    private javax.swing.JLabel welcomeLabel;
    private javax.swing.JCheckBox wesnothCheckBox;
    private javax.swing.JLabel wesnothLabel;
    // End of variables declaration//GEN-END:variables
}
