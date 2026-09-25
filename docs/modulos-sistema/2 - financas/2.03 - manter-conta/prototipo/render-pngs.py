#!/usr/bin/env python3
"""Renderiza os PNGs do documento 2.03 - Manter Conta.

Requisitos:
    pip install --user playwright
    python3 -m playwright install chromium
    (Fedora) sudo dnf install -y nss atk at-spi2-atk cups-libs libdrm libxkbcommon \
        libXcomposite libXdamage libXrandr mesa-libgbm pango alsa-lib

Uso:  python3 render-pngs.py   (Windows: py render-pngs.py)
Saída: ../images/mc-tela-1..4.png, manter-conta-der.png, manter-conta-casos-uso.png
"""
import pathlib
from playwright.sync_api import sync_playwright

HERE = pathlib.Path(__file__).resolve().parent
IMG = HERE.parent / "images"
IMG.mkdir(exist_ok=True)
PROTO = (HERE / "manter-conta-prototipo.html").as_uri()
DER = (HERE / "_diagrama-der.html").as_uri()
UC = (HERE / "_diagrama-casos-uso.html").as_uri()

# (arquivo, data-v do botao da proto-bar, tipo)   tipo: "page" = tela cheia, "modal" = recorta o .scrim
TELAS = [
    ("mc-tela-1.png", "mc-list",   "page"),
    ("mc-tela-2.png", "mc-filtro", "modal"),
    ("mc-tela-3.png", "mc-form",   "modal"),
    ("mc-tela-4.png", "mc-saldo",  "modal"),
]


def main():
    with sync_playwright() as p:
        br = p.chromium.launch(args=["--no-sandbox", "--force-color-profile=srgb"])
        pg = br.new_page(viewport={"width": 1400, "height": 900}, device_scale_factor=2,
                         color_scheme="light")

        for nome, view, tipo in TELAS:
            pg.goto(PROTO)
            pg.wait_for_selector(".proto-bar")
            pg.click(f'.proto-bar button[data-v="{view}"]')
            pg.wait_for_timeout(250)
            out = IMG / nome
            if tipo == "modal":
                pg.locator(".scrim:not([hidden])").screenshot(path=str(out))
            else:
                pg.screenshot(path=str(out), full_page=True)
            print("ok", out.relative_to(HERE.parent))

        for src, nome in [(DER, "manter-conta-der.png"),
                          (UC, "manter-conta-casos-uso.png")]:
            pg.goto(src)
            pg.wait_for_selector("#diagram")
            pg.wait_for_timeout(150)
            out = IMG / nome
            pg.locator("#diagram").screenshot(path=str(out))
            print("ok", out.relative_to(HERE.parent))

        br.close()


if __name__ == "__main__":
    main()
