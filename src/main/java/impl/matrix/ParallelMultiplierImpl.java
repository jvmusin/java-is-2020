package impl.matrix;

import api.matrix.ParallelMultiplier;

public class ParallelMultiplierImpl implements ParallelMultiplier {

    private static double calculateCell(double[][] a, double[][] b, int row, int column) {
        int k = b.length;
        double res = 0;
        for (int i = 0; i < k; i++) res += a[row][i] * b[i][column];
        return res;
    }

    private final ThreadPool threadPool;

    public ParallelMultiplierImpl(int maxThreadsCount) {
        threadPool = new ThreadPool(maxThreadsCount);
    }

    @Override
    public double[][] mul(double[][] a, double[][] b) {
        int n = a.length;
        int m = b[0].length;
        double[][] result = new double[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                int row = i;
                int column = j;
                threadPool.addTask(() -> result[row][column] = calculateCell(a, b, row, column));
            }
        }
        threadPool.run();
        return result;
    }
}
