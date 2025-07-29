package org.anandi.quality;

import org.apache.commons.math3.util.Pair;
import org.jbpt.pm.quality.EntropyPrecisionRecallMeasure;
import org.jbpt.pm.tools.QualityMeasuresCLI;

public class QualityChecker {
    public static Pair<Double, Double> calculateModelLogPreRec(String log, String model) {
        Object relTraces = QualityMeasuresCLI.parseModel(log);
        Object retTraces = QualityMeasuresCLI.parseModel(model);
        EntropyPrecisionRecallMeasure epr = new EntropyPrecisionRecallMeasure(relTraces, retTraces, 0, 0, true, true, true);
        Pair<Double, Double> result = null;
        try {
            result = epr.computeMeasure(); // <recall, precision>
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        return result;
    }

    public static Pair<Double, Double> calculateSystemLogPreRec(String log, String system) {
        return calculateModelLogPreRec(system, log);
    }

    public static double calculateModelLogPrecision(String log, String model) {
        Object relTraces = QualityMeasuresCLI.parseModel(log);
        Object retTraces = QualityMeasuresCLI.parseModel(model);
        EntropyPrecisionRecallMeasure epr = new EntropyPrecisionRecallMeasure(relTraces, retTraces, 0, 0, true, true, true);
        double precision = 0;
        try {
            Pair<Double, Double> result = epr.computeMeasure();
            precision = result.getSecond();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        return precision;
    }

    public static double calculateModelLogRecall(String log, String model) {
        Object relTraces = QualityMeasuresCLI.parseModel(log);
        Object retTraces = QualityMeasuresCLI.parseModel(model);
        EntropyPrecisionRecallMeasure epr = new EntropyPrecisionRecallMeasure(relTraces, retTraces, 0, 0, true, true, true);
        double recall = 0;
        try {
            Pair<Double, Double> result = epr.computeMeasure();
            recall = result.getFirst();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        return recall;
    }

    public static double calculateSystemLogPrecision(String log, String system) {
        Object relTraces = QualityMeasuresCLI.parseModel(system);
        Object retTraces = QualityMeasuresCLI.parseModel(log);
        EntropyPrecisionRecallMeasure epr = new EntropyPrecisionRecallMeasure(relTraces, retTraces, 0, 0, true, true, true);
        double precision = 0;
        try {
            Pair<Double, Double> result = epr.computeMeasure();
            precision = result.getSecond();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        return precision;
    }

    public static double calculateSystemLogRecall(String log, String system) {
        Object relTraces = QualityMeasuresCLI.parseModel(log);
        Object retTraces = QualityMeasuresCLI.parseModel(system);
        EntropyPrecisionRecallMeasure epr = new EntropyPrecisionRecallMeasure(relTraces, retTraces, 0, 0, true, true, true);
        double recall = 0;
        try {
            Pair<Double, Double> result = epr.computeMeasure();
            recall = result.getFirst();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        return recall;
    }
}
