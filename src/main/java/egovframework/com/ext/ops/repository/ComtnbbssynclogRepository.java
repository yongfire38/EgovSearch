package egovframework.com.ext.ops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import egovframework.com.ext.ops.entity.Comtnbbssynclog;

@Repository
public interface ComtnbbssynclogRepository extends JpaRepository<Comtnbbssynclog, String> {
}
