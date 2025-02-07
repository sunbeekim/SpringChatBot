package com.example.demo.util;

import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Component
public class PythonScriptExecutor {
    public String executeScript(String url, String word, Path filePath) {
        System.out.println("Executing script with URL: " + url + " and Word: " + word);

        String scriptPath = "C:\\java\\MainProject\\newtest\\LLaMA\\scrape_wikipedia.py";

        // 기본 실행 명령어 리스트 (우선순위: Windows에서는 `py` -> `python`, Linux/macOS에서는 `python` -> `python3`)
        List<String> pythonCommands = System.getProperty("os.name").toLowerCase().contains("win")
                ? Arrays.asList("py", "python")
                : Arrays.asList("python", "python3");

        for (String pythonCommand : pythonCommands) {
            try {
                System.out.println("Trying to execute with: " + pythonCommand);

                // Ensure the output directory exists
                Files.createDirectories(filePath.getParent());

                // Start the process
                ProcessBuilder pb = new ProcessBuilder(pythonCommand, scriptPath, word, filePath.toString(), url);
                pb.environment().put("PYTHONIOENCODING", "UTF-8");
                Process process = pb.start();

                // Capture output and errors
                StringBuilder output = new StringBuilder();
                StringBuilder errorOutput = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                    reader.lines().forEach(line -> output.append(line).append("\n"));
                }
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"))) {
                    errorReader.lines().forEach(line -> errorOutput.append(line).append("\n"));
                }

                // Wait for the process to complete
                int exitCode = process.waitFor();
                System.out.printf("Python script finished with exit code: %d using %s%n", exitCode, pythonCommand);

                if (exitCode == 0) {
                    // 실행 성공
                    if (Files.exists(filePath)) {
                        System.out.println("Result file exists: " + filePath);
                        if (Files.size(filePath) == 0) {
                            System.err.println("Result file is empty. Task failed.");
                            return "Task failed: File is empty.";
                        } else {
                            System.out.println("Result file successfully created with content.");
                            return "Task completed successfully.";
                        }
                    } else {
                        System.err.println("Result file does not exist: " + filePath);
                        return "Task failed: File not found.";
                    }
                } else {
                    // 실행 실패 시 로그 출력
                    System.err.println("Error output: " + errorOutput);
                }

            } catch (Exception e) {
                System.err.println("Failed to execute script with " + pythonCommand + ": " + e.getMessage());
            }
        }

        // 모든 실행 명령어가 실패했을 경우
        return "Error: Unable to execute the script with any Python command.";
    }
}
