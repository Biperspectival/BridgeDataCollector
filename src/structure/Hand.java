package structure;

import datagrabber.Card;
import datagrabber.ConsoleColors;

import java.io.*;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * This object denotes a hand of 13 cards held by a single player; together the four players hold the entire 52 card deck.
 */
public class Hand {
    public LinkedList<Card>[] cardsBySuit; //sorts by {Spades, Hearts, Diamonds, Clubs}


    /**
     * builds an empty hand
     */
    public Hand() {
        cardsBySuit = new LinkedList[4];
        for (int i = 0; i < 4; i++) {
            cardsBySuit[i] = new LinkedList<>();
        }
    }

    /**
     * Builds a specified hand; used for bug-testing
     * @param cardsBySuit the cards held in the hand
     */
    public Hand(LinkedList<Card>[] cardsBySuit) {
        this.cardsBySuit = cardsBySuit;
    }

    /**
     * checks that the hand holds 13 cards
     * @return true if the hand holds 13 cards
     */
    public boolean isValid() {
        int sum = 0;
        for (LinkedList<Card> c : cardsBySuit) {
            sum += c.size();
        }
        return sum == 13;
    }

    /**
     * Checks that a collection of hands holds all 52 cards.
     * @param hands a collection of 4 hands to be checked
     * @return true if all 52 cards are evenly distributed amongst the 4 hands
     */
    public static boolean verifyAll(Hand[] hands) {
        if (hands.length != 4) {//checks that there are four hands
            return false;
        }
        for (Hand h : hands) { //checks that each hand holds 13 cards
            if (!h.isValid()) {
                return false;
            }
        }
        HashSet<Card> suit = new HashSet<>(); //a HashSet is used so that duplicate cards are discarded
        for (int i = 0; i < 4; i++) {
            for (Hand hand : hands) {
                suit.addAll(hand.cardsBySuit[i]);
            }
            if (suit.size() != 13) {//after adding all hands' cards, there should be all 13 cards in the set
                return false;
            }
            suit.clear();
        }
        return true;
    }

    public static boolean cardsNotFoundUnexpectedError = false;
    public static boolean unknownCardError = false;
    public static boolean knownWildcardUsed = false;

    /**
     * creates an array of {N,E,S,W} Hands from a raw handRecord
     * @param handRecord raw hand record
     * @return processed hands
     */
    public static Hand[] getHandsFromHTMLSegment(String handRecord) {
        String[] substrings = handRecord.split(",");
        Hand[] hands = new Hand[4];
        String[] directions = {"north", "east", "south", "west"};
        String[] suits = {"spades", "hearts", "diamonds", "clubs"};
        for (int i = 0; i < 4; i++) {
            hands[i] = new Hand();
            for (int j = 0; j < 4; j++) {
                String cards = findCards(substrings, directions[i], suits[j]);
                if (cards == null) {// note that if a hand has, for example, no Spades, it should return an empty string, not null.
                    cardsNotFoundUnexpectedError = true;
                    System.out.println(ConsoleColors.BLUE + "Could not find cards for " + directions[i] + " and suit " + suits[j]);
                    System.out.println(ConsoleColors.BLUE + "Hand records: " + handRecord + ConsoleColors.RESET);
                    System.out.println(ConsoleColors.BLUE + "Hands, getHandsFromHTMLSegment, #1");
                    return null;
                }
                char[] arr = cards.toCharArray();
                for (char c : arr) {
                    Card card = Card.stringToCard(c);
                    if (card != null) {
                        hands[i].cardsBySuit[j].add(Card.stringToCard(c));
                    }
                    else {
                        int isError = Card.checkCardForError(c);//this can return 1 if it is a wildcard used for an empty suit
                        if (isError == -1) {//checks for unknown character errors
                            System.out.println(ConsoleColors.BLUE + "There was an error with an unknown character: " + c);
                            System.out.println("Hand records: " + handRecord);
                            System.out.println(ConsoleColors.BLUE + "Hands, getHandsFromHTMLSegment, #2");
                            unknownCardError = true;
                            return null;
                        }
                        if (isError == 0) {//handles known wildcard errors
                            knownWildcardUsed = true;
                            return null;
                        }
                    }
                }
            }
        }

        if (verifyAll(hands)) {//confirms all hands and returns them
            return hands;
        }
        else {//throws an error and skips the deal
            System.out.println(ConsoleColors.RED + "Missing card detected! Please check.");
            System.out.println("HandRecords: " + handRecord);
            return null;
        }
    }

    /**
     * Parses a raw handRecord into a String to be written to a batch
     * @param handRecord raw String
     * @return cleaned Hand data
     */
    public static String getHandDataFromHTMLSegment(String handRecord){
        Hand[] hands = getHandsFromHTMLSegment(handRecord);
        String hand = "";
        if (hands == null) {
            return null;
        }
        for(int i = 0; i < 4; i++) {
            hand = hand + formatHand(hands[i]) + ";";
        }
        return hand;
    }

    /**
     * Formats a hand for writing to a file
     * @param h hand to format
     * @return clean string
     */
    public static String formatHand(Hand h){
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            for (Card c : h.cardsBySuit[i]) {
                s.append(Card.cardToString(c));
            }
            s.append(",");
        }
        return s.toString();
    }

    /**
     * finds the raw Card String for a specific direction and suit
     * @param substrings all comma-separated sequences in the file
     * @param direction direction (NESW) to search for
     * @param suit suit to search for
     * @return cleaned cards, or null if no direction/suit pair exists
     */
    public static String findCards(String[] substrings, String direction, String suit) {
        for (String s : substrings) {
            if (s.contains(direction) && s.contains(suit)) {
                String desired = s.split(":")[1];
                String[] split = desired.split("\"");
                if (split.length == 0) {
                    return "";
                }
                desired = desired.split("\"")[1];
                return desired;
            }
        }
        return null;
    }



}
