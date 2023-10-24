# RDTD Algorithm and Datasets of Our Paper

✅ Author: GongZhihai

✅ Concat: 222270029@hdu.edu.cn

✅ Please cite this work if the code is used, thanks

### Datasets

⭐ This file contains 4 datasets, **DOG**, **NLP**, **Weather Sentiment(WS)**, and **Sentiment Popularity(SP)**.

We organized these datasets in our work and the raw datasets from: https://dbgroup.cs.tsinghua.edu.cn/ligl/crowddata/

Each dataset's property is given as follows:

| Datasets                | **NLP** | WS   | DOG  | SP   |
| ----------------------- | ------- | ---- | ---- | ---- |
| Task Count              | 1000    | 300  | 807  | 500  |
| Worker Count            | 85      | 109  | 109  | 142  |
| Workers Number per Task | 20      | 20   | 10   | 20   |
| Label Size              | 2       | 5    | 4    | 2    |

⭐ The raw datasets all contain the following files:

1. "truth.csv": The first line is the property of Label Size, Workers Number per Task, Task Count, and Worker Count. The second line is the tag of the question and the ground truth of the question. The third to the end is the data, which is shown in the following:

```
2,20,500,142
question,truth
0,1
...
```

2. "answer.csv": The first line is the tag of the question, the worker who finished the task, and the answer provided by the worker. The second to the end is the data. (No Sybil workers here) Shown as follows:

```
question,worker,answer
0,0,0
0,1,1
0,2,1
...
```

3. "sybil.csv": This file originated from "answer.csv", which contains Sybil workers.

👉 The Sybil workers' properties are shown as follows:

| Properties                                                   | Value ranging |
| ------------------------------------------------------------ | ------------- |
| $\mu$ (The proportion of Sybil workers)                      | 0 - 0.6       |
| $\lambda$ (Attacker number)                                  | 1, 2, 3, 4, 5 |
| $\epsilon$ (The probability of Sybil workers act independently) | 0 - 0.5       |
| $\theta$ (The average accuracy of Sybil workers, according to label size) | 0.15 - 0.45   |

### Compete Algorithms

Other voting-based algorithm we conclude in this code, including the following:

⭐ **CRH**, Reference: "Resolving Conflicts in Heterogeneous Data by Truth Discovery and Source Reliability Estimation".

⭐ **MV**, Majority Voting.

⭐ **CATD**, Reference: "A Confidence-Aware Approach for Truth Discovery on Long-Tail Data"

# Running

1.  **Compile**

⚒️ You may not need to do this step, it was already compiled.

⚒️ We use a lib of Apache: commons-math3-3.6.1.jar, which needs to specify the path, the command is shown as follows:

```
javac -encoding utf-8 -d bin -cp .\lib\commons-math3-3.6.1.jar .\src\*.java
```

The ".class" files are saved in the directory of "bin".



2. **Running**

We use `;` to separate the path in **Windows**， and `:` to separate the path in **Linux**.

✅ **Windows OS**

```
java -cp .\bin;.\lib\commons-math3-3.6.1.jar Run RDTD NLP 50 0.1 0.1 1 0.25 0.92 2
```

✅ **Linux OS**:

```
java -cp .\bin:.\lib\commons-math3-3.6.1.jar Run RDTD NLP 50 0.1 0.1 1 0.25 0.92 2
```



3. **Parameter Setting**

🚩 9 params need to be set in the command, as shown follows:

| Param  | Function                                                     | Optional                   | Example |
| ------ | ------------------------------------------------------------ | -------------------------- | ------- |
| arg[0] | specific the algorithm                                       | RDTD, CRH, MV, CATD        | RDTD    |
| arg[1] | Dataset                                                      | NLP, WS, SP, DOG           | WS      |
| arg[2] | Number of Runs                                               | All integer number was OK, | 50      |
| arg[3] | $\mu$ proportion of Sybil worker                             | 0 - 0.6                    | 0.4     |
| arg[4] | $\epsilon$ - The probability of Sybil workers act independently | 0 - 0.5                    | 0.1     |
| arg[5] | $\lambda$ - Attacker counts                                  | 1,2,3,4,5                  | 1       |
| arg[6] | $\theta$ - The average accuracy of Sybil workers, according to label size | 0.15 - 0.45                | 0.25    |
| arg[7] | p - Refer to the paper                                       | 0.8 - 1                    | 0.92    |
| arg[8] | step - Refer to the paper                                    | 1,2,3,4,5                  | 2       |

