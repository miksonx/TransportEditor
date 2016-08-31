/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain;

import com.oskopek.transporteditor.planning.domain.action.functions.Function;
import com.oskopek.transporteditor.planning.domain.action.functions.RoadLength;
import com.oskopek.transporteditor.planning.domain.action.functions.TotalCost;
import com.oskopek.transporteditor.planning.domain.action.predicates.*;

import java.util.Arrays;
import java.util.List;

public class SequentialDomain implements Domain {

    private final List<Predicate> predicateList = Arrays.asList(new At(null), new HasCapacity(null), new In(null),
            new IsRoad(null));

    private final List<Function> functionList = Arrays.asList(new TotalCost(), new RoadLength());

    @Override
    public List<? extends Predicate> getPredicates() {
        return predicateList;
    }

    @Override
    public List<? extends Function> getFunctions() {
        return functionList;
    }
}
