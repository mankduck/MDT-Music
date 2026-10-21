# MDT Music – Android Offline Player

Dự án Android Studio **Java + Android SDK**, hoạt động hoàn toàn offline khi nghe nhạc.

## Đã triển khai
- **Trang chủ:** nhóm album theo metadata, mở album ra danh sách bài hát, danh sách nhạc gần đây.
- **Thư viện:** tìm kiếm tên/ca sĩ/album; lọc theo chủ đề tùy nhập.
- **Quản lý:** dùng bộ chọn tệp Android nhập nhạc đã tải về từ máy (MP3 và định dạng MediaPlayer hỗ trợ); sao chép nhạc vào bộ nhớ riêng; chỉnh tên, ca sĩ, album, chủ đề, mô tả, lời bài hát; gắn ảnh bìa; xóa bài hát.
- **Cài đặt:** sáng/tối; điều chỉnh độ sáng *trong cửa sổ ứng dụng*; đặt, đổi, tắt PIN số (lưu SHA-256); giao diện tiếng Việt.
- **Phát nhạc:** dịch vụ foreground media playback để tiếp tục phát khi khóa màn hình hoặc rời ứng dụng, thông báo có nút trước/phát-tạm dừng/sau; mini-player, full-player, đĩa quay, sóng hoạt họa, lời bài hát, tua nhạc, chuyển bài, phát lặp, hẹn giờ ngủ.

## Build APK
1. Cài **Android Studio** mới và Android SDK Platform **35**.
2. Chọn **Open** → thư mục `MDTOfflineMusic`.
3. Đợi Gradle Sync (yêu cầu Java 17, Android Gradle Plugin 8.7.3; tải Gradle qua Android Studio).
4. Vào **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
5. File debug APK tại `app/build/outputs/apk/debug/app-debug.apk`.
6. Cài APK lên máy Android 8.0 (API 26) trở lên. Với Android 13+, cho phép quyền thông báo nếu muốn dùng điều khiển trên thông báo.

## Giới hạn phiên bản khởi đầu
- **Không kèm nhạc có bản quyền**: sau khi cài hãy dùng tab Quản lý để chọn những file MP3 mà bạn sở hữu/quyền sử dụng, kể cả file vừa tải vào thư mục Downloads. Chưa có trình tải qua đường link trong app.
- Visualizer là **sóng hoạt họa minh họa** khi phát, chưa phân tích phổ âm thanh thực tế.
- Lời nhạc là văn bản cuộn, chưa đồng bộ từng câu theo timestamp LRC.
- Lựa chọn ngôn ngữ chưa làm đa ngôn ngữ, hiện chỉ có tiếng Việt.
- PIN chỉ khóa giao diện, không mã hóa file nhạc, không thay thế cơ chế bảo mật của Android.
- Chưa hỗ trợ playlist tự tạo, shuffle, EQ hoặc import hàng loạt.
- Android Studio cần tải các dependencies lúc build đầu tiên; bản APK sau khi build không cần mạng khi nghe nhạc.
- Đây là **source project để build**, chưa kiểm tra compile bằng Android SDK trong môi trường tạo file.

## Cấu trúc chính
`MusicDb.java`: SQLite bài hát. `PlayerService.java`: Android foreground media player. `MainActivity.java`: giao diện + import + chỉnh sửa. `DiscView.java`: đĩa than vẽ bằng Canvas. `WaveView.java`: sóng hoạt họa.


## Cập nhật v1.8.0
- Chuyển mục nhập playlist YouTube từ tab **Quản lý** sang **Cài đặt**.
- Đồng bộ màu header/footer của hộp thoại kết quả playlist YouTube với giao diện sáng/tối, bỏ hai dải đen thừa.
- Giữ nguyên cơ chế đọc playlist bằng URL, chưa tải MP3.
