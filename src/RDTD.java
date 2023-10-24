import entity.Task;
import entity.Worker;
import utils.Variance;

import java.util.*;

public class RDTD {

    public Map<Integer, Task> taskMap = new HashMap<>();
    public Map<Integer, Worker> workerMap = new HashMap<>();
    private double p;
    private int step;

    public RDTD(Map<Integer, Task> taskMap, Map<Integer, Worker> workerMap,double p, int step) {
        this.taskMap = taskMap;
        this.workerMap = workerMap;
        this.step = step;
        this.p = p;
    }


    /**
     *
     * @return
     */
    public double calculate_accuracy() {

        List<Integer> taskPool = new ArrayList<>(taskMap.keySet());
        Collections.shuffle(taskPool);

        while (taskPool.size() != 0) {
            List<Integer> batchTask = new ArrayList<>();    // 10 task in a batch
            for (int i = 0; i < DataParameter.taskForWorkerNum && i < taskPool.size(); i++) {
                int taskId = taskPool.get(i);
                batchTask.add(taskId);
                taskPool.remove((Integer) taskId);
            }

            // construct the batchWorker, from the batchTask
            Set<Integer> temp = new HashSet<>();
            for (int taskId : batchTask) {
                Task task = taskMap.get(taskId);
                temp.addAll(task.assigned_worker_and_answer.keySet());
            }
            List<Integer> batchWorker = new ArrayList<>(temp);
            batchProcess(batchTask, batchWorker);
        }

        // for accuracy
        double acc = 0;
        for (Task task : taskMap.values()) {
            acc += (task.getAggregateLabel() == task.getTrueLabel() ? 1 : 0);
        }
        acc = acc / taskMap.size();

        return acc;
    }

    public void batchProcess(List<Integer> batchTask, List<Integer> batchWorker) {

        int iteration = 0;
        boolean isEnd = false;

        // start Truth Discovery
        while (++iteration < 20 && !isEnd) {
            // init workers' weight, if weight == -1
            for (int workerId : batchWorker) {
                Worker worker = workerMap.get(workerId);
                if (worker.getWeight() == -1) {
                    worker.setWeight(1.0 / (double) batchWorker.size());    // initialization: 1/N
                }
            }

            isEnd = update_label(batchWorker, batchTask);
            update_weight(batchWorker, batchTask);

        }

    }

    public void update_weight(List<Integer> batchWorker, List<Integer> batchTask) {

        // set each workers' indicator function, the indicator function only related to the tasks in batchTask
        for (int workerId : batchWorker) {
            double indicator = 0;
            Worker worker = workerMap.get(workerId);

            // store all the task the worker finished
            List<Integer> taskIdArr = new ArrayList<>(worker.finished_task_and_answer.keySet());

            // all the task in THIS batch
            for (Integer taskId : batchTask) {
                int aggregateLabel = taskMap.get(taskId).getAggregateLabel();
                if (taskIdArr.contains(taskId)) {
                    int worker_ans = worker.finished_task_and_answer.get(taskId);
                    indicator += (aggregateLabel == worker_ans ? 1 : 0);
                }
            }
            worker.setIndicator(indicator);
        }

        // update the weight of workers
        double totalIndicator = 0;
        for (int workerId : batchWorker) {
            totalIndicator += workerMap.get(workerId).getIndicator();
        }
        for (int workerId : batchWorker) {
            Worker worker = workerMap.get(workerId);
            worker.setWeight(-Math.log((worker.getIndicator()) / totalIndicator));
        }

        // transform batchWorker and batchTask to the Map
        Map<Integer, Worker> batchWorkerMap = new HashMap<>();
        Map<Integer, Task> batchTaskMap = new HashMap<>();

        for (Integer workerId : batchWorker) {
            batchWorkerMap.put(workerId, workerMap.get(workerId));
        }
        for (Integer taskId : batchTask) {
            batchTaskMap.put(taskId, taskMap.get(taskId));
        }

        // update by RDTD
        TruthDiscovery td = new TruthDiscovery(batchWorkerMap, batchTaskMap);
        td.run();

    }

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

    public static void start(double mu, double epsilon, int lambda, double sybil_accuracy, double p, int step, int iteration, String dataset){

        DataParameter.datasetName = dataset;
        double total_acc = 0;
        double total_time = 0;
        double[] std_arr = new double[iteration];
        double max_acc = 0;
        double min_acc = 1;

        for (int iter = 0; iter < iteration; iter++) {

            Initialize initialize = new Initialize(mu, epsilon, lambda, sybil_accuracy, p, step);
            Task_and_Worker task_and_worker = initialize.init();

            RDTD rdtd = new RDTD(task_and_worker.taskMap, task_and_worker.workerMap,p,step);
            long startTime = System.nanoTime();
            double accuracy = rdtd.calculate_accuracy();
            long endTime = System.nanoTime();
            if (accuracy > max_acc){
                max_acc = accuracy;
            }
            if (accuracy < min_acc){
                min_acc = accuracy;
            }
            std_arr[iter] = accuracy;

            double time = (endTime - startTime) * 1e-6;

            String acc = String.format("%.3f", accuracy * 100); // convert to percentage
            System.out.println("RUN" + (iter + 1) + ":\t" + acc + "%" + "\t\tTime:" + String.format("%.2f", time) + "ms");
            total_acc += accuracy;
            total_time+= time;
        }


        System.out.println();
        System.out.println("Sybil-Parameter: " + "mu=" + mu + ", epsilon=" + epsilon + ", lambda=" + lambda + ", sybil_accuracy=" +sybil_accuracy);
        System.out.println("Average Accuracy: " + String.format("%.3f", total_acc / iteration * 100) + "%");
        System.out.println("Max Accuracy: " + String.format("%.3f",max_acc * 100) + "%");
        System.out.println("Min Accuracy: " + String.format("%.3f",min_acc * 100) + "%");
        System.out.println("Average Time: " + String.format("%.2f", total_time / iteration ) + "ms");

        double std = Variance.get_std_error(std_arr);
        System.out.println("Standard Error: " + String.format("%.4f",std));

    }


}
