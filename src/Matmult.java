import java.util.ArrayList;
import java.util.List;

public class Matmult {
    public static double[][] multScalar(double[][] matrix, double scalar) {
        double[][] result = new double[matrix.length][matrix[0].length];
        int i = 0;
        for (double[] row : matrix) {
            double[] newRow = new double[row.length];
            for (int j = 0; j < row.length; j++) {
                newRow[j] = row[j] * scalar;
            }
            result[i] = newRow;
            i++;
        }
        return result;
    }

    public static List<List<Double>> multMatrix(List<List<Double>> matrix1, double[][] matrix2) {
        List<List<Double>> result = new ArrayList<>();
        for (int i = 0; i < matrix1.size(); i++) {
            List<Double> newRow = new ArrayList<>();
            for (int j = 0; j < matrix2[0].length; j++) {
                newRow.add(0.0);
            }
            result.add(newRow);
        }
        for (int i = 0; i < matrix1.size(); i++) {
            for (int j = 0; j < matrix2[0].length; j++) {
                for (int k = 0; k < matrix2.length; k++) {
                    result.get(i).set(j, result.get(i).get(j) + matrix1.get(i).get(k) * matrix2[k][j]);
                }
            }
        }
        return result;
    }

    public static double euclideanDistance(List<List<Double>> matrix1, List<List<Double>> matrix2) {
        double sum = 0;
        for (int i = 0; i < matrix1.get(0).size(); i++) {
            sum += Math.pow(matrix1.get(0).get(i) - matrix2.get(0).get(i), 2);
        }
        return Math.sqrt(sum);
    }
}
