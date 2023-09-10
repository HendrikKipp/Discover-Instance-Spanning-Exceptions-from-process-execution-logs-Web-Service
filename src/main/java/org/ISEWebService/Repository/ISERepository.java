package org.ISEWebService.Repository;

import org.ISEWebService.Model.Entity.ISEEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ISERepository extends JpaRepository<ISEEntity, Long> {
}
