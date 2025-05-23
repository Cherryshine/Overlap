// 글로벌 변수: 선택된 셀을 저장하는 객체
window.selectedCells = window.selectedCells || {};

// 초기화 플래그와 재시도 방지 변수
let isInitialized = false;
let isWaitingForJsonData = false;

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
    const result = { hour: parseInt(hh, 10), minute: parseInt(mm, 10) };
    return result;
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
 * 12시간 형식을 24시간 형식으로 변환합니다.
 * @param {string} time12h - "HH:MM AM/PM" 형식의 시간 문자열
 * @returns {string} "HH:MM" 형식의 24시간제 시간 문자열
 */
function convert12to24(time12h) {
    const [time, period] = time12h.split(' ');
    let [hours, minutes] = time.split(':');
    hours = parseInt(hours);

    if (period === 'PM' && hours !== 12) {
        hours = hours + 12;
    }
    if (period === 'AM' && hours === 12) {
        hours = 0;
    }

    return `${hours.toString().padStart(2, '0')}:${minutes}`;
}

/*****************************************
 * 시간표 HTML 생성 함수
 *****************************************/

/**
 * 시간표 테이블의 HTML을 생성합니다.
 * @param {Object} data - 시간표 데이터
 * @returns {string} 생성된 HTML 문자열
 */
function buildScheduleTable(data) {
    console.log("[로그] buildScheduleTable 호출됨, 데이터:", JSON.stringify(data));
    
    // 이전/다음 페이지 여부 계산 추가
    const hasPrevPage = data.startIndex > 0;
    const hasNextPage = data.startIndex + data.days.length < data.totalDays;
    
    const start = parseTimeString(data.startTime);
    let end = parseTimeString(data.endTime);

    // 종료 시간이 00:00인 경우 24:00으로 처리
    if (end.hour === 0 && end.minute === 0) {
        console.log("[로그] 종료 시간 00:00을 24:00으로 변경");
        end.hour = 24;
    }

    const startTotal = toTotalMinutes(start.hour, start.minute);
    const endTotal = toTotalMinutes(end.hour, end.minute);
    console.log(`[로그] 시간 범위: ${startTotal}분(${data.startTime}) ~ ${endTotal}분(${data.endTime}), 간격: ${data.interval}분`);
    
    const interval = data.interval;
    const totalBlocks = Math.floor((endTotal - startTotal) / interval);
    console.log(`[로그] 총 블록 수: ${totalBlocks}`);

    // 왼쪽 시간 바 생성 로그
    console.log(`[로그] 시간 바 생성 시작 - 시작 시간: ${start.hour}시 ${start.minute}분`);
    
    // 왼쪽 시간 바
    let timeBarHtml = `<div class="relative w-16 mr-2">`;
    
    // 시간 간격 계산 (시작 시간부터 종료 시간까지)
    for (let i = 0; i <= Math.ceil((endTotal - startTotal) / 60); i++) {
        const currentMinutes = startTotal + (i * 60);
        if (currentMinutes > endTotal) break; // 종료 시간을 넘어가면 중단
        
        // 현재 시간을 시:분 형식으로 변환
        const currentHour = Math.floor(currentMinutes / 60) % 24;
        const topPos = (i * (60 / interval) * 32); // 시간당 높이 계산
        
        console.log(`[로그] 시간 라벨 생성: ${currentHour}시, 위치: ${topPos}px, 인덱스: ${i}`);
        
        timeBarHtml += `
            <div class="time-label absolute left-0 text-sm font-semibold"
                 style="top: ${topPos}px; transform: translateY(-50%);"
                 data-hour="${currentHour}"
                 data-log="시간: ${currentHour}, 시작시간: ${start.hour}, 인덱스: ${i}">
                ${currentHour}시
            </div>
        `;
    }
    timeBarHtml += `</div>`;
    console.log("[로그] 시간 바 HTML 생성 완료");

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
 * 드래그 선택 및 오버레이 관련 함수
 *****************************************/

/**
 * 드래그 이벤트를 초기화하여 셀 선택 기능을 구현합니다.
 * @param {number} numCols - 컬럼(날짜) 수
 */
function initDragEvents(numCols) {
    window.gridNumCols = numCols;
    let isDragging = false;
    let startCell = null;
    let lastTouchedCell = null;
    let dragAction = null; // 드래그 모드 ("select" 또는 "deselect")
    let autoScrollInterval = null;
    const SCROLL_SPEED = 10;
    const SCROLL_ZONE = 50;

    let dragTooltip = null;

    function createDragTooltip(clientX, clientY) {
        dragTooltip = document.createElement("div");
        dragTooltip.style.position = "fixed";
        dragTooltip.style.background = "rgba(0, 0, 0, 0.7)";
        dragTooltip.style.color = "#fff";
        dragTooltip.style.padding = "4px 8px";
        dragTooltip.style.borderRadius = "4px";
        dragTooltip.style.pointerEvents = "none";
        dragTooltip.style.zIndex = "1000";
        dragTooltip.style.fontSize = "12px";
        dragTooltip.innerText = "";
        document.body.appendChild(dragTooltip);
        const tooltipWidth = dragTooltip.offsetWidth;
        const tooltipHeight = dragTooltip.offsetHeight;
        dragTooltip.style.left = (clientX - tooltipWidth - 10) + "px";
        dragTooltip.style.top = (clientY - tooltipHeight - 20) + "px";
    }

    function updateDragTooltip(start, current, clientX, clientY) {
        if (!dragTooltip) return;
        const startPos = getCellPosition(start);
        const currentPos = getCellPosition(current);
        const rowStart = Math.min(startPos.row, currentPos.row);
        const rowEnd = Math.max(startPos.row, currentPos.row);
        const startTimeObj = parseTimeString(jsonData.startTime);
        const startTotalMin = toTotalMinutes(startTimeObj.hour, startTimeObj.minute);
        const dragStartTime = formatTime24(startTotalMin + rowStart * jsonData.interval);
        const dragEndTime = formatTime24(startTotalMin + (rowEnd + 1) * jsonData.interval);
        dragTooltip.innerText = `${dragStartTime} ~ ${dragEndTime}`;
        const tooltipWidth = dragTooltip.offsetWidth;
        const tooltipHeight = dragTooltip.offsetHeight;
        dragTooltip.style.left = (clientX - tooltipWidth - 10) + "px";
        dragTooltip.style.top = (clientY - tooltipHeight - 20) + "px";
    }

    function removeDragTooltip() {
        if (dragTooltip) {
            dragTooltip.parentNode.removeChild(dragTooltip);
            dragTooltip = null;
        }
    }

    function handleAutoScroll(clientY) {
        const windowHeight = window.innerHeight;
        if (autoScrollInterval) {
            clearInterval(autoScrollInterval);
            autoScrollInterval = null;
        }
        if (clientY < SCROLL_ZONE) {
            autoScrollInterval = setInterval(() => {
                window.scrollBy(0, -SCROLL_SPEED);
            }, 16);
        } else if (clientY > windowHeight - SCROLL_ZONE) {
            autoScrollInterval = setInterval(() => {
                window.scrollBy(0, SCROLL_SPEED);
            }, 16);
        }
    }

    const cells = Array.from(document.querySelectorAll(".grid-cell"));

    function getCellPosition(cell) {
        const index = cells.indexOf(cell);
        const row = Math.floor(index / numCols);
        const col = index % numCols;
        return { row, col };
    }

    /**
     * 드래그 시작 셀의 상태에 따라 선택/해제 모드에 맞게 영역 내의 셀들을 임시 하이라이트 합니다.
     */
    function highlightRectangle(start, end) {
        // 모든 셀에서 기존의 임시 하이라이트 오버레이 제거
        cells.forEach(cell => {
            const existingOverlay = cell.querySelector('.drag-highlight-overlay');
            if (existingOverlay) {
                existingOverlay.remove();
            }
            // 미리보기 모드, 일반 모드 관계없이 임시 클래스 제거
            cell.classList.remove("bg-blue-200", "bg-red-200");
        });

        const startPos = getCellPosition(start);
        const endPos   = getCellPosition(end);
        const rowStart = Math.min(startPos.row, endPos.row);
        const rowEnd   = Math.max(startPos.row, endPos.row);
        const colStart = Math.min(startPos.col, endPos.col);
        const colEnd   = Math.max(startPos.col, endPos.col);

        for (let r = rowStart; r <= rowEnd; r++) {
            for (let c = colStart; c <= colEnd; c++) {
                const idx = r * numCols + c;
                const cell = cells[idx];
                const cellKey = cell.getAttribute('data-cell-key');

                if (dragAction === "select") {
                    // 이미 선택된 셀이면 진한 파란색 오버레이를 적용
                    if (window.selectedCells && window.selectedCells[cellKey]) {
                        let overlay = document.createElement("div");
                        overlay.className = "drag-highlight-overlay";
                        overlay.style.position = "absolute";
                        overlay.style.top = "0";
                        overlay.style.left = "0";
                        overlay.style.width = "100%";
                        overlay.style.height = "100%";
                        overlay.style.background = "rgba(0, 0, 139, 0.7)"; // 진한 파란색
                        overlay.style.pointerEvents = "none";
                        overlay.style.zIndex = "9999";
                        cell.appendChild(overlay);
                    } else {
                        cell.classList.add("bg-blue-200");
                    }
                } else if (dragAction === "deselect") {
                    if (window.isPreviewMode) {
                        if (window.selectedCells && window.selectedCells[cellKey]) {
                            let overlay = document.createElement("div");
                            overlay.className = "drag-highlight-overlay";
                            overlay.style.position = "absolute";
                            overlay.style.top = "0";
                            overlay.style.left = "0";
                            overlay.style.width = "100%";
                            overlay.style.height = "100%";
                            overlay.style.background = "rgba(0, 0, 255, 0.5)";
                            overlay.style.pointerEvents = "none";
                            overlay.style.zIndex = "9999";
                            cell.appendChild(overlay);
                        }
                    } else {
                        if (cell.classList.contains("bg-green-300")) {
                            cell.classList.add("bg-red-200");
                        }
                    }
                }
            }
        }
    }

    /**
     * 드래그 시작 셀의 상태에 따라 선택/해제 모드에 맞게 영역 내의 셀들을 일괄 처리합니다.
     * @param {Element} start - 드래그 시작 셀
     * @param {Element} end - 드래그 끝 셀
     * @param {string} mode - "select" 또는 "deselect"
     */
    function toggleRectangle(start, end, mode) {
        const startPos = getCellPosition(start);
        const endPos   = getCellPosition(end);
        const rowStart = Math.min(startPos.row, endPos.row);
        const rowEnd   = Math.max(startPos.row, endPos.row);
        const colStart = Math.min(startPos.col, endPos.col);
        const colEnd   = Math.max(startPos.col, endPos.col);

        for (let r = rowStart; r <= rowEnd; r++) {
            for (let c = colStart; c <= colEnd; c++) {
                const idx = r * numCols + c;
                const cell = cells[idx];
                const key = cell.getAttribute('data-cell-key');
                if (mode === "deselect") {
                    if (cell.classList.contains("bg-green-300")) {
                        cell.classList.remove("bg-green-300");
                        delete window.selectedCells[key];
                    }
                } else if (mode === "select") {
                    if (!cell.classList.contains("bg-green-300")) {
                        cell.classList.add("bg-green-300");
                        window.selectedCells[key] = true;
                    }
                }
            }
        }
        updateSelectedInfo();
        generateOverlaysGlobal();
    }

    function generateOverlaysGlobal() {
        const container = document.querySelector('.flex-1.relative');
        if (!container) return;
        container.querySelectorAll('.selection-overlay').forEach(overlay => overlay.remove());
        const totalRows = cells.length / numCols;
        let candidates = [];
        for (let col = 0; col < numCols; col++) {
            let row = 0;
            while (row < totalRows) {
                const idx = row * numCols + col;
                const cell = cells[idx];
                if (cell.classList.contains("bg-green-300")) {
                    const startRow = row;
                    while (row < totalRows && cells[row * numCols + col].classList.contains("bg-green-300")) {
                        row++;
                    }
                    const endRow = row - 1;
                    const firstCell = cells[startRow * numCols + col];
                    const lastCell = cells[endRow * numCols + col];
                    const top = firstCell.offsetTop;
                    const bottom = lastCell.offsetTop + lastCell.offsetHeight;
                    const left = firstCell.offsetLeft;
                    const right = firstCell.offsetLeft + firstCell.offsetWidth;
                    candidates.push({
                        col: col,
                        startRow: startRow,
                        endRow: endRow,
                        top: top,
                        bottom: bottom,
                        left: left,
                        right: right
                    });
                } else {
                    row++;
                }
            }
        }
        candidates.sort((a, b) => a.col - b.col);
        let mergedGroups = [];
        let used = new Array(candidates.length).fill(false);
        for (let i = 0; i < candidates.length; i++) {
            if (used[i]) continue;
            let group = [candidates[i]];
            used[i] = true;
            for (let j = i + 1; j < candidates.length; j++) {
                if (used[j]) continue;
                if (
                    candidates[j].startRow === candidates[i].startRow &&
                    candidates[j].endRow === candidates[i].endRow &&
                    candidates[j].col === group[group.length - 1].col + 1
                ) {
                    group.push(candidates[j]);
                    used[j] = true;
                }
            }
            mergedGroups.push(group);
        }
        mergedGroups.forEach(group => {
            const first = group[0];
            const last = group[group.length - 1];
            const verticalCount = first.endRow - first.startRow + 1;
            if (group.length === 1) {
                if (verticalCount < 4) return;
            } else {
                if (verticalCount < 2) return;
            }
            const overlayLeft = first.left + (last.right - first.left) / 2;
            const overlayTop = first.top + (first.bottom - first.top) / 2;
            const startTimeObj = parseTimeString(jsonData.startTime);
            const startTotalMin = toTotalMinutes(startTimeObj.hour, startTimeObj.minute);
            const groupStartTime = formatTime24(startTotalMin + first.startRow * jsonData.interval);
            const groupEndTime = formatTime24(startTotalMin + (first.endRow + 1) * jsonData.interval);
            const overlayText = `${groupStartTime} ~ ${groupEndTime}`;
            const overlay = document.createElement('div');
            overlay.className = 'selection-overlay';
            overlay.style.position = 'absolute';
            overlay.style.top = overlayTop + 'px';
            overlay.style.left = overlayLeft + 'px';
            overlay.style.transform = 'translate(-50%, -50%)';
            overlay.style.padding = '2px 4px';
            overlay.style.background = '#fff';
            overlay.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.1)';
            overlay.style.border = '1px solid #ccc';
            overlay.style.borderRadius = '4px';
            overlay.style.fontSize = '12px';
            overlay.style.zIndex = '10';
            overlay.style.textAlign = 'center';
            overlay.style.maxWidth = (last.right - first.left) + 'px';
            overlay.innerText = overlayText;
            container.appendChild(overlay);
        });
    }

    function findGridCell(element) {
        while (element && !element.classList.contains('grid-cell')) {
            element = element.parentElement;
        }
        return element;
    }

    // 마우스 및 터치 이벤트 등록
    cells.forEach(cell => {
        cell.addEventListener("mousedown", (e) => {
            // 드래그 시작 시 페이지 버튼 fade out
            const prevButton = document.getElementById("prevPage");
            const nextButton = document.getElementById("nextPage");
            if (prevButton) prevButton.style.opacity = "0";
            if (nextButton) nextButton.style.opacity = "0";
            isDragging = true;
            startCell = cell;
            // 드래그 시작 시 현재 셀이 이미 선택되어 있으면 "deselect", 아니면 "select" 모드로 설정
            dragAction = cell.classList.contains("bg-green-300") ? "deselect" : "select";
            createDragTooltip(e.clientX, e.clientY);
            updateDragTooltip(startCell, startCell, e.clientX, e.clientY);
            highlightRectangle(startCell, startCell);
        });

        cell.addEventListener("mouseenter", (e) => {
            if (isDragging && startCell) {
                highlightRectangle(startCell, cell);
                updateDragTooltip(startCell, cell, e.clientX, e.clientY);
            }
        });

        cell.addEventListener("mousemove", (e) => {
            if (isDragging) {
                handleAutoScroll(e.clientY);
                updateDragTooltip(startCell, cell, e.clientX, e.clientY);
            }
        });

        cell.addEventListener("touchstart", (e) => {
            e.preventDefault(); // 스크롤 방지
            const prevButton = document.getElementById("prevPage");
            const nextButton = document.getElementById("nextPage");
            if (prevButton) prevButton.style.opacity = "0";
            if (nextButton) nextButton.style.opacity = "0";
            isDragging = true;
            startCell = cell;
            lastTouchedCell = cell;
            // 터치 시작 시 현재 셀이 이미 선택되어 있으면 "deselect", 아니면 "select" 모드로 설정
            dragAction = cell.classList.contains("bg-green-300") ? "deselect" : "select";
            const touch = e.touches[0];
            createDragTooltip(touch.clientX, touch.clientY);
            updateDragTooltip(startCell, startCell, touch.clientX, touch.clientY);
            highlightRectangle(startCell, startCell);
        });

        cell.addEventListener("touchmove", (e) => {
            e.preventDefault();
            if (isDragging && startCell) {
                const touch = e.touches[0];
                handleAutoScroll(touch.clientY);
                const touchedElement = document.elementFromPoint(touch.clientX, touch.clientY);
                const gridCell = findGridCell(touchedElement);
                if (gridCell && gridCell !== lastTouchedCell) {
                    lastTouchedCell = gridCell;
                    highlightRectangle(startCell, gridCell);
                    updateDragTooltip(startCell, gridCell, touch.clientX, touch.clientY);
                }
            }
        });
    });

    // 드래그 종료 시: 페이지 버튼 fade in
    document.addEventListener("mouseup", (e) => {
        if (autoScrollInterval) {
            clearInterval(autoScrollInterval);
            autoScrollInterval = null;
        }
        removeDragTooltip();

        const prevButton = document.getElementById("prevPage");
        const nextButton = document.getElementById("nextPage");
        if (prevButton) prevButton.style.opacity = "1";
        if (nextButton) nextButton.style.opacity = "1";

        if (isDragging && startCell) {
            if (e.target.classList.contains("grid-cell")) {
                toggleRectangle(startCell, e.target, dragAction);
            }
            // 기존 임시 하이라이트 클래스 제거
            cells.forEach(cell => cell.classList.remove("bg-blue-200", "bg-red-200"));
            // 모든 드래그 하이라이트 오버레이 제거
            cells.forEach(cell => {
                const overlay = cell.querySelector('.drag-highlight-overlay');
                if (overlay) {
                    overlay.remove();
                }
            });
        }
        isDragging = false;
        startCell = null;
        dragAction = null; // 모드 초기화
    });

    document.addEventListener("touchend", (e) => {
        if (autoScrollInterval) {
            clearInterval(autoScrollInterval);
            autoScrollInterval = null;
        }
        removeDragTooltip();

        const prevButton = document.getElementById("prevPage");
        const nextButton = document.getElementById("nextPage");
        if (prevButton) prevButton.style.opacity = "1";
        if (nextButton) nextButton.style.opacity = "1";

        if (isDragging && startCell && lastTouchedCell) {
            toggleRectangle(startCell, lastTouchedCell, dragAction);
            cells.forEach(cell => cell.classList.remove("bg-blue-200", "bg-red-200"));
            // 모든 드래그 하이라이트 오버레이 제거
            cells.forEach(cell => {
                const overlay = cell.querySelector('.drag-highlight-overlay');
                if (overlay) {
                    overlay.remove();
                }
            });
        }
        isDragging = false;
        startCell = null;
        lastTouchedCell = null;
        dragAction = null;  // 모드 초기화
    });

    window.generateOverlaysGlobal = generateOverlaysGlobal;
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
            // 여기서 adjustTimeBarPositions 함수 사용 안함
        });
        observer.observe(container);
    } else {
        // ResizeObserver 미지원 시도 함수 호출 안함
        window.addEventListener("resize", debounce(() => {
            // 여기서 adjustTimeBarPositions 함수 사용 안함
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

// jsonData가 준비되었는지 확인하는 함수
function isJsonDataReady() {
  return window.jsonData && window.jsonData.days && window.jsonData.days.length > 0;
}

/**
 * 시간표 및 관련 UI를 업데이트합니다.
 */
function updateSchedule() {
  // jsonData가 없으면 단순히 오류 로그만 남기고 종료
  if (!isJsonDataReady()) {
    console.error('[로그] jsonData가 없습니다. 업데이트를 중단합니다.');
    return;
  }
  
  // 현재 페이지 및 아이템 수 설정
  window.currentPage = window.currentPage || 0;
  window.itemsPerPage = window.innerWidth <= 629 ? 3 : 5;
  
  const startIndex = window.currentPage * window.itemsPerPage;
  const paginatedDays = window.jsonData.days.slice(startIndex, startIndex + window.itemsPerPage);
  
  // 기존 페이지 버튼 제거
  const prevPageBtn = document.getElementById("prevPage");
  if (prevPageBtn) prevPageBtn.remove();
  const nextPageBtn = document.getElementById("nextPage");
  if (nextPageBtn) nextPageBtn.remove();
  
  // 요일 및 월 이름 변환
  const dayKorean = {
    'Sun': '일', 'Mon': '월', 'Tue': '화', 'Wed': '수', 
    'Thu': '목', 'Fri': '금', 'Sat': '토'
  };
  
  const monthKorean = {
    'Jan': '1월', 'Feb': '2월', 'Mar': '3월', 'Apr': '4월', 'May': '5월', 'Jun': '6월',
    'Jul': '7월', 'Aug': '8월', 'Sep': '9월', 'Oct': '10월', 'Nov': '11월', 'Dec': '12월'
  };
  
  // 날짜 헤더 HTML 생성
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
  
  // 시간표 HTML 생성
  const tableHTML = buildScheduleTable({
    ...window.jsonData,
    days: paginatedDays,
    startIndex,
    totalDays: window.jsonData.days.length
  });
  
  // HTML 업데이트
  const app = document.getElementById("app");
  if (!app) return;
  app.innerHTML = dayHeaderHtml + tableHTML;
  
  // 페이지 버튼 추가 (단일 setTimeout으로)
  setTimeout(() => {
    const gridContainer = document.querySelector('.flex-1.relative');
    if (!gridContainer) return;
    
    // 이전 페이지 버튼
    if (window.currentPage > 0) {
      const btnPrev = document.createElement('button');
      btnPrev.id = "prevPage";
      btnPrev.className = "absolute left-[-20px] top-1/2 transform -translate-y-1/2 page-button";
      btnPrev.innerText = "←";
      gridContainer.appendChild(btnPrev);
      
      btnPrev.addEventListener("click", function() {
        if (window.currentPage > 0) {
          window.currentPage--;
          updateSchedule();
        }
      });
    }
    
    // 다음 페이지 버튼
    if (startIndex + window.itemsPerPage < window.jsonData.days.length) {
      const btnNext = document.createElement('button');
      btnNext.id = "nextPage";
      btnNext.className = "absolute right-[-20px] top-1/2 transform -translate-y-1/2 page-button";
      btnNext.innerText = "→";
      gridContainer.appendChild(btnNext);
      
      btnNext.addEventListener("click", function() {
        if ((window.currentPage + 1) * window.itemsPerPage < window.jsonData.days.length) {
          window.currentPage++;
          updateSchedule();
        }
      });
    }
    
    // 드래그 이벤트 연결
    initDragEvents(paginatedDays.length);
    
    // 오버레이 업데이트
    if (window.generateOverlaysGlobal) {
      window.generateOverlaysGlobal();
    }
  }, 10);
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

/**
 * 이전 슬라이드에서 선택된 시간 범위를 가져옵니다.
 * @returns {{startTime: string, endTime: string} | null}
 */
function getPreviousSelectedTimeRange() {
    const selectedCells = window.selectedCells || {};
    if (Object.keys(selectedCells).length === 0) return null;

    let minBlock = Infinity;
    let maxBlock = -Infinity;

    // 선택된 모든 셀을 순회하며 최소/최대 블록 인덱스 찾기
    for (const key in selectedCells) {
        if (!selectedCells[key]) continue;
        const parts = key.split("-");
        if (parts.length < 3) continue;
        const blockIndex = parseInt(parts[2], 10);
        minBlock = Math.min(minBlock, blockIndex);
        maxBlock = Math.max(maxBlock, blockIndex);
    }

    if (minBlock === Infinity || maxBlock === -Infinity) return null;

    // 기존 시작 시간을 기준으로 새로운 시작/종료 시간 계산
    const startTimeObj = parseTimeString(window.jsonData.startTime);
    const startTotalMin = toTotalMinutes(startTimeObj.hour, startTimeObj.minute);
    
    const newStartTime = formatTime24(startTotalMin + minBlock * window.jsonData.interval);
    const newEndTime = formatTime24(startTotalMin + (maxBlock + 1) * window.jsonData.interval);

    return {
        startTime: newStartTime,
        endTime: newEndTime
    };
}

/**
 * 시간표 초기화 및 업데이트
 */
function initializeSchedule() {
    // 이미 초기화 되었으면 중복 실행 방지
    if (isInitialized) return;
    
    // 드롭다운에서 선택된 시작 시간 가져오기
    const startTimeSelect = document.getElementById("startTime");
    if (startTimeSelect) {
        window.jsonData.startTime = startTimeSelect.value;
    }
    
    updateSchedule();
}

// 문서 로드 완료 이벤트에서 중복 초기화 방지
document.addEventListener('DOMContentLoaded', function() {
    // HTML 파일에서 이미 updateSchedule을 호출했다면 여기서는 스킵
    if (window.jsonData && isInitialized) return;
    
    const startTimeSelect = document.getElementById("startTime");
    if (startTimeSelect) {
        startTimeSelect.addEventListener('change', function() {
            window.jsonData.startTime = this.value;
            updateSchedule();
        });
    }
    
    // 첫 번째 슬라이드가 활성 상태일 때는 초기화하지 않음
    const slide1 = document.getElementById("slide1");
    if (slide1 && slide1.classList.contains("active")) {
        return;
    }
    
    // 필요한 경우에만 초기화
    if (window.jsonData && !isInitialized) {
        initializeSchedule();
    }
});

// 전역 노출 함수
window.buildScheduleTable = buildScheduleTable;
window.initDragEvents = initDragEvents;
window.updateSchedule = updateSchedule;
window.convert12to24 = convert12to24;

// 추가: 초기화 시 시간 정보가 올바르게 설정되었는지 확인하는 함수
function debugTimeInfo() {
    console.log("현재 시간 설정:", window.jsonData);
    const labels = document.querySelectorAll(".time-label");
    console.log("시간 라벨 개수:", labels.length);
    labels.forEach((label, i) => {
        console.log(`라벨 ${i}: ${label.textContent} (top: ${label.style.top})`);
    });
}

// 전역 함수로 등록하여 디버깅 가능하게
window.debugTimeInfo = debugTimeInfo;



