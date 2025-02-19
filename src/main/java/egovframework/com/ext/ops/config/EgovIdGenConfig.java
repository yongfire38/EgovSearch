package egovframework.com.ext.ops.config;

import javax.sql.DataSource;

import org.egovframe.rte.fdl.idgnr.impl.EgovTableIdGnrServiceImpl;
import org.egovframe.rte.fdl.idgnr.impl.strategy.EgovIdGnrStrategyImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EgovIdGenConfig {
	
	@Bean
	public EgovIdGnrStrategyImpl mixPrefixSample() {
		EgovIdGnrStrategyImpl egovIdGnrStrategyImpl = new EgovIdGnrStrategyImpl();
		egovIdGnrStrategyImpl.setPrefix("SYNC_");
		egovIdGnrStrategyImpl.setCipers(15);
		egovIdGnrStrategyImpl.setFillChar('0');
		return egovIdGnrStrategyImpl;
	}

	@Bean(destroyMethod="destroy")
	public EgovTableIdGnrServiceImpl egovIdGnrService(@Qualifier("dataSource") DataSource dataSource) {
		EgovTableIdGnrServiceImpl egovTableIdGnrServiceImpl = new EgovTableIdGnrServiceImpl();
		egovTableIdGnrServiceImpl.setDataSource(dataSource);
		egovTableIdGnrServiceImpl.setStrategy(mixPrefixSample());
		egovTableIdGnrServiceImpl.setBlockSize(1);
		egovTableIdGnrServiceImpl.setTable("COMTECOPSEQ");
		egovTableIdGnrServiceImpl.setTableName("SYNC_ID");
		return egovTableIdGnrServiceImpl;	
	}
	
	
}