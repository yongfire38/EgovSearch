package egovframework.com.ext.ops.service;

import java.io.IOException;

public interface EgovOpenSearchManageService {
	
	public void createTextIndex() throws IOException;

	public void createVectorIndex() throws IOException;
	
	public void insertTotalData();
	
	public void insertTotalVectorData();
	
	public void deleteIndex(String indexName) throws IOException;
	
	public void reprocessFailedSync(String syncSttusCode);
	
}
