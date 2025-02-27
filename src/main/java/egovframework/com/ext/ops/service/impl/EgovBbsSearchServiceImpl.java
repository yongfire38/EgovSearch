package egovframework.com.ext.ops.service.impl;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.google.gson.Gson;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel;
import dev.langchain4j.model.embedding.onnx.PoolingMode;
import egovframework.com.ext.ops.config.SearchConfig;
import egovframework.com.ext.ops.service.BoardVectorVO;
import egovframework.com.ext.ops.service.BoardVO;
import egovframework.com.ext.ops.service.EgovBbsSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class EgovBbsSearchServiceImpl extends EgovAbstractServiceImpl implements EgovBbsSearchService, InitializingBean {

	@Value("${opensearch.text.indexname}")
    public String textIndexName;
	
	@Value("${opensearch.vector.indexname}")
    public String vectorIndexName;
	
	@Value("${egov.textsearch.count}")
    private int textSearchCount;
	
	@Value("${egov.textsearch.page.size}")
    private int textSearchPageSize;
	
	@Value("${egov.vectorsearch.count}")
    private int vectorSearchCount;
	
	@Value("${egov.vectorsearch.page.size}")
    private int vectorSearchPageSize;
	
	@Value("${app.search-config-path}")
	private String configPath;
	
	private String modelPath;
	
    private String tokenizerPath;
    
    private EmbeddingModel embeddingModel;
    
    private final OpenSearchClient client;
	
	private static final Map<String, String> SEARCH_FIELD_MAP;
    
    static {
        Map<String, String> map = new HashMap<>();
        map.put("1", "nttSj");
        map.put("2", "nttCn");
        map.put("3", "ntcrNm");
        SEARCH_FIELD_MAP = Collections.unmodifiableMap(map);
    }
	
    @Override
    public void afterPropertiesSet() throws Exception {
        loadConfig();
        this.embeddingModel = new OnnxEmbeddingModel(modelPath, tokenizerPath, PoolingMode.MEAN);
    }
    
    private void loadConfig() {
        try {
            String jsonStr = new String(Files.readAllBytes(Paths.get(configPath)));
            SearchConfig config = new Gson().fromJson(jsonStr, SearchConfig.class);
            
            this.modelPath = config.getModelPath();
            this.tokenizerPath = config.getTokenizerPath();
            
        } catch (Exception e) {
            log.error("Failed to load search config: " + e.getMessage());
            throw new RuntimeException("Failed to load configuration", e);
        }
    }
	
	private <T> Page<T> executeSearch(String indexName, int searchCount, int pageSize, 
            int pageIndex, SearchRequest.Builder builder, Class<T> responseType) throws Exception {
		
		try {
			// 기본 검색 설정
			builder.index(indexName)
            .trackScores(true)
            .from(0)
            .size(searchCount)
            .sort(s -> s.score(f -> f.order(SortOrder.Desc)));

			SearchResponse<T> searchResponse = client.search(builder.build(), responseType);
			
			// 검색 결과 처리 (유사도 점수 추가)
			List<T> searchResults = processSearchResults(searchResponse);
			
			// 페이징 처리
			return createPagedResult(searchResults, pageIndex, pageSize, searchCount, searchResponse.hits().total().value());
			
		} catch (Exception e) {
            log.error("Error occurred during search process:", e);
            throw processException("fail.common.msg", e);
        }
	}
	
	private <T> List<T> processSearchResults(SearchResponse<T> searchResponse) {
		return searchResponse.hits().hits().stream().map(hit -> {
			T result = hit.source();
			
			if (result instanceof BoardVO) {
				((BoardVO) result).setScore(hit.score());
			} else if (result instanceof BoardVectorVO) {
				((BoardVectorVO) result).setScore(hit.score());
			}
			
			return result;
		}).collect(Collectors.toList());
	}
	
	private <T> Page<T> createPagedResult(List<T> results, int pageIndex, int pageSize, 
        int searchCount, long totalHits) {
		int pageNumber = pageIndex - 1;
        int start = pageNumber * pageSize;
        int end = Math.min(start + pageSize, results.size());
        
        // 시작 인덱스가 전체 결과 수보다 크면 빈 리스트 반환
        List<T> pageContent = start >= results.size() ? 
                new ArrayList<>() : results.subList(start, end);
        
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("nttId").descending());
        return new PageImpl<>(pageContent, pageable, Math.min(totalHits, searchCount));
	}
	
	@Override
    public Page<BoardVO> textSearch(BoardVO boardVO) throws Exception {
		SearchRequest.Builder builder = new SearchRequest.Builder();
		
		// 검색 조건 설정
		if (!ObjectUtils.isEmpty(boardVO.getSearchWrd())) {
			String field = SEARCH_FIELD_MAP.getOrDefault(boardVO.getSearchCnd(), "");
			
			if (!field.isEmpty()) {
				builder.query(q -> q.bool(b -> b
	                    .must(m -> m.match(mt -> mt.field(field)
	                        .query(FieldValue.of(boardVO.getSearchWrd()))
	                        .analyzer("nori-analyzer")
	                        .fuzziness("AUTO")))
	                    .must(m -> m.match(mt -> mt.field("useAt")
	                        .query(FieldValue.of("Y"))))
	                ));
			}
        }
		
		 return executeSearch(textIndexName, textSearchCount, textSearchPageSize, 
	                boardVO.getPageIndex(), builder, BoardVO.class);
	}
	
	@Override
	public Page<BoardVectorVO> vectorSearch(BoardVO boardVO) throws Exception {
		SearchRequest.Builder builder = new SearchRequest.Builder();
        
        Embedding response = embeddingModel.embed(boardVO.getSearchWrd()).content();
        
        builder.query(q -> q.bool(b -> b
            .must(m -> m.knn(k -> k.field("bbsArticleVector")
                .vector(response.vector())
                .k(vectorSearchCount)))
            .must(m -> m.match(mt -> mt.field("useAt")
                .query(FieldValue.of("Y"))))
        ));
        
        return executeSearch(vectorIndexName, vectorSearchCount, vectorSearchPageSize,
                boardVO.getPageIndex(), builder, BoardVectorVO.class);
	}
}
