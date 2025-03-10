package datagrabber;

import structure.Hand;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class ProcessWebPage {



    public static final String AVERAGE_RESULT = "AVE_ERROR";
    public static final String ENTRY_ERROR = "ENTRY_ERROR";
    public static final String WEIRD_F_ERROR = "WEIRD_F_ERROR";
    public static final String HAND_FILE_NAME = "hand_file.txt";
    public static final String POINT_COUNT_ERROR = "POINT_COUNT_ERROR";
    public static final String BOARD_RESULTS_FILE_NAME = "board_results.txt";
    public static final String REF_FILE_NAME = "ref.txt";
    public static final String ALL_BOARDS_CORRUPTED = "ALL_BOARDS_CORRUPTED";
    public static final String BOARD_RESULTS_DO_NOT_EXIST = "BOARD_RESULTS_DO_NOT_EXIST";
    public static final String NORMAL_BOARD_RESULTS_DO_NOT_EXIST = "NORMAL_BOARD_RESULTS_DO_NOT_EXIST";
    public static final String HAND_FOLDER_PATH = "D:/Bridge/Hands/";
    public static final String BATCH_FOLDER_NAME = "Batch";
    public static final String FORMAT_ERROR_OCCURRED_DURING_POINT_COUNTS = "ERROR_OCCURRED_DURING_POINT_COUNTS";

    public static final String STRING_SPLIT_ERROR = "STRING_SPLIT_ERROR";
    public static final String SEVERE_ERROR_OCCURRED = "SEVERE_ERROR_OCCURRED";
    public static final String MISSING_CONTRACT_ERROR = "MISSING_CONTRACT_ERROR";
    public static final int TOTAL_GAMES = 934000;
    public static final int BATCH_SIZE = 10000;


    public static boolean testingMode = false;
    public static final int GAME_TO_TEST = 713603;

    /**
     * This execution is for pulling the html files from local storage and parsing them into usable CSV files.
     * @param args
     */
    public static void main(String[] args){

        int[] arr = loadState();//loads the state from the previous execution. On a slower computer, it is not practical to run this continuously
        int batchNumber = arr[0];

        int gameNumber;
        if (testingMode) {//testing mode is for bug-testing unexpected errors for specific games
            System.out.println(ConsoleColors.YELLOW + "Entering Testing Mode");
            gameNumber = GAME_TO_TEST;

        } else {
            gameNumber = arr[1];
        }

        //total hands processed and total errors found are used to estimate how clean the initial data is. There can be
        //data-entry errors that will not appear as erroneous data, so this is intended to serve as a proxy for the
        //errors that cannot be caught.
        int totalHands = arr[2];
        int totalErrors = arr[3];

        while (gameNumber < TOTAL_GAMES && gameNumber != -2) {//-2 is an escape value for a fatal error, or an error that otherwise should result in stopped processing for human supervision
            int[] output = processHTMLBatch(gameNumber, batchNumber);
            gameNumber = output[0];
            totalHands += output[1];
            totalErrors += dataEntryErrors;
            dataEntryErrors = 0;
            batchNumber++;
            if (gameNumber != -2 && !testingMode) {//saves the state in case something crashes
                storeState(batchNumber, gameNumber, totalHands, totalErrors);
            }
            if (gameNumber != -2 && testingMode) {
                gameNumber = -2;
                System.out.println(ConsoleColors.YELLOW + "Please exit testing mode and begin a normal batching.");
            }
        }

    }

    public static final String STATE_PATH = "D:/Bridge/state.txt";

    /**
     * stores where the reader should resume
     * @param batchNumber next batch to write
     * @param gameNumber next game to read
     * @return false if an error occurs;
     */
    public static void storeState(int batchNumber, int gameNumber, int totalHands, int totalErrors) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(STATE_PATH)));
            writer.write("" + batchNumber);
            writer.newLine();
            writer.write("" + gameNumber);
            writer.newLine();
            writer.write("" + totalHands);
            writer.newLine();
            writer.write("" + totalErrors);

            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * loads where the reader last left off
     * @return {batchNumber, gameNumber, totalHands, totalErrors}
     */
    public static int[] loadState() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(STATE_PATH)));
            int batchNumber = Integer.parseInt(reader.readLine());
            int gameNumber = Integer.parseInt(reader.readLine());
            int totalHands = Integer.parseInt(reader.readLine());
            int totalErrors = Integer.parseInt(reader.readLine());
            return new int[]{batchNumber, gameNumber, totalHands, totalErrors};
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int dataEntryErrors = 0;

    /**
     * Batches 10000 game files into a collection of 3 files
     * @param gameNumber starting file to read
     * @param handBatchNumber which batch to write to
     * @return returns game that should be read by next batch, or -2 if process should stop.
     */
    public static int[] processHTMLBatch(int gameNumber, int handBatchNumber){
        String batchFolder = HAND_FOLDER_PATH + BATCH_FOLDER_NAME + handBatchNumber;
        File f = new File(batchFolder);
        int startNumber = gameNumber;
        f.mkdir();
        try {
            BufferedWriter handWriter = new BufferedWriter(new FileWriter(new File(batchFolder + "/" + HAND_FILE_NAME)));
            BufferedWriter contractWriter = new BufferedWriter(new FileWriter(new File(batchFolder + "/" + BOARD_RESULTS_FILE_NAME)));
            BufferedWriter refWriter = new BufferedWriter(new FileWriter(new File(batchFolder + "/" + REF_FILE_NAME)));

            int count = 0;
            while (count < BATCH_SIZE && gameNumber < TOTAL_GAMES) {

                int increment = processHTMLFile(gameNumber, handWriter, contractWriter, refWriter);
                if (increment >= 0) {
                    count += increment;
                }
                if (increment == -2) {
                    printBatchReport(startNumber, gameNumber, count);
                    return new int[]{-2, 0};
                }

                gameNumber++;
                if (gameNumber % 100 == 0) {
                    System.out.println(ConsoleColors.GREEN + "Processed game " + gameNumber);
                }

            }
            handWriter.flush();
            handWriter.close();
            contractWriter.flush();
            contractWriter.close();
            refWriter.flush();
            refWriter.close();
            printBatchReport(startNumber, gameNumber, count);

            /*
            if (count/(gameNumber - startNumber) < 6) {

                System.out.println(ConsoleColors.BLUE + "The rate of successful extraction has dropped too low! Please check the data!");
                return new int[]{-2, 0};

            }
            */



            return new int[]{gameNumber, count};
        } catch (IOException e) {
            System.out.println(ConsoleColors.BLUE + "An unexpected error occurred when creating the fileWriters at game" + gameNumber );
            System.out.println("ProcesseWebPage, processHTMLBatch" + ConsoleColors.RESET);
            return new int[]{-2, 0};
        }
    }

    /**
     * prints an update to the console; useful for tracking if there is a high rate of data errors
     */
    public static void printBatchReport(int startNumber, int gameNumber, int handCount) {
        System.out.println(ConsoleColors.PURPLE + "Processed " + (gameNumber - startNumber) + " games for a total of " + handCount + " hands at an average of " + (handCount/(gameNumber - startNumber)) + " hands per game.");
    }

    /**
     * processes a single HTML file for a game
     * @param gameNumber number for the file being read
     * @param handWriter writes the hand file
     * @param contractWriter writes the contract file
     * @param refWriter writes the ref file
     * @return If positive, it's the number of boards written to the hand, contract, and ref files. If -1, an unfixable error occurred. If -2, the process should stop.
     */
    public static int processHTMLFile(int gameNumber, BufferedWriter handWriter, BufferedWriter contractWriter, BufferedWriter refWriter){
        //reads the HTML file
        String path = "D:/Bridge/HTMLData/data" + gameNumber + ".txt";
        File file = new File(path);
        if (!file.exists()) {
            return -1;
        }
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while (reader.ready()) {
                stringBuilder.append(reader.readLine() + "\n");
            }
            reader.close();

            //passes the HTML file to page processor
            return processPage(stringBuilder.toString(), gameNumber, handWriter, contractWriter, refWriter);
        } catch (IOException e) {
            System.out.println(ConsoleColors.BLUE + "An unexpected exception has been thrown at gameFile " + gameNumber + ConsoleColors.RESET);
            System.out.println(ConsoleColors.BLUE + "ProcessWebPage, Process HTMLFile");
            return -2;
        }

    }


    /**
     * Processes the string of an HTML game file and writes it to a batch, may throw an IOException, but this is okay, because it should not happen normally.
     * @param text HTML file
     * @param gameNumber number of the game source corresponding to the HTML file
     * @param handWriter output to hand file batch
     * @param contractWriter output to contract file batch
     * @param refWriter output to reference batch
     * @return returns number of successful boards, OR -1 if the data was bad, OR -2 if an unexpected error occurred
     */
    public static int processPage(String text, int gameNumber, BufferedWriter handWriter, BufferedWriter contractWriter, BufferedWriter refWriter) throws IOException {

        LinkedList<String> hands = getHands(text);
        if (hands == null) {
            if (checkIfItBounces) {
                System.out.println(ConsoleColors.BLUE + "Game number: " + gameNumber);
                checkIfItBounces = false;
            }
            if (getHandsSevereError) {
                System.out.println(ConsoleColors.BLUE + "The severe error occurred at game " + gameNumber);
                return -2;
            }
            else {
                return -1;
            }
        }

        int numberOfSections = getNumberOfSections(text);
        if (numberOfSections == -2) {
            System.out.println(ConsoleColors.WHITE + "There is no section numbering in " + gameNumber);
        }
        LinkedList<String> boards = getBoards(text, numberOfSections);

        HashMap<Integer, String> boardsByNumber = new HashMap<>();
        HashMap<Integer, String> handsByNumber = new HashMap<>();

        numberBoards(boards, boardsByNumber);
        numberHands(hands, handsByNumber);

        HashSet<Integer> index = new HashSet<>();
        for (int i : handsByNumber.keySet()) {
            if (boardsByNumber.containsKey(i)) {
                index.add(i);
            }
        }

        int successfulBoards = 0;

        for (Integer i : index) {
            String handResults = Hand.getHandDataFromHTMLSegment(handsByNumber.get(i));
            if (handResults != null) {
                String boardResults = getBoardResultDataFromHTMLSegment(boardsByNumber.get(i), i);

                if (boardResults != null) {
                    if (needsGameNumber) {
                        System.out.println("Game number: " + gameNumber);
                        needsGameNumber = false;
                    }
                    if (boardResults.equals(SEVERE_ERROR_OCCURRED)) {
                        return -2;
                    }
                    if (boardResults.equals(ALL_BOARDS_CORRUPTED)) {
                        return -1;
                    }


                    handWriter.write(handResults);
                    handWriter.newLine();

                    contractWriter.write(boardResults);
                    contractWriter.newLine();

                    refWriter.write("" + gameNumber + "," + i);
                    refWriter.newLine();

                    successfulBoards++;


                }
            }
            else {
                if (Hand.unknownCardError || Hand.cardsNotFoundUnexpectedError) {
                    System.out.println("Error occurred at game " + gameNumber + " and board " + i);
                    return -2;
                }
                if (teamGame) {
                    teamGame = false;
                    return -1;
                }
                if (checkIfItBounces) {
                    System.out.println(ConsoleColors.BLUE + " Check GameNumber: " + gameNumber);
                    return -1;
                }
            }

        }

        return successfulBoards;
    }

    private static int getNumberOfSections(String HTMLText) {
        String[] split = HTMLText.split("\"number_of_sections\":");
        if (split.length < 2) {
            return -2;
        }
        String text = split[1].split(",")[0];
        return Integer.parseInt(text);
    }

    private static boolean needsGameNumber = false;
    /**
     * Cleans a raw board HTML String into a writable version
     * @param segment raw board HTML String
     * @param boardNumber board number for trick counting
     * @return cleaned data
     */
    private static String getBoardResultDataFromHTMLSegment(String segment, int boardNumber) {
        HashMap map;
        map = Bracketeer.getInBrackets(segment.split("\"board_results\":")[1], '[', ']');
        String resultsText = (String)map.get(Bracketeer.RESULT);
        LinkedList<String> results = Bracketeer.getBracketedBreakdown(resultsText, '{', '}');

        String toReturn = "";
        boolean hasSuccess = false;

        for (String result : results) {
            result = cleanBoardResultForPrinting(result, boardNumber);
            int errorType = getErrorType(result);
            if (errorType == 2) {
                hasSuccess = true;
                toReturn = toReturn + result + ";";
            }
            else if (errorType == 1) {
                needsGameNumber = true;
            }
            else if (errorType == -1) {
                needsGameNumber = true;
                return SEVERE_ERROR_OCCURRED;
            }
            else if (errorType == -2) {
                needsGameNumber = true;
                return ALL_BOARDS_CORRUPTED;
            }
            else if (errorType == -3) {
                return ALL_BOARDS_CORRUPTED;
            }
        }

        if (hasSuccess) {
            return toReturn;
        }
        else {
            return null;
        }

    }

    public static boolean getHandsSevereError = false;
    public static boolean teamGame = false;
    public static boolean checkIfItBounces = false;


    /**
     * Receives HTML text to extract raw hands
     * @param text HTML text
     * @return raw hand files, null if an error occurred, see getHandsSevereError
     */
    public static LinkedList<String> getHands(String text) {
        String[] breaks = text.split("\"hand_records\":");
        if (breaks.length < 2) {
            if (text.contains("\"teamMatches\"")) {//It is a team game, not a pairs game.
                teamGame = true;
                return null;
            }
            if (text.contains("NotFound...") && text.contains("Thegameyouaretryingtoaccesshasbeendeleted")) {
                return null;
            }
            if (text.contains("<script>alert('Youmustbeloggedintoviewthisresult.');window.location.href='/login';</script>")) {
                return null;
            }
            if (text.contains("<title>ClubResults</title>")) {
                System.out.println(ConsoleColors.BLUE + " Club Results Not Found, returns to main page! ");
                checkIfItBounces = true;
                return null;
            }
            System.out.println(ConsoleColors.BLUE + "\"hand_records\" not found in file" + ConsoleColors.RESET);
            System.out.println(ConsoleColors.BLUE + "ProcessWebPage, getHands, spot 1");
            getHandsSevereError = true;
            return null;
        }

        String current = breaks[1].split("]")[0];
        if (current.length() < 10) {
            if (!current.equals("[")) {// a normal empty hand produces just the character '['
                System.out.println(ConsoleColors.RED + "hand_records empty: " + current);
                System.out.println(ConsoleColors.RED + "ProcessWebPage, getHands, spot 2");
            }
            return null;
        }


        LinkedList<String> hands = Bracketeer.getBracketedBreakdown(current, '{', '}');
        return hands;
    }

    /**
     * Pulls raw board data from HTML text. This does not throw errors
     * @param text HTML text
     * @return list of raw boards
     */
    public static LinkedList<String> getBoards(String text, int numberOfSections) {
        String[] arr = text.split("\"boards\":");
        LinkedList<String> boards = new LinkedList<>();
        for (int i = 1; i <= numberOfSections; i++) {
            HashMap map = Bracketeer.getInBrackets(arr[i], '[', ']');
            String boardText = (String) map.get(Bracketeer.RESULT);
            boards.addAll(Bracketeer.getBracketedBreakdown(boardText, '{', '}'));
        }
        return boards;
    }

    /**
     * pulls the board number from each board. This should not throw an error
     * @param boards raw board strings
     * @param map where to place numbered boards
     */
    public static void numberBoards(LinkedList<String> boards, HashMap<Integer, String > map) {
        for (String s : boards) {
            if (s.split("\"board_number\":").length >= 2) {
                String remainder = s.split("\"board_number\":")[1];
                remainder = remainder.split(",")[0];
                int number = Integer.parseInt(remainder);
                map.put(number, s);
            }
        }
    }

    /**
     * Places hands into map using their board numbers
     * @param hands raw hand strings
     * @param map destination
     */
    public static void numberHands(LinkedList<String> hands, HashMap<Integer, String> map) {
        for (String s : hands) {
            String remainder = s.split("\"board\":")[1];
            remainder = remainder.split(",")[0];
            int number = Integer.parseInt(remainder);
            map.put(number, s);
        }
    }

    /**
     * Checks if a String is an Error report
     * @param result String to check
     * @return {-3, stop reading boards; -2 stop reading boards and give game number, -1 = Severe error, stop; 0 = normal error continue, 1 = error with report continue; 2 = no error}
     */
    private static int getErrorType(String result) {
        if (result.equals(AVERAGE_RESULT)) {
            return 0;
        }
        if (result.equals(ENTRY_ERROR)) {
            return -1;
        }
        if (result.equals(WEIRD_F_ERROR)) {
            return 0;
        }
        if (result.equals(POINT_COUNT_ERROR)) {
            return 1;
        }
        if (result.equals(FORMAT_ERROR_OCCURRED_DURING_POINT_COUNTS)) {
            return -1;
        }
        if (result.equals(STRING_SPLIT_ERROR)) {
            return -1;
        }
        if (result.equals(BOARD_RESULTS_DO_NOT_EXIST)) {
            return -2;
        }
        if (result.equals(NORMAL_BOARD_RESULTS_DO_NOT_EXIST)) {
            return -3;
        }
        if (result.equals(MISSING_CONTRACT_ERROR)) {
            return 0;
        }
        return 2;
    }



    private static boolean hasFError(String pointsNS) {
        if (pointsNS.equalsIgnoreCase("pass")) {
            return false;
        }
        char lastChar = pointsNS.toCharArray()[pointsNS.length() - 1];
        if (lastChar >= 'A' && lastChar <= 'Z') {
            return true;
        }
        return false;
    }

    /**
     * takes a raw board result from HTML and prepares it for printing to a batch
     * @param resultsText raw String
     * @param boardNumber board number
     * @return cleaned data
     */
    private static String cleanBoardResultForPrinting(String resultsText, int boardNumber){
        String contract = resultsText.split("\"contract\":")[1];
        contract = contract.split("\"")[1];
        String declarer = resultsText.split("\"declarer\":")[1];
        declarer = declarer.split("\"")[1];
        String pointsNS = resultsText.split("\"ns_score\":")[1];
        pointsNS = pointsNS.split("\"")[1];
        if (pointsNS.equalsIgnoreCase("ave") || pointsNS.equalsIgnoreCase("ave+") || pointsNS.equalsIgnoreCase("ave-") || pointsNS.equalsIgnoreCase("np") || contract.equalsIgnoreCase("lp")) {
            return AVERAGE_RESULT;
        }
        if (resultsText.contains("ew_score\":\"AVE+") || resultsText.contains("ew_score\":\"AVE") || resultsText.contains("ew_score\":\"AVE-")) {
            return AVERAGE_RESULT;
        }
        if (!checkCompatibility(resultsText)) {
            return AVERAGE_RESULT;
        }
        if (pointsNS.equalsIgnoreCase("pass") && !contract.equalsIgnoreCase("pass")) {
            if (resultsText.contains("\"contract\":null") && resultsText.contains("\"declarer\":null")) {
                return "pass,_,_,pass";
            }
            String temp = resultsText.toLowerCase();
            if (temp.contains("\"ew_score\":\"pass\"")) {
                return "pass,_,_,pass";
            }
            else {
                System.out.println(ConsoleColors.BLUE + "An entry error occurred. Contract: " + contract + ", declarer: " + declarer + ", pointsNS: " + pointsNS + ", boardNumber: " + boardNumber);
                System.out.println(resultsText);
                System.out.println("ProcessWebPage, cleanBoardResultsForPrinting #1");
                return ENTRY_ERROR;
            }
        }
        if (hasFError(pointsNS)) {
            System.out.println(ConsoleColors.RED + "Weird F Error has occurred. PointsNS: " + pointsNS);
            return WEIRD_F_ERROR;
        }

        int tricksTaken = calculateTricksTaken(contract, declarer, pointsNS, boardNumber);
        if (tricksTaken == -1001) {
            if (contract.equals("")) {
                return MISSING_CONTRACT_ERROR;
            }
            System.out.println("Board number: " + boardNumber);
            System.out.println(resultsText);
            return FORMAT_ERROR_OCCURRED_DURING_POINT_COUNTS;
        }
        if (tricksTaken == -1002) {
            System.out.println("Board number: " + boardNumber);
            return BOARD_RESULTS_DO_NOT_EXIST;
        }
        if(tricksTaken == -1003) {
            return NORMAL_BOARD_RESULTS_DO_NOT_EXIST;
        }
        if (tricksTaken > 13 || tricksTaken < 0) {
            System.out.println(ConsoleColors.RED + "There was a data entry error on board " + boardNumber + ". PointsNS: " + pointsNS + ", Contract: " + contract + ", Declarer: " + declarer);
            dataEntryErrors++;
            return POINT_COUNT_ERROR;
        }

        String[] splitContract = splitContract(contract);
        if (splitContract == null) {
            System.out.println(ConsoleColors.BLUE + "There was a string splitting error on board: " + boardNumber + ", the contract was: " + contract);
            System.out.println(ConsoleColors.BLUE + "ProcessWebPage, cleanBoardResultsForPrinting");
            return STRING_SPLIT_ERROR;
        }



        return splitContract[0] + "," + splitContract[1] + "," + splitContract[2] + "," + declarer + "," + tricksTaken;
    }

    private static boolean checkCompatibility(String text) {
        String nsPoints = text.split("\"ns_score\":")[1];
        nsPoints = nsPoints.split("\"")[1];
        String ewPoints = text.split("\"ew_score\":")[1];
        ewPoints = ewPoints.split("\"")[1];
        if (nsPoints.equalsIgnoreCase("pass") && ewPoints.equalsIgnoreCase("pass")) {
            return true;
        }
        if (nsPoints.equalsIgnoreCase("pass") || ewPoints.equalsIgnoreCase("pass")) {
            return false;
        }
        return true;
    }

    /**
     * breaks a contract to make it easy to print
     * @param contract raw String, commonly of form 3Hx
     * @return {#tricks, Suit, doubled}
     */
    private static String[] splitContract(String contract) {
        contract = contract.toLowerCase();
        String arr[] = null;
        String fill = "";
        if (contract.contains("nt")) {
            arr = contract.split("nt");
            fill = "nt";
        }
        else if (contract.contains("n")) {
            arr = contract.split("n");
            fill = "nt";
        }
        if (contract.contains("h")) {
            arr = contract.split("h");
            fill = "h";
        }
        if (contract.contains("s")) {
            arr = contract.split("s");
            fill = "s";
        }
        if (contract.contains("d")) {
            arr = contract.split("d");
            fill = "d";
        }
        if (contract.contains("c")) {
            arr = contract.split("c");
            fill = "c";
        }

        if (arr == null) {
            return null;
        }
        if (arr.length > 1) {
            if (arr[1].equals("")) {
                arr[1] = "_";
            }
            arr = new String[]{arr[0], fill, arr[1]};
        }
        else {
            arr = new String[]{arr[0], fill, "_"};
        }
        return arr;
    }

    /**
     * calculates the total number of tricks that were taken by the declarer
     * @param contract contract
     * @param declarer declarer direction
     * @param pointsNS points scored by NS
     * @param boardNumber board number (for vulnerability)
     * @return number of tricks taken, -1001 for contract not recognized, cause break, -1002 for contract invalid, please check, -1003 normal invalid contract error
     */
    private static int calculateTricksTaken(String contract, String declarer, String pointsNS, int boardNumber) {
        int suit = 0;// {-1 = minor, 0 = major, 1 = NT}
        boolean doubled = false;
        boolean redoubled = false;
        String remainder;

        if (contract.equals("")) {
            return -1003;
        }

        contract = contract.toLowerCase();
        if (contract.equals("pass")) {
            return 0;
        }

        if (contract.contains("x")) {
            doubled = true;
        }
        if (contract.contains("xx")) {
            redoubled = true;
        }

        if (contract.contains("nt")) {
            suit = 1;
            String[] split = contract.split("nt");
            contract = split[0];
            remainder = getRemainder(split);
        }
        else if (contract.contains("n")) {
            suit = 1;
            String[] split = contract.split("n");
            contract = split[0];
            remainder = getRemainder(split);
        }
        else if (contract.contains("s")) {
            suit = 0;
            String[] split = contract.split("s");
            contract = split[0];
            remainder = getRemainder(split);
        }
        else if (contract.contains("h")) {
            suit = 0;
            String[] split = contract.split("h");
            contract = split[0];
            remainder = getRemainder(split);
        }
        else if (contract.contains("c")) {
            suit = -1;
            String[] split = contract.split("c");
            contract = split[0];
            remainder = getRemainder(split);
        }
        else if (contract.contains("d")) {
            suit = -1;
            String[] split = contract.split("d");
            contract = split[0];
            remainder = getRemainder(split);
        }
        else {
            System.out.println(ConsoleColors.BLUE + "Could not identify contract. Contract: " + contract + ", points: " + pointsNS + ", declarer: " + declarer);
            System.out.println("ProcessWebPage, CalculateTricksTaken #1");
            char[] arr = contract.toCharArray();
            if (arr.length == 2 && arr[1] == 'w') {
                System.out.println("it's a weird data entry error, continuing!");
                return -1002;
            }
            return -1001;
        }


        //checks that my things actually work.
        if (!doubled && remainder.length() > 0) {
            if (contract.equals("de") && declarer.equals("ew_match_points") || contract.equals("")) {
                return -1003;
            }
            System.out.println(ConsoleColors.RED + "Error in contract identification, not doubled but string remains. Contract: " + contract + ", points: " + pointsNS + ", declarer: " + declarer);
            System.out.println("ProcessWebPage, CalculateTricksTaken #2");
            return -1002;
        }
        if (doubled && !redoubled && !remainder.equals("x")) {
            System.out.println(ConsoleColors.BLUE + "Error in contract identification, doubled but remainder is not x. Contract: " + contract + ", points: " + pointsNS + ", declarer: " + declarer);
            System.out.println("ProcessWebPage, CalculateTricksTaken #3");
            return -1001;
        }
        if (redoubled && !remainder.equals("xx")) {
            System.out.println(ConsoleColors.BLUE + "Error in contract identification, redoubled but remainder is not xx. Contract: " + contract + ", points: " + pointsNS + ", declarer: " + declarer);
            System.out.println("ProcessWebPage, CalculateTricksTaken #4");
            return -1001;
        }


        //begins parsing points
        int points = Integer.parseInt(pointsNS);


        //finds declarer and corrects direction
        boolean nsDeclarer;
        declarer = declarer.toLowerCase();
        if (declarer.equals("e") || declarer.equals("w")) {
            points = -points;
            nsDeclarer = false;
        }
        else if ((declarer.equals("n") || declarer.equals("s"))) {
            nsDeclarer = true;
        }
        else {
            if (declarer.equalsIgnoreCase("ns") || declarer.equalsIgnoreCase("ew")) {
                System.out.print(ConsoleColors.YELLOW);
                return -1002;
            }
            System.out.println(ConsoleColors.BLUE + "Could not identify the declarer. Contract: " + contract + ", points: " + pointsNS + ", declarer: " + declarer);
            System.out.println("ProcessWebPage, CalculateTricksTaken #5");
            return -1001;
        }


        int tricksRequired = Integer.parseInt(contract);

        //decides vulnerability
        boolean isVulnerable = isVulnerable(nsDeclarer, boardNumber);

        //handles lost contracts
        if (points < 0) {
            int lost = calculateLostTricks(-points, doubled, redoubled, isVulnerable);
            return 6 + tricksRequired - lost;
        }

        //handles won contracts
        return calculateWonTricks(points, suit, doubled, redoubled, isVulnerable, tricksRequired);

    }

    public static String getRemainder(String[] split) {
        if (split.length == 1) {
            return "";
        }
        else {
            return split[1];
        }
    }

    /**
     * calculates how many total tricks were won by a contract. This does not throw exceptions
     * @param points points scored
     * @param suit trump suit, {1 = NT, 0 = Major, -1 = Minor}
     * @param doubled
     * @param redoubled
     * @param isVulnerable
     * @param tricksRequired how many tricks were bid by the declarer
     * @return number of tricks taken
     */
    private static int calculateWonTricks(int points, int suit, boolean doubled, boolean redoubled, boolean isVulnerable, int tricksRequired) {
        if (tricksRequired == 7) {// grand slam
            if (isVulnerable) {
                points = points - 1500;
            }
            else {
                points = points - 1000;
            }
        }
        if (tricksRequired == 6) {// small slam
            if (isVulnerable) {
                points = points - 750;
            }
            else {
                points = points - 500;
            }
        }

        //easier to hit game if doubled
        if (doubled) {
            tricksRequired = tricksRequired * 2;
        }
        if (redoubled) {
            tricksRequired = tricksRequired * 2;
        }

        //game calculations
        if ((suit == 1 && tricksRequired >= 3) || (suit == 0 && tricksRequired >= 4) || ((suit == -1) && tricksRequired >=5) ) {
            if (isVulnerable) {
                points = points - 500;
            }
            else {
                points = points - 300;
            }
        }
        //remove partscore bonus
        else {
            points = points - 50;
        }

        //undoes the previous doubling.
        if (doubled) {
            tricksRequired = tricksRequired/2;
        }
        if (redoubled) {
            tricksRequired = tricksRequired/2;
        }

        if (!doubled) {
            return calculateUndoubledTricks(points, suit);
        }
        if (!redoubled) {
            return calculateDoubledTricks(points, suit, isVulnerable, tricksRequired);
        }
        return calculateRedoubledTricks(points, suit, isVulnerable, tricksRequired);
    }

    private static int calculateDoubledTricks(int points, int suit, boolean isVulnerable, int tricksRequired) {
        //insult bonus
        points = points - 50;

        //tricks bid
        if (suit == 1) {
            points = points - 2 * (40 + (tricksRequired - 1) * 30);
        }
        if (suit == 0) {
            points = points - 2 * (30 * tricksRequired);
        }
        if (suit == -1) {
            points = points - 2 * (20 * tricksRequired);
        }

        int overtricks;
        if (isVulnerable) {
            overtricks = points/200;
        }
        else {
            overtricks = points/100;
        }

        return tricksRequired + overtricks + 6;

    }

    private static int calculateRedoubledTricks(int points, int suit, boolean isVulnerable, int tricksRequired) {
        //insult bonus
        points = points - 100;

        //tricks bid
        if (suit == 1) {
            points = points - 4 * (40 + (tricksRequired - 1) * 30);
        }
        if (suit == 0) {
            points = points - 4 * (30 * tricksRequired);
        }
        if (suit == -1) {
            points = points - 4 * (20 * tricksRequired);
        }

        int overtricks;
        if (isVulnerable) {
            overtricks = points/400;
        }
        else {
            overtricks = points/200;
        }

        return tricksRequired + overtricks + 6;
    }

    private static int calculateUndoubledTricks(int points, int suit) {
        if (suit == 1) {//NT
            if (points == 40) {
                return 1 + 6;
            }
            else {
                points = points - 40;
                return 6 + 1 + points/30;
            }
        }
        if (suit == 0) {
            return 6 + points/30;
        }
        if (suit == -1) {
            return 6 + points/20;
        }
        return 0;
    }

    /**
     * Calculates how many tricks down a contract was. This does not throw errors.
     * @param points
     * @param doubled whether the contract was doubled
     * @param redoubled whether the contract was redoubled
     * @param vulnerable whether the declarer was vulnerable
     * @return number of lost tricks
     */
    private static int calculateLostTricks(int points, boolean doubled, boolean redoubled, boolean vulnerable) {
        if (!doubled) {
            int divisor;
            if (vulnerable) {
                divisor = 100;
            }
            else {
                divisor = 50;
            }
            return points/divisor;
        }
        if (redoubled) {
            points = points/2;
        }
        if (vulnerable) {
            switch (points) {
                case 100 -> {
                    return 1;
                }
                case 300 -> {
                    return 2;
                }
                case 500 -> {
                    return 3;
                }
            }
            int down = 3;
            points = points - 500;
            return down + points/300;
        }
        else {
            switch (points) {
                case 200 -> {
                    return 1;
                }
                case 500 -> {
                    return 2;
                }
            }
            int down = 2;
            points = points - 500;
            return down + points/300;
        }
    }

    /**
     * decides if the declarer was vulnerable. This *should* not throw an exception, but technically can.
     * @param nsDeclarer is the declarer in the NS partnership
     * @param boardNumber
     * @return true if the declarer is vulnerable.
     */
    private static boolean isVulnerable(boolean nsDeclarer, int boardNumber) {
        boardNumber = boardNumber % 16;
        switch (boardNumber) {
            case 1, 8, 11, 14:
                return false;
            case 2, 5, 12, 15:
                if (nsDeclarer) {
                    return true;
                }
                return false;
            case 3, 6, 9, 0:
                if (nsDeclarer) {
                    return false;
                }
                return true;
            case 4, 7, 10, 13:
                return true;

        }
        FormatError error = new FormatError(); //this is only allowed because I genuinely do not know how this would be reached.
        error.severelyUnexpected = true;
        throw error;
    }

    //TODO: Implement multiple sections, then restart entire process! Oh boy!

}
