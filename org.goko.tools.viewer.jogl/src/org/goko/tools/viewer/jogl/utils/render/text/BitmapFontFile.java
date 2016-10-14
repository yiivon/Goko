package org.goko.tools.viewer.jogl.utils.render.text;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.media.opengl.GL3;

import org.goko.core.common.exception.GkException;
import org.goko.core.common.exception.GkTechnicalException;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;

/**
 * Bitmap font file parser 
 * 
 * Documentation 
 * http://www.angelcode.com/products/bmfont/doc/file_format.html
 * 
 * @author Psyko
 */
public class BitmapFontFile {
	/** Character info */
	private Map<Integer, CharBlock> mapChars;
	/** Pages */
	private Map<Integer, PageBlock> mapPages;
	/** Total width of the texture */
	private int textureWidth;
	/** Total height of the texture */
	private int textureHeight;
	/** The texture itself */
	private Texture texture;
	/** Byte buffer for the texture*/
	private ByteBuffer buffer;
	/** Base height of the font */
	private int base;
	/** Lineheight of the font */
	private int lineHeight;
		
	protected CharBlock getCharacterInfo(char character){
		return mapChars.get((int)character);
	}
	
	protected void load(String bffFileName) throws GkException{

		try{
			URL url = new URL("platform:/plugin/org.goko.tools.viewer.jogl"+bffFileName);
			InputStream inputStream = url.openConnection().getInputStream();
			
			mapChars = new HashMap<Integer, CharBlock>();
			mapPages = new HashMap<Integer, PageBlock>();
			
			loadFileIdentifier(inputStream);
			loadInfoBlock(inputStream);
			loadCommonBlock(inputStream);
			loadPagesBlock(inputStream);
			loadCharsBlock(inputStream);
			
			
			
		}catch(Exception e){
			throw new GkTechnicalException(e);
		}
	}
	
	protected void loadBuffer(URL urlFile) throws IOException{
		 // open image		 
		 BufferedImage bufferedImage = ImageIO.read(urlFile);

		 // get DataBufferBytes from Raster
		 WritableRaster raster = bufferedImage .getRaster();
		 DataBufferByte data   = (DataBufferByte) raster.getDataBuffer();

		 buffer = ByteBuffer.wrap(data.getData());
		 
	}
	
	/**
	 * Loads the file identifier
	 * @param inputStream the {@link InputStream}
	 * @throws IOException IOException
	 */
	private void loadFileIdentifier(InputStream inputStream) throws IOException {
		byte[] identifier = new byte[3];
		inputStream.read(identifier);
		//System.out.println("File identifier : "+ String.valueOf(identifier));
		byte[] version = new byte[1];
		inputStream.read(version);
		//System.out.println("File version : "+ String.valueOf((int)version[0]));
	}

	/**
	 * Loads data about the information in the bitmap file
	 * @param inputStream the {@link InputStream}
	 * @throws IOException IOException
	 */
	private void loadInfoBlock(InputStream inputStream) throws IOException {
		ByteBuffer data = getBlockData(inputStream);
	}

	/**
	 * Loads commons data in the bitmap file
	 * @param inputStream the {@link InputStream}
	 * @throws IOException IOException
	 */
	private void loadCommonBlock(InputStream inputStream) throws IOException { 
		ByteBuffer data = getBlockData(inputStream);
//		lineHeight 2 uint 0  
//		base 2 uint 2  
//		scaleW 2 uint 4  
//		scaleH 2 uint 6  
//		pages 2 uint 8  
//		bitField 1 bits 10 bits 0-6: reserved, bit 7: packed 
//		alphaChnl 1 uint 11  
//		redChnl 1 uint 12  
//		greenChnl 1 uint 13  
//		blueChnl 1 uint 14 
		lineHeight = getUint16(data);
		base = getUint16(data);
		textureWidth  = getUint16(data);
		textureHeight = getUint16(data);
	}
	
	/**
	 * Loads data about the pages in the bitmap file
	 * @param inputStream the {@link InputStream}
	 * @throws IOException IOException
	 */
	private void loadPagesBlock(InputStream inputStream) throws IOException {
		ByteBuffer data = getBlockData(inputStream);
		int size = data.limit();
		int id = 0;
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < size; i++) {
			byte c = data.get();
			if(c == 0){
				// Page detected
				mapPages.put(id, new PageBlock(id, buffer.toString()));
				//System.out.println("Detected image "+buffer.toString());
				URL url = new URL("platform:/plugin/org.goko.tools.viewer.jogl/resources/font/"+mapPages.get(id).getFile());
				loadBuffer(url);
				id++;
				buffer.setLength(0);
			}else{
				buffer.append((char)c);
			}
		}
	}
	
	/**
	 * Loads data about the characters in the bitmap file
	 * @param inputStream the {@link InputStream}
	 * @throws IOException IOException
	 */
	private void loadCharsBlock(InputStream inputStream) throws IOException {
		ByteBuffer data = getBlockData(inputStream);
		int nbChars = data.limit() / 20;
		
		for (int i = 0; i < nbChars; i++) {
			int id = getUint32(data);
			int x = getUint16(data);
			int y = getUint16(data);
			int width  = getUint16(data);
			int height = getUint16(data);
			int xOffset = getInt16(data);
			int yOffset = getInt16(data);
			int xAdvance= getInt16(data);
			int page = getUint8(data);
			int chnl = getUint8(data);
			mapChars.put(id, new CharBlock(id, x, y, width, height, xOffset, yOffset, xAdvance, page));
			//System.out.println((char)id+","+id+", "+x+", "+ y+", "+ width+", "+ height+", "+ xOffset+", "+ yOffset+", "+ xAdvance+", "+page);
		}
	}
	
	/**
	 * Builds the ByteBuffer using the bloc kdata information at the beginning of the given input stream
	 * @param inputStream the input stream
	 * @return ByteBuffer
	 * @throws IOException IOException
	 */
	private ByteBuffer getBlockData(InputStream inputStream) throws IOException{
		byte[] blockHeader = new byte[5];
		inputStream.read(blockHeader);
		ByteBuffer data = ByteBuffer.wrap(blockHeader);
		int blockId = getUint8(data);
		//System.out.println("Block id : "+ String.valueOf(blockId));		
		
		
		int length = getUint32(data);
		//System.out.println("Block length : "+ String.valueOf(length));
		
		byte[] blockData = new byte[length];
		inputStream.read(blockData); 
		return ByteBuffer.wrap(blockData);
	}
	
	/**
	 * Extract a signed int in this buffer using the 4 first bytes
	 * @param buffer the buffer to extract data from
	 * @return an int
	 */
	public static int getInt32(ByteBuffer buffer) {
		byte[] val = new byte[4];
		buffer.get(val);
		return val[3] << 24 | val[2] << 16 | val[1] << 8 | val[0];		
	}
	
	/**
	 * Extract a signed unsigned int in this buffer using the 4 first bytes
	 * @param buffer the buffer to extract data from
	 * @return an int
	 */
	private int getUint32(ByteBuffer buffer) {
		byte[] val = new byte[4];
		buffer.get(val);
		return (val[3] & 0xFFFFFFFF) << 24 | (val[2]& 0xFFFFFF) << 16 | (val[1]& 0xFFFF) << 8 | (val[0] & 0xFF);		
	}
	
	/**
	 * Extract a signed int in this buffer using the 2 first bytes
	 * @param buffer the buffer to extract data from
	 * @return an int
	 */
	private int getInt16(ByteBuffer buffer) {
		byte[] val = new byte[2];
		buffer.get(val);
		return val[1] << 8 | val[0];		
	}
	
	/**
	 * Extract an unsigned int in this buffer using the 2 first bytes
	 * @param buffer the buffer to extract data from
	 * @return an int
	 */
	private int getUint16(ByteBuffer buffer) {		
		byte[] val = new byte[2];
		buffer.get(val);
		return (val[1]& 0xFFFF) << 8 | (val[0] & 0xFF);		
	}
	
	/**
	 * Extract a signed int in this buffer using the 1 first byte
	 * @param buffer the buffer to extract data from
	 * @return an int
	 */
	private int getInt8(ByteBuffer buffer) {		
		return buffer.get();		
	}
	
	/**
	 * Extract a unsigned int in this buffer using the 2 first bytes
	 * @param buffer the buffer to extract data from
	 * @return an int
	 */
	private int getUint8(ByteBuffer buffer) {		
		return buffer.get() & 0xFF;
	}
	
	/**
	 * Bodge to get unsigned byte values
	 * @param val the value
	 * @return the uint value
	 */
	private int getUnsignedByteVal(byte val) {
		return val & 0xFF;
	}

	/**
	 * @return the textureWidth
	 */
	public int getTextureWidth() {
		return textureWidth;
	}

	/**
	 * @param textureWidth the textureWidth to set
	 */
	public void setTextureWidth(int textureWidth) {
		this.textureWidth = textureWidth;
	}

	/**
	 * @return the textureHeight
	 */
	public int getTextureHeight() {
		return textureHeight;
	}

	/**
	 * @param textureHeight the textureHeight to set
	 */
	public void setTextureHeight(int textureHeight) {
		this.textureHeight = textureHeight;
	}

	
	/**
	 * @param gl
	 * @return
	 */
	public Texture getTexture(GL3 gl) {
		if(texture == null){
			TextureData tData = new TextureData(gl.getGLProfile(),    //GLProfile glp,
					   GL3.GL_RGBA,     	  //int internalFormat,
			           getTextureWidth(),     //int width,
			           getTextureHeight(), 	  //int height,
			           0,                     //int border,
			           GL3.GL_RGBA,           //int pixelFormat,
			           GL3.GL_UNSIGNED_BYTE,  //int pixelType,
			           false,                 //boolean mipmap,
			           false,                 //boolean dataIsCompressed,
			           false,                 //boolean mustFlipVertically,
			           getBuffer(),		       //Buffer buffer,
			           null);                 //TextureData.Flusher flusher)
			texture = new Texture(gl, tData);
		}
		return texture;
	}

	/**
	 * @return the buffer
	 */
	public ByteBuffer getBuffer() {
		return buffer;
	}

	/**
	 * @return the base
	 */
	public int getBase() {
		return base;
	}

	/**
	 * @param base the base to set
	 */
	public void setBase(int base) {
		this.base = base;
	}

	/**
	 * @return the lineHeight
	 */
	public int getLineHeight() {
		return lineHeight;
	}

	/**
	 * @param lineHeight the lineHeight to set
	 */
	public void setLineHeight(int lineHeight) {
		this.lineHeight = lineHeight;
	}
}
