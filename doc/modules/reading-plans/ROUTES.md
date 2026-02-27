# Reading Plans — Routes & Navigation

> Inter-module communication via Decompose navigation, Verse Bus, and deep links.

---

## 1. Exposed Configurations

| Config | Parameters | Description |
|--------|-----------|-------------|
| `ReadingPlansConfig.Main` | — | Active plan / browser |
| `ReadingPlansConfig.Plan(id)` | `planId: Long` | Specific plan detail |

---

## 2. Consumed Configurations

| Target module | Config | Context |
|--------------|--------|---------|
| `bible-reader` | via VerseBus | Open daily reading passage |

---

## 3. Deep Links

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://reading-plans` | — | Active plan or browser |
| `biblestudio://reading-plans/{id}` | `biblestudio://reading-plans/1` | Plan detail |

---

## 4. Verse Bus (LinkEvent)

| Role | Description |
|------|-------------|
| **Publisher** | Publishes `PassageSelected` when user taps a reading passage |
