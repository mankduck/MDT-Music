# MDT Music v1.1 — Sửa lỗi thư viện offline

- Giữ nguyên dữ liệu SQLite khi nâng cấp từ phiên bản 1: tạo bảng albums ở database version 2.
- Cho tạo album riêng (kể cả album chưa có bài), tìm và chọn nhiều bài hát để gán vào album.
- Dropdown chọn album có sẵn trong biểu mẫu thêm/sửa bài hát, có lựa chọn không chọn album.
- Khi nhập audio, chỉ tự điền tên bài dựa trên tên tệp; tên tác giả, mô tả, lời, chủ đề, album để trống.
- Copy tệp MP3 và ảnh từ Storage Access Framework trên thread riêng, có dialog tiến trình chặn thao tác khi nhập.
- Sửa chọn ảnh: giữ nguyên dialog chỉnh sửa, xem trước ngay sau khi chọn và lưu đường dẫn ảnh.
- Thông báo thành công sau khi lưu; khóa nút lưu trong thời gian ghi database để tránh lưu lặp.
- Khi nhấn Play, đẩy queue kèm Intent tới playback service, kiểm tra đường dẫn tệp, in lỗi vào Logcat và Toast khi MediaPlayer không phát được.

## Build trên GitHub
Đẩy những file đã giải nén vào repository, giữ cả `.github/workflows/build-apk.yml`. Vào Actions > Build MDT Music APK > Run workflow; tải Artifact `MDT-Music-debug-APK` nếu build thành công.

**Giới hạn kiểm thử:** Chưa được compile bằng Android SDK hoặc thử trên thiết bị trong môi trường tạo bản ZIP này. Khi gặp build lỗi, gửi log phần Build debug APK. Nếu app đã lưu dữ liệu trước đó, cài APK phiên bản mới đè lên APK cũ với cùng applicationId, không gỡ cài đặt nếu cần giữ bài hát.
