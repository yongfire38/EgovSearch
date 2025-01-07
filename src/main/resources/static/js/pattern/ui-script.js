/* ** 도움말 ** */
const helperArea = document.querySelectorAll('.helper-area');
const helperBox = {
	init: () => {
		if (helperArea.length > 0) {  //해당 클래스 존재할떄만 실행
			setTimeout(() => {
				helperBox.paddingSet();
			},50);
			setTimeout(() => {
				helperBox.heightSet();
			},100);
		}
	},
	paddingSet: () => { //영역 세팅
		const bnH = document.querySelector('#header-top').offsetHeight;
		const headerH = document.querySelector('#header').offsetHeight;

		const defaultPadding = bnH + headerH;
		const bnHiddgnPadding = headerH;

		const $wrap = document.querySelector('#wrap');
		const $expandBtn = document.querySelector('.btn-helper.expand');
		const $expandBox = document.querySelector('.helper-wrap');

		const $collapseBtn = document.querySelector('.btn-helper.fold');
		if (document.body.classList.contains('bn-hidden')) { //top banner 안보임
			if ($wrap.classList.contains('scroll-down')) { //header영역 안보임
				$expandBtn.style.marginTop = '0';
				if (winSize == 'pc') {
					$expandBox.style.paddingTop = '0';
					$collapseBtn.style.marginTop = '0';
				} else {
					$expandBox.removeAttribute('style');
					$collapseBtn.removeAttribute('style');
				}
			} else { //header영역 보임
				$expandBtn.style.marginTop = bnHiddgnPadding + 'px';
				if (winSize == 'pc') {
					$expandBox.style.paddingTop = bnHiddgnPadding + 'px';
					$collapseBtn.style.marginTop = bnHiddgnPadding + 'px';
				} else {
					$expandBox.style.paddingTop = '0';
					$collapseBtn.removeAttribute('style');
				}
			}
		} else { //top banner 보임
			$expandBtn.style.marginTop = defaultPadding + 'px';
			if (winSize == 'pc') {
				$expandBox.style.paddingTop = defaultPadding + 'px';
				$collapseBtn.style.marginTop = defaultPadding + 'px';
			} else {
				$expandBox.removeAttribute('style');
				$collapseBtn.removeAttribute('style');
			}
		}

	},
	trigger: () => { //도움말열기 버튼에 추가한 class 삭제
		const btnExec = document.querySelectorAll('.btn-help-exec');
		if (helperArea.length > 0) {
			btnExec.forEach(e => {
				e.classList.remove('btn-help-clicked');
			});
		}
	},
	expand: () => { //도움말버튼 클릭 시 실행
		const btnExec = document.querySelectorAll('.btn-help-exec');
		const target = document.querySelector('.helper-wrap');
		if (helperArea.length > 0) {
			btnExec.forEach(e => {
				e.addEventListener('click', () => {
					helperBox.open();
					helperBox.trigger();
					e.classList.add('btn-help-clicked');
					setTimeout(() => {
						target.focus();
					}, 50);
				});
			});
		}

	},
	collapse: () => { //도움말 접어두기 버튼 클릭 시 실행
		const btn = document.querySelector('.btn-helper.fold');

		if (helperArea.length > 0) {
			btn.addEventListener('click', () => {
				if (winSize == 'mob') {
					document.body.classList.remove('scroll-no');
				}
				helperBox.close();
			});
		}
	},
	open: () => { //도움말 열기
		if (helperArea.length > 0) {  //해당 클래스 존재할떄만 실행
			const target = document.querySelector('.helper-wrap');
			const inner = document.querySelector('#container > .inner');
			const $header = document.querySelector("#header .head-body > .inner");
			const $container = document.querySelector("#container");
			const _width = document.body.clientWidth;
			if (winSize == 'mob') {
				document.body.classList.add('scroll-no');
			}
			target.setAttribute('aria-expanded', 'true');
			target.setAttribute('tabindex', '0');
			document.querySelector('.helper-area').classList.add('expand');
			
			if (inner.classList.contains('flexible')) { // 화면 사이즈 줄어들면 영역도 줄어들게
				inner.classList.remove('folded');
				$container.classList.remove('help-close');
				$container.classList.add("help-open");
				const _headerL = $header.offsetLeft;
				if( _width > 1024 && _width < 1900  ){
					$container.classList.remove("help-open");
					$container.classList.add("help-open");
					$container.style.paddingRight="";
					$container.style.paddingLeft=`${_headerL+26}px`;
				}

				helperBox.resize($header, $container);
			}
		}
	},
	close: () => { //도움말 접기
		const $header = document.querySelector("#header .head-body > .inner");
		const $container = document.querySelector('#container');
		const target = document.querySelector('.helper-wrap');
		const inner = document.querySelector('#container > .inner');
		const trigger = document.querySelectorAll('.btn-help-clicked');
		const _width = document.body.clientWidth;
		target.setAttribute('aria-expanded', 'false');
		target.removeAttribute('tabindex');
		document.querySelector('.helper-area').classList.remove('expand');

		if (trigger.length > 0) { //버튼 클릭으로 도움말 펼친경우 클릭한 버튼으로 포커스
			trigger[0].focus();
		}

		if (inner.classList.contains('flexible')) { // 도움말 닫히면 컨텐츠 영역 늘리기
			inner.classList.add('folded');
			$container.classList.add("help-close");
			$container.classList.remove("help-open");
			$container.style.paddingLeft=``;
			if ($container.classList.contains('help-close')) {
				$container.style.paddingLight=``;
				$container.style.paddingRight=``;
			} else if (_width > 1900 || _width <= 1024 ) {
				$container.classList.remove("help-open");
				$container.classList.remove("help-close");
			}
			helperBox.resize($header, $container);
		}
	},
	resize: ($header, $container) => {
		window.addEventListener("resize", () => {
			const _headerL = $header.offsetLeft;
			const _width = document.body.clientWidth;
			if ( _width > 1024 && _width < 1900) {
				$container.style.paddingRight="";
				if($container.classList.contains("help-open")) {
					$container.style.paddingLeft=`${_headerL+26}px`;	
				}else {
					$container.style.paddingLeft=``;	
				}
			} else if ( _width <= 1024 ) {
				$container.style.paddingLeft="";
				$container.style.paddingRight="";
			} else {
				$container.style.paddingLeft= "";
			}
		});
	},
	heightSet: () => {
		const $helperArea = document.querySelector('.helper-area');
		const $expandBox = document.querySelector('.helper-wrap');
		const $contsArea = document.querySelector('.helper-conts-area');
		const helperTitH = document.querySelector('.helper-tit').offsetHeight;

		const contsPt = parseInt(getComputedStyle($expandBox).paddingTop);
		const contsAreaH = window.innerHeight - helperTitH - contsPt;

		$contsArea.style.height = contsAreaH + 'px';

		if (winSize == 'mob') {
			if ($helperArea.classList.contains('expand')) {
				document.body.classList.add('scroll-no');
			}
		} else {
			document.body.classList.remove('scroll-no');
		}

	}
}

/* ** 영역 높이 확장 축소 ** */
function collapseBox() {
	const box = document.querySelectorAll('.conts-expand-area');
	box.forEach(e => {
		const btn = e.querySelector('.btn-conts-expand');
		btn.addEventListener('click', () => {
			e.classList.toggle('active');
		});
	});
}

/* ** 박스형 체크박스 상태에 따른 디자인 변경 ** */
function chkBox() {
	const box = document.querySelectorAll('.chk-group-wrap');
	box.forEach(e => {
		const boxList = e.querySelectorAll('li');
		boxList.forEach(ele => {
			ele.removeAttribute('class');
			const thisList = ele.closest('li');
			const checkbox = ele.querySelector('input[type=checkbox]');
			const is_disabled = checkbox.disabled;
			let is_checked = checkbox.checked;

			if (is_disabled == true) {
				thisList.classList.add('disabled');
			}
			else {
				if (is_checked == true) {
					thisList.classList.add('checked');
				}
			}

			checkbox.addEventListener('click', () => {
				if (is_checked == true) {
					thisList.classList.remove('checked');
					is_checked = false;
				} else {
					thisList.classList.add('checked');
					is_checked = true;
				}
			});
		});
	});
}

/* ** 스크롤 값 체크 ** */
let scrollY = window.scrollY;
let scrollH = document.body.scrollHeight;
function scrollVal() {
	scrollY = window.scrollY;
	scrollH = document.body.scrollHeight;
}

/* ** 스크롤 네비게이션 ** */
const winHeight = window.innerHeight;
const quickIndicators = document.querySelectorAll('.quick-list');

const quickNav = {
	init: () => {
		if (quickIndicators.length > 0) { //해당 클래스 존재할떄만 실행
			quickNav.linkNav();
		}
	},
	reset: () => { //초기화
		quickIndicators.forEach(e => {
			const locationList = e.querySelectorAll('a');
			locationList.forEach(ele => {
				ele.classList.remove('active');
			});
		});
	},
	linkNav: () => { //퀵 네비게이션 클릭 시 스크롤 이동
		quickIndicators.forEach(e => {
			const locationList = e.querySelectorAll('a');
			locationList.forEach(ele => {
				const target = document.querySelector(ele.getAttribute('href'));
				const offsetY = target.getBoundingClientRect().top + scrollY;

				ele.addEventListener('click', (ev) => {
					ev.preventDefault();

					window.scrollTo({
						left: 0,
						top: offsetY,
						behavior: 'smooth',
					});
				});
			});
		});
	},
	navHighlight: () => { //페이지 스크롤 시 퀵 네비게이션 해당메뉴 active
		if (quickIndicators.length > 0) {
			const sectionArea = document.querySelectorAll('.section-link');
			const topHeight = Math.ceil(winHeight / 5);
			const firstSecTop = document.querySelectorAll('.scroll-check .section-link')[0].offsetTop;
			sectionArea.forEach(current => {
				const sectionHeight = current.offsetHeight;
				const sectionTop = current.offsetTop - topHeight;
				const sectionId = current.getAttribute("id");
				if (scrollY <= firstSecTop) { //스크롤이 첫번째 섹션보다 위에 있으면 맨 위 네비 active
					document.querySelector(".conts-area > .quick-nav-area .quick-list li:first-of-type a").classList.add("active");
				}
				else if (scrollY + winHeight >= scrollH) { //스크롤이 맨 하단에 있으면 맨 아래 네비 active
					quickNav.reset();
					document.querySelector(".conts-area > .quick-nav-area .quick-list li:last-of-type a").classList.add("active");
				}
				else {
					if (scrollY > sectionTop && scrollY <= sectionTop + sectionHeight) { //스크롤이 해당 섹션
						document.querySelector(".conts-area > .quick-nav-area a[href*=" + sectionId + "]").classList.add("active");
					} else {
						document.querySelector(".conts-area > .quick-nav-area a[href*=" + sectionId + "]").classList.remove("active");
					}
				}
			});
		}
	},
}

/* ** 스킵네비게이션 클릭 시 scroll 맨 위로 ** */
function goTop() {
	const $skip = document.querySelector('#skip-nav');
	$skip.addEventListener('click', () => {
		setTimeout(() => {
			window.scrollTo({
				left: 0,
				top: 0,
				behavior: 'smooth',
			});
		}, 300);
	});
}

window.addEventListener("DOMContentLoaded", () => {
	/* ** 영역 높이 확장 축소 ** */
	collapseBox();

	/* ** 박스형 체크박스 상태에 따른 디자인 변경 ** */
	chkBox();

	/* ** 스크롤 네비게이션 ** */
	quickNav.init();

	/* ** 스킵네비게이션 클릭 시 scroll 맨 위로 ** */
	if(document.querySelector('#skip-nav') !== null) goTop();

	setTimeout(() => { //gnb footer 등 include영역으로 로딩시간이 필요한경우 settimeout에 넣어줌 (배포시 삭제필요)
		/* ** 도움말 ** */
		helperBox.init();
		if (winSize == 'pc') {
			helperBox.open();
		}

		//클릭이벤트는 로드시에만 실행시키기
		helperBox.expand();
		helperBox.collapse();
	}, 200);
});
window.addEventListener('scroll', () => {
	/* ** 스크롤 값 체크 ** */
	scrollVal();

	/* ** 스크롤 네비게이션 ** */
	quickNav.navHighlight();

	setTimeout(() => { //gnb footer 등 include영역으로 로딩시간이 필요한경우 settimeout에 넣어줌 (배포시 삭제필요)
		/* ** 도움말 ** */
		helperBox.init();
	}, 200);
});
window.addEventListener('resize', () => {
	/* ** 윈도우 사이즈 체크 (반응형) ** */
	winSizeSet();

	/* ** 도움말 ** */
	helperBox.init();
});