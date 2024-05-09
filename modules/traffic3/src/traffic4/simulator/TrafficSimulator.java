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


import traffic3.manager.TrafficManager;
import traffic3.simulator.TrafficSimulatorGUI;
import traffic3.objects.TrafficAgent;
import traffic3.objects.TrafficBlockade;
import traffic4.objects.TrafficArea;


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

    private TrafficManager manager;

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
        manager.clear();
        for (StandardEntity next : model) {
            if (next instanceof Area) {
                convertAreaToTrafficArea((Area) next);
            }
        }

    }

    private void clearAreaCacheDrone(EntityID entityArea) {
        manager.getTrafficArea((Area) model.getEntity(entityArea)).clearBlockadeCache();
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
        TrafficAgent agent = new TrafficAgent(human, manager, radius, velocityLimit);
        agent.setLocation(human.getX(), human.getY());
        manager.register(agent);
    }

    private void convertGround() {}

    private void handleMove(AKMove move) {
        Human human = (Human) model.getEntity(move.getAgentID());
        TrafficAgent agent = manager.getTrafficAgent(human);
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
        List<PathElement> steps = new ArrayList<PathElement>();
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
    }
}

