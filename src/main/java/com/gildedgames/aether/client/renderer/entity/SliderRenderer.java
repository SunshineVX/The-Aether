package com.gildedgames.aether.client.renderer.entity;

import com.gildedgames.aether.Aether;
import com.gildedgames.aether.client.registry.AetherModelLayers;
import com.gildedgames.aether.client.renderer.entity.model.SliderModel;
import com.gildedgames.aether.common.entity.monster.dungeon.Slider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SliderRenderer extends MobRenderer<Slider, SliderModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Aether.MODID, "textures/entity/mobs/slider/slider_awake.png");

    public SliderRenderer(EntityRendererProvider.Context context) {
        super(context, new SliderModel(context.bakeLayer(AetherModelLayers.SLIDER)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(Slider pEntity) {
        return TEXTURE;
    }

    @Override
    protected void scale(Slider slider, PoseStack matrixStackIn, float partialTickTime) {
        matrixStackIn.scale(2F, 2F, 2F);
    }

    @Override
    public void render(Slider pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    //    @Override
//    protected float getBob(SliderEntity cockatrice, float partialTicks) {
////        float f1 = cockatrice.prevWingRotation + (cockatrice.wingRotation - cockatrice.prevWingRotation) * partialTicks;
////        float f2 = cockatrice.prevDestPos + (cockatrice.destPos - cockatrice.prevDestPos) * partialTicks;
//        return (Mth.sin(f1) + 1.0F) * f2;
//    }


}
