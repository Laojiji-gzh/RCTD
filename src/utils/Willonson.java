package utils;

import org.apache.commons.math3.distribution.NormalDistribution;

public class Willonson {


    public int positiveCount;
    public int totalCount;
    public double confidenceLevel;


    /**
     *
     * @param positiveCount true number
     * @param totalCount
     * @param confidenceLevel
     */
    public Willonson(int positiveCount, int totalCount, double confidenceLevel) {
        this.positiveCount = positiveCount;
        this.totalCount = totalCount;
        this.confidenceLevel = confidenceLevel;
    }

    public double calculateLowerBound() {
        double z = calculateZ();
        double p = (double) positiveCount / totalCount;

        return (p + z * z / (2 * totalCount) - z * Math.sqrt((p * (1 - p) + z * z / (4 * totalCount)) / totalCount))
                / (1 + z * z / totalCount);
    }

    // 根据置信水平计算z值
    public double calculateZ() {
        double z = new Z_value().calculate();   // if confidence level is not reasonable, use the default value of 0.7
        if (confidenceLevel<1 && confidenceLevel >0 ){
            z = new Z_value(confidenceLevel).calculate();
        }
        return z;
    }
}


class Z_value {
    private double alpha = 0.7; // the init value

    public Z_value() {

    }

    public Z_value(double alpha) {
        this.alpha = alpha;
    }

    public double calculate(){
        NormalDistribution standardNormal = new NormalDistribution();
        double z = standardNormal.inverseCumulativeProbability(1 - alpha);
        return z;
    }

}
