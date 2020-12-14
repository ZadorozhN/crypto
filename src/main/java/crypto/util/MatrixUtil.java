package crypto.util;

public class MatrixUtil {

    /**
     * Swap values of given arrays of any matrix
     *
     * @since 1.0
     */
    public void swapTwoRows(int[] row1, int[] row2) {
        for (int i = 0; i < row1.length; i++) {
            int buf = row1[i];
            row1[i] = row2[i];
            row2[i] = buf;
        }
    }

    /**
     * Sum values of given arrays of any matrix and save the result as values of first array
     *
     * @since 1.0
     */
    public void rowSum(int[] row1, int[] row2) {
        if (row1.length != row2.length) {
            return;
        }
        for (int i = 0; i < row1.length; i++) {
            row1[i] += row2[i];
            row1[i] %= 2;
        }
    }


    /**
     * Sort rows of matrix by the alphabet.
     *
     * @since 1.0
     */
    public void sortMatrixByAlphabet(int[][] matrix) {
        int lengthOfWord = matrix.length;

        for (int i = 0; i < lengthOfWord - 1; i++) {
            for (int j = 0; j < lengthOfWord - 1; j++) {
                if (matrix[j][0] > matrix[j + 1][0]) {
                    swapTwoRows(matrix[j], matrix[j + 1]);
                } else if (matrix[j][0] == matrix[j + 1][0]) {
                    for (int k = 1; k < lengthOfWord; k++) {
                        if (matrix[j][k] > matrix[j + 1][k]) {
                            swapTwoRows(matrix[j], matrix[j + 1]);
                            break;
                        } else if (matrix[j][k] < matrix[j + 1][k]) {
                            break;
                        }
                    }
                }
            }
        }
    }
}