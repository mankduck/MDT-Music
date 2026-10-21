# MDT Music v1.9.0

- Chọn nhiều video trong playlist YouTube và nhập từ các URL tệp âm thanh HTTPS được cấp quyền tải.
- Download file âm thanh vào `files/library` với thanh tiến trình và giới hạn 150 MB/tệp.
- Tải thumbnail tương ứng từ `i.ytimg.com`, lưu ở `files/covers`.
- Tự động thêm bài vào SQLite/Quản lý chung, lấy tiêu đề và tên kênh làm metadata gợi ý.
- Kiểm tra tệp có thời lượng âm thanh trước khi thêm, dọn file lỗi và báo lỗi theo từng bài.
- **Giới hạn:** YouTube Data API không cung cấp URL file MP3. Bản này không tự trích xuất audio từ URL video YouTube. Người dùng phải cung cấp URL audio HTTPS trực tiếp hợp lệ. Không chuyển mã M4A thành MP3; giữ nguyên M4A nếu nguồn là M4A.
- Cài đè chỉ thành công khi APK có cùng applicationId, chứng chỉ ký và versionCode tăng.
