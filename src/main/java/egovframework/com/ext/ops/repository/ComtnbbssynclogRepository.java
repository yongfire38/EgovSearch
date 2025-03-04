package egovframework.com.ext.ops.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import egovframework.com.ext.ops.entity.Comtnbbssynclog;

@Repository
public interface ComtnbbssynclogRepository extends JpaRepository<Comtnbbssynclog, String> {
    
    /**
     * NTT_ID로 가장 최근의 동기화 로그를 조회한다.
     * 
     * @param nttId 게시물 ID
     * @return 동기화 로그
     */
    Optional<Comtnbbssynclog> findTopByNttIdOrderByRegistPnttmDesc(Long nttId);
    
    /**
     * 동기화 상태 코드로 동기화 로그 목록을 조회한다.
     * 
     * @param syncSttusCode 동기화 상태 코드
     * @return 동기화 로그 목록
     */
    List<Comtnbbssynclog> findBySyncSttusCode(String syncSttusCode);
}
