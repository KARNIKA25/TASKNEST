package com.tasknest;

import com.tasknest.entity.Priority;
import com.tasknest.entity.Task;
import com.tasknest.entity.TaskList;
import com.tasknest.entity.User;
import com.tasknest.repository.TaskListRepository;
import com.tasknest.repository.TaskRepository;
import com.tasknest.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
public class TaskNestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskNestApplication.class, args);
    }

    /**
     * Seeds initial default user and sample task lists (Academics, Personal, Projects)
     * if the database is completely empty on first launch.
     */
    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository,
                                         TaskListRepository taskListRepository,
                                         TaskRepository taskRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                User defaultUser = new User("Student User", "student@tasknest.com");
                userRepository.save(defaultUser);

                TaskList academics = new TaskList("Academics", defaultUser);
                TaskList personal = new TaskList("Personal", defaultUser);
                TaskList projects = new TaskList("Projects", defaultUser);

                taskListRepository.save(academics);
                taskListRepository.save(personal);
                taskListRepository.save(projects);

                LocalDate today = LocalDate.now();
                taskRepository.save(new Task("DBMS Lab Record Submission", "Complete ER diagrams and relational normalization write-up", today, Priority.HIGH, academics));
                taskRepository.save(new Task("Operating Systems Viva Preparation", "Revise CPU scheduling algorithms and paging concepts", today.plusDays(2), Priority.MEDIUM, academics));
                taskRepository.save(new Task("Data Structures Assignment 1", "Implement AVL tree self-balancing and rotations", today.minusDays(7), Priority.HIGH, academics));

                Task t1 = new Task("Morning Workout & Cardio", "Leg day routine and 20 mins light jogging", today, Priority.LOW, personal);
                t1.setCompleted(true);
                taskRepository.save(t1);

                taskRepository.save(new Task("Pay Electricity & Wifi Bills", "Settle monthly household utilities through net banking", today.minusDays(4), Priority.HIGH, personal));
                taskRepository.save(new Task("Read 2 Chapters of Clean Code", "Focus on meaningful variable names and functions", today.plusDays(5), Priority.LOW, personal));

                Task t2 = new Task("TaskNest Frontend UI Polishing", "Add responsive CSS layout, toast alerts, and live category filters", today.minusDays(1), Priority.MEDIUM, projects);
                t2.setCompleted(true);
                taskRepository.save(t2);

                taskRepository.save(new Task("Design MySQL Schema & ER Diagram", "Export high-resolution database schema diagram for report", today, Priority.HIGH, projects));
                taskRepository.save(new Task("Prepare DBMS Mini Project Presentation", "Create 10 slides covering tech stack, schema, Postman tests, and demo", today.plusDays(5), Priority.MEDIUM, projects));
            }
        };
    }
}
