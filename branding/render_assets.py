#!/usr/bin/env python3
"""Render Rokstrn's original Android icons and TV banner with Inkscape."""

from pathlib import Path
from subprocess import run
from tempfile import TemporaryDirectory

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app/src/main"
RES = APP / "res"
ANDROID = 'xmlns:android="http://schemas.android.com/apk/res/android"'
CHARCOAL = "#100E17"
LAVENDER = "#C3ACFF"
CRIMSON = "#ED486C"
WHITE = "#F8F5FF"
MARK = "M29,25H58C72,25 80,33 80,45C80,54 75,61 67,64L82,83H66L51,64H42V83H29Z M42,37V52H58C64,52 67,49 67,45C67,40 64,37 58,37Z"
TIP = "M66,83H82L76,75H60Z"
CAP = "M29,25H58C65,25 70,27 74,30H29Z"
LETTERS = {
    "R": "M1,12V1H6L9,3V5L6,7H1 M5,7L9,12",
    "O": "M3,1H7L9,3V10L7,12H3L1,10V3Z",
    "K": "M1,1V12 M9,1L1,7 M4.5,5L9,12",
    "S": "M9,1H3L1,3V5L3,6.5H7L9,8V10L7,12H1",
    "T": "M0.5,1H9.5 M5,1V12",
    "N": "M1,12V1L9,12V1",
}


def write(path, text):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def path(data, fill):
    return f'    <path android:fillColor="{fill}" android:pathData="{data}"/>'


def vector(viewport, body, *, dp=None):
    size = dp or viewport
    return (
        f'<vector {ANDROID} android:width="{size}dp" android:height="{size}dp"\n'
        f'    android:viewportWidth="{viewport}" android:viewportHeight="{viewport}">\n'
        f'{body}\n</vector>\n'
    )


def svg(width, height, body):
    return (
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">\n'
        '<defs><linearGradient id="bg" x2="100%" y2="100%">'
        f'<stop stop-color="#2C1B4B"/><stop offset=".58" stop-color="{CHARCOAL}"/>'
        '<stop offset="1" stop-color="#210F20"/></linearGradient></defs>\n'
        f'{body}\n</svg>\n'
    )


def render(art, dest, width, height):
    dest.parent.mkdir(parents=True, exist_ok=True)
    with TemporaryDirectory() as tmp:
        source = Path(tmp) / "source.svg"
        write(source, art)
        run(["inkscape", str(source), f"--export-filename={dest}", f"--export-width={width}",
             f"--export-height={height}"], check=True)


def main():
    pieces = [(MARK, WHITE), (TIP, CRIMSON), (CAP, LAVENDER)]
    mark = "".join(f'<path fill="{fill}" d="{data}"/>' for data, fill in pieces)
    write(RES / "drawable/ic_launcher_foreground.xml", vector(108, "\n".join(path(*piece) for piece in pieces)))
    write(RES / "drawable/ic_rokstrn_monochrome.xml", vector(108, path(MARK, "#FFFFFF")))
    status = f'    <group android:translateX="-18" android:translateY="-18">\n{path(MARK, "#FFFFFF")}\n    </group>'
    write(RES / "drawable/ic_rokstrn_status.xml", vector(72, status, dp=24))
    write(
        RES / "drawable/ic_launcher_background.xml",
        f'<shape {ANDROID} android:shape="rectangle">\n'
        f'    <gradient android:angle="315" android:startColor="#2C1B4B" android:centerColor="{CHARCOAL}" android:endColor="#210F20"/>\n'
        '</shape>\n',
    )
    for name, corner in [("ic_launcher", 21), ("ic_launcher_round", 54)]:
        write(
            RES / f"mipmap-anydpi-v26/{name}.xml",
            f'<adaptive-icon {ANDROID}>\n'
            '    <background android:drawable="@drawable/ic_launcher_background"/>\n'
            '    <foreground android:drawable="@drawable/ic_launcher_foreground"/>\n'
            '    <monochrome android:drawable="@drawable/ic_rokstrn_monochrome"/>\n'
            '</adaptive-icon>\n',
        )
        icon = svg(108, 108, f'<rect width="108" height="108" rx="{corner}" fill="url(#bg)"/>' + mark)
        for density, size in [("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)]:
            render(icon, RES / f"mipmap-{density}/{name}.png", size, size)
        if name == "ic_launcher":
            write(ROOT / "branding/rokstrn.svg", icon)
            render(icon, APP / "ic_launcher-playstore.png", 512, 512)
            render(icon, ROOT / "desktopApp/src/desktop-icons/icon-linux.png", 512, 512)

    letters = "".join(f'<path transform="translate({index*13} 0)" d="{LETTERS[letter]}"/>' for index, letter in enumerate("ROKSTRN"))
    banner = svg(
        320, 180,
        '<rect width="320" height="180" fill="url(#bg)"/>'
        '<path d="M0,152 152,0H169L0,169Z" fill="#382556" opacity=".42"/>'
        '<path d="M251,180 320,111V123L263,180Z" fill="#59253D" opacity=".55"/>'
        f'<g transform="translate(17 37) scale(.98)">{mark}</g>'
        f'<g transform="translate(113 76) scale(1.9)" fill="none" stroke="{WHITE}" stroke-width="1.42" stroke-linejoin="round">{letters}</g>'
        f'<path d="M114,112H151" stroke="{LAVENDER}" stroke-width="2"/>'
        f'<path d="M153,112H163" stroke="{CRIMSON}" stroke-width="2"/>',
    )
    write(ROOT / "branding/rokstrn-tv.svg", banner)
    render(banner, RES / "mipmap-xhdpi/ic_banner.png", 320, 180)
    render(banner, RES / "drawable-xhdpi/rokstrn_tv_art.png", 320, 180)
    write(
        RES / "drawable/ic_banner_background.xml",
        f'<layer-list {ANDROID}>\n'
        '    <item android:drawable="@drawable/ic_launcher_background"/>\n'
        '    <item android:left="26dp" android:right="26dp" android:top="15dp" android:bottom="15dp"\n'
        '        android:drawable="@drawable/rokstrn_tv_art"/>\n</layer-list>\n',
    )


if __name__ == "__main__":
    main()
