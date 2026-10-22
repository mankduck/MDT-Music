# MDT Music v2.4.0

## Đã bổ sung trong mã nguồn
- Cài đặt chia nhóm: Giao diện, Phát nhạc, Bộ nhớ & dữ liệu, Bảo mật, Ngôn ngữ và Thông tin ứng dụng.
- Chọn giao diện sáng, tối hoặc theo hệ thống; mặc định sáng, bảo toàn tùy chọn của phiên bản cũ.
- Bảng màu dựng sẵn: màu nút, màu chữ (theo từng chế độ sáng/tối) và màu nhạt cho các dòng xen kẽ và một số khu vực popup; khôi phục mặc định. Không phải tất cả popup hệ thống đều hỗ trợ đổi màu.
- Độ sáng mặc định theo máy, tùy chọn thanh trượt riêng.
- Thống kê kích thước tệp trong thư mục library của app.
- Sao lưu thư viện (bài hát hiện có và bài xóa mềm, MP3/M4A được lưu trong app, ảnh, album, metadata) sang ZIP qua Android document picker.
- Khôi phục từ ZIP của chính MDT Music: nhập thêm dữ liệu, không xóa thư viện cũ. Chú ý có thể tạo bản sao của bài hát nếu nhập nhiều lần.
- Mã PIN giữ nguyên; tốc độ phát vẫn thay đổi trong màn hình Đang phát.
- VersionCode = 21, versionName = 2.4.0; giữ applicationId và schema SQLite.

## Chưa triển khai
- Dịch toàn bộ giao diện sang tiếng Anh và khóa sinh trắc học (vẫn dùng mã PIN).
- Màu sắc toàn bộ thành phần thuộc giao diện hệ thống Android, dialog tùy nền tảng.
- Sao lưu trong khi nhạc đang tải hoặc tự động định kỳ.

## Lưu ý
Chưa có Android SDK tại môi trường soạn mã, do đó chưa thể biên dịch hoặc thử nghiệm APK. Giữ keystore ký APK hiện có để cập nhật mà không mất dữ liệu. Nên thử khôi phục trên thiết bị thử nghiệm trước khi sử dụng với thư viện quan trọng.
