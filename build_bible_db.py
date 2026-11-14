#!/usr/bin/env python3
"""
build_bible_db.py - Fase 1: Ingeniería de Datos, Extracción Híbrida del PDF y Motor SQLite/FTS4
Genera: app/src/main/assets/database/bible_hymns_choruses.db

Fuentes:
1. Biblia Reina Valera 1960 (RVR1960 - 66 libros, 1,189 capítulos, ~31,104 versículos)
   desde https://github.com/thiagobodruk/bible (es_rvr1960.json) / josevladimir/bible-json.
2. Himnario y Corario "Melodías Celestiales" (Asambleas de Dios) desde el PDF oficial:
   - Himnos (type='HYMN'): Páginas 2 a 225, excluyendo estrictamente las páginas 212 y 213
     (y omitiendo páginas 226 a 247 de índices).
   - Coros (type='CHORUS'): Páginas 248 a 334, segmentando múltiples coros numerados por página.
"""

from __future__ import annotations

import io
import json
import os
import re
import sqlite3
import time
import unicodedata
import urllib.request
from concurrent.futures import ProcessPoolExecutor
from pathlib import Path
from typing import Dict, List, Optional, Tuple

import fitz  # PyMuPDF
from PIL import Image

try:
    import pytesseract
except ImportError:
    pytesseract = None

try:
    from rapidocr_onnxruntime import RapidOCR
except ImportError:
    RapidOCR = None

# ---------------------------------------------------------------------------
# Paths & Constants
# ---------------------------------------------------------------------------
PROJECT_ROOT = Path(__file__).resolve().parent
DB_OUTPUT_PATH = PROJECT_ROOT / "app" / "src" / "main" / "assets" / "database" / "bible_hymns_choruses.db"
PDF_DEFAULT_PATH = Path("/tmp/pdf-melodias-celestiales-asambleas-de-dios.pdf")
OCR_CACHE_PATH = Path("/tmp/pdf_ocr_cache.json")
RVR1960_JSON_PATH = Path("/tmp/thiagobodruk-bible/json/es_rvr1960.json")
RVR1960_FALLBACK_URL = "https://raw.githubusercontent.com/thiagobodruk/bible/master/json/es_rvr1960.json"
TESSDATA_DIR = Path("/tmp/tessdata")

# Canonical RVR1960 66 Books Metadata (ID, Testament, Spanish Name, Standard Abbreviation)
CANONICAL_BOOKS: List[Tuple[int, str, str, str]] = [
    # Antiguo Testamento (39 libros)
    (1, "AT", "Génesis", "Gén"),
    (2, "AT", "Éxodo", "Éx"),
    (3, "AT", "Levítico", "Lev"),
    (4, "AT", "Números", "Núm"),
    (5, "AT", "Deuteronomio", "Deut"),
    (6, "AT", "Josué", "Jos"),
    (7, "AT", "Jueces", "Jue"),
    (8, "AT", "Rut", "Rut"),
    (9, "AT", "1 Samuel", "1 Sam"),
    (10, "AT", "2 Samuel", "2 Sam"),
    (11, "AT", "1 Reyes", "1 Rey"),
    (12, "AT", "2 Reyes", "2 Rey"),
    (13, "AT", "1 Crónicas", "1 Cró"),
    (14, "AT", "2 Crónicas", "2 Cró"),
    (15, "AT", "Esdras", "Esd"),
    (16, "AT", "Nehemías", "Neh"),
    (17, "AT", "Ester", "Est"),
    (18, "AT", "Job", "Job"),
    (19, "AT", "Salmos", "Sal"),
    (20, "AT", "Proverbios", "Prov"),
    (21, "AT", "Eclesiastés", "Ecl"),
    (22, "AT", "Cantares", "Cant"),
    (23, "AT", "Isaías", "Isa"),
    (24, "AT", "Jeremías", "Jer"),
    (25, "AT", "Lamentaciones", "Lam"),
    (26, "AT", "Ezequiel", "Eze"),
    (27, "AT", "Daniel", "Dan"),
    (28, "AT", "Oseas", "Ose"),
    (29, "AT", "Joel", "Joel"),
    (30, "AT", "Amós", "Amós"),
    (31, "AT", "Abdías", "Abd"),
    (32, "AT", "Jonás", "Jon"),
    (33, "AT", "Miqueas", "Miq"),
    (34, "AT", "Nahúm", "Nah"),
    (35, "AT", "Habacuc", "Hab"),
    (36, "AT", "Sofonías", "Sof"),
    (37, "AT", "Hageo", "Hag"),
    (38, "AT", "Zacarías", "Zac"),
    (39, "AT", "Malaquías", "Mal"),
    # Nuevo Testamento (27 libros)
    (40, "NT", "Mateo", "Mat"),
    (41, "NT", "Marcos", "Mar"),
    (42, "NT", "Lucas", "Luc"),
    (43, "NT", "Juan", "Juan"),
    (44, "NT", "Hechos", "Hch"),
    (45, "NT", "Romanos", "Rom"),
    (46, "NT", "1 Corintios", "1 Cor"),
    (47, "NT", "2 Corintios", "2 Cor"),
    (48, "NT", "Gálatas", "Gál"),
    (49, "NT", "Efesios", "Efe"),
    (50, "NT", "Filipenses", "Fil"),
    (51, "NT", "Colosenses", "Col"),
    (52, "NT", "1 Tesalonicenses", "1 Tes"),
    (53, "NT", "2 Tesalonicenses", "2 Tes"),
    (54, "NT", "1 Timoteo", "1 Tim"),
    (55, "NT", "2 Timoteo", "2 Tim"),
    (56, "NT", "Tito", "Tito"),
    (57, "NT", "Filemón", "Flm"),
    (58, "NT", "Hebreos", "Heb"),
    (59, "NT", "Santiago", "Stg"),
    (60, "NT", "1 Pedro", "1 Ped"),
    (61, "NT", "2 Pedro", "2 Ped"),
    (62, "NT", "1 Juan", "1 Jn"),
    (63, "NT", "2 Juan", "2 Jn"),
    (64, "NT", "3 Juan", "3 Jn"),
    (65, "NT", "Judas", "Jud"),
    (66, "NT", "Apocalipsis", "Apoc"),
]

# Known OCR title cleanups for scanned hymn headers so all 222 hymn titles are 100% spotless
HYMN_TITLE_FIXES: Dict[int, str] = {
    4: "EL GRAN MÉDICO",
    10: "MELODÍAS CELESTIALES",
    13: "DULCE ORACIÓN",
    14: "¡OH, QUÉ AMIGO!",
    19: "ACEPTA EL PERDÓN DE JESÚS",
    22: "¡OH! YO QUIERO ANDAR CON CRISTO",
    28: "MÁS DE JESÚS",
    30: "HAY PODER EN JESÚS",
    37: "DÍA FELIZ",
    40: "EN LOS NEGOCIOS DEL REY",
    41: "SÍ, YO SÉ",
    45: "A TI Y A MÍ",
    46: "MÁS BLANCO QUE LA NIEVE",
    48: "¿ERES LIMPIO EN LA SANGRE?",
    50: "TE LOAMOS, OH DIOS",
    56: "SANTO ESPÍRITU, DESCIENDE",
    58: "HE DE VER A JESÚS",
    64: "JESÚS ES MI AMIGO",
    67: "DE MI TIERNO SALVADOR",
    68: "EN LA NUEVA JERUSALÉN",
    69: "PRECIOSO ES JESÚS",
    75: "TUYO SOY JESÚS",
    80: "PUEDO OÍR TU VOZ",
    81: "BUSCANDO A JESÚS",
    92: "EN JESÚS, MI SALVADOR",
    96: "¡SANTO! ¡SANTO! ¡SANTO!",
    97: "JESÚS VIVE EN MI CORAZÓN",
    99: "BIENAVENTURADOS LOS DE LIMPIO CORAZÓN",
    100: "A LOS PIES DE JESUCRISTO",
    107: "¡OH CRISTO MÍO!",
    109: "PECADOR, VEN AL DULCE JESÚS",
    112: "TODAS LAS PROMESAS DEL SEÑOR JESÚS",
    113: "VOY AL CIELO",
    116: "QUIERO SEGUIR LAS PISADAS DEL MAESTRO",
    118: "NO PUEDE EL MUNDO SER MI HOGAR",
    120: "MIS CULPAS ÉL LLEVÓ",
    126: "EN UN APOSENTO ALTO",
    129: "ALLÍ NO HABRÁ TRIBULACIÓN",
    136: "ME GUÍA ÉL, CON CUÁNTO AMOR",
    143: "EN LA VIÑA DEL SEÑOR",
    144: "CANTARÉ, CANTARÉ DEL HERMOSO PAÍS",
    149: "CRISTO AYER Y HOY EL MISMO",
    152: "HABLA, JESÚS, CONMIGO",
    160: "GRANDES COSAS CRISTO HA HECHO PARA MÍ",
    161: "CRISTO ESTÁ BUSCANDO OBREROS",
    165: "¡OH DIOS, VISÍTANOS!",
    168: "EL AMOR DE CRISTO VIENE SOBRE MÍ",
    169: "LA PAZ QUE CRISTO DA",
    172: "SOY FELIZ EN EL SERVICIO DEL SEÑOR",
    173: "¿NOS VEREMOS EN EL RÍO?",
    174: "SALVO EN LOS TIERNOS BRAZOS",
    175: "CUANDO ALLÁ SE PASE LISTA",
    176: "SÓLO UN POCO MÁS DE LA VIDA AQUÍ",
    177: "A JESÚS MI VIDA ENTREGO",
    179: "ÉL ES MI TODO AQUÍ",
    185: "HAY LUZ GLORIOSA EN MI CAMINO",
    198: "A MEDIA NOCHE EN BETHLEHEM",
    199: "VEN A MI CORAZÓN, OH JESÚS",
    200: "OÍD UN SON EN ALTA ESFERA",
    203: "SOY EL HIJO DE UN REY",
    209: "YO CANTO A CRISTO",
    211: "GRATO ES LA SANTA PALABRA LEER",
    212: "A JESUCRISTO QUIERO ALLEGARME",
    213: "CRISTO ME AMA",
    221: "LA PUERTA ABRID A LOS NIÑOS",
}


# ---------------------------------------------------------------------------
# Normalizer Function (search_text)
# ---------------------------------------------------------------------------
def normalize_search_text(text: str) -> str:
    """
    Convierte a minúsculas y elimina tildes/diacríticos (á->a, é->e, í->i, ó->o, ú->u, ü->u),
    conservando letras, números y espacios limpios para búsquedas FTS4 instantáneas.
    """
    if not text:
        return ""
    lowered = text.lower()
    # Replace explicit Spanish vowel diacritics first while preserving ñ if needed, or strip all combining marks
    trans_table = str.maketrans(
        {
            "á": "a",
            "à": "a",
            "ä": "a",
            "â": "a",
            "é": "e",
            "è": "e",
            "ë": "e",
            "ê": "e",
            "í": "i",
            "ì": "i",
            "ï": "i",
            "î": "i",
            "ó": "o",
            "ò": "o",
            "ö": "o",
            "ô": "o",
            "ú": "u",
            "ù": "u",
            "ü": "u",
            "û": "u",
        }
    )
    normalized = lowered.translate(trans_table)
    # Also decompose any remaining combining marks except tilde on n
    decomposed = unicodedata.normalize("NFD", normalized)
    cleaned_chars = []
    for ch in decomposed:
        if unicodedata.category(ch) == "Mn":
            continue
        cleaned_chars.append(ch)
    result = "".join(cleaned_chars)
    result = re.sub(r"[^\w\s]", " ", result)
    result = re.sub(r"\s+", " ", result).strip()
    return result


# ---------------------------------------------------------------------------
# Step 1: RVR1960 Bible Extraction
# ---------------------------------------------------------------------------
def load_rvr1960_bible() -> List[Dict]:
    """Carga el JSON de la Biblia Reina Valera 1960 (66 libros)."""
    if RVR1960_JSON_PATH.exists():
        with open(RVR1960_JSON_PATH, "r", encoding="utf-8-sig") as f:
            return json.load(f)

    print(f"[RVR1960] Descargando desde {RVR1960_FALLBACK_URL}...")
    req = urllib.request.Request(RVR1960_FALLBACK_URL, headers={"User-Agent": "Mozilla/5.0"})
    with urllib.request.urlopen(req, timeout=30) as resp:
        raw = resp.read().decode("utf-8-sig")
    return json.loads(raw)


# ---------------------------------------------------------------------------
# Step 2: Hybrid PDF Extraction (PyMuPDF Native Text + OCR Fallback)
# ---------------------------------------------------------------------------
def _extract_single_pdf_page(page_num: int, pdf_path_str: str) -> Tuple[int, str, str]:
    """
    Extrae el texto de una página 1-based del PDF:
    - Usa PyMuPDF (fitz) si la página posee capa de texto digital.
    - Usa OCR local (pytesseract en español o RapidOCR) si es una página escaneada.
    """
    doc = fitz.open(pdf_path_str)
    page = doc[page_num - 1]
    native_text = page.get_text("text").strip()
    if len(native_text) > 30:
        return page_num, native_text, "native"

    pix = page.get_pixmap(dpi=220)
    png_bytes = pix.tobytes("png")

    if pytesseract is not None:
        img = Image.open(io.BytesIO(png_bytes))
        tess_cfg = "--psm 6"
        lang = "spa" if (TESSDATA_DIR / "spa.traineddata").exists() else "eng"
        if TESSDATA_DIR.exists():
            tess_cfg = f"--tessdata-dir {TESSDATA_DIR} --psm 6"
        ocr_text = pytesseract.image_to_string(img, lang=lang, config=tess_cfg)
        return page_num, ocr_text, "ocr_tesseract"

    if RapidOCR is not None:
        engine = RapidOCR()
        result, _ = engine(png_bytes)
        lines = [item[1] for item in (result or [])]
        return page_num, "\n".join(lines), "ocr_rapidocr"

    raise RuntimeError("No se encontró motor OCR (pytesseract o rapidocr-onnxruntime).")


def extract_pdf_pages_hybrid(pdf_path: Path) -> Dict[int, Dict[str, str]]:
    """
    Ejecuta el procesamiento híbrido (PyMuPDF + OCR) sobre las páginas requeridas:
    - Himnos: páginas 2 a 225, excluyendo estrictamente 212 y 213.
    - Coros: páginas 248 a 334.
    """
    hymn_pages = [p for p in range(2, 226) if p not in (212, 213)]
    chorus_pages = list(range(248, 335))
    target_pages = hymn_pages + chorus_pages

    if OCR_CACHE_PATH.exists():
        with open(OCR_CACHE_PATH, "r", encoding="utf-8") as f:
            cached = json.load(f)
        if all(str(p) in cached for p in target_pages):
            print(f"[PDF] Cargando extracción híbrida desde caché ({len(target_pages)} páginas)...")
            return {int(k): v for k, v in cached.items()}

    if not pdf_path.exists():
        raise FileNotFoundError(f"No se encontró el PDF en {pdf_path}")

    print(f"[PDF] Procesando {len(target_pages)} páginas en modo híbrido (PyMuPDF + OCR)...")
    results: Dict[int, Dict[str, str]] = {}
    workers = os.cpu_count() or 2
    with ProcessPoolExecutor(max_workers=workers) as executor:
        futures = [executor.submit(_extract_single_pdf_page, p, str(pdf_path)) for p in target_pages]
        for fut in futures:
            p, txt, mode = fut.result()
            results[p] = {"text": txt, "mode": mode}

    with open(OCR_CACHE_PATH, "w", encoding="utf-8") as f:
        json.dump({str(k): v for k, v in results.items()}, f, ensure_ascii=False, indent=2)

    return results


# ---------------------------------------------------------------------------
# Step 3: Parsing Hymns (Pages 2..225 excluding 212, 213)
# ---------------------------------------------------------------------------
TONALITY_NORM_MAP = {
    "do": ("Do", "C"),
    "re": ("Re", "D"),
    "mi": ("Mi", "E"),
    "fa": ("Fa", "F"),
    "sol": ("Sol", "G"),
    "la": ("La", "A"),
    "si": ("Si", "B"),
    "st": ("Si", "B"),
}

TONALITY_REGEX = re.compile(
    r"\b(Do|Re|Mi|Fa|Sol|La|Si|ST)\s*\([A-Za-z]{1,2}\)(?:\s*(Bemol|Sostenido|Menor|Mayor))?",
    re.IGNORECASE,
)


def _clean_ocr_lyric_line(line: str) -> str:
    """Limpia artefactos de escaneo en márgenes izquierdos y derechos de cada verso."""
    s = line.strip()
    # Normalize OCR repeat markers first ('/1/', '1//', '/1') -> '//'
    s = re.sub(r"^(?:1//|/1/|/1)\s*", "// ", s)
    # Strip leading pipes, brackets, stray punctuation (preserving '//' and '///' repeat markers)
    s = re.sub(r"^[\|\[\]\{\}\`\~\-\_\>\<\+\=\:\;\.\,\\]+", "", s).strip()
    s = re.sub(r"^\/(?!\/)\s*", "", s).strip()
    # Strip border artifacts like '1 En todo', '4 Un rayo', 'l Que el niño', 'F Los que' before a capital letter
    s = re.sub(r"^(?:[14lIÍi\!\|F]\s+|[a-z]{1,2}\s+)(?=[A-ZÁÉÍÓÚÑ¡¿])", "", s).strip()
    # Strip trailing pipes, brackets, or isolated single-char scan noise at end of line
    s = re.sub(r"[\|\[\]\{\}\`\~\_\>\<\\]+$", "", s).strip()
    s = re.sub(r"\s+[\|\[\]\]\)\(1lIj]$", "", s).strip()
    return s


def parse_hymns(page_map: Dict[int, Dict[str, str]]) -> List[Dict]:
    """
    Extrae los Himnos desde la página 2 hasta la página 225,
    excluyendo estrictamente las páginas 212 y 213.
    """
    hymn_pages = [p for p in range(2, 226) if p not in (212, 213)]
    hymns: List[Dict] = []

    for idx, page_num in enumerate(hymn_pages, start=1):
        raw_text = page_map[page_num]["text"]
        raw_lines = [l.strip() for l in raw_text.splitlines() if l.strip()]

        tonality: Optional[str] = None
        subtitle_en: Optional[str] = None
        title_parts: List[str] = []
        body_start_idx = 0

        for i, line in enumerate(raw_lines[:8]):
            # 1. Check tonality line (e.g., "Mi (E) Bemol", "La (A)", "Sol (G)")
            tm = TONALITY_REGEX.search(line)
            if tm and tonality is None:
                note_key = tm.group(1).lower()
                modifier = tm.group(2)
                note_es, note_en = TONALITY_NORM_MAP.get(note_key, ("Do", "C"))
                tonality = f"{note_es} ({note_en})" + (f" {modifier.capitalize()}" if modifier else "")
                body_start_idx = max(body_start_idx, i + 1)
                continue

            # 2. Check English subtitle in parentheses
            sm = re.search(r"\(([A-Za-z0-9\s\,\'\`\“\”\"\.\!\?\-]{4,})\)", line)
            if sm and subtitle_en is None and not TONALITY_REGEX.search(line):
                sub_cand = sm.group(1).strip()
                # Clean OCR quirks in English subtitle like "T- Will Praise Him" or "111 Be a Sunbeam"
                sub_cand = re.sub(r"^(?:T-|111|1\s*“ll)\s*", "I'll ", sub_cand)
                subtitle_en = sub_cand
                body_start_idx = max(body_start_idx, i + 1)
                continue

            # Also handle English subtitle missing opening parenthesis like "afe in the Arms of Jesus)"
            if i <= 3 and subtitle_en is None and line.endswith(")") and "(" not in line and not line.isupper():
                subtitle_en = line[:-1].strip()
                body_start_idx = max(body_start_idx, i + 1)
                continue

            # 3. Stop header scan when reaching stanza 1
            if re.match(r"^[\|\:\-\.\'\(\s]*[1I][\.\)\s]*$", line) or re.match(
                r"^[\|\:\-\.\'\(\s]*1[\.\)\s]+[A-ZÁÉÍÓÚÑ¡¿]", line
            ):
                if title_parts or idx in HYMN_TITLE_FIXES:
                    body_start_idx = max(body_start_idx, i)
                    break

            # 4. Collect Spanish uppercase title lines
            if i < 5 and subtitle_en is None and tonality is None:
                t = re.sub(r"\b" + str(idx) + r"\b", " ", line)
                t = re.sub(r"^[^A-ZÁÉÍÓÚÑ¡¿]+", "", t)
                t = re.sub(r"[^A-ZÁÉÍÓÚÑ\!\?]+$", "", t)
                t = re.sub(r"\s+\d+\s*$", "", t).strip()
                upper_chars = sum(1 for c in t if c.isupper() or c in "ÁÉÍÓÚÑ")
                if upper_chars >= 4:
                    title_parts.append(t)
                    body_start_idx = max(body_start_idx, i + 1)

        title = HYMN_TITLE_FIXES.get(idx, " ".join(title_parts).strip())
        title = re.sub(r"\s+", " ", title).strip()

        # Category classification based on hymn section in Melodías Celestiales
        if 195 <= idx <= 201:
            category = "Navidad"
        elif idx >= 211:
            category = "Niños"
        else:
            category = "Himnario General"

        # Parse lyrics separating stanzas (1, 2, 3...) and CORO block
        blocks: List[Tuple[str, List[str]]] = []
        current_header = "1"
        current_lines: List[str] = []
        next_stanza_num = 1

        for raw_line in raw_lines[body_start_idx:]:
            s = raw_line.strip()
            if not s:
                continue

            # Detect CORO header
            if re.match(r"^[\|\'\`\-\.\:\s]*CORO[\.\:\|\'\s]*$", s, re.IGNORECASE):
                if current_lines:
                    blocks.append((current_header, current_lines))
                    current_lines = []
                current_header = "CORO"
                continue

            # Detect standalone stanza number (1..8, or OCR 'I'/'PIE' for 1/2)
            stanza_match = re.match(r"^[\|\'\`\-\.\:\;\/\,\(\s]*([1-8I])[\.\)\|\'\s]*$", s)
            if stanza_match or s == "PIE":
                tok = stanza_match.group(1) if stanza_match else "2"
                st_num = 1 if tok == "I" else (2 if s == "PIE" else int(tok))
                if st_num == next_stanza_num or st_num == next_stanza_num + 1 or (not current_lines and st_num == 1):
                    if current_lines:
                        blocks.append((current_header, current_lines))
                        current_lines = []
                    current_header = str(st_num)
                    next_stanza_num = st_num + 1
                    continue

            # Detect inline stanza number at start of stanza (e.g. "1 Castillo fuerte es nuestro Dios")
            inline_stanza = re.match(
                r"^[\|\'\`\-\.\:\;\(\slI]*([1-8])[\.\)\s]+([A-ZÁÉÍÓÚÑ¡¿].*)$", s
            )
            if inline_stanza:
                st_num = int(inline_stanza.group(1))
                rest = _clean_ocr_lyric_line(inline_stanza.group(2))
                if st_num == next_stanza_num and (st_num == 1 or len(current_lines) >= 3):
                    if current_lines:
                        blocks.append((current_header, current_lines))
                        current_lines = []
                    current_header = str(st_num)
                    next_stanza_num = st_num + 1
                    if rest:
                        current_lines.append(rest)
                    continue

            cleaned_line = _clean_ocr_lyric_line(s)
            # Filter out tiny 1-2 char scan artifacts
            if len(cleaned_line) <= 2 and not re.match(r"^[A-Za-záéíóúñÁÉÍÓÚÑ]{2}$", cleaned_line):
                continue
            if cleaned_line in ("Martín Lutero", "LLB. 122"):
                continue
            current_lines.append(cleaned_line)

        if current_lines:
            blocks.append((current_header, current_lines))

        formatted_blocks = []
        for header, b_lines in blocks:
            if not b_lines:
                continue
            formatted_blocks.append(f"{header}\n" + "\n".join(b_lines))

        lyrics = "\n\n".join(formatted_blocks).strip()
        search_blob = f"{idx} {title} {subtitle_en or ''} {tonality or ''} {lyrics}"

        hymns.append(
            {
                "type": "HYMN",
                "number": idx,
                "title": title,
                "subtitle_en": subtitle_en,
                "tonality": tonality,
                "category": category,
                "lyrics": lyrics,
                "search_text": normalize_search_text(search_blob),
            }
        )

    return hymns


# ---------------------------------------------------------------------------
# Step 4: Parsing Choruses (Pages 248..334)
# ---------------------------------------------------------------------------
CHORUS_HEADER_OVERRIDES = {
    21: (re.compile(r"^1\s+Jesús\s+me\s+Sostendrá", re.I), "Jesús me Sostendrá"),
    29: (re.compile(r"^Párate\s+Firme$", re.I), "Párate Firme"),
    75: (re.compile(r"^Mira\s+lo\s+que\s+hizo\s+mi\s+Señor$", re.I), "Mira lo que Hizo Mi Señor"),
    113: (re.compile(r"^413\)\s*Yo\s+Tengo\s+un\s+Amigo", re.I), "Yo Tengo un Amigo que me Ama"),
    160: (re.compile(r"^[ÍI1]60[\/\'\"\s]*Grita\s+y\s+Canta", re.I), "Grita y Canta"),
    200: (re.compile(r"^\(?209\s+Los\s+que\s+Esperan\s+en\s+Jehová", re.I), "Los que Esperan en Jehová"),
}

WRAPPED_CHORUS_TITLES = {
    158: ("Espiritu Santo", "Si Quieres Sentir el Gozo del Espíritu Santo"),
    165: ("Naciones", "Te Alabamos Señor entre las Naciones"),
    238: ("de Oro", "Yo Quiero Andar las Calles de Oro"),
}


def _clean_chorus_title(raw_title: str) -> str:
    t = re.sub(r"[\|\[\]\{\}\`\~\_\>\<\\]+$", "", raw_title).strip()
    t = re.sub(r"\s+[jpA\|\:\']$", "", t).strip()
    t = re.sub(r"^[\-\'\`\.\:\;\/]+", "", t).strip()
    # Fix joined words from OCR
    fixes = {
        "ALaBatalla": "A La Batalla",
        "VenA Morar": "Ven A Morar",
        "LaPresencia del Señor": "La Presencia del Señor",
        "Hlumíname Señor con Tu Espíritu": "Ilumíname Señor con Tu Espíritu",
        "Ala Escuela Dominical ¿": "A la Escuela Dominical",
    }
    return fixes.get(t, t)


def parse_choruses(page_map: Dict[int, Dict[str, str]]) -> List[Dict]:
    """
    Extrae los Coros desde la página 248 hasta la página 334 del PDF,
    segmentando cada coro por su número inicial y limpiando ruidos de escaneo.
    """
    all_lines: List[Tuple[int, str]] = []
    for p in range(248, 335):
        for l in page_map[p]["text"].splitlines():
            s = l.strip()
            if s:
                all_lines.append((p, s))

    raw_choruses: List[Dict] = []
    current_num = 0

    for p, line in all_lines:
        next_expected = current_num + 1
        matched_title: Optional[str] = None

        if next_expected in CHORUS_HEADER_OVERRIDES:
            pat_re, canonical_title = CHORUS_HEADER_OVERRIDES[next_expected]
            if pat_re.search(line):
                matched_title = canonical_title

        if matched_title is None:
            pat = (
                r"^[\(\|\'\-\.\s\/oO¡¿\:i\"]*0*"
                + str(next_expected)
                + r"(?:[\)\.\'\s\/\-\\\"\“\”]+|(?=[A-ZÁÉÍÓÚÑ¡¿]))([A-ZÁÉÍÓÚÑ¡¿].*)$"
            )
            m = re.match(pat, line)
            if m:
                matched_title = _clean_chorus_title(m.group(1).strip())

        if matched_title is not None:
            current_num = next_expected
            raw_choruses.append(
                {
                    "number": current_num,
                    "page": p,
                    "title": matched_title,
                    "lines": [],
                }
            )
        elif raw_choruses:
            raw_choruses[-1]["lines"].append(line)

    choruses: List[Dict] = []
    for item in raw_choruses:
        num = item["number"]
        title = item["title"]
        lines = item["lines"]

        # Merge wrapped title lines if applicable
        if num in WRAPPED_CHORUS_TITLES and lines and lines[0].strip() == WRAPPED_CHORUS_TITLES[num][0]:
            title = WRAPPED_CHORUS_TITLES[num][1]
            lines = lines[1:]

        cleaned_lines: List[str] = []
        for raw_l in lines:
            cl = _clean_ocr_lyric_line(raw_l)
            # Normalize OCR slash repeat markers like '1//' or '/1/' -> '///' or '//'
            cl = re.sub(r"^(?:1//|/1/|/1)\s*", "// ", cl)
            if len(cl) <= 2 and not re.match(r"^(?:Si|Sí|No|Él|El|Oh|Ti|Mí|Ya|Fe)$", cl, re.I):
                continue
            if re.match(r"^\d+$", cl):
                continue
            cleaned_lines.append(cl)

        lyrics = "\n".join(cleaned_lines).strip()

        # Categorize choruses (General / Alabanza y Adoración / Infantiles)
        if num >= 218:
            category = "Infantiles / Escuela Dominical"
        elif any(w in title.lower() for w in ("espíritu", "pentecostal", "fuego")):
            category = "Espíritu Santo"
        elif any(w in title.lower() for w in ("alaba", "gloria", "santo", "digno", "majestad", "bendice")):
            category = "Adoración y Alabanza"
        else:
            category = "Coros Generales"

        search_blob = f"{num} {title} {category} {lyrics}"
        choruses.append(
            {
                "type": "CHORUS",
                "number": num,
                "title": title,
                "subtitle_en": None,
                "tonality": None,
                "category": category,
                "lyrics": lyrics,
                "search_text": normalize_search_text(search_blob),
            }
        )

    return choruses


# ---------------------------------------------------------------------------
# Step 5: Build Ultra-Fast SQLite + FTS4 Database (< 5 ms)
# ---------------------------------------------------------------------------
def build_database(books_data: List[Dict], hymns: List[Dict], choruses: List[Dict], db_path: Path) -> None:
    db_path.parent.mkdir(parents=True, exist_ok=True)
    if db_path.exists():
        db_path.unlink()

    conn = sqlite3.connect(str(db_path))
    cur = conn.cursor()

    # Ultra-fast build pragmas required by Fase 1
    cur.executescript(
        """
        PRAGMA journal_mode = OFF;
        PRAGMA synchronous = OFF;
        PRAGMA page_size = 4096;
        """
    )

    # Create Schema (Room & FTS4 compatible)
    cur.executescript(
        """
        CREATE TABLE books (
            id INTEGER NOT NULL PRIMARY KEY,
            testament TEXT NOT NULL,
            name TEXT NOT NULL,
            abbreviation TEXT NOT NULL,
            search_name TEXT NOT NULL,
            chapter_count INTEGER NOT NULL
        );

        CREATE TABLE verses (
            id INTEGER NOT NULL PRIMARY KEY,
            book_id INTEGER NOT NULL,
            chapter INTEGER NOT NULL,
            verse INTEGER NOT NULL,
            text TEXT NOT NULL,
            search_text TEXT NOT NULL,
            FOREIGN KEY(book_id) REFERENCES books(id) ON UPDATE NO ACTION ON DELETE CASCADE
        );

        CREATE UNIQUE INDEX idx_verses_location ON verses(book_id, chapter, verse);

        CREATE VIRTUAL TABLE verses_fts USING fts4(
            content="verses",
            text,
            search_text,
            tokenize=unicode61 "remove_diacritics=2",
            prefix='2,3,4'
        );

        CREATE TABLE songs (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            type TEXT NOT NULL,
            number INTEGER NOT NULL,
            title TEXT NOT NULL,
            subtitle_en TEXT,
            tonality TEXT,
            category TEXT,
            lyrics TEXT NOT NULL,
            search_text TEXT NOT NULL
        );

        CREATE INDEX idx_songs_type_number ON songs(type, number);

        CREATE VIRTUAL TABLE songs_fts USING fts4(
            content="songs",
            title,
            lyrics,
            search_text,
            tokenize=unicode61 "remove_diacritics=2",
            prefix='2,3,4'
        );

        CREATE TABLE bookmarks (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            item_type TEXT NOT NULL,
            reference_id INTEGER NOT NULL,
            title_preview TEXT NOT NULL,
            content_preview TEXT NOT NULL,
            color_hex TEXT NOT NULL,
            note TEXT,
            created_at INTEGER NOT NULL
        );
        """
    )

    # Insert Books & Verses
    book_rows = []
    verse_rows = []
    verse_id = 1

    for idx, book_json in enumerate(books_data):
        book_id, testament, canonical_name, canonical_abbr = CANONICAL_BOOKS[idx]
        chapters = book_json["chapters"]
        chapter_count = len(chapters)
        search_name = normalize_search_text(f"{canonical_name} {canonical_abbr} {book_json.get('name', '')}")
        book_rows.append((book_id, testament, canonical_name, canonical_abbr, search_name, chapter_count))

        for ch_idx, ch_verses in enumerate(chapters, start=1):
            for v_idx, v_text in enumerate(ch_verses, start=1):
                clean_v = v_text.strip()
                verse_rows.append(
                    (
                        verse_id,
                        book_id,
                        ch_idx,
                        v_idx,
                        clean_v,
                        normalize_search_text(clean_v),
                    )
                )
                verse_id += 1

    cur.executemany(
        "INSERT INTO books (id, testament, name, abbreviation, search_name, chapter_count) VALUES (?, ?, ?, ?, ?, ?);",
        book_rows,
    )
    cur.executemany(
        "INSERT INTO verses (id, book_id, chapter, verse, text, search_text) VALUES (?, ?, ?, ?, ?, ?);",
        verse_rows,
    )

    # Insert Songs (Hymns + Choruses)
    song_rows = [
        (
            s["type"],
            s["number"],
            s["title"],
            s["subtitle_en"],
            s["tonality"],
            s["category"],
            s["lyrics"],
            s["search_text"],
        )
        for s in (hymns + choruses)
    ]
    cur.executemany(
        """
        INSERT INTO songs (type, number, title, subtitle_en, tonality, category, lyrics, search_text)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?);
        """,
        song_rows,
    )

    # Rebuild FTS4 virtual tables and optimize
    cur.execute("INSERT INTO verses_fts(verses_fts) VALUES('rebuild');")
    cur.execute("INSERT INTO songs_fts(songs_fts) VALUES('rebuild');")
    conn.commit()

    cur.execute("PRAGMA optimize;")
    cur.execute("VACUUM;")
    conn.close()


# ---------------------------------------------------------------------------
# Step 6: Verification & Ultra-Low Latency Benchmark (< 5 ms)
# ---------------------------------------------------------------------------
def verify_database(db_path: Path) -> None:
    conn = sqlite3.connect(str(db_path))
    cur = conn.cursor()

    books_count = cur.execute("SELECT COUNT(*) FROM books;").fetchone()[0]
    at_count = cur.execute("SELECT COUNT(*) FROM books WHERE testament = 'AT';").fetchone()[0]
    nt_count = cur.execute("SELECT COUNT(*) FROM books WHERE testament = 'NT';").fetchone()[0]
    verses_count = cur.execute("SELECT COUNT(*) FROM verses;").fetchone()[0]
    hymns_count = cur.execute("SELECT COUNT(*) FROM songs WHERE type = 'HYMN';").fetchone()[0]
    choruses_count = cur.execute("SELECT COUNT(*) FROM songs WHERE type = 'CHORUS';").fetchone()[0]

    # Benchmark 1: FTS4 Bible search
    t0 = time.perf_counter()
    fts_verses = cur.execute(
        """
        SELECT v.id, v.book_id, b.name AS bookName, b.abbreviation AS bookAbbr, v.chapter, v.verse, v.text
        FROM verses v
        JOIN verses_fts fts ON v.id = fts.rowid
        JOIN books b ON v.book_id = b.id
        WHERE verses_fts MATCH 'pastor*'
        LIMIT 100;
        """
    ).fetchall()
    t_bible_fts_ms = (time.perf_counter() - t0) * 1000.0

    # Benchmark 2: FTS4 Songs search
    t0 = time.perf_counter()
    fts_songs = cur.execute(
        """
        SELECT s.id, s.type, s.number, s.title
        FROM songs s
        JOIN songs_fts fts ON s.id = fts.rowid
        WHERE songs_fts MATCH 'lluvias* gracia*'
        ORDER BY s.type ASC, s.number ASC
        LIMIT 80;
        """
    ).fetchall()
    t_songs_fts_ms = (time.perf_counter() - t0) * 1000.0

    # Benchmark 3: Direct index lookup by number
    t0 = time.perf_counter()
    song_104 = cur.execute(
        "SELECT type, number, title, subtitle_en, tonality FROM songs WHERE type = 'HYMN' AND number = 104;"
    ).fetchone()
    t_song_num_ms = (time.perf_counter() - t0) * 1000.0

    size_mb = db_path.stat().st_size / (1024 * 1024)
    print("\n" + "=" * 72)
    print("REPORTE DE VERIFICACIÓN - FASE 1 (bible_hymns_choruses.db)")
    print("=" * 72)
    print(f"Ruta de salida       : {db_path}")
    print(f"Tamaño final (VACUUM): {size_mb:.2f} MB")
    print(f"Total Libros RVR1960 : {books_count} (AT: {at_count}, NT: {nt_count})")
    print(f"Total Versículos     : {verses_count:,}")
    print(f"Total Himnos (HYMN)  : {hymns_count} (Págs. 2-225 excluyendo 212-213)")
    print(f"Total Coros (CHORUS) : {choruses_count} (Págs. 248-334)")
    print(f"Total Canciones      : {hymns_count + choruses_count}")
    print("-" * 72)
    print(f"Benchmark FTS4 Biblia ('pastor*')        : {t_bible_fts_ms:.2f} ms ({len(fts_verses)} resultados)")
    print(f"Benchmark FTS4 Canciones ('lluvias*')    : {t_songs_fts_ms:.2f} ms ({len(fts_songs)} resultados)")
    print(f"Benchmark Índice #104 (idx_songs_type_nr): {t_song_num_ms:.3f} ms -> {song_104}")
    print("=" * 72)

    conn.close()


def main() -> None:
    t_start = time.perf_counter()
    books_data = load_rvr1960_bible()
    page_map = extract_pdf_pages_hybrid(PDF_DEFAULT_PATH)
    hymns = parse_hymns(page_map)
    choruses = parse_choruses(page_map)
    build_database(books_data, hymns, choruses, DB_OUTPUT_PATH)
    verify_database(DB_OUTPUT_PATH)
    print(f"Tiempo total de construcción SQLite/FTS4: {time.perf_counter() - t_start:.2f} s")


if __name__ == "__main__":
    main()
