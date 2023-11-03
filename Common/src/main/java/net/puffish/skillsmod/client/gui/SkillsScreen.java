package net.puffish.skillsmod.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.client.SkillsClientMod;
import net.puffish.skillsmod.client.data.ClientFrameData;
import net.puffish.skillsmod.client.data.ClientIconData;
import net.puffish.skillsmod.client.data.ClientSkillCategoryData;
import net.puffish.skillsmod.client.data.ClientSkillData;
import net.puffish.skillsmod.client.data.ClientSkillDefinitionData;
import net.puffish.skillsmod.client.network.packets.out.SkillClickOutPacket;
import net.puffish.skillsmod.skill.SkillState;
import net.puffish.skillsmod.utils.Bounds2i;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class SkillsScreen extends Screen {
	private static final Identifier WINDOW_TEXTURE = new Identifier("textures/gui/advancements/window.png");
	private static final Identifier EXPERIENCE_BAR_BACKGROUND_TEXTURE = new Identifier("hud/experience_bar_background");
	private static final Identifier EXPERIENCE_BAR_PROGRESS_TEXTURE = new Identifier("hud/experience_bar_progress");
	private static final Identifier TAB_ABOVE_LEFT_SELECTED_TEXTURE = new Identifier("advancements/tab_above_left_selected");
	private static final Identifier TAB_ABOVE_MIDDLE_SELECTED_TEXTURE = new Identifier("advancements/tab_above_middle_selected");
	private static final Identifier TAB_ABOVE_LEFT_TEXTURE = new Identifier("advancements/tab_above_left");
	private static final Identifier TAB_ABOVE_MIDDLE_TEXTURE = new Identifier("advancements/tab_above_middle");

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

	private final List<ClientSkillCategoryData> categories;

	private int activeCategory = 0;

	private float minScale = 1f;
	private float maxScale = 1f;
	private float scale = 1;

	private int x = 0;
	private int y = 0;

	private boolean dragging;

	private Bounds2i bounds;

	private double dragStartX;
	private double dragStartY;

	private boolean small;

	private int contentPaddingTop;
	private int contentPaddingLeft;
	private int contentPaddingRight;
	private int contentPaddingBottom;

	public SkillsScreen(List<ClientSkillCategoryData> categories) {
		super(ScreenTexts.EMPTY);
		this.categories = categories;
	}

	private ClientSkillCategoryData getActiveCategory() {
		return categories.get(activeCategory);
	}

	private void setActiveCategory(int activeCategory) {
		this.activeCategory = activeCategory;
		resize();
	}

	@Override
	protected void init() {
		super.init();

		resize();
	}

	private void resize() {
		this.small = this.width < 450;

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

		this.x = this.width / 2;
		this.y = this.height / 2;

		this.bounds = getActiveCategory().getBounds();
		this.bounds.grow(CONTENT_GROW);
		this.bounds.extend(new Vector2i(contentPaddingLeft - this.x, contentPaddingTop - this.y));
		this.bounds.extend(new Vector2i(this.width - this.x - contentPaddingRight, this.height - this.y - contentPaddingBottom));

		var contentWidth = this.width - contentPaddingLeft - contentPaddingRight;
		var contentHeight = this.height - contentPaddingTop - contentPaddingBottom;

		var halfWidth = MathHelper.ceilDiv(this.bounds.height() * contentWidth, contentHeight * 2);
		var halfHeight = MathHelper.ceilDiv(this.bounds.width() * contentHeight, contentWidth * 2);

		this.bounds.extend(new Vector2i(-halfWidth, -halfHeight));
		this.bounds.extend(new Vector2i(halfWidth, halfHeight));

		this.minScale = Math.max(
				((float) contentWidth) / ((float) this.bounds.width()),
				((float) contentHeight) / ((float) this.bounds.height())
		);
		this.maxScale = 1f;
		this.scale = 1f;
	}

	private Vector2i getMousePos(double mouseX, double mouseY) {
		return new Vector2i(
				(int) mouseX,
				(int) mouseY
		);
	}

	private Vector2i getTransformedMousePos(double mouseX, double mouseY) {
		return new Vector2i(
				(int) Math.round((mouseX - x) / scale),
				(int) Math.round((mouseY - y) / scale)
		);
	}

	private boolean isInsideTab(Vector2i mouse, int i) {
		return mouse.x >= FRAME_PADDING + i * 32 && mouse.y >= FRAME_PADDING && mouse.x < FRAME_PADDING + i * 32 + 28 && mouse.y < FRAME_PADDING + 32;
	}

	private boolean isInsideSkill(Vector2i transformedMouse, ClientSkillData skill, ClientSkillDefinitionData definition) {
		var halfSize = Math.round(13f * definition.getSize());
		return transformedMouse.x >= skill.getX() - halfSize && transformedMouse.y >= skill.getY() - halfSize && transformedMouse.x < skill.getX() + halfSize && transformedMouse.y < skill.getY() + halfSize;
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

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		var mouse = getMousePos(mouseX, mouseY);
		var transformedMouse = getTransformedMousePos(mouseX, mouseY);

		if (isInsideContent(mouse)) {
			for (var skill : getActiveCategory().getSkills().values()) {
				var definition = getActiveCategory().getDefinitions().get(skill.getDefinitionId());
				if (definition == null) {
					continue;
				}

				if (isInsideSkill(transformedMouse, skill, definition)) {
					SkillsClientMod.getInstance()
							.getPacketSender()
							.send(SkillClickOutPacket.write(getActiveCategory().getId(), skill.getId()));
				}
			}

			if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
				dragStartX = mouseX - x;
				dragStartY = mouseY - y;
				dragging = true;
			}
		} else {
			dragging = false;
		}

		for (var i = 0; i < categories.size(); i++) {
			if (isInsideTab(mouse, i)) {
				setActiveCategory(i);
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
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
		this.renderBackground(context, mouseX, mouseY, delta);
		this.drawContent(context, mouseX, mouseY);
		this.drawWindow(context, mouseX, mouseY);
		this.drawTabs(context, mouseX, mouseY);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (dragging) {
			if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
				x = (int) Math.round(mouseX - dragStartX);
				y = (int) Math.round(mouseY - dragStartY);

				limitPosition();

				return true;
			}
		}

		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		float factor = (float) Math.pow(2, verticalAmount * 0.25);

		scale *= factor;

		if (scale < minScale) {
			scale = minScale;
			factor = minScale / scale;
		}
		if (scale > maxScale) {
			scale = maxScale;
			factor = maxScale / scale;
		}

		x -= (int) Math.round((factor - 1f) * (mouseX - x));
		y -= (int) Math.round((factor - 1f) * (mouseY - y));

		limitPosition();

		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private void limitPosition() {
		y = Math.min(y, (int) Math.floor(contentPaddingTop - bounds.min().y() * scale));
		x = Math.min(x, (int) Math.floor(contentPaddingLeft - bounds.min().x() * scale));
		x = Math.max(x, (int) Math.ceil(width - contentPaddingRight - bounds.max().x() * scale));
		y = Math.max(y, (int) Math.ceil(height - contentPaddingBottom - bounds.max().y() * scale));
	}

	private void drawIcon(DrawContext context, ClientIconData icon, float sizeScale, int x, int y) {
		if (client == null) {
			return;
		}

		if (icon instanceof ClientIconData.ItemIconData itemIcon) {
			var matrices = context.getMatrices();
			matrices.push();
			matrices.translate(x * (1f - sizeScale), y * (1f - sizeScale), 0);
			matrices.scale(sizeScale, sizeScale, 1);
			context.drawItem(
					itemIcon.getItem(),
					x - 8,
					y - 8
			);
			matrices.pop();
		} else if (icon instanceof ClientIconData.EffectIconData effectIcon) {
			var sprite = client.getStatusEffectSpriteManager().getSprite(effectIcon.getEffect());
			int halfSize = Math.round(9f * sizeScale);
			var size = halfSize * 2;
			context.drawSprite(
					x - halfSize,
					y - halfSize,
					0,
					size,
					size,
					sprite
			);
		} else if (icon instanceof ClientIconData.TextureIconData textureIcon) {
			int halfSize = Math.round(8f * sizeScale);
			var size = halfSize * 2;
			context.drawTexture(
					textureIcon.getTexture(),
					x - halfSize,
					y - halfSize,
					0,
					0,
					size,
					size,
					size,
					size
			);
		}
	}

	private void drawFrame(DrawContext context, ClientFrameData frame, float sizeScale, int x, int y, SkillState state) {
		if (client == null) {
			return;
		}

		var halfSize = Math.round(13f * sizeScale);
		var size = halfSize * 2;

		if (frame instanceof ClientFrameData.AdvancementFrameData advancementFrame) {
			var status = state == SkillState.UNLOCKED ? AdvancementObtainedStatus.OBTAINED : AdvancementObtainedStatus.UNOBTAINED;
			switch (state) {
				case LOCKED, EXCLUDED -> RenderSystem.setShaderColor(0.25f, 0.25f, 0.25f, 1f);
				default -> RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			}

			context.drawGuiTexture(
					status.getFrameTexture(advancementFrame.getFrame()),
					x - halfSize,
					y - halfSize,
					size,
					size
			);
		} else if (frame instanceof ClientFrameData.TextureFrameData textureFrame) {
			var texture = switch (state) {
				case AVAILABLE -> {
					RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
					yield textureFrame.getAvailableTexture();
				}
				case UNLOCKED -> {
					RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
					yield textureFrame.getUnlockedTexture();
				}
				case LOCKED -> {
					if (textureFrame.getLockedTexture() != null) {
						RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
						yield textureFrame.getLockedTexture();
					} else {
						RenderSystem.setShaderColor(0.25f, 0.25f, 0.25f, 1f);
						yield textureFrame.getAvailableTexture();
					}
				}
				case EXCLUDED -> {
					if (textureFrame.getExcludedTexture() != null) {
						RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
						yield textureFrame.getExcludedTexture();
					} else {
						RenderSystem.setShaderColor(0.25f, 0.25f, 0.25f, 1f);
						yield textureFrame.getAvailableTexture();
					}
				}
			};

			context.drawTexture(
					texture,
					x - halfSize,
					y - halfSize,
					0,
					0,
					size,
					size,
					size,
					size
			);
		}
	}

	private void drawContent(DrawContext context, double mouseX, double mouseY) {
		if (client == null) {
			return;
		}

		var mouse = getMousePos(mouseX, mouseY);
		var transformedMouse = getTransformedMousePos(mouseX, mouseY);

		context.enableScissor(
				contentPaddingLeft - 4,
				contentPaddingTop - 4,
				this.width - contentPaddingRight + 4,
				this.height - contentPaddingBottom + 4
		);

		context.getMatrices().push();

		context.getMatrices().translate(x, y, 0f);
		context.getMatrices().scale(scale, scale, 1f);

		context.drawTexture(
				getActiveCategory().getBackground(),
				bounds.min().x(),
				bounds.min().y(),
				0,
				0,
				bounds.width(),
				bounds.height(),
				16,
				16
		);

		for (var connection : getActiveCategory().getNormalConnections()) {
			var skillA = getActiveCategory().getSkills().get(connection.getSkillAId());
			var skillB = getActiveCategory().getSkills().get(connection.getSkillBId());
			if (skillA != null && skillB != null) {
				drawConnection(
						context,
						skillA.getX(),
						skillA.getY(),
						skillB.getX(),
						skillB.getY(),
						!connection.isBidirectional(),
						0xffffff
				);
			}
		}

		if (isInsideContent(mouse)) {
			var optHoveredSkill = getActiveCategory()
					.getSkills()
					.values()
					.stream()
					.filter(skill -> {
						var definition = getActiveCategory().getDefinitions().get(skill.getDefinitionId());
						if (definition == null) {
							return false;
						}

						return isInsideSkill(transformedMouse, skill, definition);
					})
					.findFirst();

			optHoveredSkill.ifPresent(hoveredSkill -> {
				var definition = getActiveCategory().getDefinitions().get(hoveredSkill.getDefinitionId());
				if (definition == null) {
					return;
				}

				var lines = new ArrayList<OrderedText>();
				lines.add(definition.getTitle().asOrderedText());
				lines.addAll(Tooltip.wrapLines(client, Texts.setStyleIfAbsent(definition.getDescription().copy(), Style.EMPTY.withFormatting(Formatting.GRAY))));
				if (Screen.hasShiftDown()) {
					lines.addAll(Tooltip.wrapLines(client, Texts.setStyleIfAbsent(definition.getExtraDescription().copy(), Style.EMPTY.withFormatting(Formatting.GRAY))));
				}
				if (client.options.advancedItemTooltips) {
					lines.add(Text.literal(hoveredSkill.getId()).formatted(Formatting.DARK_GRAY).asOrderedText());
				}
				setTooltip(lines);

				var connections = getActiveCategory().getExclusiveConnections().get(hoveredSkill.getId());
				if (connections != null) {
					for (var connection : connections) {
						var skillA = getActiveCategory().getSkills().get(connection.getSkillAId());
						var skillB = getActiveCategory().getSkills().get(connection.getSkillBId());
						if (skillA != null && skillB != null) {
							drawConnection(
									context,
									skillA.getX(),
									skillA.getY(),
									skillB.getX(),
									skillB.getY(),
									!connection.isBidirectional(),
									0xff0000
							);
						}
					}
				}
			});
		}

		for (var skill : getActiveCategory().getSkills().values()) {
			var definition = getActiveCategory().getDefinitions().get(skill.getDefinitionId());
			if (definition == null) {
				continue;
			}

			drawFrame(context, definition.getFrame(), definition.getSize(), skill.getX(), skill.getY(), skill.getState());

			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			drawIcon(context, definition.getIcon(), definition.getSize(), skill.getX(), skill.getY());
		}

		context.getMatrices().pop();
		context.disableScissor();
	}

	private void drawConnection(
			DrawContext context,
			float startX,
			float startY,
			float endX,
			float endY,
			boolean unidirectional,
			int rgb
	) {
		drawLine(context, startX, startY, endX, endY, 3, 0xff000000);
		if (unidirectional) {
			drawArrow(context, startX, startY, endX, endY, 8, 0xff000000);
		}
		drawLine(context, startX, startY, endX, endY, 1, rgb | 0xff000000);
		if (unidirectional) {
			drawArrow(context, startX, startY, endX, endY, 6, rgb | 0xff000000);
		}
	}

	private void drawLine(
			DrawContext context,
			float startX,
			float startY,
			float endX,
			float endY,
			float thickness,
			int argb
	) {
		var matrix = context.getMatrices().peek().getPositionMatrix();
		var side = new Vector2f(endX, endY)
				.sub(startX, startY)
				.normalize()
				.perpendicular()
				.mul(thickness / 2f);

		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(matrix, startX + side.x, startY + side.y, 0).color(argb).next();
		bufferBuilder.vertex(matrix, startX - side.x, startY - side.y, 0).color(argb).next();
		bufferBuilder.vertex(matrix, endX - side.x, endY - side.y, 0).color(argb).next();
		bufferBuilder.vertex(matrix, endX + side.x, endY + side.y, 0).color(argb).next();
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
	}

	private void drawArrow(
			DrawContext context,
			float startX,
			float startY,
			float endX,
			float endY,
			float thickness,
			int argb
	) {
		var matrix = context.getMatrices().peek().getPositionMatrix();
		var center = new Vector2f(endX, endY)
				.add(startX, startY)
				.div(2f);
		var normal = new Vector2f(endX, endY)
				.sub(startX, startY)
				.normalize();
		var forward = new Vector2f(normal)
				.mul(thickness);
		var backward = new Vector2f(forward)
				.div(-2f);
		var back = new Vector2f(center)
				.add(backward);
		var side = new Vector2f(backward)
				.perpendicular()
				.mul(MathHelper.sqrt(3f));

		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(matrix, center.x + forward.x, center.y + forward.y, 0).color(argb).next();
		bufferBuilder.vertex(matrix, back.x - side.x, back.y - side.y, 0).color(argb).next();
		bufferBuilder.vertex(matrix, back.x + side.x, back.y + side.y, 0).color(argb).next();
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
	}

	private void drawTabs(DrawContext context, double mouseX, double mouseY) {
		if (client == null) {
			return;
		}

		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.disableDepthTest();

		for (var i = 0; i < categories.size(); i++) {
			context.drawGuiTexture(
					activeCategory == i
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
			);
		}

		var mouse = getMousePos(mouseX, mouseY);

		for (var i = 0; i < categories.size(); i++) {
			var category = categories.get(i);

			drawIcon(
					context,
					category.getIcon(),
					1f,
					FRAME_PADDING + 32 * i + 6 + 8,
					FRAME_PADDING + 9 + 8
			);

			if (isInsideTab(mouse, i)) {
				var lines = new ArrayList<OrderedText>();
				lines.add(category.getTitle().asOrderedText());
				if (client.options.advancedItemTooltips) {
					lines.add(Text.literal(category.getId().toString()).formatted(Formatting.DARK_GRAY).asOrderedText());
				}
				setTooltip(lines);
			}
		}
	}

	private void drawWindow(DrawContext context, double mouseX, double mouseY) {
		if (client == null) {
			return;
		}

		var mouse = getMousePos(mouseX, mouseY);

		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.disableDepthTest();

		// bottom left
		context.drawTexture(
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
				WINDOW_TEXTURE,
				FRAME_PADDING,
				FRAME_PADDING + HALF_FRAME_HEIGHT,
				HALF_FRAME_WIDTH,
				this.height - FRAME_PADDING * 2 - FRAME_HEIGHT + 1,
				0,
				HALF_FRAME_HEIGHT - 1,
				HALF_FRAME_WIDTH,
				2,
				TEXTURE_WIDTH,
				TEXTURE_HEIGHT
		);

		// bottom
		context.drawTexture(
				WINDOW_TEXTURE,
				FRAME_PADDING + HALF_FRAME_WIDTH,
				this.height - FRAME_PADDING - HALF_FRAME_HEIGHT + 1,
				this.width - FRAME_PADDING * 2 - FRAME_WIDTH + 1,
				HALF_FRAME_HEIGHT,
				HALF_FRAME_WIDTH - 1,
				HALF_FRAME_HEIGHT,
				2,
				HALF_FRAME_HEIGHT,
				TEXTURE_WIDTH,
				TEXTURE_HEIGHT
		);

		// right
		context.drawTexture(
				WINDOW_TEXTURE,
				this.width - FRAME_PADDING - HALF_FRAME_WIDTH + 1,
				FRAME_PADDING + HALF_FRAME_HEIGHT,
				HALF_FRAME_WIDTH,
				this.height - FRAME_PADDING * 2 - FRAME_HEIGHT + 1,
				HALF_FRAME_WIDTH,
				HALF_FRAME_HEIGHT - 1,
				HALF_FRAME_WIDTH,
				2,
				TEXTURE_WIDTH,
				TEXTURE_HEIGHT
		);

		if (small) {
			// top left
			context.drawTexture(
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
					WINDOW_TEXTURE,
					FRAME_PADDING + HALF_FRAME_WIDTH,
					FRAME_PADDING + TABS_HEIGHT,
					this.width - FRAME_PADDING * 2 - FRAME_WIDTH + 1,
					FRAME_CUT,
					HALF_FRAME_WIDTH - 1,
					0,
					2,
					FRAME_CUT,
					TEXTURE_WIDTH,
					TEXTURE_HEIGHT
			);
			context.drawTexture(
					WINDOW_TEXTURE,
					FRAME_PADDING + HALF_FRAME_WIDTH,
					FRAME_PADDING + TABS_HEIGHT + FRAME_CUT,
					this.width - FRAME_PADDING * 2 - FRAME_WIDTH + 1,
					HALF_FRAME_HEIGHT - FRAME_CUT,
					HALF_FRAME_WIDTH - 1,
					FRAME_CUT * 2 - FRAME_EXPAND,
					2,
					HALF_FRAME_HEIGHT - FRAME_CUT,
					TEXTURE_WIDTH,
					TEXTURE_HEIGHT
			);
		} else {
			// top left
			context.drawTexture(
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
					WINDOW_TEXTURE,
					FRAME_PADDING + HALF_FRAME_WIDTH,
					FRAME_PADDING + TABS_HEIGHT,
					this.width - FRAME_PADDING * 2 - FRAME_WIDTH + 1,
					HALF_FRAME_HEIGHT,
					HALF_FRAME_WIDTH - 1,
					0,
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

		var leftX = tmpX + this.textRenderer.getWidth(tmpText);

		tmpX = this.width - FRAME_PADDING - 7;

		var startX = tmpX;

		var activeCategory = getActiveCategory();

		tmpText = Text.literal(activeCategory.getPointsLeft()
				+ (activeCategory.getSpentPointsLimit() == Integer.MAX_VALUE ? "" : "/" + activeCategory.getSpentPointsLeft())
		);

		tmpX -= this.textRenderer.getWidth(tmpText);
		tmpX -= 1;
		var textRenderer = MinecraftClient.getInstance().textRenderer;
		context.drawText(textRenderer, tmpText, tmpX - 1, tmpY, 0xff000000, false);
		context.drawText(textRenderer, tmpText, tmpX, tmpY - 1, 0xff000000, false);
		context.drawText(textRenderer, tmpText, tmpX + 1, tmpY, 0xff000000, false);
		context.drawText(textRenderer, tmpText, tmpX, tmpY + 1, 0xff000000, false);
		context.drawText(textRenderer, tmpText, tmpX, tmpY, 0xff80ff20, false);
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
					activeCategory.getEarnedPoints()
			).asOrderedText());
			lines.add(SkillsMod.createTranslatable(
					"tooltip",
					"spent_points",
					activeCategory.getSpentPoints()
							+ (activeCategory.getSpentPointsLimit() == Integer.MAX_VALUE ? "" : "/" + activeCategory.getSpentPointsLimit())
			).asOrderedText());
			setTooltip(lines);
		}

		var rightX = tmpX;

		if (activeCategory.getCurrentLevel() >= 0) {
			if (small) {
				tmpX = this.width - FRAME_PADDING - 8 - 182;
				tmpY = TABS_HEIGHT + 25;
			} else {
				tmpX = (leftX + rightX - 182) / 2;
				tmpY = TABS_HEIGHT + 15;
			}

			context.drawGuiTexture(EXPERIENCE_BAR_BACKGROUND_TEXTURE, tmpX, tmpY, 182, 5);
			int width = Math.min(182, (int) (getActiveCategory().getExperienceProgress() * 183f));
			if (width > 0) {
				context.drawGuiTexture(EXPERIENCE_BAR_PROGRESS_TEXTURE, 182, 5, 0, 0, tmpX, tmpY, width, 5);
			}


			if (isInsideExperience(mouse, tmpX, tmpY)) {
				var lines = new ArrayList<OrderedText>();
				lines.add(SkillsMod.createTranslatable(
						"tooltip",
						"current_level",
						activeCategory.getCurrentLevel()
				).asOrderedText());
				lines.add(SkillsMod.createTranslatable(
						"tooltip",
						"experience_progress",
						activeCategory.getCurrentExperience(),
						activeCategory.getRequiredExperience(),
						MathHelper.floor(activeCategory.getExperienceProgress() * 100f)
				).asOrderedText());
				lines.add(SkillsMod.createTranslatable(
						"tooltip",
						"to_next_level",
						activeCategory.getExperienceToNextLevel()
				).asOrderedText());
				setTooltip(lines);
			}
		}
	}

}
