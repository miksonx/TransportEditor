/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.domain.action;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public final class ActionCost {

    private final IntegerProperty cost = new SimpleIntegerProperty();

    private ActionCost(Integer cost) {
        this.cost.setValue(cost);
    }

    public static ActionCost valueOf(Integer cost) {
        return new ActionCost(cost);
    }

    public ActionCost add(ActionCost cost) {
        return ActionCost.valueOf(getCost() + cost.getCost());
    }

    public Integer getCost() {
        return cost.get();
    }

    public void setCost(Integer cost) {
        this.cost.setValue(cost);
    }

    public IntegerProperty costProperty() {
        return cost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ActionCost)) {
            return false;
        }
        ActionCost that = (ActionCost) o;
        return new EqualsBuilder().append(getCost(), that.getCost()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getCost()).toHashCode();
    }

    @Override
    public String toString() {
        return cost.getValue().toString();
    }
}