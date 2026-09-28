package com.tasknest.repository;

import com.tasknest.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByTaskListId(Long taskListId);

    // Today's tasks (tasks due today)
    List<Task> findByDueDate(LocalDate dueDate);

    // Overdue tasks: due_date < today AND completed = false
    @Query("SELECT t FROM Task t WHERE t.dueDate < :today AND t.completed = false ORDER BY t.dueDate ASC")
    List<Task> findOverdueTasks(@Param("today") LocalDate today);

    // Completed tasks
    List<Task> findByCompletedTrue();

    // Incomplete tasks
    List<Task> findByCompletedFalse();
}
