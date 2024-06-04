package traffic4.objects;

import rescuecore2.log.Logger;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Road;
//import traffic4.manager.TrafficManager;
import traffic3.objects.TrafficBlockade;
import traffic4.manager.TrafficManager1;
import traffic4.simulator.PathElement;
import traffic4.simulator.TrafficConstants;

import java.util.*;

public class TrafficAgent1 {

    private static class WallInformation {
        //wall
        private Line2D wall;
        //area
        private TrafficArea1 area;
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
        public WallInformation(Line2D wall, TrafficArea1 area) {
            this.wall = wall;
            this.area = area;
            this.distance = -1;
            this.closest = null;
            this.origin = null;
        }

        /**
         * Get the shortest distance from the agent's position. The distance may
         * not be accurate if the wall can't affect the agent in this microstep.
         *
         * @return The distance to the agent.
         */
        public double getDistance() {
            return this.distance;
        }

        /**
         * Recompute the distance to the agent and the closest point on the
         * line.
         *
         * @param from
         *            The position of the agent.
         */
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



        public TrafficArea1 getArea() {
            return area;
        }

        public Vector2D getVector() {
            return vector;
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

    //List of blocking lines near the agent
    private List<WallInformation> blockingLines;

    // Location
    private final double[] location = new double[D];

    // Velocity
    private final double[] velocity = new double[D];

    // Force
    private final double[] force = new double[D];

    private double radius;
    private double velocityLimit;
    private double height;

    // The point this agent wants to reach.
    private Point2D finalDestination;

    // The path this agent wants to take.
    private Queue<PathElement> path;

    // The current (possibly intermediate) destination.
    private PathElement currentPathElement;
    private Point2D currentDestination;

    // The area the agent is currently in.
    private TrafficArea1 currentArea;

    private List<Point2D> positionHistory;
    private double totalDistance;
    private boolean savePositionHistory;
    private int positionHistoryFrequency;
    private int historyCount;

    private Human human;
    private TrafficManager1 manager;

    private boolean mobile;
    private boolean colocated;
    private boolean verbose;

    private TrafficArea1 startPosition;


    /**
     * Construct a TrafficAgent.
     *
     * @param h
     *            The Human wrapped by this object.
     * @param manager
     *            The traffic manager.
     * @param radius
     *            The radius of this agent in mm.
     * @param velocityLimit
     *            The velicity limit.
     */
    public TrafficAgent1(Human h, TrafficManager1 manager, double radius, double velocityLimit) {
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

    /**
     * Get the Human wrapped by this object.
     *
     * @return The wrapped Human.
     */
    public Human getHuman() {
        return human;
    }

    public double getMaximumVelocity() {
        return velocityLimit;
    }

    public void setMaximumVelocity(double vlim) {
       velocityLimit = vlim;
    }


    /**
     * Get the TrafficArea the agent is currently in.
     *
     * @return The current TrafficArea.
     */
    public TrafficArea1 getArea() {
        return currentArea;
    }

    /**
     * Get the position history.
     *
     * @return The position history.
     */
    public List<Point2D> getPositionHistory() {
        return Collections.unmodifiableList(positionHistory);
    }

    /**
     * Get the distance travelled so far.
     *
     * @return The distance travelled.
     */
    public double getTravelDistance() {
        return totalDistance;
    }

    /**
     * Clear the position history and distance travelled.
     */
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

    /**
     * Set the radius of this agent.
     *
     * @param radius
     *            The new radius in mm.
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * Get the radius of this agent.
     *
     * @return The radius in mm.
     */
    public double getRadius() {
        return this.radius;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getHeight() {
        return this.height;
    }

    /**
     * Set the path this agent wants to take.
     *
     * @param steps
     *            The new path.
     */
    public void setPath(List<PathElement> steps) {
        if(steps == null || steps.isEmpty()) {
            clearPath();
            return;
        }
        path.clear();
        path.addAll(steps);
        finalDestination = steps.get(steps.size() - 1).getGoal();
        currentDestination = null;
        currentPathElement = null;

    }

    /**
     * Clear the path.
     */
    public void clearPath() {
        currentDestination = null;
        finalDestination = null;
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
    public List<PathElement> getPath() {
        return Collections.unmodifiableList((List<PathElement>) path);
    }


    /**
     * Set the location of this agent. This method will also update the position
     * history (if enabled).
     *
     * @param x
     *            location x
     * @param y
     *            location y
     */
    public void setLocation(double x, double y) {
        if (currentArea == null || !currentArea.contains(x, y)) {
            if (currentArea != null) {
                currentArea.removeAgent(this);
            }
            TrafficArea1 newArea = manager.findArea1(x, y);

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
                if (currentPathElement.getEdgeLines() != null && crossedLine(location[0], location[1], x, y, currentPathElement.getEdgeLines())) {
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

    private boolean haveThisAreaInPath(TrafficArea1 newArea) {
        for (PathElement path : getPath()) {
            if(path.getAreaID().equals(newArea.getArea().getID())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Timestep activities
     * begin timestep
     * step
     * end timesteo
     */
    public void step(double dt) {
        if (mobile) {
            updateWall(dt);
            updateGoal();
            computeForces(dt);
            updatePosition(dt);
        }
    }

    public void beginTimestep() {
        findBlockingLines();
//        if (isInsideBlockade()) {
//            setMobile(true);
//        }
        setMobile(true);
        startPosition = currentArea;
    }

    public void endTimestep() {
        handleOutOfActivitiesCivilianMoves();
    }


    private void handleOutOfActivitiesCivilianMoves() {
        if (!(getHuman() instanceof Civilian))
            return;
        if(currentArea.getArea().equals(startPosition.getArea()))
            return;
        if(!(currentArea.getArea() instanceof Building))
            return;
        if(getPath().isEmpty())
            return;
        if(haveThisAreaInPath(currentArea))
            return;
    }

    private TrafficArea1 getBestRoadNeighbor(TrafficArea1 area, HashSet<TrafficArea1> checked) {
        checked.add(area);
        if (area.getArea() instanceof Road) {
            return area;
        }
        for (TrafficArea1 neighbour : manager.getNeighbours(area)) {
            if (neighbour.getArea() instanceof Road) {
                return neighbour;
            }
        }
        for (TrafficArea1 neighbour : manager.getNeighbours(area)) {
            if (checked.contains(neighbour)){
                continue;
            }
            TrafficArea1 resarea = getBestRoadNeighbor(neighbour, checked);
            if (resarea != null) {
                return resarea;
            }
        }
        return null;
    }

    /**
     * Set mobility statues of the agent
     *
     * @param m true if the drone is mobile, false otherwise
     */
    public void setMobile(boolean m) {
        mobile = m;
    }

    /**
     * Find whether the agent is mobile
     *
     * @return True if it is.
     */
    public boolean isMobile() {
        return mobile;
    }

    public void setVerbose(boolean v) {
        verbose = v;
        Logger.debug(this + " is now " + (verbose ? "" : "not ") + "verbose");
    }

    private void updateGoal(){
        if (currentPathElement == null) {
            if (path.isEmpty()) {
                currentDestination = finalDestination;
                currentPathElement = null;
            } else {
                currentPathElement = path.remove();
                if (verbose) {
                    Logger.debug(this + " updated path:" + path);
                }
            }
        }

        if (currentPathElement != null) {
            //target edge
            currentDestination = currentPathElement.getGoal();
            Point2D current = new Point2D(location[0], location[1]);
            Vector2D vectorToEdge = currentDestination.minus(current).normalised();
            if (verbose) {
                Logger.debug(this + " searching for goal point");
                Logger.debug(this + " current path element: " + currentPathElement);
                Logger.debug(this + " current position: " + current);
                Logger.debug(this + " edge goal: " + currentDestination);
            }
            for (Point2D next : currentPathElement.getWayPoints()) {
                if (verbose) {
                    Logger.debug(this + " next possible goal: " + next);
                }
                if (next != currentPathElement.getGoal()) {
                    Vector2D vectorToNext = next.minus(current).normalised();
                    double dot = vectorToNext.dot(vectorToEdge);
                    if (dot < 0 || dot > 1) {
                        if (verbose) {
                            Logger.debug("Dot product of: " + vectorToNext + " and " + vectorToEdge + " is " + dot);
                            Logger.debug(this + " next point is " + (dot < 0 ? "backwards" : "too distant") + "; ignoring");
                        }
                        continue;
                    }
                }

                if (hasLos(current, next, currentArea)) {
                    currentDestination = next;
                    if (verbose) {
                        Logger.debug(this + " has line-of-sight to " + next);
                    }
                    break;
                }
            }
        }
    }

    private void computeForces(double dt) {
        colocated = false;
        computeDroneForce(agentsForce);
        if (!colocated) {
            computeDestinationForce(destinationForce);
            computeWallForce(wallsForce, dt);
        }

        force[0] = destinationForce[0] + agentsForce[0] + wallsForce[0];
        force[1] = destinationForce[1] + agentsForce[1] + wallsForce[1];

        if (Double.isNaN(force[0]) || Double.isNaN(force[1])) {
            Logger.warn("Force is NaN!");
            force[0] = 0;
            force[1] = 0;
        }
    }

    private void updatePosition(double dt) {
        double newVX = velocity[0] + dt * force[0];
        double newVY = velocity[1] + dt * force[1];
        double v = Math.hypot(newVX, newVY);
        if (v > this.velocityLimit) {
            // System.err.println("velocity exceeded velocityLimit");
            v /= this.velocityLimit;
            newVX /= v;
            newVY /= v;
        }

        double x = location[0] + dt * newVX;
        double y = location[1] + dt * newVY;

        if (verbose) {
            Logger.debug("Updating position for " + this);
            Logger.debug("Current position   : " + location[0] + ", " + location[1]);
            Logger.debug("Current velocity   : " + velocity[0] + ", " + velocity[1]);
            Logger.debug("Destination forces : " + destinationForce[0] + ", " + destinationForce[1]);
            Logger.debug("Agent forces       : " + agentsForce[0] + ", " + agentsForce[1]);
            Logger.debug("Wall forces        : " + wallsForce[0] + ", " + wallsForce[1]);
            Logger.debug("Total forces       : " + force[0] + ", " + force[1]);
            Logger.debug("New position       : " + x + ", " + y);
            Logger.debug("New velocity       : " + newVX + ", " + newVY);
        }
        if (crossedWall(location[0], location[1], x, y)) {
            velocity[0] = 0;
            velocity[1] = 0;
            return;
        }
        velocity[0] = newVX;
        velocity[1] = newVY;
        if (newVX != 0 || newVY != 0) {
            double dist = v * dt;
            for (TrafficAgent1.WallInformation wall : blockingLines) {
                wall.decreaseDistance(dist);
            }
            setLocation(x, y);
        }
    }

    private boolean hasLos(TrafficAgent1.WallInformation target, List<TrafficAgent1.WallInformation> blocking) {
        Line2D line = target.getLine();

        for (TrafficAgent1.WallInformation wall : blocking) {
            if (wall == target) {
                break;
            }

            Line2D next = wall.getWall();
            if (target.getClosestPoint().equals(next.getOrigin()) || target.getClosestPoint().equals(next.getEndPoint())) {
                continue;
            }

            double dotp = line.getDirection().dot(wall.getVector());
            if (dotp < wall.getDistance() * wall.getDistance()) {
                continue;
            }

            if (GeometryTools2D.getSegmentIntersectionPoint(line, next) != null) {
                return false;
            }
        }
        return true;
    }

    private boolean hasLos(Point2D source, Point2D target, TrafficArea1 area) {
        Line2D line = new Line2D(source, target);
        double dist = line.getDirection().getLength();

        for (TrafficAgent1.WallInformation wall : blockingLines) {
            if (wall.getDistance() > dist || wall.getArea() != area) {
                break;
            }

            Line2D next = wall.getWall();
            if (GeometryTools2D.getSegmentIntersectionPoint(line, next) != null) {
                return false;
            }
        }
        return true;
    }


//    private boolean isInsideBlockade() {
//        if (currentArea == null) {
//            return false;
//        }
//        for (TrafficBlockade1 block : currentArea.getBlockades()) {
//            if (block.containsLoc(location[0], location[1])) {
//                return true;
//            }
//        }
//        return false;
//    }

    public boolean crossedLine(double oldX, double oldY, double newX, double newY, Line2D line) {
        Line2D moved = new Line2D(oldX, oldY, newX - oldX, newY - oldY);
        return (GeometryTools2D.getSegmentIntersectionPoint(moved, line) != null);
    }

    private boolean crossedWall(double oldX, double oldY, double newX, double newY) {
        Line2D moved =  new Line2D(oldX, oldY, newX - oldX, newY - oldY);;
        double d = moved.getDirection().getLength();
        for (WallInformation wall : blockingLines) {
            if (wall.getDistance() >= d) {
                continue;
            }
            Line2D test = wall.getWall();
            if (GeometryTools2D.getSegmentIntersectionPoint(moved, test) != null) {
                // if (crossedLine(oldX, oldY, newX, newY, test)) {
                /*
                 * Logger.warn(this + " crossed wall");
                 * Logger.warn("Old location: " + oldX + ", " + oldY);
                 * Logger.warn("New location: " + newX + ", " + newY);
                 * Logger.warn("Movement line: " + moved);
                 * Logger.warn("Wall         : " + test);
                 * Logger.warn("Crossed at " +
                 * GeometryTools2D.getSegmentIntersectionPoint(moved, test));
                 */
                return true;
            }
        }
        return false;
    }


    private void findBlockingLines() {
        blockingLines.clear();
        if (currentArea != null) {
            for (Line2D line : currentArea.getAllBlockingLines()) {
                blockingLines.add(new WallInformation(line, currentArea));
            }
            for (TrafficArea1 neighbor : manager.getNeighbours(currentArea)) {
                for (Line2D line : neighbor.getAllBlockingLines()) {
                    blockingLines.add(new WallInformation(line, neighbor));
                }
            }
        }
    }

    private void updateWall(double dt) {
        Point2D position = new Point2D(location[0], location[1]);
        double crossCutoff = dt * this.velocityLimit;
        double forceCutoff = TrafficConstants.getWallDistanceCutoff();
        double cutoff = Math.max(forceCutoff, crossCutoff);

        for (WallInformation wall : blockingLines) {
            if (wall.getDistance() > cutoff) {
                continue;
            }
            wall.computeClosestPoint(position);
        }

        for (int i = 1; i <= blockingLines.size(); i++) {
            WallInformation wall = blockingLines.get(i);
            for (int j = i; j >= 0; j--) {
                if (j == 0) {
                    blockingLines.remove(i);
                    blockingLines.add(0, wall);
                } else if (blockingLines.get(j - 1).getDistance() < wall.getDistance()) {
                    if (j == i) {
                        break;
                    }
                    blockingLines.remove(i);
                    blockingLines.add(j, wall);
                    break;
                }
            }
        }
    }



    private void computeDestinationForce(double[] result) {
        double destX = 0;
        double destY = 0;
        if (currentDestination != null) {
            double dx = currentDestination.getX() - location[0];
            double dy = currentDestination.getY() - location[1];
            double distance = Math.hypot(dx, dy);
            if (distance == 0) {
                dx = 0;
                dy = 0;
            } else {
                dx /= distance;
                dy /= distance;
            }
            final double ddd = 0.001;
            if (currentDestination == finalDestination) {
                dx = Math.min(velocityLimit, ddd * distance) * dx;
                dy = Math.min(velocityLimit, ddd * distance) * dy;
            } else {
                dx = this.velocityLimit * dx;
                dy = this.velocityLimit * dy;
            }

            final double sss2 = 0.0002;
            destX = sss2 * (dx - velocity[0]);
            destY = sss2 * (dy - velocity[1]);
        } else {
            final double sss = 0.0001;
            destX = sss * (-velocity[0]);
            destY = sss * (-velocity[1]);
        }
        result[0] = destX;
        result[1] = destY;
        if (Double.isNaN(destX)) {
            Logger.warn("Destination force x is Nan");
            result[0] = 0;
        }
        if (Double.isNaN(destY)) {
            Logger.warn("Destination force y is Nan");
            result[1] = 0;
        }
        if (verbose) {
            Logger.debug("Destination force: " + result[0] + ", " + result[1]);
        }
    }

    private void computeDroneForce(double[] result) {
        result[0] = 0;
        result[1] = 1;
        if (currentArea == null) {
            return;
        }

        double xSum = 0;
        double ySum = 0;

        double cutoff = TrafficConstants.getDroneDistanceCutoff();
        double a = TrafficConstants.getDroneForceCoefficientA();
        double b  = TrafficConstants.getDroneForceCoefficientB();
        double c = TrafficConstants.getDroneForceCoefficientC();
        double droneForceLimit = TrafficConstants.getDroneForceLimit();

        Collection<TrafficAgent1> near = manager.getNeighbouringAgents(this);
        for (TrafficAgent1 agent : near) {
            if (!agent.isMobile()) {
                continue;
            }
            double dx = agent.getX() - location[0];
            double dy = agent.getY() - location[1];

            if (Math.abs(dx) > cutoff) {
                continue;
            }
            if (Math.abs(dy) > cutoff) {
                continue;
            }

            double totalRd = radius + agent.getRadius();
            double distanceSqrd = Math.hypot(dx, dy);


            if (distanceSqrd == 0) {
                xSum = TrafficConstants.getColocatedAgentNudge();
                ySum = TrafficConstants.getColocatedAgentNudge();
                colocated = true;
                Logger.debug(this + "is co located with " + agent);
                break;
            }
            double distance = Math.sqrt(distanceSqrd);
            double dxN = dx / distance;
            double dyN = dy / distance;
            double negative_seperation = totalRd - distance;
            double tmp = -a * Math.exp(negative_seperation * b);
            if (Double.isInfinite(tmp)) {
                Logger.warn("The calculateDroneForce(): The result of exp is infinite: exp(" + (negative_seperation * b) + ")");
            } else {
                xSum = tmp * dxN;
                ySum = tmp * dyN;
            }
            if (negative_seperation > 0) {
                xSum += -c * negative_seperation * dxN;
                ySum += -c * negative_seperation * dyN;
            }
        }

        double forceSum = Math.hypot(xSum, ySum);
        if (forceSum > droneForceLimit) {
            forceSum /= droneForceLimit;
            xSum /= forceSum;
            ySum /= forceSum;
        }
        if (Double.isNaN(xSum)) {
            Logger.warn("computeDroneForce: Sum of X force is NaN");
            xSum = 0;
        }
        if (Double.isNaN(ySum)) {
            Logger.warn("computeDroneForce: Sum of Y force is NaN");
        }
        result[0] = xSum;
        result[1] = ySum;
    }


    private void computeWallForce(double[] result, double dt) {
        double xSum = 0;
        double ySum = 0;
        if (currentArea != null) {
            double radius = getRadius();
            double distance;
            double cutoff = TrafficConstants.getWallDistanceCutoff();
            double a = TrafficConstants.getWallForceCoefficientA();
            double b = TrafficConstants.getWallForceCoefficientB();
            Point2D position = new Point2D(location[0], location[1]);
            if (verbose) {
                Logger.warn("Computing wall forces for " + this);
                Logger.warn("Position: " + position);
            }

            for(WallInformation wall : blockingLines) {
                if (wall.getDistance() > cutoff) {
                    break;
                }
                Line2D line = wall.getWall();
                distance = wall.getDistance();
                Point2D closest = wall.getClosestPoint();

                if (verbose) {
                    Logger.debug("Next wall: " + line);
                }
                if (verbose) {
                    Logger.debug("Closest point: " + closest);
                }
                if(!hasLos(wall, blockingLines)) {
                    //No line of sight closest point
                    if (verbose) {
                        Logger.debug("No line of sight");
                    }
                    continue;
                }

                boolean endPoint = false;
                if (closest == line.getOrigin() || closest == line.getEndPoint()) {
                    endPoint = true;
                }

                double currentVX = velocity[0];
                double currentVY = velocity[1];
                double currentFX = destinationForce[0] + agentsForce[0];
                double currentFY = destinationForce[1] + agentsForce[1];
                double expectedVX = currentVX + dt * currentFX;
                double expectedVY = currentVY + dt * currentFY;
                Vector2D expectedVelocity = new Vector2D(expectedVX, expectedVY);
                Vector2D wallForceVector = wall.getVector().scale(-1.0 / distance);
                double radii = distance / radius;
                // Compute the stopping force
                // Magnitude is the multiple of wallForceVector required to
                // bring the agent to a stop.
                double magnitude = -expectedVelocity.dot(wallForceVector);
                if (magnitude < 0 || radii >= 1) {
                    magnitude = 0;
                    // Agent is moving away or far enough away - no stopping
                    // force required.
                } else if (radii < 1) {
                    double d = Math.exp(-(radii - 1) * b);
                    if (d < 1) {
                        d = 0;
                    }
                    magnitude *= d;
                    if (endPoint) {
                        // Endpoints are counted twice so halve the magnitude
                        magnitude /= 2;
                    }
                }
                Vector2D stopForce = wallForceVector.scale(magnitude / dt);
                // Compute the repulsion force
                // Decreases exponentially with distance in terms of agent
                // radii.
                // double factor = a * Math.min(1, Math.exp(-(radii - 1) * b));
                // Vector2D repulsionForce = wallForceVector.scale(factor / dt);
                xSum += stopForce.getX();
                ySum += stopForce.getY();
                // xSum += repulsionForce.getX();
                // ySum += repulsionForce.getY();
                if (verbose) {
                    Logger.debug("Distance to wall : " + distance);
                    Logger.debug("Distance to wall : " + radii + " radii");
                    Logger.debug("Current velocity : " + currentVX + ", " + currentVY);
                    Logger.debug("Current force    : " + currentFX + ", " + currentFY);
                    Logger.debug("Expected velocity: " + expectedVelocity);
                    Logger.debug("Wall force       : " + wallForceVector);
                    Logger.debug("Magnitude        : " + magnitude);
                    Logger.debug("Stop force       : " + stopForce);
                    // Logger.debug("Factor           : " + factor + " (e^" +
                    // (-(dist / r) * b) + ")");
                    // Logger.debug("Repulsion force  : " + repulsionForce);
                }
            }
        }
        if (Double.isNaN(xSum) || Double.isNaN(ySum)) {
            xSum = 0;
            ySum = 0;
        }
        if (verbose) {
            Logger.debug("Total wall force: " + xSum + ", " + ySum);
        }

        result[0] = xSum;
        result[1] = ySum;

        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer("TrafficAgent[");
            sb.append("id:").append(human.getID()).append(";");
            sb.append("x:").append((int) getX()).append(";");
            sb.append("y:").append((int) getY()).append(";");
            sb.append("]");
            return sb.toString();
        }

        @Override
        public int hashCode() {
            return human.getID().hashCode();
        }
    }


