package dataanalyzer;

import structure.Contract;
import structure.Hand;

import java.util.*;

public class SuitFitAnalyzer implements DataAnalyzer{

    private int totalMatch;
    private int longerSuit;

    public static void main(String[] args) {
        LinkedList<BinByPoints> bins = new LinkedList<>();
        HCPCounter counter = new HCPCounter();
        bins.add(new BinByPoints(counter, new SuitFitAnalyzer(8, 4)));
        bins.add(new BinByPoints(counter, new SuitFitAnalyzer(8, 5)));
        bins.add(new BinByPoints(counter, new SuitFitAnalyzer(8, 6)));
        bins.add(new BinByPoints(counter, new SuitFitAnalyzer(8, 7)));
        bins.add(new BinByPoints(counter, new SuitFitAnalyzer(8, 8)));

        BinByPoints.analyzeAll(bins);
        printAnalysisResults(bins);
    }

    public SuitFitAnalyzer(int totalMatch, int longerSuit) {
        this.totalMatch = totalMatch;
        this.longerSuit = longerSuit;
    }

    private static double round(double d) {
        d = d * 1000;
        int i = (int) d;
        d = i;
        return d/1000;
    }


    @Override
    public double analyze(Hand[] hands, LinkedList<Contract> contracts, boolean isNS) {
        boolean[] arr = hasFit(hands, isNS);
        double totalTricks = 0;
        double totalContracts = 0;

        for (Contract c : contracts) {
            boolean suitWorks;
            if (0 <= c.suit && c.suit < 4) {
                suitWorks = arr[c.suit];
            }
            else {
                suitWorks = false;
            }
            if (suitWorks) {
                boolean declarerWorks;
                if (isNS) {
                    declarerWorks = (c.declarer == 0 || c.declarer == 2);
                }
                else {
                    declarerWorks = (c.declarer == 1 || c.declarer == 2);
                }
                if (declarerWorks) {
                    totalTricks += c.tricksTaken;
                    totalContracts++;
                }
            }
        }
        if (totalContracts > 0) {
            return totalTricks/totalContracts;
        }
        else {
            return -1001;
        }
    }

    private boolean[] hasFit(Hand[] hands, boolean isNS) {
        Hand hand1;
        Hand hand2;
        if (isNS) {
            hand1 = hands[0];
            hand2 = hands[2];
        }
        else {
            hand1 = hands[1];
            hand2 = hands[3];
        }

        boolean[] arr = new boolean[4];
        for (int i = 0; i < 4; i++) {
            arr[i] = (hand1.cardsBySuit[i].size() + hand2.cardsBySuit[i].size() == totalMatch) && (hand1.cardsBySuit[i].size() == longerSuit || hand2.cardsBySuit[i].size() == longerSuit);
        }
        return arr;
    }

    public static final int justifyLength = 15;

    public static String justifyString(String s) {
        while (s.length() < justifyLength) {
            s += " ";
        }
        return s;
    }

    public static void printAnalysisResults(LinkedList<BinByPoints> bins) {
        HashSet<Double> allKeys = new HashSet<>();
        for (BinByPoints bin : bins) {
            allKeys.addAll(bin.getAnalysisByPoints().keySet());
        }
        ArrayList<Double> keyList = new ArrayList<>(allKeys.size());
        keyList.addAll(allKeys);
        Collections.sort(keyList);

        for (double d : keyList) {
            System.out.print(justifyString("points: " + d) + " | ");
            for (BinByPoints bin : bins) {
                String toPrint;
                if (bin.getAnalysisByPoints().containsKey(d)) {
                    double ave = round(bin.getAnalysisByPoints().get(d)/bin.getNumberAnalyzed().get(d));
                    toPrint = "" + ave;
                }
                else {
                    toPrint = "";
                }
                toPrint = justifyString(toPrint);
                System.out.print(toPrint + "| ");
            }
            System.out.println();
        }

    }

}
