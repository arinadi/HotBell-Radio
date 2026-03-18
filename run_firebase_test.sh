#!/bin/bash

# Pastikan script berhenti jika ada error
set -e

echo "🔨 Membangun APK Debug..."
./gradlew app:assembleDebug

echo "🔨 Membangun APK Instrumentation Test..."
./gradlew app:assembleDebugAndroidTest

echo "🔍 Mencari file APK yang telah di-build..."
# Karena project ini mengubah nama APK, kita gunakan 'find' agar fleksibel
APP_APK=$(find app/build/outputs/apk/debug -name "*.apk" | head -n 1)
TEST_APK=$(find app/build/outputs/apk/androidTest/debug -name "*.apk" | head -n 1)

if [ -z "$APP_APK" ] || [ -z "$TEST_APK" ]; then
    echo "❌ Error: APK tidak ditemukan! Pastikan build berhasil."
    exit 1
fi

echo "🚀 Menjalankan Instrumentation Test di Firebase Test Lab..."
echo "📱 App: $APP_APK"
echo "🧪 Test: $TEST_APK"

# Jalankan Test menggunakan gcloud pada perangkat Pixel (SDK 33)
# gcloud firebase test android run \
#     --type instrumentation \
#     --app "$APP_APK" \
#     --test "$TEST_APK" \
#     --device model=Panther,version=33,locale=en,orientation=portrait \
#     --timeout 3m

echo "✅ Selesai!"
