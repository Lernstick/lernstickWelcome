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
    public static final String SHOW_PASSWORD_DIALOG = "ShowPasswordDialog";
    
    // PATHS
    // !!! NO trailing slash at the end (would break comparison later) !!!
    public static final String IMAGE_DIRECTORY = "/lib/live/mount/medium";
    
    public static final String USER_JBACKPACK_PREFERENCES 
            = "/home/user/.java/.userPrefs/ch/fhnw/jbackpack/";
    public static final String ROOT_JBACKPACK_PREFERENCES 
            = "/root/.java/.userPrefs/ch/fhnw/jbackpack/";
    
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
    public static final String EMPTY_PASSWORD_HINT_FILE = 
            "/home/user/.kde/share/config/empty_passwd_info";
    public static final String SQUID_ACCESS_LOG_FILE_PATH = "/var/log/squid/access.log";
    
    public static final String USER_HOME = System.getProperty("user.home");
    
    public static final String RESOURCE_FILE_PATH = "";
    public static final String HELP_FILE_PATH = RESOURCE_FILE_PATH + "/help";
    public static final String ICON_FILE_PATH = RESOURCE_FILE_PATH + "/icon";
    public static final String ICON_APPLICATION_FOLDER = "apps";
    public static final String ICON_APPLICATION_FILE_PATH = ICON_FILE_PATH + "/" + ICON_APPLICATION_FOLDER;
}
