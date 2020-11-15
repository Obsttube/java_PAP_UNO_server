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
    Card.Color currentWildColor = null;
    private int direction = 1;

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
        cardsOnTable.addLast(availableCards.get(0));
        availableCards.remove(0);
        updateAllPlayersCards();
    }

    private void giveCardFromPile(SecretPlayer secretPlayer){
        if(availableCards.size() == 0){
            while(cardsOnTable.size() > 1){
                availableCards.add(cardsOnTable.peekFirst());
                cardsOnTable.removeFirst();
            }
            Collections.shuffle(availableCards);
        }
        secretPlayer.cards.add(availableCards.get(0));
        availableCards.remove(0);
    }

    private void sendCurrentCards(SecretPlayer secretPlayer){
        secretPlayer.sendCurrentCards(cardsOnTable.peekLast(), currentWildColor);
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

    private Card.Color chooseColor(SecretPlayer currentPlayer){
        currentPlayer.sendChooseColor();
        Card.Color color = null;
        while(color == null){
            color = currentPlayer.getChoosenColor();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO
            }
        }
        System.out.println("color: " + color);
        return color;
    }

    private void nextPlayer(){
        currentPlayerIndex += direction;
        if(currentPlayerIndex >= players.size())
            currentPlayerIndex = 0;
        else if(currentPlayerIndex < 0)
        currentPlayerIndex = players.size() - 1;
    }

    private void updateAllPlayersCards(){
        for(SecretPlayer secretPlayer : players)
            sendCurrentCards(secretPlayer);
    }

    private boolean isMoveLegal(SecretPlayer currentPlayer, int choosenCardIndex){
        if(choosenCardIndex == -1)
            return true;
        if(choosenCardIndex >= currentPlayer.cards.size() || choosenCardIndex < 0)
            return false;
        Card choosenCard = currentPlayer.cards.get(choosenCardIndex);
        Card table = cardsOnTable.peekLast();
        if(choosenCard.type == Card.Type.WILD){
            return true;
        }else if(choosenCard.type == Card.Type.WILD_DRAW_FOUR){
            // TODO check when legal - https://www.unorules.com/
            // for now always legal
            return true;
        }else if(table.color == choosenCard.color){
            return true;
        }else if(table.type == choosenCard.type){
            return true;
        }else if(currentWildColor == choosenCard.color){
            return true;
        }
        return false;
    }
    
    public void run() {
        while(true){
            SecretPlayer currentPlayer = players.get(currentPlayerIndex);
            int choosenCardIndex = chooseCard(currentPlayer);
            if(isMoveLegal(currentPlayer, choosenCardIndex)){
                if(choosenCardIndex == -1){
                    giveCardFromPile(currentPlayer);
                    updateAllPlayersCards();
                    nextPlayer();
                    continue;
                }
                Card choosenCard = currentPlayer.cards.get(choosenCardIndex);
                cardsOnTable.addLast(choosenCard);
                currentWildColor = null;
                currentPlayer.cards.remove(choosenCardIndex);
                switch(choosenCard.type){
                    case SKIP:
                        nextPlayer();
                        break;
                    case REVERSE:
                        direction *= -1;
                        break;
                    case ADD_TWO:
                        nextPlayer();
                        currentPlayer = players.get(currentPlayerIndex);
                        giveCardFromPile(currentPlayer);
                        giveCardFromPile(currentPlayer);
                        sendCurrentCards(currentPlayer);
                        break;
                    case WILD: {
                        Card.Color choosenColor = chooseColor(currentPlayer);
                        if(choosenColor == Card.Color.BLACK)
                            currentPlayer.sendIllegalMove();
                        else
                            currentWildColor = choosenColor;
                        break;
                    }
                    case WILD_DRAW_FOUR: { // TODO: others should be able to check; https://www.unorules.com/
                        Card.Color choosenColor = chooseColor(currentPlayer);
                        if(choosenColor == Card.Color.BLACK)
                            currentPlayer.sendIllegalMove();
                        else
                            currentWildColor = choosenColor;
                        nextPlayer();
                        currentPlayer = players.get(currentPlayerIndex);
                        giveCardFromPile(currentPlayer);
                        giveCardFromPile(currentPlayer);
                        giveCardFromPile(currentPlayer);
                        giveCardFromPile(currentPlayer);
                        sendCurrentCards(currentPlayer);
                        break;
                    }
                    default:
                }
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