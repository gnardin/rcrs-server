package traffic4.simulator;

import java.util.ArrayList;
import java.util.Comparator;

public class Dijkstra {

    private int parent[];
    private long minCost[];
    private int mark[];
    private int numberOfVertex;
    private int marker;


    public Dijkstra(int n) {
        setGraphSize(n);
    }

    public Dijkstra() {}

    public void setGraphSize(int n) {
        parent = new int[n + 1];
        mark = new int[n + 1];
        minCost = new long[n + 1];
        this.numberOfVertex = n;
    }

    /**
     * Run Dijkstra
     */
    public void Run(int[][] graph1, int src) throws Exception {

    }

    /**
     * Get path from destination to src
     * Both Src and Des are included in the path
     */
    public ArrayList<Integer> getPathArray(int desVertex) {
        ArrayList<Integer> ar = new ArrayList<>();
        if (parent[desVertex] == desVertex) {
            System.err.println("How it executed!");
            return ar;
        }
        if (parent[desVertex] != -1) {
            ar = getPathArray(parent[desVertex]);
        }
        ar.add(desVertex);
        return ar;
    }

    /**
     * Path cost from destination to source
     */
    public long getWght(int desVertex) {
        if (minCost[desVertex] < 0)
            new Error("Cost is negative....").printStackTrace();
        return minCost[desVertex];
    }

    public class Cmp implements Comparator<Integer> {
        @Override
        public int compare(Integer a, Integer b) {
            if (minCost[a] > minCost[b])
                return 1;
            else if (minCost[a] == minCost[b])
                return 0;
            else
                return -1;
        }
    }

}
