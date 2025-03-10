
Освободить порты: server.port(приложение) = 1111 database.port(база данных) = 1488

В основной папке скачать файл - docker-compose.yml

Запустить докер

В CMD или Terminal перейти в папку где сохранили docker-compose.yml (cd C:\Users\Nikitka\IdeaProjects\test-to(это пример))

ввести команду docker-compose up -d - сразу запустится и приложение, и база данных

Swagger = http://localhost:1111/swagger-ui/index.html

Postman: 

REGISTRATION:
POST http://localhost:1111/api/auth/register

Body-raw-JSON:
{
  "email": "admin@example.com",
  "password": "admin123",
  "role": "ADMIN"
}

{
  "email": "user@example.com",
  "password": "user123",
  "role": "USER"
}


LOGIN:
POST http://localhost:1111/api/auth/login

Body-raw-JSON:
{
  "email": "admin@example.com",
  "password": "admin123"
}

{
  "email": "user@example.com",
  "password": "user123"
}

Получить JWT. Скопировать его

СREATE TASK(ADMIN):
POST http://localhost:1111/api/tasks
Добавить JWT в Authorization → Bearer Token

Body-raw-JSON:
{
  "title": "Разработать API",
  "description": "Создать Task Management System",
  "status": "PENDING",
  "priority": "HIGH"
}

GET TASK:
Добавить JWT в Authorization → Bearer Token
Body-raw-JSON:
GET http://localhost:1111/api/tasks/1


GET TASKS(filtration,pagination):
Добавить JWT в Authorization → Bearer Token

GET http://localhost:1111/api/tasks?status=IN_PROGRESS&page=0&size=5(пример)

DELETE TASK:
Добавить JWT в Authorization → Bearer Token

DELETE http://localhost:1111/api/tasks/1

UPDATE TASK(ADMIN):
Добавить JWT в Authorization → Bearer Token
PUT http://localhost:1111/api/tasks/1
Body-raw-JSON:
{
  "title": "API разработан",
  "status": "IN_PROGRESS"
}

CREATE COMMENT(USER):
Добавить JWT в Authorization → Bearer Token

POST http://localhost:1111/api/comments/1
Body-raw-JSON:
{
  "content": "Работаю над задачей"
}

GET COMMENTS:
Добавить JWT в Authorization → Bearer Token

GET http://localhost:1111/api/comments/1

