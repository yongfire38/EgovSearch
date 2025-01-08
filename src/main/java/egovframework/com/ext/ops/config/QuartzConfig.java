package egovframework.com.ext.ops.config;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import egovframework.com.ext.ops.scheduler.SyncJob;

@Configuration
public class QuartzConfig {

	@Bean
	public JobDetail syncJobDetail() {
		return JobBuilder.newJob(SyncJob.class)
                .withIdentity("syncJob")
                .storeDurably()
                .build();
	}
	
	@Bean
    public Trigger syncJobTrigger() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMinutes(1)  // 1분마다 실행
                .repeatForever();

        return TriggerBuilder.newTrigger()
                .forJob(syncJobDetail())
                .withIdentity("syncJobTrigger")
                .withSchedule(scheduleBuilder)
                .build();
    }
}
