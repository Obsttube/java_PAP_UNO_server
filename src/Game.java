import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Game extends Thread {
    List<Card> availableCards = new ArrayList<>();
    List<SecretPlayer> players = new ArrayList<>();
    int currentPlayerIndex;
    Deque<Card> cardsOnTable = new LinkedList<Card>();

    Game(List<SecretPlayer> players){
        this.players = players;
        loadInitialCards();
        giveOutCards();
        currentPlayerIndex = ThreadLocalRandom.current().nextInt(0, players.size());
    }

    private void loadInitialCards(){
        Card.Color[] colors = {Card.Color.RED, Card.Color.YELLOW, Card.Color.GREEN, Card.Color.BLUE};
        for(Card.Color color : colors){
            availableCards.add(new Card(Card.Type.ZERO, color));
            for(int i = 0; i < 2; i++){
                availableCards.add(new Card(Card.Type.ONE, color));
                availableCards.add(new Card(Card.Type.TWO, color));
                availableCards.add(new Card(Card.Type.THREE, color));
                availableCards.add(new Card(Card.Type.FOUR, color));
                availableCards.add(new Card(Card.Type.FIVE, color));
                availableCards.add(new Card(Card.Type.SIX, color));
                availableCards.add(new Card(Card.Type.SEVEN, color));
                availableCards.add(new Card(Card.Type.EIGHT, color));
                availableCards.add(new Card(Card.Type.NINE, color));
                availableCards.add(new Card(Card.Type.SKIP, color));
                availableCards.add(new Card(Card.Type.REVERSE, color));
                availableCards.add(new Card(Card.Type.ADD_TWO, color));
            }
        }
        for(int i = 0; i < 4; i++){
            availableCards.add(new Card(Card.Type.WILD, Card.Color.BLACK));
            availableCards.add(new Card(Card.Type.WILD_DRAW_FOUR, Card.Color.BLACK));
        }
        Collections.shuffle(availableCards);
    }

    private void giveOutCards(){
        for(SecretPlayer secretPlayer : players){
            for(int i = 0; i < 7; i++){
                secretPlayer.cards.add(availableCards.get(0));
                availableCards.remove(0);
            }
        }
        cardsOnTable.add(availableCards.get(0));
        availableCards.remove(0);
        updateAllPlayersCards();
    }

    private int chooseCard(SecretPlayer currentPlayer){
        currentPlayer.sendYourTurn();
        Integer cardIndex = null;
        while(cardIndex == null){
            cardIndex = currentPlayer.getCardIndex();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO
            }
        }
        System.out.println("cardIndex: " + cardIndex);
        return cardIndex.intValue();
    }

    private void nextPlayer(){
        if(currentPlayerIndex >= players.size() - 1)
            currentPlayerIndex = 0;
        else
            currentPlayerIndex++;
    }

    private void updateAllPlayersCards(){
        for(SecretPlayer secretPlayer : players)
            secretPlayer.sendCurrentCards(cardsOnTable.peekLast());
    }

    private boolean isMoveLegal(Card choosenCard){
        if(choosenCard.type == Card.Type.WILD_DRAW_FOUR){
            // TODO check when legal - https://www.unorules.com/
            // for now always legal
            return true;
        }else if(cardsOnTable.peekLast().color == choosenCard.color){
            return true;
        }else if(cardsOnTable.peekLast().type == choosenCard.type){
            return true;
        }
        return false;
    }
    
    public void run() {
        while(true){
            SecretPlayer currentPlayer = players.get(currentPlayerIndex);
            int choosenCardIndex = chooseCard(currentPlayer);
            Card choosenCard = currentPlayer.cards.get(choosenCardIndex);
            if(isMoveLegal(choosenCard)){
                cardsOnTable.add(choosenCard);
                currentPlayer.cards.remove(choosenCardIndex);
                updateAllPlayersCards();
                nextPlayer();
            } else{
                currentPlayer.sendIllegalMove();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO
            }
        }
    }  
}