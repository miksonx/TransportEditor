package com.oskopek.transport.session;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.domain.action.Action;
import com.oskopek.transport.model.domain.action.Refuel;
import com.oskopek.transport.model.domain.action.TemporalPlanAction;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.plan.TemporalPlan;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.persistence.SequentialPlanIO;
import com.oskopek.transport.persistence.VariableDomainIO;
import com.oskopek.transport.tools.test.TestUtils;
import com.oskopek.transport.validation.SequentialPlanValidator;
import com.oskopek.transport.view.problem.VisualProblemIO;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

public class DefaultPlanningSessionIOIT {

    private static DefaultPlanningSessionIO defaultPlanningSessionIO;
    private static String emptySessionFileContents;
    private static String p01SessionContents;
    private static String emptyPlannerContents;
    private static String domainFileContents;
    private static String problemFileContents;
    private static String planFileContents;
    private DefaultPlanningSession referenceSession;

    @Before
    public void setUp() throws Exception {
        emptySessionFileContents = TestUtils.getPersistenceTestFile("emptyDefaultPlanningSession.xml");
        p01SessionContents = TestUtils.getPersistenceTestFile("p01DefaultPlanningSession.xml");
        emptyPlannerContents = TestUtils.getPersistenceTestFile("emptyExternalPlanner.xml");
        domainFileContents = TestUtils.getPersistenceTestFile("variableDomainSeq.pddl");
        problemFileContents = TestUtils.getPersistenceTestFile("p01SeqProblem.pddl");
        planFileContents = TestUtils.getPersistenceTestFile("p01SeqPlan.val");
        defaultPlanningSessionIO = new DefaultPlanningSessionIO();
        referenceSession = new DefaultPlanningSession();
        referenceSession.setDomain(new VariableDomainIO().parse(domainFileContents));
        referenceSession.setProblem(new VisualProblemIO(referenceSession.getDomain())
                .parse(problemFileContents));
        referenceSession.setPlan(new SequentialPlanIO(referenceSession.getDomain(), referenceSession.getProblem())
                .parse(planFileContents));
        referenceSession.setValidator(new SequentialPlanValidator());
    }

    @Test
    public void testEmptySessionEquality() {
        PlanningSession parsed = defaultPlanningSessionIO.parse(emptySessionFileContents);
        testEqualityGradually(new DefaultPlanningSession(), parsed);
    }

    @Test
    public void testP01SessionEquality() {
        PlanningSession parsed = defaultPlanningSessionIO.parse(p01SessionContents);
        testEqualityGradually(referenceSession, parsed);
    }

    @Test
    public void testP01SessionSerializeEquality() {
        PlanningSession parsed = defaultPlanningSessionIO.parse(p01SessionContents);
        testEqualityGradually(referenceSession, parsed);
        String serialized = defaultPlanningSessionIO.serialize(parsed);
        PlanningSession parsed2 = defaultPlanningSessionIO.parse(serialized);
        assertFalse(parsed == parsed2);
        testEqualityGradually(referenceSession, parsed2);
        testEqualityGradually(parsed, parsed2);
    }

    @Test
    public void testP01SessionInDepth() throws Exception {
        PlanningSession parsed = defaultPlanningSessionIO.parse(p01SessionContents);
        Domain domain = parsed.getDomain();
        TestUtils.assertSequentialDomain(domain);
        Problem problem = parsed.getProblem();
        TestUtils.assertP01Sequential(domain, problem);
        Plan plan = parsed.getPlan();
        assertEquals(plan, TestUtils.P01SequentialPlan());
    }

    @Test
    public void testP01SessionSerializeInDepth() throws Exception {
        PlanningSession first = defaultPlanningSessionIO.parse(p01SessionContents);
        String serialized = defaultPlanningSessionIO.serialize(first);
        PlanningSession parsed = defaultPlanningSessionIO.parse(serialized);
        Domain domain = parsed.getDomain();
        TestUtils.assertSequentialDomain(domain);
        Problem problem = parsed.getProblem();
        TestUtils.assertP01Sequential(domain, problem);
        Plan plan = parsed.getPlan();
        assertEquals(plan, TestUtils.P01SequentialPlan());
    }

    @Test
    public void testParseFailsForExternalPlanner() throws Exception {
        assertThatThrownBy(() -> defaultPlanningSessionIO.parse(emptyPlannerContents, PlanningSession.class))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Could not parse");
        assertThatThrownBy(() -> defaultPlanningSessionIO.parse(emptyPlannerContents))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Could not parse");
    }

    @Test
    public void testSerializeTemporalPlan() throws Exception {
        PlanningSession session = new DefaultPlanningSession();
        Action emptyRefuel = new Refuel(null, null, Collections.emptyList(), Collections.emptyList(), null, null);
        TemporalPlan plan = new TemporalPlan(Arrays.asList(
                new TemporalPlanAction(emptyRefuel, 0d, 1d),
                new TemporalPlanAction(emptyRefuel, 0d, 5d)
        ));
        session.setPlan(plan);
        String serialized = defaultPlanningSessionIO.serialize(session);
        assertThat(serialized).containsIgnoringCase("refuel");
        assertThat(serialized).containsIgnoringCase("startTimestamp");
        assertThat(serialized).doesNotContain("head");
        assertThat(serialized).doesNotContain("inSync");
        assertThat(serialized).doesNotContain("<size>");
        PlanningSession parsed = defaultPlanningSessionIO.parse(serialized);
        assertThat(parsed.getPlan()).isNotNull().isEqualTo(plan);
    }

    private void testEqualityGradually(PlanningSession referenceSession, PlanningSession parsed) {
        assertNotNull(parsed);
        assertNull(parsed.getPlanner());
        assertEquals(referenceSession.getValidator(), parsed.getValidator());
        assertEquals(referenceSession.getDomain(), parsed.getDomain());
        assertEquals(referenceSession.getProblem(), parsed.getProblem());
        assertEquals(referenceSession.getPlan(), parsed.getPlan());
        assertEquals(referenceSession, parsed);
    }

}
