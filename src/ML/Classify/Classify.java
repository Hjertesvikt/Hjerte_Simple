package ML.Classify;

import Misc.Gui.Main.EntryPoint;
import Misc.Gui.Main.OuterFrame;
import Misc.AudioFeatures.RecordingInfo;
import ML.Train.HMM;
import ML.Train.Segmentation;
import ML.featureDetection.FindBeats;
import ML.featureDetection.NormalizeBeat;
import Misc.AudioFeatures.FromFileToAudio;
import Misc.Gui.Controller.Control;
import Misc.sampled.AudioSamples;
import java.io.File;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class Classify {
    
    float similarity ;
    public Segmentation segmt  ;
    public FromFileToAudio e = null;

    public Classify(Control controller, OuterFrame outer_frame, RecordingInfo info[]) {
        FindBeats cb ;
        LinkedList beats = null;
        RecordingInfo recordings[] = info;
        if (recordings == null) {
            String message = "No recordings available to extract features from.";
            JOptionPane.showConfirmDialog(null, message, "WARNING", 0);
        }
        if (recordings.length > 1) {
            String message = "No more than one recording for classifying.";
            JOptionPane.showConfirmDialog(null, message, "WARNING", 0);
        }

        File load_file = new File(recordings[0].file_path);
        
        try {
            e = new FromFileToAudio(
                    load_file);
        } catch (Exception ex) {
            Logger.getLogger(Classify.class.getName()).log(Level.SEVERE, null, ex);
        }
        AudioSamples smpls = e.getAudioSamples();
        float samples[] = smpls.getSamplesMixedDown();
        int smplingRate = (int) smpls.getSamplingRate();

        int heart_rate = 60;

        /**
         * Normalize dynamics of signal
         *
         */
        NormalizeBeat norm = new NormalizeBeat();

        float[] data_norm = norm.normalizeAmplitude(samples);

        cb = new FindBeats();

        // calculate beat rate
        cb.calcBeat(data_norm, smplingRate, heart_rate);

        // calculate beat rate
        beats = cb.getProbableBeats();

        /**
         *
         * Now that we have a clean sound, we will segment it to recognize the
         * significant times like the locations for S1, systole, s2 and diastole
         *
         * This is the labelled observations private LinkedList<String>
         * moreBeatsType = new LinkedList<String>(); private LinkedList<Integer>
         * moreBeatsIndx = new LinkedList<Integer>();
         *
         * Those times (S1, sys, S2, dia) will be our HMM states, and we must
         * have an observation matrix as input to the training, The result of
         * training should fill in the state transition matrix.
         */
        segmt = new Segmentation();

        segmt.segmentation(cb, smplingRate);

        // Add suffix to Observations names
        // For the HMM to separate the observations in more cases than S1-S4, we need to
        // add a "minor" numbering to the "Sx" string.
        // However it is added later than primary name, to have a reasonable amount of Observations
        // (not having hundreds that are destination, only once)
        // This because otherwise having only four kind of "Observation" is not very helpful
        LinkedList obsList = new LinkedList();
        for (int u = 0; u < segmt.segmentedSounds.size(); u++) {
            Observation obs = (Observation) segmt.segmentedSounds.get(u);
            int size = obs.getSign().size();
            // Lets have no more than 20 Observations
//            float fact = (float) (size / 20.0) ;
            int deux = (int) (size % 20);
            String suffix = String.valueOf(deux);
            obs.setNameSufx(suffix);
            obsList.add(obs) ;
        }

            EntryPoint.hmmTest = new HMM(obsList);
            EntryPoint.hmmTest.train(); 

            Viterbi vt = new Viterbi();

            similarity = vt.viterbi();
            EntryPoint.hmmTest.setSimilarity(similarity) ;
            EntryPoint.hmmTest.setS1Nb(segmt.getS1Counter()); ;
            EntryPoint.hmmTest.setS1Length(segmt.getS1Length()); ;
            EntryPoint.hmmTest.setS3Score(segmt.getS3Counter()); ;
            EntryPoint.hmmTest.setSxScore(segmt.getSxCounter()); ;
     }
}
