package com.decerto.leszek;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NumberStatisticsTest {

    @Test
    void getSmallestWillReturnTheSmallestNumber() {

        NumberStatistics ns = new NumberStatistics(List.of(5, 4, 1, 2, 3));

        assertEquals(1, ns.getSmallest());
    }

    @Test
    void getMeanWillReturnPopulationMean() {

        NumberStatistics ns = new NumberStatistics(List.of(5, 4, 1, 2, 3));

        assertEquals(3, ns.getMean(), 0.01);
    }

    @Test
    void getVarianceWillReturnPopulationVariance() {

        NumberStatistics ns = new NumberStatistics(List.of(5, 4, 1, 2, 3));

        assertEquals(2.5, ns.getVariance(), 0.01);
    }
}