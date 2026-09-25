#!/usr/bin/env python3
"""Renderiza os PNGs do documento 2.04 - Manter Cartao de Credito.

Requisitos:
    pip install --user playwright
    python3 -m playwright install chromium
    (Fedora) sudo dnf install -y nss atk at-spi2-atk cups-libs libdrm libxkbcommon \
        libXcomposite libXdamage libXrandr mesa-libgbm pango alsa-lib

Uso:  python3 render-pngs.py   (Windows: py render-pngs.py)
Saída: ../images/mcc-tela-1..3.png, manter-cartao-credito-der.png, manter-cartao-credito-casos-uso.png
"""
import pathlib
from playwright.sync_api import sync_playwright

HERE = pathlib.Path(__file__).resolve().parent
IMG = HERE.parent / "images"
IMG.mkdir(exist_ok=True)
PROTO = (HERE / "manter-cartao-credito-prototipo.html").as_uri()
DER = (HERE / "_diagrama-der.html").as_uri()
UC = (HERE / "_diagrama-casos-uso.html").as_uri()

# (arquivo, data-v do botao da proto-bar, tipo)   tipo: "page" = tela cheia, "modal" = recorta o .modal.show
TELAS = [
    ("mcc-tela-1.png", "mcc-list",   "page"),
    ("mcc-tela-2.png", "mcc-filtro", "modal"),
    ("mcc-tela-3.png", "mcc-form",   "modal"),
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
            out = IMG / nome
            if tipo == "modal":
                pg.wait_for_selector(".modal.show", state="visible")
                pg.wait_for_timeout(150)
                pg.locator(".modal.show").screenshot(path=str(out))
            else:
                pg.wait_for_timeout(250)
                pg.screenshot(path=str(out), full_page=True)
            print("ok", out.relative_to(HERE.parent))

        for src, nome in [(DER, "manter-cartao-credito-der.png"),
                          (UC, "manter-cartao-credito-casos-uso.png")]:
            pg.goto(src)
            pg.wait_for_selector("#diagram")
            pg.wait_for_timeout(150)
            out = IMG / nome
            pg.locator("#diagram").screenshot(path=str(out))
            print("ok", out.relative_to(HERE.parent))

        br.close()


if __name__ == "__main__":
    main()
