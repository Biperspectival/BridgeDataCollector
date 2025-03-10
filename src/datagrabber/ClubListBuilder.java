package datagrabber;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

public class ClubListBuilder {

    public static void main(String[] args) throws IOException {
        for (int i = 1; i < 1000; i++) {
            loadBatch(i);
        }
    }

    public static void loadBatch(int batchNumber) throws IOException {
        HashSet<String> validNumbers = new HashSet<>();
        for (int i = batchNumber * 1000; i < (batchNumber + 1) * 1000; i++) {
            if (ReadingWebPage.checkURL(i)) {
                validNumbers.add(ReadingWebPage.getClubID(i));
            }
        }
        writeList(validNumbers, batchNumber);
        System.out.println("batch " + batchNumber + " done!");
    }

    public static void writeList(HashSet<String> validNumbers, int batch) throws IOException {
        File f = new File("D:/Bridge/Clubs/clubs" + batch + ".txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(f));
        for (String s : validNumbers) {
            writer.write(s);
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }
}
