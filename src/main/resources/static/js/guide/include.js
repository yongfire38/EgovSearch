function includeHtmlGuide() {
	const includeTarget = document.querySelectorAll('.includeJsGuide');
	includeTarget.forEach(function(el, idx) {
		const targetFile = el.dataset.includeFile;
		if(targetFile){
			let xhttp = new XMLHttpRequest();

			xhttp.onreadystatechange = function() {
				if (this.readyState === XMLHttpRequest.DONE) {
					this.status === 200 ? (el.innerHTML = this.responseText) : null
					this.status === 404 ? (el.innerHTML = 'include not found.') : null
				}
			}
			xhttp.open('GET', targetFile, true);
			xhttp.send();
			return;
		}
	});
}

/* lnb active */
const lnbSet = {
	init: () => {
		lnbSet.linkActive();
		lnbSet.relativeActive();
		lnbSet.btnToggle();
	},
	initialize: () => {
		const g_lnbLink = document.querySelectorAll('.g-aside .lnb li');
		g_lnbLink.forEach(e => {
			e.classList.remove('active');
		});
	},
	linkActive: () => {
		lnbSet.initialize();
		const g_lnbLink = document.querySelectorAll('.g-aside .lnb li');
		g_lnbLink.forEach(e => {
			//lnb link 가져오기
			const link = e.querySelector('a').getAttribute('href');
			const linkStr = link.substring(link.lastIndexOf('/') + 1);

			//page url 가져오기
			const pageUrl = window.location.href;
			const urlStr = pageUrl.substring(pageUrl.lastIndexOf('/') + 1);

			const splitTxt = ".html";
			const urlStrSplit = urlStr.split(splitTxt);
			const matchUrl = urlStrSplit[0] + splitTxt;
			
			if (linkStr == matchUrl) { //page url과 일치하는 lnb link에 class add
				e.classList.add('active');
			};
		})
	},
	relativeActive: () => {
		const g_lnbLink = document.querySelectorAll('.g-aside .lnb li');
		g_lnbLink.forEach(e => {
			const lnb3d = document.querySelectorAll('.lnb .depth3');
			lnb3d.forEach(e => {
				const li = Array.from(e.children);
				li.forEach(ele => {
					if (ele.classList.contains('active')) {
						e.closest('li').classList.add('active');
					}
				});
			});
			const lnb2d = document.querySelectorAll('.lnb .depth2');
			lnb2d.forEach(e => {
				const li = Array.from(e.children);
				li.forEach(ele => {
					if (ele.classList.contains('active')) {
						e.closest('li').classList.add('active');
					}
				});
			});
		});
	},
	btnToggle: () => {
		const toggleBtn = document.querySelectorAll('.btn-menu-toggle');
		toggleBtn.forEach(e => {
			const parentLi = e.closest('li');
			e.addEventListener('click', () => {
				lnbSet.initialize();
				parentLi.classList.toggle('active');
			})
		});
	},
};

/* gnb active */
function gnbActive() {
	const g_gnbLink = document.querySelectorAll('#g-header .gnb li');
	g_gnbLink.forEach(e => {
		e.classList.remove('active');

		//lnb link 가져오기
		const link = e.querySelector('a').getAttribute('href');
		const linkSplit = link.split('/');
		const linkDirStr = linkSplit[linkSplit.length - 2];

		const pageUrl = window.location.href;
		const urlSplit = pageUrl.split('/');
		const urlDirStr = urlSplit[urlSplit.length - 2];
		if (linkDirStr == urlDirStr) { //page url과 일치하는 gnb directory에 class add
			e.classList.add('active');
		};
	});
}

/* responsive layout */

const lnbRes = {
	init: () => {
		lnbRes.open();
		lnbRes.close();
	},
	open: () => {
		const $mobMenu = document.querySelector('#g-header .mob-menu');
		const $lnb = document.querySelector('.g-aside');

		$mobMenu.addEventListener('click', () => {
			if ($lnb != null) {
				if (!$lnb.classList.contains('active')) {
					$lnb.classList.add('active');
					$kds_body.classList.add('scroll-no');
				}
			}
		});
	},
	close: () => {
		const $lnb = document.querySelector('.g-aside');
		if ($lnb != null) {
			const $lnbCLose = $lnb.querySelector('.btn.ico-close');

			$lnbCLose.addEventListener('click', () => {
				if ($lnb.classList.contains('active')) {
					$lnb.classList.remove('active');
					$kds_body.classList.remove('scroll-no');
				}
			});
		}
	},
	resize: () => {
		const $lnb = document.querySelector('.g-aside');
		if ($lnb != null) {
			if (winSize == 'pc') {
				if ($lnb.classList.contains('active')) {
					$lnb.classList.remove('active');
					$kds_body.classList.remove('scroll-no');
				}
			}
		}
	}
};

window.addEventListener("DOMContentLoaded", () => {
	includeHtmlGuide();
	setTimeout(() => {
		lnbSet.init();
		gnbActive();
		lnbRes.init();
	},300);
});
window.addEventListener('resize', () => {
	setTimeout(() => {
		lnbRes.resize();
	}, 200);
});