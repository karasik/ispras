package models;

import auxiliary.Aux;
import auxiliary.ITweetReader;
import auxiliary.TweetJsonReader;
import auxiliary.TweetTextReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Author: mi
 * Date: 3/18/13
 * Time: 9:52 PM
 */
public class LDA {
    /**
     * M x N_m array
     * contains M tweets, each of N_m words (numbers from 0 to V)
     */
    private int[][] documents;
    /**
     * number of topics
     */
    private int K;
    /**
     * number of words in vocabulary
     */
    private int V;
    /**
     * number of documents
     */
    private int D;
    /**
     * dirichlet prior (in this case it is constant)
     */
    private double alpha, beta;
    /**
     * mapping from string words to integers (for optimization purposes)
     */
    private HashMap<String, Integer> wordToIntMap;
     /*
      * the reverse mapping to restore original words
      */
    private String[] intToWordMap;

    private int[][] documentTopicCount;
    private int[] documentTopicSum;
    private int[][] topicTermCount;
    private int[] topicTermSum;
    private int[] termTopicAssignment;

    /**
     * D x K array
     * theta[m][t] -- probability of the document m is assigned to topic t
     */
    private double[][] theta;
    /**
     * K x V array
     * phi[t][w] -- probability of appearing the word w in topic t
     */
    private double[][] phi;
    /**
     * the most probable assignment for documents to themes
     */
    private int[] themeAssignment;
    /**
     * raw tweet data desu
     */
    private String[] rawDocuments;

    public static LDA fromTweetFile(String filename, int k) throws Exception {
        TweetJsonReader in = new TweetJsonReader(filename);
        return new LDA(new TweetTextReader(filename), k);
    }

    public static LDA fromTextFile(String filename, int k, String[] filter) throws Exception  {
        TweetTextReader in = new TweetTextReader(filename);
        return new LDA(new TweetTextReader(filename), k);
    }


    /**
     * reads LDA from file
     *
     */
    private LDA(ITweetReader in, int k) throws Exception {
        int wordCount=0;
        wordToIntMap = new HashMap<String, Integer>();
        ArrayList<int[]> total = new ArrayList<int[]>();
        ArrayList<String> mapToStrings = new ArrayList<String>();
        while (in.hasMoreTweets()) {
            String[] words = in.nextTweet().split(" ");
            ArrayList<Integer> tmp = new ArrayList<Integer>();

            for (String word : words) {
                if (!wordToIntMap.containsKey(word)) {
                    wordToIntMap.put(word, wordCount++);
                    mapToStrings.add(word);
                }
                tmp.add(wordToIntMap.get(word));
            }
            int[] a = new int[tmp.size()];
            for (int i=0; i<a.length; i++) {
                a[i] = tmp.get(i);
            }
            total.add(a);
        }

        intToWordMap = Aux.stringListToArray(mapToStrings);

        documents = new int[total.size()][];
        for (int i=0; i<documents.length; i++) {
            documents[i] = total.get(i);
        }

        K = k;
        V = wordCount;
        D = documents.length;

        alpha = 50. / k;
        beta = 1e-2;

        documentTopicCount = new int[D][K];
        documentTopicSum = new int[D];
        topicTermCount = new int[K][V];
        topicTermSum = new int[K];
        termTopicAssignment = new int[V];

        rawDocuments = new String[D];
        for (int m=0; m<D; m++) {
            StringBuilder tmp = new StringBuilder();
            for (int word : documents[m]) {
                tmp.append(' ');
                tmp.append(intToWordMap[word]);
            }
            rawDocuments[m] = tmp.toString();
        }

//        System.out.println("Initialized LDA:\n" + " D=" + D + "");
    }

    /**
     * fill the arrays theta and phi
     */
    public void performInference() {
        int[][] documentTopicCount = new int[D][K];
        int[] documentTopicSum = new int[D];

        int[][] topicTermCount = new int[K][V];
        int[] topicTermSum = new int[K];

        int[] termTopicAssignment = new int[V];

        Random r = new Random();

        // initialization step desu
        for (int m=0; m<D; m++) {
            for (int n=0; n<documents[m].length; n++) {
                int topic = r.nextInt(K);

                documentTopicCount[m][topic]++;
                documentTopicSum[m]++;
                topicTermCount[topic][documents[m][n]]++;
                topicTermSum[topic]++;

                termTopicAssignment[documents[m][n]] = topic;
            }
        }


        // converging
        int L = 1000;
        int readOut = 100;
        int readOutCount = 0;

        double[][] expectedTheta = new double[D][K];
        double[][] expectedPhi = new double[K][V];

        while (L-->0) {
            for (int m=0; m<D; m++) {
                for (int n=0; n<documents[m].length; n++) {
                    int oldTopic = termTopicAssignment[documents[m][n]];

                    documentTopicCount[m][oldTopic]--;
                    documentTopicSum[m]--;
                    topicTermCount[oldTopic][documents[m][n]]--;
                    topicTermSum[oldTopic]--;

                    // sample new topic p(z_i | z_-i)
                    int newTopic = -1;
                    double maxValue = Double.MIN_VALUE;
                    double[] phi = readOutPhi(m, n), theta = readOutTheta(m);
                    for (int t=0; t<K; t++) {
                        double x = phi[t] * theta[t];
                        if (x > maxValue) {
                            newTopic = t;
                            maxValue = x;
                        }
                    }

                    termTopicAssignment[documents[n][m]] = newTopic;

                    documentTopicCount[m][newTopic]++;
                    documentTopicSum[m]++;
                    topicTermCount[newTopic][documents[m][n]]++;
                    topicTermSum[newTopic]++;
                }
            }

            if (L % readOut == 0) {
                // perform read out
                readOutCount++;
                for (int m=0; m<D; m++) {
                    double[] tmp = readOutTheta(m);
                    for (int t=0; t<K; t++) {
                        expectedTheta[m][t] += tmp[t];
                    }
                }
            }
        }

        // converged
        for (int m=0; m<D; m++) {
            for (int t=0; t<K; t++) {
                expectedTheta[m][t] /= readOutCount;
            }
        }
        for (int t=0; t<K; t++) {
            for (int w=0; w<V; w++) {
                expectedPhi[t][w] /= readOutCount;
            }
        }

        theta = expectedTheta;
        phi = expectedPhi;
    }

    public boolean inferencePerformed() {
        return theta != null && phi != null;
    }

    private double[] readOutPhi(int m, int n) {
        double[] phi = new double[K];
        for (int t=0; t<K; t++) {
            phi[t] = (topicTermCount[t][documents[m][n]] + beta) / (termTopicAssignment[documents[m][n]] + beta);
        }
        return phi;
    }

    private double[] readOutTheta(int m) {
        double[] theta = new double[K];
        for (int t=0; t<K; t++) {
            theta[t] = (documentTopicCount[m][t] + alpha) / (documentTopicSum[m] + alpha);
        }
        return theta;
    }


    public int[] getThemeAssignment() {
        if (themeAssignment != null) {
            return themeAssignment;
        }
        int[] ret = new int[D];

        for (int m=0; m<D; m++) {
            double maxValue = 0;


        }

        return null;
    }

    public String[] getDocuments() {
        return rawDocuments;
    }

    public String[] themeTop(int k) {

        return null;
    }
}
