package misc;

import rescuecore2.config.Config;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Robot;
import rescuecore2.worldmodel.EntityID;

import java.util.Random;

public class RobotAttributes {
    private final Robot robot;
    private final EntityID id;
    private final DamageType damageBury;
    private final DamageType damageCollapse;
    private final Random random;

    /**
     * Construct RobotAttributes
     */
    public RobotAttributes(Robot r, Config config) {
        this.robot = r;
        this.id = r.getID();
        //Genreate random for each Robot
        this.random = new Random(config.getRandom().nextLong());
        damageCollapse = new DamageType("collapse", config, random);
        damageBury = new DamageType("bury", config, random);
    }

    /**
     Get the ID of the wrapped human.
     @return The human ID.
     */
    public EntityID getID() {
        return id;
    }

    /**
     Get the wrapped human.
     @return The wrapped human.
     */
    public Robot getRobot() {
        return robot;
    }

    /**
     Get the random sequence of the wrapped human.
     @return The random sequence.
     */
    public Random getRandom(){
        return random;
    }
    /**
     Add some collapse damage.
     @param d The amount of damage to add.
     */
    public void addCollapseDamage(double d) {
        damageCollapse.addDamage(d);
    }

    /**
     Get the amount of collapse damage this human has.
     @return The amount of collapse damage.
     */
    public double getCollapseDamage() {
        return damageCollapse.getDamage();
    }

    /**
     Set the amount of collapse damage this human has.
     @param d The new collapse damage.
     */
    public void setCollapseDamage(double d) {
        damageCollapse.setDamage(d);
    }

    /**
     Add some buriedness damage.
     @param d The amount of damage to add.
     */
    public void addBuriednessDamage(double d) {
        damageBury.addDamage(d);
    }

    /**
     Get the amount of buriedness damage this human has.
     @return The amount of buriedness damage.
     */
    public double getBuriednessDamage() {
        return damageBury.getDamage();
    }

    /**
     Set the amount of buriedness damage this human has.
     @param d The new buriedness damage.
     */
    public void setBuriednessDamage(double d) {
        damageBury.setDamage(d);
    }


    /**
     Progress all damage types.
     */
    public void progressDamage() {
        damageCollapse.progress();
        damageBury.progress();
    }

    public void progressDamageInRefuge()
    {
        //int damage = getTotalDamage();
        damageCollapse.progressInRefuge();
        damageBury.progressInRefuge();
    }

    /**
     Clear all damage.
     */
    public void clearDamage() {
        damageCollapse.setDamage(0);
        damageBury.setDamage(0);
    }
}
