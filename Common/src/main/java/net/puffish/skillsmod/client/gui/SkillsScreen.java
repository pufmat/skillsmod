package net.puffish.skillsmod.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.client.SkillsClientMod;
import net.puffish.skillsmod.client.data.ClientFrameData;
import net.puffish.skillsmod.client.data.ClientIconData;
import net.puffish.skillsmod.client.data.ClientSkillCategoryData;
import net.puffish.skillsmod.client.data.ClientSkillData;
import net.puffish.skillsmod.client.network.packets.out.SkillClickOutPacket;
import net.puffish.skillsmod.skill.SkillState;
import net.puffish.skillsmod.utils.Bounds2i;
import net.puffish.skillsmod.utils.Vec2i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class SkillsScreen extends Screen {
	private static final Identifier TABS_TEXTURE = new Identifier("textures/gui/advancements/tabs.png");
	private static final Identifier WINDOW_TEXTURE = new Identifier("textures/gui/advancements/window.png");
	private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/advancements/widgets.png");

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

	private List<OrderedText> tooltip;

	public SkillsScreen(List<ClientSkillCategoryData> categories) {
		super(LiteralText.EMPTY);
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
		this.bounds.extend(new Vec2i(contentPaddingLeft - this.x, contentPaddingTop - this.y));
		this.bounds.extend(new Vec2i(this.width - this.x - contentPaddingRight, this.height - this.y - contentPaddingBottom));

		var contentWidth = this.width - contentPaddingLeft - contentPaddingRight;
		var contentHeight = this.height - contentPaddingTop - contentPaddingBottom;

		var halfWidth = MathHelper.ceilDiv(this.bounds.height() * contentWidth, contentHeight * 2);
		var halfHeight = MathHelper.ceilDiv(this.bounds.width() * contentHeight, contentWidth * 2);

		this.bounds.extend(new Vec2i(-halfWidth, -halfHeight));
		this.bounds.extend(new Vec2i(halfWidth, halfHeight));

		this.minScale = Math.max(
				((float) contentWidth) / ((float) this.bounds.width()),
				((float) contentHeight) / ((float) this.bounds.height())
		);
		this.maxScale = 1f;
		this.scale = 1f;
	}

	private Vec2i getMousePos(double mouseX, double mouseY) {
		return new Vec2i(
				(int) mouseX,
				(int) mouseY
		);
	}

	private Vec2i getTransformedMousePos(double mouseX, double mouseY) {
		return new Vec2i(
				(int) Math.round((mouseX - x) / scale),
				(int) Math.round((mouseY - y) / scale)
		);
	}

	private boolean isInsideTab(Vec2i mouse, int i) {
		return mouse.x >= FRAME_PADDING + i * 32 && mouse.y >= FRAME_PADDING && mouse.x < FRAME_PADDING + i * 32 + 28 && mouse.y < FRAME_PADDING + 32;
	}

	private boolean isInsideSkill(Vec2i transformedMouse, ClientSkillData skill) {
		return transformedMouse.x >= skill.getX() - 13 && transformedMouse.y >= skill.getY() - 13 && transformedMouse.x < skill.getX() + 13 && transformedMouse.y < skill.getY() + 13;
	}

	private boolean isInsideContent(Vec2i mouse) {
		return mouse.x >= contentPaddingLeft && mouse.y >= contentPaddingTop && mouse.x < width - contentPaddingRight && mouse.y < height - contentPaddingBottom;
	}

	private boolean isInsideExperience(Vec2i mouse, int x, int y) {
		return mouse.x >= x && mouse.y >= y && mouse.x < x + 182 && mouse.y < y + 5;
	}

	private boolean isInsideArea(Vec2i mouse, int x1, int y1, int x2, int y2) {
		return mouse.x >= x1 && mouse.y >= y1 && mouse.x < x2 && mouse.y < y2;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		var mouse = getMousePos(mouseX, mouseY);
		var transformedMouse = getTransformedMousePos(mouseX, mouseY);

		if (isInsideContent(mouse)) {
			for (var skill : getActiveCategory().getSkills().values()) {
				if (isInsideSkill(transformedMouse, skill)) {
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
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		tooltip = null;

		this.renderBackground(matrices);
		this.drawContent(matrices, mouseX, mouseY);
		this.drawWindow(matrices, mouseX, mouseY);
		this.drawTabs(matrices, mouseX, mouseY);

		if (tooltip != null) {
			renderOrderedTooltip(matrices, tooltip, mouseX, mouseY);
		}
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
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		float factor = (float) Math.pow(2, amount * 0.25);

		scale *= factor;

		if (scale < minScale) {
			scale = minScale;
			factor = minScale / scale;
		}
		if (scale > maxScale) {
			scale = maxScale;
			factor = maxScale / scale;
		}

		x -= Math.round((factor - 1f) * (mouseX - x));
		y -= Math.round((factor - 1f) * (mouseY - y));

		limitPosition();

		return super.mouseScrolled(mouseX, mouseY, amount);
	}

	private void limitPosition() {
		y = Math.min(y, Math.round(contentPaddingTop - bounds.min().y * scale));
		x = Math.min(x, Math.round(contentPaddingLeft - bounds.min().x * scale));
		x = Math.max(x, Math.round(width - contentPaddingRight - bounds.max().x * scale));
		y = Math.max(y, Math.round(height - contentPaddingBottom - bounds.max().y * scale));
	}

	private void drawIcon(MatrixStack matrices, int x, int y, ClientIconData icon) {
		if (client == null) {
			return;
		}

		if (icon instanceof ClientIconData.ItemIconData itemIcon) {
			DrawUtils.drawItem(
					matrices,
					x - 8,
					y - 8,
					itemIcon.getItem()
			);
		} else if (icon instanceof ClientIconData.EffectIconData effectIcon) {
			var sprite = client.getStatusEffectSpriteManager().getSprite(effectIcon.getEffect());
			RenderSystem.setShaderTexture(0, sprite.getAtlas().getId());
			DrawUtils.drawSingleSprite(
					matrices,
					x - 9,
					y - 9,
					18,
					18,
					sprite
			);
		} else if (icon instanceof ClientIconData.TextureIconData textureIcon) {
			RenderSystem.setShaderTexture(0, textureIcon.getTexture());
			DrawUtils.drawSingleTexture(
					matrices,
					x - 8,
					y - 8,
					16,
					16
			);
		}
	}

	private void drawFrame(MatrixStack matrices, ClientFrameData frame, int x, int y, SkillState state) {
		if (client == null) {
			return;
		}

		if (frame instanceof ClientFrameData.AdvancementFrameData advancementFrame) {
			var status = state == SkillState.UNLOCKED ? AdvancementObtainedStatus.OBTAINED : AdvancementObtainedStatus.UNOBTAINED;
			switch (state) {
				case LOCKED -> RenderSystem.setShaderColor(0.25f, 0.25f, 0.25f, 1f);
				case EXCLUDED -> RenderSystem.setShaderColor(0.85f, 0.1f, 0.1f, 1f);
				default -> RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			}

			RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
			drawTexture(
					matrices,
					x - 13,
					y - 13,
					advancementFrame.getFrame().getTextureV(),
					128 + status.getSpriteIndex() * 26,
					26,
					26
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
						RenderSystem.setShaderColor(0.85f, 0.1f, 0.1f, 1f);
						yield textureFrame.getAvailableTexture();
					}
				}
			};

			RenderSystem.setShaderTexture(0, texture);
			drawTexture(
					matrices,
					x - 13,
					y - 13,
					0,
					0,
					26,
					26,
					26,
					26
			);
		}
	}

	private void drawContent(MatrixStack matrices, double mouseX, double mouseY) {
		if (client == null) {
			return;
		}

		var mouse = getMousePos(mouseX, mouseY);
		var transformedMouse = getTransformedMousePos(mouseX, mouseY);

		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL11.GL_ALWAYS);
		RenderSystem.colorMask(false, false, false, false);

		matrices.push();

		matrices.translate(0f, 0f, 256f);
		DrawableHelper.fill(
				matrices,
				0,
				0,
				this.width,
				this.height,
				0xff000000
		);

		matrices.translate(0f, 0f, -512f);
		DrawableHelper.fill(
				matrices,
				contentPaddingLeft - 4,
				contentPaddingTop - 4,
				this.width - contentPaddingRight + 4,
				this.height - contentPaddingBottom + 4,
				0xff000000
		);

		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.depthFunc(GL11.GL_LEQUAL);

		matrices.translate(x, y, 128f);
		matrices.scale(scale, scale, 1f);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.setShaderTexture(0, getActiveCategory().getBackground());
		DrawUtils.drawRepeatedTexture(
				matrices,
				bounds.min().x,
				bounds.min().y,
				bounds.width(),
				bounds.height(),
				0,
				0,
				16,
				16
		);

		for (var connection : getActiveCategory().getNormalConnections()) {
			var skillA = getActiveCategory().getSkills().get(connection.getSkillAId());
			var skillB = getActiveCategory().getSkills().get(connection.getSkillBId());
			if (skillA != null && skillB != null) {
				drawConnection(
						matrices,
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
					.filter(skill -> isInsideSkill(transformedMouse, skill))
					.findFirst();

			optHoveredSkill.ifPresent(hoveredSkill -> {
				var definition = getActiveCategory().getDefinitions().get(hoveredSkill.getDefinitionId());
				if (definition == null) {
					return;
				}

				var lines = new ArrayList<OrderedText>();
				lines.add(definition.getTitle().asOrderedText());
				lines.addAll(textRenderer.wrapLines(Texts.setStyleIfAbsent(definition.getDescription().copy(), Style.EMPTY.withFormatting(Formatting.GRAY)), 170));
				if (client.options.advancedItemTooltips) {
					lines.add(new LiteralText(hoveredSkill.getId()).formatted(Formatting.DARK_GRAY).asOrderedText());
				}
				tooltip = lines;

				var connections = getActiveCategory().getExclusiveConnections().get(hoveredSkill.getId());
				if (connections != null) {
					for (var connection : connections) {
						var skillA = getActiveCategory().getSkills().get(connection.getSkillAId());
						var skillB = getActiveCategory().getSkills().get(connection.getSkillBId());
						if (skillA != null && skillB != null) {
							drawConnection(
									matrices,
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

			drawFrame(matrices, definition.getFrame(), skill.getX(), skill.getY(), skill.getState());

			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			drawIcon(matrices, skill.getX(), skill.getY(), definition.getIcon());
		}

		matrices.pop();
		matrices.push();

		RenderSystem.depthFunc(GL11.GL_ALWAYS);
		matrices.translate(0f, 0f, -512f);
		RenderSystem.colorMask(false, false, false, false);
		DrawableHelper.fill(
				matrices,
				0,
				0,
				this.width,
				this.height,
				0xff000000
		);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.depthFunc(GL11.GL_LEQUAL);

		matrices.pop();
	}

	private void drawConnection(
			MatrixStack matrices,
			float startX,
			float startY,
			float endX,
			float endY,
			boolean unidirectional,
			int rgb
	) {
		drawLine(matrices, startX, startY, endX, endY, 3, 0xff000000);
		if (unidirectional) {
			drawArrow(matrices, startX, startY, endX, endY, 8, 0xff000000);
		}
		drawLine(matrices, startX, startY, endX, endY, 1, rgb | 0xff000000);
		if (unidirectional) {
			drawArrow(matrices, startX, startY, endX, endY, 6, rgb | 0xff000000);
		}
	}

	private void drawLine(
			MatrixStack matrices,
			float startX,
			float startY,
			float endX,
			float endY,
			int thickness,
			int color
	) {
		float a = ((float) (color >> 24 & 0xff)) / 255f;
		float r = ((float) (color >> 16 & 0xff)) / 255f;
		float g = ((float) (color >> 8 & 0xff)) / 255f;
		float b = ((float) (color & 0xff)) / 255f;
		var matrix = matrices.peek().getPositionMatrix();
		var tmp = new Vec2f(endX, endY)
				.add(new Vec2f(-startX, -startY))
				.normalize();

		tmp = new Vec2f(tmp.y, -tmp.x).multiply(thickness / 2f);

		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(matrix, startX + tmp.x, startY + tmp.y, 0).color(r, g, b, a).next();
		bufferBuilder.vertex(matrix, startX - tmp.x, startY - tmp.y, 0).color(r, g, b, a).next();
		bufferBuilder.vertex(matrix, endX - tmp.x, endY - tmp.y, 0).color(r, g, b, a).next();
		bufferBuilder.vertex(matrix, endX + tmp.x, endY + tmp.y, 0).color(r, g, b, a).next();
		bufferBuilder.end();
		BufferRenderer.draw(bufferBuilder);
	}

	private void drawArrow(
			MatrixStack matrices,
			float startX,
			float startY,
			float endX,
			float endY,
			float thickness,
			int argb
	) {
		var matrix = matrices.peek().getPositionMatrix();
		var center = new Vec2f(endX, endY)
				.add(new Vec2f(startX, startY))
				.multiply(0.5f);
		var normal = new Vec2f(endX, endY)
				.add(new Vec2f(-startX, -startY))
				.normalize();
		var forward = new Vec2f(normal.x, normal.y)
				.multiply(thickness);
		var backward = new Vec2f(forward.x, forward.y)
				.multiply(-0.5f);
		var back = new Vec2f(center.x, center.y)
				.add(backward);
		var side = new Vec2f(backward.y, -backward.x)
				.multiply(MathHelper.sqrt(3f));

		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(matrix, center.x + forward.x, center.y + forward.y, 0).color(argb).next();
		bufferBuilder.vertex(matrix, back.x - side.x, back.y - side.y, 0).color(argb).next();
		bufferBuilder.vertex(matrix, back.x + side.x, back.y + side.y, 0).color(argb).next();
		bufferBuilder.end();
		BufferRenderer.draw(bufferBuilder);
	}

	private void drawTabs(MatrixStack matrices, double mouseX, double mouseY) {
		if (client == null) {
			return;
		}

		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.disableDepthTest();
		RenderSystem.setShaderTexture(0, TABS_TEXTURE);

		for (var i = 0; i < categories.size(); i++) {
			DrawableHelper.drawTexture(
					matrices,
					FRAME_PADDING + 32 * i,
					FRAME_PADDING,
					i > 0 ? 28 : 0,
					activeCategory == i ? 32 : 0,
					28,
					32,
					TEXTURE_WIDTH,
					TEXTURE_HEIGHT
			);
		}

		var mouse = getMousePos(mouseX, mouseY);

		for (var i = 0; i < categories.size(); i++) {
			var category = categories.get(i);

			drawIcon(matrices, FRAME_PADDING + 32 * i + 6 + 8, FRAME_PADDING + 9 + 8, category.getIcon());

			if (isInsideTab(mouse, i)) {
				tooltip = textRenderer.wrapLines(category.getTitle(), 170);
			}
		}
	}

	private void drawWindow(MatrixStack matrices, double mouseX, double mouseY) {
		if (client == null) {
			return;
		}

		var mouse = getMousePos(mouseX, mouseY);

		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.disableDepthTest();
		RenderSystem.setShaderTexture(0, WINDOW_TEXTURE);

		// bottom left
		DrawableHelper.drawTexture(
				matrices,
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
		DrawableHelper.drawTexture(
				matrices,
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
		DrawUtils.drawScaledTexture(
				matrices,
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
		DrawUtils.drawScaledTexture(
				matrices,
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
		DrawUtils.drawScaledTexture(
				matrices,
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
			DrawableHelper.drawTexture(
					matrices,
					FRAME_PADDING,
					FRAME_PADDING + TABS_HEIGHT,
					0,
					0,
					HALF_FRAME_WIDTH,
					FRAME_CUT,
					TEXTURE_WIDTH,
					TEXTURE_HEIGHT
			);
			DrawableHelper.drawTexture(
					matrices,
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
			DrawableHelper.drawTexture(
					matrices,
					this.width - FRAME_PADDING - HALF_FRAME_WIDTH + 1,
					FRAME_PADDING + TABS_HEIGHT,
					HALF_FRAME_WIDTH,
					0,
					HALF_FRAME_WIDTH,
					FRAME_CUT,
					TEXTURE_WIDTH,
					TEXTURE_HEIGHT
			);
			DrawableHelper.drawTexture(
					matrices,
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
			DrawUtils.drawScaledTexture(
					matrices,
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
			DrawUtils.drawScaledTexture(
					matrices,
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
			DrawableHelper.drawTexture(
					matrices,
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
			DrawableHelper.drawTexture(
					matrices,
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
			DrawUtils.drawScaledTexture(
					matrices,
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

		this.textRenderer.draw(
				matrices,
				tmpText,
				tmpX,
				tmpY,
				0xff404040
		);

		var leftX = tmpX + this.textRenderer.getWidth(tmpText);

		tmpX = this.width - FRAME_PADDING - 7;

		var startX = tmpX;

		var activeCategory = getActiveCategory();

		tmpText = new LiteralText(activeCategory.getPointsLeft()
				+ (activeCategory.getSpentPointsLimit() == Integer.MAX_VALUE ? "" : "/" + activeCategory.getSpentPointsLeft())
		);
		tmpX -= this.textRenderer.getWidth(tmpText);
		tmpX -= 1;
		DrawUtils.drawTextWithBorder(
				matrices,
				tmpText,
				tmpX,
				tmpY,
				0xff000000,
				0xff80ff20
		);
		tmpX -= 1;

		tmpText = SkillsMod.createTranslatable("text", "points_left");
		tmpX -= this.textRenderer.getWidth(tmpText);
		this.textRenderer.draw(
				matrices,
				tmpText,
				tmpX,
				tmpY,
				0xff404040
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

			tooltip = lines;
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

			RenderSystem.setShaderTexture(0, InGameHud.GUI_ICONS_TEXTURE);
			drawTexture(matrices, tmpX, tmpY, 0, 64, 182, 5);
			int width = Math.min(182, (int) (getActiveCategory().getExperienceProgress() * 183f));
			if (width > 0) {
				drawTexture(matrices, tmpX, tmpY, 0, 69, width, 5);
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

				tooltip = lines;
			}
		}
	}

}
