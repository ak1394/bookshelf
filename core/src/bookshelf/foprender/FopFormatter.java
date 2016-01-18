package bookshelf.foprender;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.fop.apps.Driver;
import org.xml.sax.InputSource;

import bookshelf.book.BookWriter;
import bookshelf.font.Font;

/**
 * This class demonstrates the conversion of an FO file to PDF using FOP.
 */
public class FopFormatter
{
    private Font font;
    
    
    public void setFont(Font font)
    {
        this.font = font;
    }
    
    public void format(File foFile, BookWriter bookWriter) throws Exception
    {
        FileInputStream is = new FileInputStream(foFile);

        Driver driver = new Driver();

        //Setup logger
        Logger logger = new ConsoleLogger(ConsoleLogger.LEVEL_INFO);
        driver.setLogger(logger);
        // FIXME do I need it MessageHandler.setScreenLogger(logger);
        //Setup Renderer (output format)
        FopRenderer fopRenderer = new FopRenderer();
        fopRenderer.setBookWriter(bookWriter);
        fopRenderer.setFont(font);
        driver.setRenderer(fopRenderer);
        driver.setInputSource(new InputSource(is));
        driver.setOutputStream(new ByteArrayOutputStream());
        driver.run();
        is.close();
    }
}