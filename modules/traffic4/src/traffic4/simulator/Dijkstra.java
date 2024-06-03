package traffic4.simulator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

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
    public void Run(int[][] graph, int src) throws Exception {
        marker++;
        for (int i = 0; i < numberOfVertex; ++i) {
            minCost[i] = Long.MAX_VALUE/2;
        }
        for (int i = 0; i < parent.length; i++) {
            parent[i] = -1;
        }
        PriorityQueue<Integer> pq = new PriorityQueue<Integer>(100, new Cmp());
            parent[src] = -1;
            minCost[src] = 0;
            pq.add(src);
        while (pq.size() != 0) {
            int node = (pq.poll());
            if (mark[node] == marker)
                continue;
            else
                mark[node] = marker;
            for (int i = 0; i < graph.length; i++) {
                if (graph[node][i] > 100000)
                    continue;
                if (node == i)
                    continue;

                int childIndex = i;
                if (mark[childIndex] == marker)
                    continue;
                int w = graph[node][i];
                if (w <= 0 || minCost[node] + w<0)
                    throw new Exception("Negative Cost");

                if(minCost[childIndex] > minCost[node] + w) {
                    minCost[childIndex] = minCost[node] + w;
                    parent[childIndex] = node;
                    pq.add(childIndex);
                }
            }
        }
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
