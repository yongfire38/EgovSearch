package egovframework.com.ext.ops.web;

import java.util.HashMap;
import java.util.Map;

import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import egovframework.com.ext.ops.pagination.EgovPaginationFormat;
import egovframework.com.ext.ops.service.BoardEmbeddingVO;
import egovframework.com.ext.ops.service.BoardVO;
import egovframework.com.ext.ops.service.EgovBbsSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class EgovBbsSearchAPIController {

	@Value("${egov.textsearch.count}")
    private int textSearchCount;
	
	@Value("${egov.textsearch.page.size}")
    private int textSearchPageSize;
	
	@Value("${egov.embeddingsearch.count}")
    private int embeddingSearchCount;
	
	@Value("${egov.embeddingsearch.page.size}")
    private int  embeddingSearchPageSize;
	
	private final EgovBbsSearchService service;
	
	@PostMapping("/ext/ops/textSearchResult")
	public ResponseEntity<?> selectBbsTextSearchList(@ModelAttribute BoardVO boardVO) throws Exception {
		
		PaginationInfo paginationInfo = new PaginationInfo();
		paginationInfo.setCurrentPageNo(boardVO.getPageIndex());
		paginationInfo.setRecordCountPerPage(textSearchPageSize);
		paginationInfo.setPageSize(10); // 화면에 보여질 페이지 번호 개수
		
        boardVO.setFirstIndex(paginationInfo.getCurrentPageNo()-1);
        boardVO.setLastIndex(paginationInfo.getLastRecordIndex());
        boardVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());
		
		Page<BoardVO> textSearchList = this.service.textSearch(boardVO);
		paginationInfo.setTotalRecordCount((int)textSearchList.getTotalElements()); // 실제 검색 결과 총 개수 사용

		EgovPaginationFormat egovPaginationFormat = new EgovPaginationFormat();
		String pagination = egovPaginationFormat.paginationFormat(paginationInfo, "linkPage");

		Map<String, Object> response = new HashMap<>();
		response.put("bbsTextSearchList", textSearchList.getContent());
		response.put("pagination", pagination);
		response.put("lineNumber", (boardVO.getPageIndex() - 1) * boardVO.getPageSize());
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/ext/ops/embeddingSearchResult")
	public ResponseEntity<?> selectBbsEmbeddingSearchList(@ModelAttribute BoardVO boardVO) throws Exception {
		PaginationInfo paginationInfo = new PaginationInfo();
		paginationInfo.setCurrentPageNo(boardVO.getPageIndex());
		paginationInfo.setRecordCountPerPage(embeddingSearchPageSize);
		paginationInfo.setPageSize(10); // 화면에 보여질 페이지 번호 개수
		
		boardVO.setFirstIndex(paginationInfo.getCurrentPageNo()-1);
		boardVO.setLastIndex(paginationInfo.getLastRecordIndex());
		boardVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());
		
		Page<BoardEmbeddingVO> embeddingSearchList = this.service.embeddingSearch(boardVO);
		paginationInfo.setTotalRecordCount((int)embeddingSearchList.getTotalElements()); // 실제 검색 결과 총 개수 사용
		
		EgovPaginationFormat egovPaginationFormat = new EgovPaginationFormat();
		String pagination = egovPaginationFormat.paginationFormat(paginationInfo, "linkPage");

		Map<String, Object> response = new HashMap<>();
		response.put("bbsEmbeddingSearchList", embeddingSearchList.getContent());
		response.put("pagination", pagination);
		response.put("lineNumber", (boardVO.getPageIndex() - 1) * boardVO.getPageSize());
		return ResponseEntity.ok(response);
	}
}
