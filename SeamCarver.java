import edu.princeton.cs.algs4.Picture;

import java.awt.Color;

public class SeamCarver {
    private static final int BORDER_ENERGY = 1000;
    private static final boolean HORIZONTAL = false;
    private static final boolean VERTICAL = true;

    // for full marks, need to use one single int to represent color to satisfy space requirement.
    // may refer to https://github.com/Ramin8or/algorithms/blob/4f92b7032ce61137041fe5807bc553ac38468ef8/7_seam_carver/SeamCarver.java
    // private int[][] pixels; // Alpha and RGB values packed in integers
    private Color[][] colorsMatrix;
    private int height, width;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new java.lang.IllegalArgumentException();
        }

        height = picture.height();
        width = picture.width();

        colorsMatrix = new Color[height][width];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                colorsMatrix[row][col] = picture.get(col, row);
            }
        }
    }

    // current picture
    public Picture picture() {
        Picture picture = new Picture(width, height);
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                picture.set(col, row, colorsMatrix[row][col]);
            }
        }
        return picture;
    }

    // width of current picture
    public int width() {
        return width;
    }

    // height of current picture
    public int height() {
        return height;
    }

    // energy of pixel at column x and row y
    public double energy(int col, int row) {
        if (col < 0 || col >= width || row < 0 || row >= height) {
            throw new java.lang.IllegalArgumentException();
        }

        if (row == 0 || col == 0 || row == height - 1 || col == width - 1) {
            return BORDER_ENERGY;
        }

        Color l = colorsMatrix[row][col - 1];
        Color r = colorsMatrix[row][col + 1];
        double deltaX2 = colorSquareDiff(l, r);

        Color u = colorsMatrix[row - 1][col];
        Color d = colorsMatrix[row + 1][col];
        double deltaY2 = colorSquareDiff(u, d);

        return Math.sqrt(deltaX2 + deltaY2);
    }


    private double getEnergy(int row, int col, boolean isVertical) {
        if (isVertical) {
            return energy(col, row);
        } else {
            return energy(row, col);
        }
    }

    // for to get energy for transposed energyMatrix
    // private double getEnergyT(int row, int col) {
    //     return energy(row, col);
    // }

    private double colorSquareDiff(Color c1, Color c2) {

        double cDiffR = c1.getRed() - c2.getRed();
        double cDiffG = c1.getGreen() - c2.getGreen();
        double cDiffB = c1.getBlue() - c2.getBlue();

        double res = Math.pow(cDiffR, 2);
        res += Math.pow(cDiffG, 2);
        res += Math.pow(cDiffB, 2);
        return res;
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        return findSeam(HORIZONTAL);
    }

    // sequence of indices for vertical seam
    // using topological sort to find shortest path
    public int[] findVerticalSeam() {
        return findSeam(VERTICAL);
    }

    // using topological sort to find shortest path
    private int[] findSeam(boolean isVertical) {
        int columns, rows;

        if (isVertical) {
            columns = this.width;
            rows = this.height;
        } else {
            columns = this.height;
            rows = this.width;
        }

        double[] distTo = new double[columns];
        double[] prevDistTo = new double[columns];
        int[][] parent = new int[rows][columns];

        // relax
        for (int i = 0; i < rows; i++) {
            // to get distTo[j]
            for (int j = 0; j < columns; j++) {
                // first line
                if (i == 0) {
                    distTo[j] = getEnergy(i, j, isVertical);
                    parent[i][j] = -1;
                    continue;
                }

                // deal with special case: only one line of pixel:
                if (columns == 1) {
                    distTo[j] = prevDistTo[j] + getEnergy(i, j, isVertical);
                    parent[i][j] = j;
                    continue;
                }

                // only two prev pixel - left border or right border.
                int theOther = -1;
                if (j == 0) {
                    // left border
                    theOther = j + 1;
                } else if (j == columns - 1) {
                    // right border
                    theOther = j - 1;
                }
                if (theOther != -1) {
                    if (prevDistTo[j] < prevDistTo[theOther]) {
                        parent[i][j] = j;
                        distTo[j] = prevDistTo[j] + getEnergy(i, j, isVertical);
                    } else {
                        parent[i][j] = theOther;
                        distTo[j] = prevDistTo[theOther] + getEnergy(i, j, isVertical);
                    }
                    continue;
                }

                // three ways to [i][j]
                double minValue = Double.MAX_VALUE;
                for (int k = j - 1; k <= j + 1; k++) {
                    if (prevDistTo[k] < minValue) {
                        minValue = prevDistTo[k];
                        parent[i][j] = k;
                    }
                }
                distTo[j] = minValue + getEnergy(i, j, isVertical);
            }
            double[] tmp = prevDistTo;
            prevDistTo = distTo;
            distTo = tmp;
        }

        // find min in the last row
        distTo = prevDistTo;
        double minValue = distTo[0];
        int minCol = 0;
        for (int i = 0; i < columns; i++) {
            if (distTo[i] < minValue) {
                minCol = i;
                minValue = distTo[i];
            }
        }

        // find seam path
        int[] seam = new int[rows];
        seam[rows - 1] = minCol;
        for (int i = rows - 1; i > 0; i--) {
            minCol = parent[i][minCol]; // minCol of row i-1
            seam[i - 1] = minCol;
        }
        return seam;
    }

    // invalid seams:
    // distance between adj pixel greater than 1

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        checkSeam(seam, VERTICAL);

        Color[][] newColors = new Color[height][width - 1];
        for (int i = 0; i < height; i++) {
            // System.arraycopy(colorsMatrix[i], seam[i] + 1, colorsMatrix[i], seam[i], width - seam[i] - 1);
            System.arraycopy(colorsMatrix[i], 0, newColors[i], 0, seam[i]);
            System.arraycopy(colorsMatrix[i], seam[i] + 1, newColors[i], seam[i], width - seam[i] - 1);
        }
        colorsMatrix = newColors;
        width--;
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        checkSeam(seam, HORIZONTAL);

        Color[][] newColors = new Color[height - 1][width];
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height - 1; row++) {
                if (row < seam[col]) {
                    newColors[row][col] = colorsMatrix[row][col];
                } else {
                    newColors[row][col] = colorsMatrix[row + 1][col];
                }
            }
        }
        colorsMatrix = newColors;
        height--;
    }

    private void checkSeam(int[] seam, boolean isVertical) {
        if (seam == null) {
            throw new java.lang.IllegalArgumentException();
        }

        // width of the picture is less than or equal to 1
        if (isVertical && (seam.length != height || width <= 1)) {
            throw new java.lang.IllegalArgumentException();
        }
        // height of the picture is less than or equal to 1
        if (!isVertical && (seam.length != width || height <= 1)) {
            throw new java.lang.IllegalArgumentException();
        }

        boolean shouldThrow = false;
        for (int i = 0; i < seam.length; i++) {
            // entry outside prescribed range
            if (isVertical) {
                if (seam[i] < 0 || seam[i] >= width) {
                    shouldThrow = true;
                    break;
                }
            } else {
                if (seam[i] < 0 || seam[i] >= height) {
                    shouldThrow = true;
                    break;
                }
            }

            // adjacent entries differ by more than 1
            if (i > 0) {
                if (Math.abs(seam[i] - seam[i - 1]) > 1) {
                    shouldThrow = true;
                    break;
                }
            }
        }

        if (shouldThrow) {
            throw new java.lang.IllegalArgumentException();
        }
    }
}
