package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.Action;
import javax.swing.AbstractAction;

import java.util.Comparator;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;

import rescuecore2.misc.Pair;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.config.Config;
import rescuecore2.standard.entities.*;
import rescuecore2.view.Icons;
import rescuecore2.log.Logger;

/**
 * A view layer that renders robots
 */
public class RobotLayer extends StandardEntityViewLayer<Robot> {
    private static final int SIZE = 10;

    private static final int HP_MAX = 10000;
    private static final int HP_INJURED = 5400;
    private static final int HP_CRITICAL = 1000;

    private static final String ICON_SIZE_KEY = "view.standard.human.icons.size";
    private static final String USE_ICONS_KEY = "view.standard.human.icons.use";
    private static final int DEFAULT_ICON_SIZE = 32;

    private static RobotSorter ROBOT_SORTER = null;

    private static final Color DRONE_COLOUR = Color.CYAN;
    private static final Color DEAD_COLOUR = Color.BLACK;

    private int iconSize;
    private boolean useIcons;
    private Action useIconsAction;
    private Map<String, Map<State, Icon>> icons;

    /**
     * Construct a human view layer
     */
    public RobotLayer() {
        super(Robot.class);
        iconSize = DEFAULT_ICON_SIZE;
    }

    @Override
    public void initialise(Config config) {
        iconSize = config.getIntValue(ICON_SIZE_KEY, DEFAULT_ICON_SIZE);
        icons = new HashMap<String, Map<State, Icon>>();
        useIcons = config.getBooleanValue(USE_ICONS_KEY, false);
        icons.put(StandardEntityURN.DRONE.toString(), generateIconMap("DroneTeam"));
        useIconsAction = new UseIconsAction();
    }

    @Override
    public String getName() {
        return "Drones";
    }

    @Override
    public Shape render(Robot r, Graphics2D g, ScreenTransform s) {
        Pair<Integer, Integer> location = getLocation(r);
        if (location == null) {
            return null;
        }
        int x = s.xToScreen(location.first());
        int y = s.yToScreen(location.second());
        Shape shape;
        Icon icon = useIcons ? getIcon(r) : null;
        if (icon == null) {
            if (r.isPositionDefined() && (world.getEntity(r.getPosition()) instanceof Drone)) {
                shape = new Ellipse2D.Double(x - SIZE / 3, y - SIZE / 3, SIZE/3*2, SIZE/3*2);
            } else {
                shape = new Ellipse2D.Double(x - SIZE / 2, y - SIZE / 2, SIZE, SIZE);
            }


            g.setColor(adjustColour(getColour(r), r.isHPDefined() ? r.getHP() : 10000));
            g.fill(shape);
            g.setColor(getColour(r));
            g.draw(shape);
        }
        else {
            x -= icon.getIconWidth() / 2;
            y -= icon.getIconHeight() / 2;
            shape = new Rectangle2D.Double(x, y, icon.getIconWidth(), icon.getIconHeight());
            icon.paintIcon(null, g, x, y);
        }
        return shape;
    }

    @Override
    public List<JMenuItem> getPopupMenuItems() {
        List<JMenuItem> result = new ArrayList<JMenuItem>();
        result.add(new JMenuItem(useIconsAction));
        return result;
    }

    @Override
    protected void postView() {
        if(world == null)
            return;
        if(ROBOT_SORTER == null)
            ROBOT_SORTER = new RobotSorter(world);
        Collections.sort(entities, ROBOT_SORTER);
    }


    protected Pair<Integer, Integer> getLocation(Robot r) {
        return r.getLocation(world);
    }

    private Map<State, Icon> generateIconMap(String type) {
        Map<State, Icon> result = new EnumMap<State, Icon>(State.class);
        for (State state : State.values()) {
            String resourceName = "rescuecore2/standard/view/" + type + "-" + state.toString() + "-" + iconSize + "x" + iconSize + ".png";
            URL resource = RobotLayer.class.getClassLoader().getResource(resourceName);
            if (resource == null) {
                Logger.warn("Could not find resource: " + resourceName);
            }
            else {
                result.put(state, new ImageIcon(resource));
            }
        }
        return result;
    }

    private Color getColour(Robot r)  {
        switch (r.getStandardURN()) {
            case DRONE:
                return DRONE_COLOUR;
            default:
                throw new IllegalArgumentException("Dont know how to draw robot of type " + r.getStandardURN());
        }
    }

    private Icon getIcon(Robot r) {
        State state = getState(r);
        Map<State, Icon> iconMap = null;
        switch (r.getStandardURN()) {
            case DRONE:
                iconMap = icons.get(StandardEntityURN.DRONE.toString() + "Drone");
                break;
            default:
                iconMap = icons.get(r.getStandardURN().toString());
        }
        if (iconMap == null) {
            return null;
        }
        return iconMap.get(state);
    }

    private Color adjustColour(Color c, int hp) {
        if (hp == 0) {
            return DEAD_COLOUR;
        }
        if (hp < HP_CRITICAL) {
            c = c.darker();
        }
        if (hp < HP_INJURED) {
            c = c.darker();
        }
        if (hp < HP_MAX) {
            c = c.darker();
        }

        return c;
    }

    private BatteryLevel getBattery(Robot r) {
        int batteryLevel = r.getBattery();
        if (batteryLevel <= 0) {
            return BatteryLevel.DEAD;
        }
        if (batteryLevel <= 20) {
            return BatteryLevel.LOW;
        }
        if (batteryLevel <= 50) {
            return BatteryLevel.MEDIUM;
        }
        if (batteryLevel <= 100) {
            return BatteryLevel.HIGH;
        }

        return BatteryLevel.HIGH;
    }

    private RobotLayer.State getState(Robot r) {
        int hp = r.getHP();
        if (hp <= 0) {
            return RobotLayer.State.DEAD;
        }
        if (hp <= HP_CRITICAL) {
            return RobotLayer.State.CRITICAL;
        }
        if (hp <= HP_INJURED) {
            return RobotLayer.State.INJURED;
        }
        return RobotLayer.State.HEALTHY;
    }

    private enum State {
        HEALTHY {
            @Override
            public String toString() {
                return "Healthy";
            }
        },
        INJURED {
            @Override
            public String toString() {
                return "Injured";
            }
        },
        CRITICAL {
            @Override
            public String toString() {
                return "Critical";
            }
        },
        DEAD {
            @Override
            public String toString() {
                return "Dead";
            }
        };
    }

    private enum BatteryLevel {
        HIGH {
            @Override
            public String toString() {
                return "100%";
            }
        },
        MEDIUM {
            @Override
            public String toString() {
                return "50%";
            }
        },
        LOW {
            @Override
            public String toString() {
                return "20%";
            }
        },
        DEAD {
            @Override
            public String toString() {
                return "0%";
            }
        }
    }

    private static final class RobotSorter implements Comparator<Robot>, java.io.Serializable {
        private final StandardWorldModel world;

        public RobotSorter(StandardWorldModel world) {
            this.world = world;
        }

        @Override
        public int compare(Robot r1, Robot r2) {
            if(r1.isPositionDefined() && r2.isPositionDefined()) {
                if(world.getEntity(r1.getPosition()) instanceof Drone && !(world.getEntity(r2.getPosition()) instanceof Drone))
                    return 1;
                if(!(world.getEntity(r1.getPosition()) instanceof Drone) && (world.getEntity(r2.getPosition()) instanceof Drone))
                    return -1;
            }

            if(r1.isHPDefined() && r2.isHPDefined())
                return r2.getHP() - r1.getHP();
            return r1.getID().getValue() - r2.getID().getValue();
        }
    }

    private final class UseIconsAction extends AbstractAction {
        public UseIconsAction() {
            super("Use icons");
            putValue(Action.SELECTED_KEY, Boolean.valueOf(useIcons));
            putValue(Action.SMALL_ICON, useIcons ? Icons.TICK : Icons.CROSS);
            component.repaint();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            useIcons = !useIcons;
            putValue(Action.SELECTED_KEY, Boolean.valueOf(useIcons));
            putValue(Action.SMALL_ICON, useIcons ? Icons.TICK : Icons.CROSS);
            component.repaint();
        }
    }




}
