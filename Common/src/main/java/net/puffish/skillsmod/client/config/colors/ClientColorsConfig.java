package net.puffish.skillsmod.client.config.colors;

public record ClientColorsConfig(
		ClientConnectionsColorsConfig connections,
		ClientExchangeColorsConfig exchange,
		ClientFillStrokeColorsConfig points
) { }
