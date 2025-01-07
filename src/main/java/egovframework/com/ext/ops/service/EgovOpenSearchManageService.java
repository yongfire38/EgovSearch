package egovframework.com.ext.ops.service;

import java.io.IOException;

public interface EgovOpenSearchManageService {
	
	public void createTextIndex() throws IOException;

	public void createEmbeddingIndex() throws IOException;
	
	public void insertTotalData();
	
	public void insertTotalEmbeddingData();
	
	public void deleteIndex(String indexName) throws IOException;
	
}
