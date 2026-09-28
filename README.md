# TASKNEST — Personal To-Do & Task Manager
> **College DBMS Mini Project** | Simple, Clean, and Easy to Explain in a Viva.

TaskNest is a full-stack personal task management web application built using **Java, Spring Boot, Spring Data JPA, MySQL, and modern Vanilla HTML/CSS/JavaScript**.

---

## 🌟 Key Features

1. **Task Lists Management**: Create, view, update, and delete custom task lists (e.g., *Academics*, *Personal*, *Projects*).
2. **Comprehensive Task Operations**: Full CRUD for tasks with Title, Description, Due Date, Priority (`LOW`, `MEDIUM`, `HIGH`), and Completion Status.
3. **Status Toggles**: Mark tasks complete or incomplete with a single click.
4. **Dynamic Filter Views**:
   - **All Tasks**: Complete view of all active and completed tasks.
   - **Today's Tasks**: Filtered by `due_date = CURRENT_DATE`.
   - **Overdue Tasks**: Filtered by `due_date < CURRENT_DATE AND completed = false`.
   - **Completed Tasks**: Filtered by `completed = true`.
5. **Move Tasks**: Seamlessly move tasks between different task lists.
6. **Robust Business Rules & Exception Handling**: Rejects invalid priority values and empty titles with clean JSON error messages.

---

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.2.5 (Spring Web, Spring Data JPA, Validation)
- **Database**: MySQL (Port `3306`, Database: `tasknest_db`)
- **Frontend**: HTML5, CSS3 (Modern Flex/Grid design, responsive), Vanilla JavaScript (`fetch()` API)
- **API Testing**: Postman (Pre-configured collection included)
- **Build Tool**: Apache Maven

---

## 🗄️ Database Architecture & Schema (MySQL)

### Entity Relationship (ER) Structure
```text
  +------------------+         1 : N         +-------------------+
  |      users       | --------------------> |    task_lists     |
  +------------------+                       +-------------------+
  | id (PK)          |                       | id (PK)           |
  | name             |                       | name              |
  | email (UNIQUE)   |                       | user_id (FK)      |
  +------------------+                       +-------------------+
                                                       |
                                                       | 1 : N
                                                       v
                                             +-------------------+
                                             |       tasks       |
                                             +-------------------+
                                             | id (PK)           |
                                             | title             |
                                             | description       |
                                             | due_date          |
                                             | priority (ENUM)   |
                                             | completed (BOOL)  |
                                             | task_list_id (FK) |
                                             +-------------------+
```

### Table Definitions

1. **`users`**
   - `id`: BIGINT, Primary Key, Auto Increment
   - `name`: VARCHAR(255), Not Null
   - `email`: VARCHAR(255), Not Null, Unique

2. **`task_lists`**
   - `id`: BIGINT, Primary Key, Auto Increment
   - `name`: VARCHAR(255), Not Null
   - `user_id`: BIGINT, Foreign Key -> `users(id)`

3. **`tasks`**
   - `id`: BIGINT, Primary Key, Auto Increment
   - `title`: VARCHAR(255), Not Null
   - `description`: TEXT
   - `due_date`: DATE
   - `priority`: ENUM('LOW', 'MEDIUM', 'HIGH'), Default 'MEDIUM'
   - `completed`: BOOLEAN, Default FALSE
   - `task_list_id`: BIGINT, Foreign Key -> `task_lists(id)`

---

## ⚙️ How to Run the Project

### Step 1: Configure MySQL Credentials
Open `src/main/resources/application.properties` and enter your MySQL username and password:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tasknest_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=Asia/Kolkata
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### Step 2: Build and Run with Maven

Open your terminal in the project directory:

```bash
# Clean and compile
mvn clean install

# Run the Spring Boot application
mvn spring-boot:run
```

### Step 3: Access the Application
- Open your browser and navigate to: **`http://localhost:8080/`**

---

## 📡 REST API Reference

### 1. User APIs
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/users` | Create a new user |
| `GET` | `/api/users` | Get all users |

### 2. Task List APIs
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/lists` | Create a new task list |
| `GET` | `/api/lists` | Get all task lists with task counts |
| `GET` | `/api/lists/{id}` | Get task list by ID |
| `PUT` | `/api/lists/{id}` | Update task list name |
| `DELETE` | `/api/lists/{id}` | Delete task list (cascades to its tasks) |

### 3. Task APIs
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/tasks` | Create a new task |
| `GET` | `/api/tasks` | Get all tasks |
| `GET` | `/api/tasks/{id}` | Get task by ID |
| `PUT` | `/api/tasks/{id}` | Update task details |
| `DELETE` | `/api/tasks/{id}` | Delete task |
| `PUT` | `/api/tasks/{id}/complete` | Mark task as completed |
| `PUT` | `/api/tasks/{id}/incomplete` | Mark task as incomplete |
| `GET` | `/api/tasks/today` | Get tasks due today |
| `GET` | `/api/tasks/overdue` | Get overdue incomplete tasks (`due_date < today AND completed = false`) |
| `GET` | `/api/tasks/completed` | Get completed tasks |
| `PUT` | `/api/tasks/{taskId}/move/{listId}` | Move task to another task list |
| `GET` | `/api/tasks/list/{listId}` | Get all tasks belonging to a specific list |

---

## 🧪 Testing with Postman

A pre-configured Postman collection is included in the project:
`TaskNest_Postman_Collection.json`

### How to Import:
1. Open Postman.
2. Click **Import** (top left).
3. Select `TaskNest_Postman_Collection.json` from the project root.
4. Execute requests for Users, Task Lists, Tasks, and Business Rule Validation Tests.

---

## 🎯 Viva Q&A Guide (Quick Cheat Sheet)

- **Q: What is the relationship between entities in TaskNest?**
  - **A**: `User` has a **1-to-Many** relationship with `TaskList`. `TaskList` has a **1-to-Many** relationship with `Task`.
- **Q: How is the overdue task rule implemented?**
  - **A**: Using Spring Data JPA JPQL Query: `SELECT t FROM Task t WHERE t.dueDate < :today AND t.completed = false`. If `completed = true`, it is filtered out of the overdue view.
- **Q: How does TaskNest prevent invalid priority values?**
  - **A**: The `Priority` enum (`LOW`, `MEDIUM`, `HIGH`) is validated in the service layer. Any unrecognized value throws an `IllegalArgumentException` caught by `@RestControllerAdvice` returning HTTP 400 with a clean JSON error response.
- **Q: What is the role of `spring.jpa.hibernate.ddl-auto=update`?**
  - **A**: It automatically creates or updates MySQL database tables matching the JPA entity definitions on application startup without dropping existing data.
