# My_Acquiring_Server

## Описание

Сервер для обработки эквайринга (тестовый/локальный сервер). Использует Kotlin, Gradle и встроенную базу H2.

---

## Системные требования

* JDK 17 или выше (LTS)
---

## Установка и запуск

### 1. Клонирование репозитория

```bash
git clone https://github.com/itkiryl24-creator/My_Acquiring_Server.git
cd My_Acquiring_Server
```

### 2. Запуск проекта

* **Windows:**

```powershell
gradlew.bat run
```

* **Linux / Mac:**

```bash
chmod +x gradlew   # только первый раз
./gradlew run
```

Gradle автоматически:

1. Скачает все зависимости.
2. Скомпилирует проект.
3. Запустит функцию `main()` из `Main.kt`.



## База данных

* Используется **H2**.
* Файл базы создаётся автоматически: `posdb.mv.db`.

---

## Ключи

* В корне проекта должны находиться:

  * `private_key.pem`
  * `public_key.pem`

---

