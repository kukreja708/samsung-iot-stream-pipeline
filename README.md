# High-Throughput Reactive IoT Ingestion & Stream Processing Pipeline

A full-stack, distributed event-streaming system engineered to simulate, ingest, buffer, process, and permanently archive high-frequency telemetry metrics from a smart appliance fleet.

## System Architecture

```text
 [Internal Java Simulator] ──── High Throughput HTTP ────► [Spring WebFlux API] (Port 8080)
                                                                │
                                                                ▼ (Asynchronous Buffer)
                                                           [Apache Kafka] (Docker Port 9092)
                                                                │
                                                                ▼ (Fault-Tolerant Consumer)
 [React Analytics Dashboard] ◄── Server-Sent Events ──── [TimescaleDB] (Daily Hypertables)
```

## Architectural Highlights & Engineering Trade-Offs

### 1. Ingestion Tier (Reactive WebFlux Gateways)
* **Non-Blocking Execution:** Leveraged **Spring WebFlux (Netty)** to accept concurrent HTTP POST device telemetry payloads asynchronously. 
* **Resource Optimization:** Avoided standard Spring MVC thread-per-request blocking to fully safeguard the gateway node from thread starvation during sudden network write spikes.

### 2. Message Buffering (Apache Kafka Partitioning Key Strategy)
* **Time-Series Ordering:** Implemented a distributed partition routing strategy utilizing the unique `deviceId` as the explicit **Kafka Partition Key**. 
* **Hotspot Mitigation:** This ensures mathematical data uniformity across the multi-node broker cluster while locking a single appliance's chronological timeline to one partition to prevent out-of-order chart rendering.

### 3. Fault-Tolerant Consumption & Storage Optimization
* **Manual Offset Control:** Configured **Spring Data R2DBC** and switched Kafka to **Manual Acknowledgment Mode (`AckMode.MANUAL`)**, binding event offset commits strictly to successful database disk confirmations.
* **Chaos Resilience:** Integrated a **DefaultErrorHandler backoff policy** (3 attempts with 2-second backoff loops) to prevent head-of-line blocking during database maintenance windows.
* **Storage Chunking:** Utilized **TimescaleDB Hypertables with 1-Day Chunk Intervals** to optimize write-cache RAM footprint; enforced automated data retention policies to drop expired chunks at the file-system layer without running slow row-by-row SQL delete queries.

### 4. Real-Time UI Streaming (Server-Sent Events)
* Developed an executive telemetry console using **React and Recharts**, leveraging single-direction **Server-Sent Events (SSE / EventSource)** to stream live analytics and anomaly alerts over regular HTTP pipes without client-side polling overhead.

## Infrastructure Deployment
Ensure Docker Desktop is active on your host machine, then launch the infrastructure stack directly from the repository root:
```bash
docker compose up -d
```
