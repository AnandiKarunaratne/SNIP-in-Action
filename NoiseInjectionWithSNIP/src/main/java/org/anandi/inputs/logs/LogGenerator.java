package org.anandi.inputs.logs;

import org.anandi.inputs.models.ModelGenerator;
import org.anandi.quality.QualityChecker;
import org.anandi.snip.eventlog.EventLogUtils;
import org.anandi.snip.snip.NoiseInjectionManager;
import org.anandi.snip.eventlog.EventLog;
import org.anandi.snip.eventlog.Trace;
import org.anandi.snip.snip.NoiseType;

import java.util.*;

public class LogGenerator {
    private final EventLog eventLog;
    private final List<EventLog> cleanEventLogs = new ArrayList<>();

    private static final List<Integer> LOG_SIZES = new ArrayList<>();
    private static final List<Integer> NOISE_LEVELS = new ArrayList<>();
    private static final List<NoiseType> NOISE_TYPES = new ArrayList<>();

    private final Map<String, List<String>> data = new HashMap<>();

    public LogGenerator() {
        LOG_SIZES.add(256);
        LOG_SIZES.add(512);
        LOG_SIZES.add(1024);
        LOG_SIZES.add(2048);

        NOISE_LEVELS.add(1);
        NOISE_LEVELS.add(2);

        NOISE_TYPES.add(NoiseType.ABSENCE);
        NOISE_TYPES.add(NoiseType.INSERTION);
        NOISE_TYPES.add(NoiseType.ORDERING);
        NOISE_TYPES.add(NoiseType.SUBSTITUTION);

        String logFilePath = "src/main/resources/source-logs/PE_01_BPI_Challenge_01_009.2048.0.xes";
        eventLog = new EventLogUtils().readXES(logFilePath);
        generateCleanEventLogs();
        injectNoise();
    }

    // TODO: this process should be updated. i should rather generate clean logs from playing out a petri net.
    private void generateCleanEventLogs() {
        Random random = new Random();
        EventLog generatedEventLog = new EventLog();
        for (int j = 0; j < LOG_SIZES.size(); j++) {
            int size;
            if (j == 0) {
                size = LOG_SIZES.get(j);
            } else {
                size = LOG_SIZES.get(j) - LOG_SIZES.get(j - 1);
            }
            for (int i = 0; i < size; i++) {
                int index = random.nextInt(eventLog.size());
                generatedEventLog.add(eventLog.get(index));
            }
            EventLog eventLogOfSize = new EventLog(generatedEventLog);
            cleanEventLogs.add(eventLogOfSize);
            String logFilePath = "src/main/resources/logs/PE_01_BPI_Challenge_01_009." + LOG_SIZES.get(j) + ".0.xes";
            String modelFilePath = "src/main/resources/models/PE_01_BPI_Challenge_01_009." + LOG_SIZES.get(j) + ".0.pnml";
            data.put(modelFilePath, new ArrayList<>()); // entry for each model
            new EventLogUtils().generateXES(eventLogOfSize, logFilePath);
            new ModelGenerator().discoverModel(logFilePath, modelFilePath);
            double precision = QualityChecker.calculateModelLogPrecision(logFilePath, modelFilePath);
            double recall = QualityChecker.calculateModelLogRecall(logFilePath, modelFilePath);
            data.get(modelFilePath).add(logFilePath);
            data.get(modelFilePath).add(String.valueOf(precision));
            data.get(modelFilePath).add(String.valueOf(recall));
        }
    }

    // TODO: at the moment, only upto (larger log size / immediately smaller log size) -- which is 2 -- is supported.
    private void injectNoise() {
        // different noise types
        for (NoiseType noiseType : NOISE_TYPES) {
            Set<NoiseType> noiseTypes = new HashSet<>();
            noiseTypes.add(noiseType);
            List<EventLog> cleanTracesCopy = copyEventLogList();
            Random random = new Random();
            for (int i = 0; i < LOG_SIZES.size(); i++) {
                for (int j = 0; j < NOISE_LEVELS.size(); j++) {
                    int numOfNoisyTraces = getNumOfNoisyTraces(i, j);
                    NoiseInjectionManager noiseInjectionManager = new NoiseInjectionManager();
                    for (int k = 0; k < numOfNoisyTraces; k++) {
                        int noisyTraceIndex = random.nextInt(LOG_SIZES.get(i)); // select random index
                        Trace noisyTrace = cleanTracesCopy.get(i).get(noisyTraceIndex); // get that trace
                        System.out.println(noisyTrace);
                        noiseInjectionManager.generateNoisyTrace(noisyTrace, random.nextInt(noisyTrace.size() / 3) + 1, noiseTypes);
                    }
                    String logFilePath = "src/main/resources/logs/PE_01_BPI_Challenge_01_009." + LOG_SIZES.get(i) + "."
                            + noiseType.toString().charAt(0) + "." + NOISE_LEVELS.get(j) + ".xes";
                    String modelFilePath = "src/main/resources/models/PE_01_BPI_Challenge_01_009." + LOG_SIZES.get(i) + "."
                            + noiseType.toString().charAt(0) + "." + NOISE_LEVELS.get(j) + ".pnml";
                    new EventLogUtils().generateXES(new EventLog(cleanTracesCopy.get(i)), logFilePath);
                    new ModelGenerator().discoverModel(logFilePath, modelFilePath);
//                    System.out.println(cleanTracesCopy.get(i));
                    double precision = QualityChecker.calculateModelLogPrecision(logFilePath, modelFilePath);
                    double recall = QualityChecker.calculateModelLogRecall(logFilePath, modelFilePath);
                    data.put(modelFilePath, new ArrayList<>()); // entry for each model
                    data.get(modelFilePath).add(logFilePath);
                    data.get(modelFilePath).add(String.valueOf(precision));
                    data.get(modelFilePath).add(String.valueOf(recall));
                }
            }
        }

        // mixed noise
        List<EventLog> cleanTracesCopy = copyEventLogList();
        Random random = new Random();
        for (int i = 0; i < LOG_SIZES.size(); i++) {
            for (int j = 0; j < NOISE_LEVELS.size(); j++) {
                int numOfNoisyTraces = getNumOfNoisyTraces(i, j);
                NoiseInjectionManager noiseInjectionManager = new NoiseInjectionManager();
                for (int k = 0; k < numOfNoisyTraces; k++) {
                    int noisyTraceIndex = random.nextInt(LOG_SIZES.get(i)); // select random index
                    Trace noisyTrace = cleanTracesCopy.get(i).get(noisyTraceIndex); // get that trace
                    System.out.println();
                    noiseInjectionManager.generateNoisyTrace(noisyTrace, random.nextInt(noisyTrace.size() / 3) + 1);
                }
                String logFilePath = "src/main/resources/logs/PE_01_BPI_Challenge_01_009." + LOG_SIZES.get(i) + "."
                        + "M." + NOISE_LEVELS.get(j) + ".xes";
                String modelFilePath = "src/main/resources/models/PE_01_BPI_Challenge_01_009." + LOG_SIZES.get(i) + "."
                        + "M." + NOISE_LEVELS.get(j) + ".pnml";
                new EventLogUtils().generateXES(new EventLog(cleanTracesCopy.get(i)), logFilePath); // M for mixed
                new ModelGenerator().discoverModel(logFilePath, modelFilePath);
//                System.out.println(cleanTracesCopy.get(i));
                double precision = QualityChecker.calculateModelLogPrecision(logFilePath, modelFilePath);
                double recall = QualityChecker.calculateModelLogRecall(logFilePath, modelFilePath);
                data.put(modelFilePath, new ArrayList<>()); // entry for each model
                data.get(modelFilePath).add(logFilePath);
                data.get(modelFilePath).add(String.valueOf(precision));
                data.get(modelFilePath).add(String.valueOf(recall));
            }
        }
        System.out.println(data);
    }

    // helper methods
    private List<EventLog> copyEventLogList() {
        List<EventLog> cleanEventLogsCopy = new ArrayList<>(cleanEventLogs.size());
        EventLog originalEventLog = cleanEventLogs.get(cleanEventLogs.size() - 1); // assumption: larger logs contain smaller logs in the same trace order
        EventLog newEventLog = new EventLog();

        for (int i = 0; i < cleanEventLogs.size(); i++) {
            int startIndex;
            int endIndex = LOG_SIZES.get(i) - 1;
            if (i == 0) {
                startIndex = 0;
            } else {
                startIndex = LOG_SIZES.get(i-1);
            }

            for (int j = startIndex; j <= endIndex; j++) {
                Trace newTrace = new Trace(new ArrayList<>(originalEventLog.get(j)));
                newEventLog.add(newTrace);
            }
            cleanEventLogsCopy.add(newEventLog);

        }
        return cleanEventLogsCopy;
    }

    private static int getNumOfNoisyTraces(int i, int j) {
        int numOfNoisyTraces;
        if (i == 0 && j ==0) {
            numOfNoisyTraces = (int) Math.round(LOG_SIZES.get(i) * NOISE_LEVELS.get(j) / 100.0);
        } else if (i == 0) {
            numOfNoisyTraces = (int) Math.round(LOG_SIZES.get(i) * NOISE_LEVELS.get(j) / 100.0) -
                    (int) Math.round(LOG_SIZES.get(i) * NOISE_LEVELS.get(j -1) / 100.0);
        } else if (j == 0) {
            numOfNoisyTraces = (int) Math.round(LOG_SIZES.get(i) * NOISE_LEVELS.get(j) / 100.0) -
                    (int) Math.round(LOG_SIZES.get(i -1) * NOISE_LEVELS.get(NOISE_LEVELS.size() - 1) / 100.0);
        } else {
            numOfNoisyTraces = (int) Math.round(LOG_SIZES.get(i) * NOISE_LEVELS.get(j) / 100.0) -
                    (int) Math.round(LOG_SIZES.get(i) * NOISE_LEVELS.get(j -1) / 100.0);
        }
        return numOfNoisyTraces;
    }

    public static void main(String[] args) {
        new LogGenerator();
    }
}

