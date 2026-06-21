package com.nodecraft.nodesystem.registry;

import com.nodecraft.gui.ai.AiNodeSchemaCatalog;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeRegistryIntrospectionCacheTest {

    private final NodeRegistry registry = NodeRegistry.getInstance();

    @Test
    void getDefaultNodeStateReturnsDefensiveCopies() {
        assumeRegistryReady();

        Map<String, Object> first = registry.getDefaultNodeState("variable.set");
        Map<String, Object> second = registry.getDefaultNodeState("variable.set");

        assertEquals(first, second);
        if (!first.isEmpty()) {
            Map<String, Object> mutable = new HashMap<>(first);
            mutable.put("mutationProbe", "changed");
            Map<String, Object> third = registry.getDefaultNodeState("variable.set");
            assertEquals(first, third, "cached defaults should ignore caller mutations");
        }
    }

    @Test
    void collectAllReusesCachedSchemasUntilRegistryClears() {
        assumeRegistryReady();

        var first = AiNodeSchemaCatalog.collectAll(registry);
        assertTrue(first.size() > 0, "expected registered node schemas");
        var second = AiNodeSchemaCatalog.collectAll(registry);
        assertSame(first, second);

        long epochBeforeClear = registry.getIntrospectionEpoch();
        registry.clear();
        assertTrue(registry.getIntrospectionEpoch() > epochBeforeClear);

        registry.initialize();
        var afterReinit = AiNodeSchemaCatalog.collectAll(registry);
        assertTrue(afterReinit.size() > 0);
        assertNotSame(first, afterReinit);
    }

    private void assumeRegistryReady() {
        if (!registry.isInitialized()) {
            registry.initialize();
        }
    }
}
