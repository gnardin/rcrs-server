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
		blockingLines = null;
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

	/**
	 * Get all lines that block movement. This includes impassable edges of the
	 * area and all blockade lines.
	 *
	 * @return All movement-blocking lines.
	 */
	public List<Line2D> getAllBlockingLines() {
		if (allBlockingLines == null) {
			allBlockingLines = new ArrayList<Line2D>();
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


//	/**
//	 * Get all TrafficBlockades inside this area.
//	 *
//	 * @return All TrafficBlockades in this area.
//	 */
//	public Collection<TrafficBlockade1> getBlockades() {
//		return Collections.unmodifiableCollection(blocks);
//	}

	@Override
	public String toString() {
		return "Traffic Area (" + area + ")";
	}

	public int getNearestLineIndex(Point2D point) {
		List<Line2D> oLines = getOpenLines();
		double minDst = Integer.MAX_VALUE;
		int minIndex = -1;
		/*FOR:*/
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
			//return i;
		}
		return minIndex;
	}

	public int[][] getGraph() {
		if (graph == null) {
			List<Line2D> openLines = getOpenLines();
			graph = new int[openLines.size()][openLines.size()];
			for (int i = 0; i < graph.length; i++) {
				FOR:
				for (int j = 0; j < graph.length; j++) {
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
			for (Line2D line : getBlockingLines()) {
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
//			TrafficSimulator.debug.show("Full Lines", new ShapeDebugFrame.AWTShapeInfo(getArea().getShape(), getArea() + "", Color.blue, false),
//					new ShapeDebugFrame.Line2DShapeInfo(openLines, "openLines", Color.green, false, true)
//
//			);
		}

		return Collections.unmodifiableList(openLines);
	}
//
//	private Line2D[] getBlockadeLines() {
//		return null;
//	}

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
			// System.out.println("Edge====");
			// System.out.println(edgeLine);
			// System.out.println(line);
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

	private List<Line2D> getAllAreaLines() {
		List<Line2D> lines = new ArrayList<Line2D>(getBlockingLines());
		lines.addAll(getAreaLines());
		return lines;
	}

	private void createLine(Point2D origin, List<Line2D> openLines) {

		Line2D newLineToUp = new Line2D(origin, getBaseVectorDrone().scale(1000));
		Line2D newLineToDown = new Line2D(origin, getBaseVectorDrone().scale(-1000));
//		TrafficSimulator.debug.show("Init", new ShapeDebugFrame.AWTShapeInfo(getArea().getShape(), getArea() + "", Color.blue, false), new ShapeDebugFrame.Line2DShapeInfo(
//				newLineToUp, "lineToUp", Color.red, false, true), new ShapeDebugFrame.Line2DShapeInfo(newLineToDown, "lineTodown", Color.green, false, true));

		for (Line2D line : getAllAreaLines()) {
			if (line.getOrigin().equals(origin) || line.getEndPoint().equals(origin))
				continue;
			double distance1 = GeometryTools2D.getDistance(newLineToUp.getOrigin(), newLineToUp.getEndPoint());
			// newLineToUp.getDirection().scale(distance1);
			Point2D point1 = GeometryTools2D.getSegmentIntersectionPoint(newLineToUp, line);
			if (point1 != null && !GeometryTools2D.nearlyZero(distance1))
				newLineToUp.setEnd(point1);
			double distance2 = GeometryTools2D.getDistance(newLineToDown.getOrigin(), newLineToDown.getEndPoint());
			// newLineToDown.senewLineToDown.getDirection().scale(distance2);
			Point2D point2 = GeometryTools2D.getSegmentIntersectionPoint(newLineToDown, line);
			if (point2 != null && !GeometryTools2D.nearlyZero(distance2))
				newLineToDown.setEnd(point2);
//			TrafficSimulator.debug.show(
//					"Checking",
//					// new ShapeDebugFrame.AWTShapeInfo(getArea().getShape(),
//					// getArea()+"", Color.blue, false),
//					new ShapeDebugFrame.Line2DShapeInfo(line, "checkline", Color.MAGENTA, false, true), new ShapeDebugFrame.Line2DShapeInfo(newLineToUp, "lineToUp " + distance1,
//							Color.red, false, true), new ShapeDebugFrame.Line2DShapeInfo(newLineToDown, "lineTodown " + distance2, Color.green, false, true),
//					new ShapeDebugFrame.Point2DShapeInfo(point1, "Point1" + point1, Color.red, true), new ShapeDebugFrame.Point2DShapeInfo(point2, "Point2" + point2, Color.green,
//							true));
		}
		boolean isValidLineToUp = isValidLine(newLineToUp);
		boolean isValidLineToDown = isValidLine(newLineToDown);
//		TrafficSimulator.debug.show("Final",
//		// new ShapeDebugFrame.AWTShapeInfo(getArea().getShape(), getArea()+"",
//		// Color.blue, false),
//				new ShapeDebugFrame.Line2DShapeInfo(newLineToUp, "lineToUp:" + isValidLineToUp, Color.red, isValidLineToUp, true), new ShapeDebugFrame.Line2DShapeInfo(
//						newLineToDown, "lineTodown:" + isValidLineToDown, Color.green, isValidLineToDown, true));
		if (isValidLineToUp)
			openLines.add(newLineToUp);
		if (isValidLineToDown)
			openLines.add(newLineToDown);
	}

	private boolean isValidLine(Line2D line) {
		Point midPoint = new Point((int) (line.getOrigin().getX() + line.getEndPoint().getX()) / 2, (int) (line.getOrigin().getY() + line.getEndPoint().getY()) / 2);
		if (!getArea().getShape().contains(midPoint)) {
			return false;
//		} for (TrafficBlockade1 blockade : getBlockades()) {
//			if (blockade.getBlockade().getShape().contains(midPoint)) {
//				return true;
//			}
//		}
		}
		return true;
	}

	@Override
	public int hashCode() {
		return area.getID().hashCode();
	}
}


