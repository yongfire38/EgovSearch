package egovframework.com.ext.ops.scheduler;

import java.util.concurrent.CompletableFuture;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.stereotype.Component;

import egovframework.com.ext.ops.service.AsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
@DisallowConcurrentExecution  // 중복 실행 방지
@PersistJobDataAfterExecution
public class SyncJob implements Job {

	private final AsyncService asyncService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
    	try {
            CompletableFuture<Void> future = asyncService.performAsyncSync();
            
            // 필요한 경우 future.get()을 사용하여 작업 완료를 기다릴 수 있습니다.
            // 하지만 이렇게 하면 비동기의 이점이 줄어들 수 있으므로 주의해야 합니다.
            // future.get();
            
            log.debug("비동기 동기화 작업이 시작되었습니다.");
        } catch (Exception e) {
            log.error("비동기 동기화 작업 실행 중 오류 발생: {}", e.getMessage());
            throw new JobExecutionException(e);
        }
    }
}
