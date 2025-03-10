package datagrabber;

public class FormatError extends Error{

    public static String contract;
    public static String declarer;
    public static String points;
    public static String boardNumber;
    public static boolean severelyUnexpected = false;
    public static boolean missingHandRecords = false;

    public static boolean pointEntryError = false;

    @Override
    public String toString() {
        String s = "FormatError\n";
        if (contract != null) {
            s = s + "Contract: " + contract + "\n";
        }
        if (declarer != null) {
            s = s + "declarer: " + declarer + "\n";
        }
        if (points != null) {
            s = s + "points: " + points + "\n";
        }
        if (boardNumber != null) {
            s = s + "board number: " + boardNumber + "\n";
        }
        return s;
    }


}
