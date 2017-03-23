package com.oskopek.transporteditor.planners.benchmark.report.reporters;

/**
 * Generates a {@link LatexProblemPlannerReporter} table with runtime in seconds as elements.
 */
public class RunTimeLatexTableReporter extends LatexProblemPlannerReporter {

    /**
     * Default constructor.
     */
    public RunTimeLatexTableReporter() {
        super(r -> r.getResults().getDurationMs() / 1000d);
    }

    @Override
    public String getFileName() {
        return "runtime_table.tex";
    }
}
