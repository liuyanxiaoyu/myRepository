package com.kingdom.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class JsonUtils {

    // 缩进时填充单元格个数
    private static final int SPACE = 4;

	/**
	 * 将json字符串换行并填充缩进空格
	 * @param jsonStr
	 * @return
	 */
	public static String format(String jsonStr){
    	try (ByteArrayInputStream in = new ByteArrayInputStream(jsonStr.getBytes());
			 ByteArrayOutputStream out = new ByteArrayOutputStream()
		){
			char ch;
			int read;
			int space=0;
			while((read = in.read()) > 0){
				ch = (char)read;
				switch (ch){
					case '{': {
						space = outputAndRightMove(space, ch, out);
						break;
					}
					case '[': {
						out.write(ch);
						space += SPACE;
						break;
					}
					case '}': {
						space = outputAndLeftMove(space, ch, out);
						break;
					}
					case ']': {
						space = outputAndLeftMove(space, ch, out);
						break;
					}
					case ',': {
						out.write(ch);
						out.write('\n');
						out.write(getBlankingStringBytes(space));
						break;
					}
					default: {
						out.write(ch);
						break;
					}
				}
			}
			return out.toString();
		} catch (IOException e){
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 向右缩进
	 * @param space
	 * @param ch
	 * @param out
	 * @return
	 * @throws IOException
	 */
	private static int outputAndRightMove(int space, char ch, ByteArrayOutputStream out) throws IOException {
		out.write('\n');
		//向右缩进
		out.write(getBlankingStringBytes(space));
		out.write(ch);
		out.write('\n');
		space += SPACE;
		//再向右缩进
		out.write(getBlankingStringBytes(space));
		return space;
	}

	/**
	 * 向左缩进
	 * @param space
	 * @param ch
	 * @param out
	 * @return
	 * @throws IOException
	 */
	private static int outputAndLeftMove(int space, char ch, ByteArrayOutputStream out) throws IOException{
		out.write('\n');
		space -= SPACE;
		out.write(getBlankingStringBytes(space));
		out.write(ch);
		return space;
	}

	/**
	 * 填充空格
	 * @param space
	 * @return
	 */
	private static byte[] getBlankingStringBytes(int space){
		StringBuilder sb = new StringBuilder("");
		for (int i = 0; i < space; i++) {
			sb.append(" ");
		}
		return sb.toString().getBytes();
	}

}
