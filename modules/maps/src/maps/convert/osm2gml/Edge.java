package maps.convert.osm2gml;

import rescuecore2.misc.geometry.Line2D;

/**
   An edge. An edge is a line between two nodes.
 */
public class Edge extends ManagedObject {
    private final Node start;
    private final Node end;
    private final Line2D line;

    /**
       Construct a new Edge.
       @param id The ID of this object.
       @param start The start node.
       @param end The end node.
     */
    public Edge(long id, Node start, Node end) {
        super(id);
        this.start = start;
        this.end = end;
        line = new Line2D(start.getCoordinates(), end.getCoordinates());
    }

    /**
       Get the start node.
       @return The start node.
    */
    public Node getStart() {
        return start;
    }

    /**
       Get the end node.
       @return The end node.
    */
    public Node getEnd() {
        return end;
    }

    /**
       Get the line represented by this edge.
       @return The line.
    */
    public Line2D getLine() {
        return line;
    }

    @Override
    public String toString() {
        String result = "Edge " +
                getID() +
                " from " +
                start +
                " to " +
                end;
        return result;
    }
}
