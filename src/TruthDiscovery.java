import entity.Task;
import entity.Worker;
import utils.RBO;

import java.util.*;

public class TruthDiscovery {
    private Map<Integer, Worker> batchWorkerMap = null;
    private Map<Integer, Task> batchTaskMap = null;

    private int[] approval_rate;        // store workers' id, in the order of approval rate
    private int[] weight;               // also store workers' id, in the order of weight

    private int worker_num = 0;         // the length of Array

    private double p;
    private int step;

    /**
     * @param batchWorkerMap
     * @param batchTaskMap
     */
    public TruthDiscovery(Map<Integer, Worker> batchWorkerMap, Map<Integer, Task> batchTaskMap) {

        this.worker_num = batchWorkerMap.size();
        this.batchTaskMap = batchTaskMap;
        this.batchWorkerMap = batchWorkerMap;

        // rank worker by the approval rate, WARNING!!! >>> descending, e.g. w1>w2>w3>...>wj, a1>a2>a3>...>aj
        Comparator<Worker> comparator_approval = Comparator.comparing(Worker::getApproval_rate,Comparator.reverseOrder());
        Comparator<Worker> comparator_weight = Comparator.comparing(Worker::getWeight,Comparator.reverseOrder());

        // init worker pool with the worker set
        List<Worker> workerList = new ArrayList<>(batchWorkerMap.values());

        // init approval_rate
        workerList.sort(comparator_approval);
        approval_rate = new int[worker_num];        // rank the order according to the approval rate
        for (int j = 0; j < workerList.size(); j++) {
            approval_rate[j] = workerList.get(j).getWorkerId(); // make the approval rate to an array, also descending
        }

        // init weight, and calculate CRH part
        workerList.sort(comparator_weight);
        weight = new int[worker_num];       // rank the order according to the weight
        for (int j = 0; j < workerList.size(); j++) {
            weight[j] = workerList.get(j).getWorkerId();    // make the weight to an array, also descending
            batchWorkerMap.get(weight[j]).setRank_in_TD(j + 1);    // Init workers' rank according to their weight
        }

    }

    /**
     * We need approval_rate-ranking and weight-ranking to calculate two part:
     * 1. CRH part
     * 2. similarity part
     *
     * @param approval_rate approval-ranking, descending!!, each element is workers' id
     * @param weight        weight-ranking, descending!!, each element is workers' id
     * @return obj_function
     */
    public double get_object_function(int[] approval_rate, int[] weight) {
        double crh_value = 0;       // the value of CRH part, equal to the Sum of weight * Indicator function
        for (int j = 0; j < weight.length; j++) {
            Worker worker = batchWorkerMap.get(weight[j]);
            crh_value += worker.getWeight() * worker.getIndicator();
        }
        double similarity = new RBO().rbo(approval_rate, weight, 2, 0.92);
        return crh_value + 1 * similarity;
    }

    /**
     * get the position K of each worker
     */
    public void getK() {

        for (int j = 1; j < this.weight.length - 1; j++) {

            double init_obj_function = get_object_function(approval_rate, weight);

            int[] weight_temp = Arrays.copyOf(this.weight, worker_num);

            // swap weight[j] and weight[j-1]
            int temp = weight_temp[j];
            weight_temp[j] = weight_temp[j - 1];
            weight_temp[j - 1] = temp;
            double forward_obj_function = get_object_function(approval_rate, weight_temp);

            // swap weight[j] and weight[j+1]
            temp = weight_temp[j];
            weight_temp[j] = weight_temp[j + 1];
            weight_temp[j + 1] = temp;
            double below_obj_function = get_object_function(approval_rate, weight_temp);

            for (int i = 0; i < approval_rate.length; i++) {
                Worker worker = batchWorkerMap.get(approval_rate[i]);
                worker.setRank_in_TD(i + 1);
            }

            // Adjust the order of workers, because of the noisy and reputation inflation
            if (forward_obj_function >= init_obj_function && below_obj_function <= init_obj_function) {

                batchWorkerMap.get(weight[j]).setRank_in_TD(j - 1);
                batchWorkerMap.get(weight[j - 1]).setRank_in_TD(j);

                temp = weight[j];
                weight[j] = weight[j - 1];
                weight[j - 1] = temp;
            }

            if (forward_obj_function <= init_obj_function && below_obj_function >= init_obj_function) {

                batchWorkerMap.get(weight[j]).setRank_in_TD(j + 1);
                batchWorkerMap.get(weight[j + 1]).setRank_in_TD(j);

                temp = weight[j];
                weight[j] = weight[j + 1];
                weight[j + 1] = temp;
            }
        }
    }

    /**
     * get the weight of each worker
     */
    public void getW() {

        Comparator<Worker> rankNumberComparing = Comparator.comparing(Worker::getRank_in_TD, Comparator.reverseOrder());   // according to our paper, we need to let workers' weight ranked descending
        List<Worker> workerList = new ArrayList<>(batchWorkerMap.values());
        workerList.sort(rankNumberComparing);
        int[] new_weight = new int[worker_num];       // rank workers' id by their weight, NOTE!! descending
        for (int j = 0; j < workerList.size(); j++) {
            new_weight[j] = workerList.get(j).getWorkerId();
        }


        // The new_weight is a ranking used as a Restriction

        double[] indicator_array = new double[worker_num];  // store each worker's indicator function value
        for (int j = 0; j < new_weight.length; j++) {
            indicator_array[j] = batchWorkerMap.get(new_weight[j]).getIndicator();
        }

        double[] B_array = new double[worker_num];  // to simplify the expression, use B instead sum of indicator


        for (int j = indicator_array.length - 1; j >= 0; j--) {

            B_array[j] = B_array[(j + 1) % (worker_num - 1)] + indicator_array[j];    //  B array, according to paper
        }

        double[] epsilon_array = new double[worker_num];
        double sum_of_B = 0;
        for (int j = 0; j < B_array.length; j++) {
            sum_of_B += B_array[j];
        }
        for (int j = 0; j < epsilon_array.length; j++) {
            epsilon_array[j] = -Math.log(B_array[j] / sum_of_B);        // epsilon array, according to paper
        }

        double[] weight_temp = new double[worker_num];
        for (int j = 0; j < weight_temp.length; j++) {
            weight_temp[j] = epsilon_array[j] + weight_temp[(worker_num - 1 + j - 1) % (worker_num - 1)];
        }

        // update workers' weight
        for (int j = 0; j < new_weight.length; j++) {
            batchWorkerMap.get(new_weight[j]).setWeight(weight_temp[j]);
        }
    }


    public void run() {
        getK();
        getW();
    }


}
