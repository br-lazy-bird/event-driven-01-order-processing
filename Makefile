# Docker compose configuration
DOCKER_DIR = docker
COMPOSE_FILE = $(DOCKER_DIR)/compose.yaml
COMPOSE_TEST_FILE = $(DOCKER_DIR)/compose.test.yaml
ENV_FILE = .env
POSTGRES_DB=order_processing
POSTGRES_USER=lazybird_dev

.PHONY: help db-shell build run run-with-logs stop logs clean test test-build

help:
	@echo "Order Processing System - Available Commands:"
	@echo ""
	@echo "  make help            - Show this help message"
	@echo "  make db-shell        - Open PostgreSQL shell (requires running database)"
	@echo "  make build           - Build and start all services"
	@echo "  make run             - Start services (use cached images)"
	@echo "  make run-with-logs   - Start services and follow logs"
	@echo "  make stop            - Stop all services"
	@echo "  make logs            - Show service logs"
	@echo "  make clean           - Remove containers, volumes, and images"
	@echo "  make test            - Run E2E tests (uses cached images)"
	@echo "  make test-build      - Rebuild and run E2E tests"
	@echo ""

db-shell:
	@echo "Opening PostgreSQL shell..."
	docker compose -f $(COMPOSE_FILE) --env-file $(ENV_FILE) exec db psql -U $(POSTGRES_USER) -d $(POSTGRES_DB)

build:
	@echo "Building and starting services..."
	docker compose -f $(COMPOSE_FILE) --env-file $(ENV_FILE) up --build -d

build-with-logs:
	@echo "Building and starting services with logs..."
	docker compose -f $(COMPOSE_FILE) --env-file $(ENV_FILE) up --build

run:
	@echo "Starting services..."
	docker compose -f $(COMPOSE_FILE) --env-file $(ENV_FILE) up -d

run-with-logs:
	@echo "Starting services and following logs..."
	docker compose -f $(COMPOSE_FILE) --env-file $(ENV_FILE) up

stop:
	@echo "Stopping services..."
	docker compose -f $(COMPOSE_FILE) --env-file $(ENV_FILE) down

logs:
	@echo "Showing service logs..."
	docker compose -f $(COMPOSE_FILE) --env-file $(ENV_FILE) logs -f

clean:
	@echo "Cleaning up containers, volumes, and images..."
	docker compose -f $(COMPOSE_FILE) --env-file $(ENV_FILE) down -v --rmi all --remove-orphans
	docker compose -f $(COMPOSE_TEST_FILE) down -v --rmi all --remove-orphans

test:
	@echo "Running E2E tests..."
	@docker compose -f $(COMPOSE_TEST_FILE) up --abort-on-container-exit --exit-code-from test-runner 2>&1 | grep -E "test-runner|Tests run|SUCCESS|FAILURE|E2E TEST|ERROR|Bug confirmed|===|---"
	@echo "Cleaning up test containers..."
	@docker compose -f $(COMPOSE_TEST_FILE) down > /dev/null 2>&1

test-build:
	@echo "Building test images and running E2E tests..."
	@docker compose -f $(COMPOSE_TEST_FILE) up --build --abort-on-container-exit --exit-code-from test-runner 2>&1 | grep -E "test-runner|Tests run|SUCCESS|FAILURE|E2E TEST|ERROR|Bug confirmed|===|---"
	@echo "Cleaning up test containers..."
	@docker compose -f $(COMPOSE_TEST_FILE) down > /dev/null 2>&1
