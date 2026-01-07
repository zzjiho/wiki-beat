# Wikipedia Real-time Playground

> Real-time dashboard for Wikipedia edit data collection and analysis

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

Track Wikipedia edits around the world using the real-time Wikipedia API stream.
Real-time Wikipedia edit streaming with Kafka and Redis caching.

## Features

- **Real-time Edit Tracking** - Monitor Wikipedia edits across 300+ languages
- **Statistics Dashboard** - Per-minute, per-language, and total statistics
- **Popular Documents** - Top 10 most edited articles in the last 5 minutes
- **Vandalism Detection** - Track revert, undo, and rollback keywords
- **New Documents** - Monitor newly created articles in real-time

## Architecture

### Overview

```
Wikipedia SSE Stream
    ↓
Kafka Producer
    ↓
wiki.raw.edits Topic (16 partitions)
    ↓
Consumer → In-memory Aggregation
    ↓
@Scheduled (every 60s)
    ↓
wiki.stats.* Topics (8 topics)
    ↓
Consumers → Redis Cache
    ↓
REST API → React Dashboard
```

### Full System Architecture

```mermaid
graph TB
    subgraph "External"
        WIKI[Wikimedia EventStreams API<br/>SSE Connection]
    end

    subgraph "Data Ingestion Layer"
        API[StreamControlController<br/>POST /api/v1/stream/start]
        FACADE[WikiStreamFacade<br/>Stream Orchestration]
        WSS[WikiStreamService<br/>Async Processing]
        WSC[WikiStreamConnector<br/>SSE Connection Manager]
        WDP[WikiDataParser<br/>JSON Parsing]
        WEP_PROC[WikiEditProcessor<br/>Edit Data Processing]
    end

    subgraph "Kafka - Raw Data Layer"
        WEP[WikiEditProducerService<br/>Raw Data Producer]
        KAFKA1[(wiki.raw.edits<br/>16 Partitions<br/>Language-based)]
        REC[RawEditConsumerService<br/>Consumer]
    end

    subgraph "In-Memory Aggregation"
        SA[StatsAggregator<br/>ConcurrentHashMap<br/>60s Aggregation]
        SCHED[Scheduled<br/>Every 60s]
    end

    subgraph "Kafka - Stats Layer"
        SP[StatsProducerService<br/>Stats Producer]
        KAFKA2[(wiki.stats.minute<br/>24 Partitions)]
        KAFKA3[(wiki.stats.language<br/>8 Partitions)]
        KAFKA4[(wiki.stats.total<br/>1 Partition)]
        KAFKA5[(wiki.stats.popular<br/>1 Partition)]
        KAFKA6[(wiki.stats.newdocument<br/>1 Partition)]
        KAFKA7[(wiki.stats.vandalism<br/>1 Partition)]
        KAFKA8[(wiki.stats.size<br/>1 Partition)]
    end

    subgraph "Persistence Layer"
        MSC[MinuteStatsConsumer]
        LSC[LanguageStatsConsumer]
        TSC[TotalStatsConsumer]
        PDC[PopularDocumentConsumer]
        NDC[NewDocumentConsumer]
        VC[VandalismConsumer]
        SC[SizeStatsConsumer]
        RSS[RedisStatsService<br/>Single Source of Truth]
        REDIS[(Redis<br/>minute:* TTL 1h<br/>language:* TTL 2h<br/>total:* TTL 2h<br/>realtime:* TTL 10m)]
    end

    subgraph "API Layer"
        CTRL[BigDataStatsController<br/>Stats Query API]
        STATS_FACADE[BigDataStatsFacade<br/>Stats Orchestration]
        CLIENT[Client<br/>Dashboard]
    end

    WIKI -->|SSE Stream| API
    API --> FACADE
    FACADE --> WSS
    WSS --> WSC
    WSC -->|Line Data| WDP
    WDP -->|EditMessage| WEP_PROC
    WEP_PROC -->|EditMessage| WEP
    WEP -->|Produce| KAFKA1
    KAFKA1 -->|Consume| REC
    REC -->|aggregateEdit| SA
    SA -->|In-memory Aggregation| SCHED
    SCHED -->|Every 60s| SP
    SP -->|Produce| KAFKA2
    SP -->|Produce| KAFKA3
    SP -->|Produce| KAFKA4
    SP -->|Produce| KAFKA5
    SP -->|Produce| KAFKA6
    SP -->|Produce| KAFKA7
    SP -->|Produce| KAFKA8
    KAFKA2 -->|Consume| MSC
    KAFKA3 -->|Consume| LSC
    KAFKA4 -->|Consume| TSC
    KAFKA5 -->|Consume| PDC
    KAFKA6 -->|Consume| NDC
    KAFKA7 -->|Consume| VC
    KAFKA8 -->|Consume| SC
    MSC -->|saveMinuteStats| RSS
    LSC -->|saveLanguageStats| RSS
    TSC -->|saveTotalStats| RSS
    PDC -->|savePopularDocumentStats| RSS
    NDC -->|saveNewDocumentStats| RSS
    VC -->|saveVandalismStats| RSS
    SC -->|saveSizeStats| RSS
    RSS -->|Write| REDIS
    CLIENT -->|GET /api/v1/bigdata/*| CTRL
    CTRL --> STATS_FACADE
    STATS_FACADE -->|Query| RSS
    RSS -->|Read| REDIS
    REDIS -->|Response| CLIENT
```

### Tech Stack

**Backend**
- Java 21, Spring Boot 3.5
- Apache Kafka (message streaming)
- Redis (caching layer)
- Docker (infrastructure)

**Frontend**
- React 19, TypeScript
- Vite, TailwindCSS
- Recharts, React Query

## Quick Start

Get started in under 2 minutes with Docker:

```bash
# Download docker-compose file
curl -O https://raw.githubusercontent.com/zzjiho/wiki-beat/main/docker/docker-compose.quick.yml

# Start all services
docker compose -f docker-compose.quick.yml up -d

# Wait for services to start (~30 seconds), then start streaming
curl -X POST http://localhost:8888/api/v1/stream/start

# Open dashboard at http://localhost:5173
```

All required images (Kafka, Redis, backend, frontend) will be automatically downloaded from registries.

## Development Setup

For contributors who want to modify the code:

```bash
git clone https://github.com/zzjiho/wiki-beat.git
cd wiki-beat/docker

# Start infrastructure only (Kafka, Redis, monitoring tools)
docker compose -f docker-compose.local.yml up -d

# Run backend (new terminal)
cd ../wiki-back
./gradlew bootRun --args='--spring.profiles.active=dev'

# Run frontend (new terminal)
cd ../wiki-front
npm install && npm run dev

# Start streaming
curl -X POST http://localhost:8888/api/v1/stream/start

# Open dashboard at http://localhost:5173
```

## Kafka Topics

| Topic | Partitions | Interval | Description |
|-------|-----------|----------|-------------|
| `wiki.raw.edits` | 16 | Real-time | Individual edit events |
| `wiki.stats.minute` | 24 | 60s | Per-minute aggregated stats |
| `wiki.stats.language` | 8 | 60s | Language statistics |
| `wiki.stats.total` | 1 | 60s | Total statistics |
| `wiki.stats.popular` | 1 | 60s | Popular documents Top 10 |
| `wiki.stats.vandalism` | 1 | 60s | Vandalism detection |

## Contributing

Contributions are always welcome!

- Report bugs
- Suggest features
- Submit pull requests

**Ideas for contributors:**
- WebSocket real-time updates
- Time-based heatmap visualization
- Discord/Slack webhook integration
- Multi-language UI support

## License

MIT License - See [LICENSE](LICENSE) for details

## Acknowledgments

- [Wikimedia](https://www.wikimedia.org/) - For providing free real-time stream API
- Wikipedia editors - For creating the data

---

Star if you find it interesting! 
