.PHONY: test build build-gui build-cli build-server clean reset server gui gui-test gui-server gui-test-server cli cli-test cli-server cli-test-server all all-test installer

# Requires: Java 23+, Gradle 9.5+
VERSION := $(shell cat VERSION)

# Build
build:
	./gradlew allJars
	cp build/libs/trak-server-$(VERSION).jar trak-server
	cp build/libs/trak-cli-$(VERSION).jar trak-cli
	cp build/libs/trak-gui-$(VERSION).jar trak-gui
	chmod +x trak-server trak-cli trak-gui

build-gui:
	./gradlew trak-gui --rerun-tasks
	cp build/libs/trak-gui-$(VERSION).jar trak-gui
	chmod +x trak-gui

build-cli:
	./gradlew trak-cli
	cp build/libs/trak-cli-$(VERSION).jar trak-cli
	chmod +x trak-cli

build-server:
	./gradlew trak-server
	cp build/libs/trak-server-$(VERSION).jar trak-server
	chmod +x trak-server

test:
	./gradlew cleanTest test

clean:
	./gradlew clean
	rm -f trak-server trak-cli trak-gui

reset: clean
	rm -rf .store .cache

# macOS native full-screen requires these module opens
GUI_JVM_ARGS = --add-opens java.desktop/com.apple.eawt=ALL-UNNAMED --add-opens java.desktop/com.apple.eawt.event=ALL-UNNAMED

# Server
server: build-server
	java -jar trak-server

# GUI
gui: build-gui
	java $(GUI_JVM_ARGS) -jar trak-gui --local

gui-test: build-gui
	java $(GUI_JVM_ARGS) -jar trak-gui --local --test

gui-server: build-gui
	java $(GUI_JVM_ARGS) -jar trak-gui --remote

gui-test-server: build-gui
	java $(GUI_JVM_ARGS) -jar trak-gui --remote --test

# CLI
cli: build
	@echo "Usage: java -jar trak-cli <command>"
	@echo "Example: java -jar trak-cli info"

cli-test: build
	java -jar trak-cli info

# CLI (remote, needs server running)
cli-server: build
	@echo "Usage: java -jar trak-cli --remote <command>"
	@echo "Example: java -jar trak-cli --remote info"

cli-test-server: build
	java -jar trak-cli --remote info

# All (local with test data)
all-test: test build
	@echo ""
	@echo "Built 3 executables (local + test data):"
	@echo "  make server          # start REST server"
	@echo "  make gui-test        # GUI with test data (local)"
	@echo "  make gui-test-server # GUI with test data (remote)"
	@echo "  make cli             # CLI (local)"

# Native installer (requires jpackage / JDK 16+)
installer:
	./gradlew jpackage-gui
	@echo "Installer written to build/installer/"

# All (local, no test data)
all: test build
	@echo ""
	@echo "Built 3 executables:"
	@echo "  make server          # start REST server"
	@echo "  make gui             # GUI (local)"
	@echo "  make gui-server      # GUI (remote)"
	@echo "  make cli             # CLI (local)"
