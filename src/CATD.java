import entity.Task;
import entity.Worker;
import utils.Chi;

import java.util.*;

public class CATD {


    public Map<Integer, Task> taskMap = new HashMap<>();
    public Map<Integer, Worker> workerMap = new HashMap<>();

    public CATD(Map<Integer, Task> taskMap, Map<Integer, Worker> workerMap) {
        this.taskMap = taskMap;
        this.workerMap = workerMap;
    }

    public double calculate_accuracy() {

        List<Integer> taskPool = new ArrayList<>(taskMap.keySet());
        Collections.shuffle(taskPool);

        while (taskPool.size() != 0) {
            List<Integer> batchTask = new ArrayList<>();    // 10 task in a batch

            // randomly select 10 task, that's totally equal to the Batch Processing
            for (int i = 0; i < 10 && i < taskPool.size(); i++) {
                int taskId = taskPool.get(i);
                batchTask.add(taskId);
                taskPool.remove((Integer) taskId);
            }
            batchProcess(batchTask);
        }

        // for accuracy
        double acc = 0;
        for (Task task : taskMap.values()) {
            acc += (task.getAggregateLabel() == task.getTrueLabel() ? 1 : 0);
        }
        acc = acc / taskMap.size();
        return acc;
    }


    public void batchProcess(List<Integer> batchTask) {

        int iteration = 0;

        // construct the batchWorker, from the batchTask
        Set<Integer> temp = new HashSet<>();
        for (int taskId : batchTask) {
            Task task = taskMap.get(taskId);
            temp.addAll(task.assigned_worker_and_answer.keySet());
        }
        List<Integer> batchWorker = new ArrayList<>(temp);


        // start Truth Discovery
        boolean isEnd = false;

        // the iteration may not enough in other dataset!!
        while (++iteration < 20 && !isEnd) {

            // init workers' weight, if weight == -1
            for (int workerId : batchWorker) {
                Worker worker = workerMap.get(workerId);
                if (worker.getWeight() == -1) {
                    worker.setWeight(1.0 / (double) (workerMap.size()));
                }
            }

            isEnd = update_label(batchWorker, batchTask);

            update_weight(batchWorker, batchTask);

        }

    }

    /**
     * update workers' weights by the CRH algorithm
     *
     * @param batchWorker all the workers associated with the current batch
     * @param batchTask   N tasks associated with the current batch
     */
    public void update_weight(List<Integer> batchWorker, List<Integer> batchTask) {


        for (int workerId : batchWorker) {


            double indicator = 0;
            Worker worker = workerMap.get(workerId);

            // store all the task the worker finished
            List<Integer> taskIdArr = new ArrayList<>(worker.finished_task_and_answer.keySet());

            // The task worker finished in this batch
            int worker_finished_task_batch = 0;

            // all the task in this batch
            for (int taskId : batchTask) {
                int aggregateLabel = taskMap.get(taskId).getAggregateLabel();
                if (taskIdArr.contains(taskId)) {
                    indicator += Math.pow(aggregateLabel - worker.finished_task_and_answer.get(taskId), 2);
                    worker_finished_task_batch++;
                }
            }
            worker.setIndicator(indicator);

            double upper_bound = new Chi().get_chi_square_upper_bound(0.95, worker_finished_task_batch);

            worker.setWeight(upper_bound/worker.getIndicator());


        }

    }

    /**
     * update tasks' labels by the CRH algorithm
     *
     * @param batchWorker all the workers associated with the current batch
     * @param batchTask   N tasks associated with the current batch
     */
    public boolean update_label(List<Integer> batchWorker, List<Integer> batchTask) {

        int difference = 0;

        for (int taskId : batchTask) {
            double[] labels = {0, 0, 0, 0};
            Task task = taskMap.get(taskId);
            for (int aggregateLabel = 0; aggregateLabel < labels.length; aggregateLabel++) {
                for (int workerId : task.assigned_worker_and_answer.keySet()) {
                    Worker worker = workerMap.get(workerId);
                    labels[aggregateLabel] += (aggregateLabel == worker.finished_task_and_answer.get(taskId) ? 1 : 0) * worker.getWeight();
                }
            }
            double aggregatedLabel = -1;
            double max_label = 0;
            for (int i = 0; i < labels.length; i++) {
                if (labels[i] > max_label) {
                    max_label = labels[i];
                    aggregatedLabel = i;
                }
            }

            if (aggregatedLabel != task.getAggregateLabel()) {
                difference++;
            }
            task.setAggregateLabel((int) (aggregatedLabel));
        }
        return difference == 0;


    }


    public static void start(double mu, double epsilon, int lambda, double sybil_accuracy, int iteration, String dataset) {

        DataParameter.datasetName = dataset;
        double total_acc = 0;
        for (int iter = 0; iter < iteration; iter++) {

            // init the parameter, put the initialization in the for-loop to ensure each iteration is totally new
            Initialize initialize = new Initialize(mu, epsilon, lambda, sybil_accuracy);
            Task_and_Worker task_and_worker = initialize.init();
            CATD weightedVoting = new CATD(task_and_worker.taskMap, task_and_worker.workerMap);
            // record the time
            long startTime = System.nanoTime();
            double accuracy = weightedVoting.calculate_accuracy();
            long endTime = System.nanoTime();
            double time = (endTime - startTime) * 1e-6;


            // sout the result
            String acc = String.format("%.3f", accuracy * 100);
            System.out.println("RUN" + (iter + 1) + ":\t" + acc + "%" + "\t\tTime:" + String.format("%.2f", time) + "ms");
            total_acc += accuracy;


        }
        System.out.println("AVERAGE ACC:" + String.format("%.3f", total_acc / iteration * 100) + "%");
    }
}
