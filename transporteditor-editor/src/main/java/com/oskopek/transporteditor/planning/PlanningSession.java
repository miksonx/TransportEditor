package com.oskopek.transporteditor.planning;

import com.oskopek.transporteditor.planning.domain.Domain;
import com.oskopek.transporteditor.planning.plan.Plan;
import com.oskopek.transporteditor.planning.planner.Planner;
import com.oskopek.transporteditor.planning.problem.Problem;
import com.oskopek.transporteditor.validation.Validator;
import javafx.beans.property.ObjectProperty;

/**
 * Represents an orchestrator of the current planning session.
 * <p>
 * Keeps track of the domain, the plan, planner and any other user session
 * related data and settings (planner args, etc).
 */
public interface PlanningSession {

    /**
     * Manages a reference to the Transport domain variant used for planning in this session.
     *
     * @return the associated domain
     */
    Domain getDomain();

    void setDomain(Domain domain);

    ObjectProperty<Domain> domainProperty();

    Problem getProblem();

    void setProblem(Problem problem);

    ObjectProperty<Problem> problemProperty();

    Plan getPlan();

    void setPlan(Plan plan);

    ObjectProperty<Plan> planProperty();

    Planner getPlanner();

    void setPlanner(Planner planner);

    ObjectProperty<Planner> plannerProperty();

    Validator getValidator();

    void setValidator(Validator validator);

    ObjectProperty<Validator> validatorProperty();

    void startPlanning();

    void stopPlanning();

}