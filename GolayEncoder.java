import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class GolayEncoder {
    // 12 x 23 matrix for encoding
    private final int[][] generatorMatrix;

    // 24 x 12 parity check matrix
    private final int[][] controlMatrix;

    // Amount of added bits to missing vector length
    private int overflow;

    // On Init generate unchanging Matrices
    public GolayEncoder() {
        this.generatorMatrix = generateGeneratorMatrix();
        this.controlMatrix = generateControlMatrix();
    }

    public int getOverflow() {
        return overflow;
    }

    /**
     * Encodes file to an array of vector arrays given file path
     * @param path path to file location
     * @throws IOException when failed to read file
     * @return array of 23 length encoded vectors
     */
    public int[][] encodeFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Path.of(path));
        return getEncodedVectors(bytes);
    }

    /**
     * Encodes string of text to an array of vector arrays
     * @param text text to encode
     * @return array of 23 length encoded vectors
     */
    public int[][] encode(String text) {
        byte[] bytes = text.getBytes();
        return getEncodedVectors(bytes);
    }

    /**
     * Encodes one vector
     * @param vector input 12 length vector
     * @return 23 length encoded vector
     */
    public int[] encode(int[] vector) {
        return multiply(vector, this.generatorMatrix);
    }

    /**
     * Decodes and writes encoded vectors to a file.
     * @param encodedVectors array of 23 length vectors.
     * @param path location and file name of to be decoded file.
     * @param overflow number of additional bits to complete full vector.
     * @throws IOException when failed to write file.
     */
    public void decodeFile(int[][] encodedVectors, String path, int overflow) throws IOException {
        byte[] bytes = getDecodedBytes(encodedVectors, overflow);
        Files.write(Path.of(path), bytes);
    }

    /**
     * Decodes an array of 23 length vectors
     * @param encodedVectors array of 23 length vectors.
     * @param overflow number of additional bits to complete full vector.
     * @return decoded text.
     */
    public String decode(int[][] encodedVectors, int overflow) {
        byte[] bytes = getDecodedBytes(encodedVectors, overflow);
        return new String(bytes);
    }

    /**
     * Decodes 23 length vector
     * @param vector 23 length vector.
     * @return decoded 12 length vector.
     */
    public int[] decode(int[] vector) {
        int[] vector24 = new int[24];
        System.arraycopy(vector, 0, vector24, 0, vector.length);

        // turns vector's length from 23 to 24 by adding a "1" if current sum of values is even or "0" if sum of values is odd.
        vector24[vector24.length - 1] = (Arrays.stream(vector).sum() + 1) % 2;

        // gets a list of bits with value "1" in place where error was found.
        int[] errorPattern = findErrorPattern(vector24, this.controlMatrix);

        // fixes errors by applying binary sum on encoded vector with error pattern.
        int[] result24 = sum(vector24, errorPattern);
        int[] result = new int[12];

        // take first 12 digits of decoded and fixed an array.
        System.arraycopy(result24, 0, result, 0, result.length);

        return result;
    }

    /**
     * Combines values of vector array to a string.
     * @param vector any length vector.
     * @return string of bits.
     */
    public String toString(int[] vector) {
        StringBuilder bits = new StringBuilder();
        for (int bit : vector) {
            bits.append(bit);
        }
        return bits.toString();
    }

    /**
     * Transforms array of unencoded bytes to an array of encoded vectors.
     * @param bytes array of unencoded bytes.
     * @return array of binary vectors.
     */
    private int[][] getEncodedVectors(byte[] bytes) {
        int[][] vectors = parseBytes(bytes);

        int[][] encodedVectors = new int[vectors.length][vectors[0].length];

        for (int i = 0; i < encodedVectors.length; i++) {
            encodedVectors[i] = encode(vectors[i]);
        }

        return encodedVectors;
    }

    /**
     * Transforms array of binary vectors to array of bytes.
     * @param encodedVectors array of binary vectors.
     * @param overflow number of additional bits to complete full vector.
     * @return array of bytes.
     */
    private byte[] getDecodedBytes(int[][] encodedVectors, int overflow) {
        StringBuilder decodedStr = new StringBuilder();

        // combine encoded bits into one string.
        for (int[] encodedVector : encodedVectors) {
            decodedStr.append(toString(decode(encodedVector)));
        }

        // get length of original values.
        int decodedLength = decodedStr.length() - overflow;

        byte[] bytes = new byte[decodedLength / 8];

        // loop through a string of binary values by assigning 8 bits (1 byte) to byte array.
        for (int i = 0, b = 0; i < decodedLength; i += 8, b++) {
            bytes[b] = (byte)Integer.parseInt(decodedStr.substring(i, i + 8), 2);
        }

        return bytes;
    }

    /**
     * Transforms array of bytes to an array of binary vectors.
     * @param bytes array of bytes.
     * @return array of binary vectors.
     */
    private int[][] parseBytes(byte[] bytes) {
        int vectorLength = 12;
        StringBuilder bits = new StringBuilder();

        // Converts byte array into a string of binary values by:
        // 1. Integer.toBinaryString(b & 0xFF) turns signed bytes into unsigned bytes and converts them to binary values
        // 2. "%8s" formats byte string to take 8 spots, by placing existing values to the right
        // 3. replace(' ', '0') empty values on the left side of 8 bit string are replaced with zeroes.
        for (byte b : bytes) {
            bits.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }

        // to fill missing vector length add zeroes at the end.
        int overflow = vectorLength - (bits.length() % vectorLength);
        bits.append("0".repeat(overflow));

        this.overflow = overflow;

        int vectorCount = bits.length() / vectorLength;
        int[][] vectors = new int[vectorCount][vectorLength];

        // turn bits into array of vectors.
        for (int i = 0; i < vectorCount; i++) {
            for (int j = 0; j < vectorLength; j++) {
                vectors[i][j] = Integer.parseInt(String.valueOf(bits.charAt(i * vectorLength + j)));
            }
        }

        return vectors;
    }

    /**
     * Method to binary multiply vector with matrix
     * @param vector array of binary values.
     * @param matrix 2D array of binary values.
     * @return multiplied vector and matrix.
     */
    private int[] multiply(int[] vector, int[][] matrix) {
        int[] result = new int[matrix[0].length];

        for (int i = 0; i < result.length; i++) {
            int value = 0;
            // multiply and sum every matrix column with vector values.
            for (int j = 0; j < matrix.length; j++) {
                value += matrix[j][i] * vector[j];
            }

            // modulus for binary sum.
            result[i] = value % 2;
        }

        return result;
    }

    /**
     * Method to binary sum vectors.
     * @param vector1 array of binary values.
     * @param vector2 array of binary values.
     * @return summed vector.
     */
    private int[] sum(int[] vector1, int[] vector2) {
        int[] result = new int[vector1.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (vector1[i] + vector2[i]) % 2;
        }
        return result;
    }

    /**
     * Creates 12 x 23 matrix for encoding. Left side (first 12 columns) is an Identity matrix. Right side is a 12 x 11 matrix where row i+1 is row i shifted left. Last row contains only values of "1".
     * @return 12 x 23 matrix as 2D integer array.
     */
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

    /**
     * Creates 24 x 12 matrix for encoding. Top side (first 12 rows) is an Identity matrix. Bottom side is a 12 x 12 matrix where row i+1 is row i shifted left. Last row and last column contain only values of "1" except last index of the matrix.
     * @return  24 x 12 matrix as 2D integer array.
     */
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

    /**
     * Algorithm to find error pattern by manipulating parity check matrix with encoded 24 length vector.
     * @param vector array of 24 binary values.
     * @param controlMatrix 24 x 12 matrix for parity check.
     * @return vector with values "1" at indexes of error.
     */
    private int[] findErrorPattern(int[] vector, int[][] controlMatrix) {
        // syndrome (s) - array of parity check violations.
        int[] syndrome = multiply(vector, controlMatrix);

        // errorPattern (u)
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
