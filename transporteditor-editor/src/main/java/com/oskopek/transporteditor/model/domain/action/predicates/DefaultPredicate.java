package com.oskopek.transporteditor.model.domain.action.predicates;

import com.oskopek.transporteditor.model.domain.action.Action;
import com.oskopek.transporteditor.model.problem.Problem;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DefaultPredicate implements Predicate {

    private static final Logger logger = LoggerFactory.getLogger(Predicate.class);

    @Override
    public boolean isValid(Problem state, Action action) {
        boolean result = isValidInternal(state, action);
        if (logger.isDebugEnabled()) {
            logger.debug("Validating predicate {} in action {}: {}.", getClass().getSimpleName(), action.getName(),
                    result);
        }
        return result;
    }

    protected abstract boolean isValidInternal(Problem state, Action action);

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getPredicateName()).append(getTemporalQuantifier()).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Predicate)) {
            return false;
        }
        Predicate that = (Predicate) o;
        return new EqualsBuilder().append(getPredicateName(), that.getPredicateName()).append(getTemporalQuantifier(),
                that.getTemporalQuantifier()).isEquals();
    }

    @Override
    public String toString() {
        return getPredicateName();
    }
}
