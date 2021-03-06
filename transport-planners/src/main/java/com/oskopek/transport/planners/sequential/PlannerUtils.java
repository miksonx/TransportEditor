package com.oskopek.transport.planners.sequential;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Lists;
import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.domain.action.Drive;
import com.oskopek.transport.model.domain.action.Drop;
import com.oskopek.transport.model.domain.action.PickUp;
import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.Package;
import com.oskopek.transport.model.problem.graph.RoadEdge;
import com.oskopek.transport.model.problem.graph.RoadGraph;
import com.oskopek.transport.model.problem.Vehicle;
import com.oskopek.transport.planners.sequential.state.ImmutablePlanState;
import com.oskopek.transport.planners.sequential.state.ShortestPath;
import javaslang.Tuple;
import javaslang.Tuple3;
import org.graphstream.algorithm.APSP;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.Graphs;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Various utility methods for calculating heuristics and other.
 */
public final class PlannerUtils {

    /**
     * Default empty constructor.
     */
    private PlannerUtils() {
        // intentionally empty
    }

    /**
     * Generate reasonable actions that are applicable to the the current state. Does not generate all of them,
     * it prunes away all that are unnecessary (driving in circles, etc).
     *
     * @param domain the domain
     * @param state the state
     * @param distanceMatrix the distance matrix
     * @param packagesUnfinished the undelivered packages
     * @return a stream of applicable actions
     */
    public static Stream<Action> generateActions(Domain domain, ImmutablePlanState state,
            ArrayTable<String, String, ShortestPath> distanceMatrix, Set<Package> packagesUnfinished) {
        if (PlannerUtils.hasCycle(state.getAllActionsReversed())) { // TODO: Convert to non-generation
            return Stream.empty();
        }

        Stream.Builder<Action> generated = Stream.builder();
        Collection<Vehicle> vehicles = state.getProblem().getAllVehicles();
        RoadGraph graph = state.getProblem().getRoadGraph();
        Map<Location, Set<Vehicle>> vehicleMap = PlannerUtils.computeVehicleMap(vehicles);
        Map<Location, Set<Package>> packageMap = PlannerUtils.computePackageMap(packagesUnfinished);

        // drop at target above all else
        for (Package pkg : packagesUnfinished) {
            if (pkg.getLocation() == null) { // unfinished package is in vehicle
                Location target = pkg.getTarget();
                Set<Vehicle> vehiclesAtLoc = vehicleMap.get(target);
                if (vehiclesAtLoc != null) {
                    for (Vehicle vehicle : vehiclesAtLoc) { // vehicles at target
                        if (vehicle.getPackageList().contains(pkg)) {
                            generated.accept(domain.buildDrop(vehicle, target, pkg));
                            return generated.build();
                        }
                    }
                }
            }
        }

        Optional<Action> lastAction = Optional.ofNullable(state.getAction());
        // pick-up
        Optional<Vehicle> lastVehicleAndLastDrive = lastAction
                .filter(a -> a instanceof Drive).map(a -> (Vehicle) a.getWho())
                .map(v -> state.getProblem().getVehicle(v.getName()));
        if (lastVehicleAndLastDrive.isPresent()) { // only use active vehicle
            Vehicle vehicle = lastVehicleAndLastDrive.get();
            Location location = vehicle.getLocation();

            Set<Package> packages = packageMap.get(location);
            if (packages != null) {
                for (Package pkg : packages) {
                    if (pkg.getSize().compareTo(vehicle.getCurCapacity()) <= 0) {
                        PickUp nextAction = domain.buildPickUp(vehicle, location, pkg);
                        if (PlannerUtils.needlessDropAndPickupOccurred(state.getProblem().getAllVehicles(),
                                state.getAllActionsInList(), nextAction)) {
                            continue;
                        }
                        generated.accept(nextAction);
                    }
                }
            }
        } else {
            Map<String, Set<String>> vehicleDroppedAfterLastMove = PlannerUtils
                    .getPackagesDroppedAfterLastMoveMap(vehicles.size(), state.getAllActionsInList());
            packageMap.keySet().forEach(location -> {
                Set<Package> packages = packageMap.get(location);
                Set<Vehicle> vehiclesAtLoc = vehicleMap.get(location);
                if (packages == null || vehiclesAtLoc == null) {
                    return;
                }

                for (Vehicle vehicle : vehiclesAtLoc) {
                    Set<String> droppedNames = vehicleDroppedAfterLastMove.get(vehicle.getName());
                    for (Package pkg : packages) {
                        if (droppedNames != null && droppedNames.contains(pkg.getName())) {
                            continue;
                        }
                        if (pkg.getSize().compareTo(vehicle.getCurCapacity()) <= 0) {
                            PickUp nextAction = domain.buildPickUp(vehicle, location, pkg);
                            if (PlannerUtils.needlessDropAndPickupOccurred(state.getProblem().getAllVehicles(),
                                    state.getAllActionsInList(), nextAction)) {
                                continue;
                            }
                            generated.accept(nextAction);
                        }
                    }
                }
            });
        }

        // drop

        if (lastAction.filter(a -> !(a instanceof PickUp)).isPresent()) { // do not drop after pick up
            if (lastVehicleAndLastDrive.isPresent()) { // only drop from active vehicle
                Vehicle vehicle = lastVehicleAndLastDrive.get();
                Location current = vehicle.getLocation();
                for (Package pkg : vehicle.getPackageList()) {
                    Drop drop = domain.buildDrop(vehicle, current, pkg);
                    if (PlannerUtils.droppedPackageWhereWePickedItUp(state, drop)) {
                        continue;
                    }
                    generated.accept(drop);
                }
            } else {
                for (Vehicle vehicle : vehicles) {
                    Location current = vehicle.getLocation();
                    for (Package pkg : vehicle.getPackageList()) {
                        Drop drop = domain.buildDrop(vehicle, current, pkg);
                        if (PlannerUtils.droppedPackageWhereWePickedItUp(state, drop)) {
                            continue;
                        }
                        generated.accept(drop);
                    }
                }
            }
        }

        // drive
        Optional<Vehicle> lastVehicleAndNotDrop = lastAction
                .filter(a -> !(a instanceof Drop)).map(a -> (Vehicle) a.getWho())
                .map(v -> state.getProblem().getVehicle(v.getName()));

        List<Action> reversedActions = Lists.newArrayList(state.getAllActionsReversed());
        if (lastVehicleAndNotDrop.isPresent()) { // continue driving if driving
            Vehicle vehicle = lastVehicleAndNotDrop.get();
            generateDrivesForVehicle(vehicle, graph, domain, distanceMatrix, reversedActions)
                    .forEach(generated::add);
        } else {
            for (Vehicle vehicle : vehicles) {
                generateDrivesForVehicle(vehicle, graph, domain, distanceMatrix, reversedActions)
                        .forEach(generated::add);
            }
        }
        return generated.build();
    }


    /**
     * Test if a drop and pickup have been generated that didn't need to be generated.
     *
     * @param reverseActions actions that were generated, in reverse order
     * @param vehicle the vehicle to check for
     * @param pkg the package to check for
     * @return true iff an unneeded drop and pickup action were generated
     * @deprecated TODO: doesn't work, use {@link #needlessDropAndPickupOccurred(Collection, Iterable)}
     */
    public static boolean needlessDropAndPickup(Iterator<Action> reverseActions, Vehicle vehicle, Package pkg) {
        Map<String, String> pickedUpBy = new HashMap<>(); // Package -> Vehicle
        pickedUpBy.put(pkg.getName(), vehicle.getName());

        while (reverseActions.hasNext()) {
            Action action = reverseActions.next();
            if (action instanceof PickUp) {
                pickedUpBy.put(action.getWhat().getName(), action.getWho().getName());
            } else if (action instanceof Drop) {
                String packageName = action.getWhat().getName();
                if (action.getWho().getName().equals(pickedUpBy.get(packageName))) {
                    return true;
                } else {
                    pickedUpBy.remove(packageName);
                }
            }
        }
        return false;
    }

    /**
     * Test if a drop and pickup have been generated that didn't need to be generated.
     *
     * @param vehicles the vehicles to check for
     * @param actions the actions that were generated
     * @param lastAction the last action that was generated (not part of actions)
     * @return true iff an unneeded drop and pickup action were generated
     * @deprecated TODO: Slow, replace with a faster version
     */
    public static boolean needlessDropAndPickupOccurred(Collection<Vehicle> vehicles, Iterable<Action> actions,
            PickUp lastAction) {
        List<Action> actionsNew = Lists.newArrayList(actions);
        actionsNew.add(lastAction);
        return needlessDropAndPickupOccurred(vehicles, actionsNew);
    }

    /**
     * Test if a drop and pickup have been generated that didn't need to be generated.
     *
     * @param vehicles the vehicles to check for
     * @param actions the actions that were generated
     * @return true iff an unneeded drop and pickup action were generated
     * @deprecated TODO: Slow, replace with a faster version
     */
    public static boolean needlessDropAndPickupOccurred(Collection<Vehicle> vehicles, Iterable<Action> actions) {
        for (Vehicle v : vehicles) {
            Map<String, Integer> packagesUntouchedSince = new HashMap<>();
            List<Integer> capacities = new ArrayList<>();
            int index = 0;
            int lastCapacity = v.getMaxCapacity().getCost();
            for (Action action : actions) {
                if (action.getWho().getName().equals(v.getName())) {
                    if (action instanceof Drop) {
                        capacities.add(++lastCapacity);
                        packagesUntouchedSince.put(action.getWhat().getName(), index);
                    } else if (action instanceof PickUp) {
                        capacities.add(--lastCapacity);
                        if (packagesUntouchedSince.containsKey(action.getWhat().getName())) {
                            if (!capacities.subList(packagesUntouchedSince.get(action.getWhat().getName()), index)
                                    .contains(0)) {
//                                System.out.println(action);
                                return true;
                            }
                        }
                    } else {
                        capacities.add(lastCapacity);
                    }
                } else {
                    capacities.add(lastCapacity);
                    packagesUntouchedSince.remove(action.getWhat().getName());
                }
                index++;
            }
        }
        return false;
    }

    /**
     * Generate drive actions for the vehicle (only if they are on the shortest paths).
     *
     * @param vehicle the vehicle
     * @param graph the road graph
     * @param domain the domain
     * @param distanceMatrix the distance matrix
     * @param reversedActions reversed actions generated up to now
     * @return a stream of generated drive actions
     */
    private static Stream<Drive> generateDrivesForVehicle(Vehicle vehicle, RoadGraph graph, Domain domain,
            ArrayTable<String, String, ShortestPath> distanceMatrix, Iterable<Action> reversedActions) {
        Stream.Builder<Drive> vehicleActions = Stream.builder();
        Location current = vehicle.getLocation();
        for (Edge edge : graph.getNode(current.getName()).getEachLeavingEdge()) {
            Location target = graph.getLocation(edge.getTargetNode().getId());
            if (doesShorterPathExist(vehicle, target, reversedActions.iterator(), distanceMatrix)) {
                continue;
            }
            vehicleActions.accept(domain.buildDrive(vehicle, current, target, graph.getRoad(edge.getId())));
        }
        return vehicleActions.build();
    }

    /**
     * Calculates a map of packages that were dropped by a vehicle after its last move so far.
     *
     * @param vehicleCount the number of vehicles (used for allocation)
     * @param plannedActions the actions generated up to now
     * @return a map of vehicle names to sets of packages (Vehicle -> [Package])
     */
    private static Map<String, Set<String>> getPackagesDroppedAfterLastMoveMap(int vehicleCount,
            List<Action> plannedActions) {
        // Vehicle -> int (index into plannedActions)
        Map<String, Integer> lastDriveIndexMap = new HashMap<>(vehicleCount);
        for (int i = 0; i < plannedActions.size(); i++) {
            Action action = plannedActions.get(i);
            Drive drive;
            if (!(action instanceof Drive)) {
                continue;
            }
            drive = (Drive) action;
            lastDriveIndexMap.put(drive.getWho().getName(), i);
        }

        Map<String, Set<String>> packagesDroppedAfterLastMoveMap = new HashMap<>(lastDriveIndexMap.size());
        for (Map.Entry<String, Integer> entry : lastDriveIndexMap.entrySet()) {
            String vehicleName = entry.getKey();
            int lastDriveIndex = entry.getValue();

            for (int i = lastDriveIndex + 1; i < plannedActions.size(); i++) {
                Action action = plannedActions.get(i);
                if (action instanceof Drop && vehicleName.equals(action.getWho().getName())) {
                    packagesDroppedAfterLastMoveMap.computeIfAbsent(vehicleName, v -> new HashSet<>(2))
                            .add(action.getWhat().getName());
                }
            }
        }
        return packagesDroppedAfterLastMoveMap;
    }

    /**
     * Computes a map of locations to sets of vehicles present at that location.
     *
     * @param vehicles the vehicles
     * @return the map
     */
    private static Map<Location, Set<Vehicle>> computeVehicleMap(Collection<Vehicle> vehicles) {
        Map<Location, Set<Vehicle>> vehicleMap = new HashMap<>();
        for (Vehicle vehicle : vehicles) {
            Location current = vehicle.getLocation();
            vehicleMap.computeIfAbsent(current, c -> {
                Set<Vehicle> set = new HashSet<>();
                set.add(vehicle);
                return set;
            });
        }
        return vehicleMap;
    }

    /**
     * Computes a map of locations to sets of packages present at that location.
     *
     * @param pkgs the packages
     * @return the map
     */
    private static Map<Location, Set<Package>> computePackageMap(Collection<Package> pkgs) {
        Map<Location, Set<Package>> pkgMap = new HashMap<>();
        for (Package pkg : pkgs) {
            Location current = pkg.getLocation();
            pkgMap.computeIfAbsent(current, c -> {
                Set<Package> set = new HashSet<>(2);
                set.add(pkg);
                return set;
            });
        }
        return pkgMap;
    }

    /**
     * Computes the All-pairs shortest path algorithm (Floyd-Warshall) on the road graph.
     *
     * @param graph the graph
     * @return a lookup matrix of shortest paths
     */
    public static ArrayTable<String, String, ShortestPath> computeAPSP(final RoadGraph graph) {
        final String ATTRIBUTE_NAME = "weight";
        RoadGraph originalAPSPGraph = (RoadGraph) Graphs.clone(graph);
        originalAPSPGraph.getAllRoads().forEach(roadEdge -> originalAPSPGraph.getEdge(roadEdge.getRoad().getName())
                .addAttribute(ATTRIBUTE_NAME, roadEdge.getRoad().getLength().getCost()));
        new APSP(originalAPSPGraph, ATTRIBUTE_NAME, true).compute();
        List<String> locationNames = originalAPSPGraph.getNodeSet().stream().map(Element::getId).collect(
                Collectors.toList());
        ArrayTable<String, String, ShortestPath> distanceMatrix = ArrayTable.create(locationNames, locationNames);
        for (String from : locationNames) {
            APSP.APSPInfo current = originalAPSPGraph.getNode(from).getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);
            for (String to : locationNames) {
                Path shortestPath = current.getShortestPathTo(to);
                List<RoadEdge> roads = new ArrayList<>(shortestPath.getEdgeCount());
                int distance = (int) getLengthToCorrect(current, to);
                if (distance > 0) {
                    for (Edge edge : shortestPath.getEachEdge()) {
                        roads.add(graph.getRoadEdge(edge.getId()));
                    }
                }
                if (null != distanceMatrix.put(from, to, new ShortestPath(roads, distance))) {
                    throw new IllegalStateException("Overwritten a value.");
                }
            }
        }
        return distanceMatrix;
    }

    /**
     * Corrects the distance returned by Floyd-Warshall's implementation in {@link APSP} to correctly return 0
     * for the shortest path from a point A to A.
     *
     * @param current the current node APSP info
     * @param targetName the target location name
     * @return the shortest path length
     */
    public static double getLengthToCorrect(APSP.APSPInfo current, String targetName) {
        if (targetName.equals(current.getNodeId())) { // fix weird behavior of APSP
            return 0d;
        } else {
            return current.getLengthTo(targetName);
        }
    }

    /**
     * Determines if a shorter path leads to the target than the one at the tail of the generated actions.
     *
     * @param vehicle the vehicle for which to check
     * @param target the target location
     * @param reversedActionsIterator an iterator over the generated actions, in reverse order
     * @param distanceMatrix the distance matrix
     * @return true iff a shorter path than the current one exists (making the sequential plan suboptimal)
     */
    public static boolean doesShorterPathExist(Vehicle vehicle, Location target,
            Iterator<Action> reversedActionsIterator, ArrayTable<String, String, ShortestPath> distanceMatrix) {

        if (!reversedActionsIterator.hasNext()) {
            return false;
        }

        Drive lastDrive = null;
        int lengthOfPath = 0;
        Location sourceOfPreviousDrives;


        while (reversedActionsIterator.hasNext()) {
            Action plannedAction = reversedActionsIterator.next();
            if (!plannedAction.getWho().getName().equals(vehicle.getName())) {
                continue;
            }
            if (plannedAction instanceof Drive) {
                lastDrive = (Drive) plannedAction;
                lengthOfPath += lastDrive.getDuration().getCost();
                continue;
            }
            break;
        }

        if (lastDrive == null) {
            return false; // no drives
        } else {
            sourceOfPreviousDrives = lastDrive.getWhere();
        }

        return distanceMatrix.get(sourceOfPreviousDrives.getName(), target.getName()).getDistance() < lengthOfPath;
    }

    /**
     * Heuristic: sum distances of package locations to their targets, using the shortest available paths.
     * Also adds costs for drop and pickup actions. Is <strong>not admissible</strong>.
     *
     * @param packageList the package list
     * @param vehicleList the vehicle list
     * @param distanceMatrix the distance matrix
     * @return the heuristic value.
     */
    public static int calculateSumOfDistancesToPackageTargets(Collection<Package> packageList,
            Collection<Vehicle> vehicleList, ArrayTable<String, String, ShortestPath> distanceMatrix) {
        int sumDistances = 0;
        for (Vehicle vehicle : vehicleList) { // vehicles are never in the middle of a drive
            for (Package pkg : vehicle.getPackageList()) {
                sumDistances += distanceMatrix.get(vehicle.getLocation().getName(), pkg.getTarget().getName())
                        .getDistance() + 1; // + drop action
            }
        }
        for (Package pkg : packageList) {
            Location pkgLocation = pkg.getLocation();
            if (pkgLocation != null) {
                sumDistances += distanceMatrix.get(pkgLocation.getName(), pkg.getTarget().getName())
                        .getDistance() + 2; // + pickup and drop
            }
        }
        return sumDistances;
    }

    /**
     * Heuristic: sum the minimum of needed costs of drop and pickup actions to deliver all packages.
     * Is <strong>admissible</strong>.
     *
     * @param packageList the package list
     * @return the heuristic value.
     */
    public static int admissibleHeuristic(Collection<Package> packageList) {
        int sumDistances = 0;
        for (Package pkg : packageList) {
            Location pkgLocation = pkg.getLocation();
            if (pkgLocation != null) {
                sumDistances += 2;
            } else {
                sumDistances += 1;
            }
        }
        return sumDistances;
    }

    /**
     * Heuristic: sum distances of package locations to their targets and of the closest vehicle to the package location
     * or the closest package location,
     * using the shortest available paths.
     * Also adds costs for drop and pickup actions. Is <strong>not admissible</strong>.
     * As a rule, this heuristic returns larger values than
     * {@link #calculateSumOfDistancesToPackageTargets(Collection, Collection, ArrayTable)}.
     *
     * @param packageList the package list
     * @param vehicleList the vehicle list
     * @param distanceMatrix the distance matrix
     * @return the heuristic value.
     */
    public static int calculateSumOfDistancesToVehiclesPackageTargetsAdmissible(Collection<Package> packageList,
            Collection<Vehicle> vehicleList, ArrayTable<String, String, ShortestPath> distanceMatrix) {
        int sumDistances = 0;
//        for (Vehicle vehicle : vehicleList) { // vehicles are never in the middle of a drive
//            int maxPkgDistance = 0;
//            for (Package pkg : vehicle.getPackageList()) {
//                int dist = distanceMatrix.get(vehicle.getLocation().getName(), pkg.getTarget().getName());
//                if (dist > maxPkgDistance) {
//                    maxPkgDistance = dist;
//                }
//            } // WARNING: not true, calculate the max distance for a package in the vehicle,
//              // or the spanning tree distances
//            sumDistances += maxPkgDistance + vehicle.getPackageList().size(); // + drop actions
//        }
        for (Package pkg : packageList) {
            Location pkgLocation = pkg.getLocation();
            if (pkgLocation != null) {
                String pkgLocName = pkgLocation.getName();
                // calculate the distance to the target + pickup and drop
                sumDistances += distanceMatrix.get(pkgLocName, pkg.getTarget().getName()) // WARNING: not admissible
                        .getDistance() + 2; // + pickup and drop

                // Calculate the distance to the nearest vehicle or package
                int minVehicleDistance = Integer.MAX_VALUE;
                for (Vehicle vehicle : vehicleList) {
                    int dist = distanceMatrix.get(pkgLocName, vehicle.getLocation().getName()).getDistance();
                    if (dist < minVehicleDistance) {
                        minVehicleDistance = dist;
                    }
                }
                for (Package pkg2 : packageList) {
                    if (pkg2.getLocation() != null) {
                        int dist = distanceMatrix.get(pkgLocName, pkg2.getLocation().getName()).getDistance();
                        if (dist < minVehicleDistance) {
                            minVehicleDistance = dist;
                        }
                    }
                }
                sumDistances += minVehicleDistance;
            } else {
                sumDistances += 1; // drop, at least
            }
        }
        return sumDistances;
    }

    /**
     * Heuristic: mark edges on shortest paths of package locations to their targets or to
     * the closest vehicle or the closes package,
     * then sum up the marked edges.
     * Also adds costs for drop and pickup actions. Is <strong>admissible?</strong>.
     *
     * @param packageList the package list
     * @param vehicleList the vehicle list
     * @param distanceMatrix the distance matrix
     * @return the heuristic value.
     */
    public static int calculateSumOfDistancesToVehiclesPackageTargetsAdmissibleMark(Collection<Package> packageList,
            Collection<Vehicle> vehicleList, ArrayTable<String, String, ShortestPath> distanceMatrix) {
        int sumDistances = 0;
        Set<RoadEdge> edgesDriven = new HashSet<>();
        for (Package pkg : packageList) {
            Location pkgLocation = pkg.getLocation();
            if (pkgLocation != null) {
                String pkgLocName = pkgLocation.getName();
                // calculate the distance to the target + pickup and drop
                sumDistances += 2; // pickup and drop
                edgesDriven.addAll(distanceMatrix.get(pkgLocName, pkg.getTarget().getName()).getRoads());

                // Calculate the distance to the nearest vehicle or package
                int minVehicleDistance = Integer.MAX_VALUE;
                List<RoadEdge> minVehPath = Collections.emptyList();
                for (Vehicle vehicle : vehicleList) {
                    ShortestPath path = distanceMatrix.get(pkgLocName, vehicle.getLocation().getName());
                    if (path.getDistance() < minVehicleDistance) {
                        minVehicleDistance = path.getDistance();
                        minVehPath = path.getRoads();
                    }
                }
                for (Package pkg2 : packageList) {
                    if (pkg2.getLocation() != null) {
                        ShortestPath path = distanceMatrix.get(pkgLocName, pkg2.getLocation().getName());
                        if (path.getDistance() < minVehicleDistance) {
                            minVehicleDistance = path.getDistance();
                            minVehPath = path.getRoads();
                        }
                    }
                }
                edgesDriven.addAll(minVehPath);
            } else {
                sumDistances += 1; // drop, at least
            }
        }

        sumDistances += edgesDriven.stream().mapToInt(e -> e.getRoad().getLength().getCost()).sum();
        return sumDistances;
    }

    /**
     * Heuristic: sum distances of package locations to their targets or the closest vehicle or to the package location
     * of the closest package,
     * using the shortest available paths.
     * Also adds costs for drop and pickup actions. Is <strong>not admissible</strong>.
     * As a rule, this heuristic returns larger values than
     * {@link #calculateSumOfDistancesToPackageTargets(Collection, Collection, ArrayTable)}.
     *
     * @param packageList the package list
     * @param vehicleList the vehicle list
     * @param distanceMatrix the distance matrix
     * @return the heuristic value.
     */
    public static int calculateSumOfDistancesToVehiclesPackageTargetsAdmissibleReally(Collection<Package> packageList,
            Collection<Vehicle> vehicleList, ArrayTable<String, String, ShortestPath> distanceMatrix) {
        int sumDistances = 0;
        for (Package pkg : packageList) {
            Location pkgLocation = pkg.getLocation();
            Location target = pkg.getTarget();
            if (pkgLocation != null && pkgLocation.getName().equals(target.getName())) {
                continue; // skip packages at dest
            }
            Vehicle inVehicle = null;
            if (pkgLocation == null) {
                sumDistances += 1; // a drop is needed
                inVehicle = vehicleList.stream().filter(v -> v.getPackageList().contains(pkg))
                        .findAny()
                        .orElseThrow(() -> new IllegalStateException("Package disappeared: " + pkg.getName()));
                pkgLocation = inVehicle.getLocation();
            } else {
                sumDistances += 2; // a drop and pickup is needed
            }

            int pkgMinDist = Integer.MAX_VALUE;
            pkgMinDist = Math.min(pkgMinDist, distanceMatrix.get(pkgLocation.getName(), pkg.getTarget().getName())
                    .getDistance());
            if (inVehicle != null) { // pkg in vehicle
                int vehDist = Integer.MAX_VALUE;
                for (Vehicle vehicle : vehicleList) {
                    if (vehicle.getName().equals(inVehicle.getName())) {
                        continue;
                    }
                    int dist = distanceMatrix.get(pkgLocation.getName(), vehicle.getLocation().getName()).getDistance();
                    vehDist = Math.min(vehDist, dist);
                }
                pkgMinDist = Math.min(pkgMinDist, vehDist);
            }

            int pkgDist = Integer.MAX_VALUE;
            for (Package pkg2 : packageList) {
                if (pkg.getName().equals(pkg2.getName())) {
                    continue;
                }
                if (pkg2.getLocation() == null) {
                    continue; // already counted in vehicles in that case
                }
                int dist = distanceMatrix.get(pkgLocation.getName(), pkg2.getLocation().getName()).getDistance();
                pkgDist = Math.min(pkgDist, dist);
            }
            pkgMinDist = Math.min(pkgMinDist, pkgDist);
            sumDistances += pkgMinDist;
        }
        return sumDistances;
    }

    /**
     * Detects cycles in generated drive actions.
     *
     * @param reversedActions an iterator over generated actions, in reverse order
     * @return true iff a cycle was detected (without pickup or drop between 2 visits of one location)
     */
    private static boolean hasCycle(Iterator<Action> reversedActions) {
        Set<String> drives = new HashSet<>();
        if (!reversedActions.hasNext()) {
            return false;
        }
        Action lastAction = reversedActions.next();
        if (!reversedActions.hasNext()) { // has to have at least two actions
            return false;
        }
        if (lastAction instanceof Drive) { // add last target
            drives.add(lastAction.getWhat().getName());
        }
        while (reversedActions.hasNext()) {
            Action plannedAction = reversedActions.next();
            if (plannedAction instanceof Drive) {
                if (!drives.add(plannedAction.getWhere().getName())) {
                    return true;
                }
            } else {
                break;
            }
        }
        return false;
    }

    /**
     * Calculate a set of packages not yet at their target locations.
     *
     * @param packages all packages in the problem
     * @return a set of undelivered packages, possibly empty
     */
    public static Set<Package> getUnfinishedPackages(Collection<Package> packages) {
        Set<Package> unfinishedPackages = new HashSet<>(packages.size());
        for (Package pkg : packages) {
            if (pkg.getLocation() == null) {
                unfinishedPackages.add(pkg);
            } else if (!pkg.getLocation().getName().equals(pkg.getTarget().getName())) {
                unfinishedPackages.add(pkg);
            }
        }
        return unfinishedPackages;
    }

    /**
     * Detects drop actions that occurred at the same location as a pickup action of the same package and vehicle,
     * which is suboptimal (due to the costs of pickup and drop).
     * Similar to {@link #needlessDropAndPickup(Iterator, Vehicle, Package)}.
     *
     * @param state the state
     * @param newAction the newly generated drop action
     * @return true iff an unnecessary drop and pickup occurred
     */
    private static boolean droppedPackageWhereWePickedItUp(ImmutablePlanState state, Drop newAction) {
        Map<String, Set<String>> pickedUpAt = new HashMap<>();
        for (Iterator<Action> it = state.getAllActionsReversed(); it.hasNext();) {
            Action a = it.next();
            if (a instanceof PickUp) {
                pickedUpAt.computeIfAbsent(a.getWhat().getName(), s -> new HashSet<>(2))
                        .add(a.getWhere().getName());
            }
        }

        Set<String> pickedBySameCar = pickedUpAt.get(newAction.getWhat().getName());
        if (pickedBySameCar != null && pickedBySameCar.contains(newAction.getWhere().getName())) {
            return true;
        }
        for (Iterator<Action> it = state.getAllActionsReversed(); it.hasNext();) {
            Action a = it.next();
            if (a instanceof Drop) {
                pickedBySameCar = pickedUpAt.get(newAction.getWhat().getName());
                if (pickedBySameCar != null && pickedBySameCar.contains(newAction.getWhere().getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Calculate the maximum capacity needed at any point in time for a vehicle to contain the packages,
     * assuming the integers are "start" and "stop" times (i.e. loading and unloading times).
     *
     * @param combination the combination of packages and their times
     * @return the maximum number of packages present in the vehicle at the same time
     */
    public static Integer calculateMaxCapacity(
            javaslang.collection.List<Tuple3<Integer, Integer, Package>> combination) {
        return combination.flatMap(t -> javaslang.collection.Stream.of(Tuple.of(t._1, false, t._3),
                Tuple.of(t._2, true, t._3)))
                .sortBy(t -> t._1).foldLeft(Tuple.of(0, Integer.MIN_VALUE), (capTuple, elem) -> {
                    int curCapacity = capTuple._1;
                    if (elem._2) { // drop
                        curCapacity -= elem._3.getSize().getCost();
                    } else { // pickup
                        curCapacity += elem._3.getSize().getCost();
                    }
                    if (curCapacity > capTuple._2) {
                        return Tuple.of(curCapacity, curCapacity);
                    } else {
                        return Tuple.of(curCapacity, capTuple._2);
                    }
                })._2;
    }

    /**
     * Build pickup and drop actions for the given location.
     * Do note that the plan is being built in reverse order.
     *
     * @param domain the domain
     * @param actions the actions
     * @param afterDrop the packages that have already been dropped (i.e. not yet present in the reverse plan)
     * @param inVehicle the packages that are in the vehicle (i.e. only dropped in the reverse plan)
     * @param at the current location
     * @param vehicle the current vehicle
     * @param targetMap the map of package target locations
     */
    private static void buildPackageActions(Domain domain, List<Action> actions, Set<Package> afterDrop,
            Set<Package> inVehicle, Location at, Vehicle vehicle, Map<Package, Location> targetMap) {
        for (Iterator<Package> iter = inVehicle.iterator(); iter.hasNext();) {
            Package pkg = iter.next();
            if (pkg.getLocation().equals(at)) {
                actions.add(domain.buildPickUp(vehicle, at, pkg));
                iter.remove();
            }
        }
        for (Iterator<Package> iter = afterDrop.iterator(); iter.hasNext();) {
            Package pkg = iter.next();
            if (targetMap.getOrDefault(pkg, pkg.getTarget()).equals(at)) {
                actions.add(domain.buildDrop(vehicle, at, pkg));
                inVehicle.add(pkg);
                iter.remove();
            }
        }
    }

    /**
     * Build a plan for the vehicle, packages and their targets.
     *
     * @param domain the domain
     * @param path the vehicle's path
     * @param vehicle the vehicle
     * @param chosenPackages the chosen packages
     * @param targetMap the package target locations
     * @return the plan, in correct order
     */
    public static List<Action> buildPlan(Domain domain, List<RoadEdge> path, Vehicle vehicle,
            List<Package> chosenPackages, Map<Package, Location> targetMap) {
        if (path.isEmpty()) {
            return Collections.emptyList();
        }
        List<Action> actions = new ArrayList<>();
        Set<Package> afterDrop = new HashSet<>(chosenPackages);
        Set<Package> inVehicle = new HashSet<>(vehicle.getPackageList());
        for (int i = path.size() - 1; i >= 0; i--) {
            RoadEdge edge = path.get(i);
            Location to = edge.getTo();
            PlannerUtils.buildPackageActions(domain, actions, afterDrop, inVehicle, to, vehicle, targetMap);

            // drive
            actions.add(domain.buildDrive(vehicle, edge.getFrom(), to, edge.getRoad()));
        }
        // last loc
        Location firstLocation = path.get(0).getFrom();
        PlannerUtils.buildPackageActions(domain, actions, afterDrop, inVehicle, firstLocation, vehicle, targetMap);

        for (int i = 0; i < actions.size(); i++) { // remove redundant drives
            Action action = actions.get(i);
            if (action instanceof Drive) {
                actions.remove(i);
                i--;
            } else {
                break;
            }
        }
        return javaslang.collection.Stream.ofAll(actions).reverse().toJavaList();
    }

}
