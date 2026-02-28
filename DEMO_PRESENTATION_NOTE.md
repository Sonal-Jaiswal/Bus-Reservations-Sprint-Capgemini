# Bus Reservations Data Cleaning – One-Page Demo Note

## Dataset Snapshot
- Raw input file: `data/raw_booking.csv`
- Total raw records: **300**
- Clean/transformed output: `output/cleaned_bookings.csv` (**195 = 65%**)
- Invalid/flagged output: `output/invalid_bookings.csv` (**105 = 35%**)
- Aggregated output: `output/aggregated_report.csv`

## What We Implemented (10 Use Cases)

| Use Case | What was dirty in raw data | Rule/Logic implemented | Where shown in output |
|---|---|---|---|
| 1. Remove Duplicates | Repeated `booking_id` values | `DuplicateService` uses Set-based uniqueness | `invalid_bookings.csv` with `DUPLICATE_ID` |
| 2. Normalize Names | Extra spaces, mixed casing | `NameNormalizationRule` does trim + proper case | `cleaned_bookings.csv` `passenger_name` |
| 3. Fix Numeric Fields | Invalid age/seats/amount | `NumericValidationRule` validates range and parse | `invalid_bookings.csv` with `INVALID_NUMERIC` |
| 4. Standardize Dates | Mixed formats and invalid dates | `DateStandardizationRule` parses multiple patterns to `yyyy-MM-dd` | `cleaned_bookings.csv` `travel_date`; invalid as `INVALID_DATE` |
| 5. Map Codes | Short/unknown bus codes | `CodeMappingRule` maps `AC/NAC/SL/VOL` to full labels | `cleaned_bookings.csv` `bus_code` + `bus_type`; invalid as `INVALID_CODE` |
| 6. Validate Status | Variants like `cnf`, `confirm`, `cancel` and bad statuses | `StatusNormalizationRule` maps to `CONFIRMED/PENDING/CANCELLED` | `cleaned_bookings.csv` `status`; invalid as `INVALID_STATUS` |
| 7. Flag Invalid Records | Any rule failure | Pipeline logs reason and skips record | `output/invalid_bookings.csv` |
| 8. Derived Fields | Missing computed attributes | `DerivedFieldsRule` computes `amount_per_seat` and `age_category` | `cleaned_bookings.csv` new derived columns |
| 9. Aggregation | Need grouped summary | `AggregationService` with grouping-by route and status | `output/aggregated_report.csv` |
| 10. Data Categorization | Need buckets for explanation | Age bucket mapping (`MINOR/ADULT/SENIOR`) | `cleaned_bookings.csv` `age_category` |

## Invalid Reason Coverage (for demo)
- `DUPLICATE_ID`: 20
- `INVALID_NAME`: 15
- `INVALID_NUMERIC`: 20
- `INVALID_DATE`: 15
- `INVALID_CODE`: 15
- `INVALID_STATUS`: 20

This confirms **all major dirty data categories** are present and handled.

## What `aggregated_report.csv` Means
This file is a post-cleaning summary generated from only valid transformed records.

Format:
- `metric`: group type (`ROUTE` or `STATUS`)
- `key`: specific route/status value
- `count`: number of cleaned bookings in that group

Current sample values:
- Routes: `DEL-JPR=37`, `KOL-BBS=39`, `BLR-HYD=41`, `CHE-BLR=40`, `MUM-PUN=38`
- Statuses: `CANCELLED=36`, `CONFIRMED=77`, `PENDING=82`

So it demonstrates that after cleaning, the dataset is ready for reporting/analytics.