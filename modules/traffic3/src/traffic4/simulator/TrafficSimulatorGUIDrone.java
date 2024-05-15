package traffic4.simulator;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import rescuecore2.log.Logger;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.gui.PanZoomListener;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Edge;
import traffic4.manager.TrafficManager1;
import traffic4.objects.TrafficAgent1;
import traffic4.objects.TrafficArea1;


import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CountDownLatch;

public class TrafficSimulatorGUIDrone extends JPanel {
    private static final Color SELECTED_AREA_COLOUR = new Color(0,0,255,128);
    private static final Color AREA_OUTLINE_COLOUR = new Color(0,2,0);
    private static final Color BLOCKADE_COLOUR = new Color(255,255,0);

    private static final Stroke PASSABLE_EDGE_STROKE = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    private static final Stroke IMPASSABLE_EDGE_STROKE = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

    // path node
    private static final int FLIGHT_PATH_NODE_SIZE = 6;
    private static final int TICK_TIME_MS = 10;

    private static final double FORCE_GUI_FACTOR = 1000;

    private TrafficManager1 manager;

    private volatile boolean waitOnRefresh;
    private final Object lock = new Object();
    private CountDownLatch latch;

    private WorldView view;
    private JButton cont;
    private JCheckBox wait;
    private JCheckBox animate;
    private Timer timer;
    private Box verboseBox;


    public TrafficSimulatorGUIDrone(TrafficManager1 manager) {
        super(new BorderLayout());
        this.manager = manager;
        waitOnRefresh = false;

        view = new WorldView();
        cont = new JButton("Continue");
        wait = new JCheckBox("Wait on refresh", waitOnRefresh);
        animate = new JCheckBox("Animate", waitOnRefresh);
        verboseBox = Box.createVerticalBox();
        verboseBox.setBorder(BorderFactory.createTitledBorder("Verbose"));
        cont.setEnabled(false);
        cont.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                synchronized (lock) {
                    if (latch != null) {
                        latch.countDown();
                    }
                }
                cont.setEnabled(false);
            }
        });
        wait.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                waitOnRefresh = wait.isSelected();
            }
        });
        animate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (animate.isSelected()) {
                    timer.start();
                } else {
                    timer.stop();
                }
                cont.setEnabled(false);
            }
        });

        Box buttons = Box.createHorizontalBox();
        buttons.add(wait);
        buttons.add(cont);
        buttons.add(animate);

        add(view, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        add(new JScrollPane(verboseBox, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.EAST);

        timer = new Timer(TICK_TIME_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                synchronized (lock) {
                    if (latch != null) {
                        latch.countDown();
                    }
                }
            }
        });
    }

    /**
     * Initialise the GUI
     */
    public void initialise() {
        view.initialise();
    }

    /**
     * Refresh the view and wait for user input if required.
     */
    public void refresh() {

    }

    public void setWaitOnRefresh(boolean b) {
        waitOnRefresh = b;
    }

    private class WorldView extends JComponent {
        private ScreenTransform transform1;
        private TrafficArea1 selectedArea;
        private TrafficAgent1 selectedAgent;
        private Map<Shape, TrafficArea1> areas;
        private Map<Shape, TrafficAgent1> agents;

        public WorldView() {

        }

        public void initialise() {
            Rectangle2D bounds = null;
            for (TrafficArea1 area : manager.getAllAreas()) {
                Rectangle2D road = area.getArea().getShape().getBounds2D();
                if (bounds == null) {
                    bounds = new Rectangle2D.Double(road.getX(), road.getY(), road.getWidth(), road.getHeight());
                } else {
                    Rectangle2D.union(bounds, road, bounds);
                }
            }
            transform1 = new ScreenTransform(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY());
            new PanZoomListener(this).setScreenTransform(transform1);
            selectedArea = null;
            areas = new HashMap<Shape, TrafficArea1>();
            agents = new HashMap<Shape, TrafficAgent1>();
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Point p = e.getPoint();
                    selectedArea = null;
                    selectedAgent = null;
                    for (Map.Entry<Shape, TrafficArea1> next : areas.entrySet()) {
                        if (next.getKey().contains(p)) {
                            selectedArea = next.getValue();
                        }
                    }
                    for (Map.Entry<Shape, TrafficAgent1> next : agents.entrySet()) {
                        if (next.getKey().contains(p)) {
                            selectedAgent = next.getValue();
                        }
                    }
                    repaint();
                }
            });
        }

        @Override
        public void paintComponent(Graphics g) {
            Logger.pushLogContext("traffic4");
            try {
                int width = getWidth();
                int height = getHeight();
                Insets insets = getInsets();
                width -= insets.left + insets.right;
                height -= insets.top + insets.bottom;
                transform1.rescale(width, height);
            } finally {
                Logger.popLogContext();
            }
        }

        private void drawObjects(Graphics2D g) {
            drawAreas((Graphics2D) g.create());
        }

        private void drawAreas(Graphics2D g) {
            areas.clear();
            for (TrafficArea1 area : manager.getAllAreas()) {
                Path2D shape = new Path2D.Double();
                List<Edge> edges = area.getArea().getEdges();
                Edge e = edges.get(0);
                shape.moveTo(transform1.xToScreen(e.getStartX()), transform1.yToScreen(e.getStartY()));
                for (Edge edge : edges) {
                    shape.lineTo(transform1.xToScreen(edge.getStartX()), transform1.yToScreen(edge.getStartY()));
                }
                if (area == selectedArea) {
                    g.setColor(SELECTED_AREA_COLOUR);
                    g.fill(shape);
                    g.setColor(AREA_OUTLINE_COLOUR);
                    paintEdges(edges, g);
                    int[][] graph = area.getGraph();
                    List<Line2D> openLines = area.getOpenLines();
                    g.setColor(Color.blue);
                    paintLines(openLines, g);
                    g.setColor(Color.orange);
                    for (int i = 0; i < graph.length; i++) {
                        for (int j = 0; j < graph.length; j++) {
                            if (graph[i][j] > 10000)
                                continue;
                            Line2D line = new Line2D(TrafficSimulator.getMidPoint(openLines.get(i).getOrigin(), openLines.get(i).getEndPoint()),
                                    TrafficSimulator.getMidPoint(openLines.get(j).getOrigin(), openLines.get(j).getEndPoint()));
                            paintLine(line, g);
                        }
                    }
                } else {
                    g.setColor(AREA_OUTLINE_COLOUR);
                    paintEdges(edges, g);
                }
                areas.put(shape, area);
            }
        }

        private void paintLines(List<Line2D> lines, Graphics2D graphics) {
            for (Line2D line : lines) {
                paintLine(line, graphics);
            }
        }

        private void paintLine(Line2D line, Graphics2D graphics) {
            graphics.drawLine(transform1.xToScreen(line.getOrigin().getX()), transform1.yToScreen(line.getOrigin().getY()),
                    transform1.xToScreen(line.getEndPoint().getX()), transform1.yToScreen(line.getEndPoint().getY()));
        }

        private void paintEdges(List<Edge> edges, Graphics2D g) {
            for (Edge edge : edges) {
                if (edge.isPassable()) {
                    g.setStroke(PASSABLE_EDGE_STROKE);
                } else {
                    g.setStroke(IMPASSABLE_EDGE_STROKE);
                }
            }
        }



    }
}
