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
        /**
         * No constraint
         */
        NONE(0, "NONE"),
        /**
         * [MOVE ONLY] Block moves with height diff gte 1
         */
        BLOCK_MOVE_UP(1<<1,"No up"),
        /**
         * [MOVE ONLY] Enable BLOCK_MOVE_UP if one of your workers goes up
         */
        SET_BLOCK_MOVE_UP(1<<5, "Lock move up"),
        /**
         * [MOVE ONLY] Swap with an opponent worker
         */
        CAN_SWAP_CONSTRAINT(1<<2, "Swap"),
        /**
         * [MOVE ONLY] Push an opponent worker
         */
        CAN_PUSH_CONSTRAINT(1<<3, "Push"),
        /**
         * [BUILD ONLY] Block dome building
         */
        BLOCK_DOME_BUILD (1<<6, "No Dome"),
        /**
         * [MOVE ONLY] Prevent move to the same cell of previous moves
         */
        BLOCK_SAME_CELL_MOVE (1<<4, "No same cell"), //if active, can't return to the same cell     (used in MoveAgainAction)
        /**
         * [BUILD ONLY] Prevent building to the same cell
         */
        BLOCK_SAME_CELL_BUILD(1<<8, "No same cell"),  //if active, can't build on the same cell     (used in BuildAgainAction)
        /**
         * [BUILD ONLY] Prevent building to a different cell
         */
        BLOCK_DIFF_CELL_BUILD(1<<16, "No different cell"), //if active, can only build on the same cell  (used in BuildAgainAction)
        /**
         * [MOVE ONLY] Win when your move height diff is lower and equal than -2
         */
        WIN_BY_GOING_DOWN(1<<17, "Win by fall"),    //if active, the player wins by going down 2 lvls

        TEST(1<<32, "TEST");

        private int val;
        private String displayName;

        private Constraint(int val, String displayName) {
            this.val = val;
            this.displayName = displayName;
        }

        /**
         * Convert a constraint into a integer
         * @return integer representation of a constraint
         */
        public int toInt(){
            return val;
        }

        @Override
        public String toString()
        {
            return displayName;
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
        if(c == null) return;
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
