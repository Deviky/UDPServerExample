import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {

    private Map<Long, Map<Long, Player>> roomPlayers;
    private final int maxPacketSize = 20;
    private final byte[] buffer = new byte[maxPacketSize];
    private final DatagramSocket socket;
    private final ExecutorService threadPool;

    public GameServer(int port) throws IOException {
        this.socket = new DatagramSocket(port);
        this.roomPlayers = new ConcurrentHashMap<>();
        this.threadPool = Executors.newCachedThreadPool();  // Пул потоков для обработки сообщений от игроков
    }

    public void createRoom(Long roomId, List<Long> playerIds) {
        if (roomId == null || playerIds == null) {
            System.err.println("Room ID or Player IDs cannot be null");
            return;
        }
        Map<Long, Player> playerAddresses = new ConcurrentHashMap<>();
        for (Long playerId : playerIds) {
            if (playerId != null) {
                playerAddresses.put(playerId, new Player());
            }
        }
        roomPlayers.put(roomId, playerAddresses);
    }

    public void start() {
        System.out.println("Server starts");
        // Запуск основного потока для получения и обработки пакетов
        threadPool.submit(this::run);
    }

    private void run() {
        while (true) {
            try {
                DatagramPacket packetFromClient = new DatagramPacket(buffer, buffer.length);
                socket.receive(packetFromClient);

                // Обрабатываем полученный пакет в отдельном потоке
                threadPool.submit(() -> processPacket(packetFromClient));

            } catch (IOException e) {
                System.err.println("Error receiving packet: " + e.getMessage());
            }
        }
    }

    private void processPacket(DatagramPacket packetFromClient) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(packetFromClient.getData());
            long playerId = buffer.getLong();
            long roomId = buffer.getLong();
            int message = buffer.getInt();
            System.out.println(packetFromClient.getAddress() + " " + packetFromClient.getPort());

            if (checkPlayer(roomId, playerId, packetFromClient)) {
                sendToRoom(roomId, playerId, message);
            }
        } catch (Exception e) {
            System.err.println("Error processing packet: " + e.getMessage());
        }
    }

    private boolean checkPlayer(long roomId, long playerId, DatagramPacket packetFromClient) {
        Map<Long, Player> playerAddresses = roomPlayers.get(roomId);
        if (playerAddresses != null && playerAddresses.containsKey(playerId)) {
            Player player = playerAddresses.get(playerId);
            if (player == null || player.isEmpty()){
                playerAddresses.put(playerId, new Player(playerId, packetFromClient.getAddress(), packetFromClient.getPort()));
            }
            return true;
        }
        return false;
    }


    private void sendToRoom(Long roomId, Long senderId, int message) {
        Map<Long, Player> players = roomPlayers.get(roomId);
        if (players != null) {
            String responseMessage = "Player " + senderId + " says: " + message;
            byte[] responseBytes = responseMessage.getBytes();

            players.forEach((playerId, player) -> {
                if (player != null && !player.isEmpty() && !playerId.equals(senderId)) {
                    try {
                        DatagramPacket packetToClient = new DatagramPacket(
                                responseBytes, responseBytes.length, player.getAddress(), player.getPort()
                        );
                        socket.send(packetToClient);
                        System.out.println("Message sent to player " + playerId + ": " + responseMessage);
                    } catch (IOException e) {
                        System.err.println("Error sending to player " + playerId + ": " + e.getMessage());
                    }
                }
            });
        }
    }
}
