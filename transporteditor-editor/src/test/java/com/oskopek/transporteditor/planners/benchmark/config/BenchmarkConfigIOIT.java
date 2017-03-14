package com.oskopek.transporteditor.planners.benchmark.config;

import com.oskopek.transporteditor.model.domain.SequentialDomain;
import com.oskopek.transporteditor.planners.benchmark.Benchmark;
import static org.assertj.core.api.Assertions.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class BenchmarkConfigIOIT {

    private final String basePath = "src/test/resources/com/oskopek/transporteditor/planners/benchmark/config/";
    private final String simpleConfigPath = basePath + "simple-benchmark-config.json";

    @Before
    public void setUp() throws Exception {
        System.setProperty("transport.root", basePath);
    }

    @After
    public void tearDown() throws Exception {
        System.clearProperty("transport.root");
    }

    @Test
    public void parse() throws Exception {
        BenchmarkConfig config = BenchmarkConfig.from(simpleConfigPath);
        assertThat(config).isNotNull();
        assertThat(config.getDomain()).isNotNull();
        assertThat(config.getProblems()).hasSize(2);
        assertThat(config.getPlanners()).hasSize(2);
        assertThat(config.getPlanners().values()).containsExactlyInAnyOrder((String) null, "{0} {1} --search astar(ff())");
        assertThat(config.getPlanners().keySet()).allMatch(s -> s.startsWith("com.oskopek.transporteditor.planners."));
        assertThat(config.getScoreFunctionType()).isNotNull().isEqualTo(ScoreFunctionType.ACTION_COUNT);
        assertThat(config.getThreadCount()).isNull();
    }

    @Test
    public void parseWithThreadCount() throws Exception {
        BenchmarkConfig config = BenchmarkConfig.from(basePath + "simple-benchmark-config-threadcount.json");
        assertThat(config).isNotNull();
        assertThat(config.getDomain()).isNotNull();
        assertThat(config.getProblems()).hasSize(2);
        assertThat(config.getPlanners()).hasSize(2);
        assertThat(config.getPlanners().values()).containsExactlyInAnyOrder((String) null, "{0} {1} --search astar(ff())");
        assertThat(config.getPlanners().keySet()).allMatch(s -> s.startsWith("com.oskopek.transporteditor.planners."));
        assertThat(config.getScoreFunctionType()).isNotNull().isEqualTo(ScoreFunctionType.ACTION_COUNT);
        assertThat(config.getThreadCount()).isNotNull().isEqualTo(2);
    }

    @Test
    @Ignore("Use internal planners for this test") // TODO un-ignore
    public void toBenchmark() throws Exception {
        BenchmarkConfig config = BenchmarkConfig.from(simpleConfigPath);
        Benchmark benchmark = config.toBenchmark();
        assertThat(benchmark).isNotNull();
        Integer threadCount = config.getThreadCount();
        benchmark.benchmark(threadCount); // TODO: Verify that uses correct arguments and threads
    }
}
