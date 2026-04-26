# Node System 完善方案（2026-04-26）

## 目标

把节点系统从“可搭图”推进到“可编程、可复用、可维护”的工程化阶段。

## 现状核对（基于当前代码）

已存在但无需重复排期：

- `geometry.primitives.torus`（Torus）
- `geometry.primitives.icosahedron`（Icosahedron）
- `geometry.curves.evaluate_curve`（Curve Evaluate）
- `geometry.curves.curve_frame_along_path`（已有沿路径帧能力，偏批量输出）
- `math.logic.if`（值选择，不是执行分支）

当前真实缺口：

- 缺少执行流语义：无执行端口类型、无执行路径调度、无循环语义
- 缺少变量作用域体系：无图级/帧级变量容器，无持久化变量桥接
- 缺少子图封装机制：`Group` 仅视觉分组，无输入输出契约与复用能力

关键架构约束：

- `NodeExecutor` 目前是静态拓扑排序执行；遇到环直接失败
- 现有系统是数据流模型，不支持“执行流路由”

这意味着：`Branch/ForEach/While/Sequence` 不能只加节点类，必须先补执行模型能力。

## 优先级重排（建议）

### P0-A：执行模型前置（必须先做）

1. 新增执行端口类型（建议 `exec`）与连接规则
2. 执行调度改造：
   - 数据输入求值 + 执行触发分离
   - 支持同一图多次触发节点执行
3. 运行保护：
   - `maxIterations`
   - `maxExecTimeMs`
   - 循环断路与错误上报
4. 调试可视化：当前执行路径高亮、循环计数、最近错误

验收标准：

- 可构建并运行“有执行分支、无数据环”的图
- 循环图不再被拓扑排序直接拒绝，而是按执行语义运行
- 无限循环可被稳定中断并给出可读错误

### P0-B：最小流程控制节点集

1. `flow.control.branch`
2. `flow.control.sequence`
3. `flow.loop.for_each`
4. `flow.loop.accumulator`
5. `flow.control.do_once`

不建议在此批加入：

- `flow.loop.while`（风险高，依赖运行时保护与终止条件质量）

验收标准：

- 复杂写世界流程可通过 `Sequence` 保证顺序确定性
- 列表处理可不依赖“手工复制节点链”
- 可用 `Do Once` 抑制重复副作用

### P1：变量系统（先轻后重）

第一批（MVP）：

1. `variable.set`
2. `variable.get`
3. `variable.list`
4. `variable.frame_local`（局部作用域）

第二批（增强）：

1. `variable.persistent`（跨执行持久化）

持久化注意项：

- 存档版本兼容
- 多人/服务端一致性
- 回放与撤销语义

### P1：子图与工程化

1. `utilities.organization.graph_io`
2. `utilities.organization.subgraph`
3. `utilities.organization.preset`

验收标准：

- 任意子图可定义输入输出契约
- 子图实例可复用并支持版本更新
- 大图可显著降低连线密度

## 快赢层（可并行推进）

这些节点实现成本低、收益高，建议穿插进入每个迭代：

1. `math.trigonometry.atan2`
2. `math.scalar_math.sqrt`
3. `math.scalar_math.lerp`
4. `math.scalar_math.int_divide`
5. `math.list_sequence.zip`
6. `math.list_sequence.deduplicate`
7. `reference.points.lerp_points`
8. `reference.points.polar_point`

## 暂缓项（放到 P2）

在核心编程能力补齐前，不建议优先投入：

- 大批新几何原语（`capsule/tube/arch/staircase` 等）
- 高复杂事件与 NBT 写回（`on_block_break` / `set_block_nbt`）
- `while` 与 flood fill 同批上线

理由：会放大调试复杂度，但不能解决“图可维护性”根因。

## 推荐三次迭代落地包

迭代 1（2 周）：

- 执行端口与调度基础
- `Branch` / `Sequence` / `Do Once`
- `atan2` / `sqrt` / `lerp`

迭代 2（2 周）：

- `ForEach` / `Accumulator`
- `variable.set/get/list`
- `zip` / `deduplicate` / `int_divide`

迭代 3（2-3 周）：

- `graph_io` / `subgraph`
- `frame_local` + 初版 `preset`
- `polar_point` / `lerp_points`

## 结论

你的原判断方向是对的，且优先级应聚焦在“编程基础设施”而不是“继续加原语”。

推荐最终顺序：

1. 执行模型前置
2. Flow Control
3. Variable（MVP）
4. Subgraph/Graph IO
5. 快赢数学与参考节点
6. Persistent/Event/NBT/复杂几何
