# k-opc

Kotlin Multiplatform OPC UA client (`client/`), a Ktor backend exposing it over REST/WebSocket (`server/`), and a Compose Multiplatform desktop UI (`desktop/`).

## Running the application

The desktop app talks to the OPC UA server only through the `:server` backend — it never connects directly. Start things in this order:

1. **An OPC UA server to connect to** (e.g. the `opc-plc` simulator used in the integration tests):
   ```bash
   podman run -d --name opc-plc --hostname localhost -p 50000:50000 \
     mcr.microsoft.com/iotedge/opc-plc:2.14.22 \
     --unsecuretransport --autoaccept --trustowncert --pn=50000
   ```
   (`--unsecuretransport` is required for the `None` security policy / anonymous connections to be available at all — without it, connecting with `None` fails with `no endpoint selected`.)

2. **The `:server` backend:**
   ```bash
   ./gradlew :server:run
   ```
   Runs on `http://localhost:8080`. `GET /health` should return `ok`.

3. **The `:desktop` app:**
   ```bash
   ./gradlew :desktop:run
   ```
   If it can't reach `:server`, the UI shows `Cannot reach k-opc server at http://localhost:8080. Is :server:run running?` instead of a raw exception.
