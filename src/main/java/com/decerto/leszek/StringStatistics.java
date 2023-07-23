package com.decerto.leszek;

import java.util.List;

public class StringStatistics {

    private List<String> strings;

    public StringStatistics(List<String> strings) {
        this.strings = strings;
    }

    public String getShortest() {
        String shortest = strings.get(0);
        for (int i=0; i<strings.size() - 1; i++) {
            if (strings.get(i).length() < shortest.length()) {
                shortest = strings.get(i);
            }
        }
        return shortest;
    }

}