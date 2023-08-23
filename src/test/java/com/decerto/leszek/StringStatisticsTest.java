package com.decerto.leszek;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringStatisticsTest {

    /**
     * TODO: this test doesn't pass. Fix the method under test.
     */
    @Test
    void testGetShortest() {
        StringStatistics stringStatistics = new StringStatistics(List.of("abc", "ab", "abcd"));
        assertEquals("ab", stringStatistics.getShortest());

        StringStatistics stringStatistics2 = new StringStatistics(List.of("abc", "abcd", "ab"));
        assertEquals("ab", stringStatistics2.getShortest());

        StringStatistics stringStatistics3 = new StringStatistics(List.of());
        assertEquals("", stringStatistics3.getShortest());
    }

    /**
     * TODO: this test passes, but the method under test fails in some cases. Expand the test to include more cases and fix the method under test.
     */
    @Test
    void testGetLetterStatistics() {

        StringStatistics stringStatistics = new StringStatistics(List.of("abc!", "A BC", ",aAa"));

        var result = stringStatistics.letterStatistics();

        assertEquals(result, Map.of("a", 5l, "b", 2l, "c", 2l));
    }

}
