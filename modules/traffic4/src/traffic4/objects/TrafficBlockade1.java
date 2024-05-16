package traffic4.objects;

import rescuecore.view.Line;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.standard.entities.Blockade;

import rescuecore2.misc.geometry.Line2D;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityListener;
import rescuecore2.worldmodel.Property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrafficBlockade1 {
    private Blockade b;
    private TrafficArea1 a;
    private List<Line2D> lines = new ArrayList<Line2D>();

    /**
     * Construct a Traffic blockade object
     */
    public TrafficBlockade1(final Blockade blockade, TrafficArea1 area) {
        this.a = area;
        this.b = blockade;
        lines = null;
        blockade.addEntityListener(new EntityListener() {
            @Override
            public void propertyChanged(Entity e, Property p, Object oldValue, Object newValue) {
                if (p == blockade.getApexesProperty()) {
                    lines = null;
                }
            }
        });
    }

    /**
     * Get the lines that make up the outline of this blockade
     */
    public List<Line2D> getLines() {
        if (lines == null) {
            lines = new ArrayList<Line2D>();
            int[] apexes = b.getApexes();
            for (int i = 0; i < apexes.length - 3; i += 2) {
                Point2D first = new Point2D(apexes[i], apexes[i + 1]);
                Point2D second = new Point2D(apexes[i + 2], apexes[i + 3]);
                lines.add(new Line2D(first, second));
            }
        }
        return Collections.unmodifiableList(lines);
    }

    /**
     * Get the wrapped Blockade
     * @return the blockade
     */
    public Blockade getBlockade() {
        return b;
    }

    /**
     * Get the containing Traffic Area
     * @return the blockade
     */
    public TrafficArea1 getArea() {
        return a;
    }

    /**
     * Find out whether the blockade contains an x,y coordinate.
     * @param x x coordinate
     * @paran y y coordinate
     * @return True
     */
    public boolean containsLoc(double x , double y) {
        return b.getShape().contains(x, y);
    }

    @Override
    public int hashCode() {
        return b.getID().hashCode();
    }






}
