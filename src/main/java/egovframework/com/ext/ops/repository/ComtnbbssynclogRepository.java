package egovframework.com.ext.ops.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import egovframework.com.ext.ops.entity.Comtnbbssynclog;

@Repository
public interface ComtnbbssynclogRepository extends JpaRepository<Comtnbbssynclog, String> {
	// SYNC_STTUS_CODE가 'N'또는 'E'인 데이터를 10건 가져오기 (등록 순서대로)
		List<Comtnbbssynclog> findTop10BySyncSttusCodeInOrderByRegistPnttmAsc(List<String> syncSttusCodes);
}
