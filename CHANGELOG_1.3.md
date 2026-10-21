# MDT Music 1.3 — PIN, safe areas, mini player, album dropdown

- PIN: giữ 4 ô hiển thị, bỏ ô nhập PIN thứ hai/đường gạch dưới; bàn phím số vẫn xuất hiện khi nhập, ẩn/hiện PIN bằng nút bên cạnh.
- Áp dụng WindowInsets systemBars/displayCutout để giữ giao diện khỏi thanh trạng thái và thanh điều hướng trên Android 15 (bao gồm Xiaomi 13 khi bật edge-to-edge), hỗ trợ API cũ bằng fitsSystemWindows.
- Mini player: nền xám nhẹ khi chế độ sáng; khoảng cách với menu, giữa các nút và nút x nhỏ để dừng nhạc và đóng player.
- Ô chọn album khi chỉnh sửa/nhập bài hát có nền trắng và chữ tối kể cả khi ở chế độ sáng.
- Giữ workflow Github Actions tự động build APK.

Lưu ý: chưa có Android SDK trong môi trường để build và kiểm thử trên thiết bị. Cần kiểm tra thực tế APK bằng GitHub Actions và máy Xiaomi 13.
