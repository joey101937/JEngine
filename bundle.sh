#!/usr/bin/env bash
# =============================================================================
# bundle.sh - Build a standalone, self-contained macOS app for JEngine.
#
# MUST be run on macOS (jpackage produces a native .app for the OS it runs on).
#
# Produces:
#   JEngine.app        - double-clickable app with a bundled Java runtime
#                        (target Mac needs NO Java installed)
#   dist/JEngine-mac.zip - shareable zip of the .app
#
# Usage (from the project root):
#   chmod +x bundle.sh && ./bundle.sh
#
# Uses the current dist/JEngine.jar. Build the project first (in NetBeans, or
# `ant jar`) if you want to bundle fresh code.
# =============================================================================
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

NAME="JEngine"
MAIN_CLASS="Framework.Main"
JAR="$ROOT/dist/JEngine.jar"
PNG="$ROOT/src/Resources/JEngineIcon.png"
ASSETS="$ROOT/Assets"
STAGING="$ROOT/build/bundle-mac"
INPUT="$ROOT/build/bundle-mac-input"
APP="$STAGING/$NAME.app"
ZIP="$ROOT/dist/$NAME-mac.zip"

if [[ "$(uname)" != "Darwin" ]]; then
    echo "ERROR: bundle.sh must be run on macOS. (Use bundle.ps1 / the 'bundle' Ant target on Windows.)" >&2
    exit 1
fi

# --- Locate a JDK that has jpackage ------------------------------------------
find_jpackage() {
    if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/jpackage" ]]; then
        echo "$JAVA_HOME/bin/jpackage"; return 0
    fi
    if command -v jpackage >/dev/null 2>&1; then
        command -v jpackage; return 0
    fi
    local jp
    jp="$(ls /Library/Java/JavaVirtualMachines/*/Contents/Home/bin/jpackage 2>/dev/null | sort -r | head -1 || true)"
    if [[ -n "$jp" ]]; then echo "$jp"; return 0; fi
    return 1
}
JPACKAGE="$(find_jpackage)" || { echo "ERROR: jpackage not found. Install a JDK (17+) or set JAVA_HOME." >&2; exit 1; }
echo "Using jpackage: $JPACKAGE"

[[ -f "$JAR" ]] || { echo "ERROR: dist/JEngine.jar not found. Build the project first." >&2; exit 1; }

# --- Generate a .icns icon from the PNG (macOS needs .icns) -------------------
ICON_ARGS=()
ICNS="$ROOT/build/$NAME.icns"
if [[ -f "$PNG" ]]; then
    echo "Generating icon..."
    ICONSET="$ROOT/build/$NAME.iconset"
    rm -rf "$ICONSET"; mkdir -p "$ICONSET"
    for sz in 16 32 128 256 512; do
        sips -z $sz $sz "$PNG" --out "$ICONSET/icon_${sz}x${sz}.png" >/dev/null
        sips -z $((sz*2)) $((sz*2)) "$PNG" --out "$ICONSET/icon_${sz}x${sz}@2x.png" >/dev/null
    done
    iconutil -c icns "$ICONSET" -o "$ICNS"
    ICON_ARGS=(--icon "$ICNS")
fi

# --- Fresh staging -----------------------------------------------------------
rm -rf "$STAGING" "$INPUT"
mkdir -p "$INPUT"
cp "$JAR" "$INPUT/$NAME.jar"

# --- Build the self-contained app image (.app + trimmed runtime) -------------
echo "Running jpackage (bundles a Java runtime; takes a minute)..."
"$JPACKAGE" \
    --type app-image \
    --name "$NAME" \
    --input "$INPUT" \
    --main-jar "$NAME.jar" \
    --main-class "$MAIN_CLASS" \
    --dest "$STAGING" \
    "${ICON_ARGS[@]}" \
    --add-modules java.base,java.desktop,jdk.unsupported \
    --java-options -Dsun.java2d.uiScale=1 \
    --java-options -Xmx4096m

# --- Assets go beside the jar inside the bundle; Main.getDir() resolves them --
echo "Copying Assets into the app bundle..."
cp -R "$ASSETS" "$APP/Contents/app/Assets"

# --- Shareable zip (ditto preserves the .app bundle correctly) ---------------
echo "Zipping distribution..."
mkdir -p "$ROOT/dist"
rm -f "$ZIP"
ditto -c -k --keepParent "$APP" "$ZIP"

echo ""
echo "====================================================="
echo "Standalone macOS build complete."
echo "  Run it here:  $APP"
echo "  Share this:   $ZIP"
echo "====================================================="
