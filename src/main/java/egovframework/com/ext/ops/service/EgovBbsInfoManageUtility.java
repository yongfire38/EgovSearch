package egovframework.com.ext.ops.service;

import org.springframework.beans.BeanUtils;

import egovframework.com.ext.ops.entity.Comtnbbs;

public class EgovBbsInfoManageUtility {

	public static BoardVO entityToVO(Comtnbbs comtnbbs) {
		BoardVO boardVO = new BoardVO();
		BeanUtils.copyProperties(comtnbbs, boardVO);
		return boardVO;
	}
	
	public static Comtnbbs VOToEntity(BoardVO boardVO) {
		Comtnbbs comtnbbs = new Comtnbbs();
		BeanUtils.copyProperties(boardVO, comtnbbs);
		return comtnbbs;
	}
}
