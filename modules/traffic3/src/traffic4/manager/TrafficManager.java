package traffic4.manager;


import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;
import gnu.trove.TIntProcedure;
import rescuecore2.misc.collections.LazyMap;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import traffic3.objects.TrafficAgent;
import traffic3.objects.TrafficArea;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Maintains information about drone objects in traffic simulator
 */
public class TrafficManager {

    private Map<Integer, TrafficArea> areaByID;
    private Map<Area, TrafficArea> areas;
    private Map<Human, TrafficAgent> agents;
    private Map<TrafficArea, Collection<TrafficArea>> areaNeighbours;

    private SpatialIndex index;

    /**
     * Construct a new Traffic manager for the drone agents
     */
    public TrafficManager() {
        areas = new ConcurrentHashMap<Area, TrafficArea>();
        areaByID = new ConcurrentHashMap<Integer, TrafficArea>();
        agents = new ConcurrentHashMap<Human, TrafficAgent>();
        areaNeighbours = new LazyMap<TrafficArea, Collection<TrafficArea>>() {

            @Override
            public Collection<TrafficArea> createValue() {
                return new HashSet<TrafficArea>();
            }
        };
        index = new RTree();
        index.init(new Properties());
    }

    /**
     * Find the area that contains a point.
     *
     * @param x
     *   The X coordinate.
     * @param y
     *   The Y coordinate.
     *
     * @return The TrafficArea that contains the given point, or null if no such
     * area is found.
     */
    public TrafficArea findArea(double x, double y) {
        final List<TrafficArea> found = new ArrayList<TrafficArea>();
        index.intersects(new Rectangle((float) x, (float) y, (float) x, (float) y),
                new TIntProcedure() {
                    @Override
                    public boolean execute(int value) {
                        found.add(areaByID.get(value));
                        return true;
                    }
                });
        for (TrafficArea next : found) {
            if (next.contains(x, y)) {
                return next;
            }
        }

        return null;
    }

    /**
     * Get the neighbouring areas to a Traffic Area
     */
    public Collection<TrafficArea> getNeighbours(TrafficArea area) {
        return areaNeighbours.get(area);
    }

    /**
     * Get all agents in the neigbouring areas
     *
     * @param agent The agent to look up.
     *
     * @return all agents
     */
    public Collection<TrafficAgent> getNeighbouringAgents(TrafficAgent agent) {
        Set<TrafficAgent> result = new HashSet<TrafficAgent>();
        result.addAll(agent.getArea().getAgents());
        for (TrafficArea next : getNeighbours(agent.getArea())) {
            result.addAll(next.getAgents());
        }
        result.remove(agent);
        return result;
    }

    /**
     * Removing all objects from this traffic manager
     */
    public void clearObject() {
        areas.clear();
        agents.clear();
        areaNeighbours.clear();
        areaByID.clear();
        index = new RTree();
        index.init(new Properties());
    }

    /**
     * Registering a new traffic agent
     */
    public void register(TrafficAgent agent) {
        agents.put(agent.getHuman(), agent);
    }

    /**
     * Get all traffic agents
     *
     * @return all traffic agents
     */
    public Collection<TrafficAgent> getALLAgents() {
        return Collections.unmodifiableCollection(agents.values());
    }

    /**
     * Get all traffic areas.
     *
     * @return all traffic areas.
     */
    public Collection<TrafficArea> getAllAreas() {
        return Collections.unmodifiableCollection(areas.values());
    }

    /**Compute pre cached information about the world.
     *
     * @param world
     *      The world model
     */
    public void cacheInfo(StandardWorldModel world) {
        areaNeighbours.clear();
        for (StandardEntity next: world) {
            if(next instanceof Area) {
                computeNeighboursforDrone((Area) next, world);
            }
        }
    }

    public TrafficArea getTrafficAreaforDrone(Area area) {
        return areas.get(area);
    }

    public void computeNeighboursforDrone(Area a, StandardWorldModel world) {
        Collection<TrafficArea> neighbours = areaNeighbours.get(getTrafficAreaforDrone(a));
        neighbours.clear();
        for (EntityID id: a.getNeighbours()) {
            Entity e = world.getEntity(id);
            if (e instanceof Area) {
                neighbours.add(getTrafficAreaforDrone((Area) e));
            }
        }
    }




}
