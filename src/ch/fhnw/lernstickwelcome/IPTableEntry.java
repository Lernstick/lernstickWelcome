package ch.fhnw.lernstickwelcome;

/**
 * an entry for an iptables rule
 *
 * @author ronny
 */
public class IPTableEntry {

    /**
     * the iptables protocol
     */
    public enum Protocol {

        /**
         * the TCP protocol
         */
        TCP,
        /**
         * the UDP protocol
         */
        UDP
    }
    private Protocol protocol;
    private String target;
    private String portRange;
    private String description;

    /**
     * creates a new IPTableEntry
     *
     * @param protocol the protocol
     * @param target the target IP address or hostname
     * @param portRange the port range of the entry
     * @param description the description of this entry
     */
    public IPTableEntry(Protocol protocol,
            String target, String portRange, String description) {
        this.protocol = protocol;
        this.target = target;
        this.portRange = portRange;
        this.description = description;
    }

    /**
     * returns the protocol for this entry
     *
     * @return the protocol for this entry
     */
    public Protocol getProtocol() {
        return protocol;
    }

    /**
     * sets the protocol for this entry
     *
     * @param protocol the protocol for this entry
     */
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    /**
     * returns the target (IP address or hostname) of this entry
     *
     * @return the target (IP address or hostname) of this entry
     */
    public String getTarget() {
        return target;
    }

    /**
     * sets the target (IP address or hostname) of this entry
     *
     * @param target the target (IP address or hostname) of this entry
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * returns the port (0..65535) or portrange (<minport>:<maxport>) for this
     * entry
     *
     * @return the port (0..65535) or portrange (<minport>:<maxport>) for this
     * entry
     */
    public String getPortRange() {
        return portRange;
    }

    /**
     * sets the port (0..65535) or portrange (<minport>:<maxport>) for this
     * entry
     *
     * @param portRange the port (0..65535) or portrange (<minport>:<maxport>)
     * for this entry
     */
    public void setPortRange(String portRange) {
        this.portRange = portRange;
    }

    /**
     * returns the description of this entry
     * @return the description of this entry
     */
    public String getDescription() {
        return description;
    }

    /**
     * sets the description of this entry
     * @param description the description of this entry
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
