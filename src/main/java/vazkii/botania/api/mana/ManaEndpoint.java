package vazkii.botania.api.mana;

/**
 * Standard mana transfer: both methods return the amount that actually changed, and every move between two holders
 * goes through {@link #transfer}, which takes from the source first and only gives what was taken.
 */
public interface ManaEndpoint {

    long addMana(long amount, boolean simulate);

    long removeMana(long amount, boolean simulate);

    static long transfer(ManaEndpoint from, ManaEndpoint to, long amount) {
        if (amount <= 0) {
            return 0;
        }
        long room = to.addMana(amount, true);
        long taken = room > 0 ? from.removeMana(room, false) : 0;
        if (taken <= 0) {
            return 0;
        }
        long given = to.addMana(taken, false);
        if (given < taken) {
            from.addMana(taken - given, false);
        }
        return given;
    }
}
