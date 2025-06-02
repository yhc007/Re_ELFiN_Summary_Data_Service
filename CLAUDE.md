# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Common Commands

**IMPORTANT: Use Java 11 for compatibility**
```bash
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@11/11.0.27/libexec/openjdk.jdk/Contents/Home
```

### Running the Application
```bash
# Start first node
sbt -Dconfig.resource=local1.conf run

# Start second node (optional)
sbt -Dconfig.resource=local2.conf run

# Check service readiness
curl http://localhost:9101/ready
```

### Development
```bash
# Compile
sbt compile

# Run tests
sbt test

# Clean build artifacts
sbt clean

# Build Docker image
sbt docker:publishLocal
```

### Infrastructure
```bash
# Start local development infrastructure (PostgreSQL, Kafka, Zookeeper)
docker-compose up -d

# Stop infrastructure
docker-compose down
```

## Architecture Overview

This is a Scala-based microservice that processes machine summary data events through Kafka and persists them to PostgreSQL using Akka Cluster and Event Sourcing patterns.

### Core Components

1. **MachineEventConsumer** (`MachineEventConsumer.scala:28`): Kafka consumer that processes `SummaryDataUpdated` events from machine data service
2. **SummaryDataRepository** (`repository/`): Data access layer using Quill for PostgreSQL operations with upsert functionality
3. **Main** (`Main.scala:17`): Application entry point with dependency injection via MacWire

### Technology Stack

- **Akka Cluster**: Distributed actor system with cluster sharding
- **Akka HTTP**: REST API and health checks
- **Kafka**: Event streaming (consumes from machine-data-service topic)
- **PostgreSQL**: Primary database with async Quill driver
- **gRPC**: Protobuf-based messaging
- **MacWire**: Compile-time dependency injection

### Data Flow

1. Machine events published to Kafka topic by machine-data-service
2. MachineEventConsumer processes `SummaryDataUpdated` protobuf messages
3. Repository layer performs upsert operations on daily summary data
4. Akka Management provides health checks and cluster bootstrapping

### Configuration

- Environment-specific configs in `src/main/resources/` (local1.conf, local2.conf, etc.)
- Database connection via `QUILL_CTX_URL` environment variable
- Kafka settings in kafka-dev.conf and kafka.conf
- Cluster configuration for multi-node deployment

### Database Schema

SQL DDL scripts located in `ddl-scripts/create_tables.sql` for summary data tables.