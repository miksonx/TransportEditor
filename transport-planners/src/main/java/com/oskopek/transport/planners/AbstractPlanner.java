package com.oskopek.transport.planners;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.planner.Planner;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.tools.executables.CancellableLogStreamable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

import java.util.Optional;

/**
 * A planner implementation for simple Java-based planners. Handles listeners
 * and all needed JavaFX properties. Extensions only need to handle:
 * <ul>
 * <li>planning: {@link #plan(Domain, Problem)}</li>
 * <li>logging, using {@link #log(String)}</li>
 * </ul>
 */
public abstract class AbstractPlanner extends CancellableLogStreamable implements Planner {

    private String name = "AbstractPlanner";
    private final ObjectProperty<Plan> planProperty = new SimpleObjectProperty<>();
    private final BooleanProperty isPlanningProperty = new SimpleBooleanProperty(false);

    /**
     * Default empty constructor as per {@link Planner} requirements.
     */
    public AbstractPlanner() {
        // intentionally empty

    }

    /**
     * Create a plan for the given problem and domain. Will return empty {@link Optional} if no plan could be found,
     * for any reason.
     *
     * @param domain the domain
     * @param problem the problem
     * @return the plan, or nothing
     */
    public abstract Optional<Plan> plan(Domain domain, Problem problem);

    @Override
    public final Plan startAndWait(Domain domain, Problem problem) {
        isPlanningProperty.setValue(true);
        Plan plan = plan(domain, problem).orElse(null);
        isPlanningProperty.setValue(false);
        planProperty.setValue(plan);
        return plan;
    }

    @Override
    public final ObservableValue<Plan> currentPlanProperty() {
        return planProperty;
    }

    @Override
    public final ObservableValue<Boolean> isPlanning() {
        return isPlanningProperty;
    }

    @Override
    public final boolean isAvailable() {
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public abstract AbstractPlanner copy();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractPlanner)) {
            return false;
        }
        AbstractPlanner that = (AbstractPlanner) o;
        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}