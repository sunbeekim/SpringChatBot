import os
import sys
import requests
from bs4 import BeautifulSoup
from datetime import datetime

def scrape_wikipedia_term(word, output_path, url):
    """
    Scrapes Wikipedia for the specified term and logs debug information.
    """
    log_file = os.path.join(output_path, f"{word}_log.txt")
    with open(log_file, 'w', encoding='utf-8') as log:
        try:
            # Construct the URL
            full_url = f"{url.rstrip('/')}/{word}"
            log.write(f"Full URL: {full_url}\n")
            print(f"Full URL: {full_url}")

            # Send HTTP GET request
            headers = {
                'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
            }
            response = requests.get(full_url, headers=headers)
            response.raise_for_status()
            log.write(f"HTTP Response Code: {response.status_code}\n")

            # Log HTML content preview
            log.write(f"HTML Content (Preview):\n{response.text[:1000]}\n")

            # Parse the HTML
            soup = BeautifulSoup(response.content, 'html.parser')
            content_div = soup.find('div', {'id': 'mw-content-text'})
            if content_div is None:
                log.write("Error: 'mw-content-text' div not found.\n")
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
                log.write("'같이 보기' section found.\n")
                for li in see_also_section.find_all('li'):
                    output_lines.append(li.get_text(strip=True))
            else:
                log.write("Warning: '같이 보기' section not found.\n")

            output_lines.append("\n")

            # Extract all <p> tags
            paragraphs = content_div.find_all('p')
            for paragraph in paragraphs:
                text = paragraph.get_text(strip=True)
                if text:
                    output_lines.append(text)

            # Save the extracted content
            output_text = '\n'.join(output_lines).strip()
            if not output_text:
                log.write("Warning: No content extracted.\n")
                print("Warning: No content extracted.")
                return

            os.makedirs(output_path, exist_ok=True)
            output_file = os.path.join(output_path, f"{word}.txt")
            with open(output_file, 'w', encoding='utf-8') as file:
                file.write(output_text)

            log.write(f"Output saved to: {output_file}\n")
            print(f"Output saved to: {output_file}")

        except requests.RequestException as e:
            log.write(f"Error fetching the URL: {e}\n")
            print(f"Error fetching the URL: {e}")
        except Exception as e:
            log.write(f"An error occurred: {e}\n")
            print(f"An error occurred: {e}")

if __name__ == '__main__':
    if len(sys.argv) < 4:
        print("Usage: python script.py <word> <output_path> <url>")
        sys.exit(1)

    word = sys.argv[1]
    output_path = sys.argv[2]
    url = sys.argv[3]
    scrape_wikipedia_term(word, output_path, url)
