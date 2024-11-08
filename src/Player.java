import java.net.InetAddress;

public class Player {
    private Long id;
    private InetAddress address;
    private int port;

    private boolean isEmpty;

    public Player(Long id, InetAddress address, int port) {
        this.id = id;
        this.address = address;
        this.port = port;
        this.isEmpty = false;
    }

    public Player() {
        this.isEmpty = true;
    }

    public Long getId() {
        return id;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public boolean isEmpty() {
        return isEmpty;
    }
}

