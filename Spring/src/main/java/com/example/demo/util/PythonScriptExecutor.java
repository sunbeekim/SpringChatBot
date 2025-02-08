package com.example.demo.util;

import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;


@Component
public class PythonScriptExecutor {
    public String executeScript(String url, String word, Path filePath) {
        System.out.println("Executing script with URL: " + url + " and Word: " + word);

        String scriptPath = "C:\\java\\MainProject\\newtest\\LLaMA\\scrape_wikipedia.py";
        Path scriptPathObj = Paths.get(scriptPath);
        
        // 스크립트 파일이 없으면 생성
        if (!Files.exists(scriptPathObj)) {
            try {
                // 스크립트 파일의 디렉토리 생성
                Files.createDirectories(scriptPathObj.getParent());
                
                // 파이썬 스크립트 내용
                String scriptContent = """
                import os
                import sys
                import requests
                from bs4 import BeautifulSoup
                from datetime import datetime

                def scrape_wikipedia_term(word, output_path, url):
                    \"\"\"
                    Scrapes Wikipedia for the specified term and logs debug information.
                    \"\"\"
                    log_file = os.path.join(output_path, f"{word}_log.txt")
                    with open(log_file, 'w', encoding='utf-8') as log:
                        try:
                            # Construct the URL
                            full_url = f"{url.rstrip('/')}/{word}"
                            log.write(f"Full URL: {full_url}\\n")
                            print(f"Full URL: {full_url}")

                            # Send HTTP GET request
                            headers = {
                                'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
                            }
                            response = requests.get(full_url, headers=headers)
                            response.raise_for_status()
                            log.write(f"HTTP Response Code: {response.status_code}\\n")

                            # Parse the HTML
                            soup = BeautifulSoup(response.content, 'html.parser')
                            content_div = soup.find('div', {'id': 'mw-content-text'})
                            if content_div is None:
                                log.write("Error: 'mw-content-text' div not found.\\n")
                                print("Error: 'mw-content-text' div not found.")
                                return

                            # Extract '같이 보기' section
                            see_also_section = None
                            for header in content_div.find_all('h2'):
                                if '같이 보기' in header.get_text():
                                    see_also_section = header.find_next('ul')
                                    break

                            output_lines = []
                            if see_also_section:
                                output_lines.append("같이 보기")
                                log.write("'같이 보기' section found.\\n")
                                for li in see_also_section.find_all('li'):
                                    output_lines.append(li.get_text(strip=True))
                            else:
                                log.write("Warning: '같이 보기' section not found.\\n")

                            output_lines.append("\\n")

                            # Extract all <p> tags
                            paragraphs = content_div.find_all('p')
                            for paragraph in paragraphs:
                                text = paragraph.get_text(strip=True)
                                if text:
                                    output_lines.append(text)

                            # Save the extracted content
                            output_text = '\\n'.join(output_lines).strip()
                            if not output_text:
                                log.write("Warning: No content extracted.\\n")
                                print("Warning: No content extracted.")
                                return

                            os.makedirs(output_path, exist_ok=True)
                            output_file = os.path.join(output_path, f"{word}.txt")
                            with open(output_file, 'w', encoding='utf-8') as file:
                                file.write(output_text)

                            log.write(f"Output saved to: {output_file}\\n")
                            print(f"Output saved to: {output_file}")

                        except requests.RequestException as e:
                            log.write(f"Error fetching the URL: {e}\\n")
                            print(f"Error fetching the URL: {e}")
                        except Exception as e:
                            log.write(f"An error occurred: {e}\\n")
                            print(f"An error occurred: {e}")

                if __name__ == '__main__':
                    if len(sys.argv) < 4:
                        print("Usage: python script.py <word> <output_path> <url>")
                        sys.exit(1)

                    word = sys.argv[1]
                    output_path = sys.argv[2]
                    url = sys.argv[3]
                    scrape_wikipedia_term(word, output_path, url)
                """;
                
                // 스크립트 파일 생성
                Files.writeString(scriptPathObj, scriptContent);
                System.out.println("파이썬 스크립트 파일이 생성되었습니다: " + scriptPath);
            } catch (IOException e) {
                throw new RuntimeException("파이썬 스크립트 파일 생성 중 오류 발생: " + e.getMessage(), e);
            }
        }

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
