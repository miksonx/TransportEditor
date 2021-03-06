package com.oskopek.transport.planners.sequential;

import com.oskopek.transport.model.planner.ExternalPlanner;
import com.oskopek.transport.tools.executables.DefaultExecutableWithParameters;
import com.oskopek.transport.tools.executables.ExecutableWithParameters;

/**
 * Wrapper of the wrapper script ({@code fast-down-plan.sh})
 * for the <a href="http://www.fast-downward.org/">Fast Downward</a> planner.
 */
public class FastDownwardExternalPlanner extends ExternalPlanner {

    private static final String executable = "fast-down-plan.sh";
    private static final String defaultSearchParameters = "--search astar(lmcut())";

    /**
     * Default empty constructor.
     */
    public FastDownwardExternalPlanner() {
        this("", defaultSearchParameters);
    }

    /**
     * Constructor for the {@code fast-down-plan.sh} script with any parameters.
     * <p>
     * Parameter templates: {0} and {1} can be in any order. {0} is the domain filename, {1} is the path filename,
     * and {2} is the output filename.
     *
     * @param parameters in the format: "... {0} ... {1} ... {2} ..."
     */
    public FastDownwardExternalPlanner(String parameters) {
        super(executable, parameters);
    }

    /**
     * Constructor for the {@code fast-down-plan.sh} script with parameters before domain-problem files
     * and any parameters afterwards. Final format: "{2} {@code beforeArgs} {0} {1} {@code afterArgs}".
     *
     * @param beforeArgs arguments before domain
     * @param afterArgs arguments after problem
     */
    public FastDownwardExternalPlanner(String beforeArgs, String afterArgs) {
        super(executable, "{2} " + beforeArgs + " {0} {1} " + afterArgs);
    }

    /**
     * Overridden constructor, used in {@link #copy()}.
     *
     * @param executableWithParameters the executable with parameters
     * @param name the name
     */
    public FastDownwardExternalPlanner(ExecutableWithParameters executableWithParameters, String name) {
        super(executableWithParameters, name);
    }

    /**
     * Only available on systems with a shell ({@code sh}), <a href="http://www.fast-downward.org/">Fast Downward</a>
     * and a wrapper script ({@code fast-down-plan.sh}).
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean isAvailable() {
        return new DefaultExecutableWithParameters(executable, "").isExecutableValid()
                && new DefaultExecutableWithParameters("sh", "").isExecutableValid();
    }

    @Override
    public FastDownwardExternalPlanner copy() {
        return new FastDownwardExternalPlanner(getExecutableWithParameters(), getName());
    }
}
