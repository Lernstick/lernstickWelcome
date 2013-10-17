package ch.fhnw.lernstickwelcome;

public class IPTableEntry {

    public enum Protocol {

        TCP, UDP
    }
    private Protocol protocol;
    private String target;
    private int port;
    private String description;

    public IPTableEntry(
            Protocol protocol, String target, int port, String description) {
        this.protocol = protocol;
        this.target = target;
        this.port = port;
        this.description = description;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
