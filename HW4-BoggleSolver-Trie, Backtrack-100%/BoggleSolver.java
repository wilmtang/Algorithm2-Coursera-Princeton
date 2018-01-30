/* Similar problem as https://leetcode.com/problems/word-search-ii/description */

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.HashSet;

public class BoggleSolver {
    private final Trie trie;
    private static final int[][] dirs = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1},
            {0, 1}, {1, -1}, {1, 0}, {1, 1}};

    // Initializes the data structure using the given array of strings as the dictionary.
    // (You can assume each word in the dictionary contains only the uppercase letters A through Z.)
    public BoggleSolver(String[] dictionary) {
        trie = new Trie();
        for (String word : dictionary) {
            if (word.length() >= 3) {
                trie.insert(word);
            }
        }
    }

    // Returns the set of all valid words in the given Boggle board, as an Iterable.
    public Iterable<String> getAllValidWords(BoggleBoard board) {
        HashSet<String> res = new HashSet<>();
        boolean[][] visited = new boolean[board.rows()][board.cols()];

        for (int i = 0; i < board.rows(); i++) {
            for (int j = 0; j < board.cols(); j++) {
                backTrack(board, trie.root, i, j, visited, res);
            }
        }
        return res;
    }

    private void backTrack(BoggleBoard board, TrieNode node, int i, int j, boolean[][] visited, HashSet<String> res) {
        if (i < 0 || j < 0 || i >= board.rows() || j >= board.cols()) {
            return;
        }

        if (visited[i][j]) {
            return;
        }
        char letter = board.getLetter(i, j);

        // For Q, get Q and then get U
        if (letter == 'Q') {
            node = node.get(letter);
            if (node != null) {
                node = node.get('U');
            } else {
                return;
            }
        } else {
            node = node.get(letter);
        }

        if (node == null) {
            return;
        }

        if (node.isEnd()) {
            res.add(node.word);
        }

        // Meat
        visited[i][j] = true;

        for (int[] dir : dirs) {
            backTrack(board, node, i + dir[0], j + dir[1], visited, res);
        }

        visited[i][j] = false;
    }

    // Returns the score of the given word if it is in the dictionary, zero otherwise.
    // (You can assume the word contains only the uppercase letters A through Z.)
    public int scoreOf(String word) {
        if (trie.search(word)) {
            return wordLenToScore(word);
        } else {
            return 0;
        }
    }

    private class TrieNode {
        private final static int R = 26;

        private TrieNode links[];
        private String word;

        public TrieNode() {
            this.links = new TrieNode[R];
        }

        public boolean containsKey(char ch) {
            return links[ch - 'A'] != null;
        }

        public TrieNode get(char ch) {
            return links[ch - 'A'];
        }

        public void put(char ch, TrieNode node) {
            links[ch - 'A'] = node;
        }

        public boolean isEnd() {
            return word != null;
        }

        public void setEnd(String word) {
            this.word = word;
        }
    }

    private class Trie {
        private final TrieNode root;

        public Trie() {
            root = new TrieNode();
        }

        public void insert(String word) {
            TrieNode node = root;
            for (int i = 0; i < word.length(); i++) {
                char curChar = word.charAt(i);
                if (!node.containsKey(curChar)) {
                    node.put(curChar, new TrieNode());
                }
                node = node.get(curChar);
            }
            node.setEnd(word);
        }

        // only search a whole word, if presented, return true, else return false.
        public boolean search(String word) {
            TrieNode node = searchPrefix(word);
            return node != null && node.isEnd();

        }

        // search a prefix or whole key in trie and
        // returns the node where search ends' next.
        // Time complexity : O(m) In each step of the algorithm we search for the next key character. In the worst
        // case the algorithm performs mm operations.
        TrieNode searchPrefix(String word) {
            TrieNode node = root;
            for (int i = 0; i < word.length(); i++) {
                char curChar = word.charAt(i);
                if (node.containsKey(curChar)) {
                    node = node.get(curChar);
                } else {
                    return null;
                }
            }
            return node;
        }
    }

    private int wordLenToScore(String word) {
        switch (word.length()) {
            case 0:
            case 1:
            case 2:
                return 0;
            case 3:
            case 4:
                return 1;
            case 5:
                return 2;
            case 6:
                return 3;
            case 7:
                return 5;
            default:
                return 11;
        }
    }

    public static void main(String[] args) {
        // In in = new In("./boggle/dictionary-algs4.txt");
        // String[] dictionary = in.readAllStrings();
        // BoggleSolver solver = new BoggleSolver(dictionary);
        // BoggleBoard board = new BoggleBoard("./boggle/board-q.txt");
        // int score = 0;
        // for (String word : solver.getAllValidWords(board)) {
        //     StdOut.println(word);
        //     score += solver.scoreOf(word);
        // }
        // StdOut.println("Score = " + score);

        In in = new In(args[0]);
        String[] dictionary = in.readAllStrings();
        BoggleSolver solver = new BoggleSolver(dictionary);
        BoggleBoard board = new BoggleBoard(args[1]);
        int score = 0;
        for (String word : solver.getAllValidWords(board)) {
            StdOut.println(word);
            score += solver.scoreOf(word);
        }
        StdOut.println("Score = " + score);
    }
}