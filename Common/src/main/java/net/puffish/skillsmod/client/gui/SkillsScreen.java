package net.puffish.skillsmod.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementWidgetType;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.client.SkillsClientMod;
import net.puffish.skillsmod.client.config.ClientBackgroundConfig;
import net.puffish.skillsmod.client.config.ClientFrameConfig;
import net.puffish.skillsmod.client.config.ClientIconConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillDefinitionConfig;
import net.puffish.skillsmod.client.data.ClientCategoryData;
import net.puffish.skillsmod.client.data.ClientSkillScreenData;
import net.puffish.skillsmod.client.network.packets.out.SkillClickOutPacket;
import net.puffish.skillsmod.client.rendering.ConnectionBatchedRenderer;
import net.puffish.skillsmod.client.rendering.ItemBatchedRenderer;
import net.puffish.skillsmod.client.rendering.TextureBatchedRenderer;
import net.puffish.skillsmod.common.BackgroundPosition;
import net.puffish.skillsmod.util.Bounds2i;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.BiConsumer;

public class SkillsScreen extends Screen {
	private static final Identifier WINDOW_TEXTURE = Identifier.parse("textures/gui/advancements/window.png");
	private static final Identifier EXPERIENCE_BAR_BACKGROUND_TEXTURE = Identifier.parse("hud/experience_bar_background");
	private static final Identifier EXPERIENCE_BAR_PROGRESS_TEXTURE = Identifier.parse("hud/experience_bar_progress");
	private static final Identifier TAB_ABOVE_LEFT_SELECTED_TEXTURE = Identifier.parse("advancements/tab_above_left_selected");
	private static final Identifier TAB_ABOVE_MIDDLE_SELECTED_TEXTURE = Identifier.parse("advancements/tab_above_middle_selected");
	private static final Identifier TAB_ABOVE_LEFT_TEXTURE = Identifier.parse("advancements/tab_above_left");
	private static final Identifier TAB_ABOVE_MIDDLE_TEXTURE = Identifier.parse("advancements/tab_above_middle");
	private static final WidgetSprites PAGE_FORWARD_TEXTURES = new WidgetSprites(
			Identifier.withDefaultNamespace("recipe_book/page_forward"), Identifier.withDefaultNamespace("recipe_book/page_forward_highlighted")
	);
	private static final WidgetSprites PAGE_BACKWARD_TEXTURES = new WidgetSprites(
			Identifier.withDefaultNamespace("recipe_book/page_backward"), Identifier.withDefaultNamespace("recipe_book/page_backward_highlighted")
	);
	private static final Identifier TRIAL_AVAILABLE_TEXTURE = Identifier.parse("icon/trial_available");

	private static final int TEXTURE_WIDTH = 256;
	private static final int TEXTURE_HEIGHT = 256;
	private static final int FRAME_WIDTH = 252;
	private static final int FRAME_HEIGHT = 140;
	private static final int FRAME_PADDING = 8;
	private static final int FRAME_CUT = 16;
	private static final int FRAME_EXPAND = 24;
	private static final int CONTENT_GROW = 32;
	private static final int TABS_HEIGHT = 28;
	private static final int HALF_FRAME_WIDTH = FRAME_WIDTH / 2;
	private static final int HALF_FRAME_HEIGHT = FRAME_HEIGHT / 2;

	private static final int COLOR_WHITE = ARGB.colorFromFloat(1f, 1f, 1f, 1f);
	private static final int COLOR_GRAY = ARGB.colorFromFloat(1f, 0.25f, 0.25f, 0.25f);

	private final ClientSkillScreenData data;

	private Optional<ClientCategoryData> optActiveCategoryData = Optional.empty();

	private Optional<Identifier> optActiveCategoryId;

	private Button nextButton;
	private Button prevButton;

	private float minScale = 1f;
	private float maxScale = 1f;

	private double dragStartX = 0;
	private double dragStartY = 0;
	private double dragTotal = 0;
	private boolean canDrag = false;

	private Bounds2i bounds = Bounds2i.zero();
	private boolean small = false;

	private int contentPaddingTop = 0;
	private int contentPaddingLeft = 0;
	private int contentPaddingRight = 0;
	private int contentPaddingBottom = 0;

	public SkillsScreen(ClientSkillScreenData data, Optional<Identifier> optCategoryId) {
		super(CommonComponents.EMPTY);
		this.data = data;
		optActiveCategoryId = optCategoryId;
	}

	@Override
	protected void init() {
		super.init();
		resize();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		resize();
	}

	private void resize() {
		this.small = optActiveCategoryData
				.map(activeCategoryData -> activeCategoryData.hasExperience() && this.width < 450)
				.orElse(false);

		if (this.small) {
			contentPaddingTop = 62;
			contentPaddingLeft = 17;
			contentPaddingRight = 17;
			contentPaddingBottom = 17;
		} else {
			contentPaddingTop = 54;
			contentPaddingLeft = 17;
			contentPaddingRight = 17;
			contentPaddingBottom = 17;
		}

		var halfWidth = this.width / 2;
		var halfHeight = this.height / 2;

		this.bounds = optActiveCategoryData
				.map(activeCategoryData -> activeCategoryData.getConfig().getBounds())
				.orElseGet(Bounds2i::zero);
		this.bounds.grow(CONTENT_GROW);
		this.bounds.extend(new Vector2i(contentPaddingLeft - halfWidth, contentPaddingTop - halfHeight));
		this.bounds.extend(new Vector2i(this.width - halfWidth - contentPaddingRight, this.height - halfHeight - contentPaddingBottom));

		var contentWidth = this.width - contentPaddingLeft - contentPaddingRight;
		var contentHeight = this.height - contentPaddingTop - contentPaddingBottom;

		if (bounds.width() * contentHeight > contentWidth * bounds.height()) {
			var halfSize = Mth.positiveCeilDiv(this.bounds.width() * contentHeight, contentWidth * 2);
			bounds.extendY(-halfSize);
			bounds.extendY(halfSize);
		} else {
			var halfSize = Mth.positiveCeilDiv(this.bounds.height() * contentWidth, contentHeight * 2);
			bounds.extendX(-halfSize);
			bounds.extendX(halfSize);
		}

		this.minScale = Math.max(
				((float) contentWidth) / ((float) this.bounds.width()),
				((float) contentHeight) / ((float) this.bounds.height())
		);
		this.maxScale = 1f;

		this.optActiveCategoryData.ifPresent(
				activeCategoryData -> applyChangesWithLimits(
						activeCategoryData.getX(),
						activeCategoryData.getY(),
						activeCategoryData.getScale(),
						activeCategoryData
				)
		);

		this.nextButton = new ImageButton(
				width - FRAME_PADDING - 12,
				FRAME_PADDING + 8,
				12,
				17,
				PAGE_FORWARD_TEXTURES,
				button -> data.incrementOffset()
		);
		this.prevButton = new ImageButton(
				FRAME_PADDING,
				FRAME_PADDING + 8,
				12,
				17,
				PAGE_BACKWARD_TEXTURES,
				button -> data.decrementOffset()
		);
	}

	private Vector2i getMousePos(double mouseX, double mouseY) {
		return new Vector2i(
				(int) mouseX,
				(int) mouseY
		);
	}

	private Vector2i getTransformedMousePos(double mouseX, double mouseY, ClientCategoryData activeCategoryData) {
		return new Vector2i(
				(int) Math.round((mouseX - activeCategoryData.getX() - width / 2.0) / activeCategoryData.getScale()),
				(int) Math.round((mouseY - activeCategoryData.getY() - height / 2.0) / activeCategoryData.getScale())
		);
	}

	private boolean isInsideTab(Vector2i mouse, int x) {
		return mouse.x >= x && mouse.y >= FRAME_PADDING && mouse.x < x + 28 && mouse.y < FRAME_PADDING + 32;
	}

	private boolean isInsideSkill(Vector2i transformedMouse, ClientSkillConfig skill, ClientSkillDefinitionConfig definition) {
		var halfSize = Math.round(13f * definition.size());
		return transformedMouse.x >= skill.x() - halfSize && transformedMouse.y >= skill.y() - halfSize && transformedMouse.x < skill.x() + halfSize && transformedMouse.y < skill.y() + halfSize;
	}

	private boolean isInsideContent(Vector2i mouse) {
		return mouse.x >= contentPaddingLeft && mouse.y >= contentPaddingTop && mouse.x < width - contentPaddingRight && mouse.y < height - contentPaddingBottom;
	}

	private boolean isInsideExperience(Vector2i mouse, int x, int y) {
		return mouse.x >= x && mouse.y >= y && mouse.x < x + 182 && mouse.y < y + 5;
	}

	private boolean isInsideArea(Vector2i mouse, int x1, int y1, int x2, int y2) {
		return mouse.x >= x1 && mouse.y >= y1 && mouse.x < x2 && mouse.y < y2;
	}

	private void syncCategory() {
		var opt = optActiveCategoryId.flatMap(data::getCategory);
		opt.ifPresent(ClientCategoryData::updateLastOpen);
		if (optActiveCategoryData.isEmpty() || optActiveCategoryData.orElseThrow() != opt.orElse(null)) {
			optActiveCategoryData = data.getCategories()
					.stream()
					.max(Comparator.comparing(ClientCategoryData::getLastOpen));
			optActiveCategoryId = optActiveCategoryData
					.map(data -> data.getConfig().id());
			resize();
		}
		optActiveCategoryData.ifPresent(ClientCategoryData::updateUnseenPoints);
	}

	private int getTabX(int i) {
		return FRAME_PADDING + (i - data.getOffset()) * 32 + (data.getOffset() > 0 ? (12 + 3) : 0);
	}

	private void forEachVisibleTab(BiConsumer<Integer, ClientCategoryData> consumer) {
		var it = data.getCategories().iterator();
		var i = 0;
		while (it.hasNext()) {
			var category = it.next();
			var x = getTabX(i);
			if (x >= FRAME_PADDING && x + 28 <= this.width - FRAME_PADDING - 12 - 3) {
				consumer.accept(x, category);
			}
			i++;
		}
	}

	private boolean hasNextButton() {
		var x = getTabX(data.getCategories().size() - 1);
		return x + 28 > this.width - FRAME_PADDING - 12 - 3;
	}

	private boolean hasPrevButton() {
		return data.getOffset() > 0;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		if (event.button() == GLFW.GLFW_MOUSE_BUTTON_1) {
			optActiveCategoryData.ifPresent(activeCategoryData ->
					mouseClickedWithCategory(event.x(), event.y(), activeCategoryData)
			);
		}

		if (hasNextButton()) {
			nextButton.mouseClicked(event, doubled);
		}
		if (hasPrevButton()) {
			prevButton.mouseClicked(event, doubled);
		}

		return true;
	}

	private void mouseClickedWithCategory(double mouseX, double mouseY, ClientCategoryData activeCategoryData) {
		var mouse = getMousePos(mouseX, mouseY);

		if (isInsideContent(mouse)) {
			dragStartX = mouseX - activeCategoryData.getX();
			dragStartY = mouseY - activeCategoryData.getY();
			dragTotal = 0;
			canDrag = true;
		} else {
			canDrag = false;
		}

		forEachVisibleTab((x, category) -> {
			if (isInsideTab(mouse, x)) {
				optActiveCategoryId = Optional.ofNullable(category.getConfig().id());
				syncCategory();
			}
		});
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (event.button() == GLFW.GLFW_MOUSE_BUTTON_1) {
			if (dragTotal > 2) {
				return true;
			}

			optActiveCategoryData.ifPresent(activeCategoryData ->
					mouseReleasedWithCategory(event.x(), event.y(), activeCategoryData)
			);
		}

		return true;
	}

	private void mouseReleasedWithCategory(double mouseX, double mouseY, ClientCategoryData activeCategoryData) {
		var mouse = getMousePos(mouseX, mouseY);
		var transformedMouse = getTransformedMousePos(mouseX, mouseY, activeCategoryData);
		var activeCategory = activeCategoryData.getConfig();

		if (isInsideContent(mouse)) {
			for (var skill : activeCategory.skills().values()) {
				var definition = activeCategory.definitions().get(skill.definitionId());
				if (definition == null) {
					continue;
				}

				if (isInsideSkill(transformedMouse, skill, definition)) {
					SkillsClientMod.getInstance()
							.getPacketSender()
							.send(new SkillClickOutPacket(activeCategory.id(), skill.id()));
				}
			}
		}
	}

	@Override
	public boolean keyPressed(KeyEvent input) {
		if (SkillsClientMod.OPEN_KEY_BINDING.matches(input)) {
			this.onClose();
			return true;
		}
		return super.keyPressed(input);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		this.syncCategory();

		this.drawContent(graphics, mouseX, mouseY);
		this.drawWindow(graphics, mouseX, mouseY);
		this.drawTabs(graphics, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double offsetX, double offsetY) {
		if (!canDrag) {
			return true;
		}

		if (event.button() == GLFW.GLFW_MOUSE_BUTTON_1) {
			dragTotal += Math.abs(offsetX);
			dragTotal += Math.abs(offsetY);
			if (dragTotal > 2) {
				optActiveCategoryData.ifPresent(activeCategoryData -> {
					applyChangesWithLimits(
							(int) Math.round(event.x() - dragStartX),
							(int) Math.round(event.y() - dragStartY),
							activeCategoryData.getScale(),
							activeCategoryData
					);
				});
			}
		}

		return true;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		optActiveCategoryData.ifPresent(activeCategoryData -> {
			var factor = (float) Math.pow(2, verticalAmount * 0.25);

			var x = activeCategoryData.getX();
			var y = activeCategoryData.getY();
			var scale = activeCategoryData.getScale();

			scale *= factor;

			if (scale < minScale) {
				scale = minScale;
				factor = minScale / scale;
			}
			if (scale > maxScale) {
				scale = maxScale;
				factor = maxScale / scale;
			}

			applyChangesWithLimits(
					x - (int) Math.round((factor - 1f) * (mouseX - x - this.width / 2f)),
					y - (int) Math.round((factor - 1f) * (mouseY - y - this.height / 2f)),
					scale,
					activeCategoryData
			);
		});

		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private void applyChangesWithLimits(int x, int y, float scale, ClientCategoryData activeCategoryData) {
		var halfWidth = this.width / 2;
		var halfHeight = this.height / 2;

		scale = Mth.clamp(
				scale,
				minScale,
				maxScale
		);

		activeCategoryData.setX(Mth.clamp(
				x,
				(int) Math.ceil(halfWidth - contentPaddingRight - bounds.max().x() * scale),
				(int) Math.floor(contentPaddingLeft - halfWidth - bounds.min().x() * scale)
		));
		activeCategoryData.setY(Mth.clamp(
				y,
				(int) Math.ceil(halfHeight - contentPaddingBottom - bounds.max().y() * scale),
				(int) Math.floor(contentPaddingTop - halfHeight - bounds.min().y() * scale)
		));
		activeCategoryData.setScale(scale);
	}

	private void drawIcon(GuiGraphicsExtractor graphics, TextureBatchedRenderer textureRenderer, ItemBatchedRenderer itemRenderer, ClientIconConfig icon, float sizeScale, int x, int y) {
		if (minecraft == null) {
			return;
		}

		var matrices = graphics.pose();
		matrices.pushMatrix();

		if (icon instanceof ClientIconConfig.ItemIconConfig itemIcon) {
			matrices.translate(x * (1f - sizeScale), y * (1f - sizeScale));
			matrices.scale(sizeScale, sizeScale);
			itemRenderer.emitItem(
					graphics,
					itemIcon.item(),
					x, y
			);
		} else if (icon instanceof ClientIconConfig.EffectIconConfig effectIcon) {
			var guiAtlasManager = minecraft.getAtlasManager().getAtlasOrThrow(AtlasIds.GUI);
			var texture = Gui.getMobEffectSprite(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effectIcon.effect()));
			var sprite = guiAtlasManager.getSprite(texture);
			var halfSize = Math.round(9f * sizeScale);
			var size = halfSize * 2;
			textureRenderer.emitSprite(
					graphics, sprite, GuiSpriteScaling.DEFAULT,
					x - halfSize, y - halfSize, size, size,
					COLOR_WHITE
			);
		} else if (icon instanceof ClientIconConfig.TextureIconConfig textureIcon) {
			var halfSize = Math.round(8f * sizeScale);
			var size = halfSize * 2;
			textureRenderer.emitTexture(
					graphics, textureIcon.texture(),
					x - halfSize, y - halfSize, size, size,
					COLOR_WHITE
			);
		}

		matrices.popMatrix();
	}

	private void drawFrame(GuiGraphicsExtractor graphics, TextureBatchedRenderer textureRenderer, ClientFrameConfig frame, float sizeScale, int x, int y, Skill.State state) {
		if (minecraft == null) {
			return;
		}

		var halfSize = Math.round(13f * sizeScale);
		var size = halfSize * 2;

		if (frame instanceof ClientFrameConfig.AdvancementFrameConfig advancementFrame) {
			var guiAtlasManager = minecraft.getAtlasManager().getAtlasOrThrow(AtlasIds.GUI);
			var status = switch (state) {
				case LOCKED, EXCLUDED, AVAILABLE, AFFORDABLE -> AdvancementWidgetType.UNOBTAINED;
				case UNLOCKED -> AdvancementWidgetType.OBTAINED;
			};
			var texture = status.frameSprite(advancementFrame.frame());
			var sprite = guiAtlasManager.getSprite(texture);
			var scaling = sprite.contents()
					.getAdditionalMetadata(GuiMetadataSection.TYPE)
					.orElse(GuiMetadataSection.DEFAULT)
					.scaling();
			var color = switch (state) {
				case LOCKED, EXCLUDED -> COLOR_GRAY;
				case AVAILABLE, AFFORDABLE, UNLOCKED -> COLOR_WHITE;
			};
			textureRenderer.emitSprite(
					graphics, sprite, scaling,
					x - halfSize, y - halfSize, size, size,
					color
			);
		} else if (frame instanceof ClientFrameConfig.TextureFrameConfig textureFrame) {
			switch (state) {
				case LOCKED -> textureFrame.lockedTexture().ifPresentOrElse(
						lockedTexture -> textureRenderer.emitTexture(
								graphics, lockedTexture,
								x - halfSize, y - halfSize, size, size,
								COLOR_WHITE
						),
						() -> textureRenderer.emitTexture(
								graphics, textureFrame.availableTexture(),
								x - halfSize, y - halfSize, size, size,
								COLOR_GRAY
						)
				);
				case AVAILABLE -> textureRenderer.emitTexture(
						graphics, textureFrame.availableTexture(),
						x - halfSize, y - halfSize, size, size,
						COLOR_WHITE
				);
				case AFFORDABLE -> textureFrame.affordableTexture().ifPresentOrElse(
						affordableTexture -> textureRenderer.emitTexture(
								graphics, affordableTexture,
								x - halfSize, y - halfSize, size, size,
								COLOR_WHITE
						),
						() -> textureRenderer.emitTexture(
								graphics, textureFrame.availableTexture(),
								x - halfSize, y - halfSize, size, size,
								COLOR_WHITE
						)
				);
				case UNLOCKED -> textureRenderer.emitTexture(
						graphics, textureFrame.unlockedTexture(),
						x - halfSize, y - halfSize, size, size,
						COLOR_WHITE
				);
				case EXCLUDED -> textureFrame.excludedTexture().ifPresentOrElse(
						excludedTexture -> textureRenderer.emitTexture(
								graphics, excludedTexture,
								x - halfSize, y - halfSize, size, size,
								COLOR_WHITE
						), () -> textureRenderer.emitTexture(
								graphics, textureFrame.availableTexture(),
								x - halfSize, y - halfSize, size, size,
								COLOR_GRAY
						)
				);
				default -> throw new UnsupportedOperationException();
			}
		}
	}

	private void drawBackground(GuiGraphicsExtractor graphics, ClientBackgroundConfig background) {
		var position = background.position();

		switch (position) {
			case TILE -> {
				graphics.blit(
						RenderPipelines.GUI_TEXTURED,
						background.texture(),
						bounds.min().x(),
						bounds.min().y(),
						0,
						0,
						bounds.width(),
						bounds.height(),
						background.width(),
						background.height()
				);
				return;
			}
			case FILL -> {
				if (bounds.width() * background.height() > background.width() * bounds.height()) {
					position = BackgroundPosition.FILL_WIDTH;
				} else {
					position = BackgroundPosition.FILL_HEIGHT;
				}
			}
			default -> { }
		}

		int x;
		int y;
		int width;
		int height;

		switch (position) {
			case NONE -> {
				width = background.width();
				height = background.height();
				x = width / -2;
				y = height / -2;
			}
			case FILL_WIDTH -> {
				x = bounds.min().x();
				width = bounds.width();
				y = bounds.min().y() + bounds.height() / 2 - Mth.positiveCeilDiv(background.height() * width, 2 * background.width());
				height = Mth.positiveCeilDiv(background.height() * width, background.width());
			}
			case FILL_HEIGHT -> {
				y = bounds.min().y();
				height = bounds.height();
				x = bounds.min().x() + bounds.width() / 2 - Mth.positiveCeilDiv(background.width() * height, 2 * background.height());
				width = Mth.positiveCeilDiv(background.width() * height, background.height());
			}
			default -> throw new IllegalStateException();
		}

		graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				background.texture(),
				x,
				y,
				0,
				0,
				width,
				height,
				width,
				height
		);
	}

	private void drawContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		var minX = contentPaddingLeft - 4;
		var minY = contentPaddingTop - 4;
		var maxX = this.width - contentPaddingRight + 4;
		var maxY = this.height - contentPaddingBottom + 4;

		var scissorArea = new ScreenRectangle(minX, minY, maxX - minX, maxY - minY)
				.transformAxisAligned(graphics.pose());

		graphics.enableScissor(minX, minY, maxX, maxY);

		graphics.fill(0, 0, width, height, 0xff000000);

		optActiveCategoryData.ifPresentOrElse(
				activeCategoryData -> drawContentWithCategory(graphics, mouseX, mouseY, scissorArea, activeCategoryData),
				() -> drawContentWithoutCategory(graphics)
		);

		graphics.disableScissor();
	}

	private void drawContentWithCategory(GuiGraphicsExtractor graphics, int mouseX, int mouseY, ScreenRectangle scissorArea, ClientCategoryData activeCategoryData) {
		if (minecraft == null) {
			return;
		}

		var mouse = getMousePos(mouseX, mouseY);
		var transformedMouse = getTransformedMousePos(mouseX, mouseY, activeCategoryData);
		var activeCategory = activeCategoryData.getConfig();

		var matrices = graphics.pose();
		matrices.pushMatrix();

		matrices.translate(activeCategoryData.getX() + this.width / 2f, activeCategoryData.getY() + this.height / 2f);
		matrices.scale(activeCategoryData.getScale(), activeCategoryData.getScale());

		drawBackground(graphics, activeCategory.background());

		var connectionRenderer = new ConnectionBatchedRenderer();

		for (var connection : activeCategory.normalConnections()) {
			activeCategoryData.getConnection(connection)
					.ifPresent(relation -> connectionRenderer.emitConnection(
							graphics,
							relation.getSkillA().x(),
							relation.getSkillA().y(),
							relation.getSkillB().x(),
							relation.getSkillB().y(),
							connection.bidirectional(),
							relation.getColor().fill().argb(),
							relation.getColor().stroke().argb()
					));
		}

		if (isInsideContent(mouse)) {
			var optHoveredSkill = activeCategory
					.skills()
					.values()
					.stream()
					.filter(skill -> activeCategory
							.getDefinitionById(skill.definitionId())
							.map(definition -> isInsideSkill(transformedMouse, skill, definition))
							.orElse(false)
					)
					.findFirst();

			optHoveredSkill.ifPresent(hoveredSkill -> {
				var definition = activeCategory.definitions().get(hoveredSkill.definitionId());
				if (definition == null) {
					return;
				}

				var lines = new ArrayList<FormattedCharSequence>();
				lines.add(definition.title().getVisualOrderText());
				lines.addAll(Tooltip.splitTooltip(minecraft, ComponentUtils.mergeStyles(
						definition.description().copy(),
						Style.EMPTY.applyFormat(ChatFormatting.GRAY)
				)));
				if (minecraft.hasShiftDown()) {
					lines.addAll(Tooltip.splitTooltip(minecraft, ComponentUtils.mergeStyles(
							definition.extraDescription().copy(),
							Style.EMPTY.applyFormat(ChatFormatting.GRAY)
					)));
				}
				if (minecraft.options.advancedItemTooltips) {
					lines.add(Component.literal(hoveredSkill.id()).withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText());
				}
				graphics.setTooltipForNextFrame(lines, mouseX, mouseY);

				var connections = activeCategory.skillExclusiveConnections().get(hoveredSkill.id());
				if (connections != null) {
					for (var connection : connections) {
						activeCategoryData.getConnection(connection)
								.ifPresent(relation -> connectionRenderer.emitConnection(
										graphics,
										relation.getSkillA().x(),
										relation.getSkillA().y(),
										relation.getSkillB().x(),
										relation.getSkillB().y(),
										connection.bidirectional(),
										relation.getColor().fill().argb(),
										relation.getColor().stroke().argb()
								));
					}
				}
			});
		}

		connectionRenderer.draw(graphics, scissorArea);

		var textureRenderer = new TextureBatchedRenderer();

		for (var skill : activeCategory.skills().values()) {
			activeCategory
					.getDefinitionById(skill.definitionId())
					.ifPresent(definition -> drawFrame(
							graphics,
							textureRenderer,
							definition.frame(),
							definition.size(),
							skill.x(),
							skill.y(),
							activeCategoryData.getSkillState(skill)
					));
		}

		textureRenderer.draw(graphics, minecraft.getTextureManager(), scissorArea);
		var itemRenderer = new ItemBatchedRenderer();

		for (var skill : activeCategory.skills().values()) {
			activeCategory
					.getDefinitionById(skill.definitionId())
					.ifPresent(definition -> drawIcon(
							graphics,
							textureRenderer,
							itemRenderer,
							definition.icon(),
							definition.size(),
							skill.x(),
							skill.y()
					));
		}

		textureRenderer.draw(graphics, minecraft.getTextureManager(), scissorArea);
		itemRenderer.draw(graphics, scissorArea);

		matrices.popMatrix();
	}

	private void drawContentWithoutCategory(GuiGraphicsExtractor graphics) {
		var tmpX = contentPaddingLeft + (width - contentPaddingLeft - contentPaddingRight) / 2;

		graphics.centeredText(
				this.font,
				Component.translatable("advancements.sad_label"),
				tmpX,
				height - contentPaddingBottom - this.font.lineHeight,
				0xffffffff
		);
		graphics.centeredText(
				this.font,
				Component.translatable("advancements.empty"),
				tmpX,
				contentPaddingTop + (height - contentPaddingTop - contentPaddingBottom - this.font.lineHeight) / 2,
				0xffffffff
		);
	}

	private void drawTabs(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		if (minecraft == null) {
			return;
		}

		if (hasNextButton()) {
			nextButton.extractRenderState(graphics, mouseX, mouseY, delta);
		}
		if (hasPrevButton()) {
			prevButton.extractRenderState(graphics, mouseX, mouseY, delta);
		}

		forEachVisibleTab((x, category) -> graphics.blitSprite(
				RenderPipelines.GUI_TEXTURED,
				optActiveCategoryData.orElse(null) == category
						? x == FRAME_PADDING
						? TAB_ABOVE_LEFT_SELECTED_TEXTURE
						: TAB_ABOVE_MIDDLE_SELECTED_TEXTURE
						: x == FRAME_PADDING
						? TAB_ABOVE_LEFT_TEXTURE
						: TAB_ABOVE_MIDDLE_TEXTURE,
				x,
				FRAME_PADDING,
				28,
				32
		));

		graphics.nextStratum();

		var mouse = getMousePos(mouseX, mouseY);

		var textureRenderer = new TextureBatchedRenderer();
		var itemRenderer = new ItemBatchedRenderer();

		forEachVisibleTab((x, category) -> {
			var categoryConfig = category.getConfig();

			drawIcon(
					graphics,
					textureRenderer,
					itemRenderer,
					categoryConfig.icon(),
					1f,
					x + 6 + 8,
					FRAME_PADDING + 9 + 8
			);

			if (isInsideTab(mouse, x)) {
				var lines = new ArrayList<FormattedCharSequence>();
				lines.add(categoryConfig.title().getVisualOrderText());
				lines.addAll(Tooltip.splitTooltip(minecraft, ComponentUtils.mergeStyles(
						categoryConfig.description().copy(),
						Style.EMPTY.applyFormat(ChatFormatting.GRAY)
				)));
				if (minecraft.hasShiftDown()) {
					lines.addAll(Tooltip.splitTooltip(minecraft, ComponentUtils.mergeStyles(
							categoryConfig.extraDescription().copy(),
							Style.EMPTY.applyFormat(ChatFormatting.GRAY)
					)));
				}
				if (minecraft.options.advancedItemTooltips) {
					lines.add(Component.literal(categoryConfig.id().toString()).withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText());
				}
				graphics.setTooltipForNextFrame(lines, mouseX, mouseY);
			}
		});

		var scissorArea = new ScreenRectangle(0, 0, width, height);
		textureRenderer.draw(graphics, minecraft.getTextureManager(), scissorArea);
		itemRenderer.draw(graphics, scissorArea);

		forEachVisibleTab((x, category) -> {
			if (category.hasUnseenPoints()) {
				graphics.blitSprite(
						RenderPipelines.GUI_TEXTURED,
						TRIAL_AVAILABLE_TEXTURE,
						x + 10,
						FRAME_PADDING - 5,
						8,
						8
				);
			}
		});
	}

	private void drawWindow(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		if (minecraft == null) {
			return;
		}

		// bottom left
		graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				WINDOW_TEXTURE,
				FRAME_PADDING,
				this.height - FRAME_PADDING - HALF_FRAME_HEIGHT,
				0,
				HALF_FRAME_HEIGHT,
				HALF_FRAME_WIDTH,
				HALF_FRAME_HEIGHT,
				TEXTURE_WIDTH,
				TEXTURE_HEIGHT
		);

		// bottom right
		graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				WINDOW_TEXTURE,
				this.width - FRAME_PADDING - HALF_FRAME_WIDTH,
				this.height - FRAME_PADDING - HALF_FRAME_HEIGHT,
				HALF_FRAME_WIDTH,
				HALF_FRAME_HEIGHT,
				HALF_FRAME_WIDTH,
				HALF_FRAME_HEIGHT,
				TEXTURE_WIDTH,
				TEXTURE_HEIGHT
		);

		// left
		graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				WINDOW_TEXTURE,
				FRAME_PADDING,
				FRAME_PADDING + HALF_FRAME_HEIGHT,
				0,
				HALF_FRAME_HEIGHT - 1,
				HALF_FRAME_WIDTH,
				this.height - FRAME_PADDING * 2 - FRAME_HEIGHT,
				HALF_FRAME_WIDTH,
				2,
				TEXTURE_WIDTH,
				TEXTURE_HEIGHT
		);

		// bottom
		graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				WINDOW_TEXTURE,
				FRAME_PADDING + HALF_FRAME_WIDTH,
				this.height - FRAME_PADDING - HALF_FRAME_HEIGHT,
				HALF_FRAME_WIDTH - 1,
				HALF_FRAME_HEIGHT,
				this.width - FRAME_PADDING * 2 - FRAME_WIDTH,
				HALF_FRAME_HEIGHT,
				2,
				HALF_FRAME_HEIGHT,
				TEXTURE_WIDTH,
				TEXTURE_HEIGHT
		);

		// right
		graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				WINDOW_TEXTURE,
				this.width - FRAME_PADDING - HALF_FRAME_WIDTH,
				FRAME_PADDING + HALF_FRAME_HEIGHT,
				HALF_FRAME_WIDTH,
				HALF_FRAME_HEIGHT - 1,
				HALF_FRAME_WIDTH,
				this.height - FRAME_PADDING * 2 - FRAME_HEIGHT,
				HALF_FRAME_WIDTH,
				2,
				TEXTURE_WIDTH,
				TEXTURE_HEIGHT
		);

		if (small) {
			// top left
			graphics.blit(
					RenderPipelines.GUI_TEXTURED,
					WINDOW_TEXTURE,
					FRAME_PADDING,
					FRAME_PADDING + TABS_HEIGHT,
					0,
					0,
					HALF_FRAME_WIDTH,
					FRAME_CUT,
					TEXTURE_WIDTH,
					TEXTURE_HEIGHT
			);
			graphics.blit(
					RenderPipelines.GUI_TEXTURED,
					WINDOW_TEXTURE,
					FRAME_PADDING,
					FRAME_PADDING + TABS_HEIGHT + FRAME_CUT,
					0,
					FRAME_CUT * 2 - FRAME_EXPAND,
					HALF_FRAME_WIDTH,
					HALF_FRAME_HEIGHT - TABS_HEIGHT - FRAME_CUT,
					TEXTURE_WIDTH,
					TEXTURE_HEIGHT
			);

			// top right
			graphics.blit(
					RenderPipelines.GUI_TEXTURED,
					WINDOW_TEXTURE,
					this.width - FRAME_PADDING - HALF_FRAME_WIDTH,
					FRAME_PADDING + TABS_HEIGHT,
					HALF_FRAME_WIDTH,
					0,
					HALF_FRAME_WIDTH,
					FRAME_CUT,
					TEXTURE_WIDTH,
					TEXTURE_HEIGHT
			);
			graphics.blit(
					RenderPipelines.GUI_TEXTURED,
					WINDOW_TEXTURE,
					this.width - FRAME_PADDING - HALF_FRAME_WIDTH,
					FRAME_PADDING + TABS_HEIGHT + FRAME_CUT,
					HALF_FRAME_WIDTH,
					FRAME_CUT * 2 - FRAME_EXPAND,
					HALF_FRAME_WIDTH,
					HALF_FRAME_HEIGHT - TABS_HEIGHT - FRAME_CUT,
					TEXTURE_WIDTH,
					TEXTURE_HEIGHT
			);

			// top
			graphics.blit(
					RenderPipelines.GUI_TEXTURED,
					WINDOW_TEXTURE,
					FRAME_PADDING + HALF_FRAME_WIDTH,
					FRAME_PADDING + TABS_HEIGHT,
					HALF_FRAME_WIDTH - 1,
					0,
					this.width - FRAME_PADDING * 2 - FRAME_WIDTH,
					FRAME_CUT,
					2,
					FRAME_CUT,
					TEXTURE_WIDTH,
					TEXTURE_HEIGHT
			);
			graphics.blit(
					RenderPipelines.GUI_TEXTURED,
					WINDOW_TEXTURE,
					FRAME_PADDING + HALF_FRAME_WIDTH,
					FRAME_PADDING + TABS_HEIGHT + FRAME_CUT,
					HALF_FRAME_WIDTH - 1,
					FRAME_CUT * 2 - FRAME_EXPAND,
					this.width - FRAME_PADDING * 2 - FRAME_WIDTH,
					HALF_FRAME_HEIGHT - FRAME_CUT,
					2,
					HALF_FRAME_HEIGHT - FRAME_CUT,
					TEXTURE_WIDTH,
					TEXTURE_HEIGHT
			);
		} else {
			// top left
			graphics.blit(
					RenderPipelines.GUI_TEXTURED,
					WINDOW_TEXTURE,
					FRAME_PADDING,
					FRAME_PADDING + TABS_HEIGHT,
					0,
					0,
					HALF_FRAME_WIDTH,
					HALF_FRAME_HEIGHT - TABS_HEIGHT,
					TEXTURE_WIDTH,
					TEXTURE_HEIGHT
			);

			// top right
			graphics.blit(
					RenderPipelines.GUI_TEXTURED,
					WINDOW_TEXTURE,
					this.width - FRAME_PADDING - HALF_FRAME_WIDTH,
					FRAME_PADDING + TABS_HEIGHT,
					HALF_FRAME_WIDTH,
					0,
					HALF_FRAME_WIDTH,
					HALF_FRAME_HEIGHT - TABS_HEIGHT,
					TEXTURE_WIDTH,
					TEXTURE_HEIGHT
			);

			// top
			graphics.blit(
					RenderPipelines.GUI_TEXTURED,
					WINDOW_TEXTURE,
					FRAME_PADDING + HALF_FRAME_WIDTH,
					FRAME_PADDING + TABS_HEIGHT,
					HALF_FRAME_WIDTH - 1,
					0,
					this.width - FRAME_PADDING * 2 - FRAME_WIDTH,
					HALF_FRAME_HEIGHT,
					2,
					HALF_FRAME_HEIGHT,
					TEXTURE_WIDTH,
					TEXTURE_HEIGHT
			);
		}

		var tmpText = SkillsMod.createTranslatable("text", "skills");
		var tmpX = FRAME_PADDING + 8;
		var tmpY = FRAME_PADDING + TABS_HEIGHT + 6;

		graphics.text(
				this.font,
				tmpText,
				tmpX,
				tmpY,
				0xff404040,
				false
		);

		optActiveCategoryData.ifPresent(activeCategoryData ->
				drawWindowWithCategory(graphics, mouseX, mouseY, activeCategoryData)
		);
	}

	private void drawWindowWithCategory(GuiGraphicsExtractor graphics, int mouseX, int mouseY, ClientCategoryData activeCategoryData) {
		var mouse = getMousePos(mouseX, mouseY);
		var activeCategory = activeCategoryData.getConfig();

		var tmpX = this.width - FRAME_PADDING - 7;
		var tmpY = FRAME_PADDING + TABS_HEIGHT + 6;

		var startX = tmpX;

		var tmpText = Component.literal(activeCategoryData.getPointsLeft()
				+ (activeCategory.spentPointsLimit() == Integer.MAX_VALUE ? "" : "/" + activeCategoryData.getSpentPointsLeft())
		);

		tmpX -= this.font.width(tmpText);
		tmpX -= 1;

		var pointsColor = activeCategory.colors().points();
		var pointsStrokeColor = pointsColor.stroke().argb();
		var pointsFillColor = pointsColor.fill().argb();
		graphics.text(this.font, tmpText, tmpX - 1, tmpY, pointsStrokeColor, false);
		graphics.text(this.font, tmpText, tmpX, tmpY - 1, pointsStrokeColor, false);
		graphics.text(this.font, tmpText, tmpX + 1, tmpY, pointsStrokeColor, false);
		graphics.text(this.font, tmpText, tmpX, tmpY + 1, pointsStrokeColor, false);
		graphics.text(this.font, tmpText, tmpX, tmpY, pointsFillColor, false);
		tmpX -= 1;

		tmpText = SkillsMod.createTranslatable("text", "points_left");
		tmpX -= this.font.width(tmpText);
		graphics.text(
				this.font,
				tmpText,
				tmpX,
				tmpY,
				0xff404040,
				false
		);

		if (isInsideArea(mouse, tmpX, tmpY, startX, tmpY + this.font.lineHeight)) {
			var lines = new ArrayList<FormattedCharSequence>();
			lines.add(SkillsMod.createTranslatable(
					"tooltip",
					"earned_points",
					activeCategoryData.getEarnedPoints()
			).getVisualOrderText());
			lines.add(SkillsMod.createTranslatable(
					"tooltip",
					"spent_points",
					activeCategoryData.getSpentPoints()
							+ (activeCategory.spentPointsLimit() == Integer.MAX_VALUE ? "" : "/" + activeCategory.spentPointsLimit())
			).getVisualOrderText());
			graphics.setTooltipForNextFrame(lines, mouseX, mouseY);
		}

		if (activeCategoryData.hasExperience()) {
			if (small) {
				tmpX = this.width - FRAME_PADDING - 8 - 182;
				tmpY = TABS_HEIGHT + 25;
			} else {
				tmpX = (this.width - 182) / 2;
				tmpY = TABS_HEIGHT + 15;
			}

			graphics.blitSprite(
					RenderPipelines.GUI_TEXTURED,
					EXPERIENCE_BAR_BACKGROUND_TEXTURE,
					tmpX,
					tmpY,
					182,
					5
			);
			var width = Math.min(182, (int) (activeCategoryData.getExperienceProgress() * 183f));
			if (width > 0) {
				graphics.blitSprite(
						RenderPipelines.GUI_TEXTURED,
						EXPERIENCE_BAR_PROGRESS_TEXTURE,
						182,
						5,
						0,
						0,
						tmpX,
						tmpY,
						width,
						5
				);
			}

			if (isInsideExperience(mouse, tmpX, tmpY)) {
				var lines = new ArrayList<FormattedCharSequence>();
				lines.add(SkillsMod.createTranslatable(
						"tooltip",
						"current_level",
						activeCategoryData.getCurrentLevel()
								+ (activeCategory.levelLimit() == Integer.MAX_VALUE ? "" : "/" + activeCategory.levelLimit())
				).getVisualOrderText());
				lines.add(SkillsMod.createTranslatable(
						"tooltip",
						"experience_progress",
						activeCategoryData.getCurrentExperience(),
						activeCategoryData.getRequiredExperience(),
						Mth.floor(activeCategoryData.getExperienceProgress() * 100f)
				).getVisualOrderText());
				lines.add(SkillsMod.createTranslatable(
						"tooltip",
						"to_next_level",
						activeCategoryData.getExperienceToNextLevel()
				).getVisualOrderText());
				graphics.setTooltipForNextFrame(lines, mouseX, mouseY);
			}

			if (activeCategoryData.getCurrentLevel() > 0) {
				tmpText = Component.literal("" + activeCategoryData.getCurrentLevel());
				tmpX += (182 - this.font.width(tmpText)) / 2;
				tmpY -= 1;
				graphics.text(this.font, tmpText, tmpX - 1, tmpY, pointsStrokeColor, false);
				graphics.text(this.font, tmpText, tmpX, tmpY - 1, pointsStrokeColor, false);
				graphics.text(this.font, tmpText, tmpX + 1, tmpY, pointsStrokeColor, false);
				graphics.text(this.font, tmpText, tmpX, tmpY + 1, pointsStrokeColor, false);
				graphics.text(this.font, tmpText, tmpX, tmpY, pointsFillColor, false);
			}
		}
	}

}
