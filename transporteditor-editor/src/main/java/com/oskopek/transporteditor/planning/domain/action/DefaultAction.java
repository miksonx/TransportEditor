/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action;

import com.oskopek.transporteditor.planning.domain.action.predicates.Predicate;
import com.oskopek.transporteditor.planning.problem.ActionObject;
import com.oskopek.transporteditor.planning.problem.Locatable;
import com.oskopek.transporteditor.planning.problem.Location;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class DefaultAction<Who extends Locatable, Where extends Location, What extends ActionObject> {

    private final String name;
    private final Who who;
    private final Where where;
    private final What what;
    private final List<Predicate> preconditions;
    private final List<Predicate> effects;
    private final ActionCost cost;
    private final ActionCost duration;

    public DefaultAction(String name, Who who, Where where, What what, List<Predicate> preconditions,
            List<Predicate> effects, ActionCost cost, ActionCost duration) {
        this.name = name;
        this.who = who;
        this.where = where;
        this.what = what;
        this.preconditions = preconditions;
        this.effects = effects;
        this.cost = cost;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public Who getWho() {
        return who;
    }

    public Where getWhere() {
        return where;
    }

    public What getWhat() {
        return what;
    }

    public List<Predicate> getPreconditions() {
        return preconditions;
    }

    public List<Predicate> getEffects() {
        return effects;
    }

    public ActionCost getCost() {
        return cost;
    }

    public ActionCost getDuration() {
        return duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof DefaultAction)) {
            return false;
        }

        DefaultAction<?, ?, ?> that = (DefaultAction<?, ?, ?>) o;

        return new EqualsBuilder().append(getName(), that.getName()).append(getWho(), that.getWho()).append(getWhere(),
                that.getWhere()).append(getWhat(), that.getWhat()).append(getPreconditions(), that.getPreconditions())
                .append(getEffects(), that.getEffects()).append(getCost(), that.getCost()).append(getDuration(),
                        that.getDuration()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getName()).append(getWho()).append(getWhere()).append(getWhat())
                .append(getPreconditions()).append(getEffects()).append(getCost()).append(getDuration()).toHashCode();
    }

    @Override
    public String toString() {
        return name + "[" + who + " @ " + where + " -> " + what + "]";
    }
}
