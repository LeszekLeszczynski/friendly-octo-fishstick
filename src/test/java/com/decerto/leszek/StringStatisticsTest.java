package com.decerto.leszek;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringStatisticsTest {

    @Test
    void testGetShortest() {
        StringStatistics stringStatistics = new StringStatistics(List.of("abc", "ab", "abcd"));
        assertEquals("ab", stringStatistics.getShortest());

        StringStatistics stringStatistics2 = new StringStatistics(List.of("abc", "abcd", "ab"));
        assertEquals("ab", stringStatistics2.getShortest());

        StringStatistics stringStatistics3 = new StringStatistics(List.of());
        assertEquals("", stringStatistics3.getShortest());
    }
}
