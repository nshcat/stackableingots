package com.alexpaw.stackableingots.client;

import com.alexpaw.stackableingots.IngotBlock;
import com.alexpaw.stackableingots.IngotBlockEntity;
import com.alexpaw.stackableingots.StackableIngotsMod;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class IngotBlockEntityRenderer implements BlockEntityRenderer<IngotBlockEntity>
{
    private static final Map<Item, Integer> colorCache = new HashMap<>();

    public IngotBlockEntityRenderer(BlockEntityRendererProvider.Context renderManager)
    {
        super();
    }

    @Override
    public void render(IngotBlockEntity ibe, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay)
    {
        var state = ibe.getBlockState();
        poseStack.pushPose();

        float pixelSize = 1 / 32f;
        float xSize = 14 * pixelSize;
        float ySize = 4 * pixelSize;
        float zSize = 6 * pixelSize;
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(
                new ResourceLocation(StackableIngotsMod.MOD_ID, "block/ingot"));

        for (int i = 0; i < state.getValue(IngotBlock.COUNT); i++)
        {
            if (i >= ibe.getIngots().size())
            {
                continue;
            }

            poseStack.pushPose();
            ItemStack stack = ibe.getIngots().get(i);
            int color;
            if (!colorCache.containsKey(stack.getItem()))
            {
                Color currentColor = Color.WHITE;
                MaterialStack ingotMaterialStack = ChemicalHelper.getMaterial(stack);

                if (ingotMaterialStack != null)
                {
                    currentColor = new Color(ingotMaterialStack.material().getMaterialARGB(), false);
                }

                color = currentColor.getRGB();
                colorCache.put(stack.getItem(), color);
            }
            else
            {
                color = colorCache.get(stack.getItem());
            }

            int layer = Math.floorDiv(i, 8);
            int rowIndex = (i - layer * 8) % 4;
            int index = (i - layer * 8) % 8;
            float paddingX = pixelSize + index > 4 ? xSize + 2 * pixelSize : 0;
            float paddingY = layer * ySize;
            float paddingZ = pixelSize * (rowIndex + 1) + rowIndex * (pixelSize + zSize);

            if (layer % 2 == 1)
            {
                poseStack.translate(0.5f, 0, 0.5f);
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
                poseStack.translate(-0.5f, 0, -0.5f);
            }

            poseStack.translate(pixelSize, 0, 0);

            RenderHelpers.renderTexturedCuboid(poseStack, buffer.getBuffer(RenderType.cutoutMipped()), sprite,
                                               packedLight, packedOverlay, paddingX, paddingY, paddingZ,
                                               paddingX + xSize, paddingY + ySize, paddingZ + zSize, true, color);

            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
