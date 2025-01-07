/* ** 윈도우 사이즈 체크 (반응형) ** */
let winSize;
function winSizeSet() {
	const brekpoint = 1024;
	if (window.innerWidth >= brekpoint) {
		winSize = 'pc';
	}
	else {
		winSize = 'mob';
	}
}

/* layer tab */
function layerTab() {
	const layerTabArea = document.querySelectorAll('.tab-area.layer');

	/* 탭 접근성 텍스트 세팅 */
	const tabAccText = document.createTextNode(' 선택됨');
	const tabAccTag = document.createElement('i');
	tabAccTag.setAttribute('class', 'sr-only created');
	tabAccTag.appendChild(tabAccText);

	layerTabArea.forEach(e => {
		const layerTabEle = e.querySelectorAll('.tab > ul > li');
		const tabPanel = e.querySelectorAll('.tab-conts');

		function tab() {
			layerTabEle.forEach(ele => {
				const control = ele.getAttribute('aria-controls');
				const selectedTabPanel = document.getElementById(control);

				if (ele.classList.contains('active')) {
					//선택됨 텍스트 추가
					ele.querySelector('button').append(tabAccTag);
				}

				ele.addEventListener('click', () => {
					layerTabInitial(); //레이어탭 초기화

					ele.classList.add('active');
					ele.querySelector('button').append(tabAccTag); //선택됨 텍스트 추가
					ele.setAttribute('aria-selected', 'true');
					selectedTabPanel.classList.add('active');
				});
			});
		}

		//레이어탭 초기화
		function layerTabInitial() {
			layerTabEle.forEach(ele => {
				ele.classList.remove('active');
				ele.setAttribute('aria-selected', 'false');
				//ele.removeAttribute('style');
				if (ele.classList.contains('active')) {
					const text = ele.querySelector('.sr-only.created');
					ele.querySelector('button').removeChild(text);
				}
			});
			tabPanel.forEach(ele => {
				ele.classList.remove('active');
				//ele.removeAttribute('style');
			})
		}
		tab();
	});
}

/*** * DATEPICKER * ***/
/* ** datepicker ** */
const dateInput = document.querySelectorAll('.form-btn-datepicker');
const kds_datepicker = {
	init: () => {
		kds_datepicker.open();
		kds_datepicker.selValue();
		kds_datepicker.closeDatepicker();
		kds_datepicker.closeSingle();
	},
	tblHeightSet: () => { //datepicker table th, td height 세팅
		const cal = document.querySelectorAll('.datepicker-area');
		cal.forEach(e => {
			const datepickerEl = e.querySelector('.datepicker-tbl');
			const cell = datepickerEl.querySelectorAll('th, td');
			cell.forEach(ele => {
				const w = ele.clientWidth + 4; //윗간격 4px 추가
				const wResult = w.toFixed(2); //소수점 2자리에서 반올림됨
				ele.setAttribute('style', 'height:' + wResult + 'px');
			});
		});
	},
	contsHeightSet: () => { //datepicker contents layer height 세팅
		const cal = document.querySelectorAll('.datepicker-area');
		cal.forEach(e => {
			const body = e.querySelector('.datepicker-body');
			const bodyConts = e.querySelectorAll('.datepicker-conts');
			bodyConts.forEach(ele => {
				let contsHeight;
				if (ele.classList.contains('active')) {
					if (ele.classList.contains('datepicker-tbl-wrap')) {
						if (e.classList.contains('range')) {
							contsHeight = ele.querySelector('.datepicker-tbl').offsetHeight + ele.querySelector('.datepicker-btn-wrap').offsetHeight;
						} else {
							contsHeight = ele.querySelector('.datepicker-tbl').offsetHeight;
						}
					} else {
						contsHeight = '316';
					}
					body.setAttribute('style', 'height: '+ contsHeight +'px');
				}
			});
		});
	},
	open: () => { //datepicker 열기
		dateInput.forEach(e => {
			const cal = e.closest('.datepicker-conts').querySelector('.datepicker-area');
			const colConts = cal.querySelector('.datepicker-wrap');
			e.addEventListener('focus', () => {
				kds_datepicker.close();

				cal.classList.add('active');
				colConts.setAttribute('tabindex', '0');
				colConts.setAttribute('aria-hidden', 'false');

				const activeLayer = cal.querySelector('.datepicker-tbl-wrap');
				activeLayer.classList.add('active');
				activeLayer.setAttribute('tabindex', '0');
				activeLayer.setAttribute('aria-hidden', 'false');

				kds_datepicker.tblHeightSet();
				kds_datepicker.contsHeightSet();

				setTimeout(() => {
					colConts.focus();
				}, 50);
			});
		});
	},
	close: () => { //datepicker 닫기
		const cal = document.querySelectorAll('.datepicker-area');
		cal.forEach(e => {
			const colConts = e.querySelector('.datepicker-wrap');
			e.classList.remove('active');
			colConts.setAttribute('tabindex', '-1');
			colConts.setAttribute('aria-hidden', 'true');
		});
	},
	contentsInitialize: () => {
		const cal = document.querySelectorAll('.datepicker-area');
		cal.forEach(e => {
			const bodyConts = e.querySelectorAll('.datepicker-conts');
			bodyConts.forEach(ele => {
				ele.classList.remove('active');
				ele.setAttribute('tabindex', '-1');
				ele.setAttribute('aria-hidden', 'true');
			});
		});
	},
	selValue: () => {
		const cal = document.querySelectorAll('.datepicker-area');
		cal.forEach(e => {
			const changeCalBtn = e.querySelectorAll('.datepicker-conts .sel .btn');
			const setBtn = e.querySelectorAll('.datepicker-btn-wrap .btn');

			const yearBtn = e.querySelector('.btn-cal-switch.year');
			const monBtn = e.querySelector('.btn-cal-switch.month');

			let activeLayer;
			yearBtn.addEventListener('click', () => { //년도 레이어 활성화
				kds_datepicker.contentsInitialize();
				activeLayer = e.querySelector('.datepicker-year-wrap');
				activeLayer.classList.add('active');
				activeLayer.setAttribute('tabindex', '0');
				activeLayer.setAttribute('aria-hidden', 'false');
				setTimeout(() => {
					activeLayer.focus();
				}, 50);
				kds_datepicker.contsHeightSet();
			});
			monBtn.addEventListener('click', () => { //월 레이어 활성화
				kds_datepicker.contentsInitialize();
				activeLayer = e.querySelector('.datepicker-mon-wrap');
				activeLayer.classList.add('active');
				activeLayer.setAttribute('tabindex', '0');
				activeLayer.setAttribute('aria-hidden', 'false');
				setTimeout(() => {
					activeLayer.focus();
				}, 50);
				kds_datepicker.contsHeightSet();
			});
			setBtn.forEach(ele => { //확인 취소버튼 클릭하면 datepicker 닫힘
				ele.addEventListener('click', () => {
					kds_datepicker.close();
				});
			});
			changeCalBtn.forEach(ele => { //년도 또는 월 선택하면 캘린더 레이어 활성화
				ele.addEventListener('click', () => {
					kds_datepicker.contentsInitialize();
					activeLayer = e.querySelector('.datepicker-tbl-wrap');
					activeLayer.classList.add('active');
					activeLayer.setAttribute('tabindex', '0');
					activeLayer.setAttribute('aria-hidden', 'false');
					setTimeout(() => {
						activeLayer.focus();
					}, 50);
					kds_datepicker.contsHeightSet();
				});
			});
		});
	},
	closeDatepicker: () => {
		const cal = document.querySelectorAll('.datepicker-area');
		cal.forEach(e => {
			const bodyConts = e.querySelectorAll('.datepicker-conts');
			let lastElement;
			bodyConts.forEach(ele => {
				if (ele.classList.contains('datepicker-tbl-wrap')) {
					if (e.classList.contains('range')) {
						lastElement = ele.querySelector('.datepicker-btn-wrap > .btn:last-child');
					} else {
						lastElement = ele.querySelector('.datepicker-tbl tbody tr:last-child > td:last-child .btn-set-date');
					}
				} else {
					lastElement = ele.querySelector('.sel > li:last-child > .btn');
				}
				lastElement.addEventListener('blur', () => {
					kds_datepicker.close();
				});
			});
		});
	},
	closeSingle: () => {
		const cal = document.querySelectorAll('.datepicker-area');
		cal.forEach(e => {
			const colConts = e.querySelector('.datepicker-wrap');

			if (colConts.classList.contains('single')) {
				const calBtn = colConts.querySelectorAll('.datepicker-tbl .btn-set-date');
				calBtn.forEach(ele => {
					ele.addEventListener('click', () => {
						kds_datepicker.close();
					});
				});
			}
		});
	}
}
document.addEventListener('click', (e) => {
	if(!e.target.closest(".datepicker-conts")) {
		kds_datepicker.close();
	};
});

/*** * accordion * ***/
const $accordionBtn = document.querySelectorAll('.btn-accordion');
const kds_accordion = {
	init: () => {
		kds_accordion.expand();
	},
	expand: () => {
		$accordionBtn.forEach(e => {
			const $wrapper = e.closest('.accordion');
			const $wrapAll = $wrapper.querySelectorAll('.accordion-item');
			const $wrap = e.closest('.accordion-item');

			e.addEventListener('click', () => {
				if (!$wrap.classList.contains('active')) {
					$wrapAll.forEach(ele => {
						ele.classList.remove('active');
						ele.querySelector('.btn-accordion').classList.remove('active');
					});

					$wrap.classList.add('active');
					e.classList.add('active');
				}
				else {
					$wrap.classList.remove('active');
					e.classList.remove('active');
				}
			});

		});
	},
}

/*** * modal * ***/
const $modalTrigger = document.querySelectorAll('.open-modal');
const $modalCloseTrigger = document.querySelectorAll('.close-modal');
const $kds_body = document.querySelector('body');
const kds_modal = {
	init: () => {
		kds_modal.open();
		kds_modal.close();
	},
	open: () => {
		$modalTrigger.forEach(e => {
			e.addEventListener('click', ele => {
				const id = e.getAttribute('data-target');

				e.classList.add('modal-opened');
				e.setAttribute('tabindex', '-1');

				kds_modal.modalOpen(id);
				ele.preventDefault();
			});
		});
	},
	modalOpen: (id) => {
		const $idVal = document.getElementById(id);
		const $dialog = $idVal.querySelector('.modal-content');
		const $modalBack = $idVal.querySelector('.modal-back');
		const $modalOpened = document.querySelectorAll('.modal.in:not(.sample)');
		const $modalOpenedLen = $modalOpened.length + 1;
		$kds_body.classList.add('scroll-no');
		$idVal.setAttribute('aria-hidden', 'false');
		$modalBack.classList.add('in');
		$idVal.classList.add('shown');
		setTimeout(() => {
			$idVal.classList.add('in');
		},150);

		//열린 팝업창 포커스
		$dialog.setAttribute('tabindex', '0');

		//모달 여러개 열린경우 마지막 열린 모달 z-index높게
		if ($modalOpenedLen > 1){
			const openedLen = $modalOpenedLen;
			const zIndexNew = 1010 + openedLen;
			$idVal.setAttribute('style', 'z-index: ' + zIndexNew);
		}

		//레이어 진입 시 포커스
		setTimeout(() => {
			$dialog.focus();
		},350);
	},
	close: () => {
		$modalCloseTrigger.forEach(e => {
			e.addEventListener('click', ele => {
				const id = e.closest('.modal').getAttribute('id');
				kds_modal.modalClose(id);
			});
			e.addEventListener('keydown', ele => { //닫기버튼에서 탭 키 누르면 모달 내 첫번쨰 포커스로 키보드 이동
				if (e.classList.contains('btn-close')) {
					const keyCode = ele.keyCode || ele.which;
					if (!ele.shiftKey && keyCode == 9) {
						e.closest('.modal-content').focus();// 첫번째 링크로 이동
						ele.preventDefault();
					}
				}
			});
		});
	},
	modalClose: (id) => {
		const $idVal = document.getElementById(id);
		const $dialog = $idVal.querySelector('.modal-content');
		const $modalOpened = document.querySelectorAll('.modal.in:not(.sample)');
		const $modalOpenedLen = $modalOpened.length;
		if ($modalOpenedLen < 2) {
			$kds_body.classList.remove('scroll-no');
		}

		$idVal.setAttribute('aria-hidden', 'true');
		$idVal.classList.remove('in');

		$dialog.removeAttribute('tabindex');

		setTimeout(() => {
			$idVal.classList.remove('shown');
		},150);

		//모달 창 연 버튼에 class 삭제 및 tabindex 0로 조정 (포커스 영역 수정)
		const $triggerBtn = document.querySelector('.modal-opened');
		if ($triggerBtn != null) {
			$triggerBtn.focus();
			$triggerBtn.setAttribute('tabindex', '0');
			$triggerBtn.classList.remove('modal-opened');
		}
	},
}

/*** * tooltip * ***/
const krds_tooltip = {
    init: ()=> {
        krds_tooltip.tooltipEvent();
    },
    tooltipEvent: () => {
        const _toolBtns = document.querySelectorAll(".krds-tooltip-wrap .tool-btn");

        _toolBtns.forEach(($toolBtn) => {
            const _span = document.createElement("span");
            const _txt = document.createTextNode("열기");

            _span.classList.add("sr-only");
            _span.appendChild(_txt);
            
            $toolBtn.innerHTML="";
            $toolBtn.appendChild(_span);

            $toolBtn.addEventListener("click", ($el) => {
                const $parent = $toolBtn.closest(".krds-tooltip-wrap");
                const $closeBtn = $parent.querySelector(".tool-close");
                const $cnt = $parent.querySelector(".tool-in");
                const $srTxt = $el.target.querySelector(".sr-only");
                if($cnt.style.display !== "block") {
                    $cnt.style.display = "block";
                    $cnt.setAttribute("tabindex", 0);
                    $srTxt.textContent = "닫기";
                    krds_tooltip.tooltipResize($parent, $cnt);
                } 
                $closeBtn.addEventListener("click", () => {
                    $srTxt.textContent = "열기";
                    $cnt.style.display = "";
                    $cnt.removeAttribute("tabindex");
                    $toolBtn.focus();
                    krds_tooltip.tooltipResize($parent, $cnt);
                });

                window.addEventListener("resize", () => { 
                    krds_tooltip.tooltipResize($parent, $cnt);
                });
            });
        });
    },
    tooltipResize: ($parent, $cnt) => { 
        if (winSize === 'mob') {
            krds_tooltip.tooltipMob($parent, $cnt);
        } else { 
            krds_tooltip.tooltipPc($cnt);
        }
        window.addEventListener('resize', () => { 
            if (winSize === 'mob') {
                krds_tooltip.tooltipMob($cnt);
            } else { 
                krds_tooltip.tooltipPc($cnt);
            }
        });
    },
    tooltipMob: ($parent, $cnt) => {
        const _offsetL = $parent.offsetLeft - 16;
        const _width = document.body.clientWidth;
        const _offsetR = _width - ($parent.clientWidth + _offsetL) - 32;
        if ($cnt) { 
            $cnt.style.left = `-${_offsetL}px`;
            $cnt.style.right = `-${_offsetR}px`;
        }
    },
    tooltipPc: ($cnt) => { 
        $cnt.style.left = '';
        $cnt.style.right = '';
    },
}


window.addEventListener("DOMContentLoaded", () => {
	layerTab();
	kds_datepicker.init();
	kds_accordion.init();
    kds_modal.init();
    krds_tooltip.init();

	/* ** 윈도우 사이즈 체크 (반응형) ** */
	winSizeSet();
});

window.addEventListener('resize', () => {
	/* ** 윈도우 사이즈 체크 (반응형) ** */
	winSizeSet();
});