package dev.buildtool.blockrelocation.api;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;

public interface BlockRenderHelper {
    RenderType.CompositeState opaqueCompositeState = RenderType.CompositeState.builder().setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, true)).setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeLightningShader)).createCompositeState(false);

    /**
     * Highlights block side with a given color
     */
    default void highlightSide(VertexConsumer vertexConsumer, int red, int green, int blue, int alpha, PoseStack poseStack, Direction side) {
        final Matrix4f matrix4f = poseStack.last().pose();
        switch (side) {
            case UP -> {
                vertexConsumer.vertex(matrix4f, 0, 1.01F, 0).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 0, 1.01F, 1).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 1, 1.01F, 1).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 1, 1.01F, 0).color(red, green, blue, alpha).endVertex();
            }
            case DOWN -> {
                vertexConsumer.vertex(matrix4f, 1, -0.01F, 0).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 1, -0.01F, 1).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 0, -0.01F, 1).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 0, -0.01F, 0).color(red, green, blue, alpha).endVertex();
            }
            case NORTH -> {
                vertexConsumer.vertex(matrix4f, 0, 0, -0.01f).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 0, 1F, -0.01f).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 1, 1, -0.01f).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 1, 0, -0.01f).color(red, green, blue, alpha).endVertex();
            }
            case SOUTH -> {
                vertexConsumer.vertex(matrix4f, 1, 0, 1.01f).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 1, 1F, 1.01f).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 0, 1, 1.01f).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 0, 0, 1.01f).color(red, green, blue, alpha).endVertex();
            }
            case WEST -> {
                vertexConsumer.vertex(matrix4f, -0.01f, 0, 0).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, -0.01f, 0, 1).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, -0.01f, 1, 1).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, -0.01f, 1, 0).color(red, green, blue, alpha).endVertex();
            }
            case EAST -> {
                vertexConsumer.vertex(matrix4f, 1.01f, 1, 0).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 1.01f, 1F, 1).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 1.01f, 0, 1).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 1.01f, 0, 0).color(red, green, blue, alpha).endVertex();
            }
        }

    }
}
