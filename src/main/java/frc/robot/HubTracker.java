package frc.robot;

import static edu.wpi.first.units.Units.Seconds;

import edu.wpi.first.units.measure.Time;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import java.util.Optional;

// FOUND ON GITHUB https://gist.github.com/LordOfFrogs/240ba37cf696ba156d87f387c1461bd5
public class HubTracker { 
    /**
     * Returns an {@link Optional} containing the current {@link Shift}.
     * Will return {@link Optional#empty()} if disabled or in between auto and teleop.
     */
    public static Optional<Shift> getCurrentShift() {
        double matchTime = getMatchTime();
        if (matchTime < 0) return Optional.empty();

        for (Shift shift : Shift.values()) {
            if (matchTime < shift.endTime) {
                return Optional.of(shift);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns an {@link Optional} containing the current {@link Time} remaining in the current shift.
     * Will return {@link Optional#empty()} if disabled or in between auto and teleop.
     */
    public static Optional<Time> timeRemainingInCurrentShift() {
        return getCurrentShift().map((shift) -> Seconds.of(shift.endTime - getMatchTime()));
    }

    /**
     * Returns an {@link Optional} containing the next {@link Shift}.
     * Will return {@link Optional#empty()} if disabled or in between auto and teleop.
     */
    public static Optional<Shift> getNextShift() {
        double matchTime = getMatchTime();

        for (Shift shift : Shift.values()) {
            if (matchTime < shift.startTime) {
                return Optional.of(shift);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns whether the hub is active during the specified {@link Shift} for the specified {@link Alliance}.
     * Will return {@code false} if disabled or in between auto and teleop.
     */
    public static boolean isActive(Alliance alliance, Shift shift) {
        Optional<Alliance> autoWinner = getAutoWinner();
        switch (shift.activeType) {
            case BOTH:
                return true;
            case AUTO_WINNER:
                return autoWinner.isPresent() && autoWinner.get() == alliance;
            case AUTO_LOSER:
                return autoWinner.isPresent() && autoWinner.get() != alliance;
            default:
                return false;
        }
    }

    /**
     * Returns whether the hub is active during the current {@link Shift} for the specified {@link Alliance}.
     * Will return {@code false} if disabled or in between auto and teleop.
     */
    public static boolean isActive(Alliance alliance) {
        Optional<Shift> currentShift = getCurrentShift();
        return currentShift.isPresent() && isActive(alliance, currentShift.get());
    }

    /**
     * Returns whether the hub is active during the specified {@link Shift} for the robot's {@link Alliance}.
     * Will return {@code false} if disabled or in between auto and teleop.
     */
    public static boolean isActive(Shift shift) {
        Optional<Alliance> alliance = DriverStation.getAlliance();
        return alliance.isPresent() && isActive(alliance.get(), shift);
    }

    /**
     * Returns whether the hub is active during the current {@link Shift} for the robot's {@link Alliance}.
     * Will return {@code false} if disabled or in between auto and teleop.
     */
    public static boolean isActive() {
        Optional<Shift> currentShift = getCurrentShift();
        Optional<Alliance> alliance = DriverStation.getAlliance();
        return currentShift.isPresent() && alliance.isPresent() && isActive(alliance.get(), currentShift.get());
    }

    /**
     * Returns whether the hub is active for the next {@link Shift} for the specified {@link Alliance}.
     * Will return {@code false} if disabled or in between auto and teleop.
     */
    public static boolean isActiveNext(Alliance alliance) {
        Optional<Shift> nextShift = getNextShift();
        return nextShift.isPresent() && isActive(alliance, nextShift.get());
    }

    /**
     * Returns whether the hub is active during the specified {@link Shift} for the specified {@link Alliance}.
     * Will return {@code false} if disabled or in between auto and teleop.
     */
    public static boolean isActiveNext() {
        Optional<Shift> nextShift = getNextShift();
        Optional<Alliance> alliance = DriverStation.getAlliance();
        return nextShift.isPresent() && alliance.isPresent() && isActive(alliance.get(), nextShift.get());
    }

    /**
     * Returns the {@link Alliance} that won auto as specified by the FMS/Driver Station's game specific message data.
     * Will return {@link Optional#empty()} if no game message or alliance is available.
     */
    public static Optional<Alliance> getAutoWinner() {
        String msg = DriverStation.getGameSpecificMessage();
        char msgChar = msg.length() > 0 ? msg.charAt(0) : ' ';
        switch (msgChar) {
            case 'B':
                return Optional.of(Alliance.Blue);
            case 'R':
                return Optional.of(Alliance.Red);
            default:
                return Optional.empty();
        }
    }

    /**
     * Counts up from 0 to 160 seconds as match progresses.
     * Returns -1 if not match isn't running or if in between auto and teleop
     */
    public static double getMatchTime() {
        if (DriverStation.isAutonomous()) {
            if (DriverStation.getMatchTime() < 0) return DriverStation.getMatchTime();
            return 20 - DriverStation.getMatchTime();
        } else if (DriverStation.isTeleop()) {
            if (DriverStation.getMatchTime() < 0) return DriverStation.getMatchTime();
            return 160 - DriverStation.getMatchTime();
        }
        return -1;
    }

    /**
     * Represents an alliance shift.<br>
     * <h4>Values:</h4>
     * <ul>
     * <li>{@link Shift#AUTO}</li> (0-20 sec)
     * <li>{@link Shift#TRANSITION}</li> (20-25 sec)
     * * <li>{@link Shift#TRANSITION_BLINK}</li> (25-30 sec)
     * <li>{@link Shift#SHIFT_1}</li> (30-50 sec)
     * * <li>{@link Shift#SHIFT_1_BLINK}</li> (50-55 sec)
     * <li>{@link Shift#SHIFT_2}</li> (55-75 sec)
     * * <li>{@link Shift#SHIFT_2_BLINK}</li> (75-80 sec)
     * <li>{@link Shift#SHIFT_3}</li> (80-100 sec)
     * * <li>{@link Shift#SHIFT_3_BLINK}</li> (100-105 sec)
     * <li>{@link Shift#SHIFT_4}</li> (105-125 sec)
     * * <li>{@link Shift#SHIFT_4_BLINK}</li> (125-130 sec)
     * <li>{@link Shift#ENDGAME}</li> (130-140 sec)
     * * <li>{@link Shift#ENDGAME_BLINK_1}</li> (140-150 sec)
     * * <li>{@link Shift#ENDGAME_BLINK_2}</li> (150-155 sec)
     * * <li>{@link Shift#ENDGAME_BLINK_3}</li> (155-160 sec)
     * </ul>
     */
    public enum Shift {
        AUTO(0, 20, ActiveType.BOTH),
        TRANSITION(20, 25, ActiveType.BOTH),
        TRANSITION_BLINK(25, 30, ActiveType.BOTH), // Added
        SHIFT_1(30, 50, ActiveType.AUTO_LOSER),
        SHIFT_1_BLINK(50, 55, ActiveType.AUTO_LOSER), // Added
        SHIFT_2(55, 75, ActiveType.AUTO_WINNER),
        SHIFT_2_BLINK(75, 80, ActiveType.AUTO_WINNER), // Added
        SHIFT_3(80, 100, ActiveType.AUTO_LOSER),
        SHIFT_3_BLINK(100, 105, ActiveType.AUTO_LOSER), // Added
        SHIFT_4(105, 125, ActiveType.AUTO_WINNER),
        SHIFT_4_BLINK(125, 130, ActiveType.AUTO_WINNER), // Added
        ENDGAME(130, 140, ActiveType.BOTH),
        ENDGAME_BLINK_1(140, 150, ActiveType.BOTH), // Added
        ENDGAME_BLINK_2(150, 155, ActiveType.BOTH), // Added
        ENDGAME_BLINK_3(155, 160, ActiveType.AUTO_LOSER); // Added       

        final int startTime;
        final int endTime;
        final ActiveType activeType;

        private Shift(int startTime, int endTime, ActiveType activeType) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.activeType = activeType;
        }
    }

    private enum ActiveType {
        BOTH,
        AUTO_WINNER,
        AUTO_LOSER
    }
}