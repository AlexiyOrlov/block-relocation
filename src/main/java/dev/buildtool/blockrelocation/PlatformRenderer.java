package dev.buildtool.blockrelocation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import dev.buildtool.satako.IntegerColor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

public class PlatformRenderer implements BlockEntityRenderer<PlatformEntity> {
    @Override
    public void render(PlatformEntity platformEntity, float p_112308_, PoseStack p_112309_, MultiBufferSource multiBufferSource, int p_112311_, int p_112312_) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lightning());
        IntegerColor closed = new IntegerColor(0x821E1Aff);
        IntegerColor open = new IntegerColor(0x416A37ff);
        platformEntity.openStates.forEach((direction, aBoolean) -> {
            if (Block.shouldRenderFace(platformEntity.getBlockState(), platformEntity.getLevel(), platformEntity.getBlockPos(), direction, platformEntity.getBlockPos().relative(direction))) {
                if (aBoolean) {
                    highlight(vertexConsumer, open.getRed(), open.getGreen(), open.getBlue(), open.getAlpha(), p_112309_, direction);
                } else {
                    highlight(vertexConsumer, closed.getRed(), closed.getGreen(), closed.getBlue(), closed.getAlpha(), p_112309_, direction);
                }
            }
        });
    }

    private void highlight(VertexConsumer vertexConsumer, int red, int green, int blue, int alpha, PoseStack poseStack, Direction direction) {
        final Matrix4f matrix4f = poseStack.last().pose();
        switch (direction) {
            case UP -> {
                vertexConsumer.vertex(matrix4f, 0, 0.99F, 0).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 0, 0.99F, 1).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 1, 0.99F, 1).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 1, 0.99F, 0).color(red, green, blue, alpha).endVertex();
            }
            case DOWN -> {
                vertexConsumer.vertex(matrix4f, 1, 0.01F, 0).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 1, 0.01F, 1).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 0, 0.01F, 1).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 0, 0.01F, 0).color(red, green, blue, alpha).endVertex();
            }
            case NORTH -> {
                vertexConsumer.vertex(matrix4f, 0, 0, 0.01f).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 0, 1F, 0.01f).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 1, 1, 0.01f).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 1, 0, 0.01f).color(red, green, blue, alpha).endVertex();
            }
            case SOUTH -> {
                vertexConsumer.vertex(matrix4f, 1, 0, 0.99f).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 1, 1F, 0.99f).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 0, 1, 0.99f).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 0, 0, 0.99f).color(red, green, blue, alpha).endVertex();
            }
            case WEST -> {
                vertexConsumer.vertex(matrix4f, 0.01f, 0, 0).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 0.01f, 0, 1).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 0.01f, 1, 1).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 0.01f, 1, 0).color(red, green, blue, alpha).endVertex();
            }
            case EAST -> {
                vertexConsumer.vertex(matrix4f, 0.99f, 1, 0).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 0.99f, 1F, 1).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 0.99f, 0, 1).color(red, green, blue, alpha).endVertex();
                vertexConsumer.vertex(matrix4f, 0.99f, 0, 0).color(red, green, blue, alpha).endVertex();
            }
        }

    }
}
