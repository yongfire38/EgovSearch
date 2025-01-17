package egovframework.com.ext.ops.service.impl;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.analysis.Analyzer;
import org.opensearch.client.opensearch._types.analysis.AsciiFoldingTokenFilter;
import org.opensearch.client.opensearch._types.analysis.CharFilter;
import org.opensearch.client.opensearch._types.analysis.CustomAnalyzer;
import org.opensearch.client.opensearch._types.analysis.LowercaseTokenFilter;
import org.opensearch.client.opensearch._types.analysis.NoriDecompoundMode;
import org.opensearch.client.opensearch._types.analysis.NoriPartOfSpeechTokenFilter;
import org.opensearch.client.opensearch._types.analysis.NoriTokenizer;
import org.opensearch.client.opensearch._types.analysis.PatternReplaceCharFilter;
import org.opensearch.client.opensearch._types.analysis.SynonymGraphTokenFilter;
import org.opensearch.client.opensearch._types.analysis.TokenFilter;
import org.opensearch.client.opensearch._types.analysis.Tokenizer;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.CreateIndexResponse;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel;
import dev.langchain4j.model.embedding.onnx.PoolingMode;
import egovframework.com.ext.ops.repository.ComtnbbsRepository;
import egovframework.com.ext.ops.service.BBSDTO;
import egovframework.com.ext.ops.service.EgovOpenSearchManageService;
import egovframework.com.ext.ops.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service("openSearchManageService")
@Slf4j
@RequiredArgsConstructor
public class EgovOpenSearchManageServiceImpl extends EgovAbstractServiceImpl implements EgovOpenSearchManageService {
	
	public static final String stopTagsPath = Paths.get(System.getProperty("user.dir")).resolve("example").resolve("stoptags.txt").toString();
	public static final String synonymsPath = Paths.get(System.getProperty("user.dir")).resolve("example").resolve("synonyms.txt").toString();
	public static final String dictionaryRulesPath = Paths.get(System.getProperty("user.dir")).resolve("example").resolve("dictionaryRules.txt").toString();
	
	public static final String modelPath = Paths.get(System.getProperty("user.dir")).resolve("model").resolve("model.onnx").toString();
	public static final String tokenizerPath = Paths.get(System.getProperty("user.dir")).resolve("model").resolve("tokenizer.json").toString();
	
	@Value("${opensearch.text.indexname}")
    public String textIndexName;
	
	@Value("${opensearch.embedding.indexname}")
    public String embeddingIndexName;
	
	@Value("${index.batch.size}")
    public int batchSize;
	
	private EmbeddingModel embeddingModel;

    @PostConstruct
    public void init() {
        embeddingModel = new OnnxEmbeddingModel(modelPath, tokenizerPath, PoolingMode.MEAN);
    }
	
	private final OpenSearchClient client;
	
	private final ComtnbbsRepository comtnbbsRepository;
	
	private Map<String, CharFilter> createCharFilters() {
		Map<String, CharFilter> charFilterMap = new HashMap<>();
		
		// 줄바꿈 및 \를 공백으로 대체
		PatternReplaceCharFilter patternCharFilter = new PatternReplaceCharFilter.Builder().pattern("[\\r\\n\\\\]").replacement(" ").flags("CASE_INSENSITIVE|MULTILINE").build();
		charFilterMap.put("patternfilter", new CharFilter.Builder().definition(patternCharFilter._toCharFilterDefinition()).build());
		
		// remove punctuation chars : 구두점을 제거한다
		PatternReplaceCharFilter punctuationCharFilter = new PatternReplaceCharFilter.Builder().pattern("\\p{Punct}").replacement("").flags("CASE_INSENSITIVE|MULTILINE").build();
		charFilterMap.put("punctuationCharFilter", new CharFilter.Builder().definition(punctuationCharFilter._toCharFilterDefinition()).build());
		
		 return charFilterMap;
	}
	
	
	private Map<String, TokenFilter> createTokenFilters() {
		Map<String, TokenFilter> tokenFilterMap = new HashMap<>();
		
		// 제거할 품사의 종류를 열거한다. 코드가 의미하는 품사는 다음 페이지를 참조한다
		// https://esbook.kimjmin.net/06-text-analysis/6.7-stemming/6.7.2-nori
		List<String> stopTags = StrUtil.readWordsFromFile(stopTagsPath);
		
		// 동의어로 처리될 단어를 열거한다.
		List<String> synonym = StrUtil.readWordsFromFile(synonymsPath);
		
		tokenFilterMap.put("lowercase", new TokenFilter.Builder().definition(new LowercaseTokenFilter.Builder().build()._toTokenFilterDefinition()).build());
		tokenFilterMap.put("asciifolding", new TokenFilter.Builder().definition(new AsciiFoldingTokenFilter.Builder().preserveOriginal(false).build()._toTokenFilterDefinition()).build());
		tokenFilterMap.put("nori_part_of_speech", new TokenFilter.Builder().definition(new NoriPartOfSpeechTokenFilter.Builder().stoptags(stopTags).build()._toTokenFilterDefinition()).build());
		tokenFilterMap.put("synonym_graph", new TokenFilter.Builder().definition(new SynonymGraphTokenFilter.Builder().synonyms(synonym).expand(true).build()._toTokenFilterDefinition()).build());
	
		return tokenFilterMap;
	}
	
	private Map<String, Tokenizer> createTokenizers() {
		// 분할처리하면 안되는 단어를 열거한다.
		List<String> userDictionaryRules = StrUtil.readWordsFromFile(dictionaryRulesPath);
		
		// 한글형태소분석기인 Nori 플러그인이 미리 설치되어 있어야 함
		NoriTokenizer noriTokenizer = new NoriTokenizer.Builder()
				.decompoundMode(NoriDecompoundMode.Discard)
				.discardPunctuation(true)
				.userDictionaryRules(userDictionaryRules)
				.build();
		
		Map<String, Tokenizer> tokenizerMap = new HashMap<>();
		
		tokenizerMap.put("nori-tokenizer", new Tokenizer.Builder().definition(noriTokenizer._toTokenizerDefinition()).build());
		
		return tokenizerMap;
	}
	
	private Map<String, Analyzer> createAnalyzers(List<String> charFilterList, List<String> tokenFilterList) {
		// 커스텀 Analyzer 구성 : char_filter ==> tokenizer ==> token filter
		CustomAnalyzer noriAnalyzer = new CustomAnalyzer.Builder()
				.charFilter(charFilterList)
				.tokenizer("nori-tokenizer")
				.filter(tokenFilterList).build();
		
		Map<String, Analyzer> analyzerMap = new HashMap<>();
		
		analyzerMap.put("nori-analyzer", new Analyzer.Builder().custom(noriAnalyzer).build());
		
		return analyzerMap;
	}
	
	private void createIndexInternal(String indexName, boolean enableKnn) throws IOException {
		Map<String, CharFilter> charFilterMap = createCharFilters();
		Map<String, TokenFilter> tokenFilterMap = createTokenFilters();
		Map<String, Tokenizer> tokenizerMap = createTokenizers();
		
		List<String> charFilterList = Arrays.asList("patternfilter", "punctuationCharFilter");
		// nori_number : 한국어 숫자의 검색을 가능하게 함
		// nori_part_of_speech : 한자의 한국어 검색을 가능하게 함
		List<String> tokenFilterList = Arrays.asList(
	            "lowercase", "asciifolding", "synonym_graph",
	            "nori_number", "nori_readingform", "nori_part_of_speech"
	        );
		
		Map<String, Analyzer> analyzerMap = createAnalyzers(charFilterList, tokenFilterList);
		
		CreateIndexRequest.Builder requestBuilder = new CreateIndexRequest.Builder()
				.index(indexName)
				.settings(s-> {
					if (enableKnn) {
	                    s.knn(true);
	                }
					return s.analysis(a-> a
							.charFilter(charFilterMap)
		                    .tokenizer(tokenizerMap)
		                    .filter(tokenFilterMap)
		                    .analyzer(analyzerMap));
				});
		
		// 각 필드를 추가
		addMappings(requestBuilder, enableKnn);
		
		try {
			CreateIndexResponse createIndexResponse = client.indices().create(requestBuilder.build());
            log.debug(String.format("Index %s.", createIndexResponse.index().toString().toLowerCase()));
		} catch(OpenSearchException ex) {
			final String errorType = Objects.requireNonNull(ex.response().error().type());
            if (! errorType.equals("resource_already_exists_exception")) {
                throw ex;
            }
		}
	}
	
	private void addMappings(CreateIndexRequest.Builder builder, boolean includeEmbedding) {
	    builder.mappings(mapping -> {
	        TypeMapping.Builder mappingBuilder = mapping
	        		.properties("nttId", 
	                        p -> p.integer(f -> f.index(true)
	                            .fields("keyword", k -> k.keyword(kw -> kw.ignoreAbove(256)))))
	                    .properties("bbsId", 
	                        p -> p.text(f -> f.index(true)
	                            .fields("keyword", k -> k.keyword(kw -> kw.ignoreAbove(256)))))
	                    .properties("bbsNm", 
	                        p -> p.text(f -> f.index(true).analyzer("nori-analyzer")))
	                    .properties("nttNo", 
	                        p -> p.integer(f -> f.index(true)))
	                    .properties("nttSj", 
	                        p -> p.text(f -> f.index(true).analyzer("nori-analyzer")))
	                    .properties("nttCn", 
	                        p -> p.text(f -> f.index(true).analyzer("nori-analyzer")))
	                    .properties("answerAt", 
	                        p -> p.text(f -> f.index(true).analyzer("nori-analyzer")))
	                    .properties("parntscttNo", 
	                        p -> p.integer(f -> f.index(true)))
	                    .properties("answerLc", 
	                        p -> p.integer(f -> f.index(true)))
	                    .properties("sortOrdr", 
	                        p -> p.integer(f -> f.index(true)))
	                    .properties("useAt", 
	                        p -> p.text(f -> f.index(true).analyzer("nori-analyzer")))
	                    .properties("ntceBgnde", 
	                        p -> p.date(f -> f.index(true)))
	                    .properties("ntceEndde", 
	                        p -> p.date(f -> f.index(true)))
	                    .properties("ntcrId", 
	                        p -> p.text(f -> f.index(true).analyzer("nori-analyzer")))
	                    .properties("ntcrNm", 
	                        p -> p.text(f -> f.index(true).analyzer("nori-analyzer")))
	                    .properties("atchFileId", 
	                        p -> p.text(f -> f.index(true).analyzer("nori-analyzer")))
	                    .properties("noticeAt", 
	                        p -> p.text(f -> f.index(true).analyzer("nori-analyzer")))
	                    .properties("sjBoldAt", 
	                        p -> p.text(f -> f.index(true).analyzer("nori-analyzer")))
	                    .properties("secretAt", 
	                        p -> p.text(f -> f.index(true).analyzer("nori-analyzer")))
	                    .properties("frstRegistPnttm", 
	                        p -> p.date(f -> f.index(true)))
	                    .properties("lastUpdtPnttm", 
	                        p -> p.date(f -> f.index(true)))
	                    .properties("frstRegisterId", 
	                        p -> p.text(f -> f.index(true).analyzer("nori-analyzer")));

	        if (includeEmbedding) {
	            mappingBuilder = mappingBuilder.properties("bbsArticleEmbedding", 
	                p -> p.knnVector(k -> k.dimension(768)));
	        }
	        
	        return mappingBuilder;
	    });
	}
	
	@Override
	public void createTextIndex() throws IOException {
		 createIndexInternal(textIndexName, false);
		/*
		Map<String, Tokenizer> tokenizerMap = new HashMap<>();
		Map<String, Analyzer> analyzerMap = new HashMap<>();
		Map<String, TokenFilter> tokenFilterMap = new HashMap<>();
		Map<String, CharFilter> charFilterMap = new HashMap<>();
		
		// 줄바꿈 및 \를 공백으로 대체
		PatternReplaceCharFilter patternCharFilter = new PatternReplaceCharFilter.Builder().pattern("[\\r\\n\\\\]").replacement(" ").flags("CASE_INSENSITIVE|MULTILINE").build();
		CharFilter chrFilter =  new CharFilter.Builder().definition(patternCharFilter._toCharFilterDefinition()).build();
		charFilterMap.put("patternfilter", chrFilter);
		
		// remove punctuation chars : 구두점을 제거한다
		PatternReplaceCharFilter punctuationCharFilter = new PatternReplaceCharFilter.Builder().pattern("\\p{Punct}").replacement("").flags("CASE_INSENSITIVE|MULTILINE").build();
		CharFilter chrPatternFilter =  new CharFilter.Builder().definition(punctuationCharFilter._toCharFilterDefinition()).build();
		charFilterMap.put("punctuationCharFilter", chrPatternFilter);
				
		List<String> charFilterList = new ArrayList<>();
		charFilterList.add("patternfilter");
		charFilterList.add("punctuationCharFilter");
		
		// 제거할 품사의 종류를 열거한다. 코드가 의미하는 품사는 다음 페이지를 참조한다
		// https://esbook.kimjmin.net/06-text-analysis/6.7-stemming/6.7.2-nori
		List<String> stopTags = StrUtil.readWordsFromFile(stopTagsPath);
		
		// Token filter : 소문자 변환 / 비ASCII 문자를 ASCII 문자로 변환 / 한국어의 특정 품사를 제거
		LowercaseTokenFilter lowerFilter = new LowercaseTokenFilter.Builder().build();
		AsciiFoldingTokenFilter asciiFilter = new AsciiFoldingTokenFilter.Builder().preserveOriginal(false).build();
		NoriPartOfSpeechTokenFilter noriPartOfSpeechFilter = new NoriPartOfSpeechTokenFilter.Builder().stoptags(stopTags).build();        
		tokenFilterMap.put("lowercase", new TokenFilter.Builder().definition(lowerFilter._toTokenFilterDefinition()).build());
		tokenFilterMap.put("asciifolding", new TokenFilter.Builder().definition(asciiFilter._toTokenFilterDefinition()).build());
		tokenFilterMap.put("nori_part_of_speech", new TokenFilter.Builder().definition(noriPartOfSpeechFilter._toTokenFilterDefinition()).build());
		        
		// 동의어로 처리될 단어를 열거한다.
		List<String> synonym = StrUtil.readWordsFromFile(synonymsPath);
		
		SynonymGraphTokenFilter synonymFilter = new SynonymGraphTokenFilter.Builder().synonyms(synonym).expand(true).build();
		tokenFilterMap.put("synonym_graph", new TokenFilter.Builder().definition(synonymFilter._toTokenFilterDefinition()).build());
		
		List<String> tokenFilterList = new ArrayList<>();
			
		tokenFilterList.add("lowercase");
		tokenFilterList.add("asciifolding");
		tokenFilterList.add("synonym_graph");
		tokenFilterList.add("nori_number"); // 한국어 숫자의 검색을 가능하게 함
		tokenFilterList.add("nori_readingform"); // 한자의 한국어 검색을 가능하게 함
		tokenFilterList.add("nori_part_of_speech");
			
		// 분할처리하면 안되는 단어를 열거한다.
		List<String> userDictionaryRules = StrUtil.readWordsFromFile(dictionaryRulesPath);
		
		// 한글형태소분석기인 Nori 플러그인이 미리 설치되어 있어야 함
		NoriTokenizer noriTokenizer = new NoriTokenizer.Builder()
				.decompoundMode(NoriDecompoundMode.Discard)
				.discardPunctuation(true)
				.userDictionaryRules(userDictionaryRules)
				.build();
		
		Tokenizer tokenizer = new Tokenizer.Builder().definition(noriTokenizer._toTokenizerDefinition()).build();
		tokenizerMap.put("nori-tokenizer", tokenizer);
				
		// 커스텀 Analyzer 구성 : char_filter ==> tokenizer ==> token filter
		CustomAnalyzer noriAnalyzer = new CustomAnalyzer.Builder()
				.charFilter(charFilterList)
				.tokenizer("nori-tokenizer")
				.filter(tokenFilterList).build();
		
		Analyzer analyzer = new Analyzer.Builder().custom(noriAnalyzer).build();
		analyzerMap.put("nori-analyzer", analyzer);
		
		CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder()
			    .index(textIndexName)
			    .settings(s -> s	
			        .analysis(a -> a
			        		.charFilter(charFilterMap)
			        		.tokenizer(tokenizerMap)
			        		.filter(tokenFilterMap)
			        		.analyzer(analyzerMap)
                    )                
			    )
			    .mappings(m -> m
					.properties("nttId", p -> p
						.integer(f -> f
								.index(true)
								.fields("keyword", k -> k
										.keyword(kw -> kw
												.ignoreAbove(256)
												)
										)
							)
						)
					.properties("bbsId", p -> p
							.text(f -> f.index(true)
									.fields("keyword", k -> k
											.keyword(kw -> kw
													.ignoreAbove(256)
													)
											)
									)
							)
					.properties("bbsNm", p -> p // 통합검색 시 편의를 위해 추가
				            .text(f -> f
				                .index(true)
				                .analyzer("nori-analyzer")
				            )
				        )
					.properties("nttNo", p -> p
							.integer(f -> f
									.index(true)
								)
							)
					.properties("nttSj", p -> p
				            .text(f -> f
				                .index(true)
				                .analyzer("nori-analyzer")
				            )
				        )
					.properties("nttCn", p -> p
				            .text(f -> f
				                .index(true)
				                .analyzer("nori-analyzer")
				            )
				        )
					.properties("answerAt", p -> p
				            .text(f -> f
				                .index(true)
				                .analyzer("nori-analyzer")
				            )
				        )
					.properties("parntscttNo", p -> p
							.integer(f -> f
									.index(true)
								)
							)
					.properties("answerLc", p -> p
							.integer(f -> f
									.index(true)
								)
							)
					.properties("sortOrdr", p -> p
							.integer(f -> f
									.index(true)
								)
							)
					.properties("useAt", p -> p
				            .text(f -> f
				                .index(true)
				                .analyzer("nori-analyzer")
				            )
				        )
					.properties("ntceBgnde", p -> p
			        		.date(f -> f
					                .index(true)
					                )
			        		)
					.properties("ntceEndde", p -> p
			        		.date(f -> f
					                .index(true)
					                )
			        		)
					.properties("ntcrId", p -> p
				            .text(f -> f
				                .index(true)
				                .analyzer("nori-analyzer")
				            )
				        )
					.properties("ntcrNm", p -> p
				            .text(f -> f
				                .index(true)
				                .analyzer("nori-analyzer")
				            )
				        )
					.properties("atchFileId", p -> p
				            .text(f -> f
				                .index(true)
				                .analyzer("nori-analyzer")
				            )
				        )
					.properties("noticeAt", p -> p
				            .text(f -> f
				                .index(true)
				                .analyzer("nori-analyzer")
				            )
				        )
					.properties("sjBoldAt", p -> p
				            .text(f -> f
				                .index(true)
				                .analyzer("nori-analyzer")
				            )
				        )
					.properties("secretAt", p -> p
				            .text(f -> f
				                .index(true)
				                .analyzer("nori-analyzer")
				            )
				        )
					.properties("frstRegistPnttm", p -> p
			        		.date(f -> f
					                .index(true)
					                )
			        		)
					.properties("lastUpdtPnttm", p -> p
			        		.date(f -> f
					                .index(true)
					                )
			        		)
					.properties("frstRegisterId", p -> p
				            .text(f -> f
				                .index(true)
				                .analyzer("nori-analyzer")
				            )
				        )
			    )
			    .build();
		
		try {
        	CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest);
            log.debug(String.format("Index %s.", createIndexResponse.index().toString().toLowerCase()));
        } catch (OpenSearchException ex) {
            final String errorType = Objects.requireNonNull(ex.response().error().type());
            if (! errorType.equals("resource_already_exists_exception")) {
                throw ex;
            }
        }
        */
	}

	@Override
	public void createEmbeddingIndex() throws IOException {
		createIndexInternal(embeddingIndexName, true);
		/*
		Map<String, Tokenizer> tokenizerMap = new HashMap<>();
		Map<String, Analyzer> analyzerMap = new HashMap<>();
		Map<String, TokenFilter> tokenFilterMap = new HashMap<>();
		Map<String, CharFilter> charFilterMap = new HashMap<>();
		
		// 줄바꿈 및 \를 공백으로 대체
		PatternReplaceCharFilter patternCharFilter = new PatternReplaceCharFilter.Builder().pattern("[\\r\\n\\\\]").replacement(" ").flags("CASE_INSENSITIVE|MULTILINE").build();
		CharFilter chrFilter =  new CharFilter.Builder().definition(patternCharFilter._toCharFilterDefinition()).build();
		charFilterMap.put("patternfilter", chrFilter);
		
		// remove punctuation chars : 구두점을 제거한다
		PatternReplaceCharFilter punctuationCharFilter = new PatternReplaceCharFilter.Builder().pattern("\\p{Punct}").replacement("").flags("CASE_INSENSITIVE|MULTILINE").build();
		CharFilter chrPatternFilter =  new CharFilter.Builder().definition(punctuationCharFilter._toCharFilterDefinition()).build();
		charFilterMap.put("punctuationCharFilter", chrPatternFilter);
				
		List<String> charFilterList = new ArrayList<>();
		charFilterList.add("patternfilter");
		charFilterList.add("punctuationCharFilter");
		
		// 제거할 품사의 종류를 열거한다. 코드가 의미하는 품사는 다음 페이지를 참조한다
		// https://esbook.kimjmin.net/06-text-analysis/6.7-stemming/6.7.2-nori
		List<String> stopTags = StrUtil.readWordsFromFile(stopTagsPath);
		
		// Token filter : 소문자 변환 / 비ASCII 문자를 ASCII 문자로 변환 / 한국어의 특정 품사를 제거
		LowercaseTokenFilter lowerFilter = new LowercaseTokenFilter.Builder().build();
        AsciiFoldingTokenFilter asciiFilter = new AsciiFoldingTokenFilter.Builder().preserveOriginal(false).build();
        NoriPartOfSpeechTokenFilter noriPartOfSpeechFilter = new NoriPartOfSpeechTokenFilter.Builder().stoptags(stopTags).build();        
        tokenFilterMap.put("lowercase", new TokenFilter.Builder().definition(lowerFilter._toTokenFilterDefinition()).build());
        tokenFilterMap.put("asciifolding", new TokenFilter.Builder().definition(asciiFilter._toTokenFilterDefinition()).build());
        tokenFilterMap.put("nori_part_of_speech", new TokenFilter.Builder().definition(noriPartOfSpeechFilter._toTokenFilterDefinition()).build());
        
        // 동의어로 처리될 단어를 열거한다.
        List<String> synonym = StrUtil.readWordsFromFile(synonymsPath);
        
        SynonymGraphTokenFilter synonymFilter = new SynonymGraphTokenFilter.Builder().synonyms(synonym).expand(true).build();
        tokenFilterMap.put("synonym_graph", new TokenFilter.Builder().definition(synonymFilter._toTokenFilterDefinition()).build());
        
        List<String> tokenFilterList = new ArrayList<>();
		
		tokenFilterList.add("lowercase");
		tokenFilterList.add("asciifolding");
		tokenFilterList.add("synonym_graph");
		tokenFilterList.add("nori_number"); // 한국어 숫자의 검색을 가능하게 함
		tokenFilterList.add("nori_readingform"); // 한자의 한국어 검색을 가능하게 함
		tokenFilterList.add("nori_part_of_speech");
		
		// 분할처리하면 안되는 단어를 열거한다.
		List<String> userDictionaryRules = StrUtil.readWordsFromFile(dictionaryRulesPath);
		
		// 한글형태소분석기인 Nori 플러그인이 미리 설치되어 있어야 함
		NoriTokenizer noriTokenizer = new NoriTokenizer.Builder()
				.decompoundMode(NoriDecompoundMode.Discard)
				.discardPunctuation(true)
				.userDictionaryRules(userDictionaryRules)
				.build();
		
		Tokenizer tokenizer = new Tokenizer.Builder().definition(noriTokenizer._toTokenizerDefinition()).build();
		tokenizerMap.put("nori-tokenizer", tokenizer);
	
		// 커스텀 Analyzer 구성 : char_filter ==> tokenizer ==> token filter
		CustomAnalyzer noriAnalyzer = new CustomAnalyzer.Builder()
				.charFilter(charFilterList)
				.tokenizer("nori-tokenizer")
				.filter(tokenFilterList).build();
		
		Analyzer analyzer = new Analyzer.Builder().custom(noriAnalyzer).build();
		analyzerMap.put("nori-analyzer", analyzer);
		
		CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder()
			    .index(embeddingIndexName)
			    .settings(s -> s	
			    	.knn(true)
			        .analysis(a -> a
			        		.charFilter(charFilterMap)
			        		.tokenizer(tokenizerMap)
			        		.filter(tokenFilterMap)
			        		.analyzer(analyzerMap)
                    )                
			    )
			    .mappings(m -> m
			    		.properties("nttId", p -> p
							.integer(f -> f
									.index(true)
									.fields("keyword", k -> k
											.keyword(kw -> kw
													.ignoreAbove(256)
													)
											)
								)
							)
						.properties("bbsId", p -> p
								.text(f -> f.index(true)
										.fields("keyword", k -> k
												.keyword(kw -> kw
														.ignoreAbove(256)
														)
												)
										)
								)
						.properties("bbsNm", p -> p // 통합검색 시 편의를 위해 추가
					            .text(f -> f
					                .index(true)
					                .analyzer("nori-analyzer")
					            )
					        )
						.properties("nttNo", p -> p
								.integer(f -> f
										.index(true)
									)
								)
						.properties("nttSj", p -> p
							        .text(f -> f
							            .index(true)
							            .analyzer("nori-analyzer")
							        )
							    )
						.properties("nttCn", p -> p
							        .text(f -> f
							            .index(true)
							            .analyzer("nori-analyzer")
							        )
							    )
						.properties("answerAt", p -> p
							        .text(f -> f
							            .index(true)
							            .analyzer("nori-analyzer")
							        )
							    )
						.properties("parntscttNo", p -> p
								.integer(f -> f
										.index(true)
									)
								)
						.properties("answerLc", p -> p
								.integer(f -> f
										.index(true)
									)
								)
						.properties("sortOrdr", p -> p
								.integer(f -> f
										.index(true)
									)
								)
						.properties("useAt", p -> p
							        .text(f -> f
							            .index(true)
							            .analyzer("nori-analyzer")
							        )
							    )
						.properties("ntceBgnde", p -> p
						    		.date(f -> f
						                .index(true)
						                )
						    		)
						.properties("ntceEndde", p -> p
						    		.date(f -> f
						                .index(true)
						                )
						    		)
						.properties("ntcrId", p -> p
							        .text(f -> f
							            .index(true)
							            .analyzer("nori-analyzer")
							        )
							    )
						.properties("ntcrNm", p -> p
							        .text(f -> f
							            .index(true)
							            .analyzer("nori-analyzer")
							        )
							    )
						.properties("atchFileId", p -> p
							        .text(f -> f
							            .index(true)
							            .analyzer("nori-analyzer")
							        )
							    )
						.properties("noticeAt", p -> p
							        .text(f -> f
							            .index(true)
							            .analyzer("nori-analyzer")
							        )
							    )
						.properties("sjBoldAt", p -> p
						        .text(f -> f
						            .index(true)
						            .analyzer("nori-analyzer")
						        )
						    )
						.properties("secretAt", p -> p
							        .text(f -> f
							            .index(true)
							            .analyzer("nori-analyzer")
							        )
							    )
						.properties("frstRegistPnttm", p -> p
						    		.date(f -> f
						                .index(true)
						                )
						    		)
						.properties("lastUpdtPnttm", p -> p
						    		.date(f -> f
						                .index(true)
						                )
						    		)
						.properties("frstRegisterId", p -> p
							        .text(f -> f
							            .index(true)
							            .analyzer("nori-analyzer")
							        )
							    )
						.properties("bbsArticleEmbedding", p -> p
					            .knnVector(k -> k
					                .dimension(768)
					            )
					        )
			    )
			    .build();
		try {
        	CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest);
            log.debug(String.format("Index %s.", createIndexResponse.index().toString().toLowerCase()));
        } catch (OpenSearchException ex) {
            final String errorType = Objects.requireNonNull(ex.response().error().type());
            if (! errorType.equals("resource_already_exists_exception")) {
                throw ex;
            }
        }
        */
	}

	@Override
	public void insertTotalData() {
		
		processIndexing(false, textIndexName);
		/*
		 * 
		long beforeTime = System.currentTimeMillis();
		
		// step 1. mySql 테이블의 모든 데이터를 조회한다
		// 통합 검색 시 편의를 위해, 게시판 마스터 테이블을 join 해서 게시판 명도 추가로 가져오도록 한다
		List<BBSDTO> bbsList = comtnbbsRepository.selectAllArticle();
				
		// step 2. 대량 인덱싱을 위해 데이터를 적절한 크기로 나눈다 (예: 1000개씩)
		int batchSize = 1000;
		int totalSize = bbsList.size();
		int startIndex = 0;
		
		while (startIndex < totalSize) {
	    	int endIndex = Math.min(startIndex + batchSize, totalSize);
	    	List<BBSDTO> batchList = bbsList.subList(startIndex, endIndex);
	    	
	    	// step 3. BulkRequest 빌더 생성
	        BulkRequest.Builder bulkRequestBuilder = new BulkRequest.Builder();
	        
	        batchList.forEach(bbsArticleInfo -> {
	            Map<String, Object> dataMap = new HashMap<>();
	            // 객체의 필드들을 정리하여 Map에 추가
	            dataMap.put("nttId", bbsArticleInfo.getNttId());
	            dataMap.put("bbsId", bbsArticleInfo.getBbsId());
	            dataMap.put("bbsNm", bbsArticleInfo.getBbsNm());
	            dataMap.put("nttNo", bbsArticleInfo.getNttNo());
	            dataMap.put("nttSj", StrUtil.cleanString(bbsArticleInfo.getNttSj()));
	            dataMap.put("nttCn", StrUtil.cleanString(bbsArticleInfo.getNttCn()));
	            dataMap.put("answerAt", bbsArticleInfo.getAnswerAt());
	            dataMap.put("parntscttNo", bbsArticleInfo.getParntscttNo());
	            dataMap.put("answerLc", bbsArticleInfo.getAnswerLc());
	            dataMap.put("sortOrdr", bbsArticleInfo.getSortOrdr());
	            dataMap.put("useAt", bbsArticleInfo.getUseAt());
	            dataMap.put("ntceBgnde", bbsArticleInfo.getNtceBgnde());
	            dataMap.put("ntceEndde", bbsArticleInfo.getNtceEndde());
	            dataMap.put("ntcrId", bbsArticleInfo.getNtcrId());
	            dataMap.put("ntcrNm", bbsArticleInfo.getNtcrNm());
	            dataMap.put("atchFileId", bbsArticleInfo.getAtchFileId());
	            dataMap.put("noticeAt", bbsArticleInfo.getNoticeAt());
	            dataMap.put("sjBoldAt", bbsArticleInfo.getSjBoldAt());
	            dataMap.put("secretAt", bbsArticleInfo.getSecretAt());
	            dataMap.put("frstRegistPnttm", bbsArticleInfo.getFrstRegistPnttm());

	            // BulkRequest에 데이터를 추가
	            bulkRequestBuilder.operations(ops -> 
	                ops.index(idx -> 
	                    idx.index(textIndexName)
	                       .id(String.valueOf(dataMap.get("nttId")))  // 문서 ID로 nttId 사용
	                       .document(dataMap)                        // 문서 내용
	                )
	            );
	        });
	        
	        // step 4. 전처리한 데이터를 인덱싱한다
	        try {
	            BulkResponse bulkResponse = client.bulk(bulkRequestBuilder.build());

	            // Bulk 요청 중 오류가 발생했는지 확인
	            if (bulkResponse.errors()) {
	                bulkResponse.items().forEach(item -> {
	                    if (item.error() != null) {
	                        log.error("Error indexing document with ID: " + item.id() + ", Error: " + item.error().reason());
	                    }
	                });
	            } else {
	                log.debug("Batch [" + startIndex + " - " + (endIndex - 1) + "] completed successfully");
	            }
	        } catch (Exception e) {
	            log.error("Error occurred during bulk indexing:", e);
	        }

	        // 다음 배치로 이동
	        startIndex = endIndex;   
	    }
	    
	    long afterTime = System.currentTimeMillis(); 
	    long secDiffTime = (afterTime - beforeTime) / 1000;

	    log.debug("총 소요 시간: " + secDiffTime + "초");
	    
	    */
	}

	@Override
	public void insertTotalEmbeddingData() {
		processIndexing(true, embeddingIndexName);
		/*
		
		EmbeddingModel embeddingModel = new OnnxEmbeddingModel(modelPath, tokenizerPath, PoolingMode.MEAN);
		
		int pageSize = indexSize; // 인덱싱 시, 분할 처리할 수
	    long totalCount = comtnbbsRepository.count(); // 전체 데이터 수 조회
	    int totalPages = (int) Math.ceil((double) totalCount / pageSize);

	    log.info("Total count: {}, Total pages: {}", totalCount, totalPages);
		
		// step 2. 1에서 구한 수만큼 순회하면서
		for (int i = 0; i < totalPages; i++) {
			long beforeTime = System.currentTimeMillis();
			
			BulkRequest.Builder bulkRequestBuilder = new BulkRequest.Builder();

			List<BBSDTO> splittedBbsArticleInfoList = comtnbbsRepository.selectAllArticle();
			
			// step 4. 얻어낸 데이터 전처리 (+임베딩을 수행, 임베딩은 제목과 내용을 합친 문자열을 임베딩한다)
			splittedBbsArticleInfoList.stream().map(bbsArticleInfo -> {
				Map<String, Object> dataMap = new HashMap<>();
				
				Embedding bbsArticleResponse = embeddingModel.embed(StrUtil.cleanString(bbsArticleInfo.getNttSj() + " " + bbsArticleInfo.getNttCn())).content();
				
				float[] bbsArticleEmbeddings =  bbsArticleResponse.vector();
				
				// 객체의 필드들을 정리하여 Map에 추가
				dataMap.put("nttId", bbsArticleInfo.getNttId());
	            dataMap.put("bbsId", bbsArticleInfo.getBbsId());
	            dataMap.put("bbsNm", bbsArticleInfo.getBbsNm());
	            dataMap.put("nttNo", bbsArticleInfo.getNttNo());
	            dataMap.put("nttSj", StrUtil.cleanString(bbsArticleInfo.getNttSj()));
	            dataMap.put("nttCn", StrUtil.cleanString(bbsArticleInfo.getNttCn()));
	            dataMap.put("answerAt", bbsArticleInfo.getAnswerAt());
	            dataMap.put("parntscttNo", bbsArticleInfo.getParntscttNo());
	            dataMap.put("answerLc", bbsArticleInfo.getAnswerLc());
	            dataMap.put("sortOrdr", bbsArticleInfo.getSortOrdr());
	            dataMap.put("useAt", bbsArticleInfo.getUseAt());
	            dataMap.put("ntceBgnde", bbsArticleInfo.getNtceBgnde());
	            dataMap.put("ntceEndde", bbsArticleInfo.getNtceEndde());
	            dataMap.put("ntcrId", bbsArticleInfo.getNtcrId());
	            dataMap.put("ntcrNm", bbsArticleInfo.getNtcrNm());
	            dataMap.put("atchFileId", bbsArticleInfo.getAtchFileId());
	            dataMap.put("noticeAt", bbsArticleInfo.getNoticeAt());
	            dataMap.put("sjBoldAt", bbsArticleInfo.getSjBoldAt());
	            dataMap.put("secretAt", bbsArticleInfo.getSecretAt());
	            dataMap.put("frstRegistPnttm", bbsArticleInfo.getFrstRegistPnttm());
				dataMap.put("bbsArticleEmbedding", bbsArticleEmbeddings);
				
				return dataMap;
			})
			.forEach(dataMap -> bulkRequestBuilder.operations(ops -> 
			ops.index(IndexOperation.of(io -> 
			io.index(embeddingIndexName).id(String.valueOf(dataMap.get("nttId"))).document(dataMap)))
			));
			
			// step 5. 임베딩 값 추가 및 전처리한 데이터를 인덱싱한다
			try {
			    BulkResponse bulkResponse = client.bulk(bulkRequestBuilder.build());
			    if (bulkResponse.errors()) {
			    	log.debug("Bulk operation had errors");
			    } else {
			    	log.debug("Bulk operation completed successfully");
			    	
			    	long afterTime = System.currentTimeMillis(); 
					long secDiffTime = (afterTime - beforeTime)/1000;
					
					log.debug("page : " + i + "완료");
					log.debug("시간차이(m) : " + secDiffTime + "초");
			    }
			} catch (Exception e) {
			    e.printStackTrace();
			}	
		}
		
		*/
	}
	
	@Override
	public void deleteIndex(String indexName) throws IOException {
		DeleteIndexRequest deleteRequest = new DeleteIndexRequest.Builder().index(indexName).build();
        client.indices().delete(deleteRequest);
        log.debug(String.format("Index %s.", deleteRequest.index().toString().toLowerCase()));
		
	}
	
	private void processIndexing(boolean withEmbedding, String indexName) {
        long startTime = System.currentTimeMillis();
        
        // 전체 데이터 수와 페이지 수 계산
        long totalCount = comtnbbsRepository.countAllArticles();
        int totalPages = (int) Math.ceil((double) totalCount / batchSize);
        
        log.info("Starting indexing process. Total records: {}, Batch size: {}, Total pages: {}", 
                totalCount, batchSize, totalPages);

        for (int page = 0; page < totalPages; page++) {
            long pageStartTime = System.currentTimeMillis();
            
            try {
                // 페이지별로 데이터 조회
                Pageable pageable = PageRequest.of(page, batchSize, Sort.by("comtnbbsId.nttId").descending());
                Page<BBSDTO> pageResult = comtnbbsRepository.findAllArticlesWithPaging(pageable);
                
                if (!pageResult.isEmpty()) {
                    processBatchRequest(pageResult.getContent(), withEmbedding, indexName, page, totalPages);
                }
                
                logPageProgress(page, totalPages, pageStartTime);
            } catch (Exception e) {
                log.error("Error processing page {}: {}", page, e.getMessage(), e);
            }
        }

        logTotalExecutionTime(startTime);
    }
	
	private void processBatchRequest(List<BBSDTO> batchData, boolean withEmbedding, String indexName, int currentPage,
			int totalPages) {
		BulkRequest.Builder bulkRequestBuilder = new BulkRequest.Builder();

		batchData.forEach(bbsArticleInfo -> {
			try {
				Map<String, Object> dataMap = convertToMap(bbsArticleInfo, withEmbedding);
				bulkRequestBuilder.operations(ops -> ops
						.index(idx -> idx.index(indexName).id(String.valueOf(dataMap.get("nttId"))).document(dataMap)));
			} catch (Exception e) {
				log.error("Error processing document {}: {}", bbsArticleInfo.getNttId(), e.getMessage());
			}
		});

		executeBulkRequest(bulkRequestBuilder, currentPage, totalPages);
	}
	
	private Map<String, Object> convertToMap(BBSDTO bbsArticleInfo, boolean withEmbedding) {
        Map<String, Object> dataMap = new HashMap<>();
        
        // 기본 필드 매핑
        dataMap.put("nttId", bbsArticleInfo.getNttId());
        dataMap.put("bbsId", bbsArticleInfo.getBbsId());
        dataMap.put("bbsNm", bbsArticleInfo.getBbsNm());
        dataMap.put("nttNo", bbsArticleInfo.getNttNo());
        dataMap.put("nttSj", StrUtil.cleanString(bbsArticleInfo.getNttSj()));
        dataMap.put("nttCn", StrUtil.cleanString(bbsArticleInfo.getNttCn()));
        dataMap.put("answerAt", bbsArticleInfo.getAnswerAt());
        dataMap.put("parntscttNo", bbsArticleInfo.getParntscttNo());
        dataMap.put("answerLc", bbsArticleInfo.getAnswerLc());
        dataMap.put("sortOrdr", bbsArticleInfo.getSortOrdr());
        dataMap.put("useAt", bbsArticleInfo.getUseAt());
        dataMap.put("ntceBgnde", bbsArticleInfo.getNtceBgnde());
        dataMap.put("ntceEndde", bbsArticleInfo.getNtceEndde());
        dataMap.put("ntcrId", bbsArticleInfo.getNtcrId());
        dataMap.put("ntcrNm", bbsArticleInfo.getNtcrNm());
        dataMap.put("atchFileId", bbsArticleInfo.getAtchFileId());
        dataMap.put("noticeAt", bbsArticleInfo.getNoticeAt());
        dataMap.put("sjBoldAt", bbsArticleInfo.getSjBoldAt());
        dataMap.put("secretAt", bbsArticleInfo.getSecretAt());
        dataMap.put("frstRegistPnttm", bbsArticleInfo.getFrstRegistPnttm());
        
        // 임베딩이 필요한 경우
        if (withEmbedding) {
            String combinedText = StrUtil.cleanString(bbsArticleInfo.getNttSj() + " " + bbsArticleInfo.getNttCn());
            Embedding bbsArticleResponse = embeddingModel.embed(combinedText).content();
            dataMap.put("bbsArticleEmbedding", bbsArticleResponse.vector());
        }
        
        return dataMap;
    }
	
	private void executeBulkRequest(BulkRequest.Builder builder, int currentPage, int totalPages) {
        try {
            BulkResponse bulkResponse = client.bulk(builder.build());
            if (bulkResponse.errors()) {
                bulkResponse.items().forEach(item -> {
                    if (item.error() != null) {
                        log.error("Error indexing document with ID: {} , Error: {}", 
                                item.id(), item.error().reason());
                    }
                });
            } else {
                log.debug("Batch {}/{} completed successfully", currentPage + 1, totalPages);
            }
        } catch (Exception e) {
            log.error("Error executing bulk request for page {}: {}", currentPage + 1, e.getMessage());
        }
    }
	
	private void logPageProgress(int currentPage, int totalPages, long startTime) {
        long executionTime = (System.currentTimeMillis() - startTime) / 1000;
        log.info("Processed page {}/{} in {} seconds", 
                currentPage + 1, totalPages, executionTime);
    }
	
	private void logTotalExecutionTime(long startTime) {
        long totalTime = (System.currentTimeMillis() - startTime) / 1000;
        log.info("Total indexing process completed in {} seconds", totalTime);
    }
}
