package egovframework.com.ext.ops.service.impl;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel;
import dev.langchain4j.model.embedding.onnx.PoolingMode;
import egovframework.com.ext.ops.service.BoardEmbeddingVO;
import egovframework.com.ext.ops.service.BoardVO;
import egovframework.com.ext.ops.service.EgovBbsSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class EgovBbsSearchServiceImpl extends EgovAbstractServiceImpl implements EgovBbsSearchService {

	@Value("${opensearch.text.indexname}")
    public String textIndexName;
	
	@Value("${opensearch.embedding.indexname}")
    public String embeddingIndexName;
	
	@Value("${egov.textsearch.count}")
    private int textSearchCount;
	
	@Value("${egov.textsearch.page.size}")
    private int textSearchPageSize;
	
	@Value("${egov.embeddingsearch.count}")
    private int embeddingSearchCount;
	
	@Value("${egov.embeddingsearch.page.size}")
    private int  embeddingSearchPageSize;
	
	public static final String modelPath = Paths.get(System.getProperty("user.dir")).resolve("model").resolve("model.onnx").toString();
	public static final String tokenizerPath = Paths.get(System.getProperty("user.dir")).resolve("model").resolve("tokenizer.json").toString();
	
	private final OpenSearchClient client;
	
	@Override
    public Page<BoardVO> textSearch(BoardVO boardVO) throws Exception {
		
		try {
			// Open Search 인덱스에서 검색 요청
	        SearchRequest.Builder builder = new SearchRequest.Builder().index(textIndexName).trackScores(true);;
	        
	        // OpenSearch에서 가져올 전체 결과 수를 textSearchCount로 설정
	        builder.from(0);  // 전체 결과를 가져오기 위해 0부터 시작
	        builder.size(textSearchCount);
	        
	        // score 기준 내림차순 정렬 설정
	        builder.sort(s -> s.score(f -> f.order(SortOrder.Desc)));
			
			// 검색 조건
			if(!ObjectUtils.isEmpty(boardVO.getSearchWrd()) && "1".equals(boardVO.getSearchCnd())) {
				builder.query(q -> q.bool(b -> b
					.must(m -> m.match(mt -> mt.field("nttSj").query(FieldValue.of(boardVO.getSearchWrd())).analyzer("nori-analyzer").fuzziness("AUTO")))
					.must(m -> m.match(mt -> mt.field("useAt").query(FieldValue.of("Y"))))
				));
			} else if(!ObjectUtils.isEmpty(boardVO.getSearchWrd()) && "2".equals(boardVO.getSearchCnd())) {
				builder.query(q -> q.bool(b -> b
					.must(m -> m.match(mt -> mt.field("nttCn").query(FieldValue.of(boardVO.getSearchWrd())).analyzer("nori-analyzer").fuzziness("AUTO")))
					.must(m -> m.match(mt -> mt.field("useAt").query(FieldValue.of("Y"))))
				));
			} else if(!ObjectUtils.isEmpty(boardVO.getSearchWrd()) && "3".equals(boardVO.getSearchCnd())) {
				builder.query(q -> q.bool(b -> b
					.must(m -> m.match(mt -> mt.field("ntcrNm").query(FieldValue.of(boardVO.getSearchWrd())).analyzer("nori-analyzer").fuzziness("AUTO")))
					.must(m -> m.match(mt -> mt.field("useAt").query(FieldValue.of("Y"))))
				));
			}
			
			SearchResponse<BoardVO> searchResponse = client.search(builder.build(), BoardVO.class);
			
			//유사도 점수 추가
			List<BoardVO> searchResults = searchResponse.hits().hits().stream().map(hit -> {
				BoardVO result = hit.source();
				result.setScore(hit.score());
				return result;
			}).collect(Collectors.toList());
				
			// 전체 결과 수 계산
	        long totalHits = Math.min(searchResponse.hits().total().value(), textSearchCount);
	        
	        // 페이징 처리
	        int pageNumber = boardVO.getPageIndex() - 1;
	        int pageSize = textSearchPageSize;
	        int start = pageNumber * pageSize;
	        int end = Math.min(start + pageSize, searchResults.size());
	        
	        // 시작 인덱스가 전체 결과 수보다 크면 빈 리스트 반환
	        List<BoardVO> pageContent = start >= searchResults.size() ? 
	            new ArrayList<>() : searchResults.subList(start, end);
	            
	        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("nttId").descending());
	        
	        return new PageImpl<>(pageContent, pageable, totalHits);
		} catch (Exception e) {
			log.error("Error occurred during text search process:", e);
			throw processException("fail.common.msg", e);
		}
	}

	@Override
	public Page<BoardEmbeddingVO> embeddingSearch(BoardVO boardVO) throws Exception {
			try {
				
				// Open Search 인덱스에서 검색 요청
		        SearchRequest.Builder builder = new SearchRequest.Builder().index(embeddingIndexName).trackScores(true);;
 
		        // OpenSearch에서 가져올 전체 결과 수를 embeddingSearchCount로 설정
		        builder.from(0);  // 전체 결과를 가져오기 위해 0부터 시작
		        builder.size(embeddingSearchCount);
		        
		        // score 기준 내림차순 정렬 설정
		        builder.sort(s -> s.score(f -> f.order(SortOrder.Desc)));
				
				EmbeddingModel embeddingModel = new OnnxEmbeddingModel(modelPath, tokenizerPath, PoolingMode.MEAN);
				
				Embedding response = embeddingModel.embed(boardVO.getSearchWrd()).content();
				
				builder.query(q -> q.bool(b -> b
					.must(m -> m.knn(k -> k.field("bbsArticleEmbedding").vector(response.vector()).k(embeddingSearchCount)))
					.must(m -> m.match(mt -> mt.field("useAt").query(FieldValue.of("Y"))))
				));
				
				// bbsArticleEmbedding 컬럼을 대상으로 벡터 검색 (유사한 순으로 egov.embeddingsearch.count 에 기재된 건수만큼 조회. 디폴트는 5건)
				SearchResponse<BoardEmbeddingVO> searchResponse = client.search(builder.build(), BoardEmbeddingVO.class);
				
				// 유사도 점수 추가
				List<BoardEmbeddingVO> searchResults = searchResponse.hits().hits().stream().map(hit -> {
					BoardEmbeddingVO result = hit.source();
					result.setScore(hit.score());
					return result;
				}).collect(Collectors.toList());
				
				// 전체 결과 수 계산
		        long totalHits = Math.min(searchResponse.hits().total().value(), embeddingSearchCount);
		        
		        // 페이지 번호와 사이즈 설정
		        int pageNumber = boardVO.getPageIndex() - 1;
		        int pageSize = embeddingSearchPageSize;
		        
		        // 페이징 처리
		        int start = pageNumber * pageSize;
		        int end = Math.min(start + pageSize, searchResults.size());  // end가 리스트 크기를 넘지 않도록 보장
		        
		        // 시작 인덱스가 전체 결과 수보다 크면 빈 리스트 반환
		        List<BoardEmbeddingVO> pageContent = start >= searchResults.size() ? 
		            new ArrayList<>() : searchResults.subList(start, end);
		            
		        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("nttId").descending());
		        
		        return new PageImpl<>(pageContent, pageable, totalHits);
			
			} catch (Exception e) {
				log.error("Error occurred during embedding search process:", e);
				throw processException("fail.common.msg", e);
			}
	}
	
}
