package com.aetherteam.aether.client.gui.screen.inventory;

import com.aetherteam.aether.Aether;
import com.aetherteam.aether.client.gui.component.inventory.LorePageButton;
import com.aetherteam.aether.inventory.menu.LoreBookMenu;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoreBookScreen extends AbstractContainerScreen<LoreBookMenu> {
    private static final ResourceLocation TEXTURE_LORE_BACKING = new ResourceLocation(Aether.MODID, "textures/gui/menu/lore_backing.png");
    private static final ResourceLocation TEXTURE_LORE_BOOK = new ResourceLocation(Aether.MODID, "textures/gui/menu/lore_book.png");

    private final Map<Integer, List<FormattedCharSequence>> pages = new HashMap<>();

    private LorePageButton previousButton, nextButton;
    private int currentPageNumber;
    private ItemStack lastStack;

    public LoreBookScreen(LoreBookMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 199;
    }

    @Override
    protected void init() {
        super.init();
        int xPos = (this.width - this.getXSize()) / 2;
        int yPos = (this.height - (this.getYSize())) / 2;
        this.previousButton = this.addRenderableWidget(new LorePageButton(new Button.Builder(Component.literal("<"), (button) -> {
            if (this.currentPageNumber > 0) {
                this.currentPageNumber--;
            }
        }).bounds(xPos + 14, yPos + 169, 20, 20)));
        this.nextButton = this.addRenderableWidget(new LorePageButton(new Button.Builder(Component.literal(">"), (button) -> {
            if (this.currentPageNumber < this.pages.size() - 1) {
                this.currentPageNumber++;
            }
        }).bounds(xPos + 221, yPos + 169, 20, 20)));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        this.renderBg(poseStack, partialTicks, mouseX, mouseY);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int x, int y) {
        // Draws text for the page switching buttons.
        Component previous = Component.translatable("gui.aether.book_of_lore.previous");
        Component next = Component.translatable("gui.aether.book_of_lore.next");
        this.drawNormalBookText(poseStack, this.font, previous, 13, 158);
        this.drawNormalBookText(poseStack, this.font, next, 221, 158);

        // Draws "Book of Lore" text.
        Component book = Component.translatable("gui.aether.book_of_lore.book");
        Component ofLore = Component.translatable("gui.aether.book_of_lore.of_lore");
        this.drawCenteredBookText(poseStack, this.font, book, 75, 20);
        this.drawCenteredBookText(poseStack, this.font, ofLore, 75, 20 + 10);

        // Draws "Item:" text.
        Component item = Component.translatable("gui.aether.book_of_lore.item");
        this.drawRightBookText(poseStack, this.font, item, 78, 67);

        ItemStack itemStack = this.getMenu().slots.get(0).getItem();
        if (!itemStack.isEmpty()) { // Checks if there is an item placed in the book.
            String entryKey = this.getMenu().getLoreEntryKey(itemStack); // Get the translation key for the item's lore entry.

            if (I18n.exists(entryKey)) { // Checks if the lore entry exists for that item.
                Component entry = Component.translatable(entryKey);
                this.createPages(entry); // Sets up pages.

                if (this.currentPageNumber == 0) { // Behavior for first page.
                    Component title = itemStack.getHoverName();
                    this.createText(poseStack, this.font.split(title, 98), 136, 10); // Draw text for the item name.

                    this.createText(poseStack, this.pages.get(0), 136, 32); // Draw lines for first page.
                } else { // Behavior for subsequent pages.
                    this.createText(poseStack, this.pages.get(this.currentPageNumber), 136, 10); // Draw lines for the given page.
                }
            }
        }
        if (itemStack.isEmpty() || !itemStack.is(this.lastStack.getItem())) { // Resets page information if the item is removed or replaced.
            this.pages.clear();
            this.currentPageNumber = 0;
        }

        // Determines when the page switching buttons can be clicked.
        this.previousButton.active = this.currentPageNumber > 0;
        this.nextButton.active = this.currentPageNumber < this.pages.size() - 1;

        this.lastStack = itemStack;
    }

    /**
     * Splits lore entry text into lines and pages in the Book of Lore.
     * @param loreEntry The raw {@link Component} for a lore entry.
     */
    private void createPages(Component loreEntry) {
        List<FormattedCharSequence> formattedText = new ArrayList<>(this.font.split(loreEntry, 98)); // Split entry text into lines that break at a width of 98.
        List<FormattedCharSequence> firstPage;
        if (formattedText.size() < 6) { // Check if there are less than 6 lines; there can only be 6 lines of text on the first page.
            firstPage = formattedText.subList(0, formattedText.size());
            this.pages.put(0, firstPage); // Set up the first page with text.
        } else { // If there are more than 6 lines.
            firstPage = formattedText.subList(0, 6); // 6 lines for the first page.
            this.pages.put(0, firstPage); // Set up the first page with text.

            List<FormattedCharSequence> remainingPages = formattedText.subList(6, formattedText.size()); // Gets the text for the remaining pages.

            final List<List<FormattedCharSequence>> list = Lists.partition(remainingPages, 8); // Splits up the text for the remaining pages to have 8 lines per page.

            for (int i = 1; i < list.size() + 1; i++) {
                this.pages.put(i, list.get(i - 1)); // Sets up the remaining pages with text.
            }
        }
    }

    /**
     * Draws the given lines of text on a book page.
     * @param poseStack The rendering {@link PoseStack}.
     * @param reorderingProcessors The {@link List} of {@link FormattedCharSequence} to render text with.
     * @param x The {@link Integer} for the text x-position.
     * @param y The {@link Integer} for the text y-position.
     */
    private void createText(PoseStack poseStack, List<FormattedCharSequence> reorderingProcessors, int x, int y) {
        int length = 0;
        for (FormattedCharSequence line : reorderingProcessors) {
            this.drawBookText(poseStack, this.font, line, x, y + (length * 10));
            length++;
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int x, int y) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int xPos = (this.width - this.getXSize()) / 2;
        int yPos = (this.height - (this.getYSize())) / 2;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE_LORE_BACKING); // Draws the grey GUI backing.
        GuiComponent.blit(poseStack, xPos, yPos - 4, 0, 0, this.getXSize(), this.getYSize() + 56, 256, 256);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE_LORE_BOOK); // Draws the book GUI on top of backing.
        GuiComponent.blit(poseStack, xPos + 12, yPos + 2, 0, 0, this.getXSize(), this.getYSize() + 56, 256, 256);
    }

    private void drawNormalBookText(PoseStack poseStack, Font fontRenderer, Component component, int x, int y) {
        FormattedCharSequence sequence = component.getVisualOrderText();
        this.drawBookText(poseStack, fontRenderer, sequence, x, y);
    }

    private void drawRightBookText(PoseStack poseStack, Font fontRenderer, Component component, int x, int y) {
        FormattedCharSequence sequence = component.getVisualOrderText();
        this.drawBookText(poseStack, fontRenderer, sequence, x - fontRenderer.width(sequence), y);
    }

    private void drawCenteredBookText(PoseStack poseStack, Font fontRenderer, Component component, int x, int y) {
        FormattedCharSequence sequence = component.getVisualOrderText();
        this.drawBookText(poseStack, fontRenderer, sequence, x - fontRenderer.width(sequence) / 2, y);
    }

    private void drawBookText(PoseStack poseStack, Font fontRenderer, FormattedCharSequence sequence, int x, int y) {
        fontRenderer.draw(poseStack, sequence, (float) x, (float) y, 4210752);
    }
}
