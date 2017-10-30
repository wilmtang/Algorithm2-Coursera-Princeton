import edu.princeton.cs.algs4.*;
import edu.princeton.cs.algs4.In;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WordNet {
    private HashMap<String, List<Integer>> word2id = new HashMap<>(); // map from noun to its ids.
    private HashMap<Integer, List<String>> id2word = new HashMap<>(); // map from id to nouns.
    private SAP sap;

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        if (synsets == null || hypernyms == null) {
            throw new java.lang.IllegalArgumentException();
        }

        In in = new In(synsets);
        String str = null;

        int V = 0;
        while ((str = in.readLine()) != null) {
            V++;
            String[] tok = str.split(",");
            String[] synset = tok[1].split(" ");
            int id = Integer.parseInt(tok[0]);
            for (String s : synset) {
                word2id.computeIfAbsent(s, ignored -> new ArrayList<Integer>()).add(id);
                id2word.computeIfAbsent(id, ignored -> new ArrayList<String>()).add(s);
            }
        }
        in.close();

        in = new In(hypernyms);
        Digraph graph = new Digraph(V);
        while ((str = in.readLine()) != null) {
            String[] tok = str.split(",");
            int v = Integer.parseInt(tok[0]);
            for (int i = 1; i < tok.length; i++) {
                graph.addEdge(v, Integer.parseInt(tok[i]));
            }
        }

        DirectedCycle directedCycle = new DirectedCycle(graph);
        if (directedCycle.hasCycle()) {
            throw new java.lang.IllegalArgumentException();
        }
        int root = 0;
        for (int i = 0; i < V && root <= 1; i++) {
            if (graph.outdegree(i) == 0) {
                root++;
            }
        }
        if (root > 1) {
            throw new IllegalArgumentException();
        }

        this.sap = new SAP(graph);
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return word2id.keySet();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null) {
            throw new IllegalArgumentException();
        }
        return word2id.containsKey(word);
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB)) {
            throw new IllegalArgumentException();
        }
        return sap.length(word2id.get(nounA), word2id.get(nounB));
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB)) {
            throw new IllegalArgumentException();
        }
        int ancestorId = sap.ancestor(word2id.get(nounA), word2id.get(nounB));
        return String.join(" ", id2word.get(ancestorId));
    }

    // do unit testing of this class
    public static void main(String[] args) {
        String str = null;
        In in = new In("./wordnet/digraph2.txt");
        Digraph graph = new Digraph(in);
        System.out.println("tets");
    }
}
