package com.alexpaw.stackableingots;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.alexpaw.stackableingots.IngotBlock.COUNT;

public class IngotBlockEntity extends BlockEntity {

    private List<ItemStack> ingots = new ArrayList<>();

    public IngotBlockEntity(BlockPos pos, BlockState state) {
        super(StackableIngotsMod.INGOT_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        ingots.forEach(s->list.add(s.save(new CompoundTag())));
        tag.put("items",list);
    }

    @Override
    public void load(CompoundTag tag) {
        ingots.clear();
        tag.getList("items",ListTag.TAG_COMPOUND).forEach(t-> ingots.add(ItemStack.of((CompoundTag) t)));
        super.load(tag);
    }

    public List<ItemStack> getIngots() {
        return ingots;
    }

    public void addIngot(ItemStack ingot){
        ingots.add(ingot);
        markForSync();
    }

    public int addIngotsUntilFull(ItemStack ingotStack)
    {
        int spaceAvailable = 64 - this.ingots.size();
        int ingotsToAdd = Math.min(spaceAvailable, ingotStack.getCount());

        for(int i = 0; i < ingotsToAdd; i++)
            ingots.add(ingotStack.copyWithCount(1));

        markForSync();

        return ingotsToAdd;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        ingots.forEach(s->list.add(s.save(new CompoundTag())));
        tag.put("items",list);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    public ItemStack removeLastIngot() {
        ItemStack stack = ingots.remove(ingots.size() - 1);
        markForSync();
        return stack.copyWithCount(1);
    }

    public List<ItemStack> removeAllIngots() {
        var ingots = new ArrayList<ItemStack>(this.ingots);
        this.ingots.clear();
        this.markForSync();
        return ingots;
    }

    public void markForSync()
    {
        sendVanillaUpdatePacket();
        setChanged();
    }

    public final void sendVanillaUpdatePacket()
    {
        final ClientboundBlockEntityDataPacket packet = getUpdatePacket();
        final BlockPos pos = getBlockPos();
        if (packet != null && level instanceof ServerLevel serverLevel)
        {
            serverLevel.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false).forEach(e -> e.connection.send(packet));
            if (!ingots.isEmpty())serverLevel.setBlockAndUpdate(pos,getBlockState().setValue(COUNT, ingots.size()));
        }
    }
}
