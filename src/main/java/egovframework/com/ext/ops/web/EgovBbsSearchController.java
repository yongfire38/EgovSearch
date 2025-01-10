package egovframework.com.ext.ops.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import egovframework.com.ext.ops.service.BoardVO;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class EgovBbsSearchController {

	@GetMapping(value="/")
    public String index() {
		return this.searchPageView();
	}
	
	@PostMapping(value="/ext/ops/searchPageView")
    public String searchPageView() {
        return "bbsSearch";
    }
	
	@PostMapping(value="/ext/ops/textSearchResultView")
    public String textSearchResultView(BoardVO boardVO, Model model) {
        model.addAttribute("BoardVO", boardVO);
        log.debug(boardVO.toString());
        return "textSearchResult";
    }
    
    @PostMapping(value="/ext/ops/embeddingSearchResultView")
    public String embeddingSearchResultView(BoardVO boardVO, Model model) {
        model.addAttribute("BoardVO", boardVO);
        log.debug(boardVO.toString());
        return "embeddingSearchResult";
    }
}
