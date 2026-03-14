package org.betterx.bclib.client.gui.screens;

import de.ambertation.wunderlib.ui.layout.components.HorizontalStack;
import de.ambertation.wunderlib.ui.layout.components.LayoutComponent;
import de.ambertation.wunderlib.ui.layout.components.VerticalStack;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Objects;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class BackupFailedScreen extends BCLibLayoutScreen {
    private final Runnable onBack;
    private final Runnable onContinue;
    private boolean resolved;

    public BackupFailedScreen(@Nullable Screen parent, Runnable onBack, Runnable onContinue) {
        super(parent, Component.translatable("title.bclib.datafixer.backup_failed"), 10, 10, 10);
        this.onBack = Objects.requireNonNull(onBack, "onBack");
        this.onContinue = Objects.requireNonNull(onContinue, "onContinue");
    }

    @Override
    public void onClose() {
        resolveBack();
    }

    @Override
    protected LayoutComponent<?, ?> initContent() {
        VerticalStack grid = new VerticalStack(fill(), fill());
        grid.addSpacer(6);
        grid.addMultilineText(fill(), fit(), Component.translatable("message.bclib.datafixer.backup_failed"))
            .centerHorizontal();
        grid.addSpacer(16);

        HorizontalStack row = grid.addRow().centerHorizontal();
        row.addButton(
                fit(),
                fit(),
                Component.translatable("title.bclib.datafixer.backup_failed.back")
        ).onPress((button) -> resolveBack());
        row.addSpacer(6);
        row.addButton(
                fit(),
                fit(),
                Component.translatable("title.bclib.datafixer.backup_failed.continue")
        ).onPress((button) -> resolveContinue());
        return grid;
    }

    private void resolveBack() {
        if (resolved) return;
        resolved = true;
        onBack.run();
    }

    private void resolveContinue() {
        if (resolved) return;
        resolved = true;
        onContinue.run();
    }
}
