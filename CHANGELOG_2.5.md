# MDT Music v2.5.0 — Khu vực riêng tư (bản thử nghiệm)

- Mã bí mật `0909` tại màn nhập PIN chính (nếu đã bật khóa app), hoặc Cài đặt → Bảo mật → Mở khu vực riêng tư (nếu không bật PIN chính).
- Thêm `VaultActivity`, chọn nhiều ảnh/video bằng Android Storage Access Framework, sao chép và mã hóa AES-256-GCM bằng Android Keystore vào `files/vault/`, mục lục cũng được mã hóa.
- Kho độc lập với thư viện MP3 và MediaStore. Không tự xóa file nguồn. Chỉ khi báo nhập thành công và xem được nội dung mới nên tự xóa bản gốc qua ứng dụng Thư viện.
- Có PIN lớp hai (4–8 chữ số), tùy chọn sinh trắc học (Android 9+, phải thiết lập PIN lớp hai), tự khóa khi thoát, FLAG_SECURE chống screenshot và ảnh chụp trong đa nhiệm.
- Mở ảnh trong ứng dụng. Video tạo bản giải mã tạm trong cache riêng khi xem, sau khi đóng sẽ xóa; cache không an toàn tuyệt đối khi thiết bị root hoặc máy đang bị xâm nhập.
- Có thao tác xóa file riêng tư (không có thùng rác), hướng dẫn mở Cài đặt Xiaomi để ẩn app khác; không tự thay đổi tên/icon hoặc ẩn ứng dụng đã cài.

**Chưa hoàn thiện:** sao lưu/khôi phục kho ẩn với mật khẩu độc lập, xem ảnh/video dung lượng cực lớn, dọn file tạm khi ứng dụng bị cưỡng bức kết thúc, bảo vệ khỏi phân tích trên thiết bị đã root; chưa biên dịch và thử APK trên Xiaomi 13. Các mục này không nên coi là tính năng đã được kiểm chứng.

**Cảnh báo:** Các khóa Android Keystore không được sao lưu theo tệp; gỡ cài đặt hoặc xóa dữ liệu sẽ khiến kho ẩn không thể giải mã. KHÔNG xóa ảnh/video gốc nếu chưa có bản sao lưu độc lập kiểm chứng được. PIN bí mật `0909` là mã cố định và không phải lớp bảo mật chính.
