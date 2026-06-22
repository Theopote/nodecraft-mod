package com.nodecraft.nodesystem.preview;

import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreviewRendererMemoryTest {

    private PreviewRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = PreviewRenderer.getInstance();
        renderer.clearAllPreviews();
        renderer.getSettings().setMaxActivePreviews(2048);
        renderer.getSettings().setMaxPreviewMemoryWeight(500_000L);
        renderer.registerElementType("weighted_test", WeightedTestElement::new);
    }

    @AfterEach
    void tearDown() {
        renderer.clearAllPreviews();
        renderer.getSettings().setMaxActivePreviews(2048);
        renderer.getSettings().setMaxPreviewMemoryWeight(500_000L);
    }

    @Test
    void purgeExpiredPreviewsRemovesTimedElementsFromActiveMap() throws Exception {
        PreviewOptions options = new PreviewOptions().setDuration(1);
        String previewId = renderer.showPreview("node-a", "weighted_test", 1, options);
        assertNotNull(previewId);
        assertTrue(renderer.hasActivePreview(previewId));

        AbstractPreviewElement element = getActiveElement(previewId);
        setLastUpdatedTime(element, System.currentTimeMillis() - 5_000L);

        int removed = renderer.purgeExpiredPreviews();

        assertEquals(1, removed);
        assertFalse(renderer.hasActivePreview(previewId));
        assertEquals(0, renderer.getActiveElementCount());
    }

    @Test
    void memoryBudgetEvictsLeastRecentlyUpdatedPreview() throws Exception {
        renderer.getSettings().setMaxPreviewMemoryWeight(150L);

        String older = renderer.showPreview("node-old", "weighted_test", 100, null);
        String newer = renderer.showPreview("node-new", "weighted_test", 40, null);
        assertNotNull(older);
        assertNotNull(newer);

        assertTrue(renderer.hasActivePreview(older));
        assertTrue(renderer.hasActivePreview(newer));

        setLastUpdatedTime(getActiveElement(older), 1L);
        setLastUpdatedTime(getActiveElement(newer), 2L);

        String heavy = renderer.showPreview("node-heavy", "weighted_test", 100, null);
        assertNotNull(heavy);

        assertFalse(renderer.hasActivePreview(older));
        assertTrue(renderer.hasActivePreview(newer));
        assertTrue(renderer.hasActivePreview(heavy));
    }

    @Test
    void activePreviewLimitEvictsLeastRecentlyUpdatedPreview() throws Exception {
        renderer.getSettings().setMaxActivePreviews(2);

        String first = renderer.showPreview("node-1", "weighted_test", 1, null);
        String second = renderer.showPreview("node-2", "weighted_test", 1, null);
        assertNotNull(first);
        assertNotNull(second);

        setLastUpdatedTime(getActiveElement(first), 1L);
        setLastUpdatedTime(getActiveElement(second), 2L);

        String third = renderer.showPreview("node-3", "weighted_test", 1, null);
        assertNotNull(third);

        assertFalse(renderer.hasActivePreview(first));
        assertTrue(renderer.hasActivePreview(second));
        assertTrue(renderer.hasActivePreview(third));
    }

    private static AbstractPreviewElement getActiveElement(String previewId) throws Exception {
        Field field = PreviewRenderer.class.getDeclaredField("activeElements");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        var activeElements = (java.util.Map<String, AbstractPreviewElement>) field.get(PreviewRenderer.getInstance());
        AbstractPreviewElement element = activeElements.get(previewId);
        assertNotNull(element, "missing preview element for id=" + previewId);
        return element;
    }

    private static void setLastUpdatedTime(AbstractPreviewElement element, long timestamp) throws Exception {
        Field field = AbstractPreviewElement.class.getDeclaredField("lastUpdatedTime");
        field.setAccessible(true);
        field.setLong(element, timestamp);
    }

    private static final class WeightedTestElement extends AbstractPreviewElement {
        private int weight = 1;

        private WeightedTestElement(String id, String ownerNodeId, Object data, PreviewOptions options) {
            super(id, ownerNodeId, data, options);
        }

        @Override
        protected void processData(Object data) {
            if (data instanceof Number number) {
                weight = Math.max(1, number.intValue());
            }
        }

        @Override
        public void render(MatrixStack matrices, Camera camera, float partialTicks, float globalOpacity) {
        }

        @Override
        public boolean shouldRender(Camera camera) {
            return !hasExpired();
        }

        @Override
        public void cleanup() {
        }

        @Override
        public int estimateMemoryWeight() {
            return weight;
        }
    }
}
