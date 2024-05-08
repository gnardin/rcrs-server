package traffic4.objects;

import java.awt.geom.Line2D;
import java.util.*;
import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import org.apache.log4j.NDC;

//import rescuecore2.log.Logger;

import rescuecore2.components.AbstractAgent;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.misc.gui.ShapeDebugFrame;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Edge;
import traffic3.objects.TrafficAgent;
import traffic3.objects.TrafficBlockade;
import traffic3.simulator.TrafficSimulator;

import com.infomatiq.jsi.Rectangle;

public class TrafficArea {
    private Collection<TrafficAgent> agents;
    private List<Line2D> areaLines;

    private Area area;
    private Rectangle bounds;
    private Vector2D baseVec;
    private ArrayList<Line2D> openLines;
    private int[][] graph;

    /**
     * Construct a traffic area
     *
     * @param area
     */
    public TrafficArea(final Area area) {
        this.area = area;
        agents = new HashSet<TrafficAgent>();
        areaLines = null;
        Rectangle2D rect = area.getShape().getBounds2D();
        bounds = new Rectangle((float) rect.getMinX(), (float) rect.getMinY(), (float) rect.getMaxX(), (float) rect.getMaxY());
    }

    /**
     * Get the wrapped area.
     *
     * @return The wrapped area.
     */
    public Area getArea() {
        return area;
    }

    /**
     * Get the bounding rectangle.
     *
     * @return The bounding rectangle.
     */
    public Rectangle getBounds() {
        return bounds;
    }

    public List<Line2D> getAreaLines() {
        if (areaLines == null) {
            areaLines = new ArrayList<Line2D>();
            for (Edge edge : area.getEdges()) {
                //areaLines.add(edge.getLine());
            }
        }
        return Collections.unmodifiableList(areaLines);
    }

    /**
	 * Find out whether this area contains a point (x, y).
	 *
	 * @param x
	 *            The X coordinate to test.
	 * @param y
	 *            The Y coordinate to test.
	 * @return True if and only if this area contains the specified point.
	 */
	public boolean contains(double x, double y) {
		return area.getShape().contains(x, y);
	}

	/**
	 * Add an agent to this area.
	 *
	 * @param agent
	 *            The agent to add.
	 */
	public void addAgent(TrafficAgent agent) {
		agents.add(agent);
	}

	/**
	 * Remove an agent from this area.
	 *
	 * @param agent
	 *            The agent to remove.
	 */
	public void removeAgent(TrafficAgent agent) {
		agents.remove(agent);
	}

	/**
	 * Get all agents in this area.
	 *
	 * @return All agents inside this area.
	 */
	public Collection<TrafficAgent> getAgents() {
		return Collections.unmodifiableCollection(agents);
	}



}

