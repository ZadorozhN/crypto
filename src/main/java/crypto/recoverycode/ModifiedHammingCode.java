package crypto.recoverycode;

import crypto.util.PrintUtil;

import java.io.PrintStream;

public class ModifiedHammingCode {

    private final PrintUtil printUtil;

    public ModifiedHammingCode(PrintStream stream) {
        this.printUtil = new PrintUtil(stream);
    }

    /**
     * Modify a given matrix and after this modifying there is a way to use this matrix in modified hamming encoding way.
     * Notice that number of redundant bytes have been incremented
     *
     * @return a modified check matrix
     * @since 1.0
     */
    public int[][] modifyCheckMatrix(int[][] checkMatrix) {
        int[][] modifiedCheckMatrix = new int[checkMatrix.length + 1][checkMatrix[0].length + 1];

        for (int i = 0; i < checkMatrix.length; i++) {
            System.arraycopy(checkMatrix[i], 0, modifiedCheckMatrix[i], 0, checkMatrix[i].length);
        }

        for (int i = 0; i < modifiedCheckMatrix[0].length; i++) {
            modifiedCheckMatrix[modifiedCheckMatrix.length - 1][i] = 1;
        }

        printUtil.printMatrix(modifiedCheckMatrix);
        sumMatrixColumn(modifiedCheckMatrix);

        return modifiedCheckMatrix;
    }

    /**
     * Sum all cell's values of column on the base of a XOR operation and place the result on the end of column
     *
     * @since 1.0
     */
    private void sumMatrixColumn(int[][] matrix) {
        for (int i = 0; i < matrix[0].length; i++) {
            int columnSum = 0;
            for (int[] ints : matrix) {
                columnSum += ints[i];
            }
            matrix[matrix.length - 1][i] = columnSum % 2;
        }
    }
}