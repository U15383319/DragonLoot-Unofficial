package net.dragonloot.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import java.util.Random;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.block.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class DragonTridentBakedModel implements IBakedModel {
    private final IBakedModel baseModel;
    private final IBakedModel inHandModel;
    private final IBakedModel throwingModel;
    private final boolean throwing;
    private final ItemOverrideList overrides;

    public DragonTridentBakedModel(IBakedModel baseModel, IBakedModel inHandModel, IBakedModel throwingModel) {
        this(baseModel, inHandModel, throwingModel, false);
    }

    private DragonTridentBakedModel(IBakedModel baseModel, IBakedModel inHandModel, IBakedModel throwingModel, boolean throwing) {
        this.baseModel = baseModel;
        this.inHandModel = inHandModel;
        this.throwingModel = throwingModel;
        this.throwing = throwing;
        this.overrides = new ItemOverrideList() {
            @Nullable
            @Override
            public IBakedModel resolve(IBakedModel model, ItemStack stack, @Nullable ClientWorld level,
                                      @Nullable LivingEntity entity) {
                boolean isThrowing = entity != null && entity.isUsingItem() && entity.getUseItem() == stack;
                return new DragonTridentBakedModel(baseModel, inHandModel, throwingModel, isThrowing);
            }
        };
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, Random random) {
        return baseModel.getQuads(state, direction, random);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return baseModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return baseModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return baseModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return baseModel.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return baseModel.getParticleIcon();
    }

    @Override
    public ItemCameraTransforms getTransforms() {
        return baseModel.getTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return overrides;
    }

    @Override
    public IBakedModel handlePerspective(ItemCameraTransforms.TransformType displayContext, MatrixStack poseStack) {
        if (isHandContext(displayContext)) {
            IBakedModel model = throwing ? throwingModel : inHandModel;
            return model.handlePerspective(displayContext, poseStack);
        }

        return baseModel.handlePerspective(displayContext, poseStack);
    }

    private static boolean isHandContext(ItemCameraTransforms.TransformType displayContext) {
        return displayContext == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND
                || displayContext == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
                || displayContext == ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND
                || displayContext == ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
    }
}
