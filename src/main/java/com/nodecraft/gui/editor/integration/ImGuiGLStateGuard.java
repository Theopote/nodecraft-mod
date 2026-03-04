package com.nodecraft.gui.editor.integration;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public final class ImGuiGLStateGuard implements AutoCloseable {
    private final int prevActiveTexture;
    private final int prevSampler0;
    private final int prevTex2d0;
    private final boolean[] prevColorMask = new boolean[4];
    private final boolean prevDepthTest;
    private final boolean prevBlend;
    private final boolean prevScissor;

    private ImGuiGLStateGuard() {
        RenderSystem.assertOnRenderThread();

        IntBuffer buffer = BufferUtils.createIntBuffer(1);
        GL11.glGetIntegerv(GL13.GL_ACTIVE_TEXTURE, buffer);
        prevActiveTexture = buffer.get(0);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);

        buffer.clear();
        GL11.glGetIntegerv(GL33.GL_SAMPLER_BINDING, buffer);
        prevSampler0 = buffer.get(0);

        buffer.clear();
        GL11.glGetIntegerv(GL11.GL_TEXTURE_BINDING_2D, buffer);
        prevTex2d0 = buffer.get(0);

        ByteBuffer colorMaskBuffer = BufferUtils.createByteBuffer(4);
        GL11.glGetBooleanv(GL11.GL_COLOR_WRITEMASK, colorMaskBuffer);
        prevColorMask[0] = colorMaskBuffer.get(0) != 0;
        prevColorMask[1] = colorMaskBuffer.get(1) != 0;
        prevColorMask[2] = colorMaskBuffer.get(2) != 0;
        prevColorMask[3] = colorMaskBuffer.get(3) != 0;

        prevDepthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        prevBlend = GL11.glIsEnabled(GL11.GL_BLEND);
        prevScissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL33.glBindSampler(0, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL11.glColorMask(true, true, true, true);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public static ImGuiGLStateGuard enter() {
        return new ImGuiGLStateGuard();
    }

    @Override
    public void close() {
        RenderSystem.assertOnRenderThread();

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL33.glBindSampler(0, prevSampler0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, prevTex2d0);

        GL11.glColorMask(prevColorMask[0], prevColorMask[1], prevColorMask[2], prevColorMask[3]);

        setEnabled(GL11.GL_DEPTH_TEST, prevDepthTest);
        setEnabled(GL11.GL_BLEND, prevBlend);
        setEnabled(GL11.GL_SCISSOR_TEST, prevScissor);

        GL13.glActiveTexture(prevActiveTexture);
    }

    private static void setEnabled(int cap, boolean enabled) {
        if (enabled) {
            GL11.glEnable(cap);
        } else {
            GL11.glDisable(cap);
        }
    }
}