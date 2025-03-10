package structure;

public class Contract {

    public boolean wasPassed;
    public int numberBid;
    public int suit; //{-2, passed out, -1 = NT, 0 = S, 1 = H, 2 = D, 3 = C}
    public int doubledDegree; //{0, not doubled; 1, doubled; 2, redoubled
    public int declarer; //{0 = North, 1 = East, 2 = South, 3 = West}
    public int tricksTaken;


}
