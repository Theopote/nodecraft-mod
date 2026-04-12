# NodeCraft v1 Delete / Freeze Candidates

Last updated: 2026-04-12

## 1. Purpose

This document is the cleanup guide for the current refactor stage.

It exists to answer one question:

Which nodes and helper layers should be deleted, frozen, or kept so the v1 node system stays small, stable, and maintainable?

This is intentionally stricter than compatibility-oriented cleanup.

The project is still early enough that structural clarity is more valuable than carrying too much historical surface.

## 2. Decision Rule

Use the following rule set:

1. If a node belongs to the committed v1 modeling tree, keep it in canonical form only.
2. If a node is outside current v1 scope but still plausibly useful later, freeze it under `deferred.*`.
3. If a node exists only to load old graphs and has no product future, keep it only if backward compatibility is still a real requirement.
4. If a node is duplicated, transitional, or product-dead, delete it rather than preserving another permanent compatibility layer.

## 3. Recommended Actions

### 3.1 Keep

Keep these areas as real, supported code:

- canonical v1 mainline categories
- `utilities.assist`
- `utilities.organization`
- minimal `NodeRegistry` alias support
- graph save/load canonicalization
- clipboard migration support

### 3.2 Freeze

Freeze these as non-mainline systems:

- `deferred.math.*`
- `deferred.out_of_scope.*`
- `spatial.legacy`

Freezing means:

- no new feature growth
- no new taxonomy branching
- no UI emphasis
- no "temporary" parking-lot behavior

### 3.3 Delete

Delete nodes or helper layers when they are:

- duplicate implementations of an existing canonical node
- compatibility shells with no remaining unique responsibility
- out-of-scope systems that the product is not planning to bring back

## 4. Concrete Candidate List

## 4.1 Immediate Delete Candidates

These are the best first deletion targets if you want to aggressively simplify the codebase now.

### A. Compatibility registration shell if legacy point nodes are removed

Current files:

- [SpatialPointNodes.java](/f:/development/NC/nodecraft/src/main/java/com/nodecraft/nodesystem/nodes/utilities/legacy/spatial/points/SpatialPointNodes.java)

Recommendation:

- delete it once the remaining legacy point nodes are either deleted or formally retained elsewhere

Reason:

- it is only a legacy bootstrap shell
- it has no product-facing value
- long term it makes registration harder to reason about

## 4.2 Alias-Then-Delete Candidates

These should not keep separate implementations.

The correct pattern is:

- alias old id -> canonical id
- delete old implementation class

This has already been done for many migrated nodes, and should remain the standard.

Use this rule for any future duplicate discovered in:

- `math`
- `reference`
- `transform`
- `pattern`
- `output`

Example classes already following the right model:

- old branch/switch/compare utility implementations
- old vector construction variants
- old visualization nodes

## 4.3 Freeze-First Candidates

These are out-of-scope systems that can stay only if you explicitly want a cold-storage area.

Current files under:

- [deferred/out_of_scope](/f:/development/NC/nodecraft/src/main/java/com/nodecraft/nodesystem/nodes/deferred/out_of_scope)

Recommendation:

- keep only if you consciously want a deferred archive
- otherwise delete in batches

Suggested batches:

### A. Text processing batch

- `concatenate_text`
- `find_replace_text`
- `format_text`
- `join_text`
- `split_text`
- `text_length`
- `text_panel`
- `text_to_value`

Reason:

- not part of the v1 building/modeling/world workflow
- likely to remain peripheral even later

### B. File I/O batch

- `file_path`
- `load_graph`
- `save_graph`
- `read_data_file`
- `read_text_file`
- `write_data_file`
- `write_text_file`
- helper [SafeFilePathResolver.java](/f:/development/NC/nodecraft/src/main/java/com/nodecraft/nodesystem/nodes/deferred/out_of_scope/SafeFilePathResolver.java)

Reason:

- these are tooling/utilities, not modeling nodes
- if graph I/O is handled by editor UI, node-form file I/O has weak product value

### C. Advanced scripting / attribute batch

- `eval_expression`
- `script`
- `node_group`
- `get_attribute`
- `set_attribute`
- `filter_by_attribute`

Reason:

- these open a second, generic workflow system outside the v1 modeling language
- they complicate product boundaries fast

### D. Selector batch outside Minecraft build modeling

- `effect_type_selector`
- `entity_type_selector`
- `item_type_selector`
- `sound_event_selector`

Reason:

- these do not belong to the committed v1 building/modeling scope

### E. Workflow batch

- `for_each`
- `geometry_gate`
- `geometry_merge`
- `geometry_passthrough`
- `geometry_switch`

Reason:

- these are execution/workflow abstractions, not core modeling nodes
- if kept, they should remain clearly non-mainline

## 4.4 Legacy Compatibility Batch Candidates

These should be kept only if old graph compatibility remains a real product requirement.

Current files under:

- [utilities/legacy](/f:/development/NC/nodecraft/src/main/java/com/nodecraft/nodesystem/nodes/utilities/legacy)

Subgroups:

- spatial generator block-output nodes
- deferred analysis helpers
- instancing helpers
- voxel boolean helpers
- a few old point helpers

Recommendation:

- if you want strong backward compatibility, keep them as the only legacy bucket
- if you are willing to break early internal graphs, delete them as a batch rather than dragging them forward indefinitely

Important:

- do not half-keep them
- either keep `spatial.legacy` as a deliberate compatibility bucket
- or delete the entire bucket and simplify the system decisively

## 5. Best Structural Advice From Here

### 5.1 Introduce a canonical whitelist

Create a machine-checkable whitelist of allowed canonical category roots and canonical node ids for v1.

This should become the architecture gate.

Anything outside:

- canonical whitelist
- `deferred.*`
- `utilities.assist`
- `utilities.organization`
- explicit legacy compatibility

should be treated as architecture drift.

### 5.2 Split compatibility policy from taxonomy policy

Right now `NodeRegistry` carries both:

- canonical taxonomy resolution
- historical alias debt

That is acceptable short-term, but longer-term the compatibility tables should be isolated into a dedicated migration policy layer or file.

Reason:

- easier review
- easier deletion later
- clearer ownership of "current truth" vs "history support"

### 5.3 Stop creating "temporary" holding zones

Do not create another `utilities`-style parking lot.

Only allow:

- canonical mainline placement
- `deferred.*`
- explicit legacy compatibility

### 5.4 Prefer batch deletion over endless cold storage

If a subsystem is out of scope and not expected back soon, delete it in one deliberate batch.

This is healthier than preserving dozens of dormant node classes and their UI/config references forever.

## 6. Recommended Execution Order

If you want to simplify aggressively, use this order:

1. Decide whether `input.basic` survives at all.
2. Decide whether `deferred.out_of_scope` is a real cold-storage area or a temporary trash bin.
3. Decide whether `spatial.legacy` is worth keeping for old graph compatibility.
4. After those decisions, delete code in batches instead of one class at a time.

## 7. Strong Recommendation

My recommendation for an early-stage project is:

- keep canonical mainline
- keep `input.basic` for editor-friendly primitive inputs such as text and color
- keep `utilities.assist`
- keep `utilities.organization`
- keep minimal tested compatibility
- keep `spatial.legacy` only if you truly need old graph loading
- reduce `deferred.out_of_scope` aggressively
- delete dead or duplicate systems in batches

That will give you a smaller, more legible node system and a much lower maintenance burden later.
