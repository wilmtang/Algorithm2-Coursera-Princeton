import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.In;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BaseballElimination {
    private int[] w;
    private int[] l;
    private int[] r;
    private int[][] g;

    // maps from team name to id.
    private Map<String, Integer> map = new HashMap<>();

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        In in = new In(filename);
        int teams = in.readInt();

        w = new int[teams];
        l = new int[teams];
        r = new int[teams];
        g = new int[teams][teams];

        for (int i = 0; i < teams; i++) {
            map.put(in.readString(), i);
            w[i] = in.readInt();
            l[i] = in.readInt();
            r[i] = in.readInt();

            for (int j = 0; j < teams; j++) {
                g[i][j] = in.readInt();
            }
        }

    }

    // number of teams
    public int numberOfTeams() {
        return map.size();
    }

    // all teams
    public Iterable<String> teams() {
        return map.keySet();
    }

    // number of wins for given team
    public int wins(String team) {
        if (!map.containsKey(team)) {
            throw new java.lang.IllegalArgumentException();
        }

        return w[map.get(team)];
    }

    // number of losses for given team
    public int losses(String team) {
        if (!map.containsKey(team)) {
            throw new java.lang.IllegalArgumentException();
        }

        return l[map.get(team)];
    }

    // number of remaining games for given team
    public int remaining(String team) {
        if (!map.containsKey(team)) {
            throw new java.lang.IllegalArgumentException();
        }

        return r[map.get(team)];
    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        if (!map.containsKey(team1) || !map.containsKey(team2)) {
            throw new java.lang.IllegalArgumentException();
        }
        return g[map.get(team1)][map.get(team2)];
    }

    // is given team eliminated?
    public boolean isEliminated(String team) {
        if (!map.containsKey(team)) {
            throw new java.lang.IllegalArgumentException();
        }
        int reqId = map.get(team);
        if (trivialEliminatedCertificate(reqId) != null) {
            return true;
        }

        //
        // nonTrivial elimination.
        return nonTrivialEliminatedCertificate(reqId, false) != null;

    }

    // Non-negative if trivially eliminated, the returned value is the eliminating id. Negative if reqId cannot be
    // trivially eliminated.
    private String trivialEliminatedCertificate(int reqId) {
        for (String teamName : teams()) {
            int i = map.get(teamName);
            if (i != reqId) {
                if (w[i] > w[reqId] + r[reqId]) {
                    return teamName;
                }
            }
        }
        return null;
    }

    // return null if cannnot be non-trivially eliminated, or a list of certificate if can be eliminated.
    // must be checked that the graph cannot be trivially eliminated before this function is called.
    private List<String> nonTrivialEliminatedCertificate(int reqId, boolean needCertificate) {
        // System.out.println("enters nonTrivial!");
        int teamSum = numberOfTeams();
        int souceId = teamSum;
        int sinkId = teamSum + 1;

        int gameNodeId = teamSum + 2; // need to ++ for every node.

        int totalNodes = teamSum + 2 + (teamSum - 1) * (teamSum - 2) / 2;
        // This is C(2, n-1)
        FlowNetwork flowNetwork = new FlowNetwork(totalNodes);
        // iterate team nodes.
        for (int i = 0; i < teamSum; i++) {
            if (i == reqId) {
                continue;
            }
            // iterate game nodes.
            for (int j = i + 1; j < teamSum; j++) {
                if (j == reqId) {
                    continue;
                }
                // System.out.println("Game between" + i + " and " + j);
                flowNetwork.addEdge(new FlowEdge(souceId, gameNodeId, g[i][j]));
                flowNetwork.addEdge(new FlowEdge(gameNodeId, i, Double.POSITIVE_INFINITY));
                flowNetwork.addEdge(new FlowEdge(gameNodeId, j, Double.POSITIVE_INFINITY));
                gameNodeId++;
            }
            flowNetwork.addEdge(new FlowEdge(i, sinkId, w[reqId] + r[reqId] - w[i]));
        }

        boolean isEliFlag = false;

        FordFulkerson fordFulkerson = new FordFulkerson(flowNetwork, souceId, sinkId);
        for (FlowEdge flowEdge : flowNetwork.adj(souceId)) {
            if (flowEdge.flow() < flowEdge.capacity()) {
                isEliFlag = true;
                break;
            }
        }

        // find certificates
        if (isEliFlag) {
            LinkedList<String> res = new LinkedList<>();
            if (needCertificate) {
                for (String teamName : teams()) {
                    int id = map.get(teamName);
                    if (fordFulkerson.inCut(id)) {
                        res.add(teamName);
                    }
                }
            }
            return res;

        }
        return null;
    }

    // subset R of teams that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String team) {
        if (!map.containsKey(team)) {
            throw new java.lang.IllegalArgumentException();
        }

        LinkedList<String> res = new LinkedList<>();

        // Trivially eliminated, just return the first one encountered that can eliminate.
        int reqId = map.get(team);
        String eliStringName = trivialEliminatedCertificate(reqId);
        if (eliStringName != null) {
            res.add(eliStringName);
            return res;
        }

        return nonTrivialEliminatedCertificate(reqId, true);
    }

    public static void main(String[] args) {
        // String line = null;
        // try {
        //     FileReader fileReader = new FileReader(args[0]);
        //
        //     // Always wrap FileReader in BufferedReader.
        //     BufferedReader bufferedReader =
        //             new BufferedReader(fileReader);
        //
        //     while ((line = bufferedReader.readLine()) != null) {
        //         System.out.println(line);
        //     }
        //
        //     // Always close files.
        //     bufferedReader.close();
        // } catch (Exception e) {
        //     System.out.println("haha");
        // }
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            } else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}
