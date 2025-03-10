package dataanalyzer;

import datagrabber.Card;
import structure.Hand;

import java.util.LinkedList;

public class HCPCounter implements CountPoints{


    @Override
    public double countPoints(Hand h) {
        int total = 0;
        for (LinkedList<Card> cards : h.cardsBySuit) {
            for (Card c : cards) {
                if (c == Card.A) {
                    total = total + 4;
                }
                if (c == Card.K) {
                    total = total + 3;
                }
                if (c == Card.Q) {
                    total = total + 2;
                }
                if (c == Card.J) {
                    total = total + 1;
                }
            }
        }

        return total;
    }
}
