import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OneClient extends Thread {
    private Socket socket=null;
    private OutputStream outputStream;
    private InputStream inputStream;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    
    SecretPlayer secretPlayer = null;
    Integer choosenCardIndex = null;

    OneClient(Socket socket) throws IOException {
        this.socket = socket;
        outputStream = socket.getOutputStream();
        objectOutputStream = new ObjectOutputStream(outputStream);
    }

    public void sendPlayerList(List<Player> players){
        try{
            if(outputStream == null)
                outputStream = socket.getOutputStream();
            if(objectOutputStream == null)
                objectOutputStream = new ObjectOutputStream(outputStream);
            ServerRequest serverRequest = new ServerRequest(ServerRequest.RequestType.LIST_OF_PLAYERS);
            serverRequest.players = players;
            objectOutputStream.reset();
            objectOutputStream.writeObject(serverRequest);
        } catch (IOException e){
            // TODO
        }
    }

    public void sendCards(List<Card> hand, Card table){
        try{
            if(outputStream == null)
                outputStream = socket.getOutputStream();
            if(objectOutputStream == null)
                objectOutputStream = new ObjectOutputStream(outputStream);
            ServerRequest serverRequest = new ServerRequest(ServerRequest.RequestType.YOUR_CARDS);
            serverRequest.cardsOnHand = hand;
            serverRequest.cardOnTable = table;
            objectOutputStream.reset();
            objectOutputStream.writeObject(serverRequest);
        } catch (IOException e){
            // TODO
        }
    }

    public void sendYourTurn(){
        try{
            if(outputStream == null)
                outputStream = socket.getOutputStream();
            if(objectOutputStream == null)
                objectOutputStream = new ObjectOutputStream(outputStream);
            ServerRequest serverRequest = new ServerRequest(ServerRequest.RequestType.YOUR_TURN);
            objectOutputStream.reset();
            objectOutputStream.writeObject(serverRequest);
        } catch (IOException e){
            // TODO
        }
    }
    
    public void run() {
        try {
            inputStream = socket.getInputStream();
            ClientRequest clientRequest;
            ServerRequest serverRequest;

            objectInputStream = new ObjectInputStream(inputStream);
            clientRequest = (ClientRequest)objectInputStream.readObject();
            System.out.println(clientRequest.playerName + ": " + clientRequest.requestType); // LOGIN
            this.secretPlayer = new SecretPlayer(clientRequest.playerName, this);

            serverRequest = new ServerRequest(ServerRequest.RequestType.LOGIN_SUCCESSFUL);
            objectOutputStream.reset();
            objectOutputStream.writeObject(serverRequest);

            clientRequest = (ClientRequest)objectInputStream.readObject();
            System.out.println(this.secretPlayer.name + ": " + clientRequest.requestType); // GET_LOBBY_LIST

            serverRequest = new ServerRequest(ServerRequest.RequestType.LOBBY_LIST);
            serverRequest.lobbyList = Main.lobbyList;
            objectOutputStream.reset();
            objectOutputStream.writeObject(serverRequest);

            clientRequest = (ClientRequest)objectInputStream.readObject();
            System.out.println(this.secretPlayer.name + ": " + clientRequest.requestType); // JOIN_LOBBY
            System.out.println(this.secretPlayer.name + ": " + clientRequest.lobbyId);
            Main.lobby1_players.add(new WeakReference<>(this));
            Main.broadcastPlayerList(); // send player list to all players

            while(true){
                clientRequest = (ClientRequest)objectInputStream.readObject();
                System.out.println(this.secretPlayer.name + ": " + clientRequest.requestType);
                switch(clientRequest.requestType){
                    case CLICK_START:
                        if(Main.game == null){
                            Main.createGame();
                        }
                        break;
                    case CHOOSE_CARD:
                        this.choosenCardIndex = clientRequest.choosenCardIndex;
                        break;
                    default:
                        break;
                }
            }

        } catch (ClassNotFoundException e) {
            Logger.getLogger(getName()).log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            // TODO
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
}