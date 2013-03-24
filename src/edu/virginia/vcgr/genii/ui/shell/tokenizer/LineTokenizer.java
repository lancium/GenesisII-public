package edu.virginia.vcgr.genii.ui.shell.tokenizer;

import java.util.LinkedList;

public class LineTokenizer
{
	static public Token[] tokenize(String str)
	{
		boolean insideQuotes = false;
		Boolean spaceMode = null;
		StringBuilder builder = new StringBuilder();
		LinkedList<Token> tokens = new LinkedList<Token>();
		boolean escaping = false;

		for (int lcv = 0; lcv < str.length(); lcv++) {
			char c = str.charAt(lcv);

			if (escaping) {
				builder.append(c);
				escaping = false;
			} else {
				boolean isSpace = Character.isSpaceChar(c);
				if (insideQuotes)
					isSpace = false;

				if (spaceMode == null) {
					spaceMode = new Boolean(isSpace);
					builder.append(c);
				} else {
					if (spaceMode.booleanValue() != isSpace) {
						tokens.add(spaceMode.booleanValue() ? Token.createSpaceToken(builder.toString()) : Token
							.createWordToken(builder.toString()));
						builder.setLength(0);
						spaceMode = new Boolean(isSpace);
					}

					builder.append(c);
					if (c == '\\')
						escaping = true;
					else if (c == '"')
						insideQuotes = !insideQuotes;
				}
			}
		}

		if (spaceMode != null && builder.length() > 0) {
			tokens.add(spaceMode.booleanValue() ? Token.createSpaceToken(builder.toString()) : Token.createWordToken(builder
				.toString()));
		}

		if (tokens.size() == 0 || tokens.peekLast().isSpaceToken())
			tokens.add(Token.createWordToken(""));

		return tokens.toArray(new Token[tokens.size()]);
	}

	static private void print(Token[] tokens)
	{
		System.out.print("{");

		for (int lcv = 0; lcv < tokens.length; lcv++) {
			if (lcv != 0)
				System.out.print(", ");
			System.out.format("\"%s\"", tokens[lcv]);
		}

		System.out.println("}");
	}

	static public void main(String[] args)
	{
		print(tokenize("Mark Morgan was here."));
		print(tokenize("Mark \"Morgan was\" here."));
		print(tokenize("Mark \\\"Morgan was\" here."));
		print(tokenize("Mark Morgan\\ was here."));
	}
}