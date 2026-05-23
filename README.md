# SupportFlow

SupportFlow — серверное приложение для автоматизации работы службы поддержки.

Система позволяет пользователям создавать заявки, а сотрудникам поддержки обрабатывать их, менять статусы, назначать ответственных, оставлять комментарии, прикреплять файлы и отслеживать историю обработки обращений.

## Основные возможности

- регистрация и авторизация пользователей;
- JWT-аутентификация;
- разделение ролей: USER, AGENT, ADMIN;
- создание, просмотр и фильтрация заявок;
- назначение заявок на сотрудников поддержки;
- изменение статусов заявок;
- комментарии к заявкам;
- загрузка вложений;
- история действий по заявке;
- SLA-дедлайны;
- статистика по заявкам;
- документация API через Swagger UI;
- запуск проекта через Docker Compose;
- миграции базы данных через Flyway;
- unit-тесты сервисов и контроллеров.

## Технологии

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven
- Docker
- Docker Compose
- JUnit 5
- Mockito
- MockMvc
- Swagger / OpenAPI

## Структура проекта

```text
src/main/java/com/supportflow
├── controllers      # REST-контроллеры
├── dto              # DTO для запросов и ответов
├── entities         # JPA-сущности
├── repositories     # Репозитории для работы с БД
├── security         # JWT и настройки безопасности
├── services         # Бизнес-логика
└── config           # Конфигурация приложения
```

```text
src/main/resources
├── db/migration     # Flyway-миграции
├── static/frontend  # frontend-страницы
└── application.properties.example
```

## Требования

Для запуска проекта нужны:

- Java 17;
- Maven или Maven Wrapper;
- Docker;
- Docker Compose;
- PostgreSQL, если запуск выполняется без Docker.

## Переменные окружения

Для Docker-запуска нужно создать файл `.env` в корне проекта.

Пример `.env`:

```env
POSTGRES_DB=supportflow
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_postgres_password

POSTGRES_PORT=5433
APP_PORT=8080

SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/supportflow
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_postgres_password

SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_FLYWAY_ENABLED=true
SPRING_FLYWAY_LOCATIONS=classpath:db/migration
SPRING_FLYWAY_BASELINE_ON_MIGRATE=false

JWT_SECRET_KEY=your_long_secret_key_at_least_32_characters
JWT_EXPIRATION_MS=86400000
```

Файл `.env` не должен попадать в git, потому что в нём хранятся пароли и секретные ключи.

## Запуск через Docker Compose

Собрать и запустить приложение:

```bash
docker compose up --build
```

После запуска приложение будет доступно по адресу:

```text
http://localhost:8080/frontend/index.html
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

PostgreSQL будет доступен на порту:

```text
localhost:5433
```

Остановить контейнеры:

```bash
docker compose down
```

Остановить контейнеры и удалить данные PostgreSQL:

```bash
docker compose down -v
```

## Локальный запуск без Docker

Создать базу данных PostgreSQL:

```sql
CREATE DATABASE supportflow;
```

Создать локальный файл настроек:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

В `application.properties` нужно указать свои настройки подключения к базе данных и JWT-секрет.

Запустить приложение:

```bash
./mvnw spring-boot:run
```

## Миграции базы данных

В проекте используется Flyway.

Миграции находятся в папке:

```text
src/main/resources/db/migration
```

Основная миграция:

```text
V1__init_schema.sql
```

При запуске приложения Flyway автоматически создаёт структуру базы данных.

Hibernate работает в режиме:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Это значит, что Hibernate не создаёт таблицы сам, а только проверяет, что структура базы данных соответствует сущностям проекта.

## Тесты

Запуск тестов:

```bash
./mvnw test
```

В проекте добавлены unit-тесты для сервисов и контроллеров.

Покрываются основные части приложения:

- авторизация;
- пользователи;
- заявки;
- комментарии;
- SLA;
- проверка доступа к заявкам;
- переходы между статусами;
- REST-контроллеры.

## Основные API endpoints

### Авторизация

| Метод | Endpoint | Описание |
|---|---|---|
| POST | `/auth/register` | Регистрация пользователя |
| POST | `/auth/login` | Авторизация пользователя |

### Пользователи

| Метод | Endpoint | Описание |
|---|---|---|
| GET | `/users` | Получение списка пользователей |
| GET | `/users/{id}` | Получение пользователя по id |
| PATCH | `/users/{id}/role` | Изменение роли пользователя |

### Заявки

| Метод | Endpoint | Описание |
|---|---|---|
| POST | `/tickets` | Создание заявки |
| GET | `/tickets` | Получение списка заявок |
| GET | `/tickets/my` | Получение заявок текущего пользователя |
| GET | `/tickets/{id}` | Получение заявки по id |
| PATCH | `/tickets/{id}/assign` | Назначение заявки на сотрудника |
| PATCH | `/tickets/{id}/status` | Изменение статуса заявки |
| GET | `/tickets/{id}/timeline` | Получение истории действий по заявке |

### Комментарии

| Метод | Endpoint | Описание |
|---|---|---|
| POST | `/tickets/{ticketId}/comments` | Добавление комментария |
| GET | `/tickets/{ticketId}/comments` | Получение комментариев заявки |

### Вложения

| Метод | Endpoint | Описание |
|---|---|---|
| POST | `/tickets/{ticketId}/attachments` | Загрузка вложения |
| GET | `/attachments/{id}` | Скачивание вложения |

### Статистика

| Метод | Endpoint | Описание |
|---|---|---|
| GET | `/statistics/overview` | Общая статистика по заявкам |

## Роли пользователей

### USER

Обычный пользователь может:

- создавать заявки;
- просматривать свои заявки;
- оставлять комментарии;
- прикреплять файлы.

### AGENT

Сотрудник поддержки может:

- просматривать заявки;
- брать заявки в работу;
- менять статус заявок;
- оставлять комментарии;
- закрывать обработанные обращения.

### ADMIN

Администратор может:

- управлять пользователями;
- назначать роли;
- просматривать все заявки;
- получать статистику;
- управлять процессом обработки обращений.

## Статусы заявок

В системе используются следующие статусы:

- NEW — новая заявка;
- OPEN — заявка открыта;
- IN_PROGRESS — заявка в работе;
- WAITING — ожидание ответа;
- RESOLVED — заявка решена;
- CLOSED — заявка закрыта.

## Полезные команды

Собрать проект:

```bash
./mvnw clean package
```

Запустить тесты:

```bash
./mvnw test
```

Запустить приложение локально:

```bash
./mvnw spring-boot:run
```

Запустить через Docker:

```bash
docker compose up --build
```

Проверить статус git:

```bash
git status
```

Для примера настроек используется файл:

```text
src/main/resources/application.properties.example
```