function loadProfileImage() {
    console.log("🔄 프로필 이미지 불러오기 시작...");

    fetch('/get_user/profile')
        .then(response => response.json())
        .then(data => {
            console.log("✅ 서버 응답:", data);

            let rawUrl = data.thumbnailImageUrl;
            if (rawUrl && rawUrl.includes("?fname=")) {
                rawUrl = rawUrl.split("?fname=")[1];
            }

            console.log("🔍 최종 적용할 이미지 URL:", rawUrl);

            const profileImage = document.getElementById('thumbnailImageUrl');
            if (profileImage) {
                profileImage.src = rawUrl;
                console.log("✅ 적용된 이미지 URL:", profileImage.src);
            } else {
                console.error("❌ `thumbnailImageUrl` 요소를 찾을 수 없음");
            }
        })
        .catch(error => {
            console.error('❌ Error:', error);
        });
}

// 🔥 페이지가 완전히 로드된 후 실행
window.onload = loadProfileImage;