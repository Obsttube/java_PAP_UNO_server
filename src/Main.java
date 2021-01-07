import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class Main {

    //public static List<Lobby> lobbyList = new ArrayList<>();
    public static List<WeakReference<OneClient>> clients_list = new ArrayList<>();
    //public static List<WeakReference<OneClient>> lobby1_players = new ArrayList<>();
    public static Hashtable<String, List<WeakReference<OneClient>>> lobby_players = new Hashtable<>(); // <lobbyId, List<...>>
    public static Hashtable<String, Game> lobby_games = new Hashtable<>(); // <lobbyId, List<...>>
    public static Hashtable<String, Lobby> lobbies = new Hashtable<>(); // <lobbyId, List<...>>
    //public static Game game;

    public static void broadcastPlayerList(String lobbyId){
        List<Player> players = new ArrayList<>();
        for(WeakReference<OneClient> weakClient : lobby_players.get(lobbyId)){
            OneClient client = weakClient.get();
            if(client != null){
                players.add(client.secretPlayer.getPlayer());
            }
        }
        for(WeakReference<OneClient> weakClient : lobby_players.get(lobbyId)){
            OneClient client = weakClient.get();
            if(client != null)
                client.sendPlayerList(players);
        }
    }

    public static void createGame(String lobbyId){
        List<SecretPlayer> players = new ArrayList<>();
        for(WeakReference<OneClient> weakClient : lobby_players.get(lobbyId)){
            OneClient client = weakClient.get();
            if(client != null){
                players.add(client.secretPlayer);
            }
        }
        lobby_players.remove(lobbyId);
        lobbies.remove(lobbyId);
        lobby_games.put(lobbyId, new Game(players));
        lobby_games.get(lobbyId).start();
    }

    public static void main(String args[]) throws IOException, InterruptedException {
        Lobby lobby = new Lobby("example");
        lobbies.put(lobby.id, lobby);
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