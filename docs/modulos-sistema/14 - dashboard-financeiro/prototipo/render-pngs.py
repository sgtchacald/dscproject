#!/usr/bin/env python3
"""Renderiza os PNGs do documento 14 - Dashboard Financeiro.

Requisitos:
    pip install --user playwright
    python3 -m playwright install chromium
    (Fedora) sudo dnf install -y nss atk at-spi2-atk cups-libs libdrm libxkbcommon \
        libXcomposite libXdamage libXrandr mesa-libgbm pango alsa-lib

Uso:  python3 render-pngs.py
Saída: ../images/df-tela-1..4.png, dashboard-financeiro-der.png, dashboard-financeiro-casos-uso.png
"""
import pathlib
from playwright.sync_api import sync_playwright

HERE = pathlib.Path(__file__).resolve().parent
IMG = HERE.parent / "images"
IMG.mkdir(exist_ok=True)
PROTO = (HERE / "dashboard-financeiro-prototipo.html").as_uri()
DER = (HERE / "_diagrama-der.html").as_uri()
UC = (HERE / "_diagrama-casos-uso.html").as_uri()

# (arquivo, data-v do botao da proto-bar, legenda)
TELAS = [
    ("df-tela-1.png", "normal",  "dados normais"),
    ("df-tela-2.png", "vazio",   "estado vazio (sem lançamento no período)"),
    ("df-tela-3.png", "estouro", "limite de cartão estourado"),
    ("df-tela-4.png", "anual",   "evolução em modo anual (intervalo de anos)"),
]


def main():
    with sync_playwright() as p:
        br = p.chromium.launch(args=["--no-sandbox", "--force-color-profile=srgb"])
        pg = br.new_page(viewport={"width": 1440, "height": 1400}, device_scale_factor=2,
                         color_scheme="light")

        for nome, view, legenda in TELAS:
            pg.goto(PROTO)
            pg.wait_for_selector("#protoBar")
            pg.wait_for_timeout(250)  # deixa o CSS do Tabler (CDN) assentar
            pg.click(f'#protoBar button[data-v="{view}"]')
            pg.wait_for_timeout(250)
            out = IMG / nome
            pg.screenshot(path=str(out), full_page=True)
            print("ok", out.relative_to(HERE.parent), f"({legenda})")

        for src, nome in [(DER, "dashboard-financeiro-der.png"), (UC, "dashboard-financeiro-casos-uso.png")]:
            pg.goto(src)
            pg.wait_for_selector("#diagram")
            pg.wait_for_timeout(150)
            out = IMG / nome
            pg.locator("#diagram").screenshot(path=str(out))
            print("ok", out.relative_to(HERE.parent))

        br.close()


if __name__ == "__main__":
    main()
