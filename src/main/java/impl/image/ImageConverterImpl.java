package impl.image;

import api.image.ImageConverter;

import java.awt.*;

public class ImageConverterImpl implements ImageConverter {
    @Override
    public Color[][] convertToColor(int[][] image) {
        int n = image.length;
        int m = image[0].length;
        Color[][] result = new Color[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                result[i][j] = new Color(image[i][j], true);
            }
        }
        return result;
    }

    @Override
    public int[][] convertToRgb(Color[][] image) {
        int n = image.length;
        int m = image[0].length;
        int[][] result = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                result[i][j] = image[i][j].getRGB();
            }
        }
        return result;
    }
}
