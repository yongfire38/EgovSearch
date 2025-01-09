package egovframework.com.ext.ops.service;

import egovframework.com.ext.ops.entity.Comtnbbsmanage;
import egovframework.com.ext.ops.event.BoardEvent;
import egovframework.com.ext.ops.repository.ComtnbbsRepository;
import egovframework.com.ext.ops.repository.ComtnbbsmanageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardEventListener {
	private final ComtnbbsmanageRepository comtnbbsmanageRepository;
    private final ComtnbbsRepository comtnbbsRepository;
    private final EgovIdGnrService idgenServiceManager;
    private final EgovOpenSearchService egovOpenSearchService;
    
    @RabbitListener(queues = "${spring.rabbitmq.queue-name}")
    @Transactional
    public void handleBoardEvent(BoardEvent event) {
        log.info("Received board event: {}", event);
        
        Comtnbbsmanage bbsManage = new Comtnbbsmanage();
        try {
            String syncId = idgenServiceManager.getNextStringId();
            
            bbsManage.setSyncId(syncId);
            bbsManage.setNttId(event.getNttId());
            bbsManage.setBbsId(event.getBbsId());
            bbsManage.setSyncSttusCode("N"); // 신규
            bbsManage.setRegistPnttm(event.getEventDateTime());
            
            // COMTNBBSMANAGE에 저장
            comtnbbsmanageRepository.save(bbsManage);
            
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
            
            // 성공 시 상태 업데이트
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
            Date localDate = Date.from(now.toInstant());
            
            bbsManage.setSyncSttusCode("Y");
            bbsManage.setSyncPnttm(localDate);
            comtnbbsmanageRepository.save(bbsManage);
            
            log.info("Successfully processed board event: {}", event);
            
        } catch (Exception e) {
        	log.error("Error processing board event: {}", event, e);
            
            // 에러 발생 시 상태를 'E'로 설정
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
            Date localDate = Date.from(now.toInstant());
            
            bbsManage.setSyncSttusCode("E");
            bbsManage.setErrorPnttm(localDate);
            try {
                comtnbbsmanageRepository.save(bbsManage);
                log.info("Error status saved for event: {}", event);
            } catch (Exception ex) {
                log.error("Failed to save error status: {}", ex.getMessage());
            }
            throw new AmqpRejectAndDontRequeueException("Failed to process board event", e);
        }
    }
}
