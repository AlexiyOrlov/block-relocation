# Block Relocation

Block Relocation is a mod which adds
2 blocks: Platform and Relocator. Both can be found in
the Transportation creative tab.
They can be used together to move other blocks
in the world. Platform is responsible
for grabbing adjacent blocks, while
Relocator is responsible for their
movement. Relocator works when a direct
redstone signal is received; it has a UI
where you can set movement directions.
Platform's sides can be toggled via an
empty hand. Any entities above platforms
will also be moved along.

Relocator recipe is a piston surrounded by obsidian, and
Platform recipe is an iron ingot surrounded by sticks.

## Implementation

Relocator's block entity implements *BlockMover*
interface, and Platform's block entity
implements *BlockGrabber* interface.

While you can easily implement *BlockGrabber*
interface on your block entity so it can be moved by
a *BlockMover*, the side state toggling
logic and rendering is up to you to design.
You can use *PlatformBlock* and *PlatformRenderer*
for reference.

## Usage contract

When blocks are relocated, it is necessary to ensure
that players are not interacting with those blocks via menu screens (UIs with items).
This can be achieved by screens being closed when the game detects that there is no
appropriate block in the old position anymore; detection should be done in `AbstractContainerMenu.stillValid()`
method by checking whether a block entity in the old position is still the same.

If you are doing synchronization not via `AbstractContainerMenu` subclass, you must ensure that
the block entity at the target position is of appropriate class.

# Development setup

This mod depends on [Satako](https://github.com/AlexiyOrlov/satako) library.

To prepare a workspace with this mod, add following dependencies to the build file (change the version appropriately):

`runtimeOnly(fg.deobf('dev.buildtool:block-relocation:0.0.4-1.18.2'))`

`implementation(fg.deobf('dev.buildtool:block-relocation:0.0.4-1.18.2:api'))`
