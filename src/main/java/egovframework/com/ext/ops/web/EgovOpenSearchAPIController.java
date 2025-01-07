package egovframework.com.ext.ops.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import egovframework.com.ext.ops.service.EgovOpenSearchManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@Tag(name="EgovOpenSearchAPIController",description = "Open Search CONTROLLER(인덱스 및 데이터 생성용)")
public class EgovOpenSearchAPIController {
	
	@Resource(name="openSearchManageService")
	private EgovOpenSearchManageService openSearchManageService;
	
	@Operation(
			summary = "텍스트 인덱스 생성",
			description = "mysql 테이블과 연동되는 OpenSearch(text) 인덱스를 생성",
			tags = {"EgovOpenSearchAPIController"}
	)
	@GetMapping("/createTextIndex")
	public ResponseEntity<?> createTextIndex() {
		Map<String, Object> response = new HashMap<>();

		try {
			log.debug("##### OpenSearch createIndex...");
			openSearchManageService.createTextIndex();

			response.put("status", "success");

			log.debug("##### OpenSearch create vecIndex Complete");
		} catch (IOException e) {
			response.put("status", "error");
			return ResponseEntity.ok(response);
		}

		return ResponseEntity.ok(response);
	}
	
	@Operation(
			summary = "임베딩 값이 포함된 인덱스 생성",
			description = "mysql 테이블과 연동되는 OpenSearch(embedding) 인덱스를 생성",
			tags = {"EgovOpenSearchAPIController"}
	)
	@GetMapping("/createEmbeddingIndex")
	public ResponseEntity<?> createEmbeddingIndex() {
		Map<String, Object> response = new HashMap<>();
		
		try {
			log.debug("##### OpenSearch createIndex...");
			openSearchManageService.createEmbeddingIndex();

			response.put("status", "success");

			log.debug("##### OpenSearch create vecIndex Complete");
		} catch (IOException e) {
			response.put("status", "error");
			return ResponseEntity.ok(response);
		}

		return ResponseEntity.ok(response);
	}
	
	@Operation(
			summary = "데이터 추가",
			description = "OpenSearch 인덱스(text)에 mySql 테이블의 데이터를 추가(벌크 insert)",
			tags = {"EgovOpenSearchAPIController"}
	)
	@GetMapping("/insertTextData")
	public ResponseEntity<?> insertTextData() {
		Map<String, Object> response = new HashMap<>();

		try {
			openSearchManageService.insertTotalData();
			response.put("status", "success");
		} catch (Exception e) {
			response.put("status", "error");
			return ResponseEntity.ok(response);
		}
		
		return ResponseEntity.ok(response);
	}
	
	@Operation(
			summary = "임베딩 값이 포함된 데이터를 전부 추가",
			description = "OpenSearch 인덱스(embedding)에 mySql 테이블의 데이터를 추가(분할 insert)",
			tags = {"EgovOpenSearchAPIController"}
	)
	@GetMapping("/insertEmbeddingData")
	public ResponseEntity<?> insertEmbeddingData() {
		Map<String, Object> response = new HashMap<>();

		try {
			openSearchManageService.insertTotalEmbeddingData();
			response.put("status", "success");
		} catch (Exception e) {
			response.put("status", "error");
			return ResponseEntity.ok(response);
		}
		
		return ResponseEntity.ok(response);
	}
	
	@Operation(
			summary = "인덱스 삭제",
			description = "OpenSearch 인덱스(embedding) 삭제",
			tags = {"EgovOpenSearchAPIController"}
	)
	@GetMapping("/deleteIndex/{indexName}")
	public ResponseEntity<?> deleteIndex(@PathVariable String indexName) {
		Map<String, Object> response = new HashMap<>();
		try {
			openSearchManageService.deleteIndex(indexName);
			response.put("status", "success");
		} catch (Exception e) {
			response.put("status", "error");
			return ResponseEntity.ok(response);
		}
		
		return ResponseEntity.ok(response);
	}

}
