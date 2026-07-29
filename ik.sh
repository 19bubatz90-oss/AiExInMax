cd ~/GhostMax
cat > .github/workflows/build-apk.yml << 'EOF'
name: Build APK
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Setup Android SDK
        uses: android-actions/setup-android@v3
        with:
          packages: 'platforms;android-34 build-tools;34.0.0'
          accept-android-sdk-licenses: true
      - name: Create local.properties
        run: echo "sdk.dir=$ANDROID_HOME" > local.properties
      - name: Make gradlew executable
        run: chmod +x gradlew
      - name: Build Debug APK
        run: ./gradlew assembleDebug
      - name: Upload APK artifact
        uses: actions/upload-artifact@v4
        with:
          name: AiExInMax-Debug-APK
          path: app/build/outputs/apk/debug/app-debug.apk
EOF
git add .github/workflows/build-apk.yml
git commit -m "Workflow: setup-android@v3 mit korrekter packages-Syntax"
git push origin main
