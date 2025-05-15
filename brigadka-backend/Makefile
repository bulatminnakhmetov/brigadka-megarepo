# --- Загрузка переменных окружения из .env ---
ifneq ("$(wildcard .env)","")
	include .env
	export
endif

# --- Сборка приложения ---
build-release:
	# Сборка релизной версии (оптимизированная, без отладочной информации)
	CGO_ENABLED=0 go build -tags netgo -ldflags "-s -w" -o bin/app ./cmd/service

build-debug:
	# Сборка отладочной версии (без оптимизаций, с дебаг-инфой)
	CGO_ENABLED=0 go build -gcflags "all=-N -l" -o bin/app-debug ./cmd/service

# --- Запуск приложения ---
run-release: build-release
	# Запуск релизной версии с переменной окружения GIN_MODE=release
	GIN_MODE=release ./bin/app

run-debug: build-debug
	# Запуск отладочной версии
	./bin/app-debug

# --- Тесты ---
run-unit-tests:
	# Запуск юнит-тестов
	go test ./internal/...

# --- Тесты ---
run-integration-tests: generate-local-ca
	cp .env.docker .env
	# Запуск интеграционных тестов в Docker
ifdef DEBUG-ENV
	# Запуск с выводом логов в консоль
	@( \
		cleanup() { \
			echo "🧹 Очистка тестового окружения..."; \
			docker compose --profile test down -v --remove-orphans; \
		}; \
		trap cleanup EXIT INT TERM; \
		echo "🔍 Запуск в режиме отладки (Ctrl+C для остановки)"; \
		docker compose --profile test up --build --force-recreate --remove-orphans; \
	)
else
	# Запуск в фоновом режиме с отслеживанием логов тестов
	@( \
		cleanup() { \
			echo "🧹 Очистка тестового окружения..."; \
			docker compose --profile test down -v --remove-orphans; \
		}; \
		trap cleanup EXIT INT TERM; \
		docker compose --profile test up --build --force-recreate --remove-orphans -d || { \
			echo "❌ Ошибка во время запуска тестового окружения"; \
			echo "Чтобы посмотреть логи, запустите make run-integration-tests DEBUG-ENV=1"; \
			exit 1; \
		}; \
		docker compose logs -f tests & \
		TEST_LOGS_PID=$$!; \
		docker compose wait tests; \
		TEST_EXIT_CODE=$$?; \
		kill $$TEST_LOGS_PID 2>/dev/null || true; \
		exit $$TEST_EXIT_CODE; \
	)
endif

# --- Миграции базы данных ---
migrate-up:
	# Применить все новые миграции
	go run ./cmd/migrate -up

migrate-down:
	# Откатить последнюю миграцию
	go run ./cmd/migrate -down

migrate-create:
	# Создать новую миграцию (запросит имя)
	@read -p "Enter migration name: " name; \
	migrate create -ext sql -dir db/migrations -seq $$name

connect-db:
	# Подключение к БД по параметрам из .env
	PGPASSWORD=${DB_PASSWORD} psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME}

# --- Swagger ---
generate-swagger:
	# Генерация swagger-документации
	@echo "Generating Swagger for all packages..."; \
	swag init -q -g cmd/service/main.go -pd -o ./docs/http/all --outputTypes yaml \

	@for tag in auth profile messaging media catalog push; do \
		echo "Generating Swagger for $$tag..."; \
		swag init -q -pd -o ./docs/http/$$tag --outputTypes yaml --tags $$tag -g cmd/service/main.go; \
	done

prepare-env-vars:
	# Копируем пример .env в рабочий .env
	cp .env.debug .env
	@bash -c ' \
		ABS_CERT_PATH=$$(cd certs/ca && pwd)/ca.crt; \
		echo "Updating .env with SSL_CERT_FILE=$$ABS_CERT_PATH"; \
		if grep -q "^SSL_CERT_FILE=" .env; then \
			sed -i.bak "s|^SSL_CERT_FILE=.*|SSL_CERT_FILE=$$ABS_CERT_PATH|" .env && rm .env.bak; \
		else \
			echo "SSL_CERT_FILE=$$ABS_CERT_PATH" >> .env; \
		fi \
	'
	@bash -c ' \
		ABS_FIREBASE_KEY_PATH=$$(cd secrets && pwd)/firebase.json; \
		echo "Updating .env with GOOGLE_APPLICATION_CREDENTIALS=$$ABS_FIREBASE_KEY_PATH"; \
		if grep -q "^GOOGLE_APPLICATION_CREDENTIALS=" .env; then \
			sed -i.bak "s|^GOOGLE_APPLICATION_CREDENTIALS=.*|GOOGLE_APPLICATION_CREDENTIALS=$$ABS_FIREBASE_KEY_PATH|" .env && rm .env.bak; \
		else \
			echo "GOOGLE_APPLICATION_CREDENTIALS=$$ABS_FIREBASE_KEY_PATH" >> .env; \
		fi \
	'

# --- Подготовка окружения для отладки ---
prepare-debug-env: generate-local-ca prepare-env-vars

start-debug-env: prepare-debug-env
	# Запуск всех сервисов кроме приложения для отладки
	@echo "Starting services except app...";
	@echo "Press Ctrl+C to stop the debug environment";
	@trap 'docker compose --profile debug down -v --remove-orphans; exit' INT; \
	docker compose --profile debug up -d --build --force-recreate && \
	docker compose wait minio-init && \
	$(MAKE) migrate-up && \
	echo "✅ \033[1;32mДебаг-окружение готово! Теперь можно запустить сервис (например, make run-debug) и подебажить. Нажмите CTRL + C, чтобы остановить окружение.\033[0m"; \
	while true; do sleep 1; done

.PHONY: prepare-debug-env start-debug-env

# --- Генерация локального CA и сертификатов для MinIO ---
generate-local-ca:
	@echo "🔧 \033[1;34mГенерируем CA и серверные сертификаты...\033[0m"
	# Создаём директории для CA и MinIO сертификатов
	mkdir -p certs/ca certs/minio certs/android
	# Генерируем приватный ключ CA, если не существует
	@if [ ! -f certs/ca/ca.key ]; then \
		openssl genrsa -out certs/ca/ca.key 4096; \
	else \
		echo "CA private key already exists, skipping..."; \
	fi
	# Генерируем самоподписанный CA сертификат, если не существует
	@if [ ! -f certs/ca/ca.crt ]; then \
		openssl req -x509 -new -nodes -key certs/ca/ca.key -sha256 -days 3650 -out certs/ca/ca.crt -subj "/C=RU/ST=Local/L=Local/O=Local CA/CN=Local CA"; \
	else \
		echo "CA certificate already exists, skipping..."; \
	fi
	# Генерируем приватный ключ для MinIO, если не существует
	@if [ ! -f certs/minio/private.key ]; then \
		openssl genrsa -out certs/minio/private.key 4096; \
	else \
		echo "MinIO private key already exists, skipping..."; \
	fi
	# Все команды выполняем в одном блоке shell
	@( \
		DOCKER_HOST_IP=127.0.0.1; \
		echo "Using DOCKER_HOST_IP=$$DOCKER_HOST_IP"; \
		cat certs/minio/openssl.cnf.template | sed "s/{{DOCKER_HOST_IP}}/$$DOCKER_HOST_IP/g" > certs/minio/openssl.cnf; \
		if [ ! -f certs/minio/minio.csr ]; then \
			openssl req -new -key certs/minio/private.key -out certs/minio/minio.csr -subj "/C=RU/ST=Local/L=Local/O=MinIO/CN=minio" -config certs/minio/openssl.cnf; \
		else \
			echo "MinIO CSR already exists, skipping..."; \
		fi; \
		if [ ! -f certs/minio/public.crt ]; then \
			openssl x509 -req -in certs/minio/minio.csr -CA certs/ca/ca.crt -CAkey certs/ca/ca.key -CAcreateserial -out certs/minio/public.crt -days 3650 -sha256 -extensions v3_req -extfile certs/minio/openssl.cnf; \
		else \
			echo "MinIO certificate already exists, skipping..."; \
		fi; \
	)
	# Генерируем DER-файлы для Android, если не существуют
	@if [ ! -f certs/android/ca.der ]; then \
		openssl x509 -in certs/ca/ca.crt -out certs/android/ca.der -outform DER; \
	else \
		echo "Android CA DER already exists, skipping..."; \
	fi
	@echo "✅ \033[1;32mГотово! CA, серверные и Android DER сертификаты лежат в certs/ca, certs/minio и certs/android\033[0m"


# --- Установка сертификатор в Android эмулятор ---
install-ca-android:
	@echo "🔧 \033[1;34mУстанавливаем CA сертификат в Android эмулятор...\033[0m"
	adb root
	adb remount
	# Получаем hash и переименовываем файл локально
	@HASH=$$(openssl x509 -inform DER -subject_hash_old -in certs/android/ca.der | head -1); \
	cp certs/android/ca.der certs/android/$$HASH.0; \
	adb push certs/android/$$HASH.0 /system/etc/security/cacerts/$$HASH.0
	adb shell 'chmod 644 /system/etc/security/cacerts/*.0'
	adb reboot
	@echo "✅ \033[1;32mГотово! CA сертификат установлен в Android эмулятор\033[0m"

# --- Запуск Github Actions локально через act ---
run-gh-actions:
	@if [ ! -S /var/run/docker.sock ]; then \
		echo "❌ \033[1;31mНе найден /var/run/docker.sock\033[0m"; \
		echo "Если вы используете Colima, создайте симлинк командой:"; \
		echo "  \033[1;33msudo ln -s ~/.colima/default/docker.sock /var/run/docker.sock\033[0m"; \
		echo "Если вы используете Docker Desktop, откройте Docker Desktop → Settings → Advanced и отключите, затем снова включите опцию 'Use the default socket path'.\n"; \
		exit 1; \
	fi; \
	act -j integration-tests --container-architecture linux/amd64