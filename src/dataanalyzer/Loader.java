package dataanalyzer;

import datagrabber.Card;
import datagrabber.ConsoleColors;
import datagrabber.ProcessWebPage;
import structure.Contract;
import structure.Hand;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;

public class Loader {






    private int batchNumber;
    private BufferedReader handReader;
    private BufferedReader contractReader;
    public static final String HAND_KEY = "HAND_KEY";
    public static final String CONTRACT_KEY = "CONTRACT_KEY";
    public static final int TOTAL_BATCHES = 1308;

    public Loader() {
        batchNumber = 1;
        getReaders();
    }

    private void getReaders() {
        String folderPath = ProcessWebPage.HAND_FOLDER_PATH + "Batch" + batchNumber + "/";
        try {
            handReader = new BufferedReader(new FileReader(new File(folderPath + ProcessWebPage.HAND_FILE_NAME)));
            contractReader = new BufferedReader(new FileReader(new File(folderPath + ProcessWebPage.BOARD_RESULTS_FILE_NAME)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean prepareNextLine() {
        try {
            if (handReader.ready() && contractReader.ready()) {
                return true;
            } else if (batchNumber < 1308) {
                batchNumber++;
                System.out.println("beginning batch " + batchNumber);
                contractReader.close();
                handReader.close();
                getReaders();
                return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public HashMap getData() {
        try {
            if (prepareNextLine()) {
                HashMap map = new HashMap();
                map.put(HAND_KEY, getHandsFromLine(handReader.readLine()));
                map.put(CONTRACT_KEY, getContractsFromLine(contractReader.readLine()));
                return map;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private Hand[] getHandsFromLine(String handLine) {
        Hand[] hands = new Hand[4];
        String arr[] = handLine.split(";");
        for (int i = 0; i < 4; i++) {
            hands[i] = getHandFromBatchFile(arr[i]);
        }
        return hands;
    }


    private Hand getHandFromBatchFile(String handSegment) {
        handSegment = "X" + handSegment + "X";
        Hand hand = new Hand();


        String[] sections = handSegment.split(",");
        for (int i = 0; i < 4; i++) {

            hand.cardsBySuit[i] = getSuitFromSegment(sections[i]);

        }
        return hand;

    }

    private LinkedList<Card> getSuitFromSegment(String suitSegment) {
        LinkedList<Card> cards = new LinkedList<>();
        for (char c : suitSegment.toCharArray()) {
            Card card = Card.stringToCard(c);
            if (card != null) {
                cards.add(Card.stringToCard(c));
            }
        }
        return cards;
    }

    private LinkedList<Contract> getContractsFromLine(String contractLine) {
        LinkedList<Contract> contracts = new LinkedList<>();
        String[] arr = contractLine.split(";");
        for (String s : arr) {
            contracts.add(getContractFromSegment(s));
        }
        return contracts;
    }

    public static int counter = 0;

    private Contract getContractFromSegment(String contractSegment) {
        counter++;
        String[] arr = contractSegment.split(",");
        Contract contract = new Contract();

        if (arr[0].equals("pass") || arr[0].equals("pa")) {
            contract.numberBid = 0;
            contract.suit = -2;
            contract.wasPassed = true;
            contract.tricksTaken = 0;
        }
        else {
            contract.numberBid = Integer.parseInt(arr[0]);
            contract.suit = getSuitNumberFromString(arr[1]);
            contract.tricksTaken = Integer.parseInt(arr[4]);
        }

        contract.doubledDegree = getDoublingDegree(arr[2]);

        contract.declarer = getDeclarer(arr[3]);

        return contract;
    }

    private int getDeclarer(String declarer) {
        if (declarer.equals("N")) {
            return 0;
        }
        if (declarer.equals("E")) {
            return 1;
        }
        if (declarer.equals("S")) {
            return 2;
        }
        if (declarer.equals("W")) {
            return 3;
        }
        return -1; //passed out
    }

    private int getDoublingDegree(String doubled) {
        if (doubled.equals("xx")) {
           return 2;
        }
        else if (doubled.equals("x")) {
            return 1;
        }
        else {
            return 0;
        }
    }



    private int getSuitNumberFromString(String suit) {
        if (suit.equals("pass")) {
            return -2;
        }
        else if (suit.equals("nt")) {
            return -1;
        }
        else if (suit.equals("s")) {
            return 0;
        }
        else if (suit.equals("h")) {
            return 1;
        }
        else if (suit.equals("d")) {
            return 2;
        }
        else if (suit.equals("c")) {
            return 3;
        }
        throw new RuntimeException();
    }

}
