package egovframework.com.ext.ops.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import egovframework.com.ext.ops.entity.Comtnbbs;
import egovframework.com.ext.ops.entity.ComtnbbsId;
import egovframework.com.ext.ops.service.BBSDTO;

@Repository
public interface ComtnbbsRepository extends JpaRepository<Comtnbbs, ComtnbbsId> {

	@Query("SELECT new egovframework.com.ext.ops.service.BBSDTO(" +
	           "a.nttSj, a.ntcrId, a.ntcrNm, a.nttNo, a.nttCn, a.password, a.frstRegisterId, " +
	           "COALESCE(b.userNm, a.ntcrNm), a.frstRegistPnttm, a.ntceBgnde, a.ntceEndde, a.rdcnt, " +
	           "a.useAt, a.atchFileId, a.comtnbbsId.bbsId, a.comtnbbsId.nttId, a.sjBoldAt, a.noticeAt, " +
	           "a.secretAt, a.parntscttNo, a.answerAt, a.answerLc, a.sortOrdr, " +
	           "c.bbsTyCode, c.replyPosblAt, c.fileAtchPosblAt, c.atchPosblFileNumber, c.bbsNm) " +
	           "FROM Comtnbbs a " +
	           "LEFT JOIN Comvnusermaster b ON a.frstRegisterId = b.esntlId " +
	           "LEFT JOIN a.comtnbbsmaster c " +
	           "WHERE a.comtnbbsId.nttId = :nttId")
	    Optional<BBSDTO> findBBSDTOByNttId(@Param("nttId") Long nttId);

	    @Query("SELECT new egovframework.com.ext.ops.service.BBSDTO(" +
	           "a.nttSj, a.ntcrId, a.ntcrNm, a.nttNo, a.nttCn, a.password, a.frstRegisterId, " +
	           "COALESCE(b.userNm, a.ntcrNm), a.frstRegistPnttm, a.ntceBgnde, a.ntceEndde, a.rdcnt, " +
	           "a.useAt, a.atchFileId, a.comtnbbsId.bbsId, a.comtnbbsId.nttId, a.sjBoldAt, a.noticeAt, " +
	           "a.secretAt, a.parntscttNo, a.answerAt, a.answerLc, a.sortOrdr, " +
	           "c.bbsTyCode, c.replyPosblAt, c.fileAtchPosblAt, c.atchPosblFileNumber, c.bbsNm) " +
	           "FROM Comtnbbs a " +
	           "LEFT JOIN Comvnusermaster b ON a.frstRegisterId = b.esntlId " +
	           "LEFT JOIN a.comtnbbsmaster c " +
	           "WHERE a.useAt = 'Y'")
	    Page<BBSDTO> findAllArticlesWithPaging(Pageable pageable);
	    
	    @Query("SELECT COUNT(a) FROM Comtnbbs a WHERE a.useAt = 'Y'")
	    long countAllArticles();
}
