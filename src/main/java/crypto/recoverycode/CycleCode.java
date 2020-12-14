package crypto.recoverycode;

import crypto.util.MatrixUtil;
import crypto.util.PrintUtil;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.stream.IntStream;

public class CycleCode {

    private final PrintUtil printUtil;
    private final MatrixUtil matrixUtil;

    public CycleCode(PrintStream stream) {
        this.printUtil = new PrintUtil(stream);
        this.matrixUtil = new MatrixUtil();
    }

    /**
     * Encode a given message by the cycle code way with using the generating polynomial
     *
     * @return a encoded message
     * @since 1.0
     */
    public int[] encode(int numberOfInformationBytes, int codeWordLength, int[] polynomial, int[] generatingPolynomial) {
        int[] mainPolynomial = new int[codeWordLength];

        for (int i = 0; i < numberOfInformationBytes; i++) {
            mainPolynomial[i] = polynomial[i];
        }

        int[] redundantBytes = new int[codeWordLength - numberOfInformationBytes];
        int[] rest = trimPolyZero(polynomialDivision(mainPolynomial, generatingPolynomial));
        int startPosition = redundantBytes.length - rest.length;

        System.arraycopy(rest, 0, redundantBytes, startPosition, rest.length);

        int[] encodedMessage = new int[codeWordLength];

        for (int i = 0; i < encodedMessage.length; i++) {
            if (i < polynomial.length) {
                encodedMessage[i] = polynomial[i];
            } else {
                encodedMessage[i] = redundantBytes[i - polynomial.length];
            }
        }

        return encodedMessage;
    }

    /**
     * Generate a matrix on the base of a generating polynomial by the shift way
     *
     * @return a generating matrix
     * @since 1.0
     */
    public int[][] getGeneratingMatrix(int numberOfInformationBytes, int codeWordLength, int[] generatingPolynomial) {
        int[][] generatingMatrix = new int[numberOfInformationBytes][codeWordLength];

        for (int i = 0; i < numberOfInformationBytes; i++) {
            for (int j = 0; j < codeWordLength - numberOfInformationBytes + 1; j++) {
                generatingMatrix[i][j + i] = generatingPolynomial[j];
            }
        }

        formatGeneratingMatrix(generatingMatrix);

        return generatingMatrix;
    }

    /**
     * Calculate a syndrome on the base of a encoded message and a generating polynomial
     *
     * @return a syndrome
     * @since 1.0
     */
    public int[] calculateSyndrome(int[] encodedMessage, int[] generatingPolynomial, int numberOfInformationBytes) {
        return getRedundantBytes(polynomialDivision(encodedMessage, generatingPolynomial), numberOfInformationBytes);
    }


    /**
     * Cut the information bytes in a given message
     *
     * @return information bytes
     * @since 1.0
     */
    public int[] getInformationBytes(int[] encodedMessage, int numberOfInformationBytes) {
        return Arrays.copyOfRange(encodedMessage, 0, numberOfInformationBytes);
    }

    /**
     * Cut the redundant bytes in a given message
     *
     * @return redundant bytes
     * @since 1.0
     */
    public int[] getRedundantBytes(int[] encodedMessage, int numberOfInformationBytes) {
        return Arrays.copyOfRange(encodedMessage, numberOfInformationBytes, encodedMessage.length);
    }

    /**
     * Calculate on the base of a given syndrome recovery bytes which will be used to recovery a received message
     *
     * @return recovery bytes
     * @since 1.0
     */
    public int[] getRecoveryBytes(int[][] generatingMatrix, int[] syndrome, int numberOfInformationBytes) {
        int[] recoveryBytes = new int[generatingMatrix[0].length];

        for (int i = 0; i < generatingMatrix.length; i++) {
            for (int j = 0; j < syndrome.length; j++) {
                if (generatingMatrix[i][numberOfInformationBytes + j] != syndrome[j]) {
                    break;
                } else if (j == syndrome.length - 1) {
                    recoveryBytes[i] = 1;
                    return recoveryBytes;
                }
            }
        }

        System.arraycopy(syndrome, 0, recoveryBytes, numberOfInformationBytes, syndrome.length);

        return recoveryBytes;
    }

    /**
     * Recover a given message by a XOR operation using the recovery bytes
     *
     * @since 1.0
     */
    public int[] recoverMessage(int[] encodedMessage, int[] recoverySequence) {
        int[] recoveredMessage = new int[encodedMessage.length];

        for (int i = 0; i < recoveredMessage.length; i++){
            recoveredMessage[i] = encodedMessage[i] ^ recoverySequence[i];
        }

        return recoveredMessage;
    }

    /**
     * Format a given generating matrix to the canon view
     *
     * @since 1.0
     */
    private void formatGeneratingMatrix(int[][] generatingMatrix) {
        for (int i = 0; i < generatingMatrix.length; i++) {
            for (int j = i + 1; j < generatingMatrix.length; j++) {
                if (generatingMatrix[i][j] == 1) {
                    matrixUtil.rowSum(generatingMatrix[i], generatingMatrix[j]);
                }
            }
        }
    }

    /**
     * Divide two polynomial on the base of a XOR operation
     *
     * @return a rest of division
     * @since 1.0
     */
    private int[] polynomialDivision(int[] firstPoly, int[] secondPoly) {
        int[] resultPoly = firstPoly.clone();
        int[] divPoly = new int[firstPoly.length];
        int degreeDifference = getPolyDegree(resultPoly) - getPolyDegree(secondPoly);

        while (degreeDifference >= 0) {
            IntStream.range(0, divPoly.length).forEach(i -> divPoly[i] = 0);

            for (int i = 0; i < secondPoly.length; i++) {
                divPoly[getPolyOffset(resultPoly) + i] = secondPoly[i];
            }

            printUtil.println().printArray(resultPoly).println()
                    .println("-")
                    .printArray(divPoly).println();

            for (int i = 0; i < divPoly.length; i++) {
                resultPoly[i] += divPoly[i];
                if (resultPoly[i] % 2 == 0) {
                    resultPoly[i] = 0;
                }
            }

            printUtil.printArray(resultPoly).println();

            degreeDifference = getPolyDegree(resultPoly) - getPolyDegree(secondPoly);
        }

        return resultPoly;
    }

    /**
     * @return a max degree of polynomial elements
     * @since 1.0
     */
    private int getPolyDegree(int[] polynomial) {
        for (int i = 0; i < polynomial.length; i++) {
            if (polynomial[i] == 1) {
                return polynomial.length - 1 - i;
            }
        }

        return 0;
    }

    /**
     * @return a number of zeros till the any one in the start of message
     * @since 1.0
     */
    private int getPolyOffset(int[] polynomial) {
        for (int i = 0; i < polynomial.length; i++) {
            if (polynomial[i] == 1) {
                return i;
            }
        }

        return polynomial.length - 1;
    }

    /**
     * Trim zeros from from the start of array
     *
     * @return a trimmed poly
     * @since 1.0
     */
    private int[] trimPolyZero(int[] polynomial) {
        int trimTo = 0;
        for (int i = 0; i < polynomial.length; i++) {
            if (polynomial[i] == 1) {
                trimTo = i;
                break;
            } else if (i == polynomial.length - 1) {
                return new int[]{0};
            }
        }

        int[] trimmedPoly = new int[polynomial.length - trimTo];

        for (int i = trimTo; i < polynomial.length; i++) {
            trimmedPoly[i - trimTo] = polynomial[i];
        }

        return trimmedPoly;
    }
}