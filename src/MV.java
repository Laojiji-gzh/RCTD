import entity.Task;
import entity.Worker;

import java.util.Map;

public class MV {


    public Map<Integer, Task> taskMap;
    public Map<Integer, Worker> workerMap;

    public MV(Map<Integer, Task> taskMap, Map<Integer, Worker> workerMap) {
        this.taskMap = taskMap;
        this.workerMap = workerMap;
    }

    public double calculate_accuracy(){
        double total_true = 0;
        for (Task task : taskMap.values()) {
            int[] labels = {0,0,0,0};
            Map<Integer, Integer> worker_and_answer = task.assigned_worker_and_answer;
            for (Integer workerId : worker_and_answer.keySet()) {
                labels[worker_and_answer.get(workerId)]++;
            }

            int max_cnt = 0;
            int aggregate_label = 0;
            for (int i = 0; i < labels.length; i++) {
                if (labels[i] > max_cnt){
                    max_cnt = labels[i];
                    aggregate_label = i;
                }
            }

            task.setAggregateLabel(aggregate_label);
            if (task.getAggregateLabel() == task.getTrueLabel()){
                total_true++;
            }
        }

        return total_true / taskMap.size();
    }


    public static void start(double mu, double epsilon, int lambda, double sybil_accuracy, String dataset){
        DataParameter.datasetName = dataset;

        Initialize initialize = new Initialize(mu,epsilon,lambda,sybil_accuracy);
        Task_and_Worker task_and_worker = initialize.init();

        MV voting = new MV(task_and_worker.taskMap, task_and_worker.workerMap);

        long startTime = System.nanoTime();
        double accuracy = voting.calculate_accuracy();
        long endTime = System.nanoTime();

        double time = (endTime - startTime) * 1e-6;


        System.out.println("It is no use to run the VOTING for much times, because it get an fixed accuracy forever!");
        String acc = String.format("%.3f", accuracy * 100);
        System.out.println("Accuracy:" + acc + "%" + "\t\tTime:" + String.format("%.2f",time) + "ms");

    }

}
