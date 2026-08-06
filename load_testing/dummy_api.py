import http.server
import socketserver
import time
import random

PORT = 9999

class DummyHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        # Simulate realistic API processing time (50ms - 250ms mostly, sometimes up to 1500ms)
        delay = random.choice([
            random.uniform(0.05, 0.25),
            random.uniform(0.05, 0.25),
            random.uniform(0.05, 0.25),
            random.uniform(0.5, 1.5) # Occasionally slow
        ])
        time.sleep(delay)
        
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        self.wfile.write(b'{"status": "success"}')

class ThreadedHTTPServer(socketserver.ThreadingMixIn, http.server.HTTPServer):
    pass

with ThreadedHTTPServer(("", PORT), DummyHandler) as httpd:
    print(f"Serving dummy API at port {PORT}")
    httpd.serve_forever()
