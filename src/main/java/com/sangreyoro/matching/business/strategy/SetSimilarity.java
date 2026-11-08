package com.sangreyoro.matching.business.strategy;

import java.util.Set;
import java.util.TreeSet;

/** Small helper shared by the set-based strategies: Jaccard similarity |A∩B| / |A∪B|. */
final class SetSimilarity {
    private SetSimilarity() {
    }

    static double jaccard(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) return 0;
        Set<String> union = new TreeSet<>(a);
        union.addAll(b);
        return (double) intersection(a, b).size() / union.size();
    }

    static Set<String> intersection(Set<String> a, Set<String> b) {
        Set<String> out = new TreeSet<>(a);
        out.retainAll(b);
        return out;
    }
}
