# MDT Music v1.6.0 — cập nhật APK và playlist YouTube

## Đã triển khai
- Giữ nguyên `applicationId=com.mdt.offlinemusic`; `versionCode=7`, `versionName=1.6.0`.
- Trang Quản lý có nút đọc **playlist YouTube công khai** qua **YouTube Data API v3** khi có Internet.
- Dán API key của dự án Google Cloud (bật YouTube Data API v3) và URL playlist công khai. Hỗ trợ tìm kiếm video, mở video trên YouTube. Không tải âm thanh từ YouTube.
- Không thay đổi schema SQLite (version 3), không ghi đè thư viện MP3, không cần Internet để phát MP3 nhập trước đó.
- Workflow tạo APK debug; nếu đã thiết lập 4 GitHub Secrets ký app thì build thêm APK release có chữ ký ổn định.

## Chưa làm / cần cấu hình
- **Đăng nhập Google OAuth, truy cập playlist riêng tư**, và đồng bộ tài khoản chưa triển khai: cần Google Cloud OAuth client, consent screen, chính sách dữ liệu, xác minh khi mở rộng người dùng.
- **Tải MP3 từ YouTube** không được cung cấp bởi API chính thức; không triển khai cơ chế tải trái điều khoản. Bộ tải riêng từ nguồn cấp phép có thể làm giai đoạn sau.
- Playlist từ YouTube chỉ là danh sách tham chiếu online; không hòa vào bảng `songs` vì chưa có tệp MP3. File MP3 nhập/tải từ nguồn hợp lệ sẽ được lưu chung như trước.

## Để APK mới cài đè và giữ dữ liệu
Android yêu cầu **cùng applicationId, cùng chứng chỉ ký, versionCode mới không thấp hơn**. Bản debug của GitHub Actions có thể có khóa ký ngẫu nhiên vì mỗi runner khác nhau, do đó KHÔNG THỂ bảo đảm cập nhật đè phiên bản debug đã cài.

### Phát hành bản cập nhật ổn định cho các bản tương lai
1. Tạo **một** khóa keystore bằng `keytool` trong môi trường an toàn, sao lưu ở nơi riêng và không commit lên GitHub.
2. Trong Repository Settings -> Secrets and variables -> Actions, thêm:
   - `MDT_KEYSTORE_BASE64`: nội dung base64 của tệp .jks, một dòng.
   - `MDT_KEYSTORE_PASSWORD`: mật khẩu kho khóa.
   - `MDT_KEY_ALIAS`: tên alias.
   - `MDT_KEY_PASSWORD`: mật khẩu alias.
3. Chạy Actions, tải artifact `MDT-Music-release-signed`; dùng **cùng bốn secret** cho mọi phiên bản về sau.
4. Không thay `applicationId`, không thay khóa, tăng `versionCode` mỗi lần.

**Nếu APK v1.5 trên điện thoại đang ký bằng khóa debug khác**, APK release v1.6 sẽ báo xung đột chữ ký. Không có cách hợp lệ đổi khóa ký để cập nhật trực tiếp ứng dụng thường mà vẫn giữ dữ liệu một cách tự động. **Đừng gỡ app trước khi xuất/sao lưu nhạc.** Cần xây chức năng xuất/nhập thư viện (bao gồm MP3, ảnh, DB metadata) trước khi chuyển sang kênh ký ổn định. Tránh sử dụng `adb backup` làm chiến lược sao lưu đáng tin cậy trên Android đời mới.

## Google API
- Tạo project trong Google Cloud, kích hoạt YouTube Data API v3, tạo API key và giới hạn phù hợp.
- Đặt playlist YouTube ở chế độ Công khai (Public); playlist riêng tư cần OAuth chưa có trong bản này.
- Dán URL có tham số `list=...` trong mục Quản lý → Playlist YouTube.
- Quota và các quy định YouTube có thể thay đổi. API key hiện lưu trong SharedPreferences của app để tiện sử dụng; không dùng key đặc quyền hoặc không hạn chế; sản phẩm nhiều người dùng nên dùng backend hoặc cấu hình kiểm soát khóa tốt hơn.
