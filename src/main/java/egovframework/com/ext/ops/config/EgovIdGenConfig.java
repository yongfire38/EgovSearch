package egovframework.com.ext.ops.config;

import javax.sql.DataSource;

import org.egovframe.rte.fdl.idgnr.impl.EgovTableIdGnrServiceImpl;
import org.egovframe.rte.fdl.idgnr.impl.strategy.EgovIdGnrStrategyImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import egovframework.com.ext.ops.util.EgovIdGnrBuilder;

@Configuration
public class EgovIdGenConfig {
	
	@Autowired
	DataSource dataSource;
	
	@Bean(destroyMethod = "destroy")
	public EgovTableIdGnrServiceImpl egovSyncIdGnrService() {
		return new EgovIdGnrBuilder().setDataSource(dataSource).setEgovIdGnrStrategyImpl(new EgovIdGnrStrategyImpl())
				.setBlockSize(1).setTable("COMTECOPSEQ").setTableName("SYNC_ID").setPreFix("SYNC_").setCipers(15)
				.setFillChar('0').build();
	}

}