package dataanalyzer;

import structure.Contract;
import structure.Hand;

import java.util.HashMap;
import java.util.LinkedList;

public class BinByPoints {

    private HashMap<Double, Double> analysisByPoints;
    private HashMap<Double, Integer> numberAnalyzed;

    private CountPoints countPoints;
    private DataAnalyzer dataAnalyzer;

    public BinByPoints(CountPoints countPoints, DataAnalyzer dataAnalyzer) {
        this.countPoints = countPoints;
        analysisByPoints = new HashMap<>();
        numberAnalyzed = new HashMap<>();
        this.dataAnalyzer = dataAnalyzer;
    }

    private void acceptData(Hand[] hands, LinkedList<Contract> contracts) {
        double pointsNS = countPoints.countPoints(hands[0]) + countPoints.countPoints(hands[2]);
        double poinstEW = countPoints.countPoints(hands[1]) + countPoints.countPoints(hands[2]);

        analyze(pointsNS, dataAnalyzer.analyze(hands, contracts, true));
        analyze(poinstEW, dataAnalyzer.analyze(hands, contracts, false));
    }

    private void analyze(double pointsDirectional, double analysis) {
        if (analysis != -1001) {
            if (analysisByPoints.keySet().contains(pointsDirectional)) {
                analysisByPoints.put(pointsDirectional, analysisByPoints.get(pointsDirectional) + analysis);
                numberAnalyzed.put(pointsDirectional, numberAnalyzed.get(pointsDirectional) + 1);
            }
            else {
                analysisByPoints.put(pointsDirectional, analysis);
                numberAnalyzed.put(pointsDirectional, 1);
            }
        }
    }

    public static void analyzeAll(LinkedList<BinByPoints> bins) {
        Loader loader = new Loader();
        HashMap map;
        while ((map = loader.getData()) != null) {
            Hand[] hands = (Hand[]) map.get(Loader.HAND_KEY);
            LinkedList<Contract> contracts = (LinkedList<Contract>) map.get(Loader.CONTRACT_KEY);
            for (BinByPoints bin : bins) {
                bin.acceptData(hands, contracts);
            }
        }
    }

    public HashMap<Double, Double> getAnalysisByPoints() {
        return analysisByPoints;
    }

    public HashMap<Double, Integer> getNumberAnalyzed() {
        return numberAnalyzed;
    }


}
