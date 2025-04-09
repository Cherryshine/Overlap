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

            const offcanvasProfileImage = document.getElementById('offcanvasProfileImage');
            if (offcanvasProfileImage) {
                offcanvasProfileImage.src = rawUrl;
                console.log("✅ 오프캔버스 프로필 이미지 적용 완료:", rawUrl);
            }
            
            // 사용자 이름이 있으면 설정
            if (data.name) {
                const userName = document.getElementById('userName');
                if (userName) {
                    userName.textContent = data.name;
                }
            }
        })
        .catch(error => {
            console.error('❌ Error:', error);
        });
}

// DOM이 로드되면 프로필 이미지 로드
document.addEventListener('DOMContentLoaded', loadProfileImage);