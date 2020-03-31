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
        BLOCK_NON_LO_SO(1<<32);

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


    private int constraints;

    public GameConstraints()
    {
        constraints = 0;
    }

    public GameConstraints(GameConstraints other)
    {
        constraints = other.constraints;
    }



    /**
     *  Add a new constrain
     * @param c constraint to add
     */
    public void add(Constraint c) {
        constraints |=  c.toInt();
    }

    /**
     * Add a group of constrains to the current one
     * @param c constraint group to add
     */
    public void add (GameConstraints c) {
        constraints |= c.constraints;
    }

    /**
     * Remove a constrains, if you try to remove a disabled constrain nothing happen
     * @param c constraint to remove
     */
    public void remove(Constraint c) {
        constraints &= ~c.toInt();
    }

    /**
     * Checks if a constrain is active or not
     * Checks for NONE constraints result true only if no constraint is set
     * @param c constraint to check
     * @return true if the constrain is enabled
     */
    public boolean check(Constraint c){
        return (c == Constraint.NONE) ? constraints == 0 : (constraints & c.toInt()) != 0;
    }

    /**
     * Delete all current constrains
     */
    public void clear(){
        constraints = 0;
    }

    /**
     * Check if constrains are the same in both instances
     * @param other another constraint
     * @return true if the have the same constraints set
     */
    public boolean equals(GameConstraints other)
    {
        return constraints == other.constraints;
    }

}
