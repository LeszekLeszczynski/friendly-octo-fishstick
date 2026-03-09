package com.decerto.leszek.coverage;

import org.junit.jupiter.api.Test;

import java.util.List;

import static java.time.LocalDate.of;
import static org.assertj.core.api.Assertions.assertThat;

class CoverageAnalyzerTest {

    private final CoverageAnalyzer analyzer = new CoverageAnalyzer();

    @Test
    void shouldFindGapBetweenOverlappingAndNonContiguousPeriods() {
        var periods = List.of(
                new CoveragePeriod(of(2024, 3, 1), of(2024, 5, 31)),
                new CoveragePeriod(of(2024, 1, 1), of(2024, 2, 28)),   // nieposortowane
                new CoveragePeriod(of(2024, 4, 1), of(2024, 7, 31)),   // nakłada się
                new CoveragePeriod(of(2024, 9, 1), of(2024, 12, 31))
        );

        var gaps = analyzer.findGaps(periods);

        assertThat(gaps).containsExactly(
                new CoveragePeriod(of(2024, 8, 1), of(2024, 8, 31))
        );
    }
}
