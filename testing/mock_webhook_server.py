#!/usr/bin/env python3

"""
Mock webhook server for testing Notification Forwarder
Receives webhook POST requests and logs them for verification
"""

import json
import sys
from datetime import datetime
from http.server import HTTPServer, BaseHTTPRequestHandler
from pathlib import Path

PORT = 8080
LOG_FILE = Path(__file__).parent / "logs" / f"webhook_requests_{datetime.now().strftime('%Y%m%d_%H%M%S')}.log"

# Ensure logs directory exists
LOG_FILE.parent.mkdir(exist_ok=True)

class WebhookHandler(BaseHTTPRequestHandler):
    received_requests = []

    def log_message(self, format, *args):
        """Override to customize logging"""
        message = f"{self.log_date_time_string()} - {format % args}"
        print(message)
        with open(LOG_FILE, 'a') as f:
            f.write(message + '\n')

    def do_POST(self):
        """Handle POST requests"""
        content_length = int(self.headers.get('Content-Length', 0))
        body = self.rfile.read(content_length).decode('utf-8')
        
        # Log request details
        request_data = {
            'timestamp': datetime.now().isoformat(),
            'path': self.path,
            'headers': dict(self.headers),
            'body': body
        }
        
        # Parse JSON if possible
        try:
            request_data['body_json'] = json.loads(body)
        except json.JSONDecodeError:
            pass
        
        # Store request
        self.received_requests.append(request_data)
        
        # Log to file
        with open(LOG_FILE, 'a') as f:
            f.write('\n' + '='*80 + '\n')
            f.write('WEBHOOK RECEIVED\n')
            f.write('='*80 + '\n')
            f.write(json.dumps(request_data, indent=2))
            f.write('\n')
        
        # Print to console
        print(f"\n{'='*80}")
        print("WEBHOOK RECEIVED")
        print('='*80)
        print(f"Path: {self.path}")
        print(f"Headers: {json.dumps(dict(self.headers), indent=2)}")
        print(f"Body: {body[:500]}...")  # First 500 chars
        
        if 'body_json' in request_data:
            print("\nParsed JSON:")
            print(json.dumps(request_data['body_json'], indent=2))
        
        # Send response
        self.send_response(200)
        self.send_header('Content-Type', 'application/json')
        self.end_headers()
        
        response = {
            'status': 'success',
            'message': 'Webhook received',
            'timestamp': datetime.now().isoformat()
        }
        self.wfile.write(json.dumps(response).encode())

    def do_GET(self):
        """Handle GET requests - return status and received webhooks"""
        if self.path == '/status':
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.end_headers()
            
            response = {
                'status': 'running',
                'port': PORT,
                'received_count': len(self.received_requests),
                'uptime': 'N/A'
            }
            self.wfile.write(json.dumps(response).encode())
        
        elif self.path == '/requests':
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.end_headers()
            
            self.wfile.write(json.dumps({
                'count': len(self.received_requests),
                'requests': self.received_requests
            }, indent=2).encode())
        
        else:
            self.send_response(404)
            self.end_headers()

def run_server():
    """Start the mock webhook server"""
    server_address = ('', PORT)
    httpd = HTTPServer(server_address, WebhookHandler)
    
    print(f"""
╔════════════════════════════════════════════════════════════╗
║         Mock Webhook Server for Notification Forwarder     ║
╚════════════════════════════════════════════════════════════╝

Server running on port {PORT}
Logs will be saved to: {LOG_FILE}

Endpoints:
  POST /*           - Receive webhooks
  GET  /status      - Server status
  GET  /requests    - List received requests

To test from Android device, use server's IP address:
  http://<SERVER_IP>:{PORT}/webhook

Press Ctrl+C to stop the server
""")
    
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\n\nShutting down server...")
        print(f"Total requests received: {len(WebhookHandler.received_requests)}")
        print(f"Logs saved to: {LOG_FILE}")
        httpd.shutdown()
        sys.exit(0)

if __name__ == '__main__':
    run_server()

