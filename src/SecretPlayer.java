import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SecretPlayer{
    public List<Card> cards = new ArrayList<>();
    private final WeakReference<OneClient> weakClient;
    public String name;
    public SecretPlayer(String name, final OneClient client) {
        this.name = name;
        this.weakClient = new WeakReference<OneClient>(client);
    }
    public void sendCurrentCards(Card table, Card.Color currentWildColor){
        OneClient client = weakClient.get();
        if(client != null){
            client.sendCards(cards, table, currentWildColor);
        }
    }
    public void sendYourTurn(){
        OneClient client = weakClient.get();
        if(client != null){
            client.choosenCardIndex = null;
            client.sendYourTurn();
        }
    }
    public void sendIllegalMove(){
        OneClient client = weakClient.get();
        if(client != null){
            client.sendIllegalMove();
        }
    }
    public void sendChooseColor(){
        OneClient client = weakClient.get();
        if(client != null){
            client.choosenColor = null;
            client.sendChooseColor();
        }
    }
    public Integer getCardIndex(){
        OneClient client = weakClient.get();
        if(client != null){
            Integer choosenCardIndex = client.choosenCardIndex;
            client.choosenCardIndex = null;
            return choosenCardIndex;
        }
        return null;
    }
    public Card.Color getChoosenColor(){
        OneClient client = weakClient.get();
        if(client != null){
            Card.Color choosenColor = client.choosenColor;
            client.choosenColor = null;
            return choosenColor;
        }
        return null;
    }
    public Player getPlayer(){
        return new Player(this.name, this.cards.size());
    }
}
