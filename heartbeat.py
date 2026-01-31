import time
import requests
import sys

# CONFIG: Matches your Java Engine
KERNEL_URL = "http://127.0.0.1:8080"

def check_pulse():
    print("-" * 60)
    print("🔌 CONNECTING TO KERNX ENGINE...")
    print("-" * 60)

    # 1. Initial Handshake
    try:
        # Get baseline stats
        start_stats = requests.get(f"{KERNEL_URL}/stats").json()
        print(f"✅ CONNECTION ESTABLISHED [{KERNEL_URL}]")
        print(f"   (Engine Uptime: {start_stats.get('uptime')}s)")
    except Exception:
        print("❌ CONNECTION REFUSED. Is the engine running?")
        sys.exit(1)

    print("\n❤️  STREAMING REAL-TIME VELOCITY (Ctrl+C to stop)...")
    print("-" * 60)
    
    # Trackers for calculating Delta
    prev_count = start_stats.get("throughput", 0) * start_stats.get("uptime", 1) # Reverse engineer total count
    # Actually, let's just assume we start tracking NOW.
    # To do this accurately without changing Java, we need to poll the raw counter.
    # Since Java returns 'throughput' (avg), we can't get the raw count easily unless we tracked it differently.
    
    # WAIT. The Java code calculates rps = count / uptime.
    # We can reverse it: Total_Count = rps * uptime.
    
    while True:
        try:
            start_time = time.time()
            
            # Fetch Stats
            response = requests.get(f"{KERNEL_URL}/stats").json()
            uptime = response.get("uptime", 1)
            avg_rps = response.get("throughput", 0)
            
            # Reverse Calculate Total Requests (Approximate)
            current_total = avg_rps * uptime
            
            # Calculate Instant Delta
            # (This is rough because Java integers round down, but it shows the spikes)
            instant_rps = current_total - prev_count
            if instant_rps < 0: instant_rps = 0 # Handle jitter
            
            # Update previous
            prev_count = current_total
            
            # The HUD
            # We show BOTH: The solid Average and the Instant Spike
            print(f"[T+{uptime}s] INSTANT: {int(instant_rps):>5} req/s  |  AVG: {avg_rps:>4} req/s  |  STATUS: FLOWING 🟢")
            
            time.sleep(1)
            
        except KeyboardInterrupt:
            print("\n🛑 MONITOR STOPPED.")
            sys.exit(0)
        except Exception as e:
            print(f"⚠️ GLITCH: {e}")
            time.sleep(1)

if __name__ == "__main__":
    check_pulse()