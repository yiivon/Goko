package org.goko.tools.viewer.jogl.service.overlay;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

import javax.vecmath.Color3f;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.RGBA;
import org.goko.core.common.exception.GkException;
import org.goko.core.common.utils.AbstractIdBean;
import org.goko.tools.viewer.jogl.preferences.JoglViewerPreference;
import org.goko.tools.viewer.jogl.service.JoglSceneManager;
import org.goko.tools.viewer.jogl.utils.overlay.IOverlayRenderer;

/**
 * Overlay to display the current camera 
 * @author Psyko
 */
public class CameraNameOverlay extends AbstractIdBean implements IOverlayRenderer, IPropertyChangeListener {
	/** The jogl scene manager */
	private JoglSceneManager joglSceneManager;
	/** The overlay font */
	private Font overlayFont;
	/** Last frame reset timer */	
	private long lastFrameReset;
	/** The frame counter */
	private int frame;
	/** Computed FPS */
	private int fps;
	
	private RGBA textColor;
	/**
	 * Constructor 
	 */
	public CameraNameOverlay(JoglSceneManager joglSceneManager) {
		this.joglSceneManager =joglSceneManager;
		this.overlayFont = new Font("SansSerif", Font.TRUETYPE_FONT, 13);
		JoglViewerPreference.getInstance().addPropertyChangeListener(this);
		updateTextColor();
	}
	
	/** (inheritDoc)
	 * @see org.goko.tools.viewer.jogl.utils.overlay.IOverlayRenderer#drawOverlayData(java.awt.Graphics2D, java.awt.Rectangle)
	 */
	@Override
	public void drawOverlayData(Graphics2D g2d, Rectangle s) throws GkException {
		
		if(joglSceneManager.getActiveCamera() != null){
//			g2d.setColor(Color.WHITE);
//			g2d.drawLine(s.width/2, s.height/2 - 10, s.width/2, s.height/2 + 10);
//			g2d.drawLine(s.width/2 - 10, s.height/2 , s.width/2 + 10, s.height/2 );
			
			g2d.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			FontRenderContext 	frc = g2d.getFontRenderContext();
			String 				cameraString = joglSceneManager.getActiveCamera().getLabel();
			GlyphVector 		gv = getOverlayFont().createGlyphVector(frc, cameraString);
		    Rectangle 			glyphBounds = gv.getPixelBounds(frc, 0, 0);
		    int x = 5;
		    int y = 5 + glyphBounds.height;
		    g2d.setFont(getOverlayFont());
		    //Color overlayColor = new Color(0.8f,0.8f,0.8f);
		    Color overlayColor = new Color(textColor.rgb.red, textColor.rgb.green, textColor.rgb.blue, textColor.alpha);
		    Color transparentColor = new Color(0,0,0,0);
		    g2d.setBackground(transparentColor);
		    g2d.setColor(overlayColor);
		    if(joglSceneManager.isEnabled()){
		    	g2d.drawString(cameraString,x,y);
		    }else{
		    	g2d.drawString("Disabled",x,y);
		    }
		    if(isShowFps()){
		    	this.frame += 1;
			    if(System.currentTimeMillis() - lastFrameReset >= 500){
			    	this.lastFrameReset = System.currentTimeMillis();
			    	this.fps = this.frame;
			    	this.frame = 0;
			    }
			    g2d.setColor(new Color(0.55f,0.45f,0.28f));
			    g2d.drawString(String.valueOf(this.fps*2)+"fps",x,y+glyphBounds.height+4);
		    }		    
		}
	}

	/**
	 * Detect if FPS should be shown
	 * @return <code>true</code> if the FPS should be displayed, <code>false</code> otherwise
	 */
	private boolean isShowFps() {
		return JoglViewerPreference.getInstance().isShowFps();
	}

	/** (inheritDoc)
	 * @see org.goko.tools.viewer.jogl.utils.overlay.IOverlayRenderer#isOverlayEnabled()
	 */
	@Override
	public boolean isOverlayEnabled() {
		return true;
	}

	/** (inheritDoc)
	 * @see org.goko.tools.viewer.jogl.utils.overlay.IOverlayRenderer#setOverlayEnabled(boolean)
	 */
	@Override
	public void setOverlayEnabled(boolean enabled) {	}
	/**
	 * @return the overlayFont
	 */
	public Font getOverlayFont() {
		return overlayFont;
	}

	/**
	 * @param overlayFont the overlayFont to set
	 */
	public void setOverlayFont(Font overlayFont) {
		this.overlayFont = overlayFont;
	}

	/** (inheritDoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		updateTextColor();
	}
	
	private void updateTextColor(){
		// Display overlay text as complementary color of background
		Color3f color = JoglViewerPreference.getInstance().getBackgroundColor();
		RGB rgbColor = new RGB( (int)(color.x * 255), (int)(color.y * 255), (int)(color.z * 255));
		float[] hsb = rgbColor.getHSB();
		float complementaryHue = (hsb[0] + 180 ) % 360;
		textColor = new RGBA(complementaryHue,  1 - hsb[1], 1 - hsb[2], 255f);		
	}
}
