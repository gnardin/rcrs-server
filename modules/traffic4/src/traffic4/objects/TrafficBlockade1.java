package traffic4.objects;

import org.checkerframework.checker.signature.qual.CanonicalNameOrEmpty;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.EntityListener;

import rescuecore2.standard.entities.Blockade;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class TrafficBlockade1 {
    private Blockade blockade;
    private TrafficArea1 area1;
    private List<Line2D> lines = new ArrayList<Line2D>();

    /**
     * COnstructing a new traffic blockade
     * @param blockade The wrapped blockade.
     * @param area1 The traffic area.
     */
    public TrafficBlockade1(final Blockade blockade, TrafficArea1 area1) {
        this.blockade = blockade;
        this.area1 = area1;
        lines = null;
        blockade.addEntityListener(new EntityListener() {
            @Override
            public void propertyChanged(Entity e, Property p, Object oldValue, Object newValue) {
                if ( p == blockade.getApexesProperty() ) {
                    lines = null;
                }
            }
        });
    }

    /**
     * Get the lines that make up the blockade
     * @return the list of blockade lines.
     */
    public List<Line2D> getLines() {
        if (lines == null) {
            lines = new ArrayList<Line2D>();
            int[] apexes = blockade.getApexes();
            for (int i = 0; i < apexes.length - 3; i += 2) {
                Point2D first = new Point2D(apexes[i], apexes[i + 1]);
                Point2D second = new Point2D(apexes[i + 2], apexes[i + 3]);
                lines.add(new Line2D(first, second));
            }
            lines.add(new Line2D(new Point2D(apexes[apexes.length - 2], apexes[apexes.length - 1]), new Point2D(apexes[0], apexes[1])));
        }
        return Collections.unmodifiableList(lines);
    }

    /**
     * Get the wrapped blockade
     * @return the blockade
     */
    public Blockade getBlockade() {
        return blockade;
    }

    /**
     * Get the containing TrafficArea
     * @return the TrafficArea.
     */
    public TrafficArea1 getArea() {
        return area1;
    }

    public boolean contains(double x, double y) {
        return blockade.getShape().contains(x, y);
    }

    @Override
    public int hashCode() {
        return blockade.getID().hashCode();
    }

}
