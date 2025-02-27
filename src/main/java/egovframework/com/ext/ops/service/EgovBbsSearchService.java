package egovframework.com.ext.ops.service;

import org.springframework.data.domain.Page;

public interface EgovBbsSearchService {

	public Page<BoardVO> textSearch(BoardVO boardVO) throws Exception;
	
	public Page<BoardVectorVO> vectorSearch(BoardVO boardVO) throws Exception;
}
