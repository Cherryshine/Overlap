// 글로벌 변수: 선택된 셀을 저장하는 객체
window.selectedCells = window.selectedCells || {};

/*****************************************
 * 시간 관련 유틸리티 함수
 *****************************************/

/**
 * "HH:MM" 형식의 문자열을 파싱하여 시간 객체를 반환합니다.
 * @param {string} str - "HH:MM" 형식의 시간 문자열
 * @returns {{hour: number, minute: number}} - 파싱된 시간 객체
 */
function parseTimeString(str) {
    const [hh, mm] = str.split(":");
    return { hour: parseInt(hh, 10), minute: parseInt(mm, 10) };
}

/**
 * 시간과 분을 총 분 수로 변환합니다.
 * @param {number} h - 시간
 * @param {number} m - 분
 * @returns {number} 총 분 수
 */
function toTotalMinutes(h, m) {
    return h * 60 + m;
}

/**
 * 총 분 수를 "HH:MM" 형식의 문자열로 변환합니다.
 * @param {number} totalMin - 총 분 수
 * @returns {string} "HH:MM" 형식의 문자열
 */
function formatTime24(totalMin) {
    const h = Math.floor(totalMin / 60);
    const min = totalMin % 60;
    const hh = h < 10 ? "0" + h : "" + h;
    const mm = min < 10 ? "0" + min : "" + min;
    return `${hh}:${mm}`;
}

/**
 * 24시간을 12시간 형식의 레이블(오전/오후)로 변환하여 반환합니다.
 * @param {number} hour24 - 24시간 형식의 시간
 * @returns {string} "오전/오후 X시" 형식의 문자열
 */
function formatHourLabel(hour24) {
    let ampm = hour24 < 12 ? "오전" : "오후";
    let hour12 = hour24 % 12;
    if (hour12 === 0) hour12 = 12;
    return `${ampm} ${hour12}시`;
}

/*****************************************
 * 시간표 HTML 생성 함수
 *****************************************/

/**
 * 시간표 테이블의 HTML을 생성합니다.
 * @param {Object} data - 시간표 데이터 (startTime, endTime, interval, days, startIndex, totalDays 등)
 * @returns {string} 생성된 HTML 문자열
 */
function buildScheduleTable(data) {
    const start = parseTimeString(data.startTime);
    const end = parseTimeString(data.endTime);
    const startTotal = toTotalMinutes(start.hour, start.minute);
    const endTotal = toTotalMinutes(end.hour, end.minute);
    const interval = data.interval;
    const totalBlocks = Math.floor((endTotal - startTotal) / interval);

    // 테두리 유무 판단
    const hasPrevPage = data.startIndex > 0;
    const hasNextPage = data.startIndex + data.days.length < data.totalDays;

    // 왼쪽 시간 바 (날짜헤더와 좌측 여백 통일을 위해 w-16 사용)
    let timeBarHtml = `<div class="relative w-16 mr-2">`;
    for (let h = start.hour; h <= end.hour; h++) {
        const hourIndex = h - start.hour;
        const topPos = hourIndex * 160;
        timeBarHtml += `
            <div class="time-label absolute left-0 text-sm font-semibold"
                 style="top: ${topPos}px; transform: translateY(-50%)">
                ${formatHourLabel(h)}
            </div>
        `;
    }
    timeBarHtml += `</div>`;

    // 오른쪽 시간표 생성: grid 컨테이너에 relative 클래스 추가
    let timeGridHtml = `<div class="flex-1 relative">`;

    for (let i = 0; i < totalBlocks; i++) {
        const blockStartMin = startTotal + i * interval;
        const isHourMark = blockStartMin % 60 === 0;

        timeGridHtml += `
        <div class="grid grid-cols-${data.days.length} h-6 sm:h-8 ${i === 0 ? 'grid-row-first' : ''}">
    `;

        for (let c = 0; c < data.days.length; c++) {
            const cellKey = `${data.startIndex}-${data.days[c].date}-${i}`;
            // 미리보기 모드라면 셀이 선택되어 있을 경우 반투명 파란 오버레이를 적용, 그렇지 않으면 기존 녹색 선택 클래스 적용
            let overlayHtml = "";
            let selectedClass = "";
            if (window.selectedCells && window.selectedCells[cellKey]) {
                if (window.isPreviewMode) {
                    overlayHtml = `<div class="preview-overlay" style="position: absolute; top:0; left:0; width:100%; height:100%; background: rgba(0, 0, 255, 0.5); pointer-events: none;"></div>`;
                } else {
                    selectedClass = "bg-green-300";
                }
            }

            let borderClasses = `grid-cell bg-gray-200 ${isHourMark ? 'hour-mark' : ''} ${selectedClass}`;
            let borderStyle = "border-right: 1px solid rgb(209, 213, 219)";

            if (hasPrevPage && c === 0) {
                borderStyle = `
                    border-right: 1px solid rgb(209, 213, 219);
                    clip-path: polygon(
                        8px 0%, 100% 0%,
                        100% 100%, 8px 100%,
                        0px 75%, 8px 50%,
                        0px 25%
                    );
                    padding-left: 8px;" 
                `;
            } else if (hasNextPage && c === data.days.length - 1) {
                borderStyle = `
                    clip-path: polygon(
                        0% 0%, calc(100% - 8px) 0%,
                        100% 25%, calc(100% - 8px) 50%,
                        100% 75%, calc(100% - 8px) 100%,
                        0% 100%
                    );
                    padding-right: 8px;
                `;
            } else if (!hasPrevPage && c === 0) {
                borderStyle = "border-left: 1px solid rgb(209, 213, 219); border-right: 1px solid rgb(209, 213, 219)";
            }

            // 다른 사람의 스케줄을 표시하는 로직 제거됨.
            let otherUserOverlayHtml = "";

            timeGridHtml += `
                <div data-cell-key="${cellKey}" class="${borderClasses} relative" style="${borderStyle}">
                    ${overlayHtml}
                    <!-- ${formatTime24(blockStartMin)} -->
                    ${otherUserOverlayHtml}
                </div>
            `;
        }
        timeGridHtml += `</div>`;
    }
    timeGridHtml += `</div>`;

    return `
        <div class="flex">
            ${timeBarHtml}
            ${timeGridHtml}
        </div>
    `;
}

/*****************************************
 * 기타 유틸리티 및 이벤트 핸들러
 *****************************************/

/**
 * 컨테이너 크기 변경을 감지하여 시간바 위치를 조정합니다.
 * @param {number} interval - 시간 간격 (분)
 */
function setupResizeObserver(interval) {
    const container = document.getElementById("app");
    // ResizeObserver가 지원되는 경우
    if (container && window.ResizeObserver) {
        const observer = new ResizeObserver(() => {
            adjustTimeBarPositions(interval);
        });
        observer.observe(container);
    } else {
        // ResizeObserver 미지원 시, debouncing을 적용한 resize 이벤트 사용
        window.addEventListener("resize", debounce(() => {
            adjustTimeBarPositions(interval);
        }, 100));
    }
}

/**
 * 지속적인 이벤트 발생 시 호출 횟수를 줄이기 위해 함수 실행을 지연합니다.
 * @param {Function} fn - 지연 후 실행할 함수
 * @param {number} delay - 지연 시간 (ms)
 * @returns {Function} 지연된 함수
 */
function debounce(fn, delay) {
    let timer = null;
    return function(...args) {
        if (timer) clearTimeout(timer);
        timer = setTimeout(() => {
            fn.apply(this, args);
        }, delay);
    };
}

/**
 * 시간표 및 관련 UI를 업데이트합니다.
 */
function updateSchedule() {
    const startIndex = currentPage * itemsPerPage;
    const paginatedDays = jsonData.days.slice(startIndex, startIndex + itemsPerPage);

    // 요일, 월 한글 변환 객체는 그대로 유지
    const dayKorean = {
        'Sun': '일',
        'Mon': '월',
        'Tue': '화',
        'Wed': '수',
        'Thu': '목',
        'Fri': '금',
        'Sat': '토'
    };

    const monthKorean = {
        'Jan': '1월',
        'Feb': '2월',
        'Mar': '3월',
        'Apr': '4월',
        'May': '5월',
        'Jun': '6월',
        'Jul': '7월',
        'Aug': '8월',
        'Sep': '9월',
        'Oct': '10월',
        'Nov': '11월',
        'Dec': '12월'
    };

    let dayHeaderHtml = `
        <div class="flex mb-2">
            <div class="w-16 mr-2"></div>
            <div class="flex-1">
                <div class="grid" style="grid-template-columns: repeat(${paginatedDays.length}, 1fr)">
                    ${paginatedDays.map((d, index) => {
        const [month, day] = d.date.split(" ");
        return `
                            <div class="text-center" 
                                 style="${index !== paginatedDays.length - 1 ? 'border-right: 1px solid #d1d5db;' : ''}">
                                <div class="text-sm text-gray-500">${dayKorean[d.label]}요일</div>
                                <div class="text-lg font-semibold">${monthKorean[month]} ${day}일</div>
                            </div>
                        `;
    }).join('')}
                </div>
            </div>
        </div>
    `;

    // 빌드된 시간표 HTML (시간 바 + 셀 영역)
    const tableHTML = buildScheduleTable({
        ...jsonData,
        days: paginatedDays,
        startIndex,
        totalDays: jsonData.days.length
    });

    // 기존 페이지 버튼 컨테이너 제거하고, dayHeaderHtml과 tableHTML만 삽입
    const app = document.getElementById("app");
    app.innerHTML = dayHeaderHtml + tableHTML;

    // 테이블 렌더링 후, 셀 영역(.flex-1.relative)에 이전/다음 버튼을 삽입합니다.
    setTimeout(() => {
        const gridContainer = document.querySelector('.flex-1.relative');
        if (gridContainer) {
            // 이전 페이지 버튼 (셀 영역의 왼쪽)
            if (currentPage > 0) {
                const btnPrev = document.createElement('button');
                btnPrev.id = "prevPage";
                // 필요에 따라 left값을 조절하세요.
                btnPrev.className = "absolute left-[-20px] top-1/2 transform -translate-y-1/2 page-button";
                btnPrev.innerText = "←";
                gridContainer.appendChild(btnPrev);
            }
            // 다음 페이지 버튼 (셀 영역의 오른쪽)
            if (startIndex + itemsPerPage < jsonData.days.length) {
                const btnNext = document.createElement('button');
                btnNext.id = "nextPage";
                // 필요에 따라 right값을 조절하세요.
                btnNext.className = "absolute right-[-20px] top-1/2 transform -translate-y-1/2 page-button";
                btnNext.innerText = "→";
                gridContainer.appendChild(btnNext);
            }

            // 버튼 클릭 이벤트 등록
            const prevPageBtn = document.getElementById("prevPage");
            const nextPageBtn = document.getElementById("nextPage");
            if (prevPageBtn) {
                prevPageBtn.addEventListener("click", function() {
                    if (currentPage > 0) {
                        currentPage--;
                        updateSchedule();
                    }
                });
            }
            if (nextPageBtn) {
                nextPageBtn.addEventListener("click", function() {
                    if ((currentPage + 1) * itemsPerPage < jsonData.days.length) {
                        currentPage++;
                        updateSchedule();
                    }
                });
            }
        }
    }, 0);

    // 테이블 렌더링 후 시간바 위치 조정
    setTimeout(() => {
        adjustTimeBarPositions(jsonData.interval);
    }, 0);

    // 창 크기 변화에 따른 시간바 조정
    setupResizeObserver(jsonData.interval);

    setTimeout(() => {
        adjustTimeBarPositions(jsonData.interval);
    }, 0);

    // 드래그 선택 기능 관련 함수 호출 제거됨.
}

/**
 * 각 시간별 셀의 높이를 기반으로 시간바의 위치를 조정합니다.
 * @param {number} interval - 시간 간격 (분)
 */
function adjustTimeBarPositions(interval) {
    // 우선 셀 중 하나를 선택하여 실제 높이를 측정 (h-10 클래스가 적용된 셀)
    const cell = document.querySelector(".grid-cell");
    if (cell) {
        const cellHeight = cell.offsetHeight;
        // 1시간은 60분이므로, interval 당 셀 1개 높이에서 1시간 높이는 아래와 같이 계산합니다.
        const hourHeight = cellHeight * (60 / interval);
        document.querySelectorAll(".time-label").forEach((label, index) => {
            label.style.top = (hourHeight * index) + "px";
        });
    }
}

/**
 * 선택된 셀 정보를 업데이트하여 화면에 반영하고 로컬스토리지에 저장합니다.
 */
function updateSelectedInfo() {
    const selectedInfoDiv = document.getElementById("selectedInfo");
    if (!selectedInfoDiv) return;

    // 날짜별로 선택된 시간 범위를 모으기 위한 객체 생성
    const groupedSelections = {};
    const startTimeObj = parseTimeString(jsonData.startTime);
    const startTotalMin = toTotalMinutes(startTimeObj.hour, startTimeObj.minute);

    for (const key in window.selectedCells) {
        if (!window.selectedCells[key]) continue;
        const parts = key.split("-");
        if (parts.length < 3) continue;
        const dateKey = parts[1];
        const blockIndex = parseInt(parts[2], 10);
        const blockStart = startTotalMin + blockIndex * jsonData.interval;
        const blockEnd = blockStart + jsonData.interval;
        const timeRange = `${formatTime24(blockStart)} ~ ${formatTime24(blockEnd)}`;

        if (!groupedSelections[dateKey]) {
            groupedSelections[dateKey] = [];
        }
        groupedSelections[dateKey].push(timeRange);
    }

    let finalHtml = "";
    for (const dateKey in groupedSelections) {
        const uniqueRanges = Array.from(new Set(groupedSelections[dateKey])).sort();
        finalHtml += `<div class="mb-2"><strong>${dateKey}</strong>: ${uniqueRanges.join(", ")}</div>`;
    }
    selectedInfoDiv.innerHTML = finalHtml || "선택된 날짜 및 시간이 없습니다.";

    // 선택된 셀 정보를 로컬 스토리지에 저장하여 페이지 전환 후에도 유지
    localStorage.setItem("selectedCells", JSON.stringify(window.selectedCells));
}

// 전역 노출 함수
window.buildScheduleTable = buildScheduleTable;