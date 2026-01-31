# Contributing to Kernx

First off, thank you for considering contributing to Kernx. 
We are building the world's fastest deterministic AI kernel. This requires **extreme discipline**.

**Kernx is NOT a standard Spring Boot application.** Practices that are "fine" in enterprise Java (blocking, locking, reflection) are **banned** here.

---

## 🛠 Development Setup

### Prerequisites
* **Java 25** (OpenJDK / GraalVM) with `--enable-preview`.
* **Maven 3.8+**
* **Python 3.8+** (for SDK changes)

### Build Command
```bash
mvn clean package -DskipTests
```

### Running Tests
You must pass the WidowMaker stress test before submitting.

```bash
mvn test
```

If your change drops throughput below 20k req/sec on local hardware, it is a regression.

---

## ⚡ The 3 Golden Rules (Read Before Coding)

If your Pull Request violates these rules, it will be closed instantly.

### 1. NO BLOCKING (The "Async" Rule)
You must **never** block a Virtual Thread inside the Core Engine.
* ❌ **Illegal:** `Thread.sleep()`, `future.get()`, `socket.read()`, `db.query()`
* ✅ **Legal:** `CompletableFuture.thenAccept()`, Non-blocking I/O (`NIO`), Asynchronous message passing.

### 2. NO LOCKS (The "Single Writer" Rule)
We use the **Single Writer Principle** to avoid contention.
* ❌ **Illegal:** `synchronized`, `ReentrantLock`, `AtomicInteger` (in hot paths), `ConcurrentHashMap` (in actor state).
* ✅ **Legal:** `MPSC Queues` (JCTools), Thread Confinement (Local Variables).
* **Concept:** If you need to share data, send a `KernxPacket`. Do not share memory.

### 3. ZERO ALLOCATION (The "GC" Rule)
The "Hot Path" (Dispatcher -> Queue -> Actor) must generate **zero garbage**.
* ❌ **Illegal:** `new String()`, `stream().filter().collect()`, creating Iterators in loops.
* ✅ **Legal:** Primitive arrays, Reusable objects, `static final` constants.

---



## 🐍 Python SDK Contributions

The Python client (`sdk/python`) must remain zero-dependency (other than `requests`).

* Do not add heavy libraries (e.g., `pandas`, `numpy`) to the core client.
* Ensure typing compatibility with Python 3.8+.

---

## ⚖️ Legal: The "Keep Your Copyright" CLA

We use a lightweight **Contributor License Agreement (CLA)** similar to the Apache Software Foundation.

### 1. You Keep Ownership

You retain full copyright and ownership of your contributions. You are simply giving us permission to use and distribute them.

### 2. The Grant

By submitting a Pull Request, you grant **Kernx Inc.** a perpetual, worldwide, non-exclusive, no-charge, royalty-free, irrevocable license to reproduce, prepare derivative works of, publicly display, publicly perform, sublicense, and distribute your contribution.

### 3. The Promise

You certify that:
* You wrote the code (or have the right to submit it).
* It does not violate any third-party patents or copyrights.

### Note on DCO

We also respect the **Developer Certificate of Origin (DCO)**. Please sign off your commits (`git commit -s`) to confirm you have the right to submit this code.