/*
 * Copyright (c) 2016 Ondrej Škopek <oskopek@oskopek.com>. All rights reserved.
 */

package com.oskopek.transporteditor.planning.plan.visualization;

import com.oskopek.transporteditor.planning.domain.Domain;
import com.oskopek.transporteditor.planning.plan.Plan;
import com.oskopek.transporteditor.planning.problem.Problem;

public class DefaultVisualizer implements PlanVisualizer {

    @Override
    public PlanState build(Domain domain, Problem problem, Plan plan, TimePoint timePoint) {
        return null;
    }
}
