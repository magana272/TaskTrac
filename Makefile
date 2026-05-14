.PHONY: test build build-gui build-cli build-server clean reset server gui gui-test gui-server gui-test-server cli cli-test cli-server cli-test-server all all-test

# ── Build ────────────────────────────────────────────────
build:
	./gradlew allJars
	cp build/libs/trak-server-0.1.0.jar trak-server
	cp build/libs/trak-cli-0.1.0.jar trak-cli
	cp build/libs/trak-gui-0.1.0.jar trak-gui
	chmod +x trak-server trak-cli trak-gui

build-gui:
	./gradlew trak-gui
	cp build/libs/trak-gui-0.1.0.jar trak-gui
	chmod +x trak-gui

build-cli:
	./gradlew trak-cli
	cp build/libs/trak-cli-0.1.0.jar trak-cli
	chmod +x trak-cli

build-server:
	./gradlew trak-server
	cp build/libs/trak-server-0.1.0.jar trak-server
	chmod +x trak-server

test:
	./gradlew cleanTest test

clean:
	./gradlew clean
	rm -f trak-server trak-cli trak-gui

reset: clean
	rm -rf .store .cache

cli: build
	@echo "Usage: java -jar trak-cli <command>"
	@echo "Example: java -jar trak-cli info"

cli-test: build
	java -jar trak-cli info

# ── CLI (remote, needs server running) ───────────────────
cli-server: build
	@echo "Usage: java -jar trak-cli --remote <command>"
	@echo "Example: java -jar trak-cli --remote info"

cli-test-server: build
	java -jar trak-cli --remote info

# ── All (local with test data) ───────────────────────────
all-test: test build
	@echo ""
	@echo "Built 3 executables (local + test data):"
	@echo "  make server          # start REST server"
	@echo "  make gui-test        # GUI with test data (local)"
	@echo "  make gui-test-server # GUI with test data (remote)"
	@echo "  make cli             # CLI (local)"

# ── All (local, no test data) ────────────────────────────
all: test build
	@echo ""
	@echo "Built 3 executables:"
	@echo "  make server          # start REST server"
	@echo "  make gui             # GUI (local)"
	@echo "  make gui-server      # GUI (remote)"
	@echo "  make cli             # CLI (local)"
