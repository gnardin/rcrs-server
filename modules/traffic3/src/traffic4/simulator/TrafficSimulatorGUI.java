package traffic4.simulator;

import traffic4.manager.TrafficManager1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CountDownLatch;

public class TrafficSimulatorGUI extends JPanel {



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


    public TrafficSimulatorGUI(TrafficManager1 manager) {
        super(new BorderLayout());
        this.manager = manager;
        waitOnRefresh = false;

        view = new WorldView();
        cont = new JButton("Continue");
        wait = new JCheckBox("Wat on refresh", waitOnRefresh);
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
    }



    private class WorldView extends JComponent {

    }
}
