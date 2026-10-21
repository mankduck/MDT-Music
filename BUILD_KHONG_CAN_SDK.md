# Build APK không cần cài Android Studio/SDK trên máy

## GitHub Actions
1. Giải nén ZIP, tạo repository mới trên GitHub.
2. Upload **nội dung bên trong thư mục MDTOfflineMusic**, bảo đảm `.github/workflows/build-apk.yml` nằm ở thư mục gốc repo, cùng cấp `settings.gradle`.
3. Mở repo > Actions > Build MDT Music APK > Run workflow > Run workflow.
4. Đợi job hoàn thành; vào run > Artifacts > tải MDT-Music-debug-APK.
5. Giải nén artifact để lấy `app-debug.apk` và cài lên Android.

Lưu ý: bản debug chỉ để thử nghiệm, không phù hợp phát hành trên Play Store. Nếu repo không hiển thị workflow, kiểm tra đã upload `.github` và GitHub Actions đã được bật.

Dự án chưa được build/test thực tế bằng Android SDK trong phiên này, nên quá trình build có thể phát hiện lỗi code cần sửa.

## CMD Windows
Không cài Android Studio thì vẫn có thể chạy `gradle :app:assembleDebug`, nhưng Windows cần JDK 17, Gradle 8.9 và Android SDK command-line tools + platform 35 + build tools. **Không thể build Android APK thực sự chỉ bằng CMD trống không có toolchain**.
