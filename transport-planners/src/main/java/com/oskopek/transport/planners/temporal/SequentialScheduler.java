package com.oskopek.transport.planners.temporal;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.SequentialDomain;
import com.oskopek.transport.model.domain.action.*;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.TemporalPlan;
import com.oskopek.transport.model.planner.Planner;
import com.oskopek.transport.model.problem.*;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.graph.DefaultRoadGraph;
import com.oskopek.transport.model.problem.graph.RoadGraph;
import com.oskopek.transport.model.state.TemporalPlanStateManager;
import com.oskopek.transport.planners.AbstractPlanner;
import javaslang.Tuple;
import javaslang.collection.Stream;
import org.graphstream.algorithm.TopologicalSort;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Wraps a sequential planner and schedules its outputs using a mutex directed acyclic graph (DAG).
 * Mutexes essentially partial orderings, that enforce that some pairs of actions have to be scheduled
 * in a specific order (for example, pick up and drop of a specific package have to occur in this order).
 */
public abstract class SequentialScheduler extends AbstractPlanner {

    @Override
    public Optional<Plan> plan(Domain domain, Problem problem) {
        SequentialDomain seqDomain = new SequentialDomain(domain.getName() + "-seq");
        Problem seqProblem = translateToSequential(problem);
        Optional<Plan> sequentialPlan = plan(seqDomain, seqProblem, plan -> schedule(domain, problem,
                plan.getActions()));
        return sequentialPlan.map(plan -> {
            if (plan instanceof TemporalPlan) {
                return plan;
            }
            return schedule(domain, seqProblem, plan.getActions());
        });
    }

    /**
     * Return the wrapped planner.
     *
     * @return the wrapped planner
     */
    protected abstract Planner getInternalPlanner();

    /**
     * Schedules a sequential plan using mutexes.
     * Computes the following:
     * <ol>
     *     <li>Find mutexes in the plan (ordered pairs of actions)</li>
     *     <li>Plan actions with no mutexes at 0 and incrementally plan others after the max mutex of previous ones,
     *     following a DAG</li>
     * </ol>
     * Mutexes are:
     * <ul>
     *     <li>Drive, pickup, drop actions of the same vehicle</li>
     *     <li>Drop+pick-up actions of the same package</li>
     * </ul>
     *
     * @param domain the temporal domain
     * @param temporalProblem the temporal problem
     * @param seqActions the sequential actions
     * @return the scheduled plan
     */
    public static TemporalPlan schedule(Domain domain, Problem temporalProblem, Collection<Action> seqActions) {
        if (seqActions.isEmpty()) {
            return new TemporalPlan(Collections.emptyList());
        }
        List<Action> seqActionList = new ArrayList<>(seqActions);
        // TODO: only greedy fuel currently
        for (int i = 0; i < seqActionList.size(); i++) {
            Action action = seqActionList.get(i);
            if (action instanceof Drive) {
                Location temporalFrom = temporalProblem.getRoadGraph().getLocation(action.getWhere().getName());
                if (temporalFrom.hasPetrolStation()) {
                    seqActionList.add(i, domain.buildRefuel(temporalProblem.getVehicle(action.getWho().getName()),
                            temporalFrom));
                    i++;
                }
                Location temporalTo = temporalProblem.getRoadGraph().getLocation(action.getWhat().getName());
                Vehicle temporalVehicle = temporalProblem.getVehicle(action.getWho().getName());
                FuelRoad road = (FuelRoad) temporalProblem.getRoadGraph().getRoad(((Drive) action).getRoad().getName());
                seqActionList.set(i, domain.buildDrive(temporalVehicle, temporalFrom, temporalTo, road));
            }
        }

        Graph mutexDag = new MultiGraph("mutexDag", true, false, seqActionList.size(),
                seqActionList.size());
        for (int i = 0; i < seqActionList.size(); i++) {
            mutexDag.addNode(i + "");
        }
        int id = 0;
        for (int i = 0; i < seqActionList.size(); i++) {
            Action from = seqActionList.get(i);
            for (int j = i + 1; j < seqActionList.size(); j++) {
                Action to = seqActionList.get(j);
                if (from.getWho().getName().equals(to.getWho().getName())) { // vehicle mutex
                    // TODO: refuel and pickup/drop can be concurrent

                    // for sequential drive actions, only add the needed transitive ones
                    mutexDag.addEdge(i + "->" + j + "_" + id++, i, j, true);
                    continue;
                }
                if (((from instanceof Drop && to instanceof PickUp) || (to instanceof Drop && from instanceof PickUp))
                        && from.getWhat().getName().equals(to.getWhat().getName())) {
                    mutexDag.addEdge(i + "->" + j + "_" + id++, i, j, true);
                    continue;
                }
            }
        }
        Map<Integer, TemporalPlanAction> plannedActions = new HashMap<>();
        TopologicalSort sort = new TopologicalSort(TopologicalSort.SortAlgorithm.KAHN);
        sort.init(mutexDag);
        sort.compute();
        List<Integer> topoSorted = sort.getSortedNodes().stream().map(n -> Integer.parseInt(n.getId()))
                .collect(Collectors.toList());
        final double delta = 0.001;
        for (int actionIndex : topoSorted) {
            double maxEndTimeOfPrevious = 0d;
            for (Iterator<Edge> it = mutexDag.getNode(actionIndex).getEnteringEdgeIterator(); it.hasNext();) {
                Edge enteringEdge = it.next();
                int sourceActionIndex = Integer.parseInt(enteringEdge.getSourceNode().getId());
                TemporalPlanAction plannedAction = plannedActions.get(sourceActionIndex);
                maxEndTimeOfPrevious = Math.max(plannedAction.getEndTimestamp(), maxEndTimeOfPrevious) + delta;
            }
            Action action = seqActionList.get(actionIndex);
            plannedActions.put(actionIndex, new TemporalPlanAction(action, maxEndTimeOfPrevious,
                    maxEndTimeOfPrevious + action.getDuration().getCost()));
        }

        TemporalPlan proposedPlan = new TemporalPlan(plannedActions.values());
        TemporalPlanStateManager manager = new TemporalPlanStateManager(domain, temporalProblem, proposedPlan);
        double endTime = proposedPlan.calculateMakespan();
        try {
            manager.goToTime(endTime + 1d, true);
        } catch (IllegalStateException e) { // invalid plan
            return null;
        }
        if (manager.getCurrentPlanState().getAllVehicles().stream().anyMatch(v -> v.getCurFuelCapacity() != null
                && v.getCurFuelCapacity().getCost() < 0)) {
            // invalid plan, break
            return null;
        }
        return proposedPlan;
    }

    //

    /**
     * Removes all fuel-related stuff from vehicles and graph roads.
     * @param problem the temporal problem
     * @return a sequential variant of the same problem
     */
    protected static Problem translateToSequential(final Problem problem) {
        RoadGraph graph = problem.getRoadGraph();
        RoadGraph newGraph = new DefaultRoadGraph(graph.getId());
        graph.getAllLocations().map(l -> l.updateHasPetrolStation(false)).forEach(newGraph::addLocation);
        Map<String, Location> newLocMap = Stream.ofAll(newGraph.getAllLocations().map(l -> Tuple.of(l.getName(), l))
                .collect(Collectors.toList())).toJavaMap(v -> v);

        Problem seqProblem = problem;
        Iterator<Vehicle> newVehicles = seqProblem.getAllVehicles().stream().map(v -> v.updateCurFuelCapacity(null)
                .updateMaxFuelCapacity(null).updateLocation(newLocMap.get(v.getLocation().getName()))).iterator();
        while (newVehicles.hasNext()) {
            Vehicle vehicle = newVehicles.next();
            if (vehicle.getTarget() != null) {
                vehicle = vehicle.updateTarget(newLocMap.get(vehicle.getTarget().getName()));
            }
            seqProblem = seqProblem.putVehicle(vehicle.getName(), vehicle);
        }

        Iterator<Package> newPackages = seqProblem.getAllPackages().stream().map(p -> p.updateLocation(newLocMap
                .get(p.getLocation().getName())).updateTarget(newLocMap.get(p.getTarget().getName()))).iterator();
        while (newPackages.hasNext()) {
            Package pkg = newPackages.next();
            seqProblem = seqProblem.putPackage(pkg.getName(), pkg);
        }

        graph.getAllRoads().forEach(re -> newGraph.addRoad(new DefaultRoad(re.getRoad().getName(),
                        re.getRoad().getLength()), newLocMap.get(re.getFrom().getName()),
                newLocMap.get(re.getTo().getName())));
        return new DefaultProblem(seqProblem.getName(), newGraph, seqProblem.getVehicleMap(),
                seqProblem.getPackageMap());
    }

    @Override
    public boolean cancel() {
        Planner internal = getInternalPlanner();
        if (internal != null) {
            internal.cancel();
        }
        return super.cancel();
    }

    @Override
    public abstract SequentialScheduler copy();
}