/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.model.domain.actionbuilder;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.domain.action.PickUp;
import com.oskopek.transporteditor.model.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.model.problem.Location;
import com.oskopek.transporteditor.model.problem.Package;
import com.oskopek.transporteditor.model.problem.Vehicle;

import java.util.List;

public class PickUpBuilder implements ActionBuilder<PickUp, Vehicle, Package> {

    private final List<Predicate> preconditions;
    private final List<Predicate> effects;
    private final ActionCost cost;
    private final ActionCost duration;

    public PickUpBuilder(List<Predicate> preconditions, List<Predicate> effects, ActionCost cost, ActionCost duration) {
        this.preconditions = preconditions;
        this.effects = effects;
        this.cost = cost;
        this.duration = duration;
    }

    @Override
    public <Who_ extends Vehicle, What_ extends Package> PickUp build(Who_ who, Location where, What_ what) {
        return new PickUp(who, where, what, preconditions, effects, cost, duration);
    }
}