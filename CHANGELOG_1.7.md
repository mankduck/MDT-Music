# MDT Music v1.7.0

- Người dùng nhập URL playlist YouTube, không còn ô API Key trong app.
- Nhà phát triển cấu hình GitHub Actions secret `MDT_YOUTUBE_API_KEY` một lần. Cần khóa API đúng và playlist công khai để chạy. Khóa đóng trong APK vẫn có nguy cơ bị trích xuất; môi trường nhiều người dùng nên chuyển sang backend bảo mật.
- Trang Quản lý: tìm theo tên bài hát, nghệ sĩ hoặc album; phân trang 20 kết quả, nút Trước/Sau, tổng kết quả.
- Giữ nguyên database và mã ứng dụng. Tăng versionCode lên 8, versionName 1.7.0. Cần APK cùng khóa ký để cập nhật dữ liệu cũ.
- Tính năng tải MP3 trực tiếp từ YouTube chưa có.
