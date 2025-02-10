// 글로벌 변수: 선택된 셀을 저장하는 객체
window.selectedCells = window.selectedCells || {};

// 더미 데이터: 다른 사용자의 일정 (예: 민구의 일정)
// jsonData에 포함된 날짜 형식("Feb 9", "Feb 13" 등)과 동일하게 사용합니다.
const otherUserSchedule = [
    { user: "민구", date: "Feb 9", start: "11:00", end: "16:00" },
    { user: "민구", date: "Feb 13", start: "14:00", end: "16:00" },
    { user: "민구", date: "Feb 15", start: "14:00", end: "16:00" },
    { user: "민구", date: "Feb 22", start: "14:00", end: "16:00" },
    { user: "민구", date: "Feb 23", start: "14:00", end: "16:00" }
];

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
            const isSelected = window.selectedCells && window.selectedCells[cellKey] ? "bg-green-300" : "";
            let borderClasses = `grid-cell bg-gray-100 ${isHourMark ? 'hour-mark' : ''} ${isSelected}`;
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
                <div data-cell-key="${cellKey}" class="${borderClasses}" style="${borderStyle}">
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
        const scrollTop = window.scrollY;
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

    function highlightRectangle(start, end) {
        cells.forEach(cell => cell.classList.remove("bg-blue-200", "bg-red-200"));
        const startPos = getCellPosition(start);
        const endPos   = getCellPosition(end);
        const rowStart = Math.min(startPos.row, endPos.row);
        const rowEnd   = Math.max(startPos.row, endPos.row);
        const colStart = Math.min(startPos.col, endPos.col);
        const colEnd   = Math.max(startPos.col, endPos.col);

        for (let r = rowStart; r <= rowEnd; r++) {
            for (let c = colStart; c <= colEnd; c++) {
                const idx = r * numCols + c;
                if (cells[idx].classList.contains("bg-green-300")) {
                    cells[idx].classList.add("bg-red-200");
                } else {
                    cells[idx].classList.add("bg-blue-200");
                }
            }
        }
    }

    function toggleRectangle(start, end) {
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
                if (cell.classList.contains("bg-green-300")) {
                    cell.classList.remove("bg-green-300");
                    delete window.selectedCells[key];
                } else {
                    cell.classList.add("bg-green-300");
                    window.selectedCells[key] = true;
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
            // 드래그 시작 시 페이지 버튼(개별 버튼) fade out
            const prevButton = document.getElementById("prevPage");
            const nextButton = document.getElementById("nextPage");
            if (prevButton) prevButton.style.opacity = "0";
            if (nextButton) nextButton.style.opacity = "0";
            isDragging = true;
            startCell = cell;
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
            // 드래그 시작 시 페이지 버튼 fade out
            const prevButton = document.getElementById("prevPage");
            const nextButton = document.getElementById("nextPage");
            if (prevButton) prevButton.style.opacity = "0";
            if (nextButton) nextButton.style.opacity = "0";
            isDragging = true;
            startCell = cell;
            lastTouchedCell = cell;
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

    // 드래그 종료 시: 페이지 버튼(개별 버튼) fade in (다시 보이도록)
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
                toggleRectangle(startCell, e.target);
            }
            cells.forEach(cell => cell.classList.remove("bg-blue-200", "bg-red-200"));
        }
        isDragging = false;
        startCell = null;
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
            toggleRectangle(startCell, lastTouchedCell);
            cells.forEach(cell => cell.classList.remove("bg-blue-200", "bg-red-200"));
        }
        isDragging = false;
        startCell = null;
        lastTouchedCell = null;
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

    // 드래그 이벤트 연결
    initDragEvents(paginatedDays.length);

    // 테이블 렌더링 후 시간바 위치 조정
    setTimeout(() => {
        adjustTimeBarPositions(jsonData.interval);
    }, 0);

    // 창 크기 변화에 따른 시간바 조정
    setupResizeObserver(jsonData.interval);

    setTimeout(() => {
        adjustTimeBarPositions(jsonData.interval);
    }, 0);
    
    // 페이지 갱신 후 선택된 셀에 해당하는 오버레이를 다시 그리도록 호출합니다.
    setTimeout(() => {
        if (window.generateOverlaysGlobal) {
            window.generateOverlaysGlobal();
        }
    }, 100);
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
window.initDragEvents = initDragEvents;

  

  