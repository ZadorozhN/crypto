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
     * @version 1.0
     * @return a encoded message
     */
    public int[] encode(int numberOfInformationBytes, int codeWordLength, int[] polynomial, int[] generatingPolynomial) {
        int numberOfRedundantBytes = codeWordLength - numberOfInformationBytes;
        int[] mainPolynomial = new int[numberOfInformationBytes + numberOfRedundantBytes];

        for (int i = 0; i < numberOfInformationBytes; i++) {
            mainPolynomial[i] = polynomial[i];
        }

        int[] redundantBytes = trimPolyZero(polynomialDivision(mainPolynomial, generatingPolynomial));

        int[] encodedMessage = new int[polynomial.length + redundantBytes.length];

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
     * @version 1.0
     * @return a generating matrix
     */
    public int[][] getGeneratingMatrix(int numberOfInformationBytes, int codeWordLength, int[] generatingPolynomial) {
        int[] mainPolynomial = new int[codeWordLength];
        mainPolynomial[0] = 1;
        mainPolynomial[codeWordLength - 1] = 1;

        int[][] generatingMatrix = new int[numberOfInformationBytes][codeWordLength];

        for (int i = 0; i < numberOfInformationBytes; i++) {
            for (int j = 0; j < numberOfInformationBytes; j++) {
                generatingMatrix[i][j + i] = generatingPolynomial[j];
            }
        }

        formatGeneratingMatrix(generatingMatrix);

        return generatingMatrix;
    }

    /**
     * Calculate a syndrome on the base of a encoded message and a generating polynomial
     * @version 1.0
     * @return a syndrome
     */
    public int[] calculateSyndrome(int[] encodedMessage, int[] generatingPolynomial) {
        return getRedundantBytes(polynomialDivision(encodedMessage, generatingPolynomial), generatingPolynomial.length);
    }


    /**
     * Cut the information bytes in a given message
     * @version 1.0
     * @return information bytes
     */
    public int[] getInformationBytes(int[] encodedMessage, int numberOfInformationBytes){
        return Arrays.copyOfRange(encodedMessage, 0, numberOfInformationBytes);
    }

    /**
     * Cut the redundant bytes in a given message
     * @version 1.0
     * @return redundant bytes
     */
    public int[] getRedundantBytes(int[] encodedMessage, int k){
        return Arrays.copyOfRange(encodedMessage, k, encodedMessage.length);
    }

    /**
     * Calculate on the base of a given syndrome recovery bytes which will be used to recovery a received message
     * @version 1.0
     * @return recovery bytes
     */
    public int[] getRecoveryBytes(int[][] generatingMatrix, int[] syndrome, int k){
        int[] recoveryBytes = new int[generatingMatrix[0].length];

        for (int i = 0; i < generatingMatrix.length; i++) {
            for (int j = 0; j < syndrome.length; j++) {
                if (generatingMatrix[i][k+j] != syndrome[j]){
                    break;
                } else if(j == syndrome.length - 1){
                    recoveryBytes[i] = 1;
                    return recoveryBytes;
                }
            }
        }

        return recoveryBytes;
    }

    /**
     * Recover a given message by a XOR operation using the recovery bytes
     * @version 1.0
     */
    public void recoverMessage(int[] encodedMessage, int[] recoverySequence){
        matrixUtil.rowSum(encodedMessage, recoverySequence);
    }

    /**
     * Format a given generating matrix to the canon view
     * @version 1.0
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
     * @version 1.0
     * @return a rest of division
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
     * @version 1.0
     * @return a max degree of polynomial elements
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
     * @version 1.0
     * @return a number of zeros till the any one in the start of message
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
     * @version 1.0
     * @return a trimmed poly
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