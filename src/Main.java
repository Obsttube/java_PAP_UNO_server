import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static List<Lobby> lobbyList = new ArrayList<>();
    public static List<WeakReference<OneClient>> clients_list = new ArrayList<>();
    public static List<WeakReference<OneClient>> lobby1_players = new ArrayList<>();
    public static Game game;

    public static void broadcastPlayerList(){
        List<Player> players = new ArrayList<>();
        for(WeakReference<OneClient> weakClient : clients_list){
            OneClient client = weakClient.get();
            if(client != null){
                players.add(client.secretPlayer.getPlayer());
            }
        }
        for(WeakReference<OneClient> weakClient : clients_list){
            OneClient client = weakClient.get();
            if(client != null)
                client.sendPlayerList(players);
        }
    }

    public static void createGame(){
        List<SecretPlayer> players = new ArrayList<>();
        for(WeakReference<OneClient> weakClient : clients_list){
            OneClient client = weakClient.get();
            if(client != null){
                players.add(client.secretPlayer);
            }
        }
        game = new Game(players);
        game.start();
    }

    public static void main(String args[]) throws IOException, InterruptedException {
        lobbyList.add(new Lobby("1", "example"));
        while (true) {
            ServerSocket serverSocket = new ServerSocket(25566);
            System.out.println("Waiting for a connection"); 
            Socket socket = serverSocket.accept();
            OneClient oneClient = new OneClient(socket);
            clients_list.add(new WeakReference<>(oneClient));
            oneClient.start();
            Thread.sleep(500);
            serverSocket.close();
        }
    }   
}