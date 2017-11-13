import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.StdIn;

import java.util.HashMap;

public class Outcast {

    private final WordNet wordnet;

    // constructor takes a WordNet object
    public Outcast(WordNet wordnet) {
        this.wordnet = wordnet;
    }

    // given an array of WordNet nouns, return an outcast
    public String outcast(String[] nouns) {
        if (nouns == null) {
            throw new IllegalArgumentException();
        }

        int length = nouns.length;
        int[][] dist = new int[length][length];

        for (int i = 0; i < length; i++) {
            for (int j = i; j < length; j++) {
                if (i == j) {
                    dist[i][j] = 0;
                } else {
                    int tmp = wordnet.distance(nouns[i], nouns[j]);
                    dist[i][j] = dist[j][i] = tmp;
                }
            }
        }

        int index = -1;
        int maxDist = -1;
        for (int i = 0; i < length; i++) {
            int curDist = 0;
            for (int j = 0; j < length; j++) {
                curDist += dist[i][j];
            }

            if (curDist > maxDist) {
                maxDist = curDist;
                index = i;
            }

        }

        return nouns[index];
    }

    // see test client below
    public static void main(String[] args) {
        WordNet wordnet = new WordNet("./wordnet/" + args[0], "./wordnet/" + args[1]);
        Outcast outcast = new Outcast(wordnet);
        for (int t = 2; t < args.length; t++) {
            In in = new In("./wordnet/" + args[t]);
            String[] nouns = in.readAllStrings();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));
        }
    }
}
