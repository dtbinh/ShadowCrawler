package menu;

import app.Application;
import app.Console;
import gfx.Drawing;
import gfx.Theme;
import input.InputKeyboard;
import input.InputKeyboardKey;
import java.awt.Color;
import java.awt.Graphics;
import state.StatePause;

public abstract class Menu
{
    // State
    public StatePause parent;
    
    // Options
    public int optCursor, optCursorTickNow, optCursorTickMax, optCursorFrameNow, optCursorFrameMax;
    public String[] optString;
    
    // Input
    public InputKeyboard keyboard;
    private boolean keyboardListen;
    
    public Menu(StatePause parent)
    {
        this.parent = parent;
        
        // Options
        this.optCursor = 0;
        this.optCursorTickNow = 0;
        this.optCursorTickMax = 2;
        this.optCursorFrameNow = 1;
        this.optCursorFrameMax = 8;
        this.initOptions();
        
        // Input
        this.keyboard = Application.getInputKeyboard();
        this.keyboardListen = true;
    }
    
    public StatePause getState()
    {
        return this.parent;
    }
    
    public void keyPressed(InputKeyboardKey key)
    {
        if(this.keyboardListen)
        {
            if(key.getRef().equals("ENTER") || key.getRef().equals("SPACE")) {this.tickSelect();}
            if(key.getRef().equals("UP")) {if(this.optCursor > 1) {this.optCursor -= 1;}}
            if(key.getRef().equals("DOWN")) {if(this.optCursor < this.optString.length) {this.optCursor += 1;}}
        }
    }
    
    public void keyReleased(InputKeyboardKey key)
    {
        if(this.keyboardListen)
        {
            //
        }
    }
    
    public abstract void initOptions();
    
    public abstract void render(Graphics gfx);
    
    public void renderFrame(Graphics gfx)
    {
        // Pane
        Drawing.drawImageOpaque(gfx, Drawing.getImage("interface/menuPane2bkg.png"), 0, 0, 0.8f);
        gfx.drawImage(Drawing.getImage("interface/menuPane2border.png"), 0, 0, null);
        
        // Header
        gfx.setFont(Theme.getFont("MENUHEADER"));
        gfx.setColor(Color.BLACK);
        // NOTE: change the colour and add text shadow, while keeping central alignment
        Drawing.write(gfx, "PAUSE", 683, 90, "CENTER");
        gfx.setColor(Color.WHITE);
        Drawing.write(gfx, "PAUSE", 681, 88, "CENTER");
    }
    
    public void tick()
    {
        this.optCursorTickNow += 1;
        if(this.optCursorTickNow > this.optCursorTickMax)
        {
            this.optCursorTickNow = 0;
            this.optCursorFrameNow += 1;
            if(this.optCursorFrameNow > this.optCursorFrameMax)
            {
                this.optCursorFrameNow = 1;
            }
        }
    }
    
    public abstract void tickSelect();
    
}