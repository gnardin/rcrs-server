package traffic4.objects;

//import java.awt.geom.Line2D;
import java.awt.*;
import java.util.*;
import java.awt.geom.Rectangle2D;
import java.util.List;

//import rescuecore2.log.Logger;

import org.apache.commons.compress.harmony.pack200.NewAttributeBands;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.misc.geometry.Line2D;
import org.apache.log4j.Logger;


import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Edge;
import rescuecore2.misc.gui.ShapeDebugFrame;
import traffic4.simulator.TrafficSimulator;

import com.infomatiq.jsi.Rectangle;

/**
 * This class wraps an Area object with some extra information.
 * Does not include blockades
 */
public class TrafficArea1 {
	private Collection<TrafficAgent1> agents;
	private Collection<TrafficBlockade1> blocks;

	private List<Line2D> blockadeLines;
	private List<Line2D> blockingLines;
	private List<Line2D> allBlockingLines;
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
	public TrafficArea1(final Area area) {
		this.area = area;
		agents = new HashSet<TrafficAgent1>();
		blocks = new HashSet<TrafficBlockade1>();
		blockingLines = null;
		blockadeLines = null;
		areaLines = null;
		allBlockingLines = null;
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

	/**
	 * Get all lines around this area that block movement.
	 *
	 * @return All area lines that block movement.
	 */
	public List<Line2D> getBlockingLines() {
		if (blockingLines == null) {
			blockingLines = new ArrayList<Line2D>();
			for (Edge edge : area.getEdges()) {
				if (!edge.isPassable()) {
					blockingLines.add(edge.getLine());
				}
			}
		}
		return Collections.unmodifiableList(blockingLines);
	}


	public List<Line2D> getAreaLines() {
		if (areaLines == null) {
			areaLines = new ArrayList<Line2D>();
			for (Edge edge : area.getEdges()) {
				areaLines.add(edge.getLine());
			}
		}
		return Collections.unmodifiableList(areaLines);
	}

	public List<Line2D> getBlockadeLines() {
		if (blockadeLines == null) {
			blockadeLines = new ArrayList<Line2D>();
			for (TrafficBlockade1 blockade : blocks) {
				blockadeLines.addAll(blockade.getLines());
			}
		}
		return Collections.unmodifiableList(blockadeLines);
	}

	/**
	 * Get all lines that block movement. This includes impassable edges of the
	 * area and all blockade lines.
	 *
	 * @return All movement-blocking lines.
	 */
	public List<Line2D> getAllBlockingLines() {
		if (allBlockingLines == null) {
			allBlockingLines = new ArrayList<Line2D>();
			allBlockingLines.addAll(getBlockadeLines());
			allBlockingLines.addAll(getBlockingLines());
		}
		return Collections.unmodifiableList(allBlockingLines);
	}

	/**
	 * Find out whether this area contains a point (x, y).
	 *
	 * @param x The X coordinate to test.
	 * @param y The Y coordinate to test.
	 * @return True if and only if this area contains the specified point.
	 */
	public boolean contains(double x, double y) {
		return area.getShape().contains(x, y);
	}

	/**
	 * Add an agent to this area.
	 *
	 * @param agent The agent to add.
	 */
	public void addAgent(TrafficAgent1 agent) {
		agents.add(agent);
	}

	/**
	 * Remove an agent from this area.
	 *
	 * @param agent The agent to remove.
	 */
	public void removeAgent(TrafficAgent1 agent) {
		agents.remove(agent);
	}

	/**
	 * Get all agents in this area.
	 *
	 * @return All agents inside this area.
	 */
	public Collection<TrafficAgent1> getAgents() {
		return Collections.unmodifiableCollection(agents);
	}

	/**
	 * Add a traffic blockade
	 *
	 * @param block
	 */
	public void addBlockade(TrafficBlockade1 block) {
		blocks.add(block);
		clearBlockadeCache();
	}

	/**
	 * Remove a TrafficBlockade
	 *
	 * @param block
	 * 				The blockade to remove
	 */
	public void removeBlockade(TrafficBlockade1 block) {
		blocks.remove(block);
		clearBlockadeCache();
	}

	/**
	 * Clear any cached blockades
	 */
	public void clearBlockadeCache() {
		blockadeLines = null;
		allBlockingLines = null;
		openLines = null;
		graph = null;
	}

	/**
	 * Get all TrafficBlockades inside this area.
	 *
	 * @return All TrafficBlockades in this area.
	 */
	public Collection<TrafficBlockade1> getBlockades() {
		return Collections.unmodifiableCollection(blocks);
	}

	@Override
	public String toString() {
		return "Traffic Area (" + area + ")";
	}

	public int getNearestLineIndex(Point2D point) {
		List<Line2D> oLines = getOpenLines();
		double minDst = Integer.MAX_VALUE;
		int minIndex = -1;
//		FOR:
		for (int i = 0; i < oLines.size(); i++) {
//			Line2D line = new Line2D(point,getMidPoint(oLines.get(i).getOrigin(), oLines.get(i).getEndPoint()));
//			for (Line2D is :getAllBlockingLines()) {
//				if (GeometryTools2D.getSegmentIntersectionPoint(line, is) != null) {
//					continue FOR;
//				}
//			}
//			for (int k = 0; k < oLines.size(); k++) {
//				if(k==i)
//					continue;
//				if (GeometryTools2D.getSegmentIntersectionPoint(line, oLines.get(k)) == null) {
//					continue FOR;
//				}
//			}
			Point2D nearestPoint = GeometryTools2D.getClosestPointOnSegment(oLines.get(i), point);
			double dst = GeometryTools2D.getDistance(point, nearestPoint);
			if (dst < minDst) {
				minDst = dst;
				minIndex = i;
			}
//			return i;
		}
		return minIndex;
	}

	public int[][] getGraph() {
		if (graph == null) {
			List<Line2D> openLines = getOpenLines();
			graph = new int[openLines.size()][openLines.size()];
			for (int i = 0; i < graph.length; i++) {
				FOR: for (int j = 0; j < graph.length; j++) {
					Line2D line = new Line2D(getMidPoint(openLines.get(i).getOrigin(), openLines.get(i).getEndPoint()),
							getMidPoint(openLines.get(j).getOrigin(), openLines.get(j).getOrigin()));
					for (Line2D is : getAllBlockingLines()) {
						if (GeometryTools2D.getSegmentIntersectionPoint(line, is) != null) {
							graph[i][j] = 100000;
							continue FOR;
						}
					}
					for (int k = 0; k < openLines.size(); k++) {
						if (k == i || k == j)
							continue;
						if (GeometryTools2D.getSegmentIntersectionPoint(line, openLines.get(k)) != null) {
							graph[i][j] = Integer.MAX_VALUE;
							continue FOR;
						}
					}
					graph[i][j] = 1;
				}
			}
		}
		return graph;
	}

	private Point2D getMidPoint(Point2D p1, Point2D p2) {
		return new Point2D((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2);
	}

	public List<Line2D> getOpenLines() {
		if (openLines == null) {
			openLines = new ArrayList<Line2D>();
			HashSet<Point2D> checkedPoint = new HashSet<Point2D>();
			for (Line2D line : getBlockadeLines()) {
				if (!checkedPoint.contains(line.getOrigin())) {
					createLine(line.getOrigin(), openLines);
				}
				if (!checkedPoint.contains(line.getEndPoint())) {
					createLine(line.getEndPoint(), openLines);
				}
				checkedPoint.add(line.getOrigin());
				checkedPoint.add(line.getEndPoint());
			}
//			createPassableEdges(openLines);

		}

		return Collections.unmodifiableList(openLines);
	}


	private void createPassableEdges(List<Line2D> openLines) {
		for (Edge edge : getArea().getEdges()) {
			if (edge.isPassable()) {
				List<Line2D> edgeLines = new ArrayList<Line2D>();
				edgeLines.add(edge.getLine());
				for (Line2D line : getBlockingLines()) {
					List<Line2D> old = edgeLines;
					edgeLines = minus(edgeLines, line);
				}
				openLines.addAll(edgeLines);
			}

		}
	}

	private static List<Line2D> minus(List<Line2D> edgeLines, Line2D line) {
		List<Line2D> result = new ArrayList<Line2D>();
		for (Line2D edgeLine : edgeLines) {
			 System.out.println("Edge====");
			 System.out.println(edgeLine);
			 System.out.println(line);
			Line2D clone = new Line2D(edgeLine.getOrigin(), edgeLine.getEndPoint());
			boolean lineContaintEdgeOrigin = GeometryTools2D.contains(line, edgeLine.getOrigin());
			boolean lineContaintEdgeEnd = GeometryTools2D.contains(line, edgeLine.getEndPoint());
			boolean edgeContaintLineOrigin = GeometryTools2D.contains(edgeLine, line.getOrigin());
			boolean edgeContaintLineEnd = GeometryTools2D.contains(edgeLine, line.getEndPoint());
			if (lineContaintEdgeOrigin & lineContaintEdgeEnd)
				continue;

			if (edgeContaintLineOrigin & edgeContaintLineEnd) {
				Line2D firstLine;
				Line2D secondLine;
				double distanceoo = GeometryTools2D.getDistance(edgeLine.getOrigin(), line.getOrigin());
				double distanceoe = GeometryTools2D.getDistance(edgeLine.getOrigin(), line.getEndPoint());
				double distanceeo = GeometryTools2D.getDistance(edgeLine.getEndPoint(), line.getOrigin());
				double distanceee = GeometryTools2D.getDistance(edgeLine.getEndPoint(), line.getEndPoint());
				if (distanceoo < distanceoe) {
					firstLine = new Line2D(edgeLine.getOrigin(), line.getOrigin());
					secondLine = new Line2D(line.getEndPoint(), edgeLine.getEndPoint());
				} else {
					firstLine = new Line2D(edgeLine.getOrigin(), line.getEndPoint());
					secondLine = new Line2D(line.getOrigin(), edgeLine.getEndPoint());
				}
				// System.out.println(distanceoo);
				// System.out.println(distanceoe);
				// System.out.println(distanceeo);
				// System.out.println(distanceee);
				if (!GeometryTools2D.nearlyZero(distanceoo) && !GeometryTools2D.nearlyZero(distanceoe))
					result.add(firstLine);
				if (!GeometryTools2D.nearlyZero(distanceeo) && !GeometryTools2D.nearlyZero(distanceee))
					result.add(secondLine);
				continue;
			}
			if (lineContaintEdgeOrigin) {
				if (edgeContaintLineOrigin)
					clone.setOrigin(line.getOrigin());
				else if (edgeContaintLineEnd)
					clone.setOrigin(line.getEndPoint());
				else
					System.err.println("why?");
			}
			if (lineContaintEdgeEnd) {
				if (edgeContaintLineOrigin)
					clone.setEnd(line.getOrigin());
				else if (edgeContaintLineEnd)
					clone.setEnd(line.getEndPoint());
				else
					System.err.println("why?2");
			}

			result.add(clone);
		}
		return result;
	}

	private double getDistance(Line2D s1, Point2D p) {
		Point2D p1 = GeometryTools2D.getClosestPointOnSegment(s1, p);
		return GeometryTools2D.getDistance(p1, p);
	}

	private double getDistance(Line2D e1, Line2D e2) {
		double d1 = getDistance(e1, e2.getOrigin());
		double d2 = getDistance(e1, e2.getEndPoint());
		double d3 = getDistance(e2, e1.getOrigin());
		double d4 = getDistance(e2, e1.getEndPoint());
		d1 = Math.min(d1, d2);
		d2 = Math.min(d4, d3);
		return Math.min(d1, d2);
	}

	private Vector2D getBaseVectorDrone() {
		if (baseVec == null) {
			baseVec = new Vector2D(10, 10);
		}
		return baseVec;
	}

	private List<Line2D> getAllBlockingAreaLines() {
		List<Line2D> lines = new ArrayList<Line2D>(getBlockadeLines());
//		List<Line2D> lines = new ArrayList<Line2D>();
		lines.addAll(getAreaLines());
		return lines;
	}

	private void createLine(Point2D origin, List<Line2D> openLines) {

		Line2D newLineToUp = new Line2D(origin, getBaseVectorDrone().scale(900));
		Line2D newLineToDown = new Line2D(origin, getBaseVectorDrone().scale(-900));
		for (Line2D line : getAllBlockingAreaLines()) {
			if (line.getOrigin().equals(origin) || line.getEndPoint().equals(origin))
				continue;
			double distance1 = GeometryTools2D.getDistance(newLineToUp.getOrigin(), newLineToUp.getEndPoint());
			 newLineToUp.getDirection().scale(distance1);
			Point2D point1 = GeometryTools2D.getSegmentIntersectionPoint(newLineToUp, line);
			if (point1 != null && !GeometryTools2D.nearlyZero(distance1))
				newLineToUp.setEnd(point1);
			double distance2 = GeometryTools2D.getDistance(newLineToDown.getOrigin(), newLineToDown.getEndPoint());
//			 newLineToDown.senewLineToDown.getDirection().scale(distance2);
			Point2D point2 = GeometryTools2D.getSegmentIntersectionPoint(newLineToDown, line);
			if (point2 != null && !GeometryTools2D.nearlyZero(distance2))
				newLineToDown.setEnd(point2);
		}
		boolean isValidLineToUp = isValidLine(newLineToUp);
		boolean isValidLineToDown = isValidLine(newLineToDown);

		if (isValidLineToUp)
			openLines.add(newLineToUp);
		if (isValidLineToDown)
			openLines.add(newLineToDown);
	}

	private boolean isValidLine(Line2D line) {
		Point midPoint = new Point((int) (line.getOrigin().getX() + line.getEndPoint().getX()) / 2, (int) (line.getOrigin().getY() + line.getEndPoint().getY()) / 2);
		if (!getArea().getShape().contains(midPoint))
			return false;
//		for (TrafficBlockade1 blockade : getBlockades()) {
//			if (blockade.getBlockade().getShape().contains(midPoint))
//				return false;
//		}
		return true;
	}


	@Override
	public int hashCode() {
		return area.getID().hashCode();
	}
}


