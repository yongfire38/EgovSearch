package egovframework.com.ext.ops.service;

import java.util.Calendar;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

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
    private final EgovOpenSearchService egovOpenSearchService;
    
    @Bean
    public Consumer<BoardEvent> searchConsumer() {
        return this::handleBoardEvent;
    }
    
    public void handleBoardEvent(BoardEvent event) {
    	try {
    		// Event의 nttId로 COMTNBBSSYNCLOG의 가장 최근 데이터가 Pending 상태인지 확인
            Optional<Comtnbbssynclog> syncLogOpt = comtnbbssynclogRepository.findTopByNttIdOrderByRegistPnttmDesc(event.getNttId());
            if (syncLogOpt.isEmpty() || !"P".equals(syncLogOpt.get().getSyncSttusCode())) {
                return;
            }
    		
            Comtnbbssynclog syncLog = syncLogOpt.get();
    		
    		// 게시글 데이터 조회
            Optional<BoardVO> boardVOOptional = comtnbbsRepository.findBBSDTOByNttId(event.getNttId())
                    .map(dto -> {
                        BoardVO vo = new BoardVO();
                        BeanUtils.copyProperties(dto, vo);
                        return vo;
                    });
    		
    		if (boardVOOptional.isPresent()) {
    			// OpenSearch 처리
                egovOpenSearchService.processOpenSearchOperations(event.getNttId(), boardVOOptional.get());
                
                // 성공 시 상태 업데이트
                syncLog.setSyncSttusCode("C");  // Completed
                
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.MILLISECOND, 0);
                syncLog.setSyncPnttm(calendar.getTime());
                
                comtnbbssynclogRepository.save(syncLog);
            } else {
            	 // 게시글을 찾을 수 없는 경우
                syncLog.setSyncSttusCode("F");  // Failed
                
                // 밀리초를 제거한 현재 시간 설정
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.MILLISECOND, 0);
                syncLog.setErrorPnttm(calendar.getTime());
                
                comtnbbssynclogRepository.save(syncLog);
                log.error("Board not found with id: " + event.getNttId());
            }	
    		
    	} catch (Exception e) {
    		log.error("Failed to process board event: " + event.getNttId(), e);
    		comtnbbssynclogRepository.findTopByNttIdOrderByRegistPnttmDesc(event.getNttId())
                .ifPresent(syncLog -> {
                    syncLog.setSyncSttusCode("F");  // Failed
                    
                    // 밀리초를 제거한 현재 시간 설정
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.MILLISECOND, 0);
                    syncLog.setErrorPnttm(calendar.getTime());
                    
                    comtnbbssynclogRepository.save(syncLog);
                });
    	}
    }
}
