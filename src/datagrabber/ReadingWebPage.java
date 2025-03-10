package datagrabber;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
public class ReadingWebPage {

    //Digging into the footer is where all of the juicy information is at.
    //Hand Records can be found at "hand_records":
    //Board results can be found at "boards":

    /**
     * This downloads the raw html files so that the main server isn't pinged repeatedly when I'm analyzing the files.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        for (int i = 894539; i < 933919; i++) {//I just manually update this number as I download new files. You could have it dump a save file instead.
            pullText(i);
            System.out.println(i);
        }
    }
    public static String getClubID(int number) {
        return String.format("%06d", number);
    }

    /**
     * This simply checks that a URL is valid
     * @param number game number
     * @return true if the stream is successfully opened
     */
    public static boolean checkURL(int number){
        try {
            //Instantiating the URL class
            URL url = new URL("https://my.acbl.org/club-results/" + getClubID(number));
            URLConnection openConnection = url.openConnection();
            openConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
            //Retrieving the contents of the specified page
            InputStream stream = openConnection.getInputStream();
            stream.close();
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    /**
     * This pulls the raw data into the computer's local storage
     * @param number
     */
    public static void pullText(int number) {
        String address = "https://my.acbl.org/club-results/details/" + number;
        try {
            URL url = new URL(address);
            URLConnection openConnection = url.openConnection();
            openConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
            //Retrieving the contents of the specified page
            InputStream stream = openConnection.getInputStream();
            Scanner sc = new Scanner(stream);
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("D:/Bridge/HTMLData/data" + number + ".txt")));//TODO: Pull this out as some kind of configurable if other people want to use this code
            StringBuffer sb = new StringBuffer();
            while(sc.hasNext()) {
                writer.write(sc.next());
                writer.flush();
            }
        } catch (IOException e) {
            System.out.println("Address not found for " + number);
        }
    }

    //DEPRECATED
    public static void connectToURL(String address) throws IOException {
        //Instantiating the URL class
        URL url = new URL(address);
        URLConnection openConnection = url.openConnection();
        openConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
        //Retrieving the contents of the specified page
        InputStream stream = openConnection.getInputStream();
        Scanner sc = new Scanner(stream);
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("D:/Bridge/HTMLData/data.txt")));
        StringBuffer sb = new StringBuffer();
        while(sc.hasNext()) {
            writer.write(sc.next());
            writer.flush();
        }
        //Retrieving the String from the String Buffer object
        String result = sb.toString();
        System.out.println(result);
        //Removing the HTML tags
        System.out.println("Contents of the web page: "+result);
    }



}