package crypto.recoverycode;

import crypto.util.PrintUtil;

import java.io.PrintStream;

public final class HammingCode {

    private final PrintUtil printUtil;

    public HammingCode(PrintStream stream) {
        printUtil = new PrintUtil(stream);
    }

    /**
     * Generate a check matrix which will be used to encode any message by hamming way
     * @version 1.0
     * @return a check matrix
     */
    public int[][] getCheckMatrix(int numberOfInformationBytes, int numberOfRedundantBytes) {
        int[][] matrix = new int[numberOfRedundantBytes][numberOfInformationBytes + numberOfRedundantBytes];
        boolean key;
        int counter = 0;

        for (int i = 0; i < numberOfRedundantBytes; i++) {
            key = false;
            for (int j = 0; counter < numberOfInformationBytes; j++) {
                if (j / Math.pow(2, i) % 1 == 0) {
                    key = !key;
                }

                if(j == 0){
                    continue;
                } else if(Math.log(j) / Math.log(2) % 1 == 0){
                    continue;
                } else if(!key) {
                    matrix[i][counter] = 1;
                }
                counter++;
            }

            counter = 0;
            matrix[i][i + numberOfInformationBytes] = 1;
        }

        return matrix;
    }

    /**
     * Print a given message in the hamming code way
     * with using the separator between the information bytes and redundant bytes
     * @version 1.0
     */
    public void printEncodedMessage(int[] encodedMessage, int numberOfInformationBytes) {
        for (int i = 0; i < encodedMessage.length; i++) {
            if (i == numberOfInformationBytes) {
                printUtil.print("|");
            }
            printUtil.print(encodedMessage[i]);
        }
    }

    /**
     * Add to a given message redundant bytes on the base of a check matrix
     * @version 1.0
     * @return a encoded message
     */
    public int[] encodeMessage(int[][] checkMatrix, int[] message,
                               int numberOfInformationBytes, int numberOfRedundantBytes) {
        int codeWordLength = numberOfInformationBytes + numberOfRedundantBytes;
        int[] redundantBytes;
        int[] codeWordBytes = new int[codeWordLength];

        redundantBytes = calculateRedundantBytes(checkMatrix, message, numberOfInformationBytes, numberOfRedundantBytes);

        for (int i = 0; i < codeWordLength; i++) {
            if (i < numberOfInformationBytes) {
                codeWordBytes[i] = message[i];
            } else
                codeWordBytes[i] = redundantBytes[i - numberOfInformationBytes];
        }

        return codeWordBytes;
    }

    /**
     * Calculate redundant bytes on the base of a XOR operation
     * @version 1.0
     * @return redundant bytes
     */
    public int[] calculateRedundantBytes(int[][] checkMatrix, int[] message,
                                         int numberOfInformationBytes, int numberOfRedundantBytes) {
        int[] redundantBytes = new int[numberOfRedundantBytes];

        for (int i = 0; i < numberOfRedundantBytes; i++) {
            for (int j = 0; j < numberOfInformationBytes; j++) {
                redundantBytes[i] = (redundantBytes[i] + message[j] * checkMatrix[i][j]) % 2;
            }
        }

        return redundantBytes;
    }

    /**
     * Cut the last bytes of a given message which contains the redundant bytes
     * @version 1.0
     * @return redundant bytes
     */
    public int[] getRedundantBytes(int[] message, int numberOfInformationBytes, int numberOfRedundantBytes) {
        int[] redundantBytes = new int[numberOfRedundantBytes];

        if (numberOfRedundantBytes >= 0) System.arraycopy(message, numberOfInformationBytes,
                redundantBytes, 0, numberOfRedundantBytes);

        return redundantBytes;
    }

    /**
     * Calculate a syndrome that consists an error state which will be used to calculate recovery bytes
     * @version 1.0
     * @return a syndrome of message
     */
    public int[] getSyndrome(int[] redundantBytes, int[] calculatedRedundantBytes) {
        int[] syndrome = new int[redundantBytes.length];

        for (int i = 0; i < redundantBytes.length; i++) {
            syndrome[i] = redundantBytes[i] ^ calculatedRedundantBytes[i];
        }

        return syndrome;
    }

    /**
     * Calculate on the base of a given syndrome recovery bytes which will be used to recovery a received message
     * @version 1.0
     * @return recovery bytes
     */
    public int[] getRecoveryBytes(int[] syndrome, int[][] checkMatrix,
                                  int numberOfInformationBytes, int numberOfRedundantBytes) {
        int codeWordLength = numberOfInformationBytes + numberOfRedundantBytes;
        int[] recoveryBytes = new int[codeWordLength];
        boolean hasFound = false;
        for (int i = 0; i < codeWordLength && !hasFound; i++) {
            for (int j = 0; j < numberOfRedundantBytes; j++) {
                if (syndrome[j] == checkMatrix[j][i]) {
                    if (j == numberOfRedundantBytes - 1) {
                        hasFound = true;
                        recoveryBytes[i] = 1;
                    }
                } else {
                    break;
                }
            }
        }

        return recoveryBytes;
    }

    /**
     * Recover a given message by a XOR operation using the recovery bytes
     * @version 1.0
     */
    public int[] recoverMessage(int[] message, int[] recoveringBytes) {
        int[] recoveredMessage = new int[message.length];

        for (int i = 0; i < message.length; i++) {
            recoveredMessage[i] = (message[i] + recoveringBytes[i]) % 2;
        }

        return recoveredMessage;
    }
}

