package dev.buildtool.blockrelocation;

import dev.buildtool.blockrelocation.api.BlockMover;
import dev.buildtool.satako.gui.ButtonGroup;
import dev.buildtool.satako.gui.Label;
import dev.buildtool.satako.gui.RadioButton;
import dev.buildtool.satako.gui.Screen2;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class RelocatorScreen extends Screen2 {
    private final BlockMover blockMover;
    private final BlockPos moverPosition;

    public RelocatorScreen(TranslatableComponent title, BlockMover mover, BlockPos moverPosition) {
        super(title);
        blockMover = mover;
        this.moverPosition = moverPosition;
    }

    @Override
    public void init() {
        super.init();
        final int buttonWidth = 34;
        addRenderableWidget(new Label(centerX - 100, 10, new TranslatableComponent("block_relocation.movement.directions")));
        for (Direction direction : Direction.values()) {
            Label from = new Label(3, 40 + 20 * direction.ordinal(), new TranslatableComponent(direction.getName()).append(":"));
            addRenderableWidget(from);
            ButtonGroup group = new ButtonGroup();
            Direction to = blockMover.getToFrom(direction);
            for (Direction dir : Direction.values()) {
                RadioButton radioButton = new RadioButton(100 + buttonWidth * dir.ordinal(), from.y, new TextComponent(dir.getName()), p_93751_ -> blockMover.setFromTo(direction, dir));
                radioButton.setWidth(buttonWidth);
                group.add(radioButton);
                addRenderableWidget(radioButton);
                if (to == dir)
                    group.setSelected(radioButton);
            }
            group.connect();

        }
    }

    @Override
    public void onClose() {
        super.onClose();
        BlockRelocation.CHANNEL.sendToServer(new SetDirections(blockMover.getMovementDirections(), moverPosition));
    }
}
