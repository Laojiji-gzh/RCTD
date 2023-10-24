package utils;

import java.util.HashSet;
import java.util.Set;

public class RBO {
    public double rbo(int[] arr1, int[]  arr2, int step , double p){
        double s = 0;Set<Integer> set = new HashSet<Integer>();
        for (int i=0;i<arr1.length;i+=step){
            double o = 0;int j = 0;
            for (j=0;j<step;++j){
                if (i+j>=arr1.length) break;
                set.add(arr1[i+j]);
            } j--;
            for (int k=0;k<=i+j;++k) if (k<arr2.length && set.contains(arr2[k])) o++;

            s+= Math.pow(p, i) * o /(i+1);
        }return s*(1-p);
    }
}
