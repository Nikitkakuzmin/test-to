package kz.nik.testto.repository;

import jakarta.transaction.Transactional;
import kz.nik.testto.model.Task;
import kz.nik.testto.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByAuthorEmail(String email, Pageable pageable);
    Page<Task> findByAssigneeEmail(String email, Pageable pageable);
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findAll(Pageable pageable);
}