package util;

import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class VectorFeatures {

    private String name;
    private DescriptiveStatistics stats;

    public VectorFeatures(String name, ArrayList<Double> vector) {
        this.name = name;
        this.stats = new DescriptiveStatistics();

        for (Double d: vector) {
            this.stats.addValue(d);
        }
    }

    public String getName() {
        return name;
    }

    public Comparable getMin() {
        return stats.getMin();
    }

    public Comparable getMax() {
        return stats.getMax();
    }

    public Comparable getMean() {
        return stats.getMean();
    }

    public Comparable getSD() {
        return stats.getStandardDeviation();
    }

    public String getHeaders() {
        return String.format("%s-%s,%s-%s,%s-%s,%s-%s",
                name, "min",
                name, "max",
                name, "avg",
                name, "std");
    }

    public String getStats() {
        return String.format("%f,%f,%f,%f", getMin(), getMax(), getMean(), getSD());
    }

}
