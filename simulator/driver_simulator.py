import os
import time
import json
import random
import uuid
import urllib.request
from datetime import datetime, timezone

BASE = os.environ.get('SIM_TARGET_BASE_URL', 'http://localhost:8080')
DRIVERS = int(os.environ.get('SIM_DRIVERS', '50'))
EVENTS_PER_SEC = float(os.environ.get('SIM_EVENTS_PER_SEC', '10'))  # total events/sec
DURATION = int(os.environ.get('SIM_DURATION_SECONDS', '0'))  # 0 = infinite

def http_post(path, payload):
    data = json.dumps(payload).encode('utf-8')
    req = urllib.request.Request(BASE + path, data=data, headers={'Content-Type': 'application/json'})
    with urllib.request.urlopen(req, timeout=10) as resp:
        return resp.read().decode('utf-8')

def bootstrap():
    # Bootstrap demo dataset so that orders exist and are in PICKED_UP state.
    payload = {"drivers": DRIVERS, "restaurants": max(5, DRIVERS // 5), "orders": DRIVERS}
    print('[sim] bootstrapping dataset...', payload)
    out = http_post('/internal/demo/bootstrap', payload)
    print('[sim] bootstrap response:', out)

def send_location(driver_id, lat, lng):
    evt = {
        "eventId": str(uuid.uuid4()),
        "lat": lat,
        "lng": lng,
        "heading": random.randint(0, 359),
        "speedMps": round(random.uniform(2.0, 8.0), 2),
        "timestamp": datetime.now(timezone.utc).isoformat().replace('+00:00', 'Z')
    }
    http_post(f'/api/drivers/{driver_id}/location', evt)

def main():
    # Wait a bit for backend startup
    time.sleep(40)
    try:
        bootstrap()
    except Exception as e:
        print('[sim] bootstrap failed:', e)

    # Initialize driver starting points around a small bounding box
    drivers = []
    for i in range(1, DRIVERS + 1):
        drivers.append({
            "id": i,
            "lat": 12.90 + random.random() * 0.10,
            "lng": 77.55 + random.random() * 0.15
        })

    interval = 1.0 / max(1.0, EVENTS_PER_SEC)
    start = time.time()
    sent = 0
    print(f"[sim] starting GPS stream: drivers={DRIVERS}, events/sec={EVENTS_PER_SEC}, interval={interval:.3f}s")

    while True:
        if DURATION > 0 and time.time() - start > DURATION:
            break

        d = random.choice(drivers)
        # Random walk
        d['lat'] += random.uniform(-0.0005, 0.0005)
        d['lng'] += random.uniform(-0.0005, 0.0005)

        try:
            send_location(d['id'], d['lat'], d['lng'])
            sent += 1
        except Exception as e:
            print('[sim] send failed:', e)

        if sent % 50 == 0:
            print(f"[sim] sent {sent} events")

        time.sleep(interval)

    print(f"[sim] finished. total events sent={sent}")

if __name__ == '__main__':
    main()
