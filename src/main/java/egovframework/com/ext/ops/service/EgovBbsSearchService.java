package egovframework.com.ext.ops.service;

import org.springframework.data.domain.Page;

public interface EgovBbsSearchService {

	public Page<BoardVO> textSearch(BoardVO boardVO) throws Exception;
	
	public Page<BoardEmbeddingVO> embeddingSearch(BoardVO boardVO) throws Exception;
}
