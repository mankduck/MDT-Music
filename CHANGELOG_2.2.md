# MDT Music 2.2.0

- Tùy chỉnh tốc độ phát 0.25×, 0.5×, 1×, 1.25×, 1.5×, 2×; đặt giữa Lặp và Hẹn giờ.
- Giữ tốc độ khi chuyển bài và qua lần mở app tiếp theo bằng SharedPreferences.
- Sửa UpdateChannel._STABLE để tránh lỗi biên dịch ở v2.1.0.
- Giữ applicationId `com.mdt.offlinemusic` và dữ liệu SQLite cũ; nâng versionCode từ 15 lên 16.
- Cài đè chỉ hợp lệ nếu APK cũ và mới cùng chữ ký. Các artifact debug GitHub Actions có thể khác chữ ký giữa các lần chạy.

**Chưa build hoặc kiểm thử APK trên thiết bị.**
