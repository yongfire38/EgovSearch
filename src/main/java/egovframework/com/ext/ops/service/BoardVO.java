package egovframework.com.ext.ops.service;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BoardVO extends Board implements Serializable {
	/** 검색시작일 */
    private String searchBgnDe = "";

    /** 검색조건 */
    private String searchCnd = "";

    /** 검색종료일 */
    private String searchEndDe = "";

    /** 검색단어 */
    private String searchWrd = "";

    /** 정렬순서(DESC,ASC) */
    private long sortOrdr = 0L;

    /** 검색사용여부 */
    private String searchUseYn = "";

    /** 현재페이지 */
    private int pageIndex = 1;

    /** 페이지개수 */
    private int pageUnit = 10;

    /** 페이지사이즈 */
    private int pageSize = 10;

    /** 첫페이지 인덱스 */
    private int firstIndex = 1;

    /** 마지막페이지 인덱스 */
    private int lastIndex = 1;

    /** 페이지당 레코드 개수 */
    private int recordCountPerPage = 10;

    /** 레코드 번호 */
    private int rowNo = 0;

    /** 최초 등록자명 */
    private String frstRegisterNm = "";

    /** 최종 수정자명 */
    private String lastUpdusrNm = "";

    /** 유효여부 */
    private String isExpired = "N";

    /** 상위 정렬 순서 */
    private String parntsSortOrdr = "";

    /** 상위 답변 위치 */
    private String parntsReplyLc = "";

    /** 게시판 유형코드 */
    private String bbsTyCode = "";

    /** 게시판 속성코드 */
    private String bbsAttrbCode = "";

    /** 게시판 명 */
    private String bbsNm = "";

    /** 파일첨부가능여부 */
    private String fileAtchPosblAt = "";

    /** 첨부가능파일숫자 */
    private int posblAtchFileNumber = 0;

    /** 답장가능여부 */
    private String replyPosblAt = "";

    /** 조회 수 증가 여부 */
    private boolean plusCount = false;

    /** 익명등록 여부 */
    private String anonymousAt = "";

    /** 하위 페이지 인덱스 (댓글 및 만족도 조사 여부 확인용) */
    private String subPageIndex = "";

    /** 게시글 댓글개수 */
    private String commentCo = "";

    /** 볼드체 여부 */
    private String sjBoldAt;

    /** 공지 여부 */
    private String noticeAt;

    /** 비밀글 여부 */
    private String secretAt;
    
    /** 유사도 점수 */
    private double score;
}
