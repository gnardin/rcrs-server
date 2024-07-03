package rescuecore2.standard.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

/**
 * The Drone object.
 */
public class Drone extends Robot {

    /**
     * Construct a Drone object with entirely undefined values.
     *
     * @param id The ID of this entity.
     */
    public Drone(EntityID id) {
        super(id);
    }

    /**
     * Drone copy constructor.
     *
     * @param other The Drone to copy.
     */
    public Drone(Drone other) {
        super(other);
    }

    @Override
    protected Entity copyImpl() {
        return new Drone(getID());
    }

    @Override
    public StandardEntityURN getStandardURN() {
        return StandardEntityURN.DRONE;
    }

    @Override
    protected String getEntityName() {
        return "Drone";
    }
}
