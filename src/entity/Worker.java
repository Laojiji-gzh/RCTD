package entity;

import java.util.HashMap;
import java.util.Map;

public class Worker {

    private int workerId = -1;
    private int taskCount = -1;
    private double weight = -1;
    private double indicator = -1;
    private double accuracy = -1;
    public int[] allLabel = new int[4];
    private int sybil = 0;              // default value is 0, represent not a sybil
    private double approval_rate = 0;
    private int rank_in_TD = -1;        // workers' order in the first part
    private int attack = -1;

    public Map<Integer, Integer> finished_task_and_answer = new HashMap<>();


    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getRank_in_TD() {
        return rank_in_TD;
    }

    public void setRank_in_TD(int rank_in_TD) {
        this.rank_in_TD = rank_in_TD;
    }

    public double getApproval_rate() {
        return approval_rate;
    }

    public void setApproval_rate(double approval_rate) {
        this.approval_rate = approval_rate;
    }

    public int isSybil() {
        return sybil;
    }

    public void setSybil(int sybil) {
        this.sybil = sybil;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getIndicator() {
        return indicator;
    }

    public void setIndicator(double indicator) {
        this.indicator = indicator;
    }

    public Worker(int workerId) {
        this.workerId = workerId;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public int getWorkerId() {
        return workerId;
    }

    public void setWorkerId(int workerId) {
        this.workerId = workerId;
    }


}
