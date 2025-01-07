package egovframework.com.ext.ops.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "COMTNBBS")
public class Comtnbbs {
	
	// NTT_ID, BBS_ID
    @EmbeddedId
    private ComtnbbsId comtnbbsId;

    @ManyToOne
    @MapsId("bbsId")
    @JoinColumn(name = "BBS_ID", referencedColumnName = "BBS_ID")
    private Comtnbbsmaster comtnbbsmaster;

    @Column(name = "NTT_NO")
    private Long nttNo;

    @Column(name = "NTT_SJ")
    private String nttSj;

    @Column(name = "NTT_CN")
    private String nttCn;

    @Column(name = "ANSWER_AT")
    private String answerAt;

    @Column(name = "PARNTSCTT_NO")
    private Integer parntscttNo;

    @Column(name = "ANSWER_LC")
    private Integer answerLc;

    @Column(name = "SORT_ORDR")
    private long sortOrdr;

    @Column(name = "RDCNT")
    private Integer rdcnt;

    @Column(name = "USE_AT")
    private String useAt;

    @Column(name = "NTCE_BGNDE")
    private String ntceBgnde;

    @Column(name = "NTCE_ENDDE")
    private String ntceEndde;

    @Column(name = "NTCR_ID")
    private String ntcrId;

    @Column(name = "NTCR_NM")
    private String ntcrNm;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "ATCH_FILE_ID")
    private String atchFileId;

    @Column(name = "NOTICE_AT")
    private String noticeAt;

    @Column(name = "SJ_BOLD_AT")
    private String sjBoldAt;

    @Column(name = "SECRET_AT")
    private String secretAt;

    @Column(name = "FRST_REGIST_PNTTM")
    private LocalDateTime frstRegistPnttm;

    @Column(name = "FRST_REGISTER_ID")
    private String frstRegisterId;

    @Column(name = "LAST_UPDT_PNTTM")
    private LocalDateTime lastUpdtPnttm;

    @Column(name = "LAST_UPDUSR_ID")
    private String lastUpdusrId;

    @Column(name = "BLOG_ID")
    private String blogId;

    @ManyToOne
    @JoinColumn(name = "FRST_REGISTER_ID", insertable = false, updatable = false)
    Comvnusermaster userList;

}
