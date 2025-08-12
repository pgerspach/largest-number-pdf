# PDF Largest Number Extractor

A Play Framework application that finds the largest numerical value within PDF files, including support for text-based numbers with multipliers (e.g., "2.5 million").

## What It Does

This application extracts the largest numerical value from PDF documents by:

1. **Regular numbers**: Standard digits (e.g., 12345, 98.7)
2. **Text multipliers**: Numbers with written multipliers like:
   - "2.5 million" → 2,500,000
   - "1.2 billion" → 1,200,000,000
   - "500 thousand" → 500,000
   - Short forms: "5k", "10m", "2b", "1t"

## Prerequisites

- Java 11 or higher
- SBT (Scala Build Tool)

## How to Run

1. **Start the application**:
   ```bash
   sbt run
   ```

2. **The server will start on port 9000** (http://localhost:9000)

## How to Use

### API Endpoint

**GET** `/largest_number?file={filename}`

- **filename**: Name of the PDF file to analyze (must be in `app/pdf_files/` directory)

### Examples

```bash
# Using curl
curl "http://localhost:9000/largest_number?file=document.pdf"

# Using a browser
http://localhost:9000/largest_number?file=document.pdf
```

### Response Format

- **Success**: `"The largest number found in the PDF is: 2500000"`
- **No file**: `"File name is required."`
- **No numbers found**: `"No numbers found in the PDF."`
- **Error**: `"Failed to read PDF file."`

## Adding PDF Files

Place your PDF files in the `app/pdf_files/` directory. The application currently includes:
- `FY25_Air_Force_Working_Capital_Fund.pdf`

## Technical Details

### Architecture
- **Framework**: Play Framework 2.8 with Scala 2.13
- **PDF Processing**: Apache PDFBox 3.0.5
- **Pattern Matching**: Regular expressions for text multiplier detection

### Number Detection
- Supports decimal numbers with multipliers
- Case-insensitive matching
- Handles both full words ("million") and abbreviations ("m")
- Processes line by line for memory efficiency

### Supported Multipliers
| Text | Abbreviation | Value |
|------|-------------|-------|
| thousand | k | 1,000 |
| million | m | 1,000,000 |
| billion | b | 1,000,000,000 |
| trillion | t | 1,000,000,000,000 |

## Development

### Running Tests
```bash
sbt test
```

### Building
```bash
sbt compile
```

## Project Structure
```
app/
├── controllers/HomeController.scala    # HTTP request handling
├── services/PdfService.scala          # PDF processing logic
├── pdf_files/                         # PDF storage directory
└── views/                             # HTML templates
```