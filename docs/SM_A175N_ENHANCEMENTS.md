# SM-A175N / Android 17 / One UI 9 enhancement branch

This branch preserves upstream App Manager v4.1.1 features and existing Android 17
package-listing fixes. It does not grant root, Shizuku, or ADB privileges.

- Exact device profile: Samsung SM-A175N, Android API 37 or later.
- Default worker count is capped at 4 on this device to reduce memory pressure;
  manually configured worker counts override the default.
- Executor-pool reuse is synchronized.
- Destructive operations on critical system packages or shared privileged
  UIDs are skipped in routine actions and legacy batch uninstall, clear-data,
  freeze and component-block paths. Restorative operations remain available.
- Advanced settings exposes an on-by-default safety switch; About Device shows
  profile status; Debloater marks explicitly listed critical packages.
- Existing direct package screens and arbitrary terminal commands are **not**
  covered by this guard. The known-package list is deliberately conservative.
- Android 17 / API 37 *runtime* support does not require changing the upstream
  targetSdk (36). A move to targetSdk 37 needs separate native-library loading
  and platform-behavior validation.
- One UI version is not reliably inferred from the device model.

Build (JDK 21, Android SDK, native dependencies and submodules required):

```sh
git clone --recurse-submodules https://github.com/minaric0/AppManager.git
cd AppManager
git checkout feature/sm-a175n-oneui9-android17
./gradlew :app:testDebugUnitTest
./gradlew packageDebugUniversalApk
```

For a downloadable debug APK, use the **SM-A175N debug APK** GitHub Actions
artifact generated on the pull request, or run the workflow manually after
it is present on the default branch. Debug uses the upstream
development signing key, not a secure release signing identity.

Device testing is required for package operations, shell/ADB reconnection,
multi-user/Knox cases, and compatibility with the exact installed firmware.
