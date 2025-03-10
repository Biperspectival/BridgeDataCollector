package datagrabber;

import java.util.HashMap;
import java.util.LinkedList;

public class Bracketeer {

    public static final String RESULT = "RESULT";
    public static final String SKIPS = "SKIPS";

    /**
     * Gets one sequence of characters starting with plus and ending with minus (using bracket level(s)), does not cause errors
     * @param s String to be analyzed
     * @param plus starting character of sequence
     * @param minus ending character of sequence
     * @return Two pairs: (RESULT, String sequence) and (SKIPS, int numberOfCharactersBeforeStartOfSequence)
     */
    public static HashMap getInBrackets(String s, char plus, char minus) {
        HashMap map = new HashMap();
        int skips = 0;
        char[] arr = s.toCharArray();
        StringBuilder toReturn = new StringBuilder();
        int count = 0;
        boolean end = false;
        boolean bootup = true;
        for (int i = 0; i < arr.length && !end; i++) {
            if (arr[i] == plus) {// begins sequence if first plus character, otherwise goes into a deeper level
                bootup = false;
                count++;
            }
            if (arr[i] == minus && !bootup) {// steps up a level
                count--;
            }
            if (bootup) { //counts starting places before beginning of sequence
                skips++;
            }
            else { //if not in bootup, this adds the character to the results string
                toReturn.append(arr[i]);
            }
            if (!bootup && count == 0) {
                end = true;
            }
        }
        map.put(SKIPS, skips);
        map.put(RESULT, toReturn.toString());
        return map;
    }

    /**
     * Divides string into it's portions enclosed by char=plus char=minus pairs, does not cause errors
     * @param toBreak String to be divided
     * @param plus starting character of a term
     * @param minus ending character of a term
     * @return ordered collection of broken pieces
     */
    public static LinkedList<String> getBracketedBreakdown(String toBreak, char plus, char minus) {
        LinkedList<String> list = new LinkedList<>();
        while (toBreak.contains("" + plus)) {
            HashMap map = Bracketeer.getInBrackets(toBreak, plus, minus);
            String remainder = (String)map.get(Bracketeer.RESULT);
            int skip = (int)map.get(SKIPS);
            toBreak = toBreak.substring(skip + remainder.length());
            list.add(remainder);
        }
        return list;
    }

}
