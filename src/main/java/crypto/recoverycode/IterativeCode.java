package crypto.recoverycode;

import crypto.util.PrintUtil;

import java.io.PrintStream;
import java.util.Arrays;

public class IterativeCode {

    private final PrintUtil printUtil;

    public IterativeCode(PrintStream stream) {
        this.printUtil = new PrintUtil(stream);
    }

    /**
     * Build a matrix which will be used to encode any message by iterative coding way
     * @version 1.0
     */
    public int[][] buildMatrix(int[] message, int numberOfRows, int numberOfColumns) {
        int[][] matrix = new int[numberOfRows + 1][numberOfColumns + 1];
        for (int i = 0; i < numberOfRows; i++) {
            for (int j = 0; j < numberOfColumns; j++) {
                if (message[i * numberOfRows + j] == 1) {
                    matrix[i][j] = 1;
                    matrix[i][numberOfColumns] = (matrix[i][numberOfColumns] + 1) % 2;
                    matrix[numberOfRows][j] = (matrix[numberOfRows][j] + 1) % 2;
                }
            }
        }

        matrix[numberOfRows][numberOfColumns] = calculateTotalSum(matrix);

        return matrix;
    }

    /**
     * Add redundant bytes to a matrix which have filled by a message
     * @version 1.0
     * @return a matrix with redundant bytes
     */
    public int[][] addRedundantBytes(int[][] matrix, int numberOfRows, int numberOfColumns) {
        int[][] matrixWithRedundantBytes = new int[numberOfRows + 1][numberOfColumns + 1];

        for (int i = 0; i < numberOfRows; i++) {
            for (int j = 0; j < numberOfColumns; j++) {
                if (matrix[i][j] == 1) {
                    matrixWithRedundantBytes[i][j] = 1;
                    matrixWithRedundantBytes[i][numberOfColumns] = (matrixWithRedundantBytes[i][numberOfColumns] + 1) % 2;
                    matrixWithRedundantBytes[numberOfRows][j] = (matrixWithRedundantBytes[numberOfRows][j] + 1) % 2;
                }
            }
        }

        matrixWithRedundantBytes[numberOfRows][numberOfColumns] = calculateTotalSum(matrixWithRedundantBytes);

        return matrixWithRedundantBytes;
    }

    /**
     * @version 1.0
     * @return a submatrix without redundant bytes on the sides
     */
    public int[][] getMatrixWithoutRedundantBytes(int[][] matrix, int numberOfRows, int numberOfColumns) {
        int[][] cutMatrix = new int[numberOfRows][];
        for (int i = 0; i < numberOfRows; i++) {
            cutMatrix[i] = Arrays.copyOfRange(matrix[i], 0, numberOfColumns);
        }

        return cutMatrix;
    }

    /**
     * Convert a given matrix to a message
     * @version 1.0
     * @return a message from matrix with redundant bytes in the end of message
     */
    public int[] convertMatrixToMessage(int[][] matrix, int numberOfRows, int numberOfColumns) {
        int[] message = new int[matrix.length * matrix[0].length];

        for (int i = 0; i < numberOfRows; i++) {
            if (numberOfColumns >= 0) System.arraycopy(matrix[i], 0, message, i * numberOfRows, numberOfColumns);
        }

        int[] redundantBytes = getRedundantBytesFromMatrix(matrix, numberOfRows, numberOfColumns);

        System.arraycopy(redundantBytes, 0, message, numberOfRows * numberOfColumns, redundantBytes.length);

        return message;
    }

    /**
     * Convert a given message to a matrix
     * @version 1.0
     * @return a matrix with redundant bytes on the sides
     */
    public int[][] convertMessageToMatrix(int[] messageWithRedundantBytes, int numberOfRows, int numberOfColumns) {
        int[][] matrix = new int[numberOfRows + 1][numberOfColumns + 1];

        for (int i = 0; i < numberOfRows; i++) {
            if (numberOfColumns >= 0) System.arraycopy(messageWithRedundantBytes,
                    i * numberOfRows, matrix[i], 0, numberOfColumns);
        }

        for (int i = 0; i < numberOfRows; i++) {
            matrix[i][numberOfColumns] = messageWithRedundantBytes[numberOfRows * numberOfColumns + i];
        }

        for (int i = 0; i < numberOfColumns; i++) {
            matrix[numberOfRows][i] = messageWithRedundantBytes[numberOfRows * numberOfColumns + numberOfRows + i];
        }

        matrix[numberOfRows][numberOfColumns] = messageWithRedundantBytes[messageWithRedundantBytes.length - 1];

        return matrix;
    }

    /**
     * @version 1.0
     * @return redundant bytes from the sides of matrix
     */
    public int[] getRedundantBytesFromMatrix(int[][] matrix, int numberOfRows, int numberOfColumns) {
        int[] redundantBytes = new int[numberOfRows + numberOfColumns + 1];

        for (int i = 0; i < numberOfRows; i++) {
            redundantBytes[i] = matrix[i][numberOfColumns];
        }

        if (numberOfColumns >= 0)
            System.arraycopy(matrix[numberOfRows], 0, redundantBytes, numberOfRows, numberOfColumns);

        redundantBytes[redundantBytes.length - 1] = matrix[numberOfRows][numberOfColumns];

        return redundantBytes;
    }

    /**
     * Cut the last bytes of a given message and return these
     * @version 1.0
     * @return redundant bytes from the end of given message
     */
    public int[] getRedundantBytesFromMessage(int[] messageWithRedundantBytes, int numberOfRows, int numberOfColumns) {
        int[] redundantBytes = new int[numberOfRows + numberOfColumns + 1];

        System.arraycopy(messageWithRedundantBytes, numberOfRows * numberOfColumns,
                redundantBytes, 0, redundantBytes.length);

        return redundantBytes;
    }

    /**
     * Calculate redundant bytes on the base of a received message
     * @version 1.0
     * @return calculated redundant bytes
     */
    public int[] calculateRedundantBytes(int[] messageWithoutRedundantBytes, int numberOfRows, int numberOfColumns) {
        int[][] matrix = buildMatrix(messageWithoutRedundantBytes, numberOfRows, numberOfColumns);

        return getRedundantBytesFromMatrix(matrix, numberOfRows, numberOfColumns);
    }

    /**
     * Calculate a parity of the parities which have been calculated on the base of all bytes of the message
     * except the last byte that is the parity of parities
     * @version 1.0
     * @return a parity of the parities
     */
    private int calculateTotalSum(int[][] matrix) {
        int totalSum = 0;

        for (int i = 0; i < matrix.length; i++) {
            for(int j = 0; j < matrix[0].length; j++){
                if(i == matrix.length - 1 && j == matrix[0].length - 1){
                    break;
                }
                totalSum += matrix[i][j];
            }
        }

        return totalSum % 2;
    }

    /**
     * Calculate a syndrome that consists an error state which will be used to calculate recovery bytes
     * @version 1.0
     * @return a syndrome of message
     */
    public int[] getSyndrome(int[] redundantBytes, int[] calculatedRedundantBytes, int[] message,
                             int numberOfRows, int numberOfColumns) {
        int[] syndrome = new int[redundantBytes.length];

        for (int i = 0; i < syndrome.length; i++) {
            syndrome[i] = redundantBytes[i] ^ calculatedRedundantBytes[i];
        }

        syndrome[syndrome.length - 1] = calculateTotalSum(convertMessageToMatrix(message, numberOfRows, numberOfColumns))
                ^ calculatedRedundantBytes[calculatedRedundantBytes.length - 1];

        return syndrome;
    }

    /**
     * Cut the last bytes of a message which contains redundant bytes
     * @version 1.0
     * @return a message without redundant bytes
     */
    public int[] getMessageWithoutRedundantBytes(int[] messageWithRedundantBytes,
                                                 int numberOfRows, int numberOfColumns) {
        return Arrays.copyOfRange(messageWithRedundantBytes, 0, numberOfRows * numberOfColumns);
    }

    /**
     * Calculate on the base of a given syndrome recovery bytes which will be used to recovery a received message
     * @version 1.0
     * @return recovery bytes
     */
    public int[] getRecoveryBytes(int[] syndrome, int numberOfRows, int numberOfColumns) {
        if (syndrome[syndrome.length - 1] == 1) {
            printUtil.println("there is a need to resend");
            return new int[0];
        }

        int[] recoveryBytes = new int[numberOfRows * numberOfColumns];

        for (int i = 0; i < numberOfRows; i++) {
            for (int j = 0; j < numberOfColumns; j++) {
                if (syndrome[i] == 1 && syndrome[numberOfRows + j] == 1) {
                    recoveryBytes[i * numberOfRows + j] = 1;
                }
            }
        }

        return recoveryBytes;
    }

    /**
     * Recover a given message by a XOR operation using the recovery bytes
     * @version 1.0
     */
    public void recoverMessage(int[] message, int[] recoveryBytes){
        for (int i = 0; i < recoveryBytes.length && i < message.length; i++) {
            message[i] ^= recoveryBytes[i];
        }
    }
}