package world;

import app.Application;
import app.Console;
import gfx.Drawing;
import gfx.Tileset;
import input.InputKeyboard;
import input.InputKeyboardKey;
import input.InputMouse;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import state.StateBoard;

public class Board
{
    private String file;
    private String title;
    private int paneX, paneY, paneW, paneH;
    private int sizeX, sizeY;
    private Color background;
    private int scrollX, scrollY;
    private boolean editor;
    
    // Terrain
    private Tile terrain[][];
    private BufferedImage terrainImage;
    private boolean terrainImageReady;
    
    // Temp
    private int tempTickNow, tempTickMax, tempFrameNow, tempFrameMax;
    
    // Entities
    public EntityPlayer entityPlayer;
    private ArrayList<EntityEnemy> entityEnemies;
    private ArrayList<Visual> entityVisuals;
    
    // Vectors
    private ArrayList<Vector> vectors;
    
    // Damage
    private ArrayList<DamageMarker> damageMarkers;
    
    // Input (reference here because they're different objects when running the editor)
    private InputKeyboard inputKeyboard;
    private InputMouse inputMouse;
    
    public Board()
    {
        this.file = "MantasTomb1";
        this.title = "TITLE";
        this.paneX = 11;
        this.paneY = 16;
        this.paneW = 1344;
        this.paneH = 736;
        this.sizeX = 42;
        this.sizeY = 22;
        this.background = Color.BLACK;
        this.terrain = new Tile[this.sizeX][this.sizeY];
        this.setTerrainAll(new Tile());
        this.scrollX = 0;
        this.scrollY = 0;
        this.editor = false;
        
        // Temp
        this.tempTickNow = 0;
        this.tempTickMax = 6;
        this.tempFrameNow = 1;
        this.tempFrameMax = 6;
        
        // Entities: Player
        this.entityPlayer = new EntityPlayer("JAKKEN", this, 512, 256, new Tileset("spr|chr|Jakken", Drawing.getImage("spritesheet/character/Jakken/Jakken.png"), 64, 64, 13, 42));
        
        // Entities: Enemies
        this.entityEnemies = new ArrayList<EntityEnemy>();
        this.entityEnemies.add(new EntityEnemy("SKELETON1", this, 0, 0, new Tileset("spr|crt|Skeleton", Drawing.getImage("spritesheet/creature/Skeleton/Skeleton.png"), 64, 64, 13, 42)));
        
        // Entities: Visual
        this.entityVisuals = new ArrayList<Visual>();
        
        // Vectors
        this.vectors = new ArrayList<Vector>();
        
        // Damage
        this.damageMarkers = new ArrayList<DamageMarker>();
    }
    
    public void addVector(Vector vector)
    {
        this.vectors.add(vector);
    }
    
    public void damageInflict(Damage damage)
    {
        //this.damageMarkers.add(new DamageMarker(damage.getRect()));
        
        // Debug
        DamageMarker dm = new DamageMarker(this, damage.getRect());
        this.damageMarkers.add(dm);
        Console.echoRed("Board -> damageInflict");
        Console.echo("x " + damage.getPosX() + ", y " + damage.getPosY() + ", w " + damage.getSizeX() + ", h " + damage.getSizeY());
        
        for(int x = 0; x < damage.getSizeX(); x++)
        {
            for(int y = 0; y < damage.getSizeY(); y++)
            {
                damageInflictTile(damage, damage.getPosX() + x, damage.getPosY() + y);
            }
        }
    }
    
    public void damageVisual(int amount, int tileX, int tileY)
    {
        this.entityVisuals.add(new VisualDamage(amount, (tileX - this.getScrollTileX()) * 32, (tileY - this.getScrollTileY()) * 32));
    }
    
    private void damageInflictTile(Damage damage, int tileX, int tileY)
    {
        for(int e = 0; e < this.entityEnemies.size(); e++)
        {
            if(this.entityEnemies.get(e).getPosX() == tileX && this.entityEnemies.get(e).getPosY() == tileY)
            {
                if(!this.entityEnemies.get(e).getStatusKO()) {this.entityEnemies.get(e).inflictDamage(damage);}
            }
        }
        
        // Temp cracked wall
        if(tileX == 31 || tileX == 32)
        {
            if(tileY == 13 || tileY == 14)
            {
                Tileset crypt = new Tileset("tst|crypt", Drawing.getImage("tileset/crypt.png"));
                this.terrain[31][13].setImage(crypt.getTileAt(8, 2));
                this.terrain[32][13].setImage(crypt.getTileAt(9, 2));
                this.terrain[31][14].setImage(crypt.getTileAt(8, 3));
                this.terrain[32][14].setImage(crypt.getTileAt(9, 3));
                this.terrain[31][14].setSolid(false);
                this.terrain[32][14].setSolid(false);
                this.redrawTerrain();
                // NOTE: create a gateway
            }
        }
    }
    
    public EntityPlayer getPlayer()
    {
        return this.entityPlayer;
    }
    
    public String getFile()
    {
        return this.getFile(false);
    }
    
    public String getFile(boolean full)
    {
        if(full) {return "Board/" + this.file + ".froth";}
        return this.file;
    }
    
    private int getPaneCols()
    {
        return this.paneW / 32;
    }
    
    private int getPaneRows()
    {
        return this.paneH / 32;
    }
    
    public int getPaneX()
    {
        return this.paneX;
    }
    
    public int getPaneY()
    {
        return this.paneY;
    }
    
    /**
     * @hint returns the exact position to draw to the screen
     * @param boardX coords relative to the board
     * @return 
     */
    public int getScreenPosX(int boardX)
    {
        return boardX - this.scrollX + this.paneX;
    }
    
    /**
     * @hint returns the exact position to draw to the screen
     * @param boardY coords relative to the board
     * @return 
     */
    public int getScreenPosY(int boardY)
    {
        return boardY - this.scrollY + this.paneY;
    }
    
    public int getScrollPosX()
    {
        return this.scrollX * 64;
    }
    
    public int getScrollPosY()
    {
        return this.scrollY * 64;
    }
    
    public int getScrollTileX()
    {
        return this.scrollX;
    }
    
    public int getScrollTileY()
    {
        return this.scrollY;
    }
    
    public int getSizeX()
    {
        return this.sizeX;
    }
    
    public int getSizeY()
    {
        return this.sizeY;
    }
    
    public Tile getTerrain(int tileX, int tileY)
    {
        return this.terrain[tileX][tileY];
    }
    
    public BufferedImage getTerrainImage()
    {
        return this.terrainImage;
    }
    
    public boolean getTileValid(int tileX, int tileY)
    {
        if(this.terrain[tileX][tileY].getSolid()) {return false;}
        // NOTE: iterate through all entities and check if they are on this tile
        for(int e = 0; e < entityEnemies.size(); e++)
        {
            if(entityEnemies.get(e).getPosX() == tileX && entityEnemies.get(e).getPosY() == tileY)
            {
                return false;
            }
        }
        return true;
    }
    
    public String getVectorIntersect(Rectangle rect)
    {
        for(int v = 0; v < vectors.size(); v++)
        {
            if(vectors.get(v).getVector().intersects(rect))
            {
                return vectors.get(v).getRef();
            }
        }
        return null;
    }
    
    public void keyPressed(InputKeyboardKey key)
    {
        this.entityPlayer.keyPressed(key);
    }
    
    public void keyReleased(InputKeyboardKey key)
    {
        this.entityPlayer.keyReleased(key);
    }
    
    public void redrawTerrain()
    {
        this.terrainImageReady = false;
    }
    
    public void render(Graphics gfx)
    {
        this.renderBackground(gfx);
        this.renderTerrain(gfx);
        
        // Development
        this.renderVectors(gfx);
        this.renderDamageMarkers(gfx);
        
        if(!this.editor)
        {
            // Temp
            //gfx.drawImage(new Tileset(Drawing.getImage("spritesheet/character/Jakken/Jakken_Sword5.png"), 192, 192, 6, 4).getTileAt(this.tempFrameNow, 4), 50, 50, null);
            this.entityPlayer.render(gfx);

            if(this.entityEnemies.size() > 0) {this.renderEnemies(gfx);}
            if(this.entityVisuals.size() > 0) {this.renderVisuals(gfx);}
        }
        else {this.renderEditor(gfx);}
    }
    
    private void renderBackground(Graphics gfx)
    {
        gfx.setColor(this.background);
        gfx.fillRect(this.paneX, this.paneY, this.paneW, this.paneH);
    }
    
    private void renderDamageMarkers(Graphics gfx)
    {
        for(int dm = 0; dm < this.damageMarkers.size(); dm++)
        {
            this.damageMarkers.get(dm).render(gfx);
        }
    }
    
    private void renderDisplay(Graphics gfx)
    {
        //
    }
    
    private void renderEditor(Graphics gfx)
    {
        // NOTE: gridlines, solidity, events, etc...
    }
    
    private void renderEnemies(Graphics gfx)
    {
        for(int e = 0; e < this.entityEnemies.size(); e++)
        {
            this.entityEnemies.get(e).render(gfx);
        }
    }
    
    private void renderTerrain(Graphics gfx)
    {
        if(!this.terrainImageReady)
        {
            this.terrainImage = new BufferedImage(this.paneW, this.paneH, BufferedImage.TYPE_BYTE_INDEXED);
            Graphics terrainGfx = terrainImage.createGraphics();
            for(int x = 0; x < this.getPaneCols(); x++)
            {
                for(int y = 0; y < this.getPaneRows(); y++)
                {
                    // If we have tiles for this section of the board
                    if(x + this.getScrollTileX() < this.terrain.length && y + this.getScrollTileY() < this.terrain[x].length)
                    {
                        if(!this.terrain[x + this.getScrollTileX()][y + this.getScrollTileY()].getBlank())
                        {
                            terrainGfx.drawImage(this.terrain[x + this.getScrollTileX()][y + this.getScrollTileY()].getImage(), x * 32, y * 32, null);
                        }
                    }
                }
            }
            this.terrainImageReady = true;
        }
        gfx.drawImage(this.terrainImage, this.paneX, this.paneY, null);
    }
    
    private void renderVectors(Graphics gfx)
    {
        for(int v = 0; v < this.vectors.size(); v++)
        {
            this.vectors.get(v).render(gfx);
        }
    }
    
    private void renderVisuals(Graphics gfx)
    {
        for(int v = 0; v < this.entityVisuals.size(); v++)
        {
            this.entityVisuals.get(v).render(gfx);
        }
    }
    
    public void setBackground(Color color)
    {
        this.background = color;
    }
    
    public void setEditor(boolean value)
    {
        this.editor = value;
    }
    
    public void setInput(InputKeyboard keyboard, InputMouse mouse)
    {
        this.inputKeyboard = keyboard;
        this.inputMouse = mouse;
    }
    
    public void setPane(int posX, int posY, int sizeX, int sizeY)
    {
        this.paneX = posX;
        this.paneY = posY;
        this.paneW = sizeX;
        this.paneH = sizeY;
    }
    
    public void setScroll(int posX, int posY)
    {
        this.scrollX = posX;
        this.scrollY = posY;
    }
    
    public void setScrollPlayer()
    {
        this.scrollX = entityPlayer.getPosX() - (this.paneW / 2);
        this.scrollY = entityPlayer.getPosY() - (this.paneH / 2);
    }
    
    public void setTerrain(int posX, int posY, Tile tile)
    {
        this.terrain[posX][posY] = tile.clone();
    }
    
    public void setTerrainAll(Tile tile)
    {
        for(int x = 0; x < this.sizeX; x++)
        {
            for(int y = 0; y < this.sizeY; y++)
            {
                this.setTerrain(x, y, tile);
            }
        }
    }
    
    public void tick()
    {
        // Temp
        this.tempTickNow += 1;
        if(this.tempTickNow > this.tempTickMax)
        {
            this.tempTickNow = 0;
            this.tempFrameNow += 1;
            if(this.tempFrameNow > this.tempFrameMax)
            {
                this.tempFrameNow = 1;
            }
        }
        
        // Entities
        if(!this.editor) {this.tickEntity();}
        else {this.tickEditor();}
        
        // NOTE: animated scenery?
    }
    
    private void tickEditor()
    {
        // NOTE: may not need this
    }
    
    private void tickEntity()
    {
        // Player
        this.entityPlayer.tick();

        // Enemies
        this.tickEnemies();

        // Visuals
        this.tickVisuals();
        
        // Damage Markers
        this.tickDamageMarkers();
    }
    
    public void tickDamageMarkers()
    {
        for(int dm = 0; dm < this.damageMarkers.size(); dm++)
        {
            if(this.damageMarkers.get(dm).getDone()) {this.damageMarkers.remove(dm);}
            else {this.damageMarkers.get(dm).tick();}
        }
    }
    
    public void tickEnemies()
    {
        for(int e = 0; e < this.entityEnemies.size(); e++)
        {
            if(this.entityEnemies.get(e).getStatusKO()) {this.entityEnemies.remove(e);}
            else {this.entityEnemies.get(e).tick();}
        }
    }
    
    private void tickVisuals()
    {
        for(int v = 0; v < this.entityVisuals.size(); v++)
        {
            if(this.entityVisuals.get(v).getDone()) {this.entityVisuals.remove(v);}
            else {this.entityVisuals.get(v).tick();}
        }
    }

}