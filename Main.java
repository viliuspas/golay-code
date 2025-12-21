import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
//        readText();
        readImage();
    }

    public static int[] readConsoleInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter 12 bit vector:");

        String input =  scanner.nextLine();
        return input.chars().map(c -> c - '0').toArray();
    }

    public static void readText() {
        GolayEncoder encoder = new GolayEncoder();
        Channel channel = new Channel(0.07);

        String str = "Šokoladas ir bananas 🤕";
        int[][] encodedStr = encoder.encode(str);

        channel.send(encodedStr, encoder.getOverflow());

        int[][] receivedData = channel.receive();
        int safeData = channel.receiveSafeData();

        String decodedStr = encoder.decode(receivedData, safeData);
        System.out.println(decodedStr);
    }

    public static void readImage() {
        GolayEncoder encoder = new GolayEncoder();
        Channel channel = new Channel(0.5);

        try {
            int[][] encodedImg = encoder.encodeFile("images/gryb.bmp");

            channel.send(encodedImg, encoder.getOverflow(), 16);

            int[][] receivedData = channel.receive();
            int safeData = channel.receiveSafeData();

            encoder.decodeFile(receivedData, "images/result.bmp", safeData);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
