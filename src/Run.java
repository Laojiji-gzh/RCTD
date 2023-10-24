
public class Run {

    public static void main(String[] args) {
        String algorithmName = args[0];
        String dataset = args[1];
        int iteration = Integer.parseInt(args[2]);


        double mu = Double.parseDouble(args[3]);
        double epsilon = Double.parseDouble(args[4]);
        int lambda = Integer.parseInt(args[5]);
        double sybil_accuracy = Double.parseDouble(args[6]);

        double p = Double.parseDouble(args[7]);
        int step = Integer.parseInt(args[8]);




        /**
         * Our Algorithm RDTD, "Reputation-Driven Truth Discovery"
         *
         * mu:0-0.6
         * epsilon:0-0.5
         * lambda:1,2,3,4,5
         * sybil_accuracy:0.15-0.45
         * p:0.8-1
         * step:1,2,3,4,5
         *
         * dataset(4): DOG, NLP, WS, SP
         */
        if (algorithmName.equals("RDTD")){
            RDTD.start(mu,epsilon,lambda,sybil_accuracy,p,step,iteration,dataset);
        }

        /**
         * The CRH Algorithm, call by the following statement
         * Reference: "Resolving Conflicts in Heterogeneous Data by Truth Discovery and Source Reliability Estimation"
         */
        else if (algorithmName.equals("CRH")){
            CRH.start(mu,epsilon,lambda,sybil_accuracy,iteration,dataset);
        }

        /**
         * MV, Majority Voting, call by the following statement
         */
        else if (algorithmName.equals("MV")){
            MV.start(mu, epsilon, lambda, sybil_accuracy, dataset);
        }

        /**
         * CATD algorithm, resolve the long tail phenomenon of crowdsourcing
         * Reference: "A Confidence-Aware Approach for Truth Discovery on Long-Tail Data"
         */
        else if (algorithmName.equals("CATD")){
            CATD.start(mu,epsilon,lambda,sybil_accuracy,iteration,dataset);
        }

        else {
            System.out.println("invalid param");
        }




    }
}
