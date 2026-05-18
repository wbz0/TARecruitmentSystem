"""
Test Application Status API endpoints
"""
from playwright.sync_api import sync_playwright

def test_application_status_api():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context()
        page = context.new_page()

        # Collect console messages
        console_logs = []
        page.on("console", lambda msg: console_logs.append(f"[{msg.type}] {msg.text}"))

        print("=" * 50)
        print("Testing Application Status API")
        print("=" * 50)

        # Test 1: Try to access /apply without login (should return 401)
        print("\n[Test 1] GET /apply - Without login (should be 401)")
        response = page.goto("http://localhost:8080/groupproject/apply", wait_until="networkidle")
        print(f"Status: {response.status}")
        print(f"Response: {response.text()}")

        print("\n" + "=" * 50)
        print("All tests completed!")
        print("=" * 50)

        browser.close()

if __name__ == "__main__":
    test_application_status_api()
