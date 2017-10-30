import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Digraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

@SuppressWarnings("ALL")
public class SAP {

    private Digraph graph;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        if (G == null) {
            throw new IllegalArgumentException();
        }
        graph = new Digraph(G);
    }

    private void validIndex(int i) {
        if (i < 0 || i >= graph.V()) {
            throw new IllegalArgumentException();
        }
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        validIndex(v);
        validIndex(w);

        ArrayList<Integer> tmp1 = new ArrayList<>();
        tmp1.add(v);
        ArrayList<Integer> tmp2 = new ArrayList<>();
        tmp2.add(w);

        return length(tmp1, tmp2);
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        validIndex(v);
        validIndex(w);

        ArrayList<Integer> tmp1 = new ArrayList<>();
        tmp1.add(v);
        ArrayList<Integer> tmp2 = new ArrayList<>();
        tmp2.add(w);
        return ancestor(tmp1, tmp2);
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null) {
            throw new IllegalArgumentException();
        }

        for (Integer i : v) {
            validIndex(i);
        }
        for (Integer i : w) {
            validIndex(i);
        }

        return bfs(v, w)[0];
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null) {
            throw new IllegalArgumentException();
        }

        for (Integer i : v) {
            validIndex(i);
        }
        for (Integer i : w) {
            validIndex(i);
        }

        return bfs(v, w)[1];
    }

    /**
     * Using bidirectional bfs search, using
     *
     * @param v
     * @param w
     * @return first element if length, second is ancestor.
     */
    private int[] bfs(Iterable<Integer> v, Iterable<Integer> w) {
        Queue<Integer> queue1 = new LinkedList<>();
        Queue<Integer> queue2 = new LinkedList<>();

        // Maps from vertex to smallest depth bfs from starting points.
        HashMap<Integer, Integer> map1 = new HashMap<>();
        HashMap<Integer, Integer> map2 = new HashMap<>();

        for (int i : v) {
            queue1.add(i);
        }
        for (int i : w) {
            queue2.add(i);
        }

        int depth1 = 0, depth2 = 0;

        int[] res = new int[]{Integer.MAX_VALUE, -1};
        int remainingSteps = -1;
        while ((!queue1.isEmpty() || !queue2.isEmpty()) && (remainingSteps == -1 || remainingSteps > 0)) {
            // find adj of q1 elements.
            Queue<Integer> tmpQ1 = new LinkedList<>();
            while (!queue1.isEmpty()) {
                int node = queue1.poll();
                if (map2.containsKey(node)) {
                    int newLength = depth1 + map2.get(node);
                    if (newLength < res[0]) {
                        res[0] = newLength;
                        res[1] = node;
                        remainingSteps = Integer.max(depth1, map2.get(node));
                    }
                }
                // prepare for next iteration
                for (int vertex : graph.adj(node)) {
                    if (!map1.containsKey(vertex)) {
                        tmpQ1.add(vertex);
                    }
                }
                map1.put(node, depth1);
            }
            queue1 = tmpQ1;
            depth1++;

            // q2 elements;
            Queue<Integer> tmpQ2 = new LinkedList<>();
            while (!queue2.isEmpty()) {
                int node = queue2.poll();
                if (map1.containsKey(node)) {
                    int newLength = map1.get(node) + depth1;
                    if (newLength < res[0]) {
                        res[0] = newLength;
                        res[1] = node;
                        remainingSteps = Integer.max(map1.get(node), depth2);
                    }
                }
                // prepare for next iteration
                for (int vertex : graph.adj(node)) {
                    if (!map2.containsKey(vertex)) {
                        tmpQ2.add(vertex);
                    }
                }
                map2.put(node, depth2);
            }
            queue2 = tmpQ2;
            depth2++;

            if (remainingSteps > 0) {
                remainingSteps--;
            }
        }
        if (res[0] == Integer.MAX_VALUE) {
            return new int[]{-1, -1};
        } else {
            return res;
        }

    }

    // do unit testing of this class
    public static void main(String[] args) {
        String s = "./wordnet/digraph3.txt";
        In in = new In(s);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        int v = 10;
        int w = 7;

        int length = sap.length(v, w);
        int ancestor = sap.ancestor(v, w);
        StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
    }
}