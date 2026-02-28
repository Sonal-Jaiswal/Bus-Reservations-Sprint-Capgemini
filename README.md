# Bus Cleaning Pipeline (Core Java)

This project demonstrates end-to-end **data cleaning and transformation** for bus reservations using Core Java.

It includes:
- Dirty raw dataset generation (`300` records)
- Rule-based cleaning pipeline
- Invalid record flagging with reason codes
- Aggregated reporting for analytics

## Project Structure

```
bus-cleaning
├── src/main/java/com/bus/cleaning
│   ├── app
│   │   └── Application.java
│   ├── config
│   │   ├── AppConfig.java
│   │   └── DbConfig.java
│   ├── model
│   │   └── Booking.java
│   ├── repository
│   │   ├── BookingReader.java
│   │   ├── CsvBookingReader.java
│   │   ├── BookingWriter.java
│   │   ├── CsvBookingWriter.java
│   │   └── DbBookingRepository.java
│   ├── service
│   │   ├── CleaningPipeline.java
│   │   ├── DuplicateService.java
│   │   └── AggregationService.java
│   └── rules
│       ├── CleaningRule.java
│       ├── NameNormalizationRule.java
│       ├── NumericValidationRule.java
│       ├── DateStandardizationRule.java
│       ├── StatusNormalizationRule.java
│       ├── CodeMappingRule.java
│       └── DerivedFieldsRule.java
├── src/main/resources
│   ├── application.properties
│   └── log4j2.xml
├── data/raw_bookings.csv
├── output/
├── logs/
├── DEMO_PRESENTATION_NOTE.md
└── generate_dataset.py
```

## Dataset & Output Summary

- Raw records: **300**
- Clean/transformed records: **195 (65%)**
- Invalid/flagged records: **105 (35%)**

Generated files:
- `data/raw_bookings.csv`
- `output/cleaned_bookings.csv`
- `output/invalid_bookings.csv`
- `output/aggregated_report.csv`

## Cleaning Rules Implemented

1. **Remove duplicates** (`booking_id` uniqueness)
2. **Normalize names** (trim + proper case)
3. **Validate numeric fields** (`age`, `seats`, `amount`)
4. **Standardize dates** (multiple formats → `yyyy-MM-dd`)
5. **Map codes** (`AC/NAC/SL/VOL` → full labels)
6. **Normalize status** (`cnf/confirm/cancel/...` → standard values)
7. **Flag invalid records** with reason code
8. **Derived fields** (`amount_per_seat`, `age_category`)
9. **Aggregation** by route and status
10. **Categorization** into age buckets (`MINOR/ADULT/SENIOR`)

Invalid reasons currently covered in output:
- `DUPLICATE_ID`
- `INVALID_NAME`
- `INVALID_NUMERIC`
- `INVALID_DATE`
- `INVALID_CODE`
- `INVALID_STATUS`

## Logging (Everything We Do With Data)

This project now logs all major data operations:
- Pipeline start/end
- Raw file load count
- Per-record processing (`booking_id`)
- Rule pass/fail at each stage
- Exact invalid rejection reason
- Output file write counts

Log outputs:
- Console logs (runtime)
- File logs: `logs/cleaning.log`

To verify logs quickly:

```bash
tail -n 50 logs/cleaning.log
```

## Check / Verification

After running the app, check:
- `output/cleaned_bookings.csv` has transformed valid records
- `output/invalid_bookings.csv` contains multiple reason types
- `output/aggregated_report.csv` has grouped summary by `ROUTE` and `STATUS`
- `logs/cleaning.log` contains step-by-step execution trace

## How to Run

From project root:

1) Generate raw dirty dataset

```bash
/opt/homebrew/bin/python3 generate_dataset.py
```

2) Compile and run cleaning pipeline

```bash
mvn -q compile exec:java
```

## What is `aggregated_report.csv`?

This is a post-cleaning summary file with format:

- `metric` → grouping type (`ROUTE` or `STATUS`)
- `key` → route/status value
- `count` → cleaned record count in that group

It shows the cleaned dataset is analytics-ready after transformation.

## Demo Notes

For presentation mapping of use-cases to implementation, see:

- `DEMO_PRESENTATION_NOTE.md`