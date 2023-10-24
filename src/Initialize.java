import entity.Task;
import entity.Worker;
import utils.Willonson;

import java.io.*;
import java.util.*;

public class Initialize {
    public Map<Integer, Task> taskMap = new HashMap<>();
    public Map<Integer, Worker> workerMap = new HashMap<>();

    private double p;
    private double step;
    private double mu;
    private double epsilon;
    private int lambda;
    private double sybil_accuracy;

    private String path = System.getProperty("user.dir");


    public Initialize(double mu, double epsilon, int lambda, double sybil_accuracy) {
        this.mu = mu;
        this.epsilon = epsilon;
        this.lambda = lambda;
        this.sybil_accuracy = sybil_accuracy;
    }

    public Initialize(double mu, double epsilon, int lambda, double sybil_accuracy, double p, double step) {
        this.p = p;
        this.step = step;
        this.mu = mu;
        this.epsilon = epsilon;
        this.lambda = lambda;
        this.sybil_accuracy = sybil_accuracy;
    }

    public Task_and_Worker init() {

        // readIn the answer.csv and truth.csv
        String rawFileName = "answer.csv";
        readFile(rawFileName);
        // we read the file twice to insert the Sybil workers
        String sybilFileName = sybil_insert(mu, epsilon, lambda, sybil_accuracy);
        readFile(sybilFileName);


        set_worker_accuracy();
        set_worker_approval();
        return new Task_and_Worker(taskMap, workerMap);

    }

    /**
     * init all the workers' accuracy
     */
    public void set_worker_accuracy() {
        for (Worker worker : workerMap.values()) {
            worker.setTaskCount(worker.finished_task_and_answer.size());
            double accuracy = 0;
            for (int taskId : worker.finished_task_and_answer.keySet()) {
                accuracy += (taskMap.get(taskId).getTrueLabel() == worker.finished_task_and_answer.get(taskId) ? 1 : 0);
            }
            worker.setAccuracy(accuracy / worker.getTaskCount());
        }
    }

    /**
     * init all the workers' approval-rate
     */
    public void set_worker_approval() {
        Random random = new Random();
        double miu = 0.1;
        double xigma = 0.05;

        double aveAccuracy = 0;
        for (Worker worker : workerMap.values()) {
            aveAccuracy += worker.getAccuracy();
        }
        aveAccuracy /= workerMap.size();


        // we add the Gauss Noisy, and introduce the Wilson lower bound
        for (Worker worker : workerMap.values()) {
            double accuracy = new Willonson((int) (worker.getAccuracy() * worker.getTaskCount()), worker.getTaskCount(), 0.7).calculateLowerBound();
            double noisyValue = xigma * random.nextGaussian() + 0.2;
            double noisySybil = xigma * random.nextGaussian() + 0.1;

            // independent workers
            if (worker.isSybil() == 0) {
                int trueAns = (int) (worker.getTaskCount() * worker.getAccuracy());
                double acc_wilson = new Willonson(trueAns, worker.getTaskCount(), 0.8).calculateLowerBound();
                double acceptanceCommonWorker = acc_wilson + noisyValue >= 0.99 ? 0.99 : worker.getAccuracy() + noisyValue;
                worker.setApproval_rate(acceptanceCommonWorker);
            }
            // sybil workers
            else {
                int trueAns = (int) (worker.getTaskCount() * aveAccuracy);
                double acc_wilson = new Willonson(trueAns, worker.getTaskCount(), 0.8).calculateLowerBound();
                double acceptanceSybilWorker = acc_wilson + noisySybil >= 0.99 ? 0.99 : aveAccuracy + noisySybil;
                worker.setApproval_rate(acceptanceSybilWorker);
            }
        }
    }

    /**
     * @param mu sybil proportion
     * @param epsilon Sybil worker concluding with a probability of 1-epsilon
     * @param lambda the number of attackers, each attack control several Sybil worker
     * @param sybil_accuracy Sybil workers' accuracy
     * @return workers' answer FILE
     */
    public String sybil_insert(double mu, double epsilon, int lambda, double sybil_accuracy) {

        // set Parameter : mu
        List<Integer> sybil_arr = new ArrayList<>();
        // some dataset number the worker and task from zero >>>> 0,1,2..., and other from one >>>> 1,2,3....
        for (int i = 1; i < DataParameter.workerNum-1; i++) {
            sybil_arr.add(i + 1);
        }
        Collections.shuffle(sybil_arr);
        sybil_arr = sybil_arr.subList(0, (int) (mu * (DataParameter.workerNum - 1)));

        for (Integer workerId : sybil_arr) {
            Worker worker = workerMap.get(workerId);
            worker.setSybil(1);
            worker.setAttack(new Random().nextInt(lambda));     // each worker controlled by one attacker
        }


        // replacing independent workers with Sybil workers
        List<String> read_in_buffer = new ArrayList<>();
        try {
            BufferedReader r1 = new BufferedReader(new FileReader( path+"/data/" + DataParameter.datasetName +"/answer.csv"));
            String line = r1.readLine();
            read_in_buffer.add(line);
            for (int i = 0; i < DataParameter.taskNum * DataParameter.taskForWorkerNum; i += DataParameter.taskForWorkerNum) {


                List<Integer> attacker_ans = null;
                for (int j = i; j < DataParameter.taskForWorkerNum + i && j < DataParameter.taskNum * DataParameter.taskForWorkerNum; j++) {

                    // These 10 task are the same
                    line = r1.readLine();
                    String[] s = line.split(",");
                    int workerId = Integer.parseInt(s[1]);
                    Worker worker = workerMap.get(workerId);
                    int trueLabel = taskMap.get(Integer.parseInt(s[0])).getTrueLabel();

                    // attacker_ans is an Array, stored each attackers' shared answer
                    if ( i == j ){
                        attacker_ans = set_lambda_and_sybilAcc(trueLabel);      // set two Parameter: lambda and sybil_accuracy
                    }

                    // An independent worker
                    if (worker.getAttack() == -1){
                        read_in_buffer.add(line);       // write into file directly
                    }

                    // A Sybil worker
                    else{
                        int attackId = worker.getAttack();
                        int shared_label = attacker_ans.get(attackId);      // one attacker's shared label
                        int sybil_label = set_epsilon(shared_label);        // set Parameter: epsilon

                        String t = s[0] + "," + s[1] + "," + String.valueOf(sybil_label);   // write into file
                        read_in_buffer.add(t);
                    }

                }
            }

            r1.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            BufferedWriter w1 = new BufferedWriter(new FileWriter(path+"/data/" + DataParameter.datasetName + "/sybil.csv"));
            for (int i = 0; i < read_in_buffer.size(); i++) {
                w1.append(read_in_buffer.get(i));
                if (i != read_in_buffer.size() - 1) {
                    w1.append("\n");
                }
            }
            w1.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "sybil.csv";
    }

    public int set_epsilon(int shared_label){
        return (new Random().nextDouble() < 1 - epsilon) ? shared_label : new Random().nextInt(DataParameter.labelSize);
    }


    public List<Integer> set_lambda_and_sybilAcc(int true_label){
        // The length of error_ans is
        List<Integer> error_ans = new ArrayList<>();
        for (int i = 0; i < DataParameter.labelSize; i++) {
            if (i!=true_label){
                error_ans.add(i);
            }
        }

        // store each attacker's answer, this answer share by sybil controlled by the same attacker
        List<Integer> attacker_ans = new ArrayList<>();
        for (int l = 0; l < lambda; l++) {
            attacker_ans.add( new Random().nextDouble()<sybil_accuracy ? true_label:(error_ans.get(new Random().nextInt(DataParameter.labelSize-1))) );
        }

        return attacker_ans;
    }

    public void readFile(String fileName) {
        // construct taskMap
        try {
            BufferedReader w1 = new BufferedReader(new FileReader(path + "/data/" +DataParameter.datasetName + "/truth.csv"));
            String line = w1.readLine();
            String[] data_parameter = line.split(",");

            DataParameter.labelSize = Integer.parseInt(data_parameter[0]);
            DataParameter.taskForWorkerNum = Integer.parseInt(data_parameter[1]);
            DataParameter.taskNum = Integer.parseInt(data_parameter[2]);
            DataParameter.workerNum = Integer.parseInt(data_parameter[3]);

            line = w1.readLine();
            while ((line = w1.readLine()) != null) {
                String[] s = line.split(",");
                int taskId = Integer.parseInt(s[0]);
                int trueLabel = Integer.parseInt(s[1]);
                taskMap.put(taskId, new Task(taskId, trueLabel));
            }
            w1.close();
        } catch (Exception e) {
            System.out.println("read fail...");
            e.printStackTrace();
        }
        // construct workerMap
        try {
            BufferedReader w2 = new BufferedReader(new FileReader(path+"/data/"  + DataParameter.datasetName + "/" + fileName));
            String line = w2.readLine();
            while ((line = w2.readLine()) != null) {
                String[] s = line.split(",");
                int taskId = Integer.parseInt(s[0]);
                int workerId = Integer.parseInt(s[1]);
                int workerAns = Integer.parseInt(s[2]);

                taskMap.get(taskId).assigned_worker_and_answer.put(workerId, workerAns);

                if (workerMap.get(workerId) != null) {
                    Worker worker = workerMap.get(workerId);
                    worker.finished_task_and_answer.put(taskId, workerAns);
                } else {
                    Worker worker = new Worker(workerId);
                    worker.finished_task_and_answer.put(taskId, workerAns);
                    workerMap.put(workerId, worker);
                }
            }
            w2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}


class Task_and_Worker {
    // we use public to make the class more easy
    public Map<Integer, Task> taskMap;
    public Map<Integer, Worker> workerMap;

    public Task_and_Worker(Map<Integer, Task> taskMap, Map<Integer, Worker> workerMap) {
        this.taskMap = taskMap;
        this.workerMap = workerMap;
    }
}
