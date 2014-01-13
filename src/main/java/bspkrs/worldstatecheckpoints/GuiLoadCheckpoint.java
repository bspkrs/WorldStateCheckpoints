package bspkrs.worldstatecheckpoints;

import java.io.File;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.util.StatCollector;
import bspkrs.helpers.client.MinecraftHelper;
import bspkrs.helpers.client.gui.GuiScreenWrapper;

public class GuiLoadCheckpoint extends GuiScreenWrapper
{
    String                      guiTitle       = StatCollector.translateToLocal("wsc.loadCheckpoint.title");
    String                      guiSubTitle    = StatCollector.translateToLocal("wsc.loadCheckpoint.title2");
    
    protected boolean           showDelButtons = true;
    private boolean             gameOverScreen = false;
    
    private GuiButton[]         buttons;
    private GuiButton[]         delButtons;
    protected String[]          dirNames;
    private int[]               pageNums;
    private int                 pages          = 0;
    protected int               currentPage    = 0;
    protected int               startPage      = 0;
    
    protected CheckpointManager cpm;
    
    private GuiButton           back, prev, next, switchLoad;
    protected boolean           isAutoCheckpointsLoad;
    
    public GuiLoadCheckpoint(CheckpointManager cpm, boolean gameover, boolean isAutoCheckpointsLoad)
    {
        this.cpm = cpm;
        gameOverScreen = gameover;
        this.isAutoCheckpointsLoad = isAutoCheckpointsLoad;
    }
    
    public GuiLoadCheckpoint()
    {
        gameOverScreen = false;
        this.isAutoCheckpointsLoad = false;
    }
    
    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void initGui()
    {
        buttonList().clear();
        
        byte byte0 = -16;
        int prevX, backX, nextX, switchX;
        
        prevX = width() / 2 - 70 - 60 - 3;
        backX = width() / 2 - 70 - 1;
        switchX = width() / 2 + 1;
        nextX = width() / 2 + 70 + 3;
        
        if (this.isAutoCheckpointsLoad)
        {
            guiTitle = StatCollector.translateToLocal("wsc.loadCheckpoint.titleAutoSaves");
            switchLoad = new GuiButton(-4, switchX, height() / 4 + 24 + byte0, 70, 20, StatCollector.translateToLocal("wsc.loadCheckpoint.checkpoints"));
        }
        else
        {
            switchLoad = new GuiButton(-4, switchX, height() / 4 + 24 + byte0, 70, 20, StatCollector.translateToLocal("wsc.loadCheckpoint.autoSaves"));
        }
        
        switchLoad.field_146124_l = cpm.getHasCheckpoints(!isAutoCheckpointsLoad);
        
        prev = new GuiButton(-2, prevX, height() / 4 + 24 + byte0, 60, 20, "<<<");
        back = new GuiButton(-1, backX, height() / 4 + 24 + byte0, 70, 20, StatCollector.translateToLocal("gui.back"));
        next = new GuiButton(-3, nextX, height() / 4 + 24 + byte0, 60, 20, ">>>");
        
        field_146292_n.add(back);
        field_146292_n.add(switchLoad);
        field_146292_n.add(prev);
        field_146292_n.add(next);
        
        File[] files = cpm.getCheckpoints(isAutoCheckpointsLoad);
        
        dirNames = new String[files.length];
        pageNums = new int[files.length];
        buttons = new GuiButton[files.length];
        delButtons = new GuiButton[files.length];
        
        int page = 0;
        int pagecounter = -1; // will be turned to 0 in first cycle
        int index = -1; // will be turned to 0 in first cycle
        
        for (File file : files)
        {
            if (!file.isDirectory())
                continue;
            
            String label;
            
            try
            {
                label = file.getName().split("!", 2)[1];
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                label = file.getName();
            }
            
            index++;
            pagecounter++;
            
            if (pagecounter >= 5)
            {
                page++;
                pagecounter = 0;
            }
            
            GuiButton btn = new GuiButton(index, width() / 2 - 100, height() / 4 + 24 * 2 + 6 + 23 * pagecounter + byte0, label);
            field_146292_n.add(btn);
            
            GuiButton delbtn = new GuiButton(index + 1000, width() / 2 + 100 + 4, height() / 4 + 24 * 2 + 6 + 23 * pagecounter + byte0, 20, 20, "X");
            field_146292_n.add(delbtn);
            
            dirNames[index] = file.getName();
            pageNums[index] = page;
            buttons[index] = btn;
            delButtons[index] = delbtn;
        }
        
        pages = page;
        currentPage = startPage;
        showPage(currentPage);
    }
    
    void showPage(int page)
    {
        currentPage = page;
        
        if (currentPage < 0)
            currentPage = 0;
        if (currentPage > pages)
            currentPage = pages;
        
        for (int i = 0; i < buttons.length; i++)
        {
            buttons[i].field_146125_m = (pageNums[i] == page);
            delButtons[i].field_146125_m = (pageNums[i] == page && showDelButtons);
        }
        
        prevNextDisableIfNeeded();
    }
    
    private void prevNextDisableIfNeeded()
    {
        prev.field_146124_l = currentPage > 0;
        next.field_146124_l = currentPage < pages;
    }
    
    private void goPrev()
    {
        if (currentPage > 0)
        {
            currentPage--;
            showPage(currentPage);
        }
    }
    
    private void goNext()
    {
        if (currentPage < pages)
        {
            currentPage++;
            showPage(currentPage);
        }
    }
    
    protected void checkpointButtonClicked(int index)
    {
        String dirname = dirNames[index];
        cpm.loadCheckpoint(dirname, isAutoCheckpointsLoad);
        MinecraftHelper.displayGuiScreen(WSCSettings.mc, null);
        WSCSettings.mc.setIngameFocus();
        WSCSettings.justLoadedCheckpoint = true;
        WSCSettings.loadMessage = StatCollector.translateToLocalFormatted("wsc.chatMessage.loadedCheckpoint", dirname.split("!", 2)[1]);
    }
    
    protected void delButtonClicked(int index)
    {
        MinecraftHelper.displayGuiScreen(WSCSettings.mc, new GuiDeleteCheckpointYesNo(cpm, this, dirNames[index], currentPage, isAutoCheckpointsLoad));
    }
    
    protected void backButtonClicked()
    {
        MinecraftHelper.displayGuiScreen(WSCSettings.mc, gameOverScreen ? new GuiGameOver() : new GuiCheckpointsMenu(cpm));
    }
    
    protected void switchButtonClicked()
    {
        MinecraftHelper.displayGuiScreen(WSCSettings.mc, new GuiLoadCheckpoint(cpm, gameOverScreen, !isAutoCheckpointsLoad));
    }
    
    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    @Override
    protected void func_146284_a(GuiButton guibutton)
    {
        if (!guibutton.field_146124_l)
            return;
        
        switch (guibutton.field_146127_k)
        {
            case -1:
                backButtonClicked();
                return;
                
            case -2:
                goPrev();
                return;
                
            case -3:
                goNext();
                return;
                
            case -4:
                switchButtonClicked();
                return;
                
            default:
                if (guibutton.field_146127_k >= 1000)
                    delButtonClicked(guibutton.field_146127_k - 1000);
                else
                    checkpointButtonClicked(guibutton.field_146127_k);
                
                break;
        }
    }
    
    /**
     * Called from the main game loop to update the screen.
     */
    @Override
    public void updateScreen()
    {
        super.updateScreen();
    }
    
    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
        drawDefaultBackground();
        if (guiSubTitle == null || (gameOverScreen && WSCSettings.mc.theWorld.getWorldInfo().isHardcoreModeEnabled()))
            drawCenteredString(field_146289_q, guiTitle, width() / 2, 50 + 5, 0xffffff);
        else
        {
            drawCenteredString(field_146289_q, guiSubTitle, width() / 2, 50 + 5, 0xee0000);
            drawCenteredString(field_146289_q, guiTitle, width() / 2, 50 + 5 - 16, 0xffffff);
        }
        super.drawScreen(par1, par2, par3);
    }
}