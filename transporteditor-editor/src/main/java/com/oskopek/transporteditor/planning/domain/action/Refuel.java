/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action;

import com.oskopek.transporteditor.planning.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.planning.problem.Location;
import com.oskopek.transporteditor.planning.problem.PlaceholderActionObject;
import com.oskopek.transporteditor.planning.problem.Vehicle;

import java.util.List;

public class Refuel extends DefaultAction<Vehicle, Location, PlaceholderActionObject> {

    public Refuel(Vehicle vehicle, Location location,
            List<Predicate> preconditions, List<Predicate> effects, ActionCost cost, ActionCost duration) {
        super("refuel", vehicle, location, new PlaceholderActionObject(), preconditions, effects, cost, duration);
    }
}
