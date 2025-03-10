package dataanalyzer;

import structure.Contract;
import structure.Hand;

import java.util.LinkedList;

public interface DataAnalyzer {

    double analyze(Hand[] hands, LinkedList<Contract> contracts, boolean isNS);

}
