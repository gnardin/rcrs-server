package traffic4.objects;

import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.standard.entities.Human;
import traffic4.manager.TrafficManager;
import traffic4.simulator.PathElement;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

public class TrafficAgent {

    private static class WallInformation {
        //wall
        private Line2D wall;
        //area
        private TrafficArea area;
        //distance
        private double distance;
        //closest point
        private Point2D closest;
        //point of origin
        private Point2D origin;
        //line
        private Line2D line;
        //vector
        private Vector2D vector;


        /**
         * Create a wall info object from a line2d in a traffic area.
         *
         * @param wall
         * @param area
         */
        public WallInformation(Line2D wall, TrafficArea area) {
            this.wall = wall;
            this.area = area;
            this.distance = -1;
            this.closest = null;
            this.origin = null;
        }

        public double getDistance() {
            return this.distance;
        }

         public TrafficArea getArea() {
            return area;
        }

        public Vector2D getVector() {
            return vector;
        }

        public void computeClosestPoint(Point2D from) {
            if(from.equals(origin) && distance >=  0 && closest != null) {
                return;
            }
            origin = from;
            closest = GeometryTools2D.getClosestPointOnSegment(wall, origin);
            line = new Line2D (origin, closest);
            vector = line.getDirection();
            distance = vector.getLength();
        }

        public Point2D getClosestPoint() {
            return closest;
        }

        public void decreaseDistance(double d) {
            distance -= d;
        }

        public Point2D getOrigin() {
            return origin;
        }

        public Line2D getWall() {
            return wall;
        }

        public Line2D getLine() {
            return line;
        }

    }

    private static final int D = 2;

    private static final int DEFAULT_POSITION_HISTORY_FREQUENCY = 60;

    private static final double NEARBY_THRESHOLD_SQUARED = 1000000;

    // Force towards destination
    private final double[] destinationForce = new double[D];

    // Force away from agents
    private final double[] agentsForce = new double[D];

    // Force away from walls
    private final double[] wallsForce = new double[D];

    // Location
    private final double[] location = new double[D];

    // Velocity
    private final double[] velocity = new double[D];

    // Force
    private final double[] force = new double[D];

    private double radius;
    private double velocityLimit;

    // The point this agent wants to reach.
    private Point2D finalDestination;

    // The path this agent wants to take.
    private Queue<PathElement> path;

    // The current (possibly intermediate) destination.
    private PathElement currentPathElement;
    private Point2D currentDestination;

    // The area the agent is currently in.
    private TrafficArea currentArea;

    private List<Point2D> positionHistory;
    private double totalDistance;
    private boolean savePositionHistory;
    private int positionHistoryFrequency;
    private int historyCount;

    private Human human;
    private TrafficManager manager;

    private boolean mobile;
    private boolean colocated;
    private boolean verbose;

    private TrafficArea startPosition;

    public TrafficAgent(Human h, TrafficManager manager, double radius, double velocityLimit) {
        this.human = h;
        this.manager = manager;
        this.radius = radius;
        this.velocityLimit = velocityLimit;
        path = new LinkedList<PathElement>();
        positionHistory = new ArrayList<Point2D>();
        savePositionHistory = true;
        historyCount = 0;
        positionHistoryFrequency = DEFAULT_POSITION_HISTORY_FREQUENCY;
        mobile = true;
    }

    public Human getHuman() {
        return human;
    }

    public double getMaximumVelocity() {
        return velocityLimit;
    }

    public void setMaximumVelocity(double vlim) {
       velocityLimit = vlim;
    }

    public TrafficArea getArea() {
        return currentArea;
    }

    public List<Point2D> getPositionHistory() {
        return Collections.unmodifiableList(positionHistory);
    }

    public double getTravelDistance() {
        return totalDistance;
    }

    public void clearPositionHistory() {
        positionHistory.clear();
        historyCount = 0;
        totalDistance = 0;
    }

    public void setPositionHistoryEnabled1(boolean b) {
        savePositionHistory = b;
    }

    public void setDefaultPositionHistoryFrequency(int n) {
        positionHistoryFrequency = n;
    }

    public double getX(){
        return location[0];
    }

    public double getY(){
        return location[1];
    }

    public double getfX() {
        return force[0];
    }

    public double getfY() {
        return force[1];
    }

    public double getvX() {
        return velocity[0];
    }

    public double getvY() {
        return velocity[1];
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getRadius() {
        return this.radius;
    }

    public void setPath(List<PathElement> steps) {
        if(steps == null || steps.isEmpty()) {
            //clearPath();
            return;
        }
        path.clear();
        path.addAll(steps);
        //finalDestination = steps.get(steps.size() - 1).getGoal();
        currentDestination = null;
        currentPathElement = null;
        path.clear();
    }

    /**
     * Get the final destination.
     *
     * @return The final destination
     */
    public Point2D getFinalDestination() {
        return finalDestination;
    }

    /**
     * Get the current (possibly intermediate) destination.
     *
     * @return The current destination.
     */
    public Point2D getCurrentDestination() {
        return currentDestination;
    }

    /**
     * Get the current (possibly intermediate) path element.
     *
     * @return The current path element.
     */
    public PathElement getCurrentElement() {
        return currentPathElement;
    }

    /**
     * Get the current path.
     *
     * @return The path.
     */
//    public List<traffic3.simulator.PathElement> getPath() {
//        return Collections.unmodifiableList((List<PathElement>) path);
//    }


//    public void setLocation(double X, double Y) {
//        if(currentArea == null || !currentArea.contains(X, Y)) {
//            if(currentArea != null) {
//                currentArea.removeAgent(this);
//            }
//            TrafficArea newA = manager.findArea(X, Y);
//        }
//    }



}
