package net.puffish.skillsmod.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Scaling;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.client.SkillsClientMod;
import net.puffish.skillsmod.client.config.ClientBackgroundConfig;
import net.puffish.skillsmod.client.config.ClientFrameConfig;
import net.puffish.skillsmod.client.config.ClientIconConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillDefinitionConfig;
import net.puffish.skillsmod.client.data.ClientCategoryData;
import net.puffish.skillsmod.client.network.packets.out.SkillClickOutPacket;
import net.puffish.skillsmod.client.rendering.ConnectionBatchedRenderer;
import net.puffish.skillsmod.client.rendering.ItemBatchedRenderer;
import net.puffish.skillsmod.client.rendering.TextureBatchedRenderer;
import net.puffish.skillsmod.common.BackgroundPosition;
import net.puffish.skillsmod.util.Bounds2i;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class SkillsScreen extends Screen {
	private static final Identifier WINDOW_TEXTURE = Identifier.of("textures/gui/advancements/window.png");
	private static final Identifier EXPERIENCE_BAR_BACKGROUND_TEXTURE = Identifier.of("hud/experience_bar_background");
	private static final Identifier EXPERIENCE_BAR_PROGRESS_TEXTURE = Identifier.of("hud/experience_bar_progress");
	private static final Identifier TAB_ABOVE_LEFT_SELECTED_TEXTURE = Identifier.of("advancements/tab_above_left_selected");
	private static final Identifier TAB_ABOVE_MIDDLE_SELECTED_TEXTURE = Identifier.of("advancements/tab_above_middle_selected");
	private static final Identifier TAB_ABOVE_LEFT_TEXTURE = Identifier.of("advancements/tab_above_left");
	private static final Identifier TAB_ABOVE_MIDDLE_TEXTURE = Identifier.of("advancements/tab_above_middle");

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

	private static final Vector4fc COLOR_WHITE = new Vector4f(1f, 1f, 1f, 1f);
	private static final Vector4fc COLOR_GRAY = new Vector4f(0.25f, 0.25f, 0.25f, 1f);

	private final Map<Identifier, ClientCategoryData> categories;

	private Optional<ClientCategoryData> optActiveCategoryData = Optional.empty();

	private Optional<Identifier> optActiveCategoryId;

	private float minScale = 1f;
	private float maxScale = 1f;

	private double dragStartX = 0;
	private double dragStartY = 0;
	private boolean dragging = false;

	private Bounds2i bounds = Bounds2i.zero();
	private boolean small = false;

	private int contentPaddingTop = 0;
	private int contentPaddingLeft = 0;
	private int contentPaddingRight = 0;
	private int contentPaddingBottom = 0;

	public SkillsScreen(Map<Identifier, ClientCategoryData> categories, Optional<Identifier> optCategoryId) {
		super(ScreenTexts.EMPTY);
		this.categories = categories;
		optActiveCategoryId = optCategoryId;
	}

	@Override
	protected void init() {
		super.init();
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

		var halfContentWidth = MathHelper.ceilDiv(this.bounds.height() * contentWidth, contentHeight * 2);
		var halfContentHeight = MathHelper.ceilDiv(this.bounds.width() * contentHeight, contentWidth * 2);

		this.bounds.extend(new Vector2i(-halfContentWidth, -halfContentHeight));
		this.bounds.extend(new Vector2i(halfContentWidth, halfContentHeight));

		this.minScale = Math.max(
				((float) contentWidth) / ((float) this.bounds.width()),
				((float) contentHeight) / ((float) this.bounds.height())
		);
		this.maxScale = 1f;
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

	private boolean isInsideTab(Vector2i mouse, int i) {
		return mouse.x >= FRAME_PADDING + i * 32 && mouse.y >= FRAME_PADDING && mouse.x < FRAME_PADDING + i * 32 + 28 && mouse.y < FRAME_PADDING + 32;
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
		var opt = optActiveCategoryId.flatMap(
				activeCategoryId -> Optional.ofNullable(categories.get(activeCategoryId))
		);
		opt.ifPresent(ClientCategoryData::updateLastOpen);
		if (optActiveCategoryData.isEmpty() || optActiveCategoryData.orElseThrow() != opt.orElse(null)) {
			optActiveCategoryData = categories.values()
					.stream()
					.max(Comparator.comparing(ClientCategoryData::getLastOpen));
			resize();
		}
	}

	private void forEachCategory(BiConsumer<Integer, ClientCategoryData> consumer) {
		var it = categories.values().iterator();
		var i = 0;
		while (it.hasNext()) {
			consumer.accept(i, it.next());
			i++;
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		optActiveCategoryData.ifPresent(activeCategoryData ->
				mouseClickedWithCategory(mouseX, mouseY, button, activeCategoryData)
		);

		return super.mouseClicked(mouseX, mouseY, button);
	}

	private void mouseClickedWithCategory(double mouseX, double mouseY, int button, ClientCategoryData activeCategoryData) {
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

			if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
				dragStartX = mouseX - activeCategoryData.getX();
				dragStartY = mouseY - activeCategoryData.getY();
				dragging = true;
			}
		} else {
			dragging = false;
		}

		forEachCategory((i, category) -> {
			if (isInsideTab(mouse, i)) {
				optActiveCategoryId = Optional.ofNullable(category.getConfig().id());
				syncCategory();
			}
		});
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (SkillsClientMod.OPEN_KEY_BINDING.matchesKey(keyCode, scanCode)) {
			this.close();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.syncCategory();

		this.renderBackground(context, mouseX, mouseY, delta);
		this.drawContent(context, mouseX, mouseY);
		this.drawWindow(context, mouseX, mouseY);
		this.drawTabs(context, mouseX, mouseY);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (dragging) {
			if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
				optActiveCategoryData.ifPresent(activeCategoryData -> {
					applyChangesWithLimits(
							(int) Math.round(mouseX - dragStartX),
							(int) Math.round(mouseY - dragStartY),
							activeCategoryData.getScale(),
							activeCategoryData
					);
				});

				return true;
			}
		}

		return false;
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

		activeCategoryData.setX(MathHelper.clamp(
				x,
				(int) Math.ceil(halfWidth - contentPaddingRight - bounds.max().x() * scale),
				(int) Math.floor(contentPaddingLeft - halfWidth - bounds.min().x() * scale)
		));
		activeCategoryData.setY(MathHelper.clamp(
				y,
				(int) Math.ceil(halfHeight - contentPaddingBottom - bounds.max().y() * scale),
				(int) Math.floor(contentPaddingTop - halfHeight - bounds.min().y() * scale)
		));
		activeCategoryData.setScale(scale);
	}

	private void drawIcon(DrawContext context, TextureBatchedRenderer textureRenderer, ItemBatchedRenderer itemRenderer, ClientIconConfig icon, float sizeScale, int x, int y) {
		if (client == null) {
			return;
		}

		var matrices = context.getMatrices();
		matrices.push();

		if (icon instanceof ClientIconConfig.ItemIconConfig itemIcon) {
			matrices.translate(x * (1f - sizeScale), y * (1f - sizeScale), 1f);
			matrices.scale(sizeScale, sizeScale, 1);
			itemRenderer.emitItem(
					context,
					itemIcon.item(),
					x, y
			);
		} else if (icon instanceof ClientIconConfig.EffectIconConfig effectIcon) {
			matrices.translate(0f, 0f, 1f);
			var sprite = client.getStatusEffectSpriteManager().getSprite(Registries.STATUS_EFFECT.getEntry(effectIcon.effect()));
			var halfSize = Math.round(9f * sizeScale);
			var size = halfSize * 2;
			textureRenderer.emitSprite(
					context, sprite, Scaling.STRETCH,
					x - halfSize, y - halfSize, size, size,
					COLOR_WHITE
			);
		} else if (icon instanceof ClientIconConfig.TextureIconConfig textureIcon) {
			matrices.translate(0f, 0f, 1f);
			var halfSize = Math.round(8f * sizeScale);
			var size = halfSize * 2;
			textureRenderer.emitTexture(
					context, textureIcon.texture(),
					x - halfSize, y - halfSize, size, size,
					COLOR_WHITE
			);
		}

		matrices.pop();
	}

	private void drawFrame(DrawContext context, TextureBatchedRenderer textureRenderer, ClientFrameConfig frame, float sizeScale, int x, int y, Skill.State state) {
		if (client == null) {
			return;
		}

		var halfSize = Math.round(13f * sizeScale);
		var size = halfSize * 2;

		if (frame instanceof ClientFrameConfig.AdvancementFrameConfig advancementFrame) {
			var guiAtlasManager = client.getGuiAtlasManager();
			var status = switch (state) {
				case LOCKED, EXCLUDED, AVAILABLE, AFFORDABLE -> AdvancementObtainedStatus.UNOBTAINED;
				case UNLOCKED -> AdvancementObtainedStatus.OBTAINED;
			};
			var texture = status.getFrameTexture(advancementFrame.frame());
			var sprite = guiAtlasManager.getSprite(texture);
			var scaling = guiAtlasManager.getScaling(sprite);
			var color = switch (state) {
				case LOCKED, EXCLUDED -> COLOR_GRAY;
				case AVAILABLE, AFFORDABLE, UNLOCKED -> COLOR_WHITE;
			};
			textureRenderer.emitSprite(
					context, sprite, scaling,
					x - halfSize, y - halfSize, size, size,
					color
			);
		} else if (frame instanceof ClientFrameConfig.TextureFrameConfig textureFrame) {
			switch (state) {
				case LOCKED -> textureFrame.lockedTexture().ifPresentOrElse(
						lockedTexture -> textureRenderer.emitTexture(
								context, lockedTexture,
								x - halfSize, y - halfSize, size, size,
								COLOR_WHITE
						),
						() -> textureRenderer.emitTexture(
								context, textureFrame.availableTexture(),
								x - halfSize, y - halfSize, size, size,
								COLOR_GRAY
						)
				);
				case AVAILABLE -> textureRenderer.emitTexture(
						context, textureFrame.availableTexture(),
						x - halfSize, y - halfSize, size, size,
						COLOR_WHITE
				);
				case AFFORDABLE -> textureFrame.affordableTexture().ifPresentOrElse(
						affordableTexture -> textureRenderer.emitTexture(
								context, affordableTexture,
								x - halfSize, y - halfSize, size, size,
								COLOR_WHITE
						),
						() -> textureRenderer.emitTexture(
								context, textureFrame.availableTexture(),
								x - halfSize, y - halfSize, size, size,
								COLOR_WHITE
						)
				);
				case UNLOCKED -> textureRenderer.emitTexture(
						context, textureFrame.unlockedTexture(),
						x - halfSize, y - halfSize, size, size,
						COLOR_WHITE
				);
				case EXCLUDED -> textureFrame.excludedTexture().ifPresentOrElse(
						excludedTexture -> textureRenderer.emitTexture(
								context, excludedTexture,
								x - halfSize, y - halfSize, size, size,
								COLOR_WHITE
						), () -> textureRenderer.emitTexture(
								context, textureFrame.availableTexture(),
								x - halfSize, y - halfSize, size, size,
								COLOR_GRAY
						)
				);
				default -> throw new UnsupportedOperationException();
			}
		}
	}

	private void drawBackground(DrawContext context, ClientBackgroundConfig background) {
		var position = background.position();

		switch (position) {
			case TILE -> {
				context.drawTexture(
						RenderLayer::getGuiTextured,
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
				y = -MathHelper.ceilDiv(background.height() * width, 2 * background.width());
				height = -2 * y;
			}
			case FILL_HEIGHT -> {
				y = bounds.min().y();
				height = bounds.height();
				x = -MathHelper.ceilDiv(background.width() * height, 2 * background.height());
				width = -2 * x;
			}
			default -> throw new IllegalStateException();
		}

		context.drawTexture(
				RenderLayer::getGuiTextured,
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

	private void drawContent(DrawContext context, double mouseX, double mouseY) {
		context.enableScissor(
				contentPaddingLeft - 4,
				contentPaddingTop - 4,
				this.width - contentPaddingRight + 4,
				this.height - contentPaddingBottom + 4
		);

		context.fill(0, 0, width, height, 0xff000000);

		optActiveCategoryData.ifPresentOrElse(
				activeCategoryData -> drawContentWithCategory(context, mouseX, mouseY, activeCategoryData),
				() -> drawContentWithoutCategory(context)
		);

		context.disableScissor();
	}

	private void drawContentWithCategory(DrawContext context, double mouseX, double mouseY, ClientCategoryData activeCategoryData) {
		if (client == null) {
			return;
		}

		var mouse = getMousePos(mouseX, mouseY);
		var transformedMouse = getTransformedMousePos(mouseX, mouseY, activeCategoryData);
		var activeCategory = activeCategoryData.getConfig();

		var matrices = context.getMatrices();
		matrices.push();

		matrices.translate(activeCategoryData.getX() + this.width / 2f, activeCategoryData.getY() + this.height / 2f, 1f);
		matrices.scale(activeCategoryData.getScale(), activeCategoryData.getScale(), 1f);

		drawBackground(context, activeCategory.background());

		matrices.translate(0f, 0f, 1f);

		var connectionRenderer = new ConnectionBatchedRenderer();

		for (var connection : activeCategory.normalConnections()) {
			activeCategoryData.getConnection(connection)
					.ifPresent(relation -> connectionRenderer.emitConnection(
							context,
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

				var lines = new ArrayList<OrderedText>();
				lines.add(definition.title().asOrderedText());
				lines.addAll(Tooltip.wrapLines(client, Texts.setStyleIfAbsent(
						definition.description().copy(),
						Style.EMPTY.withFormatting(Formatting.GRAY)
				)));
				if (Screen.hasShiftDown()) {
					lines.addAll(Tooltip.wrapLines(client, Texts.setStyleIfAbsent(
							definition.extraDescription().copy(),
							Style.EMPTY.withFormatting(Formatting.GRAY)
					)));
				}
				if (client.options.advancedItemTooltips) {
					lines.add(Text.literal(hoveredSkill.id()).formatted(Formatting.DARK_GRAY).asOrderedText());
				}
				setTooltip(lines);

				var connections = activeCategory.skillExclusiveConnections().get(hoveredSkill.id());
				if (connections != null) {
					for (var connection : connections) {
						activeCategoryData.getConnection(connection)
								.ifPresent(relation -> connectionRenderer.emitConnection(
										context,
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

		connectionRenderer.draw();

		var textureRenderer = new TextureBatchedRenderer();
		var itemRenderer = new ItemBatchedRenderer();

		for (var skill : activeCategory.skills().values()) {
			activeCategory
					.getDefinitionById(skill.definitionId())
					.ifPresent(definition -> {
						drawFrame(
								context,
								textureRenderer,
								definition.frame(),
								definition.size(),
								skill.x(),
								skill.y(),
								activeCategoryData.getSkillState(skill)
						);
						drawIcon(
								context,
								textureRenderer,
								itemRenderer,
								definition.icon(),
								definition.size(),
								skill.x(),
								skill.y()
						);
					});
		}

		textureRenderer.draw();
		itemRenderer.draw();

		matrices.pop();
	}

	private void drawContentWithoutCategory(DrawContext context) {
		var tmpX = contentPaddingLeft + (width - contentPaddingLeft - contentPaddingRight) / 2;

		context.drawCenteredTextWithShadow(
				this.textRenderer,
				Text.translatable("advancements.sad_label"),
				tmpX,
				height - contentPaddingBottom - this.textRenderer.fontHeight,
				0xffffffff
		);
		context.drawCenteredTextWithShadow(
				this.textRenderer,
				Text.translatable("advancements.empty"),
				tmpX,
				contentPaddingTop + (height - contentPaddingTop - contentPaddingBottom - this.textRenderer.fontHeight) / 2,
				0xffffffff
		);
	}

	private void drawTabs(DrawContext context, double mouseX, double mouseY) {
		if (client == null) {
			return;
		}

		forEachCategory((i, category) -> context.drawGuiTexture(
				RenderLayer::getGuiTexturedOverlay,
				optActiveCategoryData.orElse(null) == category
						? i == 0
						? TAB_ABOVE_LEFT_SELECTED_TEXTURE
						: TAB_ABOVE_MIDDLE_SELECTED_TEXTURE
						: i == 0
						? TAB_ABOVE_LEFT_TEXTURE
						: TAB_ABOVE_MIDDLE_TEXTURE,
				FRAME_PADDING + 32 * i,
				FRAME_PADDING,
				28,
				32
		));

		context.draw();

		var mouse = getMousePos(mouseX, mouseY);

		var textureRenderer = new TextureBatchedRenderer();
		var itemBatch = new ItemBatchedRenderer();

		forEachCategory((i, category) -> {
			var categoryConfig = category.getConfig();

			drawIcon(
					context,
					textureRenderer,
					itemBatch,
					categoryConfig.icon(),
					1f,
					FRAME_PADDING + 32 * i + 6 + 8,
					FRAME_PADDING + 9 + 8
			);

			if (isInsideTab(mouse, i)) {
				var lines = new ArrayList<OrderedText>();
				lines.add(categoryConfig.title().asOrderedText());
				if (client.options.advancedItemTooltips) {
					lines.add(Text.literal(categoryConfig.id().toString()).formatted(Formatting.DARK_GRAY).asOrderedText());
				}
				setTooltip(lines);
			}
		});

		textureRenderer.draw();
		itemBatch.draw();
	}

	private void drawWindow(DrawContext context, double mouseX, double mouseY) {
		if (client == null) {
			return;
		}

		// bottom left
		context.drawTexture(
				RenderLayer::getGuiTexturedOverlay,
				WINDOW_TEXTURE,
				FRAME_PADDING,
				this.height - FRAME_PADDING - HALF_FRAME_HEIGHT + 1,
				0,
				HALF_FRAME_HEIGHT,
				HALF_FRAME_WIDTH,
				HALF_FRAME_HEIGHT,
				TEXTURE_WIDTH,
				TEXTURE_HEIGHT
		);

		// bottom right
		context.drawTexture(
				RenderLayer::getGuiTexturedOverlay,
				WINDOW_TEXTURE,
				this.width - FRAME_PADDING - HALF_FRAME_WIDTH + 1,
				this.height - FRAME_PADDING - HALF_FRAME_HEIGHT + 1,
				HALF_FRAME_WIDTH,
				HALF_FRAME_HEIGHT,
				HALF_FRAME_WIDTH,
				HALF_FRAME_HEIGHT,
				TEXTURE_WIDTH,
				TEXTURE_HEIGHT
		);

		// left
		context.drawTexture(
				RenderLayer::getGuiTexturedOverlay,
				WINDOW_TEXTURE,
				FRAME_PADDING,
				FRAME_PADDING + HALF_FRAME_HEIGHT,
				0,
				HALF_FRAME_HEIGHT - 1,
				HALF_FRAME_WIDTH,
				this.height - FRAME_PADDING * 2 - FRAME_HEIGHT + 1,
				HALF_FRAME_WIDTH,
				2,
				TEXTURE_WIDTH,
				TEXTURE_HEIGHT
		);

		// bottom
		context.drawTexture(
				RenderLayer::getGuiTexturedOverlay,
				WINDOW_TEXTURE,
				FRAME_PADDING + HALF_FRAME_WIDTH,
				this.height - FRAME_PADDING - HALF_FRAME_HEIGHT + 1,
				HALF_FRAME_WIDTH - 1,
				HALF_FRAME_HEIGHT,
				this.width - FRAME_PADDING * 2 - FRAME_WIDTH + 1,
				HALF_FRAME_HEIGHT,
				2,
				HALF_FRAME_HEIGHT,
				TEXTURE_WIDTH,
				TEXTURE_HEIGHT
		);

		// right
		context.drawTexture(
				RenderLayer::getGuiTexturedOverlay,
				WINDOW_TEXTURE,
				this.width - FRAME_PADDING - HALF_FRAME_WIDTH + 1,
				FRAME_PADDING + HALF_FRAME_HEIGHT,
				HALF_FRAME_WIDTH,
				HALF_FRAME_HEIGHT - 1,
				HALF_FRAME_WIDTH,
				this.height - FRAME_PADDING * 2 - FRAME_HEIGHT + 1,
				HALF_FRAME_WIDTH,
				2,
				TEXTURE_WIDTH,
				TEXTURE_HEIGHT
		);

		if (small) {
			// top left
			context.drawTexture(
					RenderLayer::getGuiTexturedOverlay,
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
			context.drawTexture(
					RenderLayer::getGuiTexturedOverlay,
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
			context.drawTexture(
					RenderLayer::getGuiTexturedOverlay,
					WINDOW_TEXTURE,
					this.width - FRAME_PADDING - HALF_FRAME_WIDTH + 1,
					FRAME_PADDING + TABS_HEIGHT,
					HALF_FRAME_WIDTH,
					0,
					HALF_FRAME_WIDTH,
					FRAME_CUT,
					TEXTURE_WIDTH,
					TEXTURE_HEIGHT
			);
			context.drawTexture(
					RenderLayer::getGuiTexturedOverlay,
					WINDOW_TEXTURE,
					this.width - FRAME_PADDING - HALF_FRAME_WIDTH + 1,
					FRAME_PADDING + TABS_HEIGHT + FRAME_CUT,
					HALF_FRAME_WIDTH,
					FRAME_CUT * 2 - FRAME_EXPAND,
					HALF_FRAME_WIDTH,
					HALF_FRAME_HEIGHT - TABS_HEIGHT - FRAME_CUT,
					TEXTURE_WIDTH,
					TEXTURE_HEIGHT
			);

			// top
			context.drawTexture(
					RenderLayer::getGuiTexturedOverlay,
					WINDOW_TEXTURE,
					FRAME_PADDING + HALF_FRAME_WIDTH,
					FRAME_PADDING + TABS_HEIGHT,
					HALF_FRAME_WIDTH - 1,
					0,
					this.width - FRAME_PADDING * 2 - FRAME_WIDTH + 1,
					FRAME_CUT,
					2,
					FRAME_CUT,
					TEXTURE_WIDTH,
					TEXTURE_HEIGHT
			);
			context.drawTexture(
					RenderLayer::getGuiTexturedOverlay,
					WINDOW_TEXTURE,
					FRAME_PADDING + HALF_FRAME_WIDTH,
					FRAME_PADDING + TABS_HEIGHT + FRAME_CUT,
					HALF_FRAME_WIDTH - 1,
					FRAME_CUT * 2 - FRAME_EXPAND,
					this.width - FRAME_PADDING * 2 - FRAME_WIDTH + 1,
					HALF_FRAME_HEIGHT - FRAME_CUT,
					2,
					HALF_FRAME_HEIGHT - FRAME_CUT,
					TEXTURE_WIDTH,
					TEXTURE_HEIGHT
			);
		} else {
			// top left
			context.drawTexture(
					RenderLayer::getGuiTexturedOverlay,
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
			context.drawTexture(
					RenderLayer::getGuiTexturedOverlay,
					WINDOW_TEXTURE,
					this.width - FRAME_PADDING - HALF_FRAME_WIDTH + 1,
					FRAME_PADDING + TABS_HEIGHT,
					HALF_FRAME_WIDTH,
					0,
					HALF_FRAME_WIDTH,
					HALF_FRAME_HEIGHT - TABS_HEIGHT,
					TEXTURE_WIDTH,
					TEXTURE_HEIGHT
			);

			// top
			context.drawTexture(
					RenderLayer::getGuiTexturedOverlay,
					WINDOW_TEXTURE,
					FRAME_PADDING + HALF_FRAME_WIDTH,
					FRAME_PADDING + TABS_HEIGHT,
					HALF_FRAME_WIDTH - 1,
					0,
					this.width - FRAME_PADDING * 2 - FRAME_WIDTH + 1,
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

		context.drawText(
				this.textRenderer,
				tmpText,
				tmpX,
				tmpY,
				0xff404040,
				false
		);

		optActiveCategoryData.ifPresent(activeCategoryData ->
				drawWindowWithCategory(context, mouseX, mouseY, tmpText, tmpX, tmpY, activeCategoryData)
		);
	}

	private void drawWindowWithCategory(DrawContext context, double mouseX, double mouseY, Text tmpText, int tmpX, int tmpY, ClientCategoryData activeCategoryData) {
		var mouse = getMousePos(mouseX, mouseY);
		var activeCategory = activeCategoryData.getConfig();

		var leftX = tmpX + this.textRenderer.getWidth(tmpText);

		tmpX = this.width - FRAME_PADDING - 7;

		var startX = tmpX;

		tmpText = Text.literal(activeCategoryData.getPointsLeft()
				+ (activeCategory.spentPointsLimit() == Integer.MAX_VALUE ? "" : "/" + activeCategoryData.getSpentPointsLeft())
		);

		tmpX -= this.textRenderer.getWidth(tmpText);
		tmpX -= 1;

		var pointsColor = activeCategory.colors().points();
		var pointsStrokeColor = pointsColor.stroke().argb();
		var pointsFillColor = pointsColor.fill().argb();
		context.drawText(this.textRenderer, tmpText, tmpX - 1, tmpY, pointsStrokeColor, false);
		context.drawText(this.textRenderer, tmpText, tmpX, tmpY - 1, pointsStrokeColor, false);
		context.drawText(this.textRenderer, tmpText, tmpX + 1, tmpY, pointsStrokeColor, false);
		context.drawText(this.textRenderer, tmpText, tmpX, tmpY + 1, pointsStrokeColor, false);
		context.drawText(this.textRenderer, tmpText, tmpX, tmpY, pointsFillColor, false);
		tmpX -= 1;

		tmpText = SkillsMod.createTranslatable("text", "points_left");
		tmpX -= this.textRenderer.getWidth(tmpText);
		context.drawText(
				this.textRenderer,
				tmpText,
				tmpX,
				tmpY,
				0xff404040,
				false
		);

		if (isInsideArea(mouse, tmpX, tmpY, startX, tmpY + this.textRenderer.fontHeight)) {
			var lines = new ArrayList<OrderedText>();
			lines.add(SkillsMod.createTranslatable(
					"tooltip",
					"earned_points",
					activeCategoryData.getEarnedPoints()
			).asOrderedText());
			lines.add(SkillsMod.createTranslatable(
					"tooltip",
					"spent_points",
					activeCategoryData.getSpentPoints()
							+ (activeCategory.spentPointsLimit() == Integer.MAX_VALUE ? "" : "/" + activeCategory.spentPointsLimit())
			).asOrderedText());
			setTooltip(lines);
		}

		var rightX = tmpX;

		if (activeCategoryData.hasExperience()) {
			if (small) {
				tmpX = this.width - FRAME_PADDING - 8 - 182;
				tmpY = TABS_HEIGHT + 25;
			} else {
				tmpX = (leftX + rightX - 182) / 2;
				tmpY = TABS_HEIGHT + 15;
			}

			context.drawGuiTexture(
					RenderLayer::getGuiTexturedOverlay,
					EXPERIENCE_BAR_BACKGROUND_TEXTURE,
					tmpX,
					tmpY,
					182,
					5
			);
			var width = Math.min(182, (int) (activeCategoryData.getExperienceProgress() * 183f));
			if (width > 0) {
				context.drawGuiTexture(
						RenderLayer::getGuiTexturedOverlay,
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
				var lines = new ArrayList<OrderedText>();
				lines.add(SkillsMod.createTranslatable(
						"tooltip",
						"current_level",
						activeCategoryData.getCurrentLevel()
				).asOrderedText());
				lines.add(SkillsMod.createTranslatable(
						"tooltip",
						"experience_progress",
						activeCategoryData.getCurrentExperience(),
						activeCategoryData.getRequiredExperience(),
						MathHelper.floor(activeCategoryData.getExperienceProgress() * 100f)
				).asOrderedText());
				lines.add(SkillsMod.createTranslatable(
						"tooltip",
						"to_next_level",
						activeCategoryData.getExperienceToNextLevel()
				).asOrderedText());
				setTooltip(lines);
			}
		}
	}

}
