package traffic4.simulator;

import java.awt.Color;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.GaussianGenerator;


import rescuecore2.GUIComponent;
import rescuecore2.standard.components.StandardSimulator;
import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.messages.control.KSCommands;
import rescuecore2.messages.control.KSUpdate;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.misc.gui.ShapeDebugFrame;
import rescuecore2.misc.gui.ShapeDebugFrame.Line2DShapeInfo;
import rescuecore2.standard.components.StandardSimulator;
import rescuecore2.standard.entities.*;
import rescuecore2.standard.messages.*;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.WorldModelListener;
import rescuecore2.worldmodel.properties.EntityRefListProperty;
import rescuecore2.worldmodel.properties.EntityRefProperty;
import rescuecore2.worldmodel.properties.IntProperty;


import traffic3.objects.TrafficArea;
import traffic3.simulator.Dijkstra;
import traffic4.manager.TrafficManager1;
import traffic4.objects.TrafficArea1;
import traffic3.simulator.TrafficSimulatorGUI;
import traffic4.objects.TrafficAgent1;
import traffic3.objects.TrafficBlockade;
import traffic4.objects.TrafficArea1;


import javax.swing.*;

public class TrafficSimulator extends StandardSimulator implements GUIComponent {
    private static final double TIME_STEP_MS = 100;
    private static final double REAL_TIME_S = 60;
    private static final int MICROSTEPS = (int) ((1000.0 / TIME_STEP_MS) * REAL_TIME_S);


    private static final int AGENT_RADIUS = 500;
//    private static final int CIVILIAN_RADIUS = 200;
    private static final double AGENT_VELOCITY_MEAN = 0.7;
    private static final double AGENT_VELOCITY_SD = 0.1;
    private static final double AGENT_HEIGHT = 25.0;
//    private static final double CIVILIAN_VELOCITY_MEAN = 0.2;
//    private static final double CIVILIAN_VELOCITY_SD = 0.002;

    private TrafficSimulatorGUI gui;

    private TrafficManager1 manager;

    /**
     * Construct a new traffic simulator but just to manage drones
     */

    @Override
    public JComponent getGUIComponent() {
        return gui;
    }

    @Override
    public String getGUIComponentName() {
        return "Trafffic simulator for drones";
    }

    protected void postConnectDrone() {
        TrafficConstants.init(config);
        manager.clearObject();
        for (StandardEntity next : model) {
            if (next instanceof Area) {
                convertAreaToTrafficArea((Area) next);
            }
        }

    }

    private void clearAreaCacheDrone(EntityID entityArea) {
        manager.getTrafficAreaforDrone((Area) model.getEntity(entityArea)).clearBlockadeCache();
    }

    private void convertAreaToTrafficArea(Area area) {
        //manager.register(new TrafficArea(area));
    }

    private void convertHumanDrone(Human human, NumberGenerator<Double> agentVelocityGenerator, NumberGenerator<Double> heightGenerator) {
        double radius = 0;
        double height = 0;
        double velocityLimit = 0;
        if(human instanceof Drone) {
            radius = AGENT_RADIUS;
            height = AGENT_HEIGHT;
            velocityLimit = agentVelocityGenerator.nextValue();
        } else {
            throw new IllegalArgumentException("Unrecognised agent type: " + human + " (" + human.getClass().getName() + ")");
        }
        TrafficAgent1 agent = new TrafficAgent1(human, manager, radius, velocityLimit);
        agent.setLocation(human.getX(), human.getY());
        manager.register(agent);
    }

    private void convertGround() {}

    private void handleMove(AKMove move) {
        Human human = (Human) model.getEntity(move.getAgentID());
        TrafficAgent1 agent = manager.getTrafficAgentForDrone(human);
        EntityID current = human.getPosition();
        if(current == null) {
            Logger.warn("Agent position is not defined");
            return;
        }
        Entity currentEntity = model.getEntity(human.getPosition());
        if (!(currentEntity instanceof Area)) {
            Logger.warn("Rejecting move: agent position is not an area: " + currentEntity);
            return;
        }
        Area currentArea = (Area) currentEntity;
        List<EntityID> list = move.getPath();
        List<traffic4.simulator.PathElement> steps1 = new ArrayList<PathElement>();
        Edge lastEdge = null;
        // check elements refer to Area instances
        // build the list of target points
        for (Iterator<EntityID> it = list.iterator(); it.hasNext();) {
            EntityID next = it.next();
            if (next.equals(current)){
                continue;
            }
            Entity e = model.getEntity(next);
            if (!(e instanceof Area)) {
                Logger.warn("Rejecting move: Entity ID " + next + " is not an area: " + e);
                return;
            }

            Edge edge = currentArea.getEdgeTo(next);
            if (edge == null) {
                Logger.warn("Rejecting move: Entity ID " + next + " is not adjacent to " + currentArea);
                return;
            }
            Area nextArea = (Area) e;

            //steps.addAll(getPathElements2(human, currentArea, lastEdge, nextArea, edge));

            current = next;
            currentArea = nextArea;
            lastEdge = edge;
        }
        int targetX = move.getDestinationX();
        int targetY = move.getDestinationY();
        if(targetX == -1 && targetY == -1) {
            targetX = currentArea.getX();
            targetY = currentArea.getY();
        } else if(list.isEmpty()) {
            Logger.warn("Rejecting move: path is empty");
        }
        steps1.add(new PathElement(current, null, new Point2D(targetX, targetY)));
        //agent.setPath1(steps1);
    }

    private Collection<? extends PathElement> getPathElements(Human human, Area lastArea, Edge lastEdge, Area nextArea,
                                                              Edge nextEdge) {
        if (human.getID().getValue() == 204623396) {
            System.out.println(
                    "lastArea=" + lastArea + " lastEdge=" + lastEdge + " nextArea=" + nextArea + " nextEdge=" + nextEdge);
        }
        ArrayList<traffic3.simulator.PathElement> steps = new ArrayList<traffic3.simulator.PathElement>();
        Point2D edgePoint = getBestPoint(nextEdge, nextArea);
        Point2D centrePoint = new Point2D(lastArea.getX(), lastArea.getY());
        if (lastEdge == null) {
            Point2D entracePoint = getEntranceOfArea(nextEdge, lastArea);
            if (entracePoint != null) {
                steps.add(new traffic3.simulator.PathElement(lastArea.getID(), null, entracePoint, centrePoint));
                steps.add(new traffic3.simulator.PathElement(lastArea.getID(), nextEdge.getLine(), edgePoint, entracePoint));
            } else
                steps.add(new traffic3.simulator.PathElement(lastArea.getID(), nextEdge.getLine(), edgePoint));

        } else {
            Point2D startEntracePoint = getEntranceOfArea(lastEdge, lastArea);
            if (startEntracePoint != null)
                steps.add(new traffic3.simulator.PathElement(lastArea.getID(), null, startEntracePoint));
            Point2D entracePoint = getEntranceOfArea(nextEdge, lastArea);
            if (entracePoint != null) {
                steps.add(new traffic3.simulator.PathElement(lastArea.getID(), nextEdge.getLine(), entracePoint, centrePoint));
                steps.add(new traffic3.simulator.PathElement(lastArea.getID(), nextEdge.getLine(), edgePoint, entracePoint));
            } else {
                steps.add(new traffic3.simulator.PathElement(lastArea.getID(), nextEdge.getLine(), edgePoint, centrePoint));
            }
        }
      return null;
    }

    private boolean havePassableEdge(Area destination) {
        for (Edge edge : destination.getEdges()) {
            if (edge.isPassable()) {
                return true;
            }
        }
        return false;
    }

    private Point2D getEntranceOfArea(Edge incomingEdge, Area destination) {

        Point2D edgeMid = getBestPoint(incomingEdge, destination);
        Line2D wallLine = incomingEdge.getLine();

        int distance = 600;
        while (distance > 0) {
            Vector2D offset = wallLine.getDirection().getNormal().normalised().scale(distance);
            Point2D destinationXY = edgeMid.plus(offset);
            if (destination.getShape().contains(destinationXY.getX(), destinationXY.getY())) {
                return destinationXY;
            }
            offset = wallLine.getDirection().getNormal().normalised().scale(-distance);
            destinationXY = edgeMid.plus(offset);
            if (destination.getShape().contains(destinationXY.getX(), destinationXY.getY())){
                return destinationXY;
            }
         }
        return null;
    }

//    private Collection<? extends PathElement> getPathElements2(Human human, Area lastArea, Edge lastEdge, Area nextArea, Edge nextEdge) {
//        Collection<? extends PathElement> originalPaths = getPathElements(human, lastArea, lastEdge, nextArea, nextEdge);
//        if (isOriginalPathOk(originalPaths))
//            return originalPaths;
//        Point2D start;
//        if (lastEdge == null)
//            start = new Point2D(human.getX(), human.getY());
//        else
//            start = getBestPoint(lastEdge, lastArea);
//        Point2D startpoint;
//        if (lastEdge == null)
//            startpoint = start;
//        else
//            startpoint = getMidPoint(lastEdge.getStart(), lastEdge.getEnd());
//        Point2D edgePoint = getBestPoint(nextEdge, nextArea);
//        Point2D centrePoint = new Point2D(lastArea.getX(), lastArea.getY());
//
//        List<ShapeDebugFrame.ShapeInfo> resultGraph = new ArrayList<ShapeDebugFrame.ShapeInfo>();
//
//        resultGraph.add(
//                new Line2DShapeInfo(new Line2D(startpoint, centrePoint), "path start to center", Color.black, false, true));
//        resultGraph.add(new Line2DShapeInfo(new Line2D(centrePoint, getMidPoint(nextEdge.getStart(), nextEdge.getEnd())),
//                "path center to end", Color.white, false, true));
//
//        TrafficArea1 trafficArea = manager.getTrafficAreaforDrone(lastArea);
//        int[][] graph = manager.getTrafficAreaforDrone(lastArea).getGraph();
//        List<Line2D> oLines = manager.getTrafficAreaforDrone(lastArea).getOpenLines();
//        List<Line2D> graphline = new ArrayList<>();
//        resultGraph.add(new Line2DShapeInfo(oLines, "openLines", Color.green, false, false));
//        for (int i = 0; i < graph.length; i++) {
//            for (int j = 0; j < graph.length; j++) {
//                if (graph[i][j] > 10000)
//                    continue;
//                Line2D line = new Line2D(getMidPoint(oLines.get(i).getOrigin(), oLines.get(i).getEndPoint()),
//                        getMidPoint(oLines.get(j).getOrigin(), oLines.get(j).getEndPoint()));
//                graphline.add(line);
//            }
//        }
//
//        int src = trafficArea.getNearestLineIndex(start);
//        int end = trafficArea.getNearestLineIndex(edgePoint);
//
//        if (src != end && src != -1 && end != -1) {
//            traffic3.simulator.Dijkstra dijkstra = new Dijkstra(graph.length);
//            try {
//                dijkstra.Run(graph, src);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            try {
//                if (dijkstra.getWeight(end) < 1000) {
//                    ArrayList<Integer> path = dijkstra.getpathArray(end);
//                    if (path.size() > 2) {
//                        List<Point2D> points = new ArrayList<Point2D>();
//                        for (Integer integer : path) {
//                            Point2D point = getMidPoint(oLines.get(integer).getOrigin(), oLines.get(integer).getEndPoint());
//                            points.add(point);
//                        }
//
//                        ArrayList<PathElement> result = new ArrayList<PathElement>();
//                        result.add(new PathElement(nextArea.getID(), nextEdge.getLine(), start));
//
//                        for (Point2D point : points)
//                            result.add(new PathElement(nextArea.getID(), nextEdge.getLine(), point));
//
//                        result.add(new PathElement(nextArea.getID(), nextEdge.getLine(), edgePoint));
//
//                        return result;
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        return originalPaths;
//    }

    private boolean isOriginalPathOk(Collection<? extends PathElement> originalPaths) {
        if(originalPaths.isEmpty()) {
            Logger.warn("original paths is full");
            return true;
        }

        TrafficArea1 lastArea = null;
        ArrayList<PathElement> SameAreaElements = new ArrayList<>();
        for (PathElement path : originalPaths) {

            TrafficArea1 area = manager.getTrafficAreaforDrone((Area) model.getEntity(path.getAreaID()));
            for (TrafficBlockade block : area.getBlockades()) {
                if (block.getBlockade().getShape().contains(path.getGoal().getX(), path.getGoal().getY()))
                    return true;
            }
            double minDist = getMinimumDistance(area.getAllBlockingLines(), path.getGoal());

            if (minDist < TrafficSimulator.AGENT_RADIUS / 2)
                return false;
            if (lastArea == null || lastArea == area) {
                SameAreaElements.add(path);
            } else {
                if (!checkElements(lastArea, SameAreaElements)) {
                    return false;
                }
                SameAreaElements.clear();
            }
            lastArea = area;
        }
        if(!checkElements(lastArea, SameAreaElements))
            return false;
        return true;
    }

    private boolean checkElements(TrafficArea1 lastArea, List<PathElement> sameAreaElem) {
        if (sameAreaElem.size() <= 1)
            return true;

        for (int i = 1; i < sameAreaElem.size(); i++) {
            Line2D line2D = new Line2D(sameAreaElem.get(i - 1).getGoal(), sameAreaElem.get(i).getGoal());
            for (Line2D block : lastArea.getAllBlockingLines()) {
                if (GeometryTools2D.getSegmentIntersectionPoint(line2D, block) != null)
                    return false;
            }
        }
        return true;
    }

    private Point2D getBestPoint(Edge edge, Area destination) {
        return getMidPoint(edge.getStart(), edge.getEnd());
    }

    static Point2D getMidPoint(Point2D p1, Point2D p2) {
        return new Point2D((p1.getX() + p2.getX()) / 2, (p2.getX() + p2.getY()) / 2);
    }

    private Point2D getTransivit2(Point2D base, Point2D p1) {
        return new Point2D((base.getX() - (p1.getX() - base.getX())), (base.getY() - (p1.getY()- base.getY())));
    }

    private double getMinimumDistance(List<Line2D> lines, Point2D point) {
        double min = Integer.MAX_VALUE;
        for(Line2D block : lines) {
            Point2D tempPoint = GeometryTools2D.getClosestPointOnSegment(block, point);
            double tempDistance = GeometryTools2D.getDistance(point, tempPoint);
            if (tempDistance < min) {
                min = tempDistance;
            }
        }
        return min;
    }

    private void handleFly(AKFly fly, ChangeSet changes) {
        EntityID agentID = fly.getAgentID();
        Entity agent = model.getEntity(agentID);
        if (agent instanceof Human) {
            manager.getTrafficAgentForDrone((Human) agent).setMobile(true);
            Logger.debug(agent + " is flying over " );
        }
    }

    private void timestep() {
        long start = System.currentTimeMillis();
        for (TrafficAgent1 agent : manager.getALLAgents()) {
            agent.beginTimestep();
        }

        long pre = System.currentTimeMillis();
        Logger.debug("Running " + MICROSTEPS + " microsteps");
        for(int i = 0; i < MICROSTEPS; i++){
            microstep();
        }

        long post = System.currentTimeMillis();
        for(TrafficAgent1 agent : manager.getALLAgents()){
            agent.endTimestep();
        }

        long end = System.currentTimeMillis();
        if (manager.getALLAgents().size() != 0) {
            Logger.debug("Pre timestep took " + (pre - start) + " ms (average " +((pre - start) / manager.getALLAgents().size()) + "ms per agent)");
            Logger.debug("Microsteps: " + (post - pre) + "ms (average " + ((post - pre) / MICROSTEPS) + "ms");
            Logger.debug("Post timestep: " + (end - post) + " ms (average " + ((end - post) / manager.getALLAgents().size()) + "ms per agent");
        }
        Logger.debug("Total time " + (end - start));
    }

    private void microstep() {
        for (TrafficAgent1 agent : manager.getALLAgents()) {
            agent.step(TIME_STEP_MS);
        }
        gui.refresh();
    }
}

