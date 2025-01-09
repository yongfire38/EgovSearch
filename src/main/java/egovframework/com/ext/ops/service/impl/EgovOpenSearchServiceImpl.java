package egovframework.com.ext.ops.service.impl;

import java.io.IOException;
import java.nio.file.Paths;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.GetRequest;
import org.opensearch.client.opensearch.core.GetResponse;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.UpdateRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel;
import dev.langchain4j.model.embedding.onnx.PoolingMode;
import egovframework.com.ext.ops.service.BoardEmbeddingVO;
import egovframework.com.ext.ops.service.BoardVO;
import egovframework.com.ext.ops.service.EgovOpenSearchService;
import egovframework.com.ext.ops.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class EgovOpenSearchServiceImpl implements EgovOpenSearchService {
	
	@Value("${opensearch.text.indexname}")
    private String textIndexName;
    
    @Value("${opensearch.embedding.indexname}")
    private String embeddingIndexName;
    
    private static final String modelPath = Paths.get(System.getProperty("user.dir"))
            .resolve("model")
            .resolve("model.onnx")
            .toString();
            
    private static final String tokenizerPath = Paths.get(System.getProperty("user.dir"))
            .resolve("model")
            .resolve("tokenizer.json")
            .toString();
            
    private final OpenSearchClient client;
    
    public void processOpenSearchOperations(Long nttId, BoardVO boardVO) {
        try {
            performOpenSearchTextOperation(nttId, boardVO);
            performOpenSearchEmbeddingOperation(nttId, boardVO);
        } catch (Exception e) {
            log.error("Error processing OpenSearch operations for nttId: {}", nttId, e);
            throw new RuntimeException("Failed to process OpenSearch operations", e);
        }
    }
    
    private void performOpenSearchTextOperation(Long nttId, BoardVO boardVO) throws IOException {
        try {
            GetRequest getRequest = new GetRequest.Builder()
                    .index(textIndexName)
                    .id(String.valueOf(nttId))
                    .build();
                    
            GetResponse<BoardVO> getResponse = client.get(getRequest, BoardVO.class);
            
            if (getResponse.found()) {
                // Update existing document
                UpdateRequest<BoardVO, BoardVO> updateRequest = new UpdateRequest.Builder<BoardVO, BoardVO>()
                        .index(textIndexName)
                        .id(String.valueOf(nttId))
                        .doc(boardVO)
                        .build();
                        
                client.update(updateRequest, BoardVO.class);
                log.info("Updated document in text index for nttId: {}", nttId);
            } else {
                // Insert new document
                IndexRequest<BoardVO> indexRequest = new IndexRequest.Builder<BoardVO>()
                        .index(textIndexName)
                        .id(String.valueOf(nttId))
                        .document(boardVO)
                        .build();
                        
                client.index(indexRequest);
                log.info("Inserted new document in text index for nttId: {}", nttId);
            }
        } catch (Exception e) {
            log.error("Error in text operation for nttId: {}", nttId, e);
            throw e;
        }
    }
    
    private void performOpenSearchEmbeddingOperation(Long nttId, BoardVO boardVO) throws IOException {
        try {
            BoardEmbeddingVO embeddingVO = addEmbedding(boardVO);
            
            GetRequest getRequest = new GetRequest.Builder()
                    .index(embeddingIndexName)
                    .id(String.valueOf(nttId))
                    .build();
                    
            GetResponse<BoardEmbeddingVO> getResponse = client.get(getRequest, BoardEmbeddingVO.class);
            
            if (getResponse.found()) {
                // Update existing document
                UpdateRequest<BoardEmbeddingVO, BoardEmbeddingVO> updateRequest = new UpdateRequest.Builder<BoardEmbeddingVO, BoardEmbeddingVO>()
                        .index(embeddingIndexName)
                        .id(String.valueOf(nttId))
                        .doc(embeddingVO)
                        .build();
                        
                client.update(updateRequest, BoardEmbeddingVO.class);
                log.info("Updated document in embedding index for nttId: {}", nttId);
            } else {
                // Insert new document
                IndexRequest<BoardEmbeddingVO> indexRequest = new IndexRequest.Builder<BoardEmbeddingVO>()
                        .index(embeddingIndexName)
                        .id(String.valueOf(nttId))
                        .document(embeddingVO)
                        .build();
                        
                client.index(indexRequest);
                log.info("Inserted new document in embedding index for nttId: {}", nttId);
            }
        } catch (Exception e) {
            log.error("Error in embedding operation for nttId: {}", nttId, e);
            throw e;
        }
    }
    
    private BoardEmbeddingVO addEmbedding(BoardVO boardVO) {
        try {
            EmbeddingModel model = new OnnxEmbeddingModel(modelPath, tokenizerPath, PoolingMode.MEAN);
            String combinedText = boardVO.getNttSj() + " " + boardVO.getNttCn();
            
            Embedding articleResponse = model.embed(StrUtil.cleanString(combinedText)).content();
            float[] bbsArticleEmbedding = articleResponse.vector();
            
            BoardEmbeddingVO boardEmbeddingVO = new BoardEmbeddingVO();
            BeanUtils.copyProperties(boardVO, boardEmbeddingVO); // 기존 필드 복사
            boardEmbeddingVO.setBbsArticleEmbedding(bbsArticleEmbedding);
            
            return boardEmbeddingVO;
        } catch (Exception e) {
            log.error("Error creating embedding for nttId: {}", boardVO.getNttId(), e);
            throw new RuntimeException("Failed to create embedding", e);
        }
    }

}
