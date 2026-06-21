# Execution Flow Control (P0)

## Review conclusion (2026-06-21)

The comprehensive review is **directionally correct**: NodeCraft is still primarily a **dataflow** engine. Flow nodes exist, but they route **values**, not **execution**.

| Review claim | Actual state |
|--------------|--------------|
| No exec port type | **Fixed (P0-A slice 1)** — `NodeDataType.EXEC` |
| Static topo only | **Still default** for graphs without exec wires |
| Branch/While cannot skip branches | **Still true for dataflow nodes**; exec scheduler is the path to fix this |
| Cycles fail immediately | **Data cycles still fail**; exec cycles are bounded by `ExecutionRunGuard` |

Existing flow nodes (`flow.control.branch`, `flow.control.sequence`, `flow.loop.*`) remain **dataflow helpers**. Their `@NodeInfo` descriptions already say Branch does not skip downstream nodes.

Reference roadmap: `docs/node-system-完善版路线图-2026-04-26.md` (P0-A / P0-B).

---

## Architecture: two scheduling modes

### 1. Dataflow mode (default)

Used when the graph has **no** `exec` port connections.

- `GraphExecutionPlanner` topo-sorts **data edges only**
- Each node runs **at most once**
- `flow.control.branch` clears the unselected output to `null`, but **both downstream nodes still run**

### 2. Exec-flow mode

Activated automatically when at least one `exec → exec` connection exists.

- **Entry nodes**: nodes with no incoming exec wires
- **Frontier queue**: after a node completes, enqueue its exec successors
- **Lazy data pull**: before running a node, recursively evaluate data upstream on demand
- Nodes outside the exec frontier are **not executed** unless pulled as data dependencies
- **Runaway protection**: `ExecutionRunGuard` enforces `maxSteps` + `maxDurationMs`

---

## Implemented in P0-A (slice 1)

| Component | Role |
|-----------|------|
| `NodeDataType.EXEC` | Execution-control port type |
| `TypeConversionRegistry` | `exec` connects only to `exec` |
| `ExecutionPortKind` | Classifies data vs exec connections |
| `ExecutionFlowGraph` | Entry nodes + exec adjacency |
| `ExecutionRunGuard` / `ExecutionRunLimits` | Step/time circuit breaker |
| `NodeExecutor` | Chooses dataflow vs exec-flow; guard in both modes |
| `GraphExecutionPlanner` / dirty propagation | Ignore exec edges for data topo |

---

## Not done yet (P0-A slice 2 / P0-B)

1. **Exec ports on flow nodes** — e.g. `Branch` fires `exec_true` OR `exec_false`, not both data outputs
2. **Conditional exec routing** — scheduler follows one exec pin based on node result
3. **Multi-trigger / loop bodies** — `ForEach` / `While` as exec drivers, not list expanders only
4. **Editor UX** — white exec wires, entry-node markers, live exec highlight
5. **Partial exec + exec mode** — reconcile incremental cache with repeated exec visits

Until slice 2 lands, use:

- `math.logic.if` for **value** selection
- `flow.control.do_once` + `ExecutionContext` variables for **once-per-run** side effects
- `ExecutionRunLimits` tightening for stress tests

---

## Usage notes

### Connecting exec ports

Only `exec → exec` is allowed. Data ports cannot mix with exec ports on the same edge.

### Guard defaults

```java
ExecutionRunLimits.defaults()
// maxSteps = 100_000, maxDurationMs = 30_000
```

Pass custom limits to `NodeExecutor`:

```java
new NodeExecutor(graph, context, null, IncrementalExecutionOptions.defaults(), new ExecutionRunLimits(5L, 1_000L));
```

### Tests

- `ExecFlowExecutorTest` — skip off-frontier nodes, lazy data pull, cycle guard
- `ExecutionFlowGraphTest` — topology analysis
- `FlowControlNodeTest.branchDoesNotSkipEitherDownstreamNodeInExecutor` — documents dataflow limitation

---

## Migration strategy

1. **Existing graphs**: unchanged (no exec wires → dataflow mode)
2. **New control-heavy graphs**: add exec wires incrementally; start from `output.execute.*` entry nodes
3. **Flow node upgrade**: add optional exec outputs alongside legacy data outputs; deprecate data-only routing later
