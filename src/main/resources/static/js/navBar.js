// 레이아웃 핸들러 - 2024.06.14 업데이트됨
document.addEventListener('DOMContentLoaded', function() {
    console.log("🔄 DOM 로드됨, 프로필 로딩 시작");
    loadUserProfile();
});

// 사용자 프로필 로드 함수
function loadUserProfile() {
    // 캐시 방지 쿼리 파라미터 추가
    const timestamp = new Date().getTime();
    const url = `/get_user/profile?t=${timestamp}`;
    
    console.log("🔄 프로필 정보 요청 시작:", url);
    
    fetch(url)
        .then(response => {
            console.log("🔄 서버 응답 상태:", response.status);
            return response.json();
        })
        .then(data => {
            console.log("✅ 응답 데이터:", data);
            
            // 게스트 여부 확인 - 다양한 방식으로 체크
            const isGuest = checkIfGuest(data);
            
            if (isGuest) {
                console.log("👤 게스트 사용자로 처리합니다");
                handleGuestUser();
            } else {
                console.log("👤 로그인된 사용자로 처리합니다");
                handleLoggedInUser(data);
            }
        })
        .catch(error => {
            console.error("❌ 오류 발생:", error);
            // 오류 발생 시 게스트로 처리
            handleGuestUser();
        });
}

// 게스트 여부 확인 함수
function checkIfGuest(data) {
    console.log("🔍 게스트 여부 확인 중...");
    
    // 여러 방식으로 게스트 체크
    if (!data) {
        console.log("👤 데이터 없음 -> 게스트");
        return true;
    }
    
    if (data.usertype === 'guest') {
        console.log("👤 usertype이 guest -> 게스트");
        return true;
    }
    
    if (data.userType === 'guest') {
        console.log("👤 userType이 guest -> 게스트");
        return true;
    }
    
    const dataString = JSON.stringify(data).toLowerCase();
    if (dataString.includes('guest')) {
        console.log("👤 응답에 guest 포함 -> 게스트");
        return true;
    }
    
    if (!data.thumbnailImageUrl) {
        console.log("👤 프로필 이미지 없음 -> 게스트");
        return true;
    }
    
    console.log("👤 게스트 아님 -> 일반 사용자");
    return false;
}

// 게스트 사용자 처리 함수
function handleGuestUser() {
    console.log("🔧 게스트 UI 설정 시작");
    
    // 1. 프로필 이미지 설정
    const profileImage = document.getElementById('offcanvasProfileImage');
    if (profileImage) {
        profileImage.src = '/images/guest_profile.png';
        console.log("✅ 게스트 프로필 이미지 설정 완료");
    } else {
        console.log("❌ 프로필 이미지 요소를 찾을 수 없음");
    }
    
    // 2. 이름 설정
    const userName = document.getElementById('userName');
    if (userName) {
        userName.textContent = '게스트';
        console.log("✅ 게스트 이름 설정 완료");
    }
    
    // 3. 마이페이지 메뉴 숨기기
    hideMyPageElements();
    
    // 4. 로그아웃 버튼을 로그인 버튼으로 변경
    const logoutButton = document.querySelector('a[onclick="kakaoLogout()"]');
    if (logoutButton) {
        logoutButton.setAttribute('onclick', '');
        logoutButton.setAttribute('href', '/login');
        
        // 아이콘과 텍스트 변경
        const icon = logoutButton.querySelector('i');
        if (icon) icon.className = 'fas fa-sign-in-alt';
        
        const span = logoutButton.querySelector('span');
        if (span) span.textContent = '로그인';
        console.log("✅ 로그인 버튼으로 변경 완료");
    }
    
    console.log("🔧 게스트 UI 설정 완료");
}

// 로그인된 사용자 처리 함수
function handleLoggedInUser(data) {
    console.log("🔧 로그인 사용자 UI 설정 시작");
    
    // 1. 마이페이지 메뉴 표시
    showMyPageElements();
    
    // 2. 프로필 이미지 설정
    try {
        let imageUrl = data.thumbnailImageUrl;
        
        // URL 처리
        if (typeof imageUrl === 'string') {
            // JSON 문자열인 경우 파싱
            if (imageUrl.trim().startsWith('{')) {
                try {
                    const parsed = JSON.parse(imageUrl);
                    imageUrl = parsed.url || '';
                } catch (e) {
                    console.error("❌ JSON 파싱 실패:", e);
                }
            }
            
            // 카카오 이미지 URL 처리
            if (imageUrl.includes("?fname=")) {
                imageUrl = imageUrl.split("?fname=")[1];
            }
            
            // 상대 경로 처리
            if (imageUrl && !imageUrl.startsWith("http")) {
                imageUrl = `/images/${imageUrl}`;
            }
            
            // 이미지 적용
            const profileImage = document.getElementById('offcanvasProfileImage');
            if (profileImage) {
                profileImage.src = imageUrl;
                console.log("✅ 프로필 이미지 설정 완료:", imageUrl);
            }
        } else {
            console.error("❌ 이미지 URL이 유효하지 않음");
            document.getElementById('offcanvasProfileImage').src = '/images/default_profile.png';
        }
    } catch (error) {
        console.error("❌ 프로필 이미지 설정 중 오류:", error);
        document.getElementById('offcanvasProfileImage').src = '/images/default_profile.png';
    }
    
    // 3. 사용자 이름 설정
    if (data.name) {
        const userName = document.getElementById('userName');
        if (userName) {
            userName.textContent = data.name;
            console.log("✅ 사용자 이름 설정 완료:", data.name);
        }
    }
    
    console.log("🔧 로그인 사용자 UI 설정 완료");
}

// 마이페이지 관련 요소 숨기기
function hideMyPageElements() {
    // 마이페이지 관련 메뉴 항목 숨기기
    try {
        // 내가 만든 일정
        const mySchedulesLink = document.querySelector('a[href="/my-schedules"]');
        if (mySchedulesLink) {
            const mySchedulesItem = mySchedulesLink.closest('li');
            if (mySchedulesItem) mySchedulesItem.style.display = 'none';
        }
        
        // 참여중인 일정
        const participatingLink = document.querySelector('a[href="/participating-schedules"]');
        if (participatingLink) {
            const participatingItem = participatingLink.closest('li');
            if (participatingItem) participatingItem.style.display = 'none';
        }
        
        // 마이페이지
        const myPageLink = document.querySelector('a[href="/mypage"]');
        if (myPageLink) {
            const myPageItem = myPageLink.closest('li');
            if (myPageItem) myPageItem.style.display = 'none';
        }
        
        // 내 일정 섹션 숨기기
        const mySchedulesSection = document.querySelector('.offcanvas-body div:nth-child(1)');
        if (mySchedulesSection) {
            mySchedulesSection.style.display = 'none';
        }
        
        console.log("✅ 마이페이지 메뉴 숨김 처리 완료");
    } catch (error) {
        console.error("❌ 마이페이지 메뉴 숨김 처리 중 오류:", error);
    }
}

// 마이페이지 관련 요소 표시
function showMyPageElements() {
    try {
        // 내가 만든 일정
        const mySchedulesLink = document.querySelector('a[href="/my-schedules"]');
        if (mySchedulesLink) {
            const mySchedulesItem = mySchedulesLink.closest('li');
            if (mySchedulesItem) mySchedulesItem.style.display = '';
        }
        
        // 참여중인 일정
        const participatingLink = document.querySelector('a[href="/participating-schedules"]');
        if (participatingLink) {
            const participatingItem = participatingLink.closest('li');
            if (participatingItem) participatingItem.style.display = '';
        }
        
        // 마이페이지
        const myPageLink = document.querySelector('a[href="/mypage"]');
        if (myPageLink) {
            const myPageItem = myPageLink.closest('li');
            if (myPageItem) myPageItem.style.display = '';
        }
        
        // 내 일정 섹션 표시
        const mySchedulesSection = document.querySelector('.offcanvas-body div:nth-child(1)');
        if (mySchedulesSection) {
            mySchedulesSection.style.display = '';
        }
        
        console.log("✅ 마이페이지 메뉴 표시 처리 완료");
    } catch (error) {
        console.error("❌ 마이페이지 메뉴 표시 처리 중 오류:", error);
    }
}