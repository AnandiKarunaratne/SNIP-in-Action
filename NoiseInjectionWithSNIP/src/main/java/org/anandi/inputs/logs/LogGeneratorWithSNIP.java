package org.anandi.inputs.logs;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LogGeneratorWithSNIP {

    public void generateNoisyLogs(String logPath, List<String> noiseTypes, List<String> noiseLevels) {
        for (String noiseType : noiseTypes) {
            String readingLogPath = logPath;
            for (String noiseLevel : noiseLevels) {
                runSNIPJar(readingLogPath, noiseType);

                String baseLogPath = readingLogPath.substring(0, readingLogPath.lastIndexOf('.'));
                readingLogPath = baseLogPath + "-noise" + noiseType + "-0.5.xes";
                if (!noiseLevel.equals("0.5")) {
                    String newLogPath = baseLogPath.substring(0, baseLogPath.length() - 3) + noiseLevel + ".xes";
                    renameFile(readingLogPath, newLogPath);
                    renameFile(readingLogPath.substring(0, readingLogPath.length()-4) + "-log.txt",
                            newLogPath.substring(0, newLogPath.length()-4) + "-log.txt");
                    readingLogPath = newLogPath;
                }
            }
        }
    }

    public void runSNIPJar(String logPath, String noiseType) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (noiseType.equals("-i-a-o-s")) noiseType = "";
        processBuilder.command(
                "java",
                "-jar",
                "/Users/anandik/Library/CloudStorage/OneDrive-TheUniversityofMelbourne/GR/RQ1.1/NIP/target/" +
                        "SNIP-1.0-SNAPSHOT.jar",
                "-l=" + logPath,
                "-n=0.5",
                noiseType
        );

        try {
            Process process = processBuilder.inheritIO().start();
            int exitCode = process.waitFor();
            System.out.println("Process exited with code: " + exitCode);
        } catch (InterruptedException | IOException e) {
            System.err.println("Error running JAR: " + e.getMessage());
        }
    }

    public void renameFile(String oldPath, String newPath) {
        File oldFile = new File(oldPath);
        File newFile = new File(newPath);

        boolean success = oldFile.renameTo(newFile);
        if (success) {
            System.out.println("File renamed to: " + newPath);
        } else {
            System.err.println("Failed to rename file.");
        }
    }
}
