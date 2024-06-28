package rescuecore2.standard.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.properties.IntProperty;

/**
 * The Drone object.
 */
public class Drone extends /*Human*/ Robot {

    private IntProperty height;

    /**
     * Construct a Drone object with entirely undefined values.
     *
     * @param id The ID of this entity.
     */
    public Drone(EntityID id) {
        super(id);
        height = new IntProperty(StandardPropertyURN.HEIGHT);
        registerProperties(height);
    }

    /**
     * Drone copy constructor.
     *
     * @param other The Drone to copy.
     */
    public Drone(Drone other) {
        super(other);
        height = new IntProperty(other.height);
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
    public Property getProperty(int urn) {
        StandardPropertyURN type;
        try {
            type = StandardPropertyURN.fromInt(urn);
        } catch (IllegalArgumentException ex) {
            return super.getProperty(urn);
        }
        switch (type) {
            case HEIGHT:
                return height;
            default:
                return super.getProperty(urn);
        }
    }

    /**
     * Get the height property
     *
     * @return The height property
     */
    public IntProperty getHeightProperty() {
        return height;
    }

    public int getHeight() {
        return height.getValue();
    }

    /**
     * Set the height of the drone
     *
     * @param height
     *              The new height of the drone.
     */
    public void setHeight( int height ) {
        this.height.setValue( height );
    }

    /**
     * Find out if the height property has been defined.
     *
     * @return True if the height property has been defined, otherwise false.
     */
    public boolean isHeightDefined() {
        return height.isDefined();
    }

    /**
     * Undefine the height property.
     */
    public void undefineHeight() {
        height.undefine();
    }

    @Override
    protected String getEntityName() {
        return "Drone";
    }
}


