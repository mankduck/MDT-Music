# MDT Music v2.1.0 — tải nhạc / UX

- Tiếp tục sử dụng yt-dlp + FFmpeg từ v2.0.3 cho thao tác tải trực tiếp trên thiết bị.
- Kiểm tra source video ID đã có MP3 trong thư viện; cho phép bỏ qua hoặc tải bản khác.
- Bổ sung hành động Thử lại, Bỏ qua bài, Dừng hàng đợi khi một bài tải lỗi.
- Hiển thị thông báo rõ hơn cho HTTP 403, lỗi FFmpeg, lỗi trích xuất.
- Giữ applicationId, đường dẫn file và database; versionCode 15.

**Không tích hợp API của YTDown**: chưa có API chính thức được xác minh.
**Không bảo đảm xử lý được lỗi HTTP 403**: việc thử lại hoặc cập nhật extractor không đồng nghĩa YouTube cho phép tải.
**Chưa build/test APK trong môi trường này.**
