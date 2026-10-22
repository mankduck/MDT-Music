# Cập nhật không xung đột chữ ký — MDT Music v2.3.1

1. Tải riêng file `MDTMusic_SIGNING_SECRETS_PRIVATE.txt` từ cuộc trò chuyện. **KHÔNG upload file đó hoặc tệp .jks lên GitHub**, kể cả repo private; giữ bản sao offline an toàn.
2. Mở https://github.com/mankduck/MDT-Music/settings/secrets/actions -> **New repository secret**. Tạo đủ 4 secret, sao chép tên và giá trị theo file riêng: `MDT_KEYSTORE_BASE64`, `MDT_KEYSTORE_PASSWORD`, `MDT_KEY_ALIAS`, `MDT_KEY_PASSWORD`. Nếu có `MDT_YOUTUBE_API_KEY`, giữ nguyên.
3. Upload source v2.3.1 vào repository qua GitHub Desktop (Commit, Push).
4. Vào Actions > **Build MDT Music Signed APK**. Chỉ tải artifact **MDT-Music-RELEASE-update**, không dùng APK debug.
5. Nếu bản trên máy được ký bằng khóa debug cũ khác khóa mới, **không thể cài đè lần đầu**. Sao lưu MP3/dữ liệu có thể truy cập và hiểu rằng gỡ app sẽ xóa dữ liệu nội bộ. Sau khi cài bản release v2.3.1 mới, tất cả bản tiếp theo phải dùng cùng 4 secrets, tăng versionCode để cài đè và giữ dữ liệu.

Quan trọng: nếu mất keystore hoặc thay bất kỳ bí mật ký nào, cập nhật có thể bị từ chối. Sao lưu file bí mật vào nơi an toàn. Không chia sẻ công khai.
