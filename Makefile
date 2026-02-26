# Docker compose configuration
DOCKER_DIR = docker
COMPOSE_FILE = $(DOCKER_DIR)/compose.yaml
ENV_FILE = .env
POSTGRES_DB=order_processing
POSTGRES_USER=lazybird_dev

.PHONY: help db-shell build run stop logs clean

help:
	@echo "Order Processing System - Available Commands:"
	@echo ""
	@echo "  make help       - Show this help message"
	@echo "  make db-shell   - Open PostgreSQL shell (requires running database)"
	@echo "  make build      - Build and start all services"
	@echo "  make run        - Start services (use cached images)"
	@echo "  make stop       - Stop all services"
	@echo "  make logs       - Show service logs"
	@echo "  make clean      - Remove containers, volumes, and images"
	@echo ""
	@echo "More commands will be added as the system is built"

db-shell:
	@echo "Opening PostgreSQL shell..."
	docker compose -f $(COMPOSE_FILE) --env-file $(ENV_FILE) exec db psql -U $(POSTGRES_USER) -d $(POSTGRES_DB)

build:
	@echo "Building and starting services..."
	docker compose -f $(COMPOSE_FILE) --env-file $(ENV_FILE) up --build -d

run:
	@echo "Starting services..."
	docker compose -f $(COMPOSE_FILE) --env-file $(ENV_FILE) up -d

stop:
	@echo "Stopping services..."
	docker compose -f $(COMPOSE_FILE) --env-file $(ENV_FILE) down

logs:
	@echo "Showing service logs..."
	docker compose -f $(COMPOSE_FILE) --env-file $(ENV_FILE) logs -f

clean:
	@echo "Cleaning up containers, volumes, and images..."
	docker compose -f $(COMPOSE_FILE) --env-file $(ENV_FILE) down -v --rmi all --remove-orphans
