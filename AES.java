//package AES;
import java.io.*;
import java.util.*;

/**
 * So this is my program, i first read the s-box from box.txt, you can simply put it on the same
 * directory as your java file. I will submit my box.txt as well but you can use your own.
 * 
 * Then i will read plaintext/encry from input.txt, and initial key from key.txt, just put those 2 files
 * along with box.txt in the same directory as AES.java before compile and run.
 * 
 * then use: javac AES.java, then run my program i will print the result to the standard out.
 * however to run my program, i take a mode parameter, 1 stands for ECB encry, 2 stands for CBC
 * encry, and 3 stands for decry, so run java AES 1 or java AES 2 or java AES 3
 * 
 * a trick, you can run this: java -ea AES 1 < input.txt | diff - output.txt
 * 
 * when you put your solution in output.txt, the above command can compare my result with yours.
 * 
 *
 */
public class AES {
	
	static class Byte
	{
		String hex;
		int dec;
		
		public Byte(String hex)
		{
			this.hex = hex; dec = Integer.parseInt(hex, 16);
		}
		
		public Byte(int dec)
		{
			this.dec = dec; hex = String.format("%02x", dec);
			if(hex.length() < 2)
			{
				
			}
			if(hex.length() > 2)
			{
				System.out.println("error");
			}
		}
	}
	
	static HashMap<Integer, Integer> map1 = new HashMap<Integer, Integer>();
	static HashMap<Integer, Integer> map2 = new HashMap<Integer, Integer>();
	static int poly = 1;
	static int mask = 283;
	static int round;
	static int mode; //1 for ECB mode, 2 for CBC mode, 3 for decry.
	public static void main(String args[]) throws IOException
	{
		mode = Integer.parseInt(args[0]);

		File box = new File("box.txt");
		Scanner in = new Scanner(box);
		
		while(in.hasNext())
		{
			int first = Integer.parseInt(in.next(), 16);
			int second = Integer.parseInt(in.next(), 16);
			map1.put(first, second); map2.put(second, first);
		}
		
		File key = new File("key.txt");
		in = new Scanner(key);
		String k = in.next();		  //original key.
		String expand = expansion(k); //this is the expansion version of key.
		mask = 283;
		File input = new File("input.txt");
		in = new Scanner(input);
		String text = in.next(); 		//this is the plain text.
		
		if(mode == 1 || mode == 2)
		{
			int start = 0;
			String encry = new String();
			int prev = 0;
			while(start < text.length())
			{
				String token = text.substring(start, start + 32);
				if(mode == 2)
				{
					while(prev < encry.length())
					{
						String previous = encry.substring(prev, prev + 32);
						token = roundKey(token, previous);
						prev += 32;
					}				
				}
				
				String next = roundKey(token, k);
				int keyIndex = 32;
				for(int i = 1; i <= round; i++)
				{				
					Byte[][] mat = subByte(next);
					mat = rowShift(mat);
										
					if(i != round)
					{
						mat = mixCol(mat);
					}
					
					next = new String();
					for(int j = 0; j < 4; j++)
					{
						for(int x = 0; x < 4; x++)
						{
							next += mat[x][j].hex;
						}
					}
					
					String kk = expand.substring(keyIndex, keyIndex + 32);
					next = roundKey(next, kk);
					keyIndex += 32;
				}
				encry += next;
				start += 32;
			}
			System.out.println(encry);
		}
		else
		{
			int start = 0;
			String decry = new String();
			while(start < text.length())
			{
				String next = text.substring(start, start + 32);
				int keyIndex = expand.length();
				for(int i = 1; i <= round; i++)
				{
					String kk = expand.substring(keyIndex-32, keyIndex);
					next = roundKey(next, kk);
					keyIndex -= 32;
					
					Byte[][] mat = new Byte[4][4];
					int ini = 0;
					for(int j = 0; j < 4; j++)
					{
						for(int x = 0; x < 4; x++)
						{
							String sb = next.substring(ini, ini + 2);
							mat[x][j] = new Byte(sb);
							ini += 2;
						}
					}
					
					if(i != 1)
					{
						for(int j = 0; j < 4; j++)
						{
							Byte b1 = mat[0][j]; Byte b2 = mat[1][j]; Byte b3 = mat[2][j]; Byte b4 = mat[3][j];
							int d1 = (2*b1.dec ^ (2*b2.dec ^ b2.dec)  ^ b3.dec ^ b4.dec);
							d1 = d1 > 255 ? d1 ^ mask : d1; mat[0][j] = new Byte(d1);
							int d2 = (1*b1.dec ^ 2*b2.dec  ^ (2*b3.dec ^ b3.dec) ^ b4.dec);
							d2 = d2 > 255 ? d2 ^ mask : d2; mat[1][j] = new Byte(d2);
							int d3 = (1*b1.dec ^ 1*b2.dec  ^ 2*b3.dec ^ (2*b4.dec ^ b4.dec));
							d3 = d3 > 255 ? d3 ^ mask : d3; mat[2][j] = new Byte(d3);
							int d4 = ((2*b1.dec ^ b1.dec) ^ 1*b2.dec  ^ b3.dec ^ 2*b4.dec);
							d4 = d4 > 255 ? d4 ^ mask : d4; mat[3][j] = new Byte(d4);
						}
					}
					
					Byte temp = mat[1][3]; mat[1][3] = mat[1][2]; mat[1][2] = mat[1][1]; mat[1][1]
							= mat[1][0]; mat[1][0] = temp;
					temp = mat[2][3]; Byte temp1 = mat[2][2]; mat[2][3] = mat[2][1]; mat[2][2]
							= mat[2][0]; mat[2][1] = temp; mat[2][0] = temp1;
					temp = mat[3][0]; mat[3][0] = mat[3][1]; mat[3][1] = mat[3][2]; mat[3][2]
							= mat[3][3]; mat[3][3] = temp;
					
							int ini1 = 0;
							for(int j = 0; j < 4; j++)
							{
								for(int x = 0; x < 4; x++)
								{
									String sb = next.substring(ini1, ini1 + 2);
									mat[x][j] = new Byte(map2.get(new Byte(sb).dec));
									ini1 += 2;
								}
							}								
					
					next = new String();
					for(int j = 0; j < 4; j++)
					{
						for(int x = 0; x < 4; x++)
						{
							next += mat[x][j].hex;
						}
					}	
				}
				next = roundKey(next, k);
				decry += next;
				start += 32;
			}		
			System.out.print(decry);
		}
	}
	
	/**
	 * subByte function.
	 * @param next
	 * @return
	 */
	private static Byte[][] subByte(String next)
	{
		Byte[][] mat = new Byte[4][4];
		int ini = 0;
		for(int j = 0; j < 4; j++)
		{
			for(int x = 0; x < 4; x++)
			{
				String sb = next.substring(ini, ini + 2);
				mat[x][j] = new Byte(map1.get(new Byte(sb).dec));
				ini += 2;
			}
		}	
		return mat;
	}
	
	/**
	 * row shift function.
	 * @param mat
	 * @return
	 */
	private static Byte[][] rowShift(Byte[][] mat)
	{
		Byte temp = mat[1][0]; mat[1][0] = mat[1][1]; mat[1][1] = mat[1][2]; mat[1][2]
				= mat[1][3]; mat[1][3] = temp;
		temp = mat[2][0]; Byte temp1 = mat[2][1]; mat[2][0] = mat[2][2]; mat[2][1]
				= mat[2][3]; mat[2][2] = temp; mat[2][3] = temp1;
		temp = mat[3][3]; mat[3][3] = mat[3][2]; mat[3][2] = mat[3][1]; mat[3][1]
				= mat[3][0]; mat[3][0] = temp;
		return mat;
	}
	
	/**
	 * mixCol
	 * @param mat
	 * @return
	 */
	private static Byte[][] mixCol(Byte[][] mat)
	{
		for(int j = 0; j < 4; j++)
		{
			Byte b1 = mat[0][j]; Byte b2 = mat[1][j]; Byte b3 = mat[2][j]; Byte b4 = mat[3][j];
			int d1 = (2*b1.dec ^ (2*b2.dec ^ b2.dec)  ^ b3.dec ^ b4.dec);
			d1 = d1 > 255 ? d1 ^ mask : d1; mat[0][j] = new Byte(d1);
			int d2 = (1*b1.dec ^ 2*b2.dec  ^ (2*b3.dec ^ b3.dec) ^ b4.dec);
			d2 = d2 > 255 ? d2 ^ mask : d2; mat[1][j] = new Byte(d2);
			int d3 = (1*b1.dec ^ 1*b2.dec  ^ 2*b3.dec ^ (2*b4.dec ^ b4.dec));
			d3 = d3 > 255 ? d3 ^ mask : d3; mat[2][j] = new Byte(d3);
			int d4 = ((2*b1.dec ^ b1.dec) ^ 1*b2.dec  ^ b3.dec ^ 2*b4.dec);
			d4 = d4 > 255 ? d4 ^ mask : d4; mat[3][j] = new Byte(d4);
		}
		return mat;
	}
	
	/**
	 * roundKey function.
	 * @param input
	 * @param key
	 * @return
	 */
	private static String roundKey(String input, String key)
	{
		String result = new String();
		int start = 0;
		while(start < 32)
		{
			Byte b1 = new Byte(input.substring(start, start + 2));
			Byte b2 = new Byte(key.substring(start, start + 2));
			result += String.format("%02x", b1.dec ^ b2.dec);
			start += 2;
		}	
		return result;
	}
	
	private static Byte[] core(Byte[] ori)
	{
		Byte temp = ori[0]; ori[0] = ori[1]; ori[1] = ori[2]; ori[2] = ori[3]; ori[3] = temp;
		for(int i = 0; i < 4; i++)
		{
			ori[i] = new Byte(map1.get(ori[i].dec));
		}
		if(poly > 128)
		{
			int evil = poly ^ mask;
			ori[0] = new Byte(ori[0].dec ^ evil);
			mask = mask << 1;
		}
		else
		{
			ori[0] = new Byte(ori[0].dec ^ poly);
		}
		
		poly = poly << 1;
		return ori;
	}
	
	/**
	 * keyExpansion function.
	 * @param key
	 * @return
	 */
	private static String expansion(String key)
	{
		String exp = key; int size = key.length();
		int totalSize, inner, last;
		if(size == 32)
		{
			totalSize = 176; inner = 4; last = 16; round = 10;
		}
		else if(size == 48)
		{
			totalSize = 208; inner = 6; last = 24; round = 12;
		}
		else
		{
			totalSize = 240; inner = 8; last = 32; round = 14;
		}
		
		while(exp.length() < totalSize*2)
		{
			for(int j = 0; j < inner; j++)
			{
				String chunk1 = exp.substring(exp.length() - 8, exp.length());
				Byte[] temp1 = new Byte[4]; int index = 0;
				for(int i = 0; i < 4; i++)
				{
					temp1[i] = new Byte(chunk1.substring(index, index + 2));
					index += 2;
				}
				
				if(j == 0)
				{
					temp1 = core(temp1);
				}
				
				if(inner == 8)
				{
					if(j == 4)
					{
						for(int i = 0; i < 4; i++)
						{
							temp1[i] = new Byte(map1.get(temp1[i].dec));
						}
					}
				}
				String chunk2 = exp.substring(exp.length() - last*2, exp.length() - last*2 + 8);
				Byte[] temp2 = new Byte[4]; int index1 = 0;
				for(int i = 0; i < 4; i++)
				{
					temp2[i] = new Byte(chunk2.substring(index1, index1 + 2));
					exp += String.format("%02x", (temp1[i].dec ^ temp2[i].dec));
					index1 += 2;
				}
			}
		}	
		return exp;
	}
}