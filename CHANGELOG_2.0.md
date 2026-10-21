# MDT Music 2.0.0 - thử nghiệm tải MP3 trực tiếp từ video

- Không hỏi đường dẫn MP3 nữa: chọn video trong playlist, bấm Tải MP3.
- Tích hợp youtubedl-android 0.18.1 + FFmpeg để thử tải và chuyển âm thanh sang MP3 ngay trên Android.
- Tải ảnh thumbnail nếu có, nếu không vẫn giữ MP3 hợp lệ.
- Lưu MP3/ảnh riêng của app, thêm metadata vào bảng bài hát chung.
- Chạy trên luồng nền, hiển thị % và kết quả thành công/thất bại mỗi video.
- Tăng versionCode 11, versionName 2.0.0; giữ applicationId.

CHƯA KIỂM THỬ APK. Cơ chế trích xuất video phụ thuộc yt-dlp, có thể thất bại do hạn chế của YouTube. Cần kiểm tra build GitHub Actions và test Xiaomi 13. Thư viện yt-dlp có GPL-3.0, lưu ý nghĩa vụ giấy phép khi phân phối.
