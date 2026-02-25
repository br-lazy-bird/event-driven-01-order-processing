# Order Processing System - Makefile
# Commands are populated incrementally as features are implemented

.PHONY: help db-shell

help:
	@echo "Order Processing System - Available Commands:"
	@echo ""
	@echo "  make help       - Show this help message"
	@echo "  make db-shell   - Open PostgreSQL shell (requires running database)"
	@echo ""
	@echo "More commands will be added as the system is built"

db-shell:
	@docker exec -it order_processing_db psql -U $(POSTGRES_USER) -d $(POSTGRES_DB)
