import csv
import random
from datetime import date, timedelta
from pathlib import Path

random.seed(123)
root = Path(__file__).resolve().parent
raw_path = root / "data" / "raw_bookings.csv"

dirty_names = [
    "  rahul   sharma ",
    "PRIYA  KUMARI",
    "anil  verma",
    "sNeHa reddy",
    "   mohit singh",
    "KAVYA nair",
]
routes = ["BLR-HYD", "DEL-JPR", "MUM-PUN", "CHE-BLR", "KOL-BBS"]
bus_codes = ["ac", "NAC", "sl", "VOL"]
statuses = ["cnf", "PENDING", " cancel ", "confirm", "waiting"]


def mixed_date(index: int) -> str:
    base = date(2026, 3, 1) + timedelta(days=index % 120)
    formats = ["%Y-%m-%d", "%d/%m/%Y", "%m-%d-%Y", "%d-%b-%Y"]
    return base.strftime(formats[index % len(formats)])


rows = []

# 195 cleanable rows
for i in range(1, 196):
    booking_id = f"BK-{2000 + i}"
    seats = random.randint(1, 4)
    amount = random.randint(350, 1800) * seats
    rows.append(
        [
            booking_id,
            random.choice(dirty_names),
            str(random.randint(18, 65)),
            str(seats),
            str(amount),
            mixed_date(i),
            random.choice(bus_codes),
            random.choice(statuses),
            random.choice(routes),
        ]
    )

invalid_rows = []

# 20 duplicate id rows
for i in range(20):
    invalid_rows.append(rows[i].copy())

# 15 invalid names
for i in range(15):
    invalid_rows.append(
        [
            f"BK-{3000 + i}",
            "" if i % 2 == 0 else "   ",
            str(random.randint(20, 55)),
            str(random.randint(1, 4)),
            str(random.randint(500, 3000)),
            mixed_date(200 + i),
            random.choice(bus_codes),
            random.choice(statuses),
            random.choice(routes),
        ]
    )

# 20 invalid numeric
for i in range(20):
    age = "-5" if i % 3 == 0 else ("abc" if i % 3 == 1 else str(random.randint(10, 90)))
    seats = "0" if i % 2 == 0 else "x"
    amount = "-100" if i % 4 == 0 else ("NaN" if i % 4 == 1 else str(random.randint(100, 2000)))
    invalid_rows.append(
        [
            f"BK-{3100 + i}",
            random.choice(dirty_names),
            age,
            seats,
            amount,
            mixed_date(220 + i),
            random.choice(bus_codes),
            random.choice(statuses),
            random.choice(routes),
        ]
    )

# 15 invalid dates
for i in range(15):
    invalid_rows.append(
        [
            f"BK-{3200 + i}",
            random.choice(dirty_names),
            str(random.randint(18, 60)),
            str(random.randint(1, 4)),
            str(random.randint(400, 2500)),
            random.choice(["31/31/2026", "2026/99/01", "40-13-2026", "not-a-date"]),
            random.choice(bus_codes),
            random.choice(statuses),
            random.choice(routes),
        ]
    )

# 15 invalid bus codes
for i in range(15):
    invalid_rows.append(
        [
            f"BK-{3300 + i}",
            random.choice(dirty_names),
            str(random.randint(18, 60)),
            str(random.randint(1, 4)),
            str(random.randint(400, 2500)),
            mixed_date(250 + i),
            random.choice(["XX", "BUS", "??"]),
            random.choice(statuses),
            random.choice(routes),
        ]
    )

# 20 invalid statuses
for i in range(20):
    invalid_rows.append(
        [
            f"BK-{3400 + i}",
            random.choice(dirty_names),
            str(random.randint(18, 60)),
            str(random.randint(1, 4)),
            str(random.randint(400, 2500)),
            mixed_date(280 + i),
            random.choice(bus_codes),
            random.choice(["UNKNOWN", "DONE", "INPROGRESS"]),
            random.choice(routes),
        ]
    )

assert len(invalid_rows) == 105
rows.extend(invalid_rows)
assert len(rows) == 300

with raw_path.open("w", newline="") as file:
    writer = csv.writer(file)
    writer.writerow(
        [
            "booking_id",
            "passenger_name",
            "age",
            "seats",
            "amount",
            "travel_date",
            "bus_code",
            "status",
            "route",
        ]
    )
    writer.writerows(rows)

print(f"Created {len(rows)} rows at {raw_path}")