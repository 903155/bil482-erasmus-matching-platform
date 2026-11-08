package com.sangreyoro.matching.business.strategy;

import com.sangreyoro.matching.domain.MatchCriterion;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * STRATEGY pattern – selection of the ConcreteStrategy for the criterion chosen by the user (UC2-S3).
 * <p>
 * The registry maps each {@link MatchCriterion} to its strategy. It replaces a {@code switch(criterion)}
 * in the service: the service asks the registry and never names a concrete strategy class.
 * New strategies are added with {@link #register} (see {@code MatchingApplication} and test UC2-T09).
 */
public class MatchingStrategyRegistry {
    private final Map<MatchCriterion, MatchingStrategy> strategies = new EnumMap<>(MatchCriterion.class);

    /** Registry with the three criteria of FR-2.1. */
    public static MatchingStrategyRegistry withDefaults() {
        return new MatchingStrategyRegistry()
                .register(new LanguageExchangeStrategy())
                .register(new HobbyMatchingStrategy())
                .register(new SportsMatchingStrategy());
    }

    public MatchingStrategyRegistry register(MatchingStrategy strategy) {
        strategies.put(strategy.criterion(), strategy);
        return this;
    }

    public MatchingStrategy get(MatchCriterion criterion) {
        MatchingStrategy s = strategies.get(criterion);
        if (s == null) throw new IllegalArgumentException("No matching strategy registered for " + criterion);
        return s;
    }

    public Set<MatchCriterion> criteria() {
        return Collections.unmodifiableSet(strategies.keySet());
    }
}
