package traffic4.simulator;

public class Dijkstra {

    private int parent[];
    private long minCost[];
    private int mark[];
    private int numberOfVertex;
    private int marker;


    public Dijkstra() {

    }

    public void setGaprhSize(int n) {
        parent = new int[n + 1];
        mark = new int[n + 1];
        minCost = new long[n + 1];
        this.numberOfVertex = n;
    }


}
