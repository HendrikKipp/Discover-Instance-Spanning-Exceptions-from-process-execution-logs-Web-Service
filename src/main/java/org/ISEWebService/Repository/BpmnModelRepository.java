package org.ISEWebService.Repository;

import org.ISEWebService.Model.Entity.BpmnModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BpmnModelRepository extends JpaRepository<BpmnModelEntity, Long> {
}
