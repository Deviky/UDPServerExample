import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        try {
            // Создаем экземпляр GameServer с указанным портом
            GameServer gameServer = new GameServer(9099);

            // Создаем комнаты и добавляем в них игроков
            gameServer.createRoom(1L, Arrays.asList(101L, 102L, 103L)); // Комната 1 с игроками 101, 102, 103
            gameServer.createRoom(2L, Arrays.asList(201L, 202L, 203L)); // Комната 2 с игроками 201, 202, 203
            gameServer.createRoom(3L, Arrays.asList(301L, 302L));       // Комната 3 с игроками 301, 302

            // Запускаем сервер
            gameServer.start();

            System.out.println("Game server started on port 9099...");
        } catch (IOException e) {
            System.err.println("Failed to start game server: " + e.getMessage());
        }
    }
}