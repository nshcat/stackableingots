# Stackableingots for GTCEu-Modern 1.20.x

Addon for GTCEu-Modern that allows players to stack their ingots as piles in the world.
Good for dragons (or kobolds) wanting to sit on top of their hoard of precious (and/or hazardous) metals!

Heavily modified (and fixed) version of https://github.com/iglee42/PlaceableIngots.

<p align="center">
  <img src="/media/ingots.png" width="512">
</p>

## How to use
- Right-click the ground with any number of ingots in your hand to place one ingot
- Right-click an existing pile to add ingots to it. You can mix ingots in piles!
- Crouching while placing ingots will place or add the whole held stack (until pile is full)
- Adding ingots to a full pile will create a new pile on top of it

## Dependencies
- GTCEu-Modern 1.6.3 and all of its dependencies
- Jade, JEI and EMI

## Changes to iglee42's versions
- Support for all GTCEu material ingots, with proper colour rendering
- Much improved renderer that fixes quite a few bugs (disappearing block, missing lighting, etc)
- (In my opinion) improved interaction with ingot piles
- Jade integration

## TODO, mostly dev stuff
- Make all string literals translateable
- Either make ingot piles not mineable, or fix mining particles showing the missing texture texture
- Rework old, bad code from IngotBlock.java
- Look into not storing a list of ItemStacks in the block entity, but rather item instances
- Look into issues arising when interacting with e.g. an AE2 terminal while holding an ingot. Currently, this will place an ingot pile in the air. Maybe require solid block under the pile that is to be created
