import argparse
import json
import os
import re
from typing import List, Dict, Any

try:
    from docling.document_converter import DocumentConverter
    from docling.datamodel.pipeline_options import PdfPipelineOptions
except Exception as exc:  # pragma: no cover
    raise RuntimeError("docling is required for docling_extract.py: pip install docling") from exc


def to_markdown(pdf_path: str) -> (str, Dict[str, Any]):
    # Disable OCR to keep it fast; docling will still parse text-based PDFs.
    pipeline_options = PdfPipelineOptions(do_ocr=False, extract_images=False)
    converter = DocumentConverter(pipeline_options=pipeline_options)
    result = converter.convert(pdf_path)
    md = result.document.export_to_markdown()
    meta = {}
    # best-effort metadata extraction
    try:
        meta = result.document.metadata.__dict__  # type: ignore[attr-defined]
    except Exception:
        meta = {}
    return md, meta


def split_sections(markdown: str):
    sections = []
    current = {"title": "preamble", "content": []}
    for line in markdown.splitlines():
        if line.startswith("#"):
            if current["content"]:
                sections.append(current)
            heading = line.lstrip("#").strip()
            current = {"title": heading if heading else "untitled", "content": []}
        else:
            current["content"].append(line)
    if current["content"]:
        sections.append(current)
    return sections


def first_paragraph(lines: List[str]) -> str:
    paras = []
    buffer = []
    for line in lines:
        if line.strip():
            buffer.append(line.strip())
        elif buffer:
            paras.append(" ".join(buffer))
            buffer = []
    if buffer:
        paras.append(" ".join(buffer))
    return paras[0] if paras else ""


def find_section(sections, keywords: List[str]) -> str:
    for sec in sections:
        title = sec.get("title", "").lower()
        if any(kw in title for kw in keywords):
            return first_paragraph(sec.get("content", []))
    return ""


def extract_structured(markdown: str, metadata: Dict[str, Any]) -> Dict[str, Any]:
    sections = split_sections(markdown)
    title = metadata.get("title") or (sections[0]["title"] if sections else "")
    authors = metadata.get("authors") or metadata.get("author") or []
    if isinstance(authors, str):
        authors = [authors]

    abstract = find_section(sections, ["abstract"])
    introduction = find_section(sections, ["introduction", "intro"])
    conclusion = find_section(sections, ["conclusion", "summary", "future work", "discussion"])

    section_snippets = []
    for sec in sections:
        if not sec.get("title"):
            continue
        snippet = first_paragraph(sec.get("content", []))
        if snippet:
            section_snippets.append({"title": sec["title"], "first_paragraph": snippet})

    # Keep a short markdown snippet to provide extra context if needed
    flat_text = "\n".join(markdown.splitlines()[:200])

    return {
        "title": title or "",
        "authors": authors if isinstance(authors, list) else [],
        "abstract": abstract,
        "introduction": introduction,
        "conclusion": conclusion,
        "sections": section_snippets,
        "markdown_head": flat_text,
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--input_pdf", required=True)
    parser.add_argument("--output_json", required=True)
    args = parser.parse_args()

    os.makedirs(os.path.dirname(args.output_json), exist_ok=True)

    md, meta = to_markdown(args.input_pdf)
    data = extract_structured(md, meta)

    with open(args.output_json, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

    print(f"Docling extraction saved to {args.output_json}")


if __name__ == "__main__":
    main()
