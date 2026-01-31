import requests
import threading
import time

# CONFIG: Match the engine exactly
URL = "http://127.0.0.1:8080/api/kernel"

def shock_system():
    # 1. Create a Session (Reuses the TCP connection)
    session = requests.Session()
    adapter = requests.adapters.HTTPAdapter(pool_connections=1, pool_maxsize=1)
    session.mount('http://', adapter)
    
    print("⚡ THREAD STARTED: PIPELINE OPEN...")
    payload = {"agent": "probe-01", "msg": "WAKE_UP"}
    
    # 2. Hammer the engine using the same connection
    # Increased count to 20,000 per thread for a longer test
    for _ in range(20000):
        try:
            # timeout is critical so we don't hang on lost packets
            session.post(URL, json=payload, timeout=1)
        except Exception as e:
            pass
            
    print("✅ THREAD COMPLETE")

print(f"🔌 TARGET: {URL}")
print("⚡ INITIATING HIGH-SPEED SHOCK THERAPY...")

threads = []
# Launch 10 parallel threads
for i in range(10):
    t = threading.Thread(target=shock_system)
    t.start()
    threads.append(t)

for t in threads:
    t.join()

print("🏁 CLEAR. SYSTEM SHOCKED.")