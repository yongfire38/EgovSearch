package egovframework.com.ext.ops.service;

import java.time.LocalDateTime;

public class BBSDTO {
    private String nttSj;
    private String ntcrId;
    private String ntcrNm;
    private Long nttNo;
    private String nttCn;
    private String password;
    private String frstRegisterId;
    private String frstRegisterNm;
    private LocalDateTime frstRegistPnttm;
    private String ntceBgnde;
    private String ntceEndde;
    private Integer rdcnt;
    private String useAt;
    private String atchFileId;
    private String bbsId;
    private Long nttId;
    private String sjBoldAt;
    private String noticeAt;
    private String secretAt;
    private Integer parntscttNo;
    private String answerAt;
    private Integer answerLc;
    private long sortOrdr;
    private String bbsTyCode;
    private String replyPosblAt;
    private String fileAtchPosblAt;
    private Integer atchPosblFileNumber;
    private String bbsNm;

    public BBSDTO(String nttSj, String ntcrId, String ntcrNm, Long nttNo, String nttCn, String password, String frstRegisterId, String frstRegisterNm, LocalDateTime frstRegistPnttm, String ntceBgnde, String ntceEndde, Integer rdcnt, String useAt, String atchFileId, String bbsId, Long nttId, String sjBoldAt, String noticeAt, String secretAt, Integer parntscttNo, String answerAt, Integer answerLc, long sortOrdr, String bbsTyCode, String replyPosblAt, String fileAtchPosblAt, Integer atchPosblFileNumber, String bbsNm) {
        this.nttSj = nttSj;
        this.ntcrId = ntcrId;
        this.ntcrNm = ntcrNm;
        this.nttNo = nttNo;
        this.nttCn = nttCn;
        this.password = password;
        this.frstRegisterId = frstRegisterId;
        this.frstRegisterNm = frstRegisterNm;
        this.frstRegistPnttm = frstRegistPnttm;
        this.ntceBgnde = ntceBgnde;
        this.ntceEndde = ntceEndde;
        this.rdcnt = rdcnt;
        this.useAt = useAt;
        this.atchFileId = atchFileId;
        this.bbsId = bbsId;
        this.nttId = nttId;
        this.sjBoldAt = sjBoldAt;
        this.noticeAt = noticeAt;
        this.secretAt = secretAt;
        this.parntscttNo = parntscttNo;
        this.answerAt = answerAt;
        this.answerLc = answerLc;
        this.sortOrdr = sortOrdr;
        this.bbsTyCode = bbsTyCode;
        this.replyPosblAt = replyPosblAt;
        this.fileAtchPosblAt = fileAtchPosblAt;
        this.atchPosblFileNumber = atchPosblFileNumber;
        this.bbsNm = bbsNm;
    }

    public String getNttSj() {
        return nttSj;
    }

    public void setNttSj(String nttSj) {
        this.nttSj = nttSj;
    }

    public String getNtcrId() {
        return ntcrId;
    }

    public void setNtcrId(String ntcrId) {
        this.ntcrId = ntcrId;
    }

    public String getNtcrNm() {
        return ntcrNm;
    }

    public void setNtcrNm(String ntcrNm) {
        this.ntcrNm = ntcrNm;
    }

    public Long getNttNo() {
        return nttNo;
    }

    public void setNttNo(Long nttNo) {
        this.nttNo = nttNo;
    }

    public String getNttCn() {
        return nttCn;
    }

    public void setNttCn(String nttCn) {
        this.nttCn = nttCn;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFrstRegisterId() {
        return frstRegisterId;
    }

    public void setFrstRegisterId(String frstRegisterId) {
        this.frstRegisterId = frstRegisterId;
    }

    public String getFrstRegisterNm() {
        return frstRegisterNm;
    }

    public void setFrstRegisterNm(String frstRegisterNm) {
        this.frstRegisterNm = frstRegisterNm;
    }

    public LocalDateTime getFrstRegistPnttm() {
        return frstRegistPnttm;
    }

    public void setFrstRegistPnttm(LocalDateTime frstRegistPnttm) {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd");
//        this.frstRegistPnttm = frstRegistPnttm.format(formatter);
        this.frstRegistPnttm = frstRegistPnttm;
    }

    public String getNtceBgnde() {
        return ntceBgnde;
    }

    public void setNtceBgnde(String ntceBgnde) {
        this.ntceBgnde = ntceBgnde;
    }

    public String getNtceEndde() {
        return ntceEndde;
    }

    public void setNtceEndde(String ntceEndde) {
        this.ntceEndde = ntceEndde;
    }

    public Integer getRdcnt() {
        return rdcnt;
    }

    public void setRdcnt(Integer rdcnt) {
        this.rdcnt = rdcnt;
    }

    public String getUseAt() {
        return useAt;
    }

    public void setUseAt(String useAt) {
        this.useAt = useAt;
    }

    public String getAtchFileId() {
        return atchFileId;
    }

    public void setAtchFileId(String atchFileId) {
        this.atchFileId = atchFileId;
    }

    public String getBbsId() {
        return bbsId;
    }

    public void setBbsId(String bbsId) {
        this.bbsId = bbsId;
    }

    public Long getNttId() {
        return nttId;
    }

    public void setNttId(Long nttId) {
        this.nttId = nttId;
    }

    public String getSjBoldAt() {
        return sjBoldAt;
    }

    public void setSjBoldAt(String sjBoldAt) {
        this.sjBoldAt = sjBoldAt;
    }

    public String getNoticeAt() {
        return noticeAt;
    }

    public void setNoticeAt(String noticeAt) {
        this.noticeAt = noticeAt;
    }

    public String getSecretAt() {
        return secretAt;
    }

    public void setSecretAt(String secretAt) {
        this.secretAt = secretAt;
    }

    public Integer getParntscttNo() {
        return parntscttNo;
    }

    public void setParntscttNo(Integer parntscttNo) {
        this.parntscttNo = parntscttNo;
    }

    public String getAnswerAt() {
        return answerAt;
    }

    public void setAnswerAt(String answerAt) {
        this.answerAt = answerAt;
    }

    public Integer getAnswerLc() {
        return answerLc;
    }

    public void setAnswerLc(Integer answerLc) {
        this.answerLc = answerLc;
    }

    public long getSortOrdr() {
        return sortOrdr;
    }

    public void setSortOrdr(long sortOrdr) {
        this.sortOrdr = sortOrdr;
    }

    public String getBbsTyCode() {
        return bbsTyCode;
    }

    public void setBbsTyCode(String bbsTyCode) {
        this.bbsTyCode = bbsTyCode;
    }

    public String getReplyPosblAt() {
        return replyPosblAt;
    }

    public void setReplyPosblAt(String replyPosblAt) {
        this.replyPosblAt = replyPosblAt;
    }

    public String getFileAtchPosblAt() {
        return fileAtchPosblAt;
    }

    public void setFileAtchPosblAt(String fileAtchPosblAt) {
        this.fileAtchPosblAt = fileAtchPosblAt;
    }

    public Integer getAtchPosblFileNumber() {
        return atchPosblFileNumber;
    }

    public void setAtchPosblFileNumber(Integer atchPosblFileNumber) {
        this.atchPosblFileNumber = atchPosblFileNumber;
    }

    public String getBbsNm() {
        return bbsNm;
    }

    public void setBbsNm(String bbsNm) {
        this.bbsNm = bbsNm;
    }
}
