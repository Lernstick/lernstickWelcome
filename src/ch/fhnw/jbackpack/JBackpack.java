package ch.fhnw.jbackpack;

/**
 * This is only here so that we can access the JBackpack preferences
 *
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class JBackpack {

    /**
     * preferences key for the source directory
     */
    public static final String SOURCE = "source";

    /**
     * preferences key for the destination ("local", "ssh" or "smb")
     */
    public static final String DESTINATION = "destination";

    /**
     * preferences key for the local destination directory
     */
    public static final String LOCAL_DESTINATION_DIRECTORY
            = "local_destination_directory";
}
