package com.oskopek.transporteditor.model.domain.action.functions;

import com.oskopek.transporteditor.model.domain.action.ActionCost;
import com.oskopek.transporteditor.model.problem.ActionObject;

/**
 * A PDDL function representation.
 */
public interface Function {

    /**
     * Implementations can choose how many parameters they take and how strict they are.
     *
     * @param actionObjects an array of actionObjects, possibly empty, non-null
     * @return non-null
     */
    ActionCost apply(ActionObject... actionObjects);

}
