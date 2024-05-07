/*
package traffic4.objects;

//import java.awt.geom.Line2D;
import java.util.*;

import rescuecore2.log.Logger;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Human;
import traffic3.manager.TrafficManager;
import traffic3.simulator.PathElement;


public class TrafficAgent {

    private static class wallInfo {
        private rescuecore2.misc.geometry.Line2D wall;
        private TrafficArea area;
        private double distance;
        private Point2D closest;
        private Point2D origin;
        private Line2D line;
        private Vector2D vector;

        public wallInfo(Line2D wall, TrafficArea area) {
            this.wall = wall;
            this.area = area;
            this.distance = -1;
            this.closest = null;
            this.origin = null;
        }

*
         * get the shortest distance from the agent


        public double getDistance() {
            return this.distance;
        }

*
         * Recompute the distance to the agent and the closest point on the
         * line.
         *
         * @param from
         *            The position of the agent.


        public void computeClostestPoint(Point2D from) {
            if (from.equals(origin) && distance >= 0 && closest != null) {
                return;
            }
            origin = from;
            closest = GeometryTools2D.getClosestPointOnSegment(wall, origin);
            line = new Line2D(origin, closest);
            vector = line.getDirection();
            distance = vector.getLength();
        }

*
         * Get the clostest point to the agent on the wall.
         *
         * @return The closest point.


        public Point2D getClosestPoint() {
            return closest;
        }

*
         * Decrease the distance from the wall by an amount.
         *
         * @param d
         *            The amount by which to decrease the distance.


        public void decreaseDistance(double d) {
            distance -= d;
        }

*
         * Get the wall this WallInfo represents.
         *
         * @return The wall.


        public Line2D getWall() {
            return wall;
        }

*
         * Get the line from the agent to the closest point on the wall.
         *
         * @return Line2D to wall.


        public Line2D getLine() {
            return line;
        }

*
         * Get the vector from the agent to the closest point on the wall.
         *
         * @return Vector2D to wall.


        public Vector2D getVector() {
            return vector;
        }

*
         * Get the are the wall lies in.
         *
         * @return The area of this wall.


        public TrafficArea getArea() {
            return area;
        }
    }

    private static final int D = 8;
    private static final int DEFAULT_POSITION_HISTORY_FREQUENCY = 60;
    private static final double NEARBY_THRESHOLD_SQUARED = 10000000;

    // Force towards destination
    private final double[] destinationForce = new double[D];

    // Force away from agents
    private final double[] agentsForce = new double[D];

    // Force awat from walls
    private final double[] wallsForce = new double[D];

    // Height
    private final double[] height = new double[D];

    //Location
    private final double[] location = new double[D];

    // Velocity
    private final double[] velocity = new double[D];

    // Force
    private final double[] force = new double[D];

    private double radius;
    private double velocityLimit;

    //The path this agent wants to take;
    private Queue<PathElement> path;

    //The point this agent wants to reach
    private Point2D finalPoint;

    // The current destination (possible intermediate)
    private PathElement currentPathElement;
    private Point2D currentDestination;

    // The area the agent is currently located in
    private TrafficArea currentArea;


    private List<Point2D> posHistory;
    private double totalDistance;
    private boolean savePosHistory;
    private int posHistoryFrequency;
    private int historyCount;

    private Human human;
    private TrafficManager manager;

    private boolean mobile;

*/
/*
*Construct a traffic agent
     *
     * @param human The human wrapped by this object
     *
     * @param manager The Traffic manager
     *
     * @param radius
     *
     * @param velocityLimit

*//*


    public TrafficAgent(Human human, TrafficManager manager, double radius, double velocityLimit, double height) {
        this.human = human;
        this.manager = manager;
        this.radius = radius;
        this.velocityLimit = velocityLimit;
        path = new LinkedList<PathElement>();
        posHistory = new ArrayList<Point2D>();
        historyCount = 0;
        posHistoryFrequency = DEFAULT_POSITION_HISTORY_FREQUENCY;
        mobile = true;
    }

*
     * Get the Human wrapped by this object.
     *
     * @return The wrapped Human.


    public Human getHuman() {
        return human;
    }

*
     * Get the maximum velocity of this agent.
     *
     * @return The maximum velocity.


    public double getMaxVelocity() {
        return velocityLimit;
    }

*
     * Set the maximum velocity of this agent.
     *
     * @param vLimit
     *            The new maximum velocity.


    public void setMaxVelocity(double vLimit) {
        velocityLimit = vLimit;
    }

*
     * Get the TrafficArea the agent is currently in.
     *
     * @return The current TrafficArea.


    public TrafficArea getArea() {
        return currentArea;
    }

*
     * Get the position history.
     *
     * @return The position history.


    public List<Point2D> getPositionHistory() {
        return Collections.unmodifiableList(posHistory);
    }

*
     * Get the distance travelled so far.
     *
     * @return The distance travelled.


    public double getTravelDistance() {
        return totalDistance;
    }

*
     * Clear the position history and distance travelled.


    public void clearPositionHistory() {
        posHistory.clear();
        historyCount = 0;
        totalDistance = 0;
    }

*
     * Set the frequency of position history records. One record will be created
     * every nth microstep.
     *
     * @param n
     *            The new frequency.


    public void setPositionHistoryFrequency(int n) {
        posHistoryFrequency = n;
    }

*
     * Enable or disable position history recording.
     *
     * @param b
     *            True to enable position history recording, false otherwise.


    public void setPositionHistoryEnabled(boolean b) {
        savePosHistory = b;
    }

*
     * Get the X coordinate of this agent.
     *
     * @return The X coordinate.


    public double getX() {
        return location[0];
    }

*
     * Get the Y coordinate of this agent.
     *
     * @return The Y coordinate.


    public double getY() {
        return location[1];
    }

*
     * Get the total X force on this agent.
     *
     * @return The total X force in N.


    public double getFX() {
        return force[0];
    }

*
     * Get the total Y force on this agent.
     *
     * @return The total Y force in N.


    public double getFY() {
        return force[1];
    }

*
     * Get the X velocity of this agent.
     *
     * @return The X velocity in mm/s.


    public double getVX() {
        return velocity[0];
    }

*
     * Get the Y velocity of this agent.
     *
     * @return The Y velocity in mm/s.


    public double getVY() {
        return velocity[1];
    }

*
     * Set the radius of this agent.
     *
     * @param r
     *            The new radius in mm.


    public void setRadius(double r) {
        this.radius = r;
    }

*
     * Get the radius of this agent.
     *
     * @return The radius in mm.


    public double getRadius() {
        return this.radius;
    }

*
     * Set the path this agent wants to take.
     *
     * @param steps
     *            The new path.


    public void setPath(List<PathElement> steps) {
        if (steps == null || steps.isEmpty()) {
            clearPath();
            return;
        }
        path.clear();
        path.addAll(steps);
        finalDestination = steps.get(steps.size() - 1).getGoal();
        currentDestination = null;
        currentPathElement = null;
        // Logger.debug(this + " destination set: " + path);
        // Logger.debug(this + " final destination set: " + finalDestination);
    }

*
     * Clear the path.


    public void clearPath() {
        finalDestination = null;
        currentDestination = null;
        currentPathElement = null;
        path.clear();
    }

*
     * Get the final destination.
     *
     * @return The final destination


    public Point2D getFinalDestination() {
        return finalDestination;
    }

*
     * Get the current (possibly intermediate) destination.
     *
     * @return The current destination.


    public Point2D getCurrentDestination() {
        return currentDestination;
    }

*
     * Get the current (possibly intermediate) path element.
     *
     * @return The current path element.


    public PathElement getCurrentElement() {
        return currentPathElement;
    }

*
     * Get the current path.
     *
     * @return The path.


    public List<PathElement> getPath() {
        return Collections.unmodifiableList((List<PathElement>) path);
    }

*
     * Set the location of this agent. This method will also update the position
     * history (if enabled).
     *
     * @param x
     *            location x
     * @param y
     *            location y


    public void setLocation(double x, double y) {
        if (currentArea == null || !currentArea.contains(x, y)) {
            if (currentArea != null) {
                currentArea.removeAgent(this);
            }
            traffic3.objects.TrafficArea newArea = manager.findArea(x, y);

            if (newArea == null) {
                Logger.warn(getHuman() + "moved outside area: " + this);
                return;
            }

            currentArea = newArea;
            findBlockingLines();
            currentArea.addAgent(this);
        }
        // Check current destination
        if (currentPathElement != null) {
            // If we just crossed the target edge then clear the current path
            // element
            if (currentDestination == currentPathElement.getGoal() && currentDestination != finalDestination) {
                // Did we cross the edge?
                if (currentPathElement.getEdgeLine() != null && crossedLine(location[0], location[1], x, y, currentPathElement.getEdgeLine())) {
                    currentPathElement = null;
                }
                // Are we close enough to the goal point?
                else {
                    double dx = x - currentDestination.getX();
                    double dy = y - currentDestination.getY();
                    double distanceSquared = dx * dx + dy * dy;
                    if (distanceSquared < NEARBY_THRESHOLD_SQUARED) {
                        currentPathElement = null;
                    }
                }
            }
        }
        // Save position history
        if (savePositionHistory) {
            if (historyCount % positionHistoryFrequency == 0) {
                positionHistory.add(new Point2D(x, y));
            }
            historyCount++;

            // Update distance travelled
            double dx = x - location[0];
            double dy = y - location[1];
            totalDistance += Math.hypot(dx, dy);
        }
        location[0] = x;
        location[1] = y;
    }

    private boolean haveThisAreaInPath(traffic3.objects.TrafficArea newArea) {
        for (PathElement path : getPath()) {
            if (path.getAreaID().equals(newArea.getArea().getID()))
                return true;
        }
        return false;
    }

*
     * Perform any pre-timestep activities required.


    public void beginTimestep() {
        findBlockingLines();
        if (insideBlockade()) {
            Logger.debug(this + " inside blockade");
            setMobile(false);
        }
        startPosition = currentArea;
    }

*
     * Execute a microstep.
     *
     * @param dt
     *            The amount of time to simulate in ms.


    public void step(double dt) {
        if (mobile) {
            updateWalls(dt);
            updateGoals();
            computeForces(dt);
            updatePosition(dt);
        }
    }

*
     * Perform any post-timestep activities required.


    public void endTimestep() {
        handleOutOfActionCivilianMoves();

    }

    private void handleOutOfActionCivilianMoves() {
        if (!(getHuman() instanceof Civilian))
            return;
        if (currentArea.getArea().equals(startPosition.getArea()))
            return;
        if (!(currentArea.getArea() instanceof Building))
            return;
        if (getPath().isEmpty())
            return;
        if (haveThisAreaInPath(currentArea))
            return;

        Logger.warn(getHuman() + " moved to unplaned building(" + currentArea + ") " + this);
        traffic3.objects.TrafficArea newDest = getBestRoadNeighbor(currentArea, new HashSet<traffic3.objects.TrafficArea>());
        if (newDest == null) {
            Logger.warn(currentArea + " dosen't connected to any Road!");
            return;
        }
        setLocation(newDest.getArea().getX(), newDest.getArea().getY());

    }

}
*/
