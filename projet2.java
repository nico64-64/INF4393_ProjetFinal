import java.util.Iterator;
import java.util.NoSuchElementException;

public class CardHand {
    
    // 1. STRUCTURE EN NOEUD (Liste doublement chaînée)
    // Chaque carte connaît maintenant sa voisine de gauche (prev) et de droite (next)
    public static class Card {
        int rank;    
        String suit; 
        Card prev; // Lien vers la carte précédente
        Card next; // Lien vers la carte suivante

        public Card(int rank, String suit) {
            this.rank = rank;
            this.suit = suit;
        }

        @Override
        public String toString() {
            return "[" + suit + " " + rank + "]";
        }
    }

    // Sentinelles pour créer une liste doublement chaînée sécurisée
    private Card header;
    private Card trailer;
    private int size = 0;

    // 2. LES 4 FINGERS (Pointeurs directs vers les objets Card, pas des index !)
    private Card fingerHearts = null;
    private Card fingerClubs = null;
    private Card fingerSpades = null;
    private Card fingerDiamonds = null;

    // Constructeur de la main : on crée une liste vide avec ses deux barrières (sentinelles)
    public CardHand() {
        header = new Card(0, "header");
        trailer = new Card(0, "trailer");
        header.next = trailer;
        trailer.prev = header;
    }

    // 3. AJOUT EN TEMPS CONSTANT O(1)
    public void addCard(int r, String s) { 
        Card newCard = new Card(r, s);
        Card targetFinger = getFinger(s);

        if (targetFinger != null) {
            // S'il y a déjà cette couleur, on attache la carte juste après en O(1)
            addAfter(targetFinger, newCard);
        } else {
            // Sinon, on la met à la fin de la main (juste avant la sentinelle trailer) en O(1)
            addBefore(trailer, newCard);
        }
        
        // On met à jour le finger de cette couleur pour qu'il pointe sur notre nouvelle carte
        setFinger(s, newCard);
    }

    // 4. JOUER EN TEMPS CONSTANT O(1)
    public Card play(String s) { 
        if (size == 0) {
            return null; 
        }

        Card targetFinger = getFinger(s);
        Card cardToRemove;

        if (targetFinger != null) {
            // Si la couleur existe, on prend directement la carte pointée par le finger
            cardToRemove = targetFinger;
            // Le finger doit reculer d'une case (vers la gauche) pour pointer la carte précédente de même couleur
            if (cardToRemove.prev != header && cardToRemove.prev.suit.equals(s)) {
                setFinger(s, cardToRemove.prev);
            } else {
                setFinger(s, null); // Plus de carte de cette couleur
            }
        } else {
            // Si la couleur n'existe pas, on prend la toute première vraie carte (après le header)
            cardToRemove = header.next;
            // Si cette première carte était un finger pour une autre couleur, on doit ajuster ce finger
            if (getFinger(cardToRemove.suit) == cardToRemove) {
                setFinger(cardToRemove.suit, null);
            }
        }

        // On détache la carte de la chaîne en O(1)
        removeCard(cardToRemove);
        return cardToRemove; 
    }

    // FONCTIONS OUTILS POUR LE MAILLAGE (FLÈCHES) 

    // Insère le nœud 'newCard' juste après le nœud 'position' en O(1)
    private void addAfter(Card position, Card newCard) {
        newCard.prev = position;
        newCard.next = position.next;
        position.next.prev = newCard;
        position.next = newCard;
        size++;
    }

    // Insère le nœud 'newCard' juste avant le nœud 'position' en O(1)
    private void addBefore(Card position, Card newCard) {
        newCard.next = position;
        newCard.prev = position.prev;
        position.prev.next = newCard;
        position.prev = newCard;
        size++;
    }

    // Supprime un nœud de la chaîne en modifiant les flèches en O(1)
    private void removeCard(Card card) {
        card.prev.next = card.next;
        card.next.prev = card.prev;
        size--;
    }

    private Card getFinger(String s) {
        if (s.equals("hearts")) return fingerHearts; 
        if (s.equals("clubs")) return fingerClubs; 
        if (s.equals("spades")) return fingerSpades; 
        if (s.equals("diamonds")) return fingerDiamonds; 
        return null;
    }

    private void setFinger(String s, Card card) {
        if (s.equals("hearts")) fingerHearts = card; 
        if (s.equals("clubs")) fingerClubs = card; 
        if (s.equals("spades")) fingerSpades = card; 
        if (s.equals("diamonds")) fingerDiamonds = card; 
    }

    //  ITÉRATEURS 

    public Iterator<Card> iterator() { 
        return new Iterator<Card>() {
            private Card current = header.next;
            @Override
            public boolean hasNext() { return current != trailer; }
            @Override
            public Card next() {
                if (!hasNext()) throw new NoSuchElementException();
                Card c = current;
                current = current.next;
                return c;
            }
        };
    }

    public Iterator<Card> suitIterator(String s) { 
        return new Iterator<Card>() {
            private Card current = header.next;
            @Override
            public boolean hasNext() {
                while (current != trailer && !current.suit.equals(s)) {
                    current = current.next;
                }
                return current != trailer;
            }
            @Override
            public Card next() {
                if (!hasNext()) throw new NoSuchElementException();
                Card c = current;
                current = current.next;
                return c;
            }
        };
    }

    // ZONE DE TEST POUR VALIDER 
    public static void main(String[] args) {
        CardHand hand = new CardHand();
        
        System.out.println("--- TEST AJOUT (PROPRE ET EN O(1)) ---");
        hand.addCard(10, "hearts"); 
        hand.addCard(5, "hearts");  
        hand.addCard(11, "spades"); 
        hand.addCard(2, "clubs");   
        
        printHand(hand);
        
        System.out.println("\n--- TEST JOUER EN O(1) ---");
        System.out.println("Je joue un coeur : " + hand.play("hearts")); 
        printHand(hand);
    }

    private static void printHand(CardHand hand) {
        System.out.print("Ma main actuelle : [");
        Iterator<Card> it = hand.iterator();
        while (it.hasNext()) {
            System.out.print(it.next());
            if (it.hasNext()) System.out.print(", ");
        }
        System.out.println("]");
    }
}