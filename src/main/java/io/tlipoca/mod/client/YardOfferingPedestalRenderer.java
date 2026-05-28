package io.tlipoca.mod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.tlipoca.mod.block.entity.YardOfferingPedestalBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class YardOfferingPedestalRenderer implements BlockEntityRenderer<YardOfferingPedestalBlockEntity, YardOfferingPedestalRenderer.RenderState> {
    private final ItemModelResolver itemModelResolver;

    public YardOfferingPedestalRenderer(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(YardOfferingPedestalBlockEntity blockEntity, RenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
        state.item.clear();
        ItemStack offering = blockEntity.getOffering();
        Level level = blockEntity.getLevel();
        if (!offering.isEmpty() && level != null) {
            float time = level.getGameTime() + partialTick;
            state.rotationDegrees = (time * 0.5F) % 360.0F;
            state.bobOffset = (float) Math.sin(time * 0.08F) * 0.06F;
            itemModelResolver.updateForTopItem(state.item, offering, ItemDisplayContext.GROUND, level, null, blockEntity.getBlockPos().hashCode());
        }
    }

    @Override
    public void submit(RenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (state.item.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5F, 1.12F + state.bobOffset, 0.5F);
        if (cameraRenderState.initialized) {
            poseStack.mulPose(cameraRenderState.orientation);
        }
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.rotationDegrees));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        state.item.submit(poseStack, submitNodeCollector, LightTexture.FULL_BRIGHT, 0, 0);
        poseStack.popPose();
    }

    public static class RenderState extends BlockEntityRenderState {
        private final ItemStackRenderState item = new ItemStackRenderState();
        private float rotationDegrees;
        private float bobOffset;
    }
}
