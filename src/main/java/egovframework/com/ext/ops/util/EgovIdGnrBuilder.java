package egovframework.com.ext.ops.util;

import javax.sql.DataSource;

import org.egovframe.rte.fdl.idgnr.impl.EgovTableIdGnrServiceImpl;
import org.egovframe.rte.fdl.idgnr.impl.strategy.EgovIdGnrStrategyImpl;

public class EgovIdGnrBuilder {

	private DataSource dataSource;
	private EgovIdGnrStrategyImpl egovIdGnrStrategyImpl;

	private String preFix;
	private int cipers;
	private char fillChar;

	private int blockSize;
	private String table;
	private String tableName;

	public EgovIdGnrBuilder setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		return this;
	}

	public EgovIdGnrBuilder setEgovIdGnrStrategyImpl(EgovIdGnrStrategyImpl egovIdGnrStrategyImpl) {
		this.egovIdGnrStrategyImpl = egovIdGnrStrategyImpl;
		return this;
	}

	public EgovIdGnrBuilder setPreFix(String preFix) {
		this.preFix = preFix;
		return this;
	}
	public EgovIdGnrBuilder setCipers(int cipers) {
		this.cipers = cipers;
		return this;
	}
	public EgovIdGnrBuilder setFillChar(char fillChar) {
		this.fillChar = fillChar;
		return this;
	}
	public EgovIdGnrBuilder setBlockSize(int blockSize) {
		this.blockSize = blockSize;
		return this;
	}
	public EgovIdGnrBuilder setTable(String table) {
		this.table = table;
		return this;
	}
	public EgovIdGnrBuilder setTableName(String tableName) {
		this.tableName = tableName;
		return this;
	}

	public EgovTableIdGnrServiceImpl build() {

		EgovTableIdGnrServiceImpl egovTableIdGnrServiceImpl = new EgovTableIdGnrServiceImpl();
		egovTableIdGnrServiceImpl.setDataSource(dataSource);
		if(egovIdGnrStrategyImpl != null) {
			egovIdGnrStrategyImpl = new EgovIdGnrStrategyImpl();
			egovIdGnrStrategyImpl.setPrefix(preFix);
			egovIdGnrStrategyImpl.setCipers(cipers);
			egovIdGnrStrategyImpl.setFillChar(fillChar);

			egovTableIdGnrServiceImpl.setStrategy(egovIdGnrStrategyImpl);
		}
		egovTableIdGnrServiceImpl.setBlockSize(blockSize);
		egovTableIdGnrServiceImpl.setTable(table);
		egovTableIdGnrServiceImpl.setTableName(tableName);

		return egovTableIdGnrServiceImpl;
	}

}
