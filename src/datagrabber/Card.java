package datagrabber;

public enum Card {
    A,
    K,
    Q,
    J,
    T,
    nine,
    eight,
    seven,
    six,
    five,
    four,
    three,
    two;
    public static Card stringToCard(char c) {
        return switch (c) {
            case 'A' -> A;
            case 'K' -> K;
            case 'Q' -> Q;
            case 'J' -> J;
            case 'T' -> T;
            case '9' -> nine;
            case '8' -> eight;
            case '7' -> seven;
            case '6' -> six;
            case '5' -> five;
            case '4' -> four;
            case '3' -> three;
            case '2' -> two;
            case '1' -> T;
            case '0' -> null;
            default -> null;
        };
    }

    /**
     * explains why a character returns a null card
     * @param c character
     * @return {1 = normal, 0 = data entry error, -1 = unknown}
     */
    public static int checkCardForError(char c) {
        if (c == '0') {
            return 1;
        }
        if (c == '?') {
            return 0;
        }
        if (c == '-') {
            return 1;
        }
        return -1;
    }

    public static String cardToString(Card c) {
        return switch (c) {
            case A -> "A";
            case K -> "K";
            case Q -> "Q";
            case J -> "J";
            case T -> "T";
            case nine -> "9";
            case eight -> "8";
            case seven -> "7";
            case six -> "6";
            case five -> "5";
            case four -> "4";
            case three -> "3";
            case two -> "2";
        };
    }

    public static Card stringToCard(String s) {
        return stringToCard(s.toCharArray()[0]);
    }

}
