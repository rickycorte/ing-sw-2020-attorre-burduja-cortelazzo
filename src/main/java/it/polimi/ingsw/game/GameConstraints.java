package it.polimi.ingsw.game;

/**
 * Constrains describe blocked behaviours of Actions
 * This implementation supports up to 32 different constraints that can be active at the same time
 */
public class GameConstraints
{

    /**
     * List of available constrains
     */
    public enum Constraint{
        NONE(0),
        BLOCK_MOVE_UP(1<<1),
        BLOCK_NON_LO_SO(1<<2);

        private int val;

        private Constraint(int val) {
            this.val = val;
        }

        /**
         * Convert a constraint into a integer
         * @return integer representation of a constraint
         */
        public int toInt(){
            return val;
        }
    }


    int constraints;

    /**
     *  Add a new constrain
     * @param c constraint to add
     */
    public void add(Constraint c) {
        constraints |=  c.toInt();
    }

    /**
     * Remove a constrains, if you try to remove a disabled constrain nothing happen
     * @param c constraint to remove
     */
    public void remove(Constraint c) {
        constraints &= ~(1 << c.toInt());
    }

    /**
     * Checks if a constrain is active or not
     * @param c constraint to check
     * @return true if the constrain is enabled
     */
    public boolean isActive(Constraint c){
        return (constraints & (1 << c.toInt())) != 0;
    }

    /**
     * Delete all current constrains
     */
    public void clear(){
        constraints = 0;
    }

}
