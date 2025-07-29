package org.anandi;

import org.anandi.inputs.logs.LogGeneratorWithSNIP;
import org.anandi.inputs.models.ModelGenerator;
import org.anandi.quality.QualityChecker;
import org.anandi.snip.eventlog.EventLog;
import org.anandi.snip.eventlog.EventLogUtils;
import org.apache.commons.math3.util.Pair;
import org.jbpt.pm.log.ActivityOps;
import org.jbpt.pm.log.DFRelationOps;
import org.jbpt.pm.log.TraceOps;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        generateLogs();
//        runExperiment();
//        getLogInsights();

    }

    public static void getLogInsights() {
        String baseFilePath =
                "/Users/anandik/Library/CloudStorage/OneDrive-TheUniversityofMelbourne/GR/Datasets/CaseStudyNoise/logs/";
        String[] systems = {"Sepsis", "RTFMS", "BPIC2012"};
        String[] noiseList = {"-a", "-i", "-o", "-s", "-i-a-o-s"};
        String[] noiseLevels = {"0.5", "1.0", "1.5", "2.0"};
        for (String system : systems) {
            for (String noise : noiseList) {
                for (String noiseLevel : noiseLevels) {
                    String logFilePath = baseFilePath + system + "-noise" + noise + "-" + noiseLevel + ".xes";
//                    EventLog eventLog = new EventLogUtils().readXES(logFilePath);
//                    System.out.println(
//                            system + "-noise" + noise + "-" + noiseLevel + ".xes, "+
//                                    eventLog.getNumOfTraces() + ", " +
//                                    eventLog.getNumOfDistinctTraces() + ", " +
//                                    eventLog.getNumOfActivities()
//                    );

                    org.jbpt.pm.log.EventLog eventLog1 = new org.jbpt.pm.log.EventLog().parseEventLogFromXES(logFilePath);
//                    org.jbpt.pm.log.EventLogUtils utils = new org.jbpt.pm.log.EventLogUtils();
                    System.out.println(new ActivityOps(eventLog1).getDistinctEventDataCount() + ", " +
                            new DFRelationOps(eventLog1).getDistinctEventDataCount() + ", " +
                            new TraceOps(eventLog1).getDistinctEventDataCount() + ", " );

                }
            }


        }

        for (String system : systems) {
            String logFilePath = baseFilePath + system + ".xes";
            org.jbpt.pm.log.EventLog eventLog1 = new org.jbpt.pm.log.EventLog().parseEventLogFromXES(logFilePath);
//                    org.jbpt.pm.log.EventLogUtils utils = new org.jbpt.pm.log.EventLogUtils();
            System.out.println(new ActivityOps(eventLog1).getDistinctEventDataCount() + ", " +
                    new DFRelationOps(eventLog1).getDistinctEventDataCount() + ", " +
                    new TraceOps(eventLog1).getDistinctEventDataCount());
        }
    }

    public static void generateLogs() {
        String baseFilePath =
                "/Users/anandik/Library/CloudStorage/OneDrive-TheUniversityofMelbourne/GR/Datasets/CaseStudyNoise/Repetition/logs/";
        String[] systems = {"RTFMS"};
        String[] noiseList = {"-i-a-o-s"};
        String[] noiseLevels = {"0.5", "1.0", "1.5", "2.0"};
        for (String system : systems) {
//            for (int i = 2; i <= 10; i++) {
                String systemFilePath = baseFilePath + system + ".xes";
                new LogGeneratorWithSNIP().generateNoisyLogs(systemFilePath, Arrays.asList(noiseList), Arrays.asList(noiseLevels));
//            }

        }
    }

    public static void runExperiment() {
        String baseFilePath =
                "/Users/anandik/Library/CloudStorage/OneDrive-TheUniversityofMelbourne/GR/Datasets/CaseStudyNoise/";
        final List<List<String>> csvContent = new ArrayList<>();

        List<String> csvHeader = new ArrayList<>();
        csvHeader.add("system");
        csvHeader.add("cleanPrecision");
        csvHeader.add("cleanRecall");
        csvHeader.add("log");
        csvHeader.add("model");
        csvHeader.add("noiseType");
        csvHeader.add("noiseLevel");
        csvHeader.add("discAlg");
        csvHeader.add("precision");
        csvHeader.add("recall");

        // generate models for each log in the logs
        String systemsFilePath = baseFilePath + "systems/";
        String logsFilePath = baseFilePath + "logs/";
        String modelsFilePath = baseFilePath + "models/";

        String[] systems = {"BPIC2012"};
        String[] noiseList = {"-a", "-i", "-o", "-s", "-i-a-o-s"};
        String[] noiseLevels = {"0.5", "1.0", "1.5", "2.0"};

        for (String system : systems) {
            String systemFilePath = systemsFilePath + system + ".xes";


            new ModelGenerator().discoverModel(systemFilePath, systemsFilePath + system + ".pnml");
            Pair<Double, Double> idealRecPre =
                    QualityChecker.calculateModelLogPreRec(systemFilePath, systemsFilePath + system + ".pnml");

            for (String noiseType : noiseList) {
                for (String noiseLevel : noiseLevels) {
                    List<String> csvEntry = new ArrayList<>();
                    csvEntry.add(systemFilePath);
                    csvEntry.add(String.valueOf(idealRecPre.getSecond()));
                    csvEntry.add(String.valueOf(idealRecPre.getFirst()));

                    String logFilePath = logsFilePath + system + "-noise" + noiseType + "-" +noiseLevel + ".xes";
                    String modelFilePath = modelsFilePath + system + "-noise" + noiseType + "-" +noiseLevel + ".pnml";
//                    String distinctFilePath = baseFilePath + "SepsisCases-noise-" + noiseType + noiseLevel + ".xes";
                    new ModelGenerator().discoverModel(logFilePath, modelFilePath);

                    csvEntry.add(logFilePath);
                    csvEntry.add(modelFilePath);
                    csvEntry.add(noiseType);
                    csvEntry.add(noiseLevel);
                    csvEntry.add("Alpha");

                    Pair<Double, Double> recPre = QualityChecker.calculateModelLogPreRec(logFilePath, modelFilePath);
                    csvEntry.add(String.valueOf(recPre.getSecond()));
                    csvEntry.add(String.valueOf(recPre.getFirst()));

                    csvContent.add(csvEntry);
                    writeToCSV(baseFilePath+"results.csv", csvEntry);
                }
            }
        }
//        writeToCSV(baseFilePath+"results.csv", csvContent);
    }

    public static void writeToCSV(String filePath, List<String> data) {
        try (FileWriter writer = new FileWriter(filePath, true)) { // 'true' to append
            for (String row : data) {
                writer.write(row + ",");  // Write each line individually
            }
            writer.write("\n");
            writer.flush(); // Explicitly flush to write data to the file
            System.out.println("CSV file written successfully to: " + filePath);
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
    }

    private static void testMethod() {
        String logFilePath = "src/main/resources/source-logs/PE_01_BPI_Challenge_01_009.128.0.xes";
        String distinctFilePath = "src/main/resources/source-logs/PE_01_BPI_Challenge_01_009.128.0.distinct.xes";
        EventLog eventLog = new EventLogUtils().readFile(logFilePath);
        new EventLogUtils().generateXES(new EventLog(eventLog.getDistinctTraces()), distinctFilePath);
        System.out.println(QualityChecker.calculateModelLogPreRec(logFilePath, distinctFilePath));
    }
}

/*
 * Exception happened: RTFMS logs are missing for mixed noise type from 1.0 onwards (included).
 * Have to calculate them again.
 * Sepsis completed.
 * RTFMS -
 * 1. delete mixed noise logs [done]
 * 2. generate mixed noise logs again [done]
 * 3. remove the entries related to deleted logs from the csv [done]
 * 4. adjust the code to run RTFMS mixed noise only for experiment [done]
 * 5. run [done]
 * 6. run BPIC2012 for all noise types
 */