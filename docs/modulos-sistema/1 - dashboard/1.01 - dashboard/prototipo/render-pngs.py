#!/usr/bin/env python3
"""Renderiza os PNGs do documento 1.01 - Dashboard (orquestrador).

Requisitos:
    pip install --user playwright
    python3 -m playwright install chromium
    (Fedora) sudo dnf install -y nss atk at-spi2-atk cups-libs libdrm libxkbcommon \
        libXcomposite libXdamage libXrandr mesa-libgbm pango alsa-lib

Uso:  python3 render-pngs.py   (Windows: py render-pngs.py)
Saída: ../images/dash-tela-1..2.png, dashboard-casos-uso.png

Este documento não lê nenhuma tabela (mecânica de orquestração pura) — não há DER.
"""
import pathlib
from playwright.sync_api import sync_playwright

HERE = pathlib.Path(__file__).resolve().parent
IMG = HERE.parent / "images"
IMG.mkdir(exist_ok=True)
PROTO = (HERE / "dashboard-prototipo.html").as_uri()
UC = (HERE / "_diagrama-casos-uso.html").as_uri()

# (arquivo, data-v do botao da proto-bar)
TELAS = [
    ("dash-tela-1.png", "sem"),
    ("dash-tela-2.png", "com"),
]


def main():
    with sync_playwright() as p:
        br = p.chromium.launch(args=["--no-sandbox", "--force-color-profile=srgb"])
        pg = br.new_page(viewport={"width": 1400, "height": 900}, device_scale_factor=2,
                         color_scheme="light")

        for nome, view in TELAS:
            pg.goto(PROTO)
            pg.wait_for_selector(".proto-bar")
            pg.wait_for_timeout(200)  # deixa o CSS do Tabler (CDN) assentar
            pg.click(f'.proto-bar button[data-v="{view}"]')
            pg.wait_for_timeout(250)
            out = IMG / nome
            pg.screenshot(path=str(out), full_page=True)
            print("ok", out.relative_to(HERE.parent))

        pg.goto(UC)
        pg.wait_for_selector("#diagram")
        pg.wait_for_timeout(150)
        out = IMG / "dashboard-casos-uso.png"
        pg.locator("#diagram").screenshot(path=str(out))
        print("ok", out.relative_to(HERE.parent))

        br.close()


if __name__ == "__main__":
    main()
