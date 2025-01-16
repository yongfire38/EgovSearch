package egovframework.com.ext.ops.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.function.Consumer;

import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.com.ext.ops.entity.Comtnbbssynclog;
import egovframework.com.ext.ops.event.BoardEvent;
import egovframework.com.ext.ops.repository.ComtnbbsRepository;
import egovframework.com.ext.ops.repository.ComtnbbssynclogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardEventListener {
	private final ComtnbbssynclogRepository comtnbbssynclogRepository;
    private final ComtnbbsRepository comtnbbsRepository;
    private final EgovIdGnrService idgenServiceManager;
    private final EgovOpenSearchService egovOpenSearchService;
    
    @Bean
    public Consumer<BoardEvent> basicConsumer() {
        return this::handleBoardEvent;
    }
    
    @Transactional
    public void handleBoardEvent(BoardEvent event) {
    	log.info("Received board event: {}", event);
    	
    	Comtnbbssynclog bbsManage = new Comtnbbssynclog();
        String syncId;
        
        try {
        	
            syncId = idgenServiceManager.getNextStringId();
            
            bbsManage.setSyncId(syncId);
            bbsManage.setNttId(event.getNttId());
            bbsManage.setBbsId(event.getBbsId());
            bbsManage.setSyncSttusCode("N"); // 신규
            bbsManage.setRegistPnttm(event.getEventDateTime());
            
            // COMTNBBSSYNCLOG에 저장
            comtnbbssynclogRepository.save(bbsManage);
            
            // BoardVO 조회
            BoardVO boardVO = comtnbbsRepository.findBBSDTOByNttId(event.getNttId())
                .map(dto -> {
                    BoardVO vo = new BoardVO();
                    BeanUtils.copyProperties(dto, vo);
                    return vo;
                })
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + event.getNttId()));
            
            // OpenSearch 처리
            egovOpenSearchService.processOpenSearchOperations(event.getNttId(), boardVO);
            
            // 상태 업데이트
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
            Date localDate = Date.from(now.toInstant());
            
            bbsManage.setSyncSttusCode("Y");
            bbsManage.setSyncPnttm(localDate);
            comtnbbssynclogRepository.save(bbsManage);
            
            log.info("Successfully processed board event: {}", event);
            
        } catch (Exception e) {
        	log.error("Error processing board event: {}", event, e);
            
            // 에러 발생 시 상태를 'E'로 설정
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
            Date localDate = Date.from(now.toInstant());
            
            if (bbsManage.getSyncId() != null) {
            	bbsManage.setSyncSttusCode("E");
                bbsManage.setErrorPnttm(localDate);
                comtnbbssynclogRepository.save(bbsManage);
            }
            
            throw new AmqpRejectAndDontRequeueException("Failed to process board event", e);
        }
    }
}
