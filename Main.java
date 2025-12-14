import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        GolayEncoder encoder = new GolayEncoder();

        String str = "Long and boring text :)";
        int[][] encodedStr = encoder.encode(str);
        randomFlip(encodedStr, 0.08);
        String decodedStr = encoder.decode(encodedStr);
        System.out.println(decodedStr);
    }

    public static int[] readConsoleInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter 12 bit vector:");

        String input =  scanner.nextLine();
        return input.chars().map(c -> c - '0').toArray();
    }

    public static void randomFlip(int[][] data, double probability) {
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                if (Math.random() < probability)
                    data[i][j] ^= 1;
            }
        }
    }
}
