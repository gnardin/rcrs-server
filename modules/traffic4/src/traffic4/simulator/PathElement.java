package traffic4.simulator;

import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PathElement {
    private EntityID areaID;
    private Line2D edgeLine;
    private Point2D targetPoint;
    private List<Point2D> allPoints;

    /**
     * Construct a Path element
     * @param areaID The ID of the area this element refers to.
     * @param edgeLine The line of the edge the agent is heading for.
     * @param targetPoint The target path of the agent.
     * @param wayPoints
     */
    public PathElement(EntityID areaID, Line2D edgeLine, Point2D targetPoint, Point2D... wayPoints) {
        this.areaID = areaID;
        this.edgeLine = edgeLine;
        this.targetPoint = targetPoint;
        allPoints = new ArrayList<Point2D>(Arrays.asList(wayPoints));
        allPoints.add(targetPoint);
        Collections.reverse(allPoints);
    }

    @Override
    public String toString() {
         return "Move to area " + areaID + " = " + allPoints;
    }

    public Point2D getGoal() {
        return targetPoint;
    }

    public Line2D getEdgeLines() {
        return edgeLine;
    }

    public Point2D getTargetPoint() {
        return targetPoint;
    }

    public List<Point2D> getWayPoints() {
        return Collections.unmodifiableList(allPoints);
    }

    public void removeWayPoints(Point2D point) {
        allPoints.remove(point);
    }

    public EntityID getAreaID() {
        return areaID;
    }
}
