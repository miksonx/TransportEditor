/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain;

import com.oskopek.transporteditor.planning.domain.action.Drive;
import com.oskopek.transporteditor.planning.domain.action.Drop;
import com.oskopek.transporteditor.planning.domain.action.PickUp;
import com.oskopek.transporteditor.planning.domain.action.Refuel;
import com.oskopek.transporteditor.planning.domain.action.functions.Function;
import com.oskopek.transporteditor.planning.domain.action.functions.RoadLength;
import com.oskopek.transporteditor.planning.domain.action.functions.TotalCost;
import com.oskopek.transporteditor.planning.domain.action.predicates.*;

import java.util.Arrays;
import java.util.List;

public class SequentialDomain extends DefaultDomain {

    private final List<Class<? extends Predicate>> predicateList = Arrays.asList(At.class, HasCapacity.class, In.class,
            IsRoad.class);

    private final List<Class<? extends Function>> functionList = Arrays.asList(RoadLength.class, TotalCost.class);

    public SequentialDomain(String name) {
        super(name, Drive.class, Drop.class, PickUp.class, Refuel.class);
    }

    @Override
    public List<Class<? extends Predicate>> getPredicateList() {
        return predicateList;
    }

    @Override
    public List<Class<? extends Function>> getFunctionList() {
        return functionList;
    }

    @Override
    public DomainType getDomainType() {
        return DomainType.ActionCost;
    }
}
