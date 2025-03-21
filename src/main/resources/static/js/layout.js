function loadProfileImage() {
    console.log("🔄 프로필 이미지 불러오기 시작...");

    fetch('/get_user/profile')
        .then(response => response.json())
        .then(data => {
            console.log("✅ 서버 응답:", data);

            let rawUrl = data.thumbnailImageUrl;

            if (!rawUrl) {
                console.error("❌ 서버에서 thumbnailImageUrl을 받지 못했습니다.");
                return;
            }

            // thumbnailImageUrl 안에 JSON이 문자열로 들어온 경우 파싱
            try {
                if (typeof rawUrl === 'string' && rawUrl.trim().startsWith('{')) {
                    const parsed = JSON.parse(rawUrl);
                    console.log("🔧 thumbnailImageUrl이 JSON 문자열이었음:", parsed);
                    rawUrl = parsed.url || '';  // 객체 키에 따라 조정
                }
            } catch (e) {
                console.error('❌ thumbnailImageUrl 파싱 실패:', e);
            }

            if (rawUrl.includes("?fname=")) {
                rawUrl = rawUrl.split("?fname=")[1];
            }

            if (!rawUrl.startsWith("http")) {
                rawUrl = `/images/${rawUrl}`;
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

window.onload = loadProfileImage;