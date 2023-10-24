package utils;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;

public class Chi {

    public double get_chi_square_upper_bound(double confidenceLevel, int degreesOfFreedom) {

        ChiSquaredDistribution chiSquareDistribution = new ChiSquaredDistribution(degreesOfFreedom);
        // upperBound
        double lowerConfidenceLimit = chiSquareDistribution.inverseCumulativeProbability((1 - confidenceLevel) / 2);

        double upperConfidenceLimit = chiSquareDistribution.inverseCumulativeProbability(1 - (1 - confidenceLevel) / 2);

        return lowerConfidenceLimit;

    }

}
