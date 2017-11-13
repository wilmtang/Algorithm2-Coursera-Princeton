import edu.princeton.cs.algs4.Picture;

import java.awt.Color;


public class SeamCarver {
    private static final int BORDER_ENERGY = 1000;
    private Color[][] colorsMatrix;
    private double[][] energyMatrix;
    private double[][] energyMatrixT; // transposed energyMatrix
    private int height, width;

    private Picture picture;
    private boolean isPicObsolete = false;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new java.lang.IllegalArgumentException();
        }
        this.picture = picture;

        height = picture.height();
        width = picture.width();

        colorsMatrix = new Color[height][width];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                colorsMatrix[row][col] = picture.get(col, row);
            }
        }
        getEnergyMatrix();
    }

    // current picture
    public Picture picture() {
        if (isPicObsolete) {
            picture = new Picture(width, height);
            for (int col = 0; col < width; col++) {
                for (int row = 0; row < height; row++) {
                    picture.set(col, row, colorsMatrix[row][col]);
                }
            }
            isPicObsolete = false;
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
    public double energy(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new java.lang.IllegalArgumentException();
        }

        return energyMatrix[y][x];
    }

    private void getEnergyMatrix() {
        energyMatrix = new double[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (row == 0 || col == 0 || row == height - 1 || col == width - 1) {
                    energyMatrix[row][col] = BORDER_ENERGY;
                    continue;
                }
                Color l = colorsMatrix[row][col - 1];
                Color r = colorsMatrix[row][col + 1];
                double deltaX2 = colorSquareDiff(l, r);

                Color u = colorsMatrix[row - 1][col];
                Color d = colorsMatrix[row + 1][col];
                double deltaY2 = colorSquareDiff(u, d);
                energyMatrix[row][col] = Math.sqrt(deltaX2 + deltaY2);
            }
        }
        getEnergyTransposed();
    }

    private void getEnergyTransposed() {
        energyMatrixT = new double[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                energyMatrixT[i][j] = energyMatrix[j][i];
            }
        }
    }

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
        return findSeam(energyMatrixT, false);
    }


    // sequence of indices for vertical seam
    // using topological sort to find shortest path
    public int[] findVerticalSeam() {
        return findSeam(energyMatrix, true);
    }

    // using topological sort to find shortest path
    private int[] findSeam(double[][] energyMatrix, boolean isVertical) {
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
                    distTo[j] = energyMatrix[i][j];
                    parent[i][j] = -1;
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
                        distTo[j] = prevDistTo[j] + energyMatrix[i][j];
                    } else {
                        parent[i][j] = theOther;
                        distTo[j] = prevDistTo[theOther] + energyMatrix[i][j];
                    }
                    continue;
                }

                // three ways to [i][j]
                double minValue = Double.MAX_VALUE;
                for (int k = j - 1; k <= j + 1; k++) {
                    if (prevDistTo[k] < minValue) {
                        minValue = prevDistTo[k];
                        distTo[j] = prevDistTo[k] + energyMatrix[i][j];
                        parent[i][j] = k;
                    }
                }
                distTo[j] = minValue + energyMatrix[i][j];
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

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (seam == null || seam.length != width) {
            throw new java.lang.IllegalArgumentException();
        }
        for (int col = 0; col < width; col++) {
            for (int row = seam[col]; row < height - 1; row++) {
                colorsMatrix[row][col] = colorsMatrix[row + 1][col];
            }
        }
        height--;

        getEnergyMatrix();
        isPicObsolete = true;
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (seam == null || seam.length != height) {
            throw new java.lang.IllegalArgumentException();
        }
        for (int i = 0; i < height; i++) {
            System.arraycopy(colorsMatrix[i], seam[i] + 1, colorsMatrix[i], seam[i], width - seam[i] - 1);
        }
        width--;

        getEnergyMatrix();
        isPicObsolete = true;
    }

    public static void main(String[] args) {
        Picture picture = new Picture("./seam/6x5.png");

    }
}
