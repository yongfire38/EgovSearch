package egovframework.com.ext.ops.service.impl;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.beans.BeanUtils;
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
	        SearchRequest.Builder builder = new SearchRequest.Builder().index(textIndexName);
	        
	        // 페이지 번호와 사이즈 설정
	        int pageNumber = boardVO.getPageIndex() - 1;
	        int pageSize = textSearchPageSize;
	        
	        builder.from(pageNumber * pageSize);
	        builder.size(pageSize);  // 한 페이지당 보여줄 개수만큼만 가져오도록 수정
			
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
				
			long totalHits = searchResponse.hits().total().value();
	        Sort sort = Sort.by("nttId").descending();
	        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

	        return new PageImpl<>(searchResults, pageable, totalHits);
		} catch (Exception e) {
			log.error("Error occurred during text search process:", e);
			throw processException("fail.common.msg", e);
		}
	}

	@Override
	public Page<BoardEmbeddingVO> embeddingSearch(BoardVO boardVO) throws Exception {
			try {
				
				// Open Search 인덱스에서 검색 요청
		        SearchRequest.Builder builder = new SearchRequest.Builder().index(embeddingIndexName);
		        
		        // 페이지 번호와 사이즈 설정
		        int pageNumber = boardVO.getPageIndex() - 1;
		        int pageSize = embeddingSearchPageSize;
		        
		        builder.from(pageNumber * pageSize);
		        builder.size(pageSize);  // 한 페이지당 보여줄 개수만큼만 가져오도록 수정
				
				BoardEmbeddingVO boardEmbeddingVO = new BoardEmbeddingVO();
		        BeanUtils.copyProperties(boardVO, boardEmbeddingVO); // 기존 필드 복사
				
				EmbeddingModel embeddingModel = new OnnxEmbeddingModel(modelPath, tokenizerPath, PoolingMode.MEAN);
				
				//화면에서는 보통 파라미터 1개만 검색 창에서 넘길 것이므로 임의로 '글 내용'으로 함
				Embedding response = embeddingModel.embed(boardEmbeddingVO.getNttCn()).content();
				
				builder.query(q -> q.bool(b -> b
					.must(m -> m.knn(k -> k.field("bbsArticleEmbedding").vector(response.vector()).k(embeddingSearchCount)))
					.must(m -> m.match(mt -> mt.field("useAt").query(FieldValue.of("Y"))))
				));
				
				builder.query(q -> q.knn(k -> k.field("bbsArticleEmbedding").vector(response.vector()).k(embeddingSearchCount)));
				
				// qestnEmbedding 컬럼을 대상으로 벡터 검색 (유사한 순으로 egov.embeddingsearch.count 에 기재된 건수만큼 조회. 디폴트는 5건)
				SearchResponse<BoardEmbeddingVO> searchResponse = client.search(builder.build(), BoardEmbeddingVO.class);
				
				// 유사도 점수 추가
				List<BoardEmbeddingVO> searchResults = searchResponse.hits().hits().stream().map(hit -> {
					BoardEmbeddingVO result = hit.source();
					result.setScore(hit.score());
					return result;
				}).collect(Collectors.toList());
				
				long totalHits = searchResponse.hits().total().value();
		        Sort sort = Sort.by("nttId").descending();
		        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
		        
		        return new PageImpl<>(searchResults, pageable, totalHits);
			
			} catch (Exception e) {
				log.error("Error occurred during embedding search process:", e);
				throw processException("fail.common.msg", e);
			}
	}
	
}
