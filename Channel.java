public class Channel {
    private int[][] data;
    private double channelNoise;
    private int safeData;

    public Channel() {
        channelNoise = 0;
    }

    public Channel(double channelNoise) {
        this.channelNoise = channelNoise;
    }

    public void setChannelNoise(int channelNoise) {
        this.channelNoise = channelNoise;
    }

    public void send(int[][] data, int overflow) {
        this.safeData = overflow;
        randomFlip(data, this.channelNoise);
        this.data = data.clone();
    }

    public int[][] receive() {
        return this.data;
    }

    public int receiveSafeData() {
        return this.safeData;
    }

    private void randomFlip(int[][] data, double probability) {
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                if (Math.random() < probability)
                    data[i][j] ^= 1;
            }
        }
    }
}
