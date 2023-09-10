package org.ISEWebService.Repository;

import org.ISEWebService.Model.Entity.ProcessEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessRepository extends JpaRepository<ProcessEntity, Long> {
}
