import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        int runs = 10;

        for (double i = 0; i < 1; i += 0.1) {
            long totalTime = 0;

            for (int j = 0; j < runs; j++) {
                totalTime += sendImage(i);
            }

            double averageTime = (double) totalTime / runs;
            System.out.println(i + " " + averageTime);
        }
    }

    private static long sendImage(double errorRate) {
        GolayEncoder encoder = new GolayEncoder();
        Channel channel = new Channel(errorRate);

        try {
            int[][] encodedImg = encoder.encodeFile("images/psip.bmp");
            int headerSize = 16;
            channel.send(encodedImg, encoder.getOverflow(), headerSize);

            int[][] receivedData = channel.receive();
            int safeData = channel.receiveSafeData();

            String outputPath = "images/result.bmp";
            long start = System.currentTimeMillis();
            encoder.decodeFile(receivedData, outputPath, safeData);
            long end = System.currentTimeMillis();
            return end-start;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
