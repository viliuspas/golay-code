import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        GolayEncoder encoder = new GolayEncoder();

        String str = "Today's challenge: Š.";
        int[][] encodedStr = encoder.encode(str);
        String decodedStr = encoder.decode(encodedStr);
        System.out.println(decodedStr);
    }

    public static int[] readConsoleInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter 12 bit vector:");

        String input =  scanner.nextLine();
        return input.chars().map(c -> c - '0').toArray();
    }
}
