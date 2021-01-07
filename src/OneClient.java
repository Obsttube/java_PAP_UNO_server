import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OneClient extends Thread {
    private Socket socket = null;
    private OutputStream outputStream;
    private InputStream inputStream;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    SecretPlayer secretPlayer = null;
    Integer choosenCardIndex = null;
    Card.Color choosenColor = null;

    OneClient(Socket socket) throws IOException {
        this.socket = socket;
        outputStream = socket.getOutputStream();
        objectOutputStream = new ObjectOutputStream(outputStream);
    }

    private void sendServerRequest(ServerRequest serverRequest) {
        try {
            if (objectOutputStream == null) {
                if (outputStream == null)
                    outputStream = socket.getOutputStream();
                objectOutputStream = new ObjectOutputStream(outputStream);
            }
            objectOutputStream.reset();
            objectOutputStream.writeObject(serverRequest);
        } catch (IOException e) {
            // TODO
        }
    }

    public void sendPlayerList(List<Player> players) {
        ServerRequest serverRequest = new ServerRequest(ServerRequest.RequestType.LIST_OF_PLAYERS);
        serverRequest.players = players;
        sendServerRequest(serverRequest);
    }

    public void sendCards(List<Card> hand, Card table, Card.Color currentWildColor) {
        ServerRequest serverRequest = new ServerRequest(ServerRequest.RequestType.YOUR_CARDS);
        serverRequest.cardsOnHand = hand;
        serverRequest.cardOnTable = table;
        serverRequest.currentWildColor = currentWildColor;
        sendServerRequest(serverRequest);
    }

    public void sendYourTurn() {
        ServerRequest serverRequest = new ServerRequest(ServerRequest.RequestType.YOUR_TURN);
        sendServerRequest(serverRequest);
    }

    public void sendIllegalMove() {
        ServerRequest serverRequest = new ServerRequest(ServerRequest.RequestType.ILLEGAL_MOVE);
        sendServerRequest(serverRequest);
    }

    public void sendChooseColor() {
        ServerRequest serverRequest = new ServerRequest(ServerRequest.RequestType.CHOOSE_COLOR);
        sendServerRequest(serverRequest);
    }

    public void run() {
        try {
            inputStream = socket.getInputStream();
            ClientRequest clientRequest;
            ServerRequest serverRequest;
            String selectedLobbyId = null;

            objectInputStream = new ObjectInputStream(inputStream);

            // TODO add stages and allow some functions only after login or after joining a
            // lobby

            while (true) {
                clientRequest = (ClientRequest) objectInputStream.readObject();
                if (this.secretPlayer != null)
                    System.out.println(this.secretPlayer.name + ": " + clientRequest.requestType);
                else
                    System.out.println(clientRequest.requestType);
                switch (clientRequest.requestType) {
                    case CLICK_START:
                        if (Main.lobby_games.get(selectedLobbyId) == null && selectedLobbyId != null) {
                            Main.createGame(selectedLobbyId); // send player list to all players
                        }
                        break;
                    case CHOOSE_CARD:
                        this.choosenCardIndex = clientRequest.choosenCardIndex;
                        break;
                    case CHOOSE_COLOR:
                        this.choosenColor = clientRequest.choosenColor;
                        break;
                    case LOGIN:
                        this.secretPlayer = new SecretPlayer(clientRequest.playerName, this);
                        serverRequest = new ServerRequest(ServerRequest.RequestType.LOGIN_SUCCESSFUL);
                        sendServerRequest(serverRequest);
                        break;
                    case GET_LOBBY_LIST:
                        serverRequest = new ServerRequest(ServerRequest.RequestType.LOBBY_LIST);
                        serverRequest.lobbyList = new ArrayList<Lobby>(Main.lobbies.values());
                        sendServerRequest(serverRequest);
                        break;
                    case JOIN_LOBBY:
                        System.out.println(this.secretPlayer.name + ": " + clientRequest.lobbyId);
                        if (Main.lobbies.get(clientRequest.lobbyId) != null)
                            selectedLobbyId = clientRequest.lobbyId;
                        if (selectedLobbyId != null) {
                            List<WeakReference<OneClient>> players = Main.lobby_players.get(selectedLobbyId);
                            if (players == null) {
                                players = new ArrayList<>();
                                players.add(new WeakReference<>(this));
                                Main.lobby_players.put(selectedLobbyId, players);
                            } else {
                                players.add(new WeakReference<>(this));
                                Main.lobby_players.put(selectedLobbyId, players);
                            }
                            WeakReference<OneClient> weakOneClient = null;
                            for (WeakReference<OneClient> client : Main.clients_list) {
                                OneClient oneClient = client.get();
                                if (oneClient != null && oneClient == this) {
                                    weakOneClient = client;
                                }
                            }
                            if (weakOneClient != null)
                                Main.clients_list.remove(weakOneClient);
                            Main.broadcastPlayerList(selectedLobbyId); // send player list to all players
                        }
                        break;
                    case CREATE_LOBBY:
                        Lobby lobby = new Lobby(clientRequest.lobbyName);
                        Main.lobbies.put(lobby.id, lobby);
                        break;
                    default:
                        break;
                }
            }

        } catch (ClassNotFoundException e) {
            Logger.getLogger(getName()).log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            // TODO
            // Logger.getLogger(getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }
        }
        System.out.println("Connection closed");
    }

    private List<Lobby> List(Collection<Lobby> values) {
        return null;
    }
}