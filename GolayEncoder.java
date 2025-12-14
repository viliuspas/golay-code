import java.util.Arrays;

public class GolayEncoder {
    private final int[][] generatorMatrix;
    private final int[][] controlMatrix;
    private int overflow;

    public GolayEncoder() {
        this.generatorMatrix = generateGeneratorMatrix();
        this.controlMatrix = generateControlMatrix();
    }

    public int[][] encode(String text) {
        int[][] vectors = parseString(text, 12);

        int[][] encodedVectors = new int[vectors.length][vectors[0].length];

        for (int i = 0; i < encodedVectors.length; i++) {
            encodedVectors[i] = encode(vectors[i]);
        }

        return encodedVectors;
    }

    public int[] encode(int[] vector) {
        return multiply(vector, this.generatorMatrix);
    }

    public String decode(int[][] encodedVectors) {
        StringBuilder decodedStr = new StringBuilder();
        for (int[] encodedVector : encodedVectors) {
            decodedStr.append(toString(decode(encodedVector)));
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < decodedStr.length() - this.overflow; i += 8) {
            String byteStr = decodedStr.substring(i, i + 8);
            result.append((char)Integer.parseInt(byteStr, 2));
        }

        return result.toString();
    }

    public int[] decode(int[] vector) {
        int[] vector24 = new int[24];
        System.arraycopy(vector, 0, vector24, 0, vector.length);
        vector24[vector24.length - 1] = (Arrays.stream(vector).sum() + 1) % 2;

        int[] errorPattern = findErrorPattern(vector24, this.controlMatrix);

        int[] result24 = sum(vector24, errorPattern);
        int[] result = new int[12];
        System.arraycopy(result24, 0, result, 0, result.length);

        return result;
    }

    public String toString(int[] vector) {
        StringBuilder bits = new StringBuilder();
        for (int bit : vector) {
            bits.append(bit);
        }
        return bits.toString();
    }

    private int[][] parseString(String text, int vectorLength) {
        byte[] bytes = text.getBytes();
        StringBuilder bits = new StringBuilder();
        for (byte b : bytes) {
            bits.append(String.format("%8s", Integer.toBinaryString(b)).replace(' ', '0'));
        }

        this.overflow = vectorLength - (bits.length() % vectorLength);
        bits.append("0".repeat(this.overflow));

        int vectorCount = bits.length() / vectorLength;
        int[][] vectors = new int[vectorCount][vectorLength];

        for (int i = 0; i < vectorCount; i++) {
            for (int j = 0; j < vectorLength; j++) {
                vectors[i][j] = Integer.parseInt(String.valueOf(bits.charAt(i * vectorLength + j)));
            }
        }

        return vectors;
    }

    private int[] multiply(int[] vector, int[][] matrix) {
        int[] result = new int[matrix[0].length];

        for (int i = 0; i < result.length; i++) {
            int value = 0;
            for (int j = 0; j < matrix.length; j++) {
                value += matrix[j][i] * vector[j];
            }
            result[i] = value % 2;
        }

        return result;
    }

    private int[] sum(int[] vector1, int[] vector2) {
        int[] result = new int[vector1.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (vector1[i] + vector2[i]) % 2;
        }
        return result;
    }

    private int[][] generateGeneratorMatrix() {
        int[] bFirstLine = {1,1,0,1,1,1,0,0,0,1,0};
        int[] iFirstLine = {1,0,0,0,0,0,0,0,0,0,0,0};

        int[][] matrix = new int[12][23];

        System.arraycopy(iFirstLine, 0, matrix[0], 0, iFirstLine.length);
        System.arraycopy(bFirstLine, 0, matrix[0], iFirstLine.length, bFirstLine.length);

        for (int i = 1; i < 12; i++) {
            if (i == 11) {
                matrix[i] = new int[]{0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1};
                break;
            }

            for (int j = 0; j < iFirstLine.length; j++) {
                matrix[i][j] = matrix[i - 1][(j + iFirstLine.length - 1) % iFirstLine.length];
            }

            for (int j = 0; j < bFirstLine.length; j++) {
                matrix[i][j + iFirstLine.length] = matrix[i - 1][iFirstLine.length + (j + 1) % bFirstLine.length];
            }
        }

        return matrix;
    }

    private int[][] generateControlMatrix() {
        int[] iFirstLine = {1,0,0,0,0,0,0,0,0,0,0,0};
        int[] bFirstLine = {1,1,0,1,1,1,0,0,0,1,0,1};

        int[][] matrix = new int[24][12];

        matrix[0] = iFirstLine;

        for (int i = 1; i < 12; i++) {
            for (int j = 0; j < iFirstLine.length; j++) {
                matrix[i][j] = matrix[i-1][(j + iFirstLine.length - 1) % iFirstLine.length];
            }
        }

        matrix[12] = bFirstLine;

        for (int i = 13; i < 23; i++) {
            for (int j = 0; j < bFirstLine.length - 1; j++) {
                matrix[i][j] = matrix[i - 1][(j + 1) % (bFirstLine.length - 1)];
            }
            matrix[i][bFirstLine.length - 1] = 1;
        }

        matrix[23] = new int[]{1,1,1,1,1,1,1,1,1,1,1,0};

        return matrix;
    }

    private int[] findErrorPattern(int[] vector, int[][] controlMatrix) {
        int[] syndrome = multiply(vector, controlMatrix);
        int[] errorPattern = new int[syndrome.length * 2];

        // If wt(s) <= 3 then u = [s,0]
        if (Arrays.stream(syndrome).sum() <= 3) {
            System.arraycopy(syndrome, 0, errorPattern, 0, syndrome.length);
            return errorPattern;
        }

        // If wt(s + bi) <= 2 for some row bi of B then u = [s + bi, ei]
        for (int i = 12; i < controlMatrix.length; i++) {
            for (int j = 0; j < controlMatrix[i].length; j++) {
                errorPattern[j] = (controlMatrix[i][j] + syndrome[j]) % 2;
            }
            if (Arrays.stream(errorPattern).sum() <= 2) {
                errorPattern[i] = 1;
                return errorPattern;
            }
        }

        errorPattern = new int[syndrome.length * 2];

        int[][] matrixB = new int[12][12];
        System.arraycopy(controlMatrix, 12, matrixB, 0, matrixB.length);
        int[] syndromeB = multiply(syndrome, matrixB);

        // If wt(s + bi) <= 2 for some row bi of B then u = [s + bi, ei]
        if (Arrays.stream(syndromeB).sum() <= 3) {
            System.arraycopy(syndromeB, 0, errorPattern, errorPattern.length / 2, syndrome.length);
            return errorPattern;
        }

        // If wt(sB + bi) <= 2 for some row bi of B then u = [ei, sB + bi]
        for (int i = 12; i < controlMatrix.length; i++) {
            for (int j = 0; j < controlMatrix[i].length; j++) {
                errorPattern[j + 12] = (controlMatrix[i][j] + syndromeB[j]) % 2;
            }
            if (Arrays.stream(errorPattern).sum() <= 2) {
                errorPattern[i - 12] = 1;
                return errorPattern;
            }
        }

        return errorPattern;
    }
}
