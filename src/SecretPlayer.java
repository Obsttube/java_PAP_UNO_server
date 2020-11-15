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
    public void sendCurrentCards(Card table){
        OneClient client = weakClient.get();
        if(client != null){
            client.sendCards(cards, table);
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
    public Integer getCardIndex(){
        OneClient client = weakClient.get();
        if(client != null){
            Integer choosenCardIndex = client.choosenCardIndex;
            client.choosenCardIndex = null;
            return choosenCardIndex;
        }
        return null;
    }
    public Player getPlayer(){
        return new Player(this.name, this.cards.size());
    }
}
