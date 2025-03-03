package com.alexpaw.stackableingots.integration.jade;

import com.alexpaw.stackableingots.IngotBlockEntity;
import com.alexpaw.stackableingots.StackableIngotsMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class IngotBlockProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor>
{
    private class IngotAmount
    {
        private int _amount;
        private String _type;

        private int _id;

        public IngotAmount(String type, int amount, int id)
        {
            this._amount = amount;
            this._type = type;
            this._id = id;
        }

        public int getAmount()
        {
            return this._amount;
        }

        public int getId()
        {
            return this._id;
        }

        public String getType()
        {
            return this._type;
        }
    }

    public class IngotAmountComparator implements Comparator<IngotAmount>
    {
        @Override
        public int compare(IngotAmount o1, IngotAmount o2)
        {
            return Integer.compare(o2.getAmount(), o1.getAmount());
        }
    }

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig)
    {
        if (blockAccessor.getBlockEntity() instanceof IngotBlockEntity blockEntity)
        {
            int numIngotTypes = blockAccessor.getServerData().getInt("numIngotTypes");

            var ingotAmounts = new ArrayList<IngotAmount>();

            if (numIngotTypes > 0)
            {
                for (int i = 0; i < numIngotTypes; i++)
                {
                    var ingotType = blockAccessor.getServerData().getString("ingotType" + i);
                    var ingotId = blockAccessor.getServerData().getInt("ingotType" + i + "Id");
                    var ingotTypeAmount = blockAccessor.getServerData().getInt("ingotType" + i + "Amount");

                    ingotAmounts.add(new IngotAmount(ingotType, ingotTypeAmount, ingotId));
                }

                ingotAmounts.sort(new IngotAmountComparator());

                for (var ingotAmount : ingotAmounts)
                {
                    IElementHelper elements = iTooltip.getElementHelper();
                    IElement icon = elements.item(new ItemStack(Item.byId(ingotAmount.getId())), 0.5f).size(
                            new Vec2(10, 10)).translate(new Vec2(0, -1));

                    iTooltip.add(Component.literal("" + ingotAmount.getAmount() + "x "));
                    iTooltip.append(icon);
                    iTooltip.append(Component.literal(" " + ingotAmount.getType()));
                }
            }

        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor)
    {
        if (blockAccessor.getBlockEntity() instanceof IngotBlockEntity blockEntity)
        {
            List<ItemStack> ingots = blockEntity.getIngots();

            HashMap<Integer, Integer> ingotAmounts = new HashMap<>();
            HashMap<Integer, String> ingotTypes = new HashMap<>();

            for (ItemStack itemStack : ingots)
            {
                var ingotItem = itemStack.getItem();
                var type = ingotItem.getDescription().getString();
                var id = Item.getId(ingotItem);

                if (!ingotAmounts.containsKey(id))
                {
                    ingotAmounts.put(id, 1);
                }
                else
                {
                    ingotAmounts.put(id, ingotAmounts.get(id) + 1);
                }

                ingotTypes.put(id, type);
            }

            int uniqueIngotCount = 0;

            for (Integer id : ingotAmounts.keySet())
            {
                compoundTag.putInt("ingotType" + uniqueIngotCount + "Id", id);
                compoundTag.putString("ingotType" + uniqueIngotCount, ingotTypes.get(id));
                compoundTag.putInt("ingotType" + uniqueIngotCount + "Amount", ingotAmounts.get(id));
                uniqueIngotCount++;
            }

            compoundTag.putInt("numIngotTypes", uniqueIngotCount);
        }
    }

    @Override
    public ResourceLocation getUid()
    {
        return new ResourceLocation(StackableIngotsMod.MOD_ID, "ingot_block");
    }
}
