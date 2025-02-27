package egovframework.com.ext.ops.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchConfig {
	private String modelPath;
    private String tokenizerPath;
    private String stopTagsPath;
    private String synonymsPath;
    private String dictionaryRulesPath;
}
