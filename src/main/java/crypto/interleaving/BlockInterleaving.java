package crypto.interleaving;

import crypto.recoverycode.HammingCode;
import crypto.util.PrintUtil;
import java.io.PrintStream;

public final class BlockInterleaving {

    private final PrintUtil printUtil;
    private final HammingCode hammingCode;

    public BlockInterleaving(PrintStream stream) {
        this.printUtil = new PrintUtil(stream);
        this.hammingCode = new HammingCode(stream);
    }

    /**
     * Divide a given message into smaller strings with a given length
     * @param message will be divided
     * @param numberOfInformationBytes length of smaller words
     * @since 1.0
     * @return a matrix with the smaller words
     */
    public int[][] getInformationBytesMatrix(int[] message, int numberOfInformationBytes) {
        int[][] matrix = new int[((int) Math.ceil((double) message.length / numberOfInformationBytes))][numberOfInformationBytes];

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length && i * numberOfInformationBytes + j < message.length; j++) {
                matrix[i][j] = message[i * numberOfInformationBytes + j];
            }
        }

        return matrix;
    }

    /**
     * Encode words of the matrix separately and save those into a new encoded matrix
     * @param informationMatrix a matrix with information words
     * @param numberOfInformationBytes a length of information words
     * @param numberOfRedundantBytes a length of redundant bytes array
     * @param codeWordLength a total length of word include the information bytes and redundant bytes
     * @since 1.0
     * @return an encoded bytes matrix
     */
    public int[][] getEncodedBytesMatrix(int[][] informationMatrix, int numberOfInformationBytes,
                                         int numberOfRedundantBytes, int codeWordLength) {
        int[][] matrix = new int[informationMatrix.length][codeWordLength];

        int[][] checkMatrix = hammingCode.getCheckMatrix(numberOfInformationBytes, numberOfRedundantBytes);

        for (int i = 0; i < informationMatrix.length; i++) {
            matrix[i] = hammingCode.encodeMessage(checkMatrix, informationMatrix[i],
                    numberOfInformationBytes, numberOfRedundantBytes);
        }

        return matrix;
    }

    /**
     * Merge encoded words into a message using the interleaving way
     * @since 1.0
     * @return an interleaved sequence
     */
    public int[] getInterleavedSequence(int[][] encodedBytesMatrix) {
        int[] sequence = new int[encodedBytesMatrix.length * encodedBytesMatrix[0].length];
        for (int i = 0; i < encodedBytesMatrix[i].length; i++) {
            for (int j = 0; j < encodedBytesMatrix.length; j++) {
                sequence[i * encodedBytesMatrix.length + j] = encodedBytesMatrix[j][i];
            }
        }
        return sequence;
    }

    /**
     * Unmerge a message into a matrix of encoded words using the interleaving way
     * @param interleavedSequence a sequence of interleaved words
     * @param codeWordLength a length of any encoded word
     * @since 1.0
     * @return a matrix of encoded words
     */
    public int[][] deinterleaveSequence(int[] interleavedSequence, int codeWordLength) {
        int[][] matrix = new int[((int) Math.ceil((double) interleavedSequence.length / codeWordLength))][codeWordLength];

        for (int i = 0; i < matrix[0].length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                matrix[j][i] = interleavedSequence[j + matrix.length * i];
            }
        }

        return matrix;
    }

    /**
     * Recover a given matrix of received interleaved sequence of encoded words by the hamming way
     * @param encodedMatrix a matrix with encoded words
     * @param numberOfInformationBytes a length of information words
     * @param numberOfRedundantBytes a length of redundant bytes array
     * @param codeWordLength a total length of word include the information bytes and redundant bytes
     * @since 1.0
     * @return a recovered bytes matrix
     */
    public int[][] recoverEncodedBytesMatrix(int[][] encodedMatrix, int numberOfInformationBytes,
                                             int numberOfRedundantBytes, int codeWordLength) {

        int[][] recoveredBytesMatrix = new int[encodedMatrix.length][encodedMatrix[0].length];
        int[][] checkMatrix = hammingCode.getCheckMatrix(numberOfInformationBytes, numberOfRedundantBytes);
        int[][] syndromeMatrix = new int[encodedMatrix.length][numberOfRedundantBytes];

        for (int i = 0; i < encodedMatrix.length; i++) {
            int[] yr = hammingCode.getRedundantBytes(encodedMatrix[i], numberOfInformationBytes, numberOfRedundantBytes);
            int[] calculatedYr = hammingCode.calculateRedundantBytes(checkMatrix, encodedMatrix[i],
                    numberOfInformationBytes, numberOfRedundantBytes);

            syndromeMatrix[i] = hammingCode.getSyndrome(yr, calculatedYr);

            int[] recoveringBytes = hammingCode.getRecoveryBytes(syndromeMatrix[i], checkMatrix,
                    numberOfRedundantBytes, codeWordLength);

            for (int j = 0; j < recoveredBytesMatrix[i].length; j++) {
                recoveredBytesMatrix[i][j] = encodedMatrix[i][j] ^ recoveringBytes[j];
            }
        }
        return recoveredBytesMatrix;
    }

    /**
     * Cut the information bytes of words and return these as a matrix of decoded words
     * @since 1.0
     * @return a matrix of information words
     */
    public int[][] decodeBytesMatrix(int[][] encodedMatrix, int numberOfInformationBytes) {
        int[][] informationBytes = new int[encodedMatrix.length][numberOfInformationBytes];

        for (int i = 0; i < encodedMatrix.length; i++) {
            if (numberOfInformationBytes >= 0) System.arraycopy(encodedMatrix[i], 0,
                    informationBytes[i], 0, numberOfInformationBytes);
        }

        return informationBytes;
    }


    /**
     * Concatenate smaller words into the one word
     * @param decodedBytesMatrix a matrix of smaller words
     * @since  1.0
     * @return a message that was divided in the start
     */
    public int[] getMessageSequence(int[][] decodedBytesMatrix) {
        int[] sequence = new int[decodedBytesMatrix.length * decodedBytesMatrix[0].length];

        for (int i = 0; i < decodedBytesMatrix.length; i++) {
            System.arraycopy(decodedBytesMatrix[i], 0, sequence,
                    i * decodedBytesMatrix[0].length, decodedBytesMatrix[0].length);
        }

        return sequence;
    }
}