package utils;

public class Variance {


    public static double get_std_error(double[] array) {
        double mean = calculateMean(array);
        double sumOfSquaredDifferences = 0;
        for (double num : array) {
            double difference = num - mean;
            sumOfSquaredDifferences += Math.pow(difference, 2);
        }
        return Math.sqrt(sumOfSquaredDifferences / array.length);
    }

    public static double calculateMean(double[] array) {
        double sum = 0;
        for (double num : array) {
            sum += num;
        }
        return sum / (double) array.length;
    }
}