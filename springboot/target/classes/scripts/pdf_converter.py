# -*- coding: utf-8 -*-
import os
import sys
import time
from pdf2docx import Converter


def progress_monitor(current, total):
    """ 实时进度回调 """
    print(f"PROGRESS|{current}|{total}")
    sys.stdout.flush()  # 强制立即输出


def convert_pdf_to_docx(pdf_path, docx_path, timeout_minutes=30):
    start_time = time.time()  # 记录开始时间
    try:
        # 获取文件名（不含路径）
        pdf_filename = os.path.basename(pdf_path)
        print(f"DEBUG|输入路径: {pdf_path}")
        print(f"DEBUG|提取文件名: {pdf_filename}")

        # 检查文件名是否以 "paper" 开头
        if not pdf_filename.lower().startswith("paper"):
            print(f"跳过转换：文件名 {pdf_filename} 不以 'paper' 开头")
            return 3  # 返回 3 表示跳过非 "paper" 开头的文件

        print(f"DEBUG|文件名以 'paper' 开头，准备转换: {pdf_filename}")

        # 检查输出文件是否已存在
        if os.path.exists(docx_path):
            print(f"跳过转换：输出文件 {docx_path} 已存在")
            return 0

        cv = Converter(pdf_path)
        cv.convert(docx_path, start=0, end=None, progress_callback=progress_monitor)
        cv.close()
        print(f"转换成功: {docx_path}")
        return 0
    except Exception as e:
        print(f"错误：{str(e)}")
        return 1
    finally:
        # 检查是否超时
        elapsed_time = time.time() - start_time
        if elapsed_time > timeout_minutes * 60:
            print("错误：转换超时终止")
            return 2


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python pdf_converter.py <input_pdf> <output_docx>")
        sys.exit(1)

    exit_code = convert_pdf_to_docx(sys.argv[1], sys.argv[2])
    print(f"DEBUG|退出码: {exit_code}")
    sys.exit(exit_code)
