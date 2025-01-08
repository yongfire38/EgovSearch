package egovframework.com.ext.ops.service;

import java.util.concurrent.CompletableFuture;

public interface AsyncService {
	public CompletableFuture<Void> performAsyncSync();
}
