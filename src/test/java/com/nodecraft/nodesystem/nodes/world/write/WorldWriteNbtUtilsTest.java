package com.nodecraft.nodesystem.nodes.world.write;

import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldWriteNbtUtilsTest {

    @Test
    void parseSnbtAcceptsBlockEntityPayloads() {
        NbtCompound nbt = WorldWriteNbtUtils.parseSnbt("{Items:[],Lock:\"sealed\"}");

        assertNotNull(nbt);
        assertTrue(nbt.contains("Items"));
        assertTrue(nbt.toString().contains("sealed"));
    }

    @Test
    void mergeNbtPreservesExistingKeysAndOverridesIncomingKeys() {
        NbtCompound base = new NbtCompound();
        base.putString("id", "minecraft:chest");
        base.putString("Lock", "old");
        NbtCompound incoming = new NbtCompound();
        incoming.putString("Lock", "new");
        incoming.putString("CustomName", "{\"text\":\"Loot\"}");

        NbtCompound merged = WorldWriteNbtUtils.mergeNbt(base, incoming);

        assertTrue(merged.toString().contains("minecraft:chest"));
        assertTrue(merged.toString().contains("new"));
        assertTrue(merged.toString().contains("CustomName"));
    }
}
