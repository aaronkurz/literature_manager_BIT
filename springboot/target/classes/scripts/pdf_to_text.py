import argparse
import fitz
import os
import pytesseract
import sys
from PIL import Image


def pdftotxt(input_dir, output_dir, pytesseract_dir):
    # 设置Tesseract路径（跨平台兼容）
    tesseract_exe = 'tesseract.exe' if sys.platform == 'win32' else 'tesseract'
    pytesseract.pytesseract.tesseract_cmd = os.path.join(pytesseract_dir, tesseract_exe)

    for filename in os.listdir(input_dir):
        if not filename.lower().endswith('.pdf'):
            continue

        pdf_path = os.path.join(input_dir, filename)
        txt_filename = os.path.splitext(filename)[0] + '.txt'
        txt_path = os.path.join(output_dir, txt_filename)

        if os.path.exists(txt_path):
            print(f"跳过已存在文件: {filename}")
            continue

        process_pdf(pdf_path, txt_path)


def process_pdf(pdf_path, txt_path):
    text_content = []
    try:
        doc = fitz.open(pdf_path)
        for page_num, page in enumerate(doc):
            page_text = page.get_text()
            if page_text.strip():
                text_content.append(page_text)
            else:
                print(f"OCR处理中: {os.path.basename(pdf_path)} 第{page_num+1}页")
                text_content.append(ocr_page(page))

        save_text(txt_path, text_content)
        print(f"成功转换: {os.path.basename(pdf_path)}")
    except Exception as e:
        print(f"处理失败: {os.path.basename(pdf_path)} 错误: {str(e)}")


def ocr_page(page):
    pix = page.get_pixmap()
    img = Image.frombytes("RGB", [pix.width, pix.height], pix.samples)
    return pytesseract.image_to_string(img, lang='eng')


def save_text(txt_path, content):
    os.makedirs(os.path.dirname(txt_path), exist_ok=True)
    with open(txt_path, 'w', encoding='utf-8') as f:
        f.write('\n'.join(content).replace('\n', ''))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('--input_dir', required=True)
    parser.add_argument('--output_dir', required=True)
    parser.add_argument('--pytesseract_dir', required=True)
    args = parser.parse_args()

    pdftotxt(args.input_dir, args.output_dir, args.pytesseract_dir)
