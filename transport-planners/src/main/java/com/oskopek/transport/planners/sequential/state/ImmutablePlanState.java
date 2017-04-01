package com.oskopek.transport.planners.sequential.state;

import com.google.common.collect.Lists;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.problem.*;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.state.PlanState;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

/**
 * An immutable {@link PlanState} variant used for planning.
 * <p>
 * Wrapper of a problem used in planning: does not use standard hash code and equals,
 * but assumes the graph (and other things, like the action object names) do not change and does not compare them.
 */
public class ImmutablePlanState {

    private final Action lastAction; // TODO: non sequential domains?
    private final ImmutablePlanState lastState;
    private final int totalTime;

    private final Problem problem; // TODO: do not store the entire problem?
    private int planningHashCode;

    public ImmutablePlanState(Problem problem) {
        this.problem = problem;
        this.lastState = null;
        this.lastAction = null;
        this.totalTime = 0;
    }

    /**
     * Default constructor.
     *
     * @param problem the problem
     * @param lastState the last state
     * @param lastAction the last action
     */
    public ImmutablePlanState(Problem problem, ImmutablePlanState lastState, Action lastAction) {
        this.problem = problem;
        this.lastState = lastState;
        this.lastAction = lastAction;
        totalTime = lastState.totalTime + lastAction.getDuration().getCost();
    }

    public Problem getProblem() {
        return problem;
    }

    /**
     * Get the pre-calculated total time it takes this plan to reach this state.
     *
     * @return the total time
     */
    public int getTotalTime() {
        return totalTime;
    }

    /**
     * Get the actions.
     *
     * @return the actions
     */
    public List<Action> getAllActionsInList() {
        return Lists.reverse(Lists.newArrayList(getAllActionsReversed()));
    }

    /**
     * Get the actions.
     *
     * @return the actions
     */
    public Iterator<Action> getAllActionsReversed() {
        return new ReversedActionIterator(this);
    }

    /**
     * Applies the specified action and returns the new state.
     *
     * @param action the action to apply
     * @return the updated state or empty if the preconditions or the effects were not valid in the resulting state
     */
    public Optional<ImmutablePlanState> apply(Action action) {
        return applyPreconditions(problem, action).flatMap(p -> applyEffects(p, action))
                .map(p -> new ImmutablePlanState(p, this, action));
    }

    /**
     * Applies the specified action's preconditions and returns the new state.
     *
     * @param action the action's preconditions to apply
     * @return the updated state or empty if the preconditions were not valid before application
     */
    private static Optional<Problem> applyPreconditions(Problem problem, Action action) {
        if (!action.arePreconditionsValid(problem)) {
            return Optional.empty();
        }
        return Optional.of(action.applyPreconditions(problem));
    }

    /**
     * Applies the specified action's effects and returns the new state.
     *
     * @param action the action's effects to apply
     * @return the updated state or empty if the effects were not valid after application
     */
    private static Optional<Problem> applyEffects(Problem problem, Action action) {
        Problem newProblem = action.applyEffects(problem);
        if (!action.areEffectsValid(newProblem)) {
            return Optional.empty();
        }
        return Optional.of(newProblem);
    }

    /**
     * Check if this state is a goal state.
     *
     * @return true iff this state is a goal state, i.e. if all packages and vehicles are at their targets, if specified
     */
    public boolean isGoalState() { // TODO: merge this into the PlanState interface
        for (Package p : getProblem().getAllPackages()) {
            if (!p.getTarget().equals(p.getLocation())) {
                return false;
            }
        }
        for (Vehicle v : getProblem().getAllVehicles()) {
            Location target = v.getTarget();
            if (target != null && !target.equals(v.getLocation())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImmutablePlanState)) {
            return false;
        }
        ImmutablePlanState that = (ImmutablePlanState) o;
        return equalsDuringPlanning(problem, that.problem);
    }

    /**
     * Internal implementation of a fast assumption-dependent equals. For example, totally omits the graph
     * from comparison.
     *
     * @param problem the problem to compare
     * @param other the other problem to compare
     * @return true iff they are equal, assuming all assumptions hold
     */
    private static boolean equalsDuringPlanning(Problem problem, Problem other) {
        if (hashCodeDuringPlanning(problem) != hashCodeDuringPlanning(other)) {
            return false;
        }
        Map<String, Vehicle> otherVehicleMap = other.getVehicleMap();

        for (Vehicle vehicle : problem.getVehicleMap().values()) {
            String vehicleName = vehicle.getName();
            Vehicle otherVehicle = otherVehicleMap.get(vehicleName);
            if (otherVehicle == null || !otherVehicle.getLocation().getName().equals(vehicle.getLocation().getName())) {
                return false;
            }

            Collection<Package> packages = vehicle.getPackageList();
            Set<String> pkgs = new HashSet<>(packages.size());
            for (Package pkg : packages) {
                pkgs.add(pkg.getName());
            }
            for (Package pkg : otherVehicle.getPackageList()) {
                if (!pkgs.remove(pkg.getName())) {
                    return false;
                }
            }
            if (!pkgs.isEmpty()) {
                return false;
            }
        }

        Map<String, Package> otherPackageMap = other.getPackageMap();
        for (Package pkg : problem.getPackageMap().values()) {
            Package otherPkg = otherPackageMap.get(pkg.getName());
            if (otherPkg == null || !Objects.equals(pkg.getLocation(), otherPkg.getLocation())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (planningHashCode == 0) {
            planningHashCode = hashCodeDuringPlanning(problem);
        }
        return planningHashCode;
    }

    /**
     * Internal implementation of a fast assumption-dependent hash code.
     *
     * @param problem the problem to calculate a hash code for
     * @return the hash code
     */
    private static int hashCodeDuringPlanning(Problem problem) {
        HashCodeBuilder builder = new HashCodeBuilder(13, 17);
        for (Vehicle vehicle : problem.getVehicleMap().values()) {
            builder.append(vehicle.getName());
            String location = vehicle.getLocation().getName();
            builder.append(location);
            vehicle.getPackageList().forEach(p -> packageHashCodeDuringPlanning(builder, p, location));
        }

        for (Package pkg : problem.getPackageMap().values()) {
            if (pkg.getLocation() != null) {
                packageHashCodeDuringPlanning(builder, pkg, pkg.getLocation().getName());
            }
        }
        return builder.toHashCode();
    }

    /**
     * Util method for calculating a packages hash code fast. Assumption-dependent.
     *
     * @param builder the builder to append to
     * @param pkg the package
     * @param location the location of the package
     */
    private static void packageHashCodeDuringPlanning(HashCodeBuilder builder, Package pkg, String location) {
        builder.append(pkg.getName());
        builder.append(location);
    }

    private static final class ReversedActionIterator implements Iterator<Action> {

        private ImmutablePlanState current;

        public ReversedActionIterator(ImmutablePlanState begin) {
            current = begin;
        }

        @Override
        public boolean hasNext() {
            return current.lastAction != null;
        }

        @Override
        public Action next() {
            Action popped = current.lastAction;
            current = current.lastState;
            return popped;
        }
    }

}
