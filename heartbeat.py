import time
import sys
import requests # The only dependency you need

# Configuration
KERNEL_URL = "http://localhost:8080"

def check_pulse():
    print(f"\n{'-'*50}")
    print("🔌 CONNECTING TO KERNX ENGINE...")
    print(f"{'-'*50}")
    
    # 1. Check if Java Engine is Running
    try:
        # Simple ping to your HTTP Adapter
        response = requests.get(f"{KERNEL_URL}/health") 
        if response.status_code != 200:
            print("❌ KERNEL IS UNRESPONSIVE. (Is the Java JAR running?)")
            sys.exit(1)
    except requests.exceptions.ConnectionError:
        print("❌ CONNECTION FAILED.")
        print("   -> Please start the engine first: java -jar kernx-engine.jar")
        sys.exit(1)

    print("✅ CONNECTION ESTABLISHED [LOCALHOST:8080]")
    
    # 2. Deploy a Test Agent
    print("\n🚀 DEPLOYING 'PROBE-01'...")
    try:
        # Assuming your API has a /deploy endpoint
        payload = {"agent_id": "probe-01", "strategy": "deterministic"}
        res = requests.post(f"{KERNEL_URL}/deploy", json=payload)
        print(f"   STATUS: {res.status_code} {res.reason}")
    except Exception as e:
        print(f"   ⚠️  DEPLOY WARNING: {e}")

    # 3. Stream Life (The Real Loop)
    print("\n❤️  STREAMING VITAL SIGNS (Ctrl+C to stop)...")
    print(f"{'-'*50}")
    
    start_time = time.time()
    try:
        while True:
            # Fetch real stats from the engine
            stats = requests.get(f"{KERNEL_URL}/stats").json()
            
            # Extract metrics (adjust keys based on your actual JSON response)
            reqs = stats.get('throughput', 0)
            agents = stats.get('active_agents', 0)
            uptime = int(time.time() - start_time)
            
            # The "Medical Monitor" Output
            sys.stdout.write(f"\r[T+{uptime}s] THROUGHPUT: {reqs:,} req/s | AGENTS: {agents} | STATUS: ALIVE")
            sys.stdout.flush()
            time.sleep(1)
            
    except KeyboardInterrupt:
        print("\n\n🛑 HEARTBEAT STOPPED.")
        print("   The system remains active.")

if __name__ == "__main__":
    check_pulse()