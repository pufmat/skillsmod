package net.puffish.skillsmod.expression;

import java.util.Optional;

public class Lexer {
	private final String content;
	private int cursor;

	private Lexer(String content, int cursor) {
		this.content = content;
		this.cursor = cursor;
	}

	public static Lexer create(String content) {
		var lexer = new Lexer(content, 0);
		lexer.skipWhitespace();
		return lexer;
	}

	public static Lexer copy(Lexer lexer) {
		return new Lexer(lexer.content, lexer.cursor);
	}

	private void skipWhitespace() {
		while (cursor < content.length() && Character.isWhitespace(content.charAt(cursor))) {
			cursor++;
		}
	}

	public boolean consume(String token) {
		if (content.startsWith(token, cursor)) {
			cursor += token.length();
			skipWhitespace();
			return true;
		}
		return false;
	}

	public Optional<String> consumeOther() {
		var end = cursor;
		while (end < content.length() && isOther(content.charAt(end))) {
			end++;
		}
		if (cursor == end) {
			return Optional.empty();
		}
		var token = content.substring(cursor, end);
		cursor = end;
		skipWhitespace();
		return Optional.of(token);
	}

	private boolean isOther(char ch) {
		return ch >= '0' && ch <= '9' || ch == '.' || ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch == '_';
	}

	public boolean isEnd() {
		return cursor == content.length();
	}
}
