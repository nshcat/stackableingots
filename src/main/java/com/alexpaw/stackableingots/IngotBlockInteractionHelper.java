package com.alexpaw.stackableingots;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;

import static com.alexpaw.stackableingots.StackableIngotsMod.INGOT_BLOCK;
import static com.alexpaw.stackableingots.StackableIngotsMod.INGOT_BLOCK_ENTITY;

public class IngotBlockInteractionHelper
{
    public static void useItemOnBlock(PlayerInteractEvent.RightClickBlock event)
    {
        Player player = event.getEntity();
        if (event.getItemStack().is(Tags.Items.INGOTS) && player.mayBuild())
        {
            BlockPos pos = event.getHitVec().getBlockPos();
            BlockState state = event.getLevel().getBlockState(pos);

            // Are we aiming at an ingot pile?
            if (state.is(INGOT_BLOCK.get())) // Yes we are
            {
                // Pile is already full
                if(state.getValue(IngotBlock.COUNT) == 64)
                {
                    for (int i = 1; i < event.getLevel().getMaxBuildHeight(); i++)
                    {
                        BlockPos pos1 = pos.offset(0,i,0);
                        if (event.getLevel().getBlockState(pos1).isAir())
                        {
                            if (event.getLevel().isClientSide())
                            {
                                event.getLevel().playSound(player, event.getPos(), SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 1f, 1f);
                            }
                            else
                            {
                                createIngotPile(player, event.getLevel(), pos1, event.getItemStack());
                            }
                            event.setUseItem(Event.Result.ALLOW);
                            event.setCancellationResult(InteractionResult.CONSUME);
                            event.setCanceled(true);
                            break;
                        }
                        else if (event.getLevel().getBlockState(pos1).is(INGOT_BLOCK.get()))
                        {
                            if (event.getLevel().getBlockState(pos1).getValue(IngotBlock.COUNT) == 64)
                                continue;

                            if (event.getLevel().isClientSide())
                            {
                                event.getLevel().playSound(player, event.getPos(), SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 1f, 1f);
                            }
                            else
                            {
                                event.getLevel().getBlockEntity(pos1,INGOT_BLOCK_ENTITY.get()).ifPresent(be->{
                                    addToIngotPile(player, event.getItemStack(), (IngotBlockEntity) be);
                                });
                            }

                            event.setUseItem(Event.Result.ALLOW);
                            event.setCancellationResult(InteractionResult.CONSUME);
                            event.setCanceled(true);
                            break;
                        }
                    }
                }
                else // Add to pile that has space
                {
                    if (event.getLevel().isClientSide())
                    {
                        event.getLevel().playSound(player, event.getPos(), SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 1f, 1f);
                    }
                    else
                    {
                        event.getLevel().getBlockEntity(pos, INGOT_BLOCK_ENTITY.get()).ifPresent(be -> {
                            addToIngotPile(player, event.getItemStack(), (IngotBlockEntity) be);
                        });
                    }
                    event.setUseItem(Event.Result.ALLOW);
                    event.setCancellationResult(InteractionResult.CONSUME);
                    event.setCanceled(true);
                }
            }
            else // We are not aiming at an ingot pile.
            {
                // Create a new ingot pile.
                pos = pos.relative(event.getFace());
                state = event.getLevel().getBlockState(pos);
                if (state.isAir())
                {
                    if (event.getLevel().isClientSide())
                    {
                        event.getLevel().playSound(player, event.getPos(), SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 1f, 1f);
                    }
                    else
                    {
                        createIngotPile(player, event.getLevel(), pos, event.getItemStack());
                    }

                    event.setUseItem(Event.Result.ALLOW);
                    event.setCancellationResult(InteractionResult.CONSUME);
                    event.setCanceled(true);
                }
            }
        }
    }

    private static void createIngotPile(Player player, Level level, BlockPos position, ItemStack handItemStack)
    {
        level.setBlockAndUpdate(position, INGOT_BLOCK.get().defaultBlockState());
        level.getBlockEntity(position, INGOT_BLOCK_ENTITY.get()).ifPresent(be -> {
            addToIngotPile(player, handItemStack, (IngotBlockEntity) be);
        });
    }

    private static void addToIngotPile(Player player, ItemStack handItemStack, IngotBlockEntity be)
    {
        // If player is crouching, place whole item stack in hand
        if(player.isCrouching())
        {
            int addedIngots = be.addIngotsUntilFull(handItemStack);
            consumeItem(handItemStack, player, addedIngots);
        }
        else
        {
            be.addIngot(handItemStack.copyWithCount(1));
            consumeItem(handItemStack, player, 1);
        }

        be.markForSync();
    }

    private static void consumeItem(ItemStack stack, Player player, int amount)
    {
        if (player == null || !player.getAbilities().instabuild)
        {
            stack.setCount(stack.getCount() - amount);
            if (player != null)
                player.swing(InteractionHand.MAIN_HAND);
        }
    }

}
