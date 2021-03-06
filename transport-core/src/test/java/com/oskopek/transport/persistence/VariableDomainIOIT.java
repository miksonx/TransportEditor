package com.oskopek.transport.persistence;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.oskopek.transport.model.domain.PddlLabel;
import com.oskopek.transport.model.domain.VariableDomain;
import com.oskopek.transport.model.domain.action.ActionCost;
import com.oskopek.transport.model.domain.action.TemporalQuantifier;
import com.oskopek.transport.model.domain.action.functions.*;
import com.oskopek.transport.model.domain.action.predicates.*;
import com.oskopek.transport.model.problem.DefaultRoad;
import com.oskopek.transport.model.problem.Location;
import com.oskopek.transport.model.problem.graph.DefaultRoadGraph;
import com.oskopek.transport.model.problem.graph.RoadGraph;
import com.oskopek.transport.tools.test.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.stream.Collectors;

import static com.oskopek.transport.persistence.IOUtils.readAllLines;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class VariableDomainIOIT {

    private static final String variableDomainSeqPDDL = "variableDomainSeq.pddl";
    private static final String variableDomainBPDDL = "variableDomainB.pddl";
    private static final String variableDomainTempPDDL = "variableDomainTemp.pddl";
    private static VariableDomainIO variableDomainIO;
    private static String variableDomainSeqPDDLContents;
    private static String variableDomainBPDDLContents;
    private static String variableDomainTempPDDLContents;
    private static RoadGraph roadGraph;
    private VariableDomain variableDomainSeq;
    private VariableDomain variableDomainB;

    @BeforeClass
    public static void setUpClass() throws Exception {
        variableDomainSeqPDDLContents = readAllLines(
                VariableDomainIOIT.class.getResourceAsStream(variableDomainSeqPDDL)).stream().collect(
                Collectors.joining("\n")) + "\n";
        variableDomainBPDDLContents = readAllLines(
                VariableDomainIOIT.class.getResourceAsStream(variableDomainBPDDL)).stream().collect(
                Collectors.joining("\n")) + "\n";
        variableDomainTempPDDLContents = readAllLines(
                VariableDomainIOIT.class.getResourceAsStream(variableDomainTempPDDL)).stream().collect(
                Collectors.joining("\n")) + "\n";
        variableDomainIO = new VariableDomainIO();

        roadGraph = new DefaultRoadGraph("");
        roadGraph.addLocation(new Location("a", 0, 0));
        roadGraph.addLocation(new Location("b", 0, 0));
        roadGraph.addRoad(new DefaultRoad("a->b", ActionCost.valueOf(11)), roadGraph.getLocation("a"),
                roadGraph.getLocation("b"));
    }

    @Before
    public void setUp() throws Exception {
        variableDomainSeq = spy(new VariableDomain("Transport sequential", null, null, null, null,
                ImmutableSet.of(PddlLabel.ActionCost, PddlLabel.Capacity, PddlLabel.MaxCapacity), null, null));
        when(variableDomainSeq.getFunctionMap()).thenReturn(
                ImmutableMap.of("road-length", RoadLength.class, "total-cost", TotalCost.class));
        when(variableDomainSeq.getPredicateMap()).thenReturn(
                ImmutableMap.of("at", WhoAtWhere.class, "capacity", HasCapacity.class, "in", In.class, "road",
                        IsRoad.class));

        variableDomainB = spy(new VariableDomain("Transport temporal without fuel", null, null, null, null,
                ImmutableSet.of(PddlLabel.Temporal, PddlLabel.Capacity, PddlLabel.MaxCapacity), null, null));
        when(variableDomainB.getFunctionMap()).thenReturn(ImmutableMap
                .of("capacity", Capacity.class, "package-size", PackageSize.class, "road-length", RoadLength.class));
        when(variableDomainB.getPredicateMap()).thenReturn(ImmutableMap
                .of("at", WhoAtWhere.class, "in", In.class, "road", IsRoad.class, "ready-loading", ReadyLoading.class));
    }

    @Test
    public void parseTemp() throws Exception {
        VariableDomain parsed = variableDomainIO.parse(variableDomainTempPDDLContents);
        assertNotNull(parsed);

        assertThat(parsed.getPddlLabels()).contains(PddlLabel.Capacity);
        assertThat(parsed.getPddlLabels()).contains(PddlLabel.MaxCapacity);
        assertThat(parsed.getPddlLabels()).contains(PddlLabel.Temporal);
        assertThat(parsed.getPddlLabels()).doesNotContain(PddlLabel.ActionCost);
        assertThat(parsed.getPddlLabels()).contains(PddlLabel.Fuel);

        assertEquals(ActionCost.ONE, parsed.getDropBuilder().build(null, null, null).getCost());
        assertEquals(ActionCost.ONE, parsed.getDropBuilder().build(null, null, null).getDuration());
        assertEquals(ActionCost.ONE, parsed.getPickUpBuilder().build(null, null, null).getCost());
        assertEquals(ActionCost.ONE, parsed.getPickUpBuilder().build(null, null, null).getDuration());
        assertEquals(ActionCost.valueOf(11),
                parsed.getDriveBuilder().build(null, roadGraph.getLocation("a"), roadGraph.getLocation("b"),
                        roadGraph, true)
                        .getCost());
        assertEquals(ActionCost.valueOf(11),
                parsed.getDriveBuilder().build(null, roadGraph.getLocation("a"), roadGraph.getLocation("b"),
                        roadGraph, true)
                        .getDuration());
        assertEquals(ActionCost.valueOf(10), parsed.getRefuelBuilder().build(null, null, null).getDuration());
        assertEquals(ActionCost.valueOf(10), parsed.getRefuelBuilder().build(null, null, null).getCost());

        // drive
        assertThat(parsed.getDriveBuilder().getPreconditions()).contains(
                new TemporalPredicate(new WhoAtWhere(), TemporalQuantifier.AT_START));
        assertThat(parsed.getDriveBuilder().getPreconditions()).contains(
                new TemporalPredicate(new IsRoad(), TemporalQuantifier.AT_START));
        assertEquals(3, parsed.getDriveBuilder().getPreconditions().size());

        assertThat(parsed.getDriveBuilder().getEffects()).contains(
                new TemporalPredicate(new Not(new WhoAtWhere()), TemporalQuantifier.AT_START));
        assertThat(parsed.getDriveBuilder().getEffects()).contains(
                new TemporalPredicate(new WhoAtWhat(), TemporalQuantifier.AT_END));
        assertEquals(2, parsed.getDriveBuilder().getEffects().size());

        // pickup
        assertThat(parsed.getPickUpBuilder().getPreconditions()).contains(
                new TemporalPredicate(new WhoAtWhere(), TemporalQuantifier.AT_START));
        assertThat(parsed.getPickUpBuilder().getPreconditions()).contains(
                new TemporalPredicate(new WhoAtWhere(), TemporalQuantifier.OVER_ALL));
        assertThat(parsed.getPickUpBuilder().getPreconditions()).contains(
                new TemporalPredicate(new WhatAtWhere(), TemporalQuantifier.AT_START));
        assertEquals(3, parsed.getPickUpBuilder().getPreconditions().size());

        assertThat(parsed.getPickUpBuilder().getEffects()).contains(
                new TemporalPredicate(new In(), TemporalQuantifier.AT_END));
        assertThat(parsed.getPickUpBuilder().getEffects()).contains(
                new TemporalPredicate(new Not(new WhatAtWhere()), TemporalQuantifier.AT_START));
        assertEquals(2, parsed.getPickUpBuilder().getEffects().size());

        // drop
        assertThat(parsed.getDropBuilder().getPreconditions()).contains(
                new TemporalPredicate(new WhoAtWhere(), TemporalQuantifier.AT_START));
        assertThat(parsed.getDropBuilder().getPreconditions()).contains(
                new TemporalPredicate(new WhoAtWhere(), TemporalQuantifier.OVER_ALL));
        assertThat(parsed.getDropBuilder().getPreconditions()).contains(
                new TemporalPredicate(new In(), TemporalQuantifier.AT_START));
        assertEquals(3, parsed.getDropBuilder().getPreconditions().size());

        assertThat(parsed.getDropBuilder().getEffects()).contains(
                new TemporalPredicate(new Not(new In()), TemporalQuantifier.AT_START));
        assertThat(parsed.getDropBuilder().getEffects()).contains(
                new TemporalPredicate(new WhatAtWhere(), TemporalQuantifier.AT_END));
        assertEquals(2, parsed.getDropBuilder().getEffects().size());

        assertNotNull(parsed.getRefuelBuilder());
    }

    @Test
    public void parseSeq() throws Exception {
        VariableDomain parsed = variableDomainIO.parse(variableDomainSeqPDDLContents);
        TestUtils.assertSequentialDomain(parsed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseSeqIllegalRoad() throws Exception {
        VariableDomain parsed = variableDomainIO.parse(variableDomainSeqPDDLContents);
        assertNotNull(parsed);
        assertEquals(ActionCost.ZERO,
                parsed.getDriveBuilder().build(null, roadGraph.getLocation("b"), roadGraph.getLocation("a"),
                        roadGraph, false)
                        .getDuration());
    }

    @Test
    public void serializeSeq() throws Exception {
        String serialized = variableDomainIO.serialize(variableDomainSeq);
        assertNotNull(serialized);
        assertEquals(variableDomainSeqPDDLContents, serialized);
    }

    @Test
    public void parseB() throws Exception {
        VariableDomain parsed = variableDomainIO.parse(variableDomainBPDDLContents);
        assertNotNull(parsed);

        assertThat(parsed.getPddlLabels()).contains(PddlLabel.Capacity);
        assertThat(parsed.getPddlLabels()).contains(PddlLabel.MaxCapacity);
        assertThat(parsed.getPddlLabels()).contains(PddlLabel.Temporal);
        assertThat(parsed.getPddlLabels()).doesNotContain(PddlLabel.ActionCost);
        assertThat(parsed.getPddlLabels()).doesNotContain(PddlLabel.Fuel);

        assertEquals(ActionCost.ONE, parsed.getDropBuilder().build(null, null, null).getCost());
        assertEquals(ActionCost.ONE, parsed.getDropBuilder().build(null, null, null).getDuration());
        assertEquals(ActionCost.ONE, parsed.getPickUpBuilder().build(null, null, null).getCost());
        assertEquals(ActionCost.ONE, parsed.getPickUpBuilder().build(null, null, null).getDuration());
        assertNull(parsed.getRefuelBuilder());

        // drive
        assertThat(parsed.getDriveBuilder().getPreconditions()).contains(
                new TemporalPredicate(new WhoAtWhere(), TemporalQuantifier.AT_START));
        assertThat(parsed.getDriveBuilder().getPreconditions()).contains(
                new TemporalPredicate(new IsRoad(), TemporalQuantifier.AT_START));
        assertEquals(3, parsed.getDriveBuilder().getPreconditions().size());

        assertThat(parsed.getDriveBuilder().getEffects()).contains(
                new TemporalPredicate(new Not(new WhoAtWhere()), TemporalQuantifier.AT_START));
        assertThat(parsed.getDriveBuilder().getEffects()).contains(
                new TemporalPredicate(new WhoAtWhat(), TemporalQuantifier.AT_END));
        assertEquals(2, parsed.getDriveBuilder().getEffects().size());

        // pickup
        assertThat(parsed.getPickUpBuilder().getPreconditions()).contains(
                new TemporalPredicate(new WhoAtWhere(), TemporalQuantifier.AT_START));
        assertThat(parsed.getPickUpBuilder().getPreconditions()).contains(
                new TemporalPredicate(new WhoAtWhere(), TemporalQuantifier.OVER_ALL));
        assertThat(parsed.getPickUpBuilder().getPreconditions()).contains(
                new TemporalPredicate(new WhatAtWhere(), TemporalQuantifier.AT_START));
        assertEquals(3, parsed.getPickUpBuilder().getPreconditions().size());

        assertThat(parsed.getPickUpBuilder().getEffects()).contains(
                new TemporalPredicate(new In(), TemporalQuantifier.AT_END));
        assertThat(parsed.getPickUpBuilder().getEffects()).contains(
                new TemporalPredicate(new Not(new WhatAtWhere()), TemporalQuantifier.AT_START));
        assertEquals(2, parsed.getPickUpBuilder().getEffects().size());

        // drop
        assertThat(parsed.getDropBuilder().getPreconditions()).contains(
                new TemporalPredicate(new WhoAtWhere(), TemporalQuantifier.AT_START));
        assertThat(parsed.getDropBuilder().getPreconditions()).contains(
                new TemporalPredicate(new WhoAtWhere(), TemporalQuantifier.OVER_ALL));
        assertThat(parsed.getDropBuilder().getPreconditions()).contains(
                new TemporalPredicate(new In(), TemporalQuantifier.AT_START));
        assertEquals(3, parsed.getDropBuilder().getPreconditions().size());

        assertThat(parsed.getDropBuilder().getEffects()).contains(
                new TemporalPredicate(new Not(new In()), TemporalQuantifier.AT_START));
        assertThat(parsed.getDropBuilder().getEffects()).contains(
                new TemporalPredicate(new WhatAtWhere(), TemporalQuantifier.AT_END));
        assertEquals(2, parsed.getDropBuilder().getEffects().size());
    }

    @Test
    public void serializeB() throws Exception {
        String serialized = variableDomainIO.serialize(variableDomainB);
        assertNotNull(serialized);
        assertEquals(variableDomainBPDDLContents, serialized);
    }

    public void deserializeSerialize(String fileName) throws Exception {
        String contents = readAllLines(VariableDomainIOIT.class.getResourceAsStream(fileName)).stream()
                .collect(Collectors.joining("\n")) + "\n";
        VariableDomain domain = variableDomainIO.parse(contents);
        assertNotNull(domain);
        String serialized = variableDomainIO.serialize(domain);
        assertEquals(contents, serialized);
    }

    @Test
    public void deserializeSerializeCompareSeq() throws Exception {
        deserializeSerialize(variableDomainSeqPDDL);
    }

    @Test
    public void deserializeSerializeCompareTemp() throws Exception {
        deserializeSerialize(variableDomainTempPDDL);
    }

    @Test
    public void deserializeSerializeCompareNum() throws Exception {
        deserializeSerialize("variableDomainNum.pddl");
    }

    @Test
    public void deserializeSerializeCompareB() throws Exception {
        deserializeSerialize(variableDomainBPDDL);
    }

}
