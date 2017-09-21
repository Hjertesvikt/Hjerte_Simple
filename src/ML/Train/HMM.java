package ML.Train;

import ML.Classify.*;
import java.util.*;

// Referenced classes of package ML.Train:
//            HMMutilities, Trainer
public class HMM {

    /**
     * The times (S1, sys, S2, dia) will be our HMM states, and we must give an
     * observation matrix as input to the training, The result of training
     * should fill in the state transition matrix
     *
     */
    Observations obs;

    public String mostFreqState;

    public HashMap observationCounts;           // HashMap<String, HashMap<Observation, Integer>>

    // This is the most important item in this class
    // It gives the probability of transition from one state to the next
    // for each next step
    // "Current state" => {
    //         ("next state 1", probability = 0.1),
    //         ("next state 2", probability = 0.6),
    //         ("next state 3", probability = 0.4)
    public HashMap transitionsProbs;           // HashMap<"CurrentState", HashMap<"NextState", probability>>

    public HashMap hidnStatesCounts;            // HashMap<String, Integer>
    public HashMap stateForObservationCounts;   // HashMap<Observation, HashMap<String state, Integer>>

    Integer mostFreqStateCount;
    int numTrainingBigrams;

    private float similarity;
    int S3Counter;
    private int SxCounter;
    private float LVSTScore;

    public LinkedList worksWell;
    LinkedList worksBadly; // not used
    private int S1sLength;
    private int S1sCount;
    private int S3sCount;

    public HMM(LinkedList obb) {
        obs = null;
        mostFreqState = null;
        observationCounts = new HashMap();
        numTrainingBigrams = 0;
        transitionsProbs = new HashMap();
        hidnStatesCounts = new HashMap();
        stateForObservationCounts = new HashMap();
        mostFreqStateCount = new Integer(0);
        obs = new Observations(obb);

        worksWell = new LinkedList();
        worksBadly = new LinkedList();

        similarity = 0;
        S3Counter = 0;
        SxCounter = 0;
        LVSTScore = 0;
        S1sLength = 0;
        S1sCount = 0;
        S3sCount = 0;
    }

    public void train() {
        String prevState = null;
        Observation currentObservation = null;
        final Trainer trn = new Trainer();
        prevState = this.obs.currentStateTag();
        this.obs.gotoNextObservation();
        while (this.obs.hasNext()) {
            final String currentState = this.obs.currentStateTag();
            currentObservation = this.obs.currentObservation();
            this.obs.incPtr();
            prevState = trn.parseTrainer(this, prevState, currentState, currentObservation);
        }
    }

    /**
     * The class initialization has read observation This method sends them in a
     * list
     *
     * @return
     */
    public LinkedList getObsrvSeq() {
        LinkedList list = new LinkedList();
        for (; obs.hasNext(); list.add(obs.currentObservation())) {
            obs.gotoNextObservation();
        }
        return list;
    }

    /*
     * Calculates probability of (State|Observation), that this state corresponds to that Observation
     */
    public float calcLikelihood(String state, Observation word) {
        int vocabSize = stateForObservationCounts.keySet().size();
        int deux;
        float trois;
        deux = (HMMutilities.countsObsrv(observationCounts, state, word) + 1);
        trois = (float) (HMMutilities.countStates(hidnStatesCounts, state) + vocabSize);
        float un = deux / trois;
        return (float) un;
    }

    /*
     * Calculates probability of (State1|State2), of transition from state1 to state2
     */
    public float calcPriorProbState(String state1, String state2) {
        int vocabSize = hidnStatesCounts.keySet().size();
        float deux = (float) (HMMutilities.countsStates(transitionsProbs, state1, state2) + 1);
        float trois = (float) (HMMutilities.countStates(hidnStatesCounts, state1) + vocabSize);
        return deux / trois;
    }

    /* 
     * This method computes the probability that State is the appropriate state for this Observation,
     * given the probabilities before it (found in prevMap) 
     * 
     * probability = max ( previous Viterbi path probability *
     *                     transition probability *
     *                     state observation likelihood )
     *                     
     */
    public Node calcNode(Observation word, String state, HashMap prevMap) {
        Node n = new Node(word, state);
        float maxProb = 0.0F;
        Iterator iterator = prevMap.keySet().iterator();
        do {
            if (!iterator.hasNext()) {
                break;
            }
            String prevTag = (String) iterator.next();
            Node prevNode = (Node) prevMap.get(prevTag);
            float prevProb = prevNode.probability;
            prevProb *= this.calcPriorProbState(prevTag, state);
            if (prevProb >= maxProb) {
                maxProb = prevProb;
                n.parent = prevNode;
            }
        } while (true);
        n.probability = maxProb * calcLikelihood(state, word);
        return n;
    }

    public void setSimilarity(final float simil) {
        this.similarity = simil;
    }

    public String getSimilarity() {
        return String.valueOf(this.similarity);
    }

    public float getS3Score() {
        return (float) S3Counter
                / (float) S1sCount;
    }

    public void setS3Score(int cntr) {
        S3Counter = cntr;
    }

    public float getLVSTScore() {
        return (float) LVSTScore;
    }

    void setLVSTScore(float lvst) {
        LVSTScore = lvst;
    }

    public float getSxScore() {
        return (float) SxCounter
                / (float) S1sCount;
    }

    public void setSxScore(int sxCounter) {
        SxCounter = sxCounter;
    }

    public float getS1Length() {
        return (float) S1sLength
                / (float) S1sCount;
    }

    public void setS1Length(int s1Length) {
        S1sLength = s1Length ;
    }
    
    public void setS1Nb(int s1count) {
        S1sCount = s1count ;
    }

}
