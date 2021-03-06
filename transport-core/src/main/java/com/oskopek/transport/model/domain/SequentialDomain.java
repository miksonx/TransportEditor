package com.oskopek.transport.model.domain;

import com.oskopek.transport.model.domain.action.ActionCost;
import com.oskopek.transport.model.domain.action.functions.Function;
import com.oskopek.transport.model.domain.action.functions.RoadLength;
import com.oskopek.transport.model.domain.action.functions.TotalCost;
import com.oskopek.transport.model.domain.action.predicates.*;
import com.oskopek.transport.model.domain.actionbuilder.DriveBuilder;
import com.oskopek.transport.model.domain.actionbuilder.DropBuilder;
import com.oskopek.transport.model.domain.actionbuilder.PickUpBuilder;
import javaslang.collection.HashSet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A hard-coded sequential domain implementation. Should not differ from loading the sequential domain into a
 * {@link VariableDomain}.
 */
public class SequentialDomain extends DefaultDomain {

    private static final Map<String, Class<? extends Predicate>> predicateMap;
    private static final Map<String, Class<? extends Function>> functionMap;

    /**
     * Default constructor. Initializes all the builder and labels.
     *
     * @param name the name of the domain
     */
    public SequentialDomain(String name) {
        super(name, new DriveBuilder(Arrays.asList(new WhoAtWhere(), new IsRoad(), new HasFuelCapacityForDrive()),
                        Arrays.asList(new Not(new WhoAtWhere()), new WhoAtWhat())),
                new DropBuilder(Arrays.asList(new WhoAtWhere(), new In()),
                        Arrays.asList(new Not(new In()), new WhatAtWhere()),
                        ActionCost.ONE, ActionCost.ONE),
                new PickUpBuilder(Arrays.asList(new WhoAtWhere(), new WhatAtWhere()),
                        Arrays.asList(new Not(new WhatAtWhere()), new In()), ActionCost.ONE,
                        ActionCost.ONE), null,
                HashSet.of(PddlLabel.ActionCost, PddlLabel.Capacity, PddlLabel.MaxCapacity)
                        .toJavaSet());
    }

    @Override
    public Map<String, Class<? extends Predicate>> getPredicateMap() {
        return predicateMap;
    }

    @Override
    public Map<String, Class<? extends Function>> getFunctionMap() {
        return functionMap;
    }

    static {
        predicateMap = new HashMap<>(4);
        predicateMap.put("at", WhoAtWhere.class);
        predicateMap.put("capacity", HasCapacity.class);
        predicateMap.put("in", In.class);
        predicateMap.put("road", IsRoad.class);
    }

    static {
        functionMap = new HashMap<>(2);
        functionMap.put("road-length", RoadLength.class);
        functionMap.put("total-cost", TotalCost.class);
    }
}
