# SupportFlow

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.6-green)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-9.22.0-lightgrey)](https://flywaydb.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

SupportFlow — это веб-приложение для управления заявками службы поддержки, позволяющее пользователям создавать обращения, а сотрудникам — обрабатывать их, оставлять комментарии, прикреплять файлы и отслеживать историю действий в реальном времени.

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
- документация API через Swagger UI и OpenAPI;
- запуск проекта через Docker Compose;
- миграции базы данных через Flyway;
- unit-тесты сервисов и контроллеров;
- интеграционные тесты с Testcontainers для проверки работы с PostgreSQL.

## 🛠 Технологический стек

| Технология         | Версия / Описание                      |
|--------------------|----------------------------------------|
| Java               | 21                                     |
| Spring Boot        | 4.0.6                                  |
| Spring Security    | JWT (jjwt 0.12.6)                      |
| Spring Data JPA    | Hibernate ORM                          |
| PostgreSQL         | 16                                     |
| Flyway             | Миграции схемы БД                      |
| springdoc-openapi  | 3.0.2 — Swagger UI                     |
| Lombok             | Генерация boilerplate-кода             |
| Docker             | Dockerfile + Docker Compose            |
| JUnit 5 + Mockito  | Unit-тесты                             |
| Testcontainers     | Интеграционные тесты с PostgreSQL      |
| JaCoCo             | 0.8.13 — покрытие кода                 |

## Структура проекта

```text

├ src/main/java/com/supportflow
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com/supportflow
│   │   │       ├── audit
│   │   │       │   ├── controller      # Контроллеры для аудита
│   │   │       │   ├── dto             # DTO для аудита
│   │   │       │   ├── entity          # JPA-сущности для аудита
│   │   │       │   ├── enums           # Перечисления для аудита
│   │   │       │   ├── repository      # Репозитории для аудита
│   │   │       │   └── service         # Сервисы аудита
│   │   │       ├── auth
│   │   │       │   ├── controller      # Контроллеры аутентификации
│   │   │       │   ├── dto             # DTO для логина и регистрации
│   │   │       │   └── service         # Сервисы аутентификации
│   │   │       ├── comment
│   │   │       │   ├── controller      # Контроллеры комментариев
│   │   │       │   ├── dto             # DTO для комментариев
│   │   │       │   └── service         # Сервисы комментариев
│   │   │       ├── exception           # Общие исключения приложения
│   │   │       ├── security
│   │   │       │   ├── jwt             # JWT-сервисы
│   │   │       │   ├── CurrentUserService.java
│   │   │       │   ├── CustomUserDetailsService.java
│   │   │       │   ├── OpenApiConfig.java
│   │   │       │   └── SecurityConfig.java
│   │   │       ├── sla
│   │   │       │   ├── scheduler       # Планировщики SLA
│   │   │       │   └── service         # Сервисы SLA
│   │   │       ├── statistics
│   │   │       │   ├── controller      # Контроллеры статистики
│   │   │       │   ├── dto             # DTO статистики
│   │   │       │   └── service         # Сервисы статистики
│   │   │       ├── ticket
│   │   │       │   ├── attachment      # Работа с вложениями
│   │   │       │   ├── controller      # Контроллеры заявок
│   │   │       │   ├── dto             # DTO для заявок
│   │   │       │   ├── entity          # JPA-сущности заявок
│   │   │       │   ├── enums           # Перечисления заявок
│   │   │       │   ├── exception       # Исключения заявок
│   │   │       │   ├── repository      # Репозитории заявок
│   │   │       │   ├── service         # Сервисы заявок
│   │   │       │   └── specification   # Спецификации для запросов
│   │   │       ├── user
│   │   │       │   ├── controller      # Контроллеры пользователей
│   │   │       │   ├── dto             # DTO пользователей
│   │   │       │   ├── entity          # JPA-сущности пользователей
│   │   │       │   ├── enums           # Перечисления пользователей
│   │   │       │   ├── exception       # Исключения пользователей
│   │   │       │   ├── repository      # Репозитории пользователей
│   │   │       │   └── service         # Сервисы пользователей
│   │   │       └── TicketSystemApplication.java  # Главный класс приложения
│   │   └── resources
│   │       ├── db/migration            # Flyway-миграции
│   │       │   ├── V1__init_schema.sql
│   │       │   └── V2__seed_admin.sql
│   │       ├── static/frontend         # Frontend-страницы
│   │       │   ├── css
│   │       │   │   └── styles.css
│   │       │   └── js
│   │       │       ├── api.js
│   │       │       ├── attachments.js
│   │       │       ├── auth.js
│   │       │       └── comments.js
│   │       ├── templates               
│   │       ├── application.properties
│   │       └── application.properties.example
│   └── test
│       └── java/com/supportflow
│           ├── integration              # Интеграционные тесты (с Testcontainers)
│           └── unittests
│               └── controller           # Unit-тесты контроллеров
```


## Требования

Для запуска проекта нужны:

- Java 21;
- Maven или Maven Wrapper;
- Docker;
- Docker Compose;
- PostgreSQL, если запуск выполняется без Docker.

## Переменные окружения


Для запуска через Docker нужно создать файл `.env` в корне проекта.

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
Убедитесь, что PostgreSQL запущен локально.

Создать базу данных PostgreSQL:

```sql
CREATE DATABASE supportflow;
```

Создать локальный файл настроек:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

В `application.properties` нужно указать свои настройки подключения к базе данных и JWT-секрет.

```
spring.datasource.url=jdbc:postgresql://localhost:5432/supportflow
spring.datasource.username=postgres
spring.datasource.password=your_postgres_password

spring.jpa.hibernate.ddl-auto=validate
jwt.secret-key=your_long_secret_key_at_least_32_characters
jwt.expiration-ms=86400000
```

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

Миграции Flyway:

```text
V1__init_schema.sql # Создание основной структуры базы данных
```

```text
V2_seed_admin.sql # Добавление учетной записи администратора
```

При запуске приложения Flyway автоматически применяет все миграции, создавая необходимую структуру базы данных и добавляя начальные данные (например, администратора).

Hibernate работает в режиме validate:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Это означает, что Hibernate не создаёт таблицы автоматически, а только проверяет соответствие структуры базы данных JPA-сущностям проекта.

## Тесты

В проекте добавлены unit-тесты для сервисов и контроллеров, а также интеграционные тесты с использованием Testcontainers, которые позволяют запускать тестовую базу PostgreSQL в контейнере и проверять работу приложения в условиях, близких к продакшену.

Запуск тестов:

```bash
./mvnw test
```

В проекте добавлены unit-тесты для сервисов и контроллеров.

Покрываются основные части приложения:

- Авторизация пользователей;
- Управление пользователями и ролями;
- Создание и просмотр заявок;
- Добавление и получение комментариев;
- Контроль SLA и дедлайнов;
- Проверка прав доступа к заявкам;
- Переходы между статусами заявок;
- Валидация REST-контроллеров;
- Проверка миграций Flyway и начальных данных (например, администратора) через интеграционные тесты.

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

Для примера настроек используется файл:

```text
src/main/resources/application.properties.example
```