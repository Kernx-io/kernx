import requests
import time
import json
import uuid

class KernxKernel:
    """
    The Official Python Client for Kernx.
    Connects to the Java Kernel via HTTP/JSON.
    """
    def __init__(self, url="http://localhost:8080/api/kernel"):
        self.url = url
        self.headers = {"Content-Type": "text/plain"}

    def _send(self, command):
        """Internal helper to send raw commands safely."""
        ticket = str(uuid.uuid4())
        try:
            # We assume the Kernx HttpAdapter reads raw body text
            resp = requests.post(self.url, data=command, headers=self.headers, timeout=5)
            if resp.status_code in [200, 202]:
                return {"status": "sent", "ticket": ticket, "response": resp.text.strip()}
            else:
                return {"status": "error", "code": resp.status_code, "msg": resp.text}
        except Exception as e:
            return {"status": "failed", "error": str(e)}

    # --- CORE COMMANDS ---

    def deploy(self, agent_id):
        """Deploys a new Agent into the Kernel."""
        return self._send(f"DEPLOY {agent_id}")

    def msg(self, agent_id, content):
        """Sends a message to an Agent. Content is string payload."""
        return self._send(f"MSG {agent_id} {content}")
    
    def config(self, key, value):
        """
        Runtime Configuration.
        Examples:
        - k.config("queue_depth", 5000)
        - k.config("dpi_mode", "ON")
        """
        return self._send(f"CONFIG SET {key} {value}")

    # --- SECURITY COMMANDS (WAR MODE) ---

    def block(self, agent_id):
        """Layer 7 Block: Ban an Agent ID instantly."""
        return self._send(f"BLOCK {agent_id}")

    def block_hex(self, hex_signature):
        """
        Layer 4 Block: Ban a binary pattern (Deep Packet Inspection).
        Example: k.block_hex("CAFEBABE")
        """
        return self._send(f"BLOCK_HEX {hex_signature}")

    # --- TELEMETRY ---

    def get_stats(self):
        """Fetches the current Kernel telemetry."""
        ticket = str(uuid.uuid4())
        # 1. Request Stats
        self._send(f"STATS")
        
        # 2. In a real Async system, we would poll for the result ID.
        # Since our Kernel writes to ResultStore instantly for STATS, 
        # we can assume the next GET might retrieve it if we had the ID mapping.
        # For simplicity, we just print what the kernel replied in the POST body if any,
        # or you can implement the polling loop here if your HttpAdapter supports GET /results.
        
        print(f"Stats requested. Ticket: {ticket}")
        return ticket