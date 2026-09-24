import requests
import statistics

BASE_URL = "http://localhost:8081/api"

# ============================================
# STEP 1: Login to get JWT token
# ============================================
print("🔐 Logging in...")

# ⚠️ CHANGE THIS with your real credentials
USERNAME = "boukadi100"
PASSWORD = "99191060"

login_response = requests.post(
    f"{BASE_URL}/auth/login",
    json={"username": USERNAME, "password": PASSWORD}
)

if login_response.status_code != 200:
    print(f"❌ Login failed: {login_response.status_code}")
    print(f"   Response: {login_response.text}")
    exit(1)

token_data = login_response.json()
token = token_data.get("token") or token_data.get("accessToken") or token_data.get("jwt")

if not token:
    print(f"❌ No token in response: {token_data}")
    exit(1)

print(f"✅ Token obtained: {token[:30]}...")

# Headers with JWT for all subsequent requests
headers = {"Authorization": f"Bearer {token}"}

# ============================================
# STEP 2: Fetch all ONTs (try multiple endpoints)
# ============================================
print("\n📥 Fetching ONTs...")

possible_endpoints = [
    "/inventory/onts/paginated?page=0&size=1000",
    "/inventory/onts?size=1000",
    "/inventory/onts",
    "/onts",
]

onts = None
working_endpoint = None

for endpoint in possible_endpoints:
    url = f"{BASE_URL}{endpoint}"
    try:
        r = requests.get(url, headers=headers, timeout=10)
        print(f"   Testing {url} → Status {r.status_code}")
        if r.status_code == 200:
            data = r.json()
            if isinstance(data, list):
                onts = data
            elif isinstance(data, dict):
                onts = data.get("content", data.get("data", data.get("onts", [])))
            
            if onts and len(onts) > 0:
                working_endpoint = url
                print(f"   ✅ Found working endpoint: {url}")
                break
    except Exception as e:
        print(f"   ❌ {url} → {e}")

if not onts:
    print("\n❌ No working endpoint found.")
    exit(1)

print(f"\n📊 Total ONTs fetched: {len(onts)}")

# ============================================
# STEP 3: Train ML model first (important!)
# ============================================
print("\n🤖 Training ML model...")
train_response = requests.post(f"{BASE_URL}/ml/train", timeout=30)
print(f"   Train status: {train_response.status_code}")

# ============================================
# STEP 4: Score each ONT
# ============================================
print("\n🔍 Scoring ONTs...")

categories = {"healthy": [], "degraded": [], "critical": []}
all_scores = []

for i, ont in enumerate(onts):
    ont_id = ont.get("id") or ont.get("ontId")
    if not ont_id:
        continue
    
    try:
        r = requests.get(f"{BASE_URL}/ml/anomaly/{ont_id}", timeout=5)
        if r.status_code != 200:
            continue
        data = r.json()
        score = data.get("score", 0)
        rx = ont.get("rxPower", 0) or 0
        
        all_scores.append(score)
        
        if rx > -20:
            categories["healthy"].append(score)
        elif rx > -25:
            categories["degraded"].append(score)
        else:
            categories["critical"].append(score)
    except Exception as e:
        pass
    
    if (i + 1) % 50 == 0:
        print(f"   Processed {i+1}/{len(onts)}")

# ============================================
# STEP 5: Print results
# ============================================
print("\n" + "="*50)
print("📊 DISTRIBUTION DES SCORES PAR CATÉGORIE")
print("="*50)

for cat, scores in categories.items():
    if scores:
        print(f"\n{cat.upper()}:")
        print(f"   Nombre:  {len(scores)}")
        print(f"   Moyenne: {statistics.mean(scores):.2f}")
        print(f"   Min:     {min(scores):.2f}")
        print(f"   Max:     {max(scores):.2f}")
    else:
        print(f"\n{cat.upper()}: no data")

print("\n" + "="*50)
print("📊 STATISTIQUES GLOBALES")
print("="*50)
if all_scores:
    print(f"Total scores:  {len(all_scores)}")
    print(f"Score min:     {min(all_scores):.2f}")
    print(f"Score max:     {max(all_scores):.2f}")
    print(f"Score moyenne: {statistics.mean(all_scores):.2f}")

# ============================================
# STEP 6: Save results to file for report
# ============================================
with open("ml_evaluation_results.txt", "w") as f:
    f.write("=== ML Evaluation Results ===\n\n")
    for cat, scores in categories.items():
        if scores:
            f.write(f"{cat}: mean={statistics.mean(scores):.2f}, "
                    f"min={min(scores):.2f}, max={max(scores):.2f}, n={len(scores)}\n")
    f.write(f"\nGlobal: min={min(all_scores):.2f}, max={max(all_scores):.2f}, "
            f"mean={statistics.mean(all_scores):.2f}\n")

print("\n✅ Results saved to ml_evaluation_results.txt")