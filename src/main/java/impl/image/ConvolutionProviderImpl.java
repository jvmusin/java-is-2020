package impl.image;

import api.image.ConvolutionProvider;

import java.awt.*;

public class ConvolutionProviderImpl implements ConvolutionProvider {
    private static boolean isInside(Color[][] a, int row, int column) {
        int n = a.length;
        int m = a[0].length;
        return 0 <= row && row < n && 0 <= column && column < m;
    }

    private static Color apply(Color[][] image, int row, int column, double[][] kernel) {
        int radius = kernel.length / 2;
        int red = 0;
        int green = 0;
        int blue = 0;
        for (int i = 0; i < kernel.length; i++) {
            for (int j = 0; j < kernel[0].length; j++) {
                int x = row + i - radius;
                int y = column + j - radius;
                if (isInside(image, x, y)) {
                    Color c = image[x][y];
                    double k = kernel[i][j];
                    red += c.getRed() * k;
                    green += c.getGreen() * k;
                    blue += c.getBlue() * k;
                }
            }
        }
        return new Color(Math.min(red, 255), Math.min(green, 255), Math.min(blue, 255));
    }

    @Override
    public Color[][] apply(Color[][] image, double[][] kernel) {
        int n = image.length;
        int m = image[0].length;
        Color[][] result = new Color[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                result[i][j] = apply(image, i, j, kernel);
            }
        }
        return result;
    }
}
