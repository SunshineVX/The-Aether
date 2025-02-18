package com.aetherteam.aether.client.gui.screen.inventory;

import com.aetherteam.aether.Aether;
import com.aetherteam.aether.AetherConfig;
import com.aetherteam.aether.client.AetherKeys;
import com.aetherteam.aether.client.gui.screen.perks.AetherCustomizationsScreen;
import com.aetherteam.aether.client.gui.screen.perks.MoaSkinsScreen;
import com.aetherteam.aether.inventory.menu.AccessoriesMenu;
import com.aetherteam.aether.mixin.mixins.client.accessor.ScreenAccessor;
import com.aetherteam.aether.network.AetherPacketHandler;
import com.aetherteam.aether.network.packet.serverbound.ClearItemPacket;
import com.aetherteam.aether.perk.PerkUtil;
import com.aetherteam.nitrogen.api.users.User;
import com.aetherteam.nitrogen.api.users.UserData;
import com.aetherteam.nitrogen.network.PacketRelay;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.client.gui.CuriosScreen;
import top.theillusivec4.curios.client.gui.RenderButton;
import top.theillusivec4.curios.common.inventory.CosmeticCurioSlot;
import top.theillusivec4.curios.common.inventory.CurioSlot;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.client.CPacketDestroy;
import top.theillusivec4.curios.common.network.client.CPacketToggleRender;

import javax.annotation.Nullable;

/**
 * [CODE COPY] - {@link CuriosScreen}.<br>
 * [CODE COPY] - {@link InventoryScreen}.<br><br>
 * Modified to register slots for Aether accessories only.
 */
public class AccessoriesScreen extends EffectRenderingInventoryScreen<AccessoriesMenu> implements RecipeUpdateListener, RecipeBookBehavior<AccessoriesMenu, AccessoriesScreen> {
    public static final ResourceLocation ACCESSORIES_BUTTON = new ResourceLocation(Aether.MODID, "textures/gui/inventory/button/accessories_button.png");
    public static final ResourceLocation SKINS_BUTTON = new ResourceLocation(Aether.MODID, "textures/gui/perks/skins/skins_button.png");
    public static final ResourceLocation CUSTOMIZATION_BUTTON = new ResourceLocation(Aether.MODID, "textures/gui/perks/customization/customization_button.png");

    private static final ResourceLocation ACCESSORIES_INVENTORY = new ResourceLocation(Aether.MODID, "textures/gui/inventory/accessories.png");
    private static final ResourceLocation ACCESSORIES_INVENTORY_CREATIVE = new ResourceLocation(Aether.MODID, "textures/gui/inventory/accessories_creative.png");
    private static final ResourceLocation CURIO_INVENTORY = new ResourceLocation(Curios.MODID, "textures/gui/inventory.png");
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");

    private static final SimpleContainer DESTROY_ITEM_CONTAINER = new SimpleContainer(1);
    private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
    private boolean widthTooNarrow;
    private boolean buttonClicked;
    private boolean isRenderButtonHovered;
    @Nullable
    private Slot destroyItemSlot;

    public AccessoriesScreen(AccessoriesMenu accessoriesMenu, Inventory playerInventory, Component title) {
        super(accessoriesMenu, playerInventory, title);
        this.passEvents = true;
    }

    @Override
    protected void containerTick() {
        RecipeBookBehavior.super.containerTick(this);
    }

    @Override
    public void init() {
        super.init();
        // Basic Curio-based initialization.
        if (this.getMinecraft().player != null) {
            this.imageWidth = this.getMinecraft().player.isCreative() ? 176 + this.creativeXOffset() : 176;
        }
        this.widthTooNarrow = this.width < 379;
        this.getRecipeBookComponent().init(this.width, this.height, this.getMinecraft(), this.widthTooNarrow, this.getMenu());
        this.updateScreenPosition();
        this.addWidget(this.getRecipeBookComponent());
        this.setInitialFocus(this.getRecipeBookComponent());

        if (this.getMinecraft().player != null && this.getRecipeBookComponent().isVisible()) {
            this.getRecipeBookComponent().toggleVisibility();
            this.updateScreenPosition();
        }

        this.addRenderableWidget(new ImageButton(this.getGuiLeft() + 142, this.height / 2 - 22, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, (pressed) -> {
            this.getRecipeBookComponent().toggleVisibility();
            this.updateScreenPosition();
            pressed.setPosition(this.getGuiLeft() + 142, this.height / 2 - 22);
            this.buttonClicked = true;
        }));

        this.updateRenderButtons();

        // Create perk-related buttons.
        ImageButton skinsButton = this.createSkinsButton();
        this.addRenderableWidget(skinsButton);

        User user = UserData.Client.getClientUser();
        if (user != null && (PerkUtil.hasDeveloperGlow().test(user) || PerkUtil.hasHalo().test(user))) {
            ImageButton customizationButton = this.createCustomizationButton();
            this.addRenderableWidget(customizationButton);
        }
    }

    /**
     * [CODE COPY] - {@link CuriosScreen#updateScreenPosition()}.<br>
     * [CODE COPY] - {@link RecipeBookComponent#updateScreenPosition(int, int)}.
     */
    private void updateScreenPosition() {
        int i;
        if (this.getRecipeBookComponent().isVisible() && !this.widthTooNarrow) {
            int offset = 200 - this.creativeXOffset();
            i = 177 + (this.width - this.getXSize() - offset) / 2;
        } else {
            i = (this.width - this.getXSize()) / 2;
        }
        this.leftPos = i;
        this.updateRenderButtons();
    }

    /**
     * Creates the button for the {@link MoaSkinsScreen}.
     * @return The {@link ImageButton}.
     */
    private ImageButton createSkinsButton() {
        ImageButton skinsButton = new ImageButton(this.getGuiLeft() - 22, this.getGuiTop() + 2, 20, 20, 0, 0, 20, SKINS_BUTTON, 20, 40,
                (pressed) -> this.getMinecraft().setScreen(new MoaSkinsScreen(this)),
                Component.translatable("gui.aether.accessories.skins_button")) {
            @Override
            public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
                super.render(poseStack, mouseX, mouseY, partialTick);
                if (!AccessoriesScreen.this.getRecipeBookComponent().isVisible()) {
                    this.setX(AccessoriesScreen.this.getGuiLeft() - 22);
                    this.setY(AccessoriesScreen.this.getGuiTop() + 2);
                } else {
                    this.setX(AccessoriesScreen.this.getGuiLeft() + 2);
                    this.setY(AccessoriesScreen.this.getGuiTop() - 22);
                }
            }
        };
        skinsButton.setTooltip(Tooltip.create(Component.translatable("gui.aether.accessories.skins_button")));
        return skinsButton;
    }

    /**
     * Creates the button for the {@link AetherCustomizationsScreen}.
     * @return The {@link ImageButton}.
     */
    private ImageButton createCustomizationButton() {
        ImageButton customizationButton = new ImageButton(this.getGuiLeft() - 22, this.getGuiTop() + 24, 20, 20, 0, 0, 20, CUSTOMIZATION_BUTTON, 20, 40,
                (pressed) -> this.getMinecraft().setScreen(new AetherCustomizationsScreen(this)),
                Component.translatable("gui.aether.accessories.customization_button")) {
            @Override
            public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
                super.render(poseStack, mouseX, mouseY, partialTick);
                if (!AccessoriesScreen.this.getRecipeBookComponent().isVisible()) {
                    this.setX(AccessoriesScreen.this.getGuiLeft() - 22);
                    this.setY(AccessoriesScreen.this.getGuiTop() + 24);
                } else {
                    this.setX(AccessoriesScreen.this.getGuiLeft() + 24);
                    this.setY(AccessoriesScreen.this.getGuiTop() - 22);
                }
            }
        };
        customizationButton.setTooltip(Tooltip.create(Component.translatable("gui.aether.accessories.customization_button")));
        return customizationButton;
    }

    private void updateRenderButtons() {
        ScreenAccessor screenAccessor = (ScreenAccessor) this;
        screenAccessor.aether$getNarratables().removeIf(widget -> widget instanceof RenderButton);
        this.children().removeIf(widget -> widget instanceof RenderButton);
        this.renderables.removeIf(widget -> widget instanceof RenderButton);
        for (Slot inventorySlot : this.getMenu().slots) {
            if (inventorySlot instanceof CurioSlot curioSlot && !(inventorySlot instanceof CosmeticCurioSlot)) {
                this.addRenderableWidget(new RenderButton(curioSlot, this.getGuiLeft() + inventorySlot.x + 11, this.getGuiTop() + inventorySlot.y - 3, 8, 8, 75, 0, 8, CURIO_INVENTORY,
                        (button) -> NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new CPacketToggleRender(curioSlot.getIdentifier(), inventorySlot.getSlotIndex()))) {
                    @Override
                    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
                        this.setX(AccessoriesScreen.this.getGuiLeft() + inventorySlot.x + 11);
                        this.setY(AccessoriesScreen.this.getGuiTop() + inventorySlot.y - 3);
                    }
                });
            }
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        if (this.getRecipeBookComponent().isVisible() && this.widthTooNarrow) {
            this.renderBg(poseStack, partialTicks, mouseX, mouseY);
            this.getRecipeBookComponent().render(poseStack, mouseX, mouseY, partialTicks);
        } else {
            this.getRecipeBookComponent().render(poseStack, mouseX, mouseY, partialTicks);
            super.render(poseStack, mouseX, mouseY, partialTicks);
            this.getRecipeBookComponent().renderGhostRecipe(poseStack, this.getGuiLeft(), this.getGuiTop(), false, partialTicks);

            boolean isButtonHovered = false;
            for (Renderable renderable : this.renderables) {
                if (renderable instanceof RenderButton renderButton) {
                    renderButton.renderButtonOverlay(poseStack, mouseX, mouseY, partialTicks);
                    if (renderButton.isHovered()) {
                        isButtonHovered = true;
                    }
                }
            }
            this.isRenderButtonHovered = isButtonHovered;
            LocalPlayer clientPlayer = Minecraft.getInstance().player;
            if (!this.isRenderButtonHovered && clientPlayer != null && clientPlayer.inventoryMenu.getCarried().isEmpty() && this.getSlotUnderMouse() != null) {
                Slot slot = this.getSlotUnderMouse();
                if (slot instanceof CurioSlot curioSlot && !slot.hasItem()) {
                    this.renderTooltip(poseStack, Component.literal(curioSlot.getSlotName()), mouseX, mouseY);
                }
            }

            if (this.getMinecraft().player != null) {
                if (this.getMinecraft().player.isCreative() && this.destroyItemSlot == null) {
                    this.destroyItemSlot = new Slot(DESTROY_ITEM_CONTAINER, 0, 172, 142);
                    this.getMenu().slots.add(this.destroyItemSlot);
                } else if (!this.getMinecraft().player.isCreative() && this.destroyItemSlot != null) {
                    this.getMenu().slots.remove(this.destroyItemSlot);
                    this.destroyItemSlot = null;
                }
            }

            if (this.destroyItemSlot != null && this.isHovering(this.destroyItemSlot.x, this.destroyItemSlot.y, 16, 16, mouseX, mouseY)) {
                this.renderTooltip(poseStack, Component.translatable("inventory.binSlot"), mouseX, mouseY);
            }

            if (this.getMinecraft().player != null) {
                this.imageWidth = this.getMinecraft().player.isCreative() ? 176 + this.creativeXOffset() : 176;
            }
        }
        this.renderTooltip(poseStack, mouseX, mouseY);
        this.getRecipeBookComponent().renderTooltip(poseStack, this.getGuiLeft(), this.getGuiTop(), mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        if (this.getMinecraft().player != null) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, this.getMinecraft().player.isCreative() ? ACCESSORIES_INVENTORY_CREATIVE : ACCESSORIES_INVENTORY);
            int i = this.getGuiLeft();
            int j = this.getGuiTop();
            GuiComponent.blit(poseStack, i, j, 0, 0, this.getXSize() + this.creativeXOffset(), this.getYSize());
            InventoryScreen.renderEntityInInventoryFollowsMouse(poseStack, i + 33, j + 75, 30, (float) (i + 31) - mouseX, (float) (j + 75 - 50) - mouseY, this.getMinecraft().player);
        }
    }

    /**
     * @return The {@link Integer} y-offset for the GUI.
     */
    private int creativeXOffset() {
        return this.getMinecraft().player != null && this.getMinecraft().player.isCreative() ? 18 : 0;
    }

    @Override
    protected void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        Minecraft minecraft = this.getMinecraft();
        LocalPlayer clientPlayer = minecraft.player;
        if (clientPlayer != null && clientPlayer.inventoryMenu.getCarried().isEmpty()) {
            if (this.isRenderButtonHovered) {
                this.renderTooltip(poseStack, Component.translatable("gui.curios.toggle"), mouseX, mouseY);
            } else if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
                this.renderTooltip(poseStack, this.hoveredSlot.getItem(), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        if (this.getMinecraft().player != null) {
            this.font.draw(poseStack, this.title, 115, 6, 4210752);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.getRecipeBookComponent().isVisible() && this.widthTooNarrow) {
            this.getRecipeBookComponent().toggleVisibility();
            this.updateScreenPosition();
            return true;
        } else
        if (AetherKeys.OPEN_ACCESSORY_INVENTORY.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            LocalPlayer playerEntity = this.getMinecraft().player;
            if (playerEntity != null) {
                playerEntity.closeContainer();
            }
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    protected boolean isHovering(int rectX, int rectY, int rectWidth, int rectHeight, double pointX, double pointY) {
        if (this.isRenderButtonHovered) {
            return false;
        }
        return (!this.widthTooNarrow || !this.getRecipeBookComponent().isVisible()) && super.isHovering(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.getRecipeBookComponent().mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.getRecipeBookComponent());
            return true;
        } else {
            return (!this.widthTooNarrow || !this.getRecipeBookComponent().isVisible()) && super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.buttonClicked) {
            this.buttonClicked = false;
            return true;
        } else {
            return super.mouseReleased(mouseX, mouseY, button);
        }
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        return RecipeBookBehavior.super.hasClickedOutside(this, mouseX, mouseY, guiLeft, guiTop, mouseButton);
    }

    /**
     * [CODE COPY] {@link net.minecraft.client.gui.screens.inventory.AbstractContainerScreen}.<br><br>
     * Heavily modified to only have behavior for the item trash slot.
     */
    @Override
    protected void slotClicked(@Nullable Slot slot, int slotId, int mouseButton, ClickType type) {
        RecipeBookBehavior.super.slotClicked(this, slot);
        if (this.getMinecraft().player != null && this.getMinecraft().gameMode != null) {
            boolean flag = type == ClickType.QUICK_MOVE;
            if (slot != null || type == ClickType.QUICK_CRAFT) {
                if (slot == null || slot.mayPickup(this.getMinecraft().player)) {
                    if (slot == this.destroyItemSlot && this.destroyItemSlot != null && flag) {
                        for (int j = 0; j < this.getMinecraft().player.inventoryMenu.getItems().size(); ++j) {
                            this.getMinecraft().gameMode.handleCreativeModeItemAdd(ItemStack.EMPTY, j);
                            NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new CPacketDestroy());
                        }
                    } else {
                        if (slot == this.destroyItemSlot && this.destroyItemSlot != null) {
                            this.getMenu().setCarried(ItemStack.EMPTY);
                            PacketRelay.sendToServer(AetherPacketHandler.INSTANCE, new ClearItemPacket(this.getMinecraft().player.getId()));
                        }
                    }
                }
            }
            super.slotClicked(slot, slotId, mouseButton, type);
        }
    }

    @Override
    public void recipesUpdated() {
        RecipeBookBehavior.super.recipesUpdated(this);
    }

    @Override
    public RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }

    @Override
    public boolean canSeeEffects() {
        int i = this.getGuiLeft() + this.getXSize() + 2 + this.creativeXOffset();
        int j = this.width - i;
        return j > 13;
    }

    /**
     * Offsets the accessories screen button based on what screen is currently open.
     * @param screen The current {@link Screen}.
     * @return A {@link Tuple} containing the x and y {@link Integer}s.
     */
    public static Tuple<Integer, Integer> getButtonOffset(Screen screen) {
        int x = 0;
        int y = 0;
        if (screen instanceof InventoryScreen || screen instanceof CuriosScreen) {
            x = AetherConfig.CLIENT.button_inventory_x.get();
            y = AetherConfig.CLIENT.button_inventory_y.get();
        }
        if (screen instanceof CreativeModeInventoryScreen) {
            x = AetherConfig.CLIENT.button_creative_x.get();
            y = AetherConfig.CLIENT.button_creative_y.get();
        }
        if (screen instanceof AccessoriesScreen) {
            x = AetherConfig.CLIENT.button_accessories_x.get();
            y = AetherConfig.CLIENT.button_accessories_y.get();
        }
        return new Tuple<>(x, y);
    }
}
