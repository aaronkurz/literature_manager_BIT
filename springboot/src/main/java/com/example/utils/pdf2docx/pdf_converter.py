# -*- coding: utf-8 -*-
import os
import sys
import time
from pdf2docx import Converter


def progress_monitor(current, total):
    """Real-time progress callback"""
    print(f"PROGRESS|{current}|{total}")
    sys.stdout.flush()  # Force immediate flush


def convert_pdf_to_docx(pdf_path, docx_path, timeout_minutes=30):
    start_time = time.time()  # Record start time
    try:
        # Get filename (without path)
        pdf_filename = os.path.basename(pdf_path)
        print(f"DEBUG|Input path: {pdf_path}")
        print(f"DEBUG|Extracted filename: {pdf_filename}")

        # Check if filename starts with 'paper'
        if not pdf_filename.lower().startswith("paper"):
            print(f"Skipping: filename {pdf_filename} does not start with 'paper'")
            return 3  # Return 3 to indicate skipped non-'paper' file

        print(f"DEBUG|Filename starts with 'paper', preparing conversion: {pdf_filename}")

        # Check if output file already exists
        if os.path.exists(docx_path):
            print(f"Skipping: output file {docx_path} already exists")
            return 0

        cv = Converter(pdf_path)
        cv.convert(docx_path, start=0, end=None, progress_callback=progress_monitor)
        cv.close()
        print(f"Conversion successful: {docx_path}")
        return 0
    except Exception as e:
        print(f"Error: {str(e)}")
        return 1
    finally:
        # Check for timeout
        elapsed_time = time.time() - start_time
        if elapsed_time > timeout_minutes * 60:
            print("Error: conversion timed out")
            return 2


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python pdf_converter.py <input_pdf> <output_docx>")
        sys.exit(1)

    exit_code = convert_pdf_to_docx(sys.argv[1], sys.argv[2])
    print(f"DEBUG|Exit code: {exit_code}")
    sys.exit(exit_code)