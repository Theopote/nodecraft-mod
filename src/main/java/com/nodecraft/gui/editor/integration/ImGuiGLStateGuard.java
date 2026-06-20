package com.nodecraft.gui.editor.integration;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public final class ImGuiGLStateGuard implements AutoCloseable {
    private final int prevActiveTexture;
    private final int prevSampler0;
    private final int prevTex2d0;
    private final int prevProgram;
    private final int prevArrayBuffer;
    private final int prevElementArrayBuffer;
    private final int prevVertexArray;
    private final int prevDrawFramebuffer;
    private final int prevReadFramebuffer;
    private final int[] prevViewport;
    private final int[] prevScissorBox;
    private final boolean[] prevColorMask = new boolean[4];
    private final boolean prevDepthTest;
    private final boolean prevDepthMask;
    private final int prevDepthFunc;
    private final boolean prevBlend;
    private final int prevBlendEquationRgb;
    private final int prevBlendEquationAlpha;
    private final int prevBlendSrcRgb;
    private final int prevBlendDstRgb;
    private final int prevBlendSrcAlpha;
    private final int prevBlendDstAlpha;
    private final boolean prevScissor;
    private final boolean prevCullFace;
    private final boolean prevStencilTest;
    private final int prevStencilFunc;
    private final int prevStencilRef;
    private final int prevStencilValueMask;
    private final int prevStencilWriteMask;
    private final int prevStencilFail;
    private final int prevStencilPassDepthFail;
    private final int prevStencilPassDepthPass;

    private ImGuiGLStateGuard() {
        RenderSystem.assertOnRenderThread();

        prevActiveTexture = getInt(GL13.GL_ACTIVE_TEXTURE);
        prevProgram = getInt(GL20.GL_CURRENT_PROGRAM);
        prevArrayBuffer = getInt(GL15.GL_ARRAY_BUFFER_BINDING);
        prevElementArrayBuffer = getInt(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
        prevVertexArray = getInt(GL30.GL_VERTEX_ARRAY_BINDING);
        prevDrawFramebuffer = getInt(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        prevReadFramebuffer = getInt(GL30.GL_READ_FRAMEBUFFER_BINDING);
        prevViewport = getIntArray(GL11.GL_VIEWPORT, 4);
        prevScissorBox = getIntArray(GL11.GL_SCISSOR_BOX, 4);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);

        prevSampler0 = getInt(GL33.GL_SAMPLER_BINDING);
        prevTex2d0 = getInt(GL11.GL_TEXTURE_BINDING_2D);

        ByteBuffer colorMaskBuffer = BufferUtils.createByteBuffer(4);
        GL11.glGetBooleanv(GL11.GL_COLOR_WRITEMASK, colorMaskBuffer);
        prevColorMask[0] = colorMaskBuffer.get(0) != 0;
        prevColorMask[1] = colorMaskBuffer.get(1) != 0;
        prevColorMask[2] = colorMaskBuffer.get(2) != 0;
        prevColorMask[3] = colorMaskBuffer.get(3) != 0;

        prevDepthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        prevDepthMask = getBoolean(GL11.GL_DEPTH_WRITEMASK);
        prevDepthFunc = getInt(GL11.GL_DEPTH_FUNC);
        prevBlend = GL11.glIsEnabled(GL11.GL_BLEND);
        prevBlendEquationRgb = getInt(GL20.GL_BLEND_EQUATION_RGB);
        prevBlendEquationAlpha = getInt(GL20.GL_BLEND_EQUATION_ALPHA);
        prevBlendSrcRgb = getInt(GL14.GL_BLEND_SRC_RGB);
        prevBlendDstRgb = getInt(GL14.GL_BLEND_DST_RGB);
        prevBlendSrcAlpha = getInt(GL14.GL_BLEND_SRC_ALPHA);
        prevBlendDstAlpha = getInt(GL14.GL_BLEND_DST_ALPHA);
        prevScissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        prevCullFace = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        prevStencilTest = GL11.glIsEnabled(GL11.GL_STENCIL_TEST);
        prevStencilFunc = getInt(GL11.GL_STENCIL_FUNC);
        prevStencilRef = getInt(GL11.GL_STENCIL_REF);
        prevStencilValueMask = getInt(GL11.GL_STENCIL_VALUE_MASK);
        prevStencilWriteMask = getInt(GL11.GL_STENCIL_WRITEMASK);
        prevStencilFail = getInt(GL11.GL_STENCIL_FAIL);
        prevStencilPassDepthFail = getInt(GL11.GL_STENCIL_PASS_DEPTH_FAIL);
        prevStencilPassDepthPass = getInt(GL11.GL_STENCIL_PASS_DEPTH_PASS);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL33.glBindSampler(0, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL11.glColorMask(true, true, true, true);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL20.glBlendEquationSeparate(GL14.GL_FUNC_ADD, GL14.GL_FUNC_ADD);
        GL14.glBlendFuncSeparate(
            GL11.GL_SRC_ALPHA,
            GL11.GL_ONE_MINUS_SRC_ALPHA,
            GL11.GL_ONE,
            GL11.GL_ONE_MINUS_SRC_ALPHA
        );
    }

    public static ImGuiGLStateGuard enter() {
        return new ImGuiGLStateGuard();
    }

    @Override
    public void close() {
        RenderSystem.assertOnRenderThread();

        GL20.glUseProgram(prevProgram);
        GL30.glBindVertexArray(prevVertexArray);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevArrayBuffer);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevElementArrayBuffer);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, prevDrawFramebuffer);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, prevReadFramebuffer);
        GL11.glViewport(prevViewport[0], prevViewport[1], prevViewport[2], prevViewport[3]);
        GL11.glScissor(prevScissorBox[0], prevScissorBox[1], prevScissorBox[2], prevScissorBox[3]);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL33.glBindSampler(0, prevSampler0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, prevTex2d0);

        GL11.glColorMask(prevColorMask[0], prevColorMask[1], prevColorMask[2], prevColorMask[3]);
        GL11.glDepthMask(prevDepthMask);
        GL11.glDepthFunc(prevDepthFunc);
        GL20.glBlendEquationSeparate(prevBlendEquationRgb, prevBlendEquationAlpha);
        GL14.glBlendFuncSeparate(prevBlendSrcRgb, prevBlendDstRgb, prevBlendSrcAlpha, prevBlendDstAlpha);
        GL11.glStencilFunc(prevStencilFunc, prevStencilRef, prevStencilValueMask);
        GL11.glStencilMask(prevStencilWriteMask);
        GL11.glStencilOp(prevStencilFail, prevStencilPassDepthFail, prevStencilPassDepthPass);

        setEnabled(GL11.GL_DEPTH_TEST, prevDepthTest);
        setEnabled(GL11.GL_BLEND, prevBlend);
        setEnabled(GL11.GL_SCISSOR_TEST, prevScissor);
        setEnabled(GL11.GL_CULL_FACE, prevCullFace);
        setEnabled(GL11.GL_STENCIL_TEST, prevStencilTest);

        GL13.glActiveTexture(prevActiveTexture);
    }

    private static int getInt(int pname) {
        IntBuffer buffer = BufferUtils.createIntBuffer(1);
        GL11.glGetIntegerv(pname, buffer);
        return buffer.get(0);
    }

    private static int[] getIntArray(int pname, int size) {
        IntBuffer buffer = BufferUtils.createIntBuffer(size);
        GL11.glGetIntegerv(pname, buffer);
        int[] values = new int[size];
        for (int i = 0; i < size; i++) {
            values[i] = buffer.get(i);
        }
        return values;
    }

    private static boolean getBoolean(int pname) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(1);
        GL11.glGetBooleanv(pname, buffer);
        return buffer.get(0) != 0;
    }

    private static void setEnabled(int cap, boolean enabled) {
        if (enabled) {
            GL11.glEnable(cap);
        } else {
            GL11.glDisable(cap);
        }
    }
}
