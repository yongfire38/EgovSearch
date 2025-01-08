package egovframework.com.ext.ops.service.impl;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.GetRequest;
import org.opensearch.client.opensearch.core.GetResponse;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.UpdateRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel;
import dev.langchain4j.model.embedding.onnx.PoolingMode;
import egovframework.com.ext.ops.entity.Comtnbbsmanage;
import egovframework.com.ext.ops.repository.ComtnbbsRepository;
import egovframework.com.ext.ops.repository.ComtnbbsmanageRepository;
import egovframework.com.ext.ops.service.AsyncService;
import egovframework.com.ext.ops.service.BoardEmbeddingVO;
import egovframework.com.ext.ops.service.BoardVO;
import egovframework.com.ext.ops.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncServiceImpl implements AsyncService {

	@Value("${opensearch.text.indexname}")
    private String textIndexName;
	
	@Value("${opensearch.embedding.indexname}")
	private String embeddingIndexName;
	
	public static final String modelPath = Paths.get(System.getProperty("user.dir")).resolve("model").resolve("model.onnx").toString();
	public static final String tokenizerPath = Paths.get(System.getProperty("user.dir")).resolve("model").resolve("tokenizer.json").toString();
	
	private final ComtnbbsRepository comtnbbsRepository;
	private final ComtnbbsmanageRepository comtnbbsmanageRepository;
	private final OpenSearchClient client;
	
	@Async
	@Override
	public CompletableFuture<Void> performAsyncSync() {
		return CompletableFuture.runAsync(() -> {
			// 등록 순서대로 SYNC_STTUS_CODE가 'N' 또는 'E'인 데이터를 10건 찾기
			List<String> syncSttusCodes = Arrays.asList("N", "E");
            List<Comtnbbsmanage> records = comtnbbsmanageRepository.findTop10BySyncSttusCodeInOrderByRegistPnttmAsc(syncSttusCodes);
            
            if (records != null && !records.isEmpty()) {
            	// nttId와 bbsId가 같으면 하나만 처리하도록 그룹화
                Map<Map.Entry<Long, String>, List<Comtnbbsmanage>> groupedRecords = records.stream()
                    .collect(Collectors.groupingBy(record -> 
                        new SimpleEntry<>(record.getNttId(), record.getBbsId())
                    ));

                groupedRecords.forEach((key, bbsRecords) -> {
                    Long nttId = key.getKey();
                    String bbsId = key.getValue();
                    processGroupOfRecords(nttId, bbsId, bbsRecords);
                });
            } else {
                log.debug("SYNC_STTUS_CODE가 'N'또는 'E'인 데이터가 없습니다.");
            }
		});
	}
	
	private void processGroupOfRecords(Long nttId, String bbsId, List<Comtnbbsmanage> records) {
		try {
			BoardVO boardVO = comtnbbsRepository.findByComtnbbsIdNttId(nttId)
					.map(dto -> {
						BoardVO vo = new BoardVO();
						BeanUtils.copyProperties(dto, vo);
						return vo;
					})
					.orElse(null);
			
			if (boardVO == null) {
				log.warn("MySQL 데이터가 존재하지 않습니다. NTT_ID: {}", nttId);
				// MySQL에 데이터가 없을 때 처리: 모든 관련 레코드의 SYNC_STTUS_CODE를 'Y'로 변경
				
				records.forEach(record -> {
					ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
					Date localDate = Date.from(now.toInstant());
	                record.setSyncSttusCode("Y");
	                record.setSyncPnttm(localDate);
					comtnbbsmanageRepository.save(record);
				});
				
				return;
			}
			
			// OpenSearch 텍스트 인덱스에 MySQL상 데이터를 기준으로 insert 혹은 update 처리한다
			performOpenSearchTextOperation(nttId, boardVO);
			
			// OpenSearch 임베딩 인덱스에 MySQL상 데이터를 기준으로 insert 혹은 update 처리한다
			performOpenSearchEmbeddingOperation(nttId, boardVO);
			
			records.forEach(record -> {
            	ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
            	Date localDate = Date.from(now.toInstant());
                record.setSyncSttusCode("Y");
                record.setSyncPnttm(localDate);
                comtnbbsmanageRepository.save(record);
            });
			
		} catch (Exception e) {
            log.error("OpenSearch 작업 중 오류 발생: NTT_ID = {}, 오류: {}", nttId, e.getMessage());
        }	
	}
	
	private void performOpenSearchTextOperation(Long nttId, BoardVO boardVO) throws IOException {
		GetRequest getRequest = new GetRequest.Builder()
                .index(textIndexName)
                .id(Long.toString(nttId))
                .build();
		
		GetResponse<BoardVO> getResponse = client.get(getRequest, BoardVO.class);
		
		if (getResponse.found()) {
			log.debug("OpenSearch text 인덱스 업데이트: NTT_ID = {}" + nttId);
			
			// 테이블의 데이터를 기준으로 텍스트 기반 오픈서치 인덱스를 업데이트
			UpdateRequest<BoardVO, BoardVO> updateRequest = new UpdateRequest.Builder<BoardVO, BoardVO>()
					.id(Long.toString(nttId))
					.index(textIndexName)
					.doc(boardVO)
					.build();
			
			client.update(updateRequest, BoardVO.class);
			
			log.debug("OpenSearch text 인덱스 업데이트 완료: NTT_ID = {}", nttId);
		} else {
			log.debug("OpenSearch text 인덱스 인서트: NTT_ID = {}" + nttId);
			
			// 테이블의 데이터를 기준으로 텍스트 기반 오픈서치 인덱스에 데이터 추가
			IndexRequest<BoardVO>indexRequest = new IndexRequest.Builder<BoardVO>()
					.index(textIndexName)
					.id(Long.toString(nttId))
					.document(boardVO)
					.build();
			
			client.index(indexRequest);
            
            log.debug("OpenSearch text 인덱스 인서트 완료: NTT_ID = {}", nttId);
		}
	}

	private void performOpenSearchEmbeddingOperation(Long nttId, BoardVO boardVO) throws IOException {
		GetRequest getRequest = new GetRequest.Builder()
                .index(embeddingIndexName)
                .id(Long.toString(nttId))
                .build();
		
		GetResponse<BoardEmbeddingVO> getResponse = client.get(getRequest, BoardEmbeddingVO.class);
		
		if (getResponse.found()) {
			log.debug("OpenSearch embedding 인덱스 업데이트: NTT_ID = {}" + nttId);
    		
    		// 테이블의 데이터를 기준으로 임베딩 데이터가 포함된 오픈서치 인덱스를 업데이트
			UpdateRequest<BoardEmbeddingVO, BoardEmbeddingVO> embeddingUpdateRequest =
					new UpdateRequest.Builder<BoardEmbeddingVO, BoardEmbeddingVO>()
					.id(Long.toString(nttId))
					.index(embeddingIndexName)
					.doc(addEmbedding(boardVO))
					.build();
			
			client.update(embeddingUpdateRequest, BoardEmbeddingVO.class);
			
			log.debug("OpenSearch embedding 인덱스 업데이트 완료: NTT_ID = {}", nttId);
		} else {
			log.debug("OpenSearch embedding 인덱스 인서트: NTT_ID = {}" + nttId);
			
			// 테이블의 데이터를 기준으로 임베딩 데이터가 포함된 오픈서치 인덱스에 데이터 추가
			IndexRequest<BoardEmbeddingVO> embeddingIndexRequest = new IndexRequest.Builder<BoardEmbeddingVO>()
					.index(embeddingIndexName)
					.id(Long.toString(nttId))
					.document(addEmbedding(boardVO))
					.build();
			
			client.index(embeddingIndexRequest);
			
			log.debug("OpenSearch embedding 인덱스 인서트 완료: NTT_ID = {}", nttId);
		}
	}
	
	// `제목 + 내용`을 대상으로 임베딩 처리를 한 후 추가한다
	private BoardEmbeddingVO addEmbedding(BoardVO boardVO) {
		EmbeddingModel embeddingModel = new OnnxEmbeddingModel(
		    		modelPath,
		    		tokenizerPath,
		            PoolingMode.MEAN);
		
		String combinedText = boardVO.getNttSj() + " " + boardVO.getNttCn();
		
		Embedding articleResponse = embeddingModel.embed(StrUtil.cleanString(combinedText) ).content();
		float[] bbsArticleEmbedding = articleResponse.vector();
		
		BoardEmbeddingVO boardEmbeddingVO = new BoardEmbeddingVO();
		BeanUtils.copyProperties(boardVO, boardEmbeddingVO); // 기존 필드 복사
		boardEmbeddingVO.setBbsArticleEmbedding(bbsArticleEmbedding);	
		
		return boardEmbeddingVO;
	}
}
