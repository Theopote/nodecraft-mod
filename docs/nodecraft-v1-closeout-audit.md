# NodeCraft v1 Closeout Audit

Last updated: 2026-04-11

## 1. Mainline Scope

This audit follows the committed v1.0 taxonomy only.

In-scope:

- architectural form-making
- geometric modeling
- world-grounded construction

Out of scope for the v1 mainline:

- `animation.*`
- `flora.*`
- `world.nbt.*`
- `world.inventory.*`
- experimental nodes

These domains may still exist in source, but they are not evidence that the v1 mainline migration is unfinished.

## 2. Mainline Status

The following v1 domains are now structurally closed as canonical implementation paths:

- `input`
- `reference`
- `geometry`
- `transform`
- `pattern`
- `material.basic_assignment`
- `world.read`
- `world.query`
- `world.selection`
- `world.write`
- `output`
- `math.scalar_math`
- `math.compare`
- `math.random`
- `math.trigonometry`
- `math.list_sequence`

Legacy compatibility is routed through:

- `spatial.legacy`

Explicitly deferred nodes are routed through:

- `deferred.*`

## 3. Math Closeout

The old math domains have been retired from active mainline use:

- `math.basic`
- `math.randomness`
- `math.vector`

Their canonical replacements are:

- `math.basic.* -> math.scalar_math.*`
- `math.basic.range -> math.list_sequence.range`
- `math.logic` compare-family -> `math.compare.*`
- `math.randomness.* -> math.random.*`
- `math.vector.construct -> reference.vectors.vector`
- `math.vector.construct_coordinate -> reference.points.point_from_coordinates`

`MathSeriesNode` was not promoted into the v1 main tree.

- old id: `math.basic.series`
- current id: `deferred.math.math_series`

## 4. What Still Exists But Is Not Mainline Work

The repository still contains several other systems:

- `data.*`
- `utilities.*`
- `control.flow.*`
- `world.entity.*`
- `world.interaction.*`
- `spatial.sdf.*`

These require separate product and architecture decisions.
They should not be folded into the v1 taxonomy migration by default.

## 5. Practical Rule Going Forward

For any future node work:

1. If the node belongs to the committed v1 tree, place it directly in its canonical v1 category.
2. If it is intentionally postponed, place it under `deferred.*`.
3. If it exists only for backward compatibility with old graphs, route it through `spatial.legacy`.

## 6. Engineering Conclusion

For the original v1 refactor objective, the taxonomy migration is functionally complete for the scoped domains.

The next phase should focus on one of:

- compile and runtime verification
- graph serialization / alias compatibility validation
- separate scope definition for out-of-v1 systems such as `data`, `utilities`, `control`, `entity`, `interaction`, and `sdf`
