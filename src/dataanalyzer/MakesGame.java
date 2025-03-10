package dataanalyzer;

import datagrabber.Card;
import structure.Contract;
import structure.Hand;

import java.util.HashMap;
import java.util.LinkedList;

public class MakesGame {

    public static void main(String[] args) {
        Loader loader = new Loader();
        MakesGame makesGame = new MakesGame();
        HashMap map;
        double totalHands = 0;
        double runningTotal = 0;
        while ((map = loader.getData()) != null) {
            Hand[] hands = (Hand[]) map.get(Loader.HAND_KEY);
            LinkedList<Contract> contracts = (LinkedList<Contract>) map.get(Loader.CONTRACT_KEY);

            int direction = makesGame.whichHasPoints(hands);
            if (direction != 0) {
                double average = makesGame.averageSuccessRate(makesGame.eightCardFits(direction, hands), contracts, direction);
                if (average != -1) {
                    totalHands++;
                    runningTotal += average;
                }
            }
        }
        System.out.println("TotalHands: " + totalHands);
        System.out.println("Average success rate: " + runningTotal/totalHands);
    }

    public int countPoints(Hand h) {
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

    /**
     * Decides which, if any, partnership has exactly 24 high card points
     * @param hands hands to be examined
     * @return {-1 = EW, 0 = none, 1 = NS}
     */
    public int whichHasPoints(Hand[] hands) {
        if (countPoints(hands[0]) + countPoints(hands[2]) == 24) {
            return 1;
        }
        if (countPoints(hands[1]) + countPoints(hands[3]) == 24) {
            return -1;
        }
        return 0;
    }

    public boolean[] eightCardFits(int direction, Hand[] hands) {
        Hand partner1;
        Hand partner2;
        if (direction == -1) {
            partner1 = hands[1];
            partner2 = hands[3];
        }
        else {
            partner1 = hands[0];
            partner2 = hands[2];
        }

        boolean[] bools = new boolean[4];
        for (int i = 0; i < 4; i++) {
            bools[i] = (partner1.cardsBySuit[i].size() + partner2.cardsBySuit[i].size() >= 8);
        }

        return bools;
    }

    public double averageSuccessRate(boolean[] suits, LinkedList<Contract> contracts, int direction) {
        double average = 0;
        double contractCount = 0;
        for (int i = 0; i < 4; i++) {
            if (suits[i]) {
                for (Contract contract : contracts) {
                    if (contract.suit == i && directionMatches(contract, direction)) {
                        contractCount++;
                        if (contract.tricksTaken >= 10) {
                            average ++;
                        }
                    }
                }
            }
        }
        if (contractCount > 0) {
            return average / contractCount;
        }
        else {
            return -1;
        }
    }

    private boolean directionMatches(Contract contract, int direction) {
        if (direction == -1) {
            return (contract.declarer == 1 || contract.declarer == 3);
        }
        else {
            return (contract.declarer == 0 || contract.declarer == 2);
        }
    }

}
