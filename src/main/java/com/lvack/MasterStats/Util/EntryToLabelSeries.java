package com.lvack.MasterStats.Util;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * EntryToLabelSeriesClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

/**
 * Utility class implementing collector interface to generate two comma separated
 * lists from a stream of map entries
 * the key and/or value elements can be surrounded by quotes
 * used to generate data for chartist charts
 * objects are converted to strings using the toString() method
 * @param <K> Type of keys
 * @param <V> Type of values
 */
public class EntryToLabelSeries<K, V> implements Collector<Map.Entry<K, V>, Pair<StringJoiner, StringJoiner>, Pair<String, String>> {
    private boolean keyQuotes;
    private boolean valueQuotes;

    public EntryToLabelSeries(boolean keyQuotes, boolean valueQuotes) {
        this.keyQuotes = keyQuotes;
        this.valueQuotes = valueQuotes;
    }

    public EntryToLabelSeries() {
        this.keyQuotes = false;
        this.valueQuotes = false;
    }

    @Override
    public Supplier<Pair<StringJoiner, StringJoiner>> supplier() {
        return () -> new Pair<>(new StringJoiner(", "), new StringJoiner(", "));
    }

    @Override
    public BiConsumer<Pair<StringJoiner, StringJoiner>, Map.Entry<K, V>> accumulator() {
        return (p, e) -> {
            p.getKey().add(keyQuotes ? String.format("'%s'",
                    StringEscapeUtils.escapeEcmaScript(e.getKey().toString())) : e.getKey().toString());
            p.getValue().add(valueQuotes ? String.format("'%s'",
                    StringEscapeUtils.escapeEcmaScript(e.getValue().toString())) : e.getValue().toString());
        };
    }

    @Override
    public BinaryOperator<Pair<StringJoiner, StringJoiner>> combiner() {
        return (p1, p2) -> new Pair<>(p1.getKey().merge(p2.getKey()),
                p1.getValue().merge(p2.getValue()));
    }

    @Override
    public Function<Pair<StringJoiner, StringJoiner>, Pair<String, String>> finisher() {
        return (p) -> new Pair<>(p.getKey().toString(), p.getValue().toString());
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.unmodifiableSet(Collections.emptySet());
    }
}
