% Simple forward DFS planner for IPC 08 Transport

% TODOs:
% TODO implement BFS -- this domain guarantees optimality with BFS
% TODO implement to/from convert scripts
% TODO imlement append in constant time --> or do we just prepend?
% TODO implement IO
% TODO implement heuristic

% PDDL actions (planner output):
% (drive ?v-vehicle ?l1-location ?l2-location)
% (pick-up ?v-vehicle ?l-location ?p-package ?s1-capacity-predecessor ?s2-capacity-predecessor)
% (drop ?v-vehicle ?l-location ?p-package ?s1-capacity-predecessor ?s2-capacity-predecessor)
% ; cost = 54 (general cost)

% Prolog data structures:
% s(Vehicles, Packages) ... state
% v(name, carrying_packages, location, destinations, carryingNumber, capacity) ... vehicle
% p(name, location, destination) ... package
% r(location1, location2, cost) ... road

% action(+State, -NewState, -PlanAction, -Cost)

% force-drop all packages which are at their destination
action(s(Vehicles, Packages, Graph), NewState, PlanAction, Cost) :-
    Cost = 1,
    select(V, Vehicles, NewVehicles),
    V = v(Name, Carrying, Location, Destinations, CarryingNum, Capacity),
    select(p(PName, _, Location), Carrying, NewCarrying), % package that is at its destination
    NewCarryingNum is CarryingNum - 1,
    NewV = v(Name, NewCarrying, Location, Destinations, NewCarryingNum, Capacity),
    append(NewVehicles, [NewV], ReturnVehicles),
    NewState = s(ReturnVehicles, Packages, Graph),
    PlanAction = drop(Name, Location, PName, CarryingNum, NewCarryingNum).

% drop (only packages where dest != loc ... due to ordering of clauses)
action(s(Vehicles, Packages, Graph), NewState, PlanAction, Cost) :-
    Cost = 1,
    select(V, Vehicles, NewVehicles),
    V = v(Name, Carrying, Location, Destinations, CarryingNum, Capacity),
    select(P, Carrying, NewCarrying),
    P = p(PName, _, PDestination),
    NewP = p(PName, Location, PDestination),
    append(Packages, [NewP], NewPackages),
    NewCarryingNum is CarryingNum - 1,
    delete(Destinations, PDestination, NewDestinations), % TODO find a way around this
    NewV = v(Name, NewCarrying, Location, NewDestinations, NewCarryingNum, Capacity),
    append(NewVehicles, [NewV], ReturnVehicles),
    NewState = s(ReturnVehicles, NewPackages, Graph),
    PlanAction = drop(Name, Location, PName, CarryingNum, NewCarryingNum).

% pick-up
action(s(Vehicles, Packages, Graph), NewState, PlanAction, Cost) :-
    Cost = 1,
    select(V, Vehicles, NewVehicles),
    V = v(Name, Carrying, Location, Destinations, CarryingNum, Capacity),
    CarryingNum < Capacity,
    NewCarryingNum is CarryingNum + 1,
    select(P, Packages, NewPackages),
    P = p(PName, Location, PDestination), % TODO check if PDest =:= Loc?
    NewP = p(PName, in_transport, PDestination),
    append(Carrying, [NewP], NewCarrying),
    append(Destinations, [PDestination], NewDestinations),
    NewV = v(Name, NewCarrying, Location, NewDestinations, NewCarryingNum, Capacity),
    append(NewVehicles, [NewV], ReturnVehicles),
    NewState = s(ReturnVehicles, NewPackages, Graph),
    PlanAction = pick-up(Name, Location, PName, CarryingNum, NewCarryingNum).

% drive
action(s(Vehicles, Packages, Graph), NewState, PlanAction, Cost) :-
    member(r(Location1, Location2, Cost), Graph),
    select(V, Vehicles, NewVehicles),
    V = v(Name, Carrying, Location1, Destinations, CarryingNum, Capacity),
    delete(Destinations, Location2, NewDestinations),
    NewV = v(Name, Carrying, Location2, NewDestinations, CarryingNum, Capacity),
    append(NewVehicles, [NewV], ReturnVehicles),
    NewState = s(ReturnVehicles, Packages, Graph),
    PlanAction = drive(Name, Location1, Location2). % TODO is vs = ?

% TODO do we need the appends or do we just prepend?

% goal(+state) -- is the state a goal state?
goal(s([], [], _)) :- !.
goal(s([Vehicle|Tail], [], _)) :-
    Vehicle = v(_, [], _, _, 0, _),
    goal(s(Tail, [], _)).

% findactions(+State, +OldActions, -NewActions, +OldCost, -NewCost)
findactions([OldState|_], OldActions, RevActions, Cost, Cost) :-
    goal(OldState), reverse(OldActions, RevActions), !.
findactions(States, OldActions, NewActions, OldCost, NewCost) :-
    States = [OldState|_],
    \+goal(OldState), % do we need this?
    action(OldState, CurState, PlanAction, ActionCost),
    \+member(CurState, States),
    CurActions = [PlanAction|OldActions],
    CurCost is OldCost + ActionCost,
    findactions([CurState|States], CurActions, NewActions, CurCost, NewCost).

% plan(-Plan, -TotalCost)
plan(Plan, TotalCost) :-
    problem(InitState),
    findactions([InitState], [], Plan, 0, TotalCost).


% problemS(-InitState)
problemS(InitState) :- % TODO remove me
    Packages = [p(package1, cityloc1, cityloc2)],
    T1 = v(truck1, [], cityloc1, [], 0, 4),
    Vehicles = [T1],
    Graph = [r(cityloc1, cityloc2, 50),
             r(cityloc2, cityloc1, 50)
             ],
    InitState = s(Vehicles, Packages, Graph).

% problem(-InitState)
problem(InitState) :- % TODO remove me
    Packages = [p(package1, cityloc3, cityloc2), p(package2, cityloc3, cityloc2)],
    T1 = v(truck1, [], cityloc3, [], 0, 4),
    T2 = v(truck2, [], cityloc1, [], 0, 3),
    Vehicles = [T1, T2],
    Graph = [r(cityloc3, cityloc1, 22),
             r(cityloc1, cityloc3, 22),
             r(cityloc3, cityloc2, 50),
             r(cityloc2, cityloc3, 50)
             ],
    InitState = s(Vehicles, Packages, Graph).

