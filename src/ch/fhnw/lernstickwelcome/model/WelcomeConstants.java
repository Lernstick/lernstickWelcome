/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class contains the Constants which are used in the Welcome Application.
 * 
 * @author sschw
 */
public final class WelcomeConstants {

    private WelcomeConstants() {}
    
    // PROPERTIES CONSTANTS
    public static final String PROPERTIES_PATH = "/etc/lernstickWelcome";
    public static final String SHOW_WELCOME = "ShowWelcome";
    public static final String SHOW_READ_ONLY_INFO = "ShowReadOnlyInfo";
    public static final String BACKUP = "Backup";
    public static final String BACKUP_SOURCE = "BackupSource";
    public static final String BACKUP_DIRECTORY_ENABLED = "BackupDirectoryEnabled";
    public static final String BACKUP_DIRECTORY = "BackupDirectory";
    public static final String BACKUP_PARTITION_ENABLED = "BackupPartitionEnabled";
    public static final String BACKUP_PARTITION = "BackupPartition";
    public static final String BACKUP_SCREENSHOT = "BackupScreenshot";
    public static final String BACKUP_FREQUENCY = "BackupFrequency";
    public static final String EXCHANGE_ACCESS = "ExchangeAccess";
    public static final String KDE_LOCK = "LockKDE";
    public static final String PASSWORD_CHANGED = "ShowPasswordDialog";
    
    // PATHS
    // !!! NO trailing slash at the end (would break comparison later) !!!
    public static final String IMAGE_DIRECTORY = "/lib/live/mount/medium";
    
    public static final String IP_TABLES_FILENAME
            = "/etc/lernstick-firewall/net_whitelist";
    public static final String URL_WHITELIST_FILENAME
            = "/etc/lernstick-firewall/url_whitelist";
    public static final String LOCAL_POLKIT_PATH
            = "/etc/polkit-1/localauthority/50-local.d";
    public static final String EXAM_POLKIT_PATH
            = "/etc/polkit-1/localauthority/55-lernstick-exam.d";
    public static final Path APPLETS_CONFIG_FILE = Paths.get(
            "/home/user/.kde/share/config/plasma-desktop-appletsrc");
    public static final Path ALSA_PULSE_CONFIG_FILE = Paths.get(
            "/usr/share/alsa/alsa.conf.d/pulse.conf");
    public static final Path UDISKS_PKLA_PATH = Paths.get(
    		"/etc/polkit-1/localauthority/50-local.d/10-udisks2.pkla");
    public static final String SQUID_ACCESS_LOG_FILE_PATH = "/var/log/squid/access.log";
    
    public static final String USER_HOME = System.getProperty("user.home");
    
    public static final String RESOURCE_FILE_PATH = "res";
    public static final String HELP_FILE_PATH = RESOURCE_FILE_PATH + "/help";
    public static final String ICON_FILE_PATH = RESOURCE_FILE_PATH + "/icon";
    public static final String ICON_APPLICATION_FOLDER = "apps";
    public static final String ICON_APPLICATION_FILE_PATH = ICON_FILE_PATH + "/" + ICON_APPLICATION_FOLDER;
}
